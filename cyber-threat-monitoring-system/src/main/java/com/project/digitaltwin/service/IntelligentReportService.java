package com.project.digitaltwin.service;

import com.project.entity.IncidentReport;
import com.project.entity.ThreatHistory;
import com.project.entity.User;
import com.project.intelligence.model.AnalysisResult;
import com.project.intelligence.model.ThreatInput;
import com.project.intelligence.pipeline.IntelligencePipeline;
import com.project.repository.IncidentReportRepository;
import com.project.repository.ThreatHistoryRepository;
import com.project.service.AlertService;
import com.project.service.FastApiMlService;
import com.project.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

// ==========================================
// INTELLIGENT REPORT SERVICE
// Connects existing IncidentReport system
// with the new Intelligence Pipeline
// Extends — does NOT replace — existing
// ReportService functionality
// ==========================================
@Slf4j
@Service
@RequiredArgsConstructor
public class IntelligentReportService {

    private final HybridScoreService hybridScoreService =
            new HybridScoreService();

    private final IntelligencePipeline
            pipeline;
    private final IncidentReportRepository
            reportRepository;
    private final ThreatHistoryRepository
            historyRepository;
    private final AlertService
            alertService;
    private final UserService
            userService;
    private final FastApiMlService
            fastApiMlService;

    @Value("${intelligence.storage.upload-dir:uploads/}")
    private String uploadDir;

    private static final Map<String, BlockedDomainEntry> BLOCKED_DOMAINS = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.concurrent.atomic.AtomicLong BLOCKED_ID_SEQ = new java.util.concurrent.atomic.AtomicLong(1);

    // ==========================================
    // SUBMIT INTELLIGENT REPORT — URL
    // Extends existing report with AI analysis
    // ==========================================
    @Transactional
    public Map<String, Object>
    submitIntelligentUrlReport(
            User   reporter,
            String title,
            String description,
            String suspiciousUrl,
            String incidentType) {

        log.info(
                "Intelligent URL report from: {}",
                reporter.getEmail()
        );
        assertNotBlockedInput(
                Collections.singletonList(suspiciousUrl)
        );

        // ==============================
        // STEP 1: Run Intelligence Pipeline
        // ==============================
        ThreatInput input = ThreatInput.builder()
                .inputId(generateInputId())
                .inputType(ThreatInput.InputType.URL)
                .receivedAt(LocalDateTime.now())
                .userId(reporter.getId())
                .userEmail(reporter.getEmail())
                .institutionName(
                        reporter.getInstitutionName()
                )
                .rawUrl(suspiciousUrl)
                .build();

        AnalysisResult aiResult =
                pipeline.process(input);

        // ==============================
        // STEP 2: Create IncidentReport
        // (using existing entity)
        // ==============================
        IncidentReport report =
                new IncidentReport();
        report.setReportedBy(reporter);
        report.setTitle(title);
        report.setDescription(description);
        report.setSuspiciousUrl(suspiciousUrl);
        report.setType("URL");
        report.setTextContent(suspiciousUrl);

        // Map incident type
        report.setIncidentType(
                mapIncidentType(
                        incidentType,
                        aiResult
                )
        );

        // Use AI risk score
        report.setRiskScore(
                aiResult.getRiskScore()
        );

        // Map AI risk level to existing enum
        report.setRiskLevel(
                mapRiskLevel(aiResult.getRiskLevel())
        );
        report.setPredictedSpread(
                aiResult.getEstimatedAffectedUsers()
        );
        report.setCurrentSpread(
                calculateCurrentSpread(
                        suspiciousUrl,
                        aiResult
                )
        );

        Map<String, Object> mlResult =
                fastApiMlService.predictUrl(
                        suspiciousUrl
                );
        applyMlResult(report, mlResult);
        applyHybridScore(report, aiResult);
        normalizePrediction(report, aiResult);

        // Build enhanced AI analysis text
        report.setAiAnalysis(
                buildAiAnalysisText(aiResult)
        );

        report.setStatus(
                IncidentReport.ReportStatus.PENDING
        );
        applyTwoStageAlertState(
                report,
                reporter
        );

        // Save report
        IncidentReport saved =
                reportRepository.save(report);
        applyRiskBasedBlocking(
                saved,
                reporter,
                false
        );

        // ==============================
        // STEP 3: Log to ThreatHistory
        // ==============================
        logToThreatHistory(
                saved, reporter, aiResult
        );
        createProvisionalAlertIfRequired(
                saved,
                reporter
        );

        // ==============================
        // STEP 4: Auto Alert if Critical
        // ==============================
        if (saved.getAlertStatus() == IncidentReport.AlertStatus.NONE
                && aiResult.isAutoAlertPublished()
                && aiResult.isCriticalThreat()) {
            triggerAutoAlert(
                    saved, reporter, aiResult
            );
        }

        // ==============================
        // STEP 5: Build Response
        // ==============================
        return buildReportResponse(
                saved, aiResult
        );
    }

    // ==========================================
    // SUBMIT INTELLIGENT REPORT — EMAIL
    // ==========================================
    @Transactional
    public Map<String, Object>
    submitIntelligentEmailReport(
            User   reporter,
            String title,
            String emailSubject,
            String emailBody,
            String emailSender,
            String description) {

        log.info(
                "Intelligent email report from: {}",
                reporter.getEmail()
        );
        assertNotBlockedInput(
                Arrays.asList(
                        emailSender,
                        emailBody
                )
        );

        // Run pipeline
        ThreatInput input = ThreatInput.builder()
                .inputId(generateInputId())
                .inputType(
                        ThreatInput.InputType.EMAIL_CONTENT
                )
                .receivedAt(LocalDateTime.now())
                .userId(reporter.getId())
                .userEmail(reporter.getEmail())
                .institutionName(
                        reporter.getInstitutionName()
                )
                .emailSubject(emailSubject)
                .emailBody(emailBody)
                .emailSender(emailSender)
                .build();

        AnalysisResult aiResult =
                pipeline.process(input);

        // Create report
        IncidentReport report =
                new IncidentReport();
        report.setReportedBy(reporter);
        report.setTitle(title);
        report.setDescription(
                buildEmailDescription(
                        description,
                        emailSubject,
                        emailSender
                )
        );
        report.setSuspiciousEmail(emailSender);
        report.setType("EMAIL");
        report.setTextContent(
                "Sender: " + (emailSender == null ? "N/A" : emailSender)
                        + " | Subject: " + (emailSubject == null ? "N/A" : emailSubject)
                        + " | Body: " + (emailBody == null ? "" : emailBody)
        );
        report.setIncidentType(
                mapIncidentType("PHISHING", aiResult)
        );
        report.setRiskScore(
                aiResult.getRiskScore()
        );
        report.setRiskLevel(
                mapRiskLevel(aiResult.getRiskLevel())
        );
        report.setPredictedSpread(
                aiResult.getEstimatedAffectedUsers()
        );
        report.setCurrentSpread(
                calculateCurrentSpread(
                        null,
                        aiResult
                )
        );
        Map<String, Object> mlResult =
                fastApiMlService.predictText(
                        buildEmailDescription(
                                description,
                                emailSubject,
                                emailSender
                        ) + "\n" + emailBody,
                        "email"
                );
        applyMlResult(report, mlResult);
        applyHybridScore(report, aiResult);
        normalizePrediction(report, aiResult);
        report.setAiAnalysis(
                buildAiAnalysisText(aiResult)
        );
        report.setStatus(
                IncidentReport.ReportStatus.PENDING
        );
        applyTwoStageAlertState(
                report,
                reporter
        );

        IncidentReport saved =
                reportRepository.save(report);
        applyRiskBasedBlocking(
                saved,
                reporter,
                false
        );

        logToThreatHistory(
                saved, reporter, aiResult
        );
        createProvisionalAlertIfRequired(
                saved,
                reporter
        );

        if (saved.getAlertStatus() == IncidentReport.AlertStatus.NONE
                && aiResult.isAutoAlertPublished()
                && aiResult.isCriticalThreat()) {
            triggerAutoAlert(
                    saved, reporter, aiResult
            );
        }

        return buildReportResponse(
                saved, aiResult
        );
    }

    // ==========================================
    // SUBMIT INTELLIGENT REPORT — DOCUMENT
    // ==========================================
    @Transactional
    public Map<String, Object>
    submitIntelligentDocumentReport(
            User          reporter,
            String        title,
            String        description,
            MultipartFile file) {

        log.info(
                "Intelligent document report: {} "
                        + "from: {}",
                file.getOriginalFilename(),
                reporter.getEmail()
        );

        try {
            ThreatInput input =
                    ThreatInput.builder()
                            .inputId(generateInputId())
                            .inputType(
                                    ThreatInput.InputType
                                            .PDF_DOCUMENT
                            )
                            .receivedAt(LocalDateTime.now())
                            .userId(reporter.getId())
                            .userEmail(reporter.getEmail())
                            .institutionName(
                                    reporter.getInstitutionName()
                            )
                            .fileName(
                                    file.getOriginalFilename()
                            )
                            .fileType(
                                    file.getContentType()
                            )
                            .fileSizeBytes(file.getSize())
                            .fileBytes(file.getBytes())
                            .build();

            AnalysisResult aiResult =
                    pipeline.process(input);

            IncidentReport report =
                    new IncidentReport();
            report.setReportedBy(reporter);
            report.setTitle(title);
            report.setDescription(
                    buildDocDescription(
                            description,
                            file.getOriginalFilename(),
                            file.getSize()
                    )
            );
            report.setType("DOCUMENT");
            report.setDocumentPath(
                    saveUploadedFile(
                            file,
                            "documents"
                    )
            );
            report.setTextContent(
                    "File: "
                            + (file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename())
                            + " | Extracted Text: "
                            + (aiResult.getAttackNarrative() == null ? "" : aiResult.getAttackNarrative())
            );
            report.setIncidentType(
                    mapIncidentType(
                            "MALWARE", aiResult
                    )
            );
            report.setRiskScore(
                    aiResult.getRiskScore()
            );
            report.setRiskLevel(
                    mapRiskLevel(
                            aiResult.getRiskLevel()
                    )
            );
            report.setPredictedSpread(
                    aiResult.getEstimatedAffectedUsers()
            );
            report.setCurrentSpread(
                    calculateCurrentSpread(
                            null,
                            aiResult
                    )
            );
            Map<String, Object> mlResult =
                    fastApiMlService.predictDocument(
                            file.getBytes(),
                            aiResult.getAttackNarrative()
                    );
            applyMlResult(report, mlResult);
            applyHybridScore(report, aiResult);
            normalizePrediction(report, aiResult);
            report.setAiAnalysis(
                    buildAiAnalysisText(aiResult)
            );
            report.setStatus(
                    IncidentReport.ReportStatus.PENDING
            );
            applyTwoStageAlertState(
                    report,
                    reporter
            );

            IncidentReport saved =
                    reportRepository.save(report);
            applyRiskBasedBlocking(
                    saved,
                    reporter,
                    false
            );

            logToThreatHistory(
                    saved, reporter, aiResult
            );
            createProvisionalAlertIfRequired(
                    saved,
                    reporter
            );

            if (saved.getAlertStatus() == IncidentReport.AlertStatus.NONE
                    && aiResult.isAutoAlertPublished()
                    && aiResult.isCriticalThreat()) {
                triggerAutoAlert(
                        saved, reporter, aiResult
                );
            }

            return buildReportResponse(
                    saved, aiResult
            );

        } catch (Exception e) {
            log.error(
                    "Document report error: {}",
                    e.getMessage()
            );
            throw new RuntimeException(
                    "Failed to process document: "
                            + e.getMessage()
            );
        }
    }

    // ==========================================
    // SUBMIT INTELLIGENT REPORT — IMAGE
    // ==========================================
    @Transactional
    public Map<String, Object>
    submitIntelligentImageReport(
            User          reporter,
            String        title,
            String        description,
            MultipartFile image) {

        log.info(
                "Intelligent image report: {} "
                        + "from: {}",
                image.getOriginalFilename(),
                reporter.getEmail()
        );

        try {
            ThreatInput input =
                    ThreatInput.builder()
                            .inputId(generateInputId())
                            .inputType(
                                    ThreatInput.InputType
                                            .IMAGE_SCREENSHOT
                            )
                            .receivedAt(LocalDateTime.now())
                            .userId(reporter.getId())
                            .userEmail(reporter.getEmail())
                            .institutionName(
                                    reporter.getInstitutionName()
                            )
                            .imageName(
                                    image.getOriginalFilename()
                            )
                            .fileType(
                                    image.getContentType()
                            )
                            .imageSizeBytes(image.getSize())
                            .imageBytes(image.getBytes())
                            .extractedImageText(description)
                            .build();

            AnalysisResult aiResult =
                    pipeline.process(input);

            IncidentReport report =
                    new IncidentReport();
            report.setReportedBy(reporter);
            report.setTitle(title);
            report.setDescription(
                    buildImageDescription(
                            description,
                            image.getOriginalFilename()
                    )
            );
            report.setType("IMAGE");
            report.setImagePath(
                    saveUploadedFile(
                            image,
                            "images"
                    )
            );
            report.setTextContent(description);
            report.setIncidentType(
                    mapIncidentType(
                            "PHISHING", aiResult
                    )
            );
            report.setRiskScore(
                    aiResult.getRiskScore()
            );
            report.setRiskLevel(
                    mapRiskLevel(
                            aiResult.getRiskLevel()
                    )
            );
            report.setPredictedSpread(
                    aiResult.getEstimatedAffectedUsers()
            );
            report.setCurrentSpread(
                    calculateCurrentSpread(
                            null,
                            aiResult
                    )
            );
            Map<String, Object> mlResult =
                    fastApiMlService.predictImage(
                            image.getBytes(),
                            description
                    );
            applyMlResult(report, mlResult);
            applyHybridScore(report, aiResult);
            normalizePrediction(report, aiResult);
            report.setAiAnalysis(
                    buildAiAnalysisText(aiResult)
            );
            report.setStatus(
                    IncidentReport.ReportStatus.PENDING
            );
            applyTwoStageAlertState(
                    report,
                    reporter
            );

            IncidentReport saved =
                    reportRepository.save(report);
            applyRiskBasedBlocking(
                    saved,
                    reporter,
                    false
            );

            logToThreatHistory(
                    saved, reporter, aiResult
            );
            createProvisionalAlertIfRequired(
                    saved,
                    reporter
            );

            if (saved.getAlertStatus() == IncidentReport.AlertStatus.NONE
                    && aiResult.isAutoAlertPublished()
                    && aiResult.isCriticalThreat()) {
                triggerAutoAlert(
                        saved, reporter, aiResult
                );
            }

            return buildReportResponse(
                    saved, aiResult
            );

        } catch (Exception e) {
            log.error(
                    "Image report error: {}",
                    e.getMessage()
            );
            throw new RuntimeException(
                    "Failed to process image: "
                            + e.getMessage()
            );
        }
    }

    // ==========================================
    // RE-ANALYZE EXISTING REPORT
    // Admin can trigger re-analysis
    // ==========================================
    @Transactional
    public Map<String, Object> reAnalyzeReport(
            Long reportId,
            User admin) {

        log.info(
                "Re-analyzing report: {} by: {}",
                reportId,
                admin.getEmail()
        );

        IncidentReport report =
                reportRepository
                        .findById(reportId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Report not found: "
                                                + reportId
                                )
                        );

        ThreatInput.ThreatInputBuilder builder =
                ThreatInput.builder()
                        .inputId(generateInputId())
                        .receivedAt(LocalDateTime.now())
                        .userId(report.getReportedBy().getId())
                        .userEmail(report.getReportedBy().getEmail())
                        .institutionName(
                                report.getReportedBy()
                                        .getInstitutionName()
                        );

        if (report.getSuspiciousUrl() != null
                && !report.getSuspiciousUrl().isEmpty()) {

            builder.inputType(ThreatInput.InputType.URL)
                    .rawUrl(report.getSuspiciousUrl());

        } else if (report.getSuspiciousEmail() != null
                && !report.getSuspiciousEmail().isEmpty()) {

            builder.inputType(
                            ThreatInput.InputType.EMAIL_CONTENT)
                    .emailSender(report.getSuspiciousEmail())
                    .emailBody(report.getDescription());

        } else {

            builder.inputType(
                            ThreatInput.InputType.EMAIL_CONTENT)
                    .emailBody(report.getDescription());
        }

        AnalysisResult aiResult =
                pipeline.process(builder.build());

        report.setRiskScore(aiResult.getRiskScore());
        report.setRiskLevel(
                mapRiskLevel(aiResult.getRiskLevel()));
        report.setAiAnalysis(
                "[RE-ANALYZED] "
                        + buildAiAnalysisText(aiResult));

        IncidentReport updated =
                reportRepository.save(report);

        // ✅ FIXED ORDER HERE
        ThreatHistory history =
                ThreatHistory.forIncident(
                        ThreatHistory.ActionType.REPORT_REANALYZED,
                        admin,
                        updated,
                        "REPORT_REANALYZED",
                        updated.getStatus().name(),
                        updated.getStatus().name()
                );
        historyRepository.save(history);

        log.info(
                "Report {} re-analyzed. New score: {}",
                reportId,
                aiResult.getRiskScore()
        );

        return buildReportResponse(updated, aiResult);
    }

    // ==========================================
    // GET INTELLIGENCE SUMMARY FOR REPORT
    // ==========================================
    public Map<String, Object>
    getIntelligenceSummary(Long reportId) {

        IncidentReport report =
                reportRepository
                        .findById(reportId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Report not found"
                                )
                        );

        Map<String, Object> summary =
                new LinkedHashMap<>();

        summary.put("reportId", reportId);
        summary.put("title", report.getTitle());
        summary.put(
                "riskScore", report.getRiskScore()
        );
        summary.put(
                "riskLevel", report.getRiskLevel()
        );
        summary.put(
                "incidentType",
                report.getIncidentType()
        );
        summary.put(
                "status", report.getStatus()
        );
        summary.put(
                "aiAnalysis", report.getAiAnalysis()
        );
        summary.put(
                "reportedAt",
                report.getCreatedAt()
        );
        summary.put(
                "reportedBy",
                report.getReportedBy().getEmail()
        );

        return summary;
    }

    // ==========================================
    // AUTO ALERT TRIGGER
    // ==========================================
    private void triggerAutoAlert(
            IncidentReport report,
            User           reporter,
            AnalysisResult aiResult) {

        try {
            log.info(
                    "Triggering auto alert for "
                            + "report: {}",
                    report.getId()
            );

            // Find admin user to publish alert
            List<User> admins =
                    userService.getAllAdmins();

            if (admins == null
                    || admins.isEmpty()) {
                log.warn(
                        "No admins found for "
                                + "auto alert"
                );
                return;
            }

            User systemAdmin = admins.get(0);

            // Build alert data
            Map<String, Object> alertData =
                    new LinkedHashMap<>();
            alertData.put(
                    "title",
                    "[AUTO-ALERT] "
                            + report.getTitle()
            );
            alertData.put(
                    "message",
                    buildAutoAlertMessage(
                            report, aiResult
                    )
            );
            alertData.put(
                    "alertType",
                    mapAlertType(
                            aiResult.getThreatCategory()
                    )
            );
            alertData.put(
                    "severity",
                    mapAlertSeverity(
                            aiResult.getRiskLevel()
                    )
            );
            alertData.put(
                    "targetInstitution",
                    reporter.getInstitutionName()
            );
            alertData.put("isPublic", false);
            alertData.put(
                    "relatedIncidentId",
                    report.getId()
            );

            report.setStatus(IncidentReport.ReportStatus.VERIFIED);
            report.setAlertStatus(IncidentReport.AlertStatus.OFFICIAL);
            report.setVerificationStatus(
                    IncidentReport.VerificationStatus.APPROVED
            );
            reportRepository.save(report);
            alertService.publishAlertFromIncident(
                    systemAdmin,
                    report,
                    "[AUTO] " + report.getTitle(),
                    mapAlertSeverityEnum(aiResult.getRiskLevel()),
                    LocalDateTime.now()
            );
            log.info(
                    "Auto alert published for "
                            + "report: {}",
                    report.getId()
            );

        } catch (Exception e) {
            log.error(
                    "Auto alert failed: {}",
                    e.getMessage()
            );
            // Non-critical — don't fail report
        }
    }

    // ==========================================
    // LOG TO THREAT HISTORY
    // ==========================================
    private void logToThreatHistory(
            IncidentReport report,
            User           reporter,
            AnalysisResult aiResult) {

        try {
            ThreatHistory history =
                    ThreatHistory.forIncident(
                            ThreatHistory.ActionType.INTELLIGENT_ANALYSIS,
                            reporter,
                            report,
                            "INTELLIGENT_ANALYSIS",
                            null,
                            report.getStatus().name()
                    );
            historyRepository.save(history);
        } catch (Exception e) {
            log.warn(
                    "History log failed: {}",
                    e.getMessage()
            );
        }
    }

    // ==========================================
    // MAP RISK LEVEL
    // AI RiskLevel → Existing RiskLevel enum
    // ==========================================
    private IncidentReport.RiskLevel mapRiskLevel(
            AnalysisResult.RiskLevel aiLevel) {

        if (aiLevel == null) {
            return IncidentReport.RiskLevel.LOW;
        }

        switch (aiLevel) {
            case CRITICAL:
            case HIGH:
                return IncidentReport
                        .RiskLevel.HIGH;
            case MEDIUM:
                return IncidentReport
                        .RiskLevel.MEDIUM;
            case LOW:
            case NEGLIGIBLE:
            default:
                return IncidentReport
                        .RiskLevel.LOW;
        }
    }

    // ==========================================
    // MAP INCIDENT TYPE
    // AI ThreatCategory → Existing IncidentType
    // ==========================================
    private IncidentReport.IncidentType
    mapIncidentType(
            String         hint,
            AnalysisResult aiResult) {

        if (aiResult.getThreatCategory() != null) {
            switch (aiResult.getThreatCategory()) {
                case PHISHING:
                case CREDENTIAL_THEFT:
                    return IncidentReport.IncidentType.PHISHING_EMAIL;
                case MALWARE:
                case RANSOMWARE:
                    return IncidentReport
                            .IncidentType.MALWARE;
                case DATA_BREACH:
                    return IncidentReport
                            .IncidentType.DATA_BREACH;
                case SOCIAL_ENGINEERING:
                    return IncidentReport
                            .IncidentType
                            .SOCIAL_ENGINEERING;
                case INSIDER_THREAT:
                    return IncidentReport
                            .IncidentType
                            .UNAUTHORIZED_ACCESS;
                default:
                    break;
            }
        }

        // Fallback to hint
        if (hint != null) {
            try {
                return IncidentReport.IncidentType
                        .valueOf(hint.toUpperCase());
            } catch (Exception ignored) {}
        }

        return IncidentReport
                .IncidentType.OTHER;
    }

    // ==========================================
    // MAP ALERT TYPE
    // ==========================================
    private String mapAlertType(
            AnalysisResult.ThreatCategory cat) {

        if (cat == null) {
            return "THREAT_WARNING";
        }
        switch (cat) {
            case PHISHING:
            case CREDENTIAL_THEFT:
                return "PHISHING_ALERT";
            case MALWARE:
                return "MALWARE_ALERT";
            case RANSOMWARE:
                return "RANSOMWARE_ALERT";
            case DATA_BREACH:
                return "DATA_BREACH_ALERT";
            default:
                return "THREAT_WARNING";
        }
    }

    // ==========================================
    // MAP ALERT SEVERITY
    // ==========================================
    private String mapAlertSeverity(
            AnalysisResult.RiskLevel level) {

        if (level == null) return "MEDIUM";
        switch (level) {
            case CRITICAL: return "CRITICAL";
            case HIGH:     return "HIGH";
            case MEDIUM:   return "MEDIUM";
            default:       return "LOW";
        }
    }

    // ==========================================
    // BUILD AI ANALYSIS TEXT
    // Stores intelligence output in aiAnalysis
    // field of existing IncidentReport entity
    // ==========================================
    private String buildAiAnalysisText(
            AnalysisResult result) {

        StringBuilder sb = new StringBuilder();

        sb.append("=== AI INTELLIGENCE REPORT ===\n\n");

        // Risk summary
        sb.append("RISK ASSESSMENT:\n");
        sb.append("  Score: ")
                .append(result.getRiskScore())
                .append("/100\n");
        sb.append("  Level: ")
                .append(result.getRiskLevel())
                .append("\n");
        sb.append("  Confidence: ")
                .append(result.getConfidencePercentage())
                .append("\n\n");

        // Threat classification
        sb.append("THREAT CLASSIFICATION:\n");
        sb.append("  Category: ")
                .append(result.getThreatCategory())
                .append("\n");
        sb.append("  Sub-type: ")
                .append(result.getThreatSubCategory())
                .append("\n");
        sb.append("  Description: ")
                .append(result.getThreatDescription())
                .append("\n\n");

        // Attack stage
        sb.append("ATTACK STAGE:\n");
        sb.append("  Stage: ")
                .append(result.getDetectedStage())
                .append("\n");
        sb.append("  Detail: ")
                .append(result.getStageDescription())
                .append("\n");
        sb.append("  Active Attack: ")
                .append(result.isActiveAttack())
                .append("\n\n");

        // Pattern match
        if (result.isMatchedKnownPattern()) {
            sb.append("PATTERN MATCH:\n");
            sb.append("  Matched: ")
                    .append(result.getMatchedPatternName())
                    .append("\n");
            sb.append("  Similarity: ")
                    .append(String.format(
                            "%.1f%%",
                            result.getPatternSimilarity()
                                    * 100
                    ))
                    .append("\n\n");
        }

        // Spread simulation
        sb.append("SPREAD SIMULATION:\n");
        sb.append("  Probability: ")
                .append(String.format(
                        "%.0f%%",
                        result.getSpreadProbability() * 100
                ))
                .append("\n");
        sb.append("  Est. Affected Users: ")
                .append(result.getEstimatedAffectedUsers())
                .append("\n\n");

        // Digital twin
        if (result.isAnomalyDetected()) {
            sb.append("BEHAVIORAL ANOMALY:\n");
            sb.append("  ")
                    .append(result.getAnomalyDescription())
                    .append("\n\n");
        }

        // Red flags
        if (result.getRedFlags() != null
                && !result.getRedFlags().isEmpty()) {
            sb.append("RED FLAGS DETECTED:\n");
            result.getRedFlags()
                    .stream()
                    .limit(5)
                    .forEach(flag ->
                            sb.append("  • ")
                                    .append(flag)
                                    .append("\n")
                    );
            sb.append("\n");
        }

        // Score breakdown
        if (result.getScoreBreakdown() != null
                && !result.getScoreBreakdown()
                .isEmpty()) {
            sb.append("SCORE BREAKDOWN:\n");
            result.getScoreBreakdown()
                    .entrySet()
                    .stream()
                    .sorted(Map.Entry
                            .<String, Integer>
                                    comparingByValue()
                            .reversed())
                    .limit(5)
                    .forEach(e ->
                            sb.append("  ")
                                    .append(e.getKey())
                                    .append(": +")
                                    .append(e.getValue())
                                    .append("\n")
                    );
            sb.append("\n");
        }

        // Recommendations
        if (result.getRecommendations() != null
                && !result.getRecommendations()
                .isEmpty()) {
            sb.append("RECOMMENDATIONS:\n");
            result.getRecommendations()
                    .stream()
                    .limit(3)
                    .forEach(rec ->
                            sb.append("  ")
                                    .append(rec)
                                    .append("\n")
                    );
        }

        sb.append("\n=== END OF AI REPORT ===");

        return sb.toString();
    }

    // ==========================================
    // BUILD AUTO ALERT MESSAGE
    // ==========================================
    private String buildAutoAlertMessage(
            IncidentReport report,
            AnalysisResult aiResult) {

        return "AUTOMATED SECURITY ALERT: "
                + "A "
                + aiResult.getRiskLevel()
                + " risk threat has been detected. "
                + "Threat type: "
                + aiResult.getThreatCategory()
                + ". "
                + "AI Risk Score: "
                + aiResult.getRiskScore()
                + "/100. "
                + "Estimated affected users: "
                + aiResult.getEstimatedAffectedUsers()
                + ". "
                + "Description: "
                + aiResult.getThreatDescription()
                + ". "
                + "Immediate action recommended.";
    }

    // ==========================================
    // BUILD EMAIL DESCRIPTION
    // ==========================================
    private String buildEmailDescription(
            String description,
            String emailSubject,
            String emailSender) {

        StringBuilder sb = new StringBuilder();
        if (description != null
                && !description.isEmpty()) {
            sb.append(description).append("\n\n");
        }
        if (emailSubject != null) {
            sb.append("Email Subject: ")
                    .append(emailSubject)
                    .append("\n");
        }
        if (emailSender != null) {
            sb.append("Sender: ")
                    .append(emailSender)
                    .append("\n");
        }
        return sb.toString().trim();
    }

    // ==========================================
    // BUILD DOCUMENT DESCRIPTION
    // ==========================================
    private String buildDocDescription(
            String description,
            String fileName,
            long   fileSize) {

        StringBuilder sb = new StringBuilder();
        if (description != null
                && !description.isEmpty()) {
            sb.append(description).append("\n\n");
        }
        sb.append("File: ").append(fileName)
                .append("\n");
        sb.append("Size: ")
                .append(fileSize / 1024)
                .append(" KB");
        return sb.toString();
    }

    // ==========================================
    // BUILD IMAGE DESCRIPTION
    // ==========================================
    private String buildImageDescription(
            String description,
            String imageName) {

        StringBuilder sb = new StringBuilder();
        if (description != null
                && !description.isEmpty()) {
            sb.append(description).append("\n\n");
        }
        sb.append("Image: ").append(imageName);
        return sb.toString();
    }

    private void applyMlResult(
            IncidentReport       report,
            Map<String, Object> mlResult) {

        String prediction = String.valueOf(
                mlResult.getOrDefault(
                        "prediction",
                        report.getIncidentType() != null
                                ? report.getIncidentType().name()
                                : "SUSPICIOUS"
                )
        );
        double confidence = parseDouble(
                mlResult.get("confidence"),
                70.0
        );
        report.setPrediction(prediction);
        report.setMlScore(confidence);
        report.setConfidence(confidence);
    }

    private void applyHybridScore(
            IncidentReport report,
            AnalysisResult aiResult) {

        double mlScore = report.getMlScore() == null ? 0.0 : report.getMlScore();
        double aiScore = aiResult != null ? aiResult.getRiskScore() : 0.0;
        double finalScore = hybridScoreService.calculateFinalScore(mlScore, aiScore);

        report.setAiScore(aiScore);
        report.setFinalScore(finalScore);
        report.setRiskScore((int) Math.round(finalScore));
        report.setRiskLevel(hybridScoreService.mapRiskLevel(finalScore));
    }

    private void normalizePrediction(
            IncidentReport report,
            AnalysisResult aiResult) {

        String prediction = report.getPrediction() == null
                ? ""
                : report.getPrediction().trim().toUpperCase();

        boolean genericPrediction = prediction.isBlank()
                || "SUSPICIOUS".equals(prediction)
                || "PHISHING".equals(prediction)
                || "BENIGN".equals(prediction)
                || "UNKNOWN".equals(prediction);

        if (!genericPrediction) {
            return;
        }

        if (aiResult != null && aiResult.getThreatCategory() != null) {
            switch (aiResult.getThreatCategory()) {
                case CREDENTIAL_THEFT:
                    report.setPrediction("CREDENTIAL_THEFT");
                    return;
                case PHISHING:
                    report.setPrediction("PHISHING");
                    return;
                case MALWARE:
                    report.setPrediction("MALWARE");
                    return;
                case RANSOMWARE:
                    report.setPrediction("RANSOMWARE");
                    return;
                case SOCIAL_ENGINEERING:
                    report.setPrediction("SOCIAL_ENGINEERING");
                    return;
                case DATA_BREACH:
                    report.setPrediction("DATA_BREACH");
                    return;
                case INSIDER_THREAT:
                    report.setPrediction("INSIDER_THREAT");
                    return;
                case SAFE:
                    report.setPrediction("SAFE");
                    return;
                default:
                    break;
            }
        }

        report.setPrediction(
                (report.getType() == null ? "THREAT" : report.getType())
                        + "_RISK"
        );
    }

    private static class HybridScoreService {
        double calculateFinalScore(double mlScore, double aiScore) {
            double finalScore = (mlScore * 0.7) + (aiScore * 0.3);
            return Math.max(0.0, Math.min(100.0, finalScore));
        }

        IncidentReport.RiskLevel mapRiskLevel(double finalScore) {
            if (finalScore <= 30) {
                return IncidentReport.RiskLevel.LOW;
            }
            if (finalScore <= 60) {
                return IncidentReport.RiskLevel.MEDIUM;
            }
            if (finalScore <= 80) {
                return IncidentReport.RiskLevel.HIGH;
            }
            return IncidentReport.RiskLevel.CRITICAL;
        }
    }

    private double parseDouble(Object value, double fallback) {
        if (value == null) return fallback;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private Integer calculateCurrentSpread(
            String         suspiciousUrl,
            AnalysisResult aiResult) {

        LocalDateTime since = LocalDateTime.now().minusHours(24);
        long repeatedUrlCount = 0;
        if (suspiciousUrl != null && !suspiciousUrl.isBlank()) {
            repeatedUrlCount = reportRepository.countBySuspiciousUrlAndCreatedAtAfter(
                    suspiciousUrl,
                    since
            );
        }

        long dailyCount = reportRepository.countByCreatedAtAfter(since);
        int spikeBoost = dailyCount > 25 ? 20 : dailyCount > 10 ? 10 : 0;
        int anomalyBoost = repeatedUrlCount > 2 ? 15 : 0;

        int pipelineEstimate = aiResult != null
                ? aiResult.getEstimatedAffectedUsers()
                : 0;
        return Math.max(0, pipelineEstimate + spikeBoost + anomalyBoost);
    }

    private void applyTwoStageAlertState(
            IncidentReport report,
            User           reporter) {

        if (report.getRiskLevel() != IncidentReport.RiskLevel.HIGH
                && report.getRiskLevel() != IncidentReport.RiskLevel.CRITICAL) {
            report.setAlertStatus(IncidentReport.AlertStatus.NONE);
            report.setVerificationStatus(
                    IncidentReport.VerificationStatus.PENDING_ADMIN
            );
            return;
        }

        report.setAlertStatus(IncidentReport.AlertStatus.AI_PROVISIONAL);
        report.setVerificationStatus(
                IncidentReport.VerificationStatus.PENDING_ADMIN
        );
    }

    private void createProvisionalAlertIfRequired(
            IncidentReport report,
            User           reporter) {

        if (report.getAlertStatus() != IncidentReport.AlertStatus.AI_PROVISIONAL) {
            return;
        }

        List<User> admins = userService.getAllAdmins();
        if (admins == null || admins.isEmpty()) {
            return;
        }

        User systemAdmin = admins.get(0);
        String inputContent = buildInputContent(report);
        String reason = "Hybrid score indicates malicious indicators from ML + AI rules.";
        String action = "Do not interact with the content. Wait for admin verification.";
        String message = "ALERT: Suspicious phishing detected\n\n"
                + "Threat Type: " + (report.getType() == null ? "UNKNOWN" : report.getType()) + "\n"
                + "Input Content: " + (inputContent == null ? "N/A" : inputContent) + "\n"
                + "Final Score: " + (report.getFinalScore() == null
                ? report.getRiskScore()
                : String.format("%.0f", report.getFinalScore())) + "/100\n"
                + "Risk Level: " + report.getRiskLevel() + "\n\n"
                + "Reason: " + reason + "\n"
                + "Recommended Action: " + action;
        alertService.publishProvisionalAlert(
                systemAdmin,
                report,
                message,
                report.getRiskLevel() == IncidentReport.RiskLevel.CRITICAL
                        ? com.project.entity.CyberAlert.Severity.CRITICAL
                        : com.project.entity.CyberAlert.Severity.HIGH
        );
    }

    private String saveUploadedFile(MultipartFile file, String folder) {
        try {
            Path baseDir = Paths.get(uploadDir, folder).toAbsolutePath().normalize();
            Files.createDirectories(baseDir);

            String originalName = file.getOriginalFilename() == null
                    ? "file.bin"
                    : file.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_");
            String storedName = UUID.randomUUID() + "_" + originalName;

            Path target = baseDir.resolve(storedName).normalize();
            Files.write(target, file.getBytes());
            return "/uploads/" + folder + "/" + storedName;
        } catch (Exception ex) {
            log.warn("File save failed: {}", ex.getMessage());
            return null;
        }
    }

    public void applyRiskBasedBlocking(
            IncidentReport report,
            User blockedBy,
            boolean adminConfirmed) {
        if (report == null || report.getRiskLevel() == null) return;

        boolean shouldBlock = adminConfirmed
                || report.getRiskLevel() == IncidentReport.RiskLevel.CRITICAL;
        if (!shouldBlock) {
            return;
        }

        Set<String> domains = extractDomainsFromReport(report);
        if (domains.isEmpty()) {
            return;
        }

        String blocker = blockedBy != null ? blockedBy.getEmail() : "SYSTEM_AUTO_BLOCK";
        String reason = adminConfirmed
                ? "Blocked after admin threat verification"
                : "Critical risk auto-block";
        for (String domain : domains) {
            addBlockedDomain(
                    domain,
                    blocker,
                    reason,
                    report.getRiskLevel() == null ? "UNKNOWN" : report.getRiskLevel().name()
            );
        }
    }

    public boolean isDomainBlocked(String domainOrUrl) {
        String domain = normalizeDomain(domainOrUrl);
        return !domain.isBlank() && BLOCKED_DOMAINS.containsKey(domain);
    }

    public List<Map<String, Object>> getBlockedDomains() {
        return BLOCKED_DOMAINS.values()
                .stream()
                .sorted((a, b) -> b.blockedTime.compareTo(a.blockedTime))
                .map(entry -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", entry.id);
                    map.put("domain", entry.domain);
                    map.put("blockedBy", entry.blockedBy);
                    map.put("blockedTime", entry.blockedTime);
                    map.put("reason", entry.reason);
                    map.put("riskLevel", entry.riskLevel);
                    return map;
                })
                .toList();
    }

    private void assertNotBlockedInput(List<String> rawInputs) {
        if (rawInputs == null || rawInputs.isEmpty()) return;

        Set<String> candidateDomains = new LinkedHashSet<>();
        for (String raw : rawInputs) {
            candidateDomains.addAll(extractDomains(raw));
        }
        for (String domain : candidateDomains) {
            if (isDomainBlocked(domain)) {
                throw new RuntimeException(
                        "Access blocked for security: " + domain
                );
            }
        }
    }

    private Set<String> extractDomainsFromReport(IncidentReport report) {
        Set<String> domains = new LinkedHashSet<>();
        if (report == null) return domains;

        domains.addAll(extractDomains(report.getSuspiciousUrl()));
        domains.addAll(extractDomains(report.getSuspiciousEmail()));
        domains.addAll(extractDomains(report.getTextContent()));
        domains.removeIf(String::isBlank);
        return domains;
    }

    private Set<String> extractDomains(String text) {
        Set<String> domains = new LinkedHashSet<>();
        if (text == null || text.isBlank()) return domains;

        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("(?i)(https?://[^\\s|]+|www\\.[^\\s|]+|[a-z0-9.-]+\\.[a-z]{2,})")
                .matcher(text);

        while (matcher.find()) {
            String token = matcher.group(1);
            String domain = normalizeDomain(token);
            if (!domain.isBlank()) {
                domains.add(domain);
            }
        }
        return domains;
    }

    private String normalizeDomain(String domainOrUrl) {
        if (domainOrUrl == null || domainOrUrl.isBlank()) {
            return "";
        }
        try {
            String candidate = domainOrUrl.trim();
            if (!candidate.matches("^[a-zA-Z][a-zA-Z0-9+.-]*://.*")) {
                if (candidate.contains("@")) {
                    candidate = candidate.substring(candidate.lastIndexOf('@') + 1);
                }
                candidate = "https://" + candidate;
            }
            URI uri = URI.create(candidate);
            String host = uri.getHost();
            if (host == null) return "";
            return host.toLowerCase().replaceFirst("^www\\.", "");
        } catch (Exception ex) {
            String fallback = domainOrUrl.trim().toLowerCase();
            int at = fallback.lastIndexOf('@');
            if (at >= 0 && at < fallback.length() - 1) {
                fallback = fallback.substring(at + 1);
            }
            fallback = fallback.replaceFirst("^www\\.", "");
            int slash = fallback.indexOf('/');
            if (slash > 0) {
                fallback = fallback.substring(0, slash);
            }
            return fallback;
        }
    }

    private void addBlockedDomain(
            String domain,
            String blockedBy,
            String reason,
            String riskLevel) {
        if (domain == null || domain.isBlank()) return;
        String normalized = normalizeDomain(domain);
        if (normalized.isBlank()) return;
        BLOCKED_DOMAINS.putIfAbsent(
                normalized,
                new BlockedDomainEntry(
                        BLOCKED_ID_SEQ.getAndIncrement(),
                        normalized,
                        blockedBy,
                        LocalDateTime.now(),
                        reason,
                        riskLevel
                )
        );
    }

    private static class BlockedDomainEntry {
        private final long id;
        private final String domain;
        private final String blockedBy;
        private final LocalDateTime blockedTime;
        private final String reason;
        private final String riskLevel;

        private BlockedDomainEntry(
                long id,
                String domain,
                String blockedBy,
                LocalDateTime blockedTime,
                String reason,
                String riskLevel) {
            this.id = id;
            this.domain = domain;
            this.blockedBy = blockedBy;
            this.blockedTime = blockedTime;
            this.reason = reason;
            this.riskLevel = riskLevel;
        }
    }

    private String buildInputContent(IncidentReport report) {
        if (report == null) {
            return "N/A";
        }

        String type = report.getType() == null
                ? ""
                : report.getType().trim().toUpperCase();

        if ("URL".equals(type)) {
            return safeLine(report.getSuspiciousUrl(), 220);
        }
        if ("EMAIL".equals(type)) {
            String sender = safeLine(report.getSuspiciousEmail(), 120);
            String subject = extractToken(report.getTextContent(), "Subject:");
            if (subject == null || subject.isBlank()) {
                subject = "N/A";
            }
            return "Sender: " + sender
                    + " | Subject: " + safeLine(subject, 140);
        }
        if ("DOCUMENT".equals(type)) {
            String attackDetails = cleanNarrative(
                    extractToken(report.getTextContent(), "Extracted Text:")
            );
            return "Document Link: " + safeLine(report.getDocumentPath(), 180)
                    + " | Reported By: " + safeLine(
                    report.getReportedBy() != null
                            ? report.getReportedBy().getEmail()
                            : null,
                    120
            )
                    + " | Reported Time: " + (
                    report.getCreatedAt() == null
                            ? "N/A"
                            : report.getCreatedAt().toString()
            )
                    + " | Attack Details: " + safeLine(attackDetails, 220);
        }
        if ("IMAGE".equals(type)) {
            String fileName = extractFileName(report.getImagePath());
            String extracted = report.getTextContent();
            return "Image: " + safeLine(fileName, 100)
                    + " | OCR Text: " + safeLine(extracted, 220);
        }

        if (report.getSuspiciousUrl() != null && !report.getSuspiciousUrl().isBlank()) {
            return safeLine(report.getSuspiciousUrl(), 220);
        }
        if (report.getTextContent() != null && !report.getTextContent().isBlank()) {
            return safeLine(report.getTextContent(), 220);
        }
        if (report.getSuspiciousEmail() != null && !report.getSuspiciousEmail().isBlank()) {
            return safeLine(report.getSuspiciousEmail(), 220);
        }
        return safeLine(report.getDescription(), 220);
    }

    private String extractToken(String text, String key) {
        if (text == null || key == null) return null;
        String[] parts = text.split("\\|");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.toLowerCase().startsWith(key.toLowerCase())) {
                return trimmed.substring(key.length()).trim();
            }
        }
        return null;
    }

    private String extractFileName(String path) {
        if (path == null || path.isBlank()) return "N/A";
        int idx = path.lastIndexOf('/');
        if (idx >= 0 && idx < path.length() - 1) {
            return path.substring(idx + 1);
        }
        return path;
    }

    private String safeLine(String text, int maxLen) {
        if (text == null || text.isBlank()) return "N/A";
        String cleaned = text
                .replace("\n", " ")
                .replace("\r", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (cleaned.length() <= maxLen) {
            return cleaned;
        }
        return cleaned.substring(0, maxLen) + "...";
    }

    private String cleanNarrative(String text) {
        if (text == null || text.isBlank()) return "N/A";
        return text
                .replaceAll("(?i)=+\\s*ATTACK\\s*NARRATIVE\\s*=+", "")
                .replaceAll("(?i)=+\\s*END\\s*=+", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    // ==========================================
    // BUILD REPORT RESPONSE
    // ==========================================
    private Map<String, Object>
    buildReportResponse(
            IncidentReport report,
            AnalysisResult aiResult) {

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put("success", true);
        response.put(
                "reportId", report.getId()
        );
        response.put(
                "message",
                "Report submitted and analyzed "
                        + "by AI intelligence pipeline"
        );

        // Core report data
        Map<String, Object> reportData =
                new LinkedHashMap<>();
        reportData.put("id",      report.getId());
        reportData.put("title",   report.getTitle());
        reportData.put("status",  report.getStatus());
        reportData.put("adminStatus", report.getVerificationStatus());
        reportData.put("type", report.getType());
        reportData.put("textContent", report.getTextContent());
        reportData.put("imagePath", report.getImagePath());
        reportData.put("documentPath", report.getDocumentPath());
        reportData.put("inputContent", buildInputContent(report));
        reportData.put(
                "incidentType",
                report.getIncidentType()
        );
        reportData.put(
                "prediction",
                report.getPrediction()
        );
        reportData.put(
                "mlScore",
                report.getMlScore()
        );
        reportData.put(
                "aiScore",
                report.getAiScore()
        );
        reportData.put(
                "finalScore",
                report.getFinalScore()
        );
        reportData.put(
                "confidence",
                report.getConfidence()
        );
        reportData.put(
                "riskScore",
                report.getRiskScore()
        );
        reportData.put(
                "riskLevel",
                report.getRiskLevel()
        );
        reportData.put(
                "currentSpread",
                report.getCurrentSpread()
        );
        reportData.put(
                "predictedSpread",
                report.getPredictedSpread()
        );
        reportData.put(
                "alertStatus",
                report.getAlertStatus()
        );
        reportData.put(
                "verificationStatus",
                report.getVerificationStatus()
        );
        reportData.put(
                "adminDecision",
                report.getAdminDecision()
        );
        reportData.put(
                "reviewTime",
                report.getReviewTime()
        );
        reportData.put(
                "riskBreakdown",
                buildRiskBreakdown(report)
        );
        reportData.put(
                "investigationTimeline",
                buildInvestigationTimeline(report)
        );
        response.put("report", reportData);

        // Intelligence output
        Map<String, Object> intelligence =
                new LinkedHashMap<>();
        intelligence.put(
                "resultId",
                aiResult.getResultId()
        );
        intelligence.put(
                "threatCategory",
                aiResult.getThreatCategory()
        );
        intelligence.put(
                "threatSubCategory",
                aiResult.getThreatSubCategory()
        );
        intelligence.put(
                "threatDescription",
                aiResult.getThreatDescription()
        );
        intelligence.put(
                "prediction",
                report.getPrediction()
        );
        intelligence.put(
                "mlScore",
                report.getMlScore()
        );
        intelligence.put(
                "aiScore",
                report.getAiScore()
        );
        intelligence.put(
                "finalScore",
                report.getFinalScore()
        );
        intelligence.put(
                "attackStage",
                aiResult.getDetectedStage()
        );
        intelligence.put(
                "isActiveAttack",
                aiResult.isActiveAttack()
        );
        intelligence.put(
                "confidenceScore",
                aiResult.getConfidencePercentage()
        );
        intelligence.put("mlPrediction", report.getPrediction());
        intelligence.put("mlConfidence", report.getMlScore());
        intelligence.put("mlRiskLevel", report.getRiskLevel());
        intelligence.put(
                "patternMatched",
                aiResult.isMatchedKnownPattern()
        );
        intelligence.put(
                "patternName",
                aiResult.getMatchedPatternName()
        );
        intelligence.put(
                "spreadProbability",
                aiResult.getSpreadProbability()
        );
        intelligence.put(
                "estimatedAffectedUsers",
                aiResult.getEstimatedAffectedUsers()
        );
        intelligence.put(
                "departments",
                reportRepository.findDistinctInstitutionsSince(
                        LocalDateTime.now().minusDays(7)
                )
        );
        intelligence.put(
                "currentSpread",
                report.getCurrentSpread()
        );
        intelligence.put(
                "predictedSpread",
                report.getPredictedSpread()
        );
        intelligence.put(
                "impactLevel",
                report.getPredictedSpread() != null && report.getPredictedSpread() >= 50
                        ? "HIGH"
                        : report.getPredictedSpread() != null && report.getPredictedSpread() >= 20
                        ? "MEDIUM"
                        : "LOW"
        );
        intelligence.put(
                "anomalyDetected",
                aiResult.isAnomalyDetected()
        );
        intelligence.put(
                "autoResponseTriggered",
                aiResult.isAutoResponseTriggered()
        );
        intelligence.put(
                "autoResponseActions",
                aiResult.getAutoResponseActions()
        );
        intelligence.put(
                "redFlagCount",
                aiResult.getRedFlags() != null
                        ? aiResult.getRedFlags().size()
                        : 0
        );
        intelligence.put(
                "detectedIndicators",
                aiResult.getRedFlags() != null
                        ? aiResult.getRedFlags().stream().limit(6).toList()
                        : Collections.emptyList()
        );
        intelligence.put(
                "topRedFlags",
                aiResult.getRedFlags() != null
                        ? aiResult.getRedFlags()
                        .stream()
                        .limit(3)
                        .toList()
                        : Collections.emptyList()
        );
        intelligence.put(
                "recommendations",
                aiResult.getRecommendations()
        );
        intelligence.put(
                "timeline",
                aiResult.getTimelineEvents()
        );
        intelligence.put(
                "processingTimeMs",
                aiResult.getProcessingTimeMs()
        );
        intelligence.put(
                "digitalTwinResult",
                aiResult.isAnomalyDetected()
                        ? aiResult.getAnomalyDescription()
                        : "No significant anomaly detected"
        );
        intelligence.put(
                "spreadAnalysis",
                Map.of(
                        "currentSpread", report.getCurrentSpread() == null ? 0 : report.getCurrentSpread(),
                        "predictedSpread", report.getPredictedSpread() == null ? 0 : report.getPredictedSpread(),
                        "affectedUsers", aiResult.getEstimatedAffectedUsers(),
                        "departments", reportRepository.findDistinctInstitutionsSince(LocalDateTime.now().minusDays(7))
                )
        );
        intelligence.put("adminStatus", report.getVerificationStatus());
        intelligence.put("riskBreakdown", buildRiskBreakdown(report));
        intelligence.put("investigationTimeline", buildInvestigationTimeline(report));
        response.put(
                "intelligence", intelligence
        );

        return response;
    }

    private Map<String, Object> buildRiskBreakdown(IncidentReport report) {
        Map<String, Object> breakdown = new LinkedHashMap<>();
        double ml = report.getMlScore() == null ? 0.0 : report.getMlScore();
        double ai = report.getAiScore() == null ? 0.0 : report.getAiScore();
        double weightedMl = ml * 0.7;
        double weightedAi = ai * 0.3;
        breakdown.put("mlScore", ml);
        breakdown.put("aiScore", ai);
        breakdown.put("indicatorWeights", Map.of("mlWeight", 0.7, "aiWeight", 0.3));
        breakdown.put("weightedMl", Math.round(weightedMl * 100.0) / 100.0);
        breakdown.put("weightedAi", Math.round(weightedAi * 100.0) / 100.0);
        breakdown.put(
                "finalScore",
                report.getFinalScore() == null ? report.getRiskScore() : report.getFinalScore()
        );
        return breakdown;
    }

    private List<Map<String, Object>> buildInvestigationTimeline(IncidentReport report) {
        LocalDateTime base = report.getCreatedAt() == null
                ? LocalDateTime.now()
                : report.getCreatedAt();
        List<Map<String, Object>> timeline = new ArrayList<>();
        timeline.add(timelineItem("Input received", base, "Submission captured"));
        timeline.add(timelineItem("Text extraction", base.plusSeconds(1), "Type-specific extraction completed"));
        timeline.add(timelineItem("ML prediction", base.plusSeconds(2), "ML confidence: " + (report.getMlScore() == null ? 0 : Math.round(report.getMlScore()))));
        timeline.add(timelineItem("AI indicators", base.plusSeconds(3), "AI score: " + (report.getAiScore() == null ? 0 : Math.round(report.getAiScore()))));
        timeline.add(timelineItem("Hybrid score", base.plusSeconds(4), "Final score: " + (report.getFinalScore() == null ? report.getRiskScore() : Math.round(report.getFinalScore()))));
        timeline.add(timelineItem("Risk assigned", base.plusSeconds(5), "Risk level: " + report.getRiskLevel()));
        timeline.add(timelineItem("Alert generated", base.plusSeconds(6), "Alert status: " + report.getAlertStatus()));
        return timeline;
    }

    private Map<String, Object> timelineItem(String step, LocalDateTime ts, String detail) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("step", step);
        row.put("timestamp", ts);
        row.put("detail", detail);
        return row;
    }

    private com.project.entity.CyberAlert.Severity mapAlertSeverityEnum(
            AnalysisResult.RiskLevel level) {

        if (level == null) {
            return com.project.entity.CyberAlert.Severity.MEDIUM;
        }

        switch (level) {
            case CRITICAL:
                return com.project.entity.CyberAlert.Severity.CRITICAL;
            case HIGH:
                return com.project.entity.CyberAlert.Severity.HIGH;
            case MEDIUM:
                return com.project.entity.CyberAlert.Severity.MEDIUM;
            case LOW:
            case NEGLIGIBLE:
            default:
                return com.project.entity.CyberAlert.Severity.LOW;
        }
    }

    // ==========================================
    // GENERATE INPUT ID
    // ==========================================
    private String generateInputId() {
        return "INP-"
                + System.currentTimeMillis()
                + "-"
                + (int)(Math.random() * 9000 + 1000);
    }
}
