package com.project.service;

import com.project.digitaltwin.service.IntelligentReportService;
import com.project.entity.IncidentReport;
import com.project.entity.ThreatHistory;
import com.project.entity.User;
import com.project.repository.IncidentReportRepository;
import com.project.repository.ThreatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final IncidentReportRepository incidentReportRepository;
    private final ThreatHistoryRepository threatHistoryRepository;
    private final AlertService alertService;
    private final FeedbackLearningService feedbackLearningService;
    private final IntelligentReportService intelligentReportService;

    // ==========================================
    // SUBMIT NEW INCIDENT REPORT (STUDENT)
    // ==========================================
    @Transactional
    public IncidentReport submitReport(
            User student,
            String title,
            String description,
            IncidentReport.IncidentType incidentType,
            String suspiciousUrl,
            String suspiciousEmail) {

        // Create new report
        IncidentReport report = new IncidentReport();
        report.setReportedBy(student);
        report.setTitle(title);
        report.setDescription(description);
        report.setIncidentType(incidentType);
        report.setSuspiciousUrl(suspiciousUrl);
        report.setSuspiciousEmail(suspiciousEmail);
        report.setType("MANUAL");
        report.setTextContent(description);
        report.setStatus(IncidentReport.ReportStatus.PENDING);
        report.setAlertStatus(IncidentReport.AlertStatus.NONE);
        report.setVerificationStatus(
                IncidentReport.VerificationStatus.PENDING_ADMIN
        );

        // Run AI risk scoring
        report.calculateRiskScore();

        // Save report
        IncidentReport savedReport = incidentReportRepository.save(report);

        // Log to threat history
        ThreatHistory history = ThreatHistory.forIncident(
                ThreatHistory.ActionType.REPORT_SUBMITTED,
                student,
                savedReport,
                null,
                IncidentReport.ReportStatus.PENDING.name(),
                "New incident report submitted by: "
                        + student.getEmail()
                        + " | Title: " + title
                        + " | Risk Level: " + savedReport.getRiskLevel()
                        + " | Risk Score: " + savedReport.getRiskScore()
        );
        threatHistoryRepository.save(history);

        return savedReport;
    }

    // ==========================================
    // GET ALL REPORTS (ADMIN)
    // ==========================================
    public List<IncidentReport> getAllReports() {
        return incidentReportRepository.findAllByOrderByCreatedAtDesc();
    }

    // ==========================================
    // GET REPORT BY ID
    // ==========================================
    public IncidentReport getReportById(Long id) {
        return incidentReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Report not found with id: " + id));
    }

    // ==========================================
    // GET REPORTS BY STUDENT
    // ==========================================
    public List<IncidentReport> getReportsByStudent(User student) {
        return incidentReportRepository
                .findByReportedByOrderByCreatedAtDesc(student);
    }

    // ==========================================
    // GET REPORTS BY STATUS
    // ==========================================
    public List<IncidentReport> getReportsByStatus(
            IncidentReport.ReportStatus status) {
        return incidentReportRepository.findByStatus(status);
    }

    // ==========================================
    // GET REPORTS BY RISK LEVEL
    // ==========================================
    public List<IncidentReport> getReportsByRiskLevel(
            IncidentReport.RiskLevel riskLevel) {
        return incidentReportRepository.findByRiskLevel(riskLevel);
    }

    // ==========================================
    // GET REPORTS BY INCIDENT TYPE
    // ==========================================
    public List<IncidentReport> getReportsByIncidentType(
            IncidentReport.IncidentType incidentType) {
        return incidentReportRepository.findByIncidentType(incidentType);
    }

    // ==========================================
    // GET HIGH RISK PENDING REPORTS (ADMIN)
    // ==========================================
    public List<IncidentReport> getHighRiskPendingReports() {
        return incidentReportRepository.findHighRiskPendingReports();
    }

    // ==========================================
    // GET RECENT REPORTS (LAST 7 DAYS)
    // ==========================================
    public List<IncidentReport> getRecentReports() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return incidentReportRepository.findRecentReports(sevenDaysAgo);
    }

    // ==========================================
    // GET REPORTS BY DATE RANGE
    // ==========================================
    public List<IncidentReport> getReportsByDateRange(
            LocalDateTime startDate,
            LocalDateTime endDate) {
        return incidentReportRepository.findByDateRange(startDate, endDate);
    }

    // ==========================================
    // SEARCH REPORTS BY KEYWORD
    // ==========================================
    public List<IncidentReport> searchReports(String keyword) {
        return incidentReportRepository.searchByKeyword(keyword);
    }

    // ==========================================
    // GET REPORTS BY INSTITUTION
    // ==========================================
    public List<IncidentReport> getReportsByInstitution(
            String institutionName) {
        return incidentReportRepository.findByInstitution(institutionName);
    }

    // ==========================================
    // START REVIEW (ADMIN)
    // ==========================================
    @Transactional
    public IncidentReport startReview(Long reportId, User admin) {

        IncidentReport report = getReportById(reportId);
        String previousStatus = report.getStatus().name();

        // Validate current status
        if (report.getStatus() != IncidentReport.ReportStatus.PENDING) {
            throw new RuntimeException(
                    "Only PENDING reports can be moved to UNDER_REVIEW");
        }

        // Update status
        report.setStatus(IncidentReport.ReportStatus.UNDER_REVIEW);
        report.setVerifiedBy(admin);
        report.setAdminDecision(IncidentReport.AdminDecision.MONITOR);
        report.setReviewTime(LocalDateTime.now());
        report.setVerificationStatus(
                IncidentReport.VerificationStatus.PENDING_ADMIN
        );
        IncidentReport updatedReport = incidentReportRepository.save(report);

        // Log to threat history
        ThreatHistory history = ThreatHistory.forIncident(
                ThreatHistory.ActionType.ADMIN_REVIEW_STARTED,
                admin,
                updatedReport,
                previousStatus,
                IncidentReport.ReportStatus.UNDER_REVIEW.name(),
                "Admin started review: " + admin.getEmail()
                        + " | Report ID: " + reportId
        );
        threatHistoryRepository.save(history);

        return updatedReport;
    }

    // ==========================================
    // VERIFY REPORT (ADMIN)
    // ==========================================
    @Transactional
    public IncidentReport verifyReport(Long reportId,
                                       User admin,
                                       String adminRemarks) {

        IncidentReport report = getReportById(reportId);
        String previousStatus = report.getStatus().name();

        // Validate current status
        if (report.getStatus() != IncidentReport.ReportStatus.UNDER_REVIEW) {
            throw new RuntimeException(
                    "Only UNDER_REVIEW reports can be VERIFIED");
        }

        // Update report
        report.setStatus(IncidentReport.ReportStatus.VERIFIED);
        report.setVerifiedBy(admin);
        report.setAdminRemarks(adminRemarks);
        report.setVerifiedAt(LocalDateTime.now());
        report.setReviewTime(LocalDateTime.now());
        report.setAdminDecision(IncidentReport.AdminDecision.CONFIRM_THREAT);
        report.setVerificationStatus(
                IncidentReport.VerificationStatus.APPROVED
        );
        boolean highOrCritical =
                report.getRiskLevel() == IncidentReport.RiskLevel.HIGH
                        || report.getRiskLevel() == IncidentReport.RiskLevel.CRITICAL;
        report.setAlertStatus(highOrCritical
                ? IncidentReport.AlertStatus.OFFICIAL
                : IncidentReport.AlertStatus.NONE);
        IncidentReport verifiedReport = incidentReportRepository.save(report);

        if (highOrCritical) {
            alertService.promoteProvisionalAlert(admin, verifiedReport);
        } else {
            alertService.rejectProvisionalAlert(
                    admin,
                    verifiedReport,
                    "Risk below HIGH threshold. No public alert required."
            );
        }

        intelligentReportService.applyRiskBasedBlocking(
                verifiedReport,
                admin,
                true
        );

        if (verifiedReport.getIncidentType() == IncidentReport.IncidentType.PHISHING_EMAIL) {
            feedbackLearningService.appendConfirmedThreat(verifiedReport);
        }

        // Log to threat history
        ThreatHistory history = ThreatHistory.forIncident(
                ThreatHistory.ActionType.REPORT_VERIFIED,
                admin,
                verifiedReport,
                previousStatus,
                IncidentReport.ReportStatus.VERIFIED.name(),
                "Report verified by admin: " + admin.getEmail()
                        + " | Remarks: " + adminRemarks
        );
        threatHistoryRepository.save(history);

        return verifiedReport;
    }

    // ==========================================
    // DISMISS REPORT (ADMIN)
    // ==========================================
    @Transactional
    public IncidentReport dismissReport(Long reportId,
                                        User admin,
                                        String adminRemarks) {

        IncidentReport report = getReportById(reportId);
        String previousStatus = report.getStatus().name();

        // Validate — cannot dismiss already verified reports
        if (report.getStatus() == IncidentReport.ReportStatus.VERIFIED) {
            throw new RuntimeException(
                    "Cannot dismiss an already VERIFIED report");
        }

        // Update report
        report.setStatus(IncidentReport.ReportStatus.DISMISSED);
        report.setVerifiedBy(admin);
        report.setAdminRemarks(adminRemarks);
        report.setVerifiedAt(LocalDateTime.now());
        report.setReviewTime(LocalDateTime.now());
        report.setAdminDecision(IncidentReport.AdminDecision.MARK_SAFE);
        report.setVerificationStatus(
                IncidentReport.VerificationStatus.REJECTED
        );
        report.setAlertStatus(IncidentReport.AlertStatus.REJECTED);
        IncidentReport dismissedReport = incidentReportRepository.save(report);

        alertService.rejectProvisionalAlert(admin, dismissedReport, adminRemarks);

        // Log to threat history
        ThreatHistory history = ThreatHistory.forIncident(
                ThreatHistory.ActionType.REPORT_DISMISSED,
                admin,
                dismissedReport,
                previousStatus,
                IncidentReport.ReportStatus.DISMISSED.name(),
                "Report dismissed by admin: " + admin.getEmail()
                        + " | Remarks: " + adminRemarks
        );
        threatHistoryRepository.save(history);

        return dismissedReport;
    }

    // ==========================================
    // MONITOR REPORT (ADMIN)
    // ==========================================
    @Transactional
    public IncidentReport monitorReport(Long reportId,
                                        User admin,
                                        String adminRemarks) {

        IncidentReport report = getReportById(reportId);
        String previousStatus = report.getStatus().name();

        if (report.getStatus() == IncidentReport.ReportStatus.VERIFIED
                || report.getStatus() == IncidentReport.ReportStatus.DISMISSED) {
            throw new RuntimeException(
                    "Only active reports can be marked for monitoring");
        }

        report.setStatus(IncidentReport.ReportStatus.UNDER_REVIEW);
        report.setVerifiedBy(admin);
        report.setAdminRemarks(adminRemarks);
        report.setReviewTime(LocalDateTime.now());
        report.setAdminDecision(IncidentReport.AdminDecision.MONITOR);
        report.setVerificationStatus(IncidentReport.VerificationStatus.MONITORING);
        if (report.getRiskLevel() == IncidentReport.RiskLevel.LOW
                || report.getRiskLevel() == IncidentReport.RiskLevel.MEDIUM) {
            report.setAlertStatus(IncidentReport.AlertStatus.NONE);
            alertService.rejectProvisionalAlert(
                    admin,
                    report,
                    "Monitoring decision for non-high risk incident."
            );
        }
        IncidentReport monitoredReport = incidentReportRepository.save(report);

        ThreatHistory history = ThreatHistory.forIncident(
                ThreatHistory.ActionType.ADMIN_REVIEW_STARTED,
                admin,
                monitoredReport,
                previousStatus,
                IncidentReport.ReportStatus.UNDER_REVIEW.name(),
                "Admin marked report for MONITORING: " + admin.getEmail()
                        + " | Remarks: " + adminRemarks
        );
        threatHistoryRepository.save(history);

        return monitoredReport;
    }

    // ==========================================
    // ADD ADMIN REMARKS
    // ==========================================
    @Transactional
    public IncidentReport addAdminRemarks(Long reportId,
                                          User admin,
                                          String remarks) {

        IncidentReport report = getReportById(reportId);
        report.setAdminRemarks(remarks);
        IncidentReport updatedReport = incidentReportRepository.save(report);

        // Log to threat history
        ThreatHistory history = ThreatHistory.forIncident(
                ThreatHistory.ActionType.ADMIN_REMARKS_ADDED,
                admin,
                updatedReport,
                null,
                null,
                "Admin added remarks: " + admin.getEmail()
                        + " | Remarks: " + remarks
        );
        threatHistoryRepository.save(history);

        return updatedReport;
    }

    // ==========================================
    // RECALCULATE RISK SCORE
    // ==========================================
    @Transactional
    public IncidentReport recalculateRiskScore(Long reportId, User admin) {

        IncidentReport report = getReportById(reportId);
        IncidentReport.RiskLevel previousLevel = report.getRiskLevel();

        // Recalculate
        report.calculateRiskScore();
        IncidentReport updatedReport = incidentReportRepository.save(report);

        // Log to threat history
        ThreatHistory history = ThreatHistory.forIncident(
                ThreatHistory.ActionType.RISK_LEVEL_UPDATED,
                admin,
                updatedReport,
                previousLevel != null ? previousLevel.name() : null,
                updatedReport.getRiskLevel().name(),
                "Risk score recalculated by admin: " + admin.getEmail()
                        + " | New Score: " + updatedReport.getRiskScore()
                        + " | New Level: " + updatedReport.getRiskLevel()
        );
        threatHistoryRepository.save(history);

        return updatedReport;
    }

    // ==========================================
    // GET DASHBOARD STATS
    // ==========================================
    public ReportStats getDashboardStats() {

        Long totalReports = incidentReportRepository.count();
        Long pendingReports = incidentReportRepository
                .countByStatus(IncidentReport.ReportStatus.PENDING);
        Long underReviewReports = incidentReportRepository
                .countByStatus(IncidentReport.ReportStatus.UNDER_REVIEW);
        Long verifiedReports = incidentReportRepository
                .countByStatus(IncidentReport.ReportStatus.VERIFIED);
        Long dismissedReports = incidentReportRepository
                .countByStatus(IncidentReport.ReportStatus.DISMISSED);
        Long highRiskReports = incidentReportRepository
                .countByRiskLevel(IncidentReport.RiskLevel.HIGH);
        Long criticalRiskReports = incidentReportRepository
                .countByRiskLevel(IncidentReport.RiskLevel.CRITICAL);
        Long mediumRiskReports = incidentReportRepository
                .countByRiskLevel(IncidentReport.RiskLevel.MEDIUM);
        Long lowRiskReports = incidentReportRepository
                .countByRiskLevel(IncidentReport.RiskLevel.LOW);

        return new ReportStats(
                totalReports,
                pendingReports,
                underReviewReports,
                verifiedReports,
                dismissedReports,
                highRiskReports + criticalRiskReports,
                mediumRiskReports,
                lowRiskReports
        );
    }

    // ==========================================
    // GET GRAPH STATS (ADMIN DASHBOARD)
    // ==========================================
    public GraphStats getGraphStats() {

        // 1) Count by risk level
        Map<String, Long> riskCounts = new HashMap<>();
        for (Object[] row : incidentReportRepository.countGroupByRiskLevel()) {
            if (row[0] != null) {
                riskCounts.put(row[0].toString(), ((Number) row[1]).longValue());
            }
        }

        // Keep labels as Low / Medium / High (merge CRITICAL into High)
        List<Map<String, Object>> riskDistribution = new ArrayList<>();
        long low = riskCounts.getOrDefault("LOW", 0L);
        long medium = riskCounts.getOrDefault("MEDIUM", 0L);
        long high = riskCounts.getOrDefault("HIGH", 0L)
                + riskCounts.getOrDefault("CRITICAL", 0L);
        long totalRisk = low + medium + high;
        if (totalRisk == 0) {
            totalRisk = 1;
        }

        riskDistribution.add(buildRiskPoint(
                "Low",
                Math.round((low * 100.0) / totalRisk),
                "#00ff88"
        ));
        riskDistribution.add(buildRiskPoint(
                "Medium",
                Math.round((medium * 100.0) / totalRisk),
                "#ffd32a"
        ));
        riskDistribution.add(buildRiskPoint(
                "High",
                Math.round((high * 100.0) / totalRisk),
                "#ff4757"
        ));

        // 2) Count by date (monthly threats/alerts/resolved)
        Map<YearMonth, long[]> monthlyMap = new LinkedHashMap<>();
        for (Object[] row : incidentReportRepository.countMonthlyTrend()) {
            Integer year = ((Number) row[0]).intValue();
            Integer month = ((Number) row[1]).intValue();
            long threats = ((Number) row[2]).longValue();
            long alerts = ((Number) row[3]).longValue();
            long resolved = ((Number) row[4]).longValue();
            monthlyMap.put(YearMonth.of(year, month), new long[]{threats, alerts, resolved});
        }

        List<Map<String, Object>> monthlyTrend = new ArrayList<>();
        DateTimeFormatter monthFormat = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);
        YearMonth currentMonth = YearMonth.now();
        for (int i = 6; i >= 0; i--) {
            YearMonth ym = currentMonth.minusMonths(i);
            long[] counts = monthlyMap.getOrDefault(ym, new long[]{0, 0, 0});
            Map<String, Object> point = new HashMap<>();
            point.put("month", ym.atDay(1).format(monthFormat));
            point.put("threats", counts[0]);
            point.put("alerts", counts[1]);
            point.put("resolved", counts[2]);
            monthlyTrend.add(point);
        }

        // 3) Count by threat type
        Map<String, Long> typeCounts = new HashMap<>();
        for (Object[] row : incidentReportRepository.countGroupByIncidentType()) {
            if (row[0] != null) {
                typeCounts.put(row[0].toString(), ((Number) row[1]).longValue());
            }
        }

        List<Map<String, Object>> threatTypeDistribution = new ArrayList<>();
        threatTypeDistribution.add(buildTypePoint("Phishing",
                typeCounts.getOrDefault("PHISHING_EMAIL", 0L)));
        threatTypeDistribution.add(buildTypePoint("Malware",
                typeCounts.getOrDefault("MALWARE", 0L)));
        threatTypeDistribution.add(buildTypePoint("Ransomware",
                typeCounts.getOrDefault("RANSOMWARE", 0L)));
        threatTypeDistribution.add(buildTypePoint("Data Breach",
                typeCounts.getOrDefault("DATA_BREACH", 0L)));
        threatTypeDistribution.add(buildTypePoint("Unauth",
                typeCounts.getOrDefault("UNAUTHORIZED_ACCESS", 0L)));

        long otherCount = typeCounts.getOrDefault("SUSPICIOUS_URL", 0L)
                + typeCounts.getOrDefault("SOCIAL_ENGINEERING", 0L)
                + typeCounts.getOrDefault("OTHER", 0L);
        threatTypeDistribution.add(buildTypePoint("Other", otherCount));

        // 4) Count by alerts (from incident alert status)
        Map<String, Long> alertStatusCounts = new HashMap<>();
        for (Object[] row : incidentReportRepository.countGroupByAlertStatus()) {
            if (row[0] != null) {
                alertStatusCounts.put(row[0].toString(), ((Number) row[1]).longValue());
            }
        }

        return new GraphStats(
                monthlyTrend,
                riskDistribution,
                threatTypeDistribution,
                alertStatusCounts
        );
    }

    private Map<String, Object> buildRiskPoint(String name, long value, String color) {
        Map<String, Object> point = new HashMap<>();
        point.put("name", name);
        point.put("value", value);
        point.put("color", color);
        return point;
    }

    private Map<String, Object> buildTypePoint(String type, long count) {
        Map<String, Object> point = new HashMap<>();
        point.put("type", type);
        point.put("count", count);
        return point;
    }

    // ==========================================
    // REPORT STATS INNER CLASS
    // ==========================================
    public static class ReportStats {
        public Long totalReports;
        public Long pendingReports;
        public Long underReviewReports;
        public Long verifiedReports;
        public Long dismissedReports;
        public Long highRiskReports;
        public Long mediumRiskReports;
        public Long lowRiskReports;

        public ReportStats(Long totalReports,
                           Long pendingReports,
                           Long underReviewReports,
                           Long verifiedReports,
                           Long dismissedReports,
                           Long highRiskReports,
                           Long mediumRiskReports,
                           Long lowRiskReports) {
            this.totalReports = totalReports;
            this.pendingReports = pendingReports;
            this.underReviewReports = underReviewReports;
            this.verifiedReports = verifiedReports;
            this.dismissedReports = dismissedReports;
            this.highRiskReports = highRiskReports;
            this.mediumRiskReports = mediumRiskReports;
            this.lowRiskReports = lowRiskReports;
        }
    }

    public static class GraphStats {
        public List<Map<String, Object>> monthlyTrend;
        public List<Map<String, Object>> riskDistribution;
        public List<Map<String, Object>> threatTypeDistribution;
        public Map<String, Long> alertStatusDistribution;

        public GraphStats(List<Map<String, Object>> monthlyTrend,
                          List<Map<String, Object>> riskDistribution,
                          List<Map<String, Object>> threatTypeDistribution,
                          Map<String, Long> alertStatusDistribution) {
            this.monthlyTrend = monthlyTrend;
            this.riskDistribution = riskDistribution;
            this.threatTypeDistribution = threatTypeDistribution;
            this.alertStatusDistribution = alertStatusDistribution;
        }
    }
}
