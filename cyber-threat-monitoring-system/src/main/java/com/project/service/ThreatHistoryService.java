package com.project.service;

import com.project.entity.IncidentReport;
import com.project.entity.ThreatHistory;
import com.project.entity.User;
import com.project.repository.ThreatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ThreatHistoryService {

    private final ThreatHistoryRepository threatHistoryRepository;

    // ==========================================
    // GET ALL HISTORY (ADMIN)
    // ==========================================
    public List<ThreatHistory> getAllHistory() {
        return threatHistoryRepository.findAllByOrderByCreatedAtDesc();
    }

    // ==========================================
    // GET HISTORY BY ID
    // ==========================================
    public ThreatHistory getHistoryById(Long id) {
        return threatHistoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "History record not found with id: " + id));
    }

    // ==========================================
    // GET HISTORY BY USER
    // ==========================================
    public List<ThreatHistory> getHistoryByUser(User user) {
        return threatHistoryRepository
                .findByPerformedByOrderByCreatedAtDesc(user);
    }

    // ==========================================
    // GET HISTORY BY ACTION TYPE
    // ==========================================
    public List<ThreatHistory> getHistoryByActionType(
            ThreatHistory.ActionType actionType) {
        return threatHistoryRepository.findByActionType(actionType);
    }

    // ==========================================
    // GET INCIDENT TIMELINE
    // ==========================================
    public List<ThreatHistory> getIncidentTimeline(Long incidentId) {
        return threatHistoryRepository.findIncidentTimeline(incidentId);
    }

    // ==========================================
    // GET HISTORY BY INCIDENT REPORT
    // ==========================================
    public List<ThreatHistory> getHistoryByIncidentReport(
            Long incidentReportId) {
        return threatHistoryRepository
                .findByIncidentReportId(incidentReportId);
    }

    // ==========================================
    // GET HISTORY BY CYBER ALERT
    // ==========================================
    public List<ThreatHistory> getHistoryByCyberAlert(Long cyberAlertId) {
        return threatHistoryRepository.findByCyberAlertId(cyberAlertId);
    }

    // ==========================================
    // GET HISTORY BY RISK LEVEL
    // ==========================================
    public List<ThreatHistory> getHistoryByRiskLevel(
            IncidentReport.RiskLevel riskLevel) {
        return threatHistoryRepository.findByRiskLevel(riskLevel);
    }

    // ==========================================
    // GET RECENT HISTORY (LAST 7 DAYS)
    // ==========================================
    public List<ThreatHistory> getRecentHistory() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return threatHistoryRepository.findRecentHistory(sevenDaysAgo);
    }

    // ==========================================
    // GET HISTORY BY DATE RANGE
    // ==========================================
    public List<ThreatHistory> getHistoryByDateRange(
            LocalDateTime startDate,
            LocalDateTime endDate) {
        return threatHistoryRepository.findByDateRange(startDate, endDate);
    }

    // ==========================================
    // GET ALL ADMIN ACTIONS
    // ==========================================
    public List<ThreatHistory> getAllAdminActions() {
        return threatHistoryRepository.findAllAdminActions();
    }

    // ==========================================
    // GET ALL REPORT SUBMISSIONS
    // ==========================================
    public List<ThreatHistory> getAllReportSubmissions() {
        return threatHistoryRepository.findAllReportSubmissions();
    }

    // ==========================================
    // GET HISTORY BY INSTITUTION
    // ==========================================
    public List<ThreatHistory> getHistoryByInstitution(
            String institutionName) {
        return threatHistoryRepository.findByInstitution(institutionName);
    }

    // ==========================================
    // GET LOGIN HISTORY FOR USER
    // ==========================================
    public List<ThreatHistory> getLoginHistory(User user) {
        return threatHistoryRepository.findLoginHistory(user);
    }

    // ==========================================
    // GET HIGH RISK HISTORY
    // ==========================================
    public List<ThreatHistory> getHighRiskHistory(Integer minScore) {
        return threatHistoryRepository.findHighRiskHistory(minScore);
    }

    // ==========================================
    // MANUALLY LOG A HISTORY ENTRY
    // ==========================================
    @Transactional
    public ThreatHistory logHistory(ThreatHistory history) {
        return threatHistoryRepository.save(history);
    }

    // ==========================================
    // GET ACTIVITY SUMMARY (DASHBOARD)
    // ==========================================
    public Map<String, Long> getActivitySummary() {

        List<Object[]> results = threatHistoryRepository
                .countGroupByActionType();

        Map<String, Long> summary = new HashMap<>();
        for (Object[] row : results) {
            String actionType = row[0].toString();
            Long count = (Long) row[1];
            summary.put(actionType, count);
        }

        return summary;
    }

    // ==========================================
    // GET FULL DASHBOARD STATS
    // ==========================================
    public HistoryStats getDashboardStats() {

        Long totalRecords = threatHistoryRepository.count();

        Long reportSubmissions = threatHistoryRepository
                .countByActionType(
                        ThreatHistory.ActionType.REPORT_SUBMITTED);

        Long reportsVerified = threatHistoryRepository
                .countByActionType(
                        ThreatHistory.ActionType.REPORT_VERIFIED);

        Long reportsDismissed = threatHistoryRepository
                .countByActionType(
                        ThreatHistory.ActionType.REPORT_DISMISSED);

        Long alertsPublished = threatHistoryRepository
                .countByActionType(
                        ThreatHistory.ActionType.ALERT_PUBLISHED);

        Long alertsWithdrawn = threatHistoryRepository
                .countByActionType(
                        ThreatHistory.ActionType.ALERT_WITHDRAWN);

        Long userRegistrations = threatHistoryRepository
                .countByActionType(
                        ThreatHistory.ActionType.USER_REGISTERED);

        Long userLogins = threatHistoryRepository
                .countByActionType(
                        ThreatHistory.ActionType.USER_LOGIN);

        Long highRiskRecords = threatHistoryRepository
                .findHighRiskHistory(70).stream().count();

        return new HistoryStats(
                totalRecords,
                reportSubmissions,
                reportsVerified,
                reportsDismissed,
                alertsPublished,
                alertsWithdrawn,
                userRegistrations,
                userLogins,
                highRiskRecords
        );
    }

    // ==========================================
    // GET RECENT ACTIVITY FEED (LAST N RECORDS)
    // ==========================================
    public List<ThreatHistory> getRecentActivityFeed(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return threatHistoryRepository.findRecentHistory(since);
    }

    // ==========================================
    // HISTORY STATS INNER CLASS
    // ==========================================
    public static class HistoryStats {
        public Long totalRecords;
        public Long reportSubmissions;
        public Long reportsVerified;
        public Long reportsDismissed;
        public Long alertsPublished;
        public Long alertsWithdrawn;
        public Long userRegistrations;
        public Long userLogins;
        public Long highRiskRecords;

        public HistoryStats(Long totalRecords,
                            Long reportSubmissions,
                            Long reportsVerified,
                            Long reportsDismissed,
                            Long alertsPublished,
                            Long alertsWithdrawn,
                            Long userRegistrations,
                            Long userLogins,
                            Long highRiskRecords) {
            this.totalRecords = totalRecords;
            this.reportSubmissions = reportSubmissions;
            this.reportsVerified = reportsVerified;
            this.reportsDismissed = reportsDismissed;
            this.alertsPublished = alertsPublished;
            this.alertsWithdrawn = alertsWithdrawn;
            this.userRegistrations = userRegistrations;
            this.userLogins = userLogins;
            this.highRiskRecords = highRiskRecords;
        }
    }
}