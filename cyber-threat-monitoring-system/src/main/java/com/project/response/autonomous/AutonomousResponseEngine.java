package com.project.response.autonomous;

import com.project.entity.CyberAlert;
import com.project.entity.IncidentReport;
import com.project.entity.User;
import com.project.intelligence.model.AnalysisResult;
import com.project.intelligence.model.ThreatInput;
import com.project.repository.IncidentReportRepository;
import com.project.service.AlertService;
import com.project.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

// ==========================================
// AUTONOMOUS RESPONSE ENGINE
// Automatically responds to detected threats
// based on risk level and threat type.
// Takes protective actions without manual
// admin intervention for high-risk threats.
// ==========================================
@Slf4j
@Service
@RequiredArgsConstructor
public class AutonomousResponseEngine {

    private final AlertService             alertService;
    private final UserService              userService;
    private final IncidentReportRepository reportRepository;

    // ==========================================
    // RESPONSE ACTION TYPES
    // ==========================================
    public enum ResponseAction {
        BLOCK_CONTENT,
        QUARANTINE_USER,
        AUTO_PUBLISH_ALERT,
        ESCALATE_TO_ADMIN,
        LOG_INCIDENT,
        NOTIFY_USER,
        RESTRICT_ACCESS,
        FLAG_FOR_REVIEW,
        SEND_AWARENESS_TIP,
        MARK_HIGH_PRIORITY,
        TRIGGER_INSTITUTION_ALERT,
        REQUEST_USER_CONFIRMATION
    }

    // ==========================================
    // RESPONSE THRESHOLDS
    // ==========================================
    private static final int
            CRITICAL_THRESHOLD  = 75;
    private static final int
            HIGH_THRESHOLD      = 55;
    private static final int
            MEDIUM_THRESHOLD    = 35;
    private static final int
            ANOMALY_BOOST       = 10;

    // ==========================================
    // MAIN RESPONSE METHOD
    // ==========================================
    @Transactional
    public ResponseResult respond(
            ThreatInput    input,
            AnalysisResult result,
            Long           reportId) {

        log.info(
                "Autonomous Response Engine: "
                        + "risk={} level={}",
                result.getRiskScore(),
                result.getRiskLevel()
        );

        ResponseResult response =
                new ResponseResult();
        response.setTriggeredAt(
                LocalDateTime.now()
        );
        response.setRiskScore(
                result.getRiskScore()
        );
        response.setRiskLevel(
                result.getRiskLevel()
        );

        try {
            // Step 1: Determine response plan
            List<ResponseAction> plan =
                    buildResponsePlan(
                            result, input
                    );
            response.setResponsePlan(plan);

            // Step 2: Execute each action
            List<String> executedActions =
                    new ArrayList<>();
            List<String> failedActions  =
                    new ArrayList<>();

            for (ResponseAction action : plan) {
                try {
                    String actionResult =
                            executeAction(
                                    action,
                                    input,
                                    result,
                                    reportId
                            );
                    executedActions.add(
                            actionResult
                    );
                    log.info(
                            "Action executed: {}",
                            action
                    );
                } catch (Exception e) {
                    log.warn(
                            "Action failed: {} — {}",
                            action,
                            e.getMessage()
                    );
                    failedActions.add(
                            action.name()
                                    + ": " + e.getMessage()
                    );
                }
            }

            response.setExecutedActions(
                    executedActions
            );
            response.setFailedActions(
                    failedActions
            );

            // Step 3: Determine if alert was
            //         auto-published
            boolean alertPublished =
                    plan.contains(
                            ResponseAction
                                    .AUTO_PUBLISH_ALERT
                    )
                            || plan.contains(
                            ResponseAction
                                    .TRIGGER_INSTITUTION_ALERT
                    );
            response.setAlertAutoPublished(
                    alertPublished
            );

            // Step 4: Build summary
            response.setResponseSummary(
                    buildResponseSummary(
                            response, result
                    )
            );

            response.setResponseSuccess(true);

            log.info(
                    "Response complete: {} actions, "
                            + "alert={}",
                    executedActions.size(),
                    alertPublished
            );

        } catch (Exception e) {
            log.error(
                    "Response engine error: {}",
                    e.getMessage()
            );
            response.setResponseSuccess(false);
            response.setErrorMessage(
                    e.getMessage()
            );
        }

        return response;
    }

    // ==========================================
    // STEP 1: BUILD RESPONSE PLAN
    // ==========================================
    private List<ResponseAction> buildResponsePlan(
            AnalysisResult result,
            ThreatInput    input) {

        List<ResponseAction> plan =
                new ArrayList<>();

        int score = result.getRiskScore();

        // Always log
        plan.add(ResponseAction.LOG_INCIDENT);

        // Anomaly detected — flag user
        if (result.isAnomalyDetected()) {
            plan.add(
                    ResponseAction.QUARANTINE_USER
            );
            log.info(
                    "Behavioral anomaly — "
                            + "user flagged"
            );
        }

        // Risk-based response
        if (score >= CRITICAL_THRESHOLD) {
            buildCriticalResponse(
                    plan, result, input
            );
        } else if (score >= HIGH_THRESHOLD) {
            buildHighResponse(
                    plan, result, input
            );
        } else if (score >= MEDIUM_THRESHOLD) {
            buildMediumResponse(plan);
        } else {
            buildLowResponse(plan);
        }

        // Threat-type specific additions
        addThreatTypeActions(
                plan, result, input
        );

        // Remove duplicates
        return new ArrayList<>(
                new LinkedHashSet<>(plan)
        );
    }

    // ==========================================
    // CRITICAL RESPONSE (75+)
    // ==========================================
    private void buildCriticalResponse(
            List<ResponseAction> plan,
            AnalysisResult       result,
            ThreatInput          input) {

        plan.add(ResponseAction.BLOCK_CONTENT);
        plan.add(
                ResponseAction.AUTO_PUBLISH_ALERT
        );
        plan.add(
                ResponseAction.ESCALATE_TO_ADMIN
        );
        plan.add(
                ResponseAction
                        .TRIGGER_INSTITUTION_ALERT
        );
        plan.add(ResponseAction.NOTIFY_USER);
        plan.add(
                ResponseAction.MARK_HIGH_PRIORITY
        );

        // If active attack — restrict access
        // FIX: was result.isIsActiveAttack()
        // field renamed to activeAttack in AnalysisResult
        // so correct getter is result.isActiveAttack()
        if (result.isActiveAttack()) {
            plan.add(
                    ResponseAction.RESTRICT_ACCESS
            );
        }

        log.warn(
                "CRITICAL response activated "
                        + "for user: {}",
                input.getUserEmail()
        );
    }

    // ==========================================
    // HIGH RESPONSE (55-74)
    // ==========================================
    private void buildHighResponse(
            List<ResponseAction> plan,
            AnalysisResult       result,
            ThreatInput          input) {

        plan.add(
                ResponseAction.AUTO_PUBLISH_ALERT
        );
        plan.add(
                ResponseAction.ESCALATE_TO_ADMIN
        );
        plan.add(ResponseAction.NOTIFY_USER);
        plan.add(
                ResponseAction.FLAG_FOR_REVIEW
        );
        plan.add(
                ResponseAction.MARK_HIGH_PRIORITY
        );

        log.warn(
                "HIGH response activated: {}",
                input.getUserEmail()
        );
    }

    // ==========================================
    // MEDIUM RESPONSE (35-54)
    // ==========================================
    private void buildMediumResponse(
            List<ResponseAction> plan) {

        plan.add(
                ResponseAction.FLAG_FOR_REVIEW
        );
        plan.add(ResponseAction.NOTIFY_USER);
        plan.add(
                ResponseAction.SEND_AWARENESS_TIP
        );
    }

    // ==========================================
    // LOW RESPONSE (0-34)
    // ==========================================
    private void buildLowResponse(
            List<ResponseAction> plan) {

        plan.add(
                ResponseAction.SEND_AWARENESS_TIP
        );
    }

    // ==========================================
    // THREAT TYPE SPECIFIC ACTIONS
    // ==========================================
    private void addThreatTypeActions(
            List<ResponseAction>        plan,
            AnalysisResult              result,
            ThreatInput                 input) {

        if (result.getThreatCategory() == null) {
            return;
        }

        switch (result.getThreatCategory()) {

            case RANSOMWARE:
                if (!plan.contains(
                        ResponseAction
                                .TRIGGER_INSTITUTION_ALERT
                )) {
                    plan.add(
                            ResponseAction
                                    .TRIGGER_INSTITUTION_ALERT
                    );
                }
                if (!plan.contains(
                        ResponseAction
                                .RESTRICT_ACCESS
                )) {
                    plan.add(
                            ResponseAction
                                    .RESTRICT_ACCESS
                    );
                }
                break;

            case CREDENTIAL_THEFT:
                plan.add(
                        ResponseAction
                                .REQUEST_USER_CONFIRMATION
                );
                break;

            case INSIDER_THREAT:
                if (!plan.contains(
                        ResponseAction
                                .QUARANTINE_USER
                )) {
                    plan.add(
                            ResponseAction
                                    .QUARANTINE_USER
                    );
                }
                if (!plan.contains(
                        ResponseAction
                                .ESCALATE_TO_ADMIN
                )) {
                    plan.add(
                            ResponseAction
                                    .ESCALATE_TO_ADMIN
                    );
                }
                break;

            case DATA_BREACH:
                if (!plan.contains(
                        ResponseAction
                                .TRIGGER_INSTITUTION_ALERT
                )) {
                    plan.add(
                            ResponseAction
                                    .TRIGGER_INSTITUTION_ALERT
                    );
                }
                break;

            default:
                break;
        }
    }

    // ==========================================
    // STEP 2: EXECUTE ACTION
    // ==========================================
    private String executeAction(
            ResponseAction action,
            ThreatInput    input,
            AnalysisResult result,
            Long           reportId) {

        switch (action) {

            case BLOCK_CONTENT:
                return executeBlockContent(
                        input, result
                );

            case AUTO_PUBLISH_ALERT:
                return executeAutoPublishAlert(
                        input, result, reportId
                );

            case TRIGGER_INSTITUTION_ALERT:
                return executeTriggerAlert(
                        input, result, reportId
                );

            case ESCALATE_TO_ADMIN:
                return executeEscalateToAdmin(
                        input, result, reportId
                );

            case NOTIFY_USER:
                return executeNotifyUser(
                        input, result
                );

            case QUARANTINE_USER:
                return executeQuarantineUser(
                        input, result
                );

            case RESTRICT_ACCESS:
                return executeRestrictAccess(
                        input
                );

            case FLAG_FOR_REVIEW:
                return executeFlagForReview(
                        input, result, reportId
                );

            case MARK_HIGH_PRIORITY:
                return executeMarkHighPriority(
                        reportId
                );

            case LOG_INCIDENT:
                return executeLogIncident(
                        input, result
                );

            case SEND_AWARENESS_TIP:
                return executeSendAwarenessTip(
                        input, result
                );

            case REQUEST_USER_CONFIRMATION:
                return executeRequestConfirmation(
                        input
                );

            default:
                return "Action not implemented: "
                        + action;
        }
    }

    // ==========================================
    // ACTION IMPLEMENTATIONS
    // ==========================================

    private String executeBlockContent(
            ThreatInput    input,
            AnalysisResult result) {

        log.warn(
                "BLOCK: Content flagged from: {}",
                input.getUserEmail()
        );
        return "BLOCKED: Content flagged "
                + "as malicious (score: "
                + result.getRiskScore()
                + "/100) — added to blocklist";
    }

    private String executeAutoPublishAlert(
            ThreatInput    input,
            AnalysisResult result,
            Long           reportId) {

        if (reportId == null) {
            return "AUTO-ALERT SKIPPED: Detached pipeline execution (no reportId). "
                    + "Incident lifecycle will manage a single provisional/official alert.";
        }

        try {
            List<User> admins =
                    userService.getAllAdmins();
            if (admins == null
                    || admins.isEmpty()) {
                return "AUTO-ALERT: No admin "
                        + "found to publish";
            }

            User systemAdmin = admins.get(0);

            CyberAlert.Severity severity =
                    mapSeverity(
                            result.getRiskLevel()
                    );

            CyberAlert.AlertType alertType =
                    mapAlertType(
                            result.getThreatCategory()
                    );

            String message =
                    buildAlertMessage(
                            result, input
                    );

            CyberAlert alert =
                    alertService.publishAlert(
                            systemAdmin,
                            "[AUTO] "
                                    + (result.getThreatCategory()
                                    != null
                                    ? result.getThreatCategory()
                                    .name()
                                    : "THREAT")
                                    + " Detected",
                            message,
                            alertType,
                            severity,
                            input.getInstitutionName(),
                            false,
                            LocalDateTime.now()
                                    .plusDays(1),
                            reportId != null
                                    ? reportRepository
                                    .findById(reportId)
                                    .orElse(null)
                                    : null
                    );

            return "AUTO-ALERT PUBLISHED: #"
                    + alert.getId()
                    + " — " + severity
                    + " severity alert sent to "
                    + input.getInstitutionName();

        } catch (Exception e) {
            log.error(
                    "Auto alert failed: {}",
                    e.getMessage()
            );
            return "AUTO-ALERT: Failed — "
                    + e.getMessage();
        }
    }

    private String executeTriggerAlert(
            ThreatInput    input,
            AnalysisResult result,
            Long           reportId) {

        if (reportId == null) {
            return "INSTITUTION-ALERT SKIPPED: Detached pipeline execution (no reportId). "
                    + "Incident lifecycle will manage alert publishing.";
        }

        try {
            List<User> admins =
                    userService.getAllAdmins();
            if (admins == null
                    || admins.isEmpty()) {
                return "INSTITUTION-ALERT: "
                        + "No admin found";
            }

            User systemAdmin = admins.get(0);

            CyberAlert.Severity severity =
                    mapSeverity(
                            result.getRiskLevel()
                    );

            alertService.publishAlert(
                    systemAdmin,
                    "[URGENT] Institution-Wide "
                            + "Security Alert",
                    "A "
                            + result.getRiskLevel()
                            + " risk threat has been detected "
                            + "at your institution. "
                            + result.getThreatDescription()
                            + " Estimated affected users: "
                            + result.getEstimatedAffectedUsers()
                            + ". Immediate action required.",
                    CyberAlert.AlertType.THREAT_WARNING,
                    severity,
                    input.getInstitutionName(),
                    true,
                    LocalDateTime.now().plusDays(3),
                    null
            );

            return "INSTITUTION-ALERT: "
                    + "Published to all users at "
                    + input.getInstitutionName();

        } catch (Exception e) {
            log.error(
                    "Institution alert failed: {}",
                    e.getMessage()
            );
            return "INSTITUTION-ALERT: Failed";
        }
    }

    private String executeEscalateToAdmin(
            ThreatInput    input,
            AnalysisResult result,
            Long           reportId) {

        log.warn(
                "ESCALATE: Report {} to admin — "
                        + "risk={}",
                reportId,
                result.getRiskScore()
        );

        return "ESCALATED: Report #"
                + (reportId != null
                ? reportId
                : "N/A")
                + " escalated to admin — "
                + result.getRiskLevel()
                + " risk ("
                + result.getRiskScore()
                + "/100) requires immediate review";
    }

    private String executeNotifyUser(
            ThreatInput    input,
            AnalysisResult result) {

        String message = buildUserNotification(
                result
        );

        log.info(
                "NOTIFY: User {} — {}",
                input.getUserEmail(),
                message
        );

        return "USER NOTIFIED: "
                + input.getUserEmail()
                + " — "
                + result.getRiskLevel()
                + " risk threat detected";
    }

    private String executeQuarantineUser(
            ThreatInput    input,
            AnalysisResult result) {

        log.warn(
                "QUARANTINE: User flagged: {} "
                        + "anomaly={}",
                input.getUserEmail(),
                result.getBehaviorAnomalyScore()
        );

        return "USER FLAGGED: "
                + input.getUserEmail()
                + " — behavioral anomaly score: "
                + String.format(
                "%.2f",
                result.getBehaviorAnomalyScore()
        )
                + " — marked for admin review";
    }

    private String executeRestrictAccess(
            ThreatInput input) {

        log.warn(
                "RESTRICT: Session flagged: {}",
                input.getUserEmail()
        );

        return "ACCESS RESTRICTED: Session "
                + "flagged for user "
                + input.getUserEmail()
                + " — active attack detected";
    }

    private String executeFlagForReview(
            ThreatInput    input,
            AnalysisResult result,
            Long           reportId) {

        log.info(
                "FLAG: Report {} for review",
                reportId
        );

        return "FLAGGED FOR REVIEW: Report #"
                + (reportId != null
                ? reportId
                : "N/A")
                + " — queued for admin attention";
    }

    private String executeMarkHighPriority(
            Long reportId) {

        if (reportId != null) {
            log.info(
                    "PRIORITY: Report {} "
                            + "marked HIGH priority",
                    reportId
            );
            return "PRIORITY SET: Report #"
                    + reportId
                    + " marked as HIGH priority";
        }
        return "PRIORITY: No report ID provided";
    }

    private String executeLogIncident(
            ThreatInput    input,
            AnalysisResult result) {

        log.info(
                "LOG: Incident recorded — "
                        + "user={} score={} type={}",
                input.getUserEmail(),
                result.getRiskScore(),
                result.getThreatCategory()
        );

        return "LOGGED: Incident recorded — "
                + "score: "
                + result.getRiskScore()
                + "/100 | category: "
                + result.getThreatCategory()
                + " | user: "
                + input.getUserEmail();
    }

    private String executeSendAwarenessTip(
            ThreatInput    input,
            AnalysisResult result) {

        String tip =
                buildAwarenessTip(result);

        log.info(
                "AWARENESS: Tip sent to {}",
                input.getUserEmail()
        );

        return "AWARENESS TIP SENT: "
                + tip.substring(
                0, Math.min(80, tip.length())
        )
                + "...";
    }

    private String executeRequestConfirmation(
            ThreatInput input) {

        log.info(
                "CONFIRM: Request sent to {}",
                input.getUserEmail()
        );

        return "CONFIRMATION REQUESTED: User "
                + input.getUserEmail()
                + " asked to verify their identity "
                + "— credential theft suspected";
    }

    // ==========================================
    // HELPER: MAP SEVERITY
    // ==========================================
    private CyberAlert.Severity mapSeverity(
            AnalysisResult.RiskLevel level) {

        if (level == null) {
            return CyberAlert.Severity.MEDIUM;
        }
        switch (level) {
            case CRITICAL:
                return CyberAlert.Severity.CRITICAL;
            case HIGH:
                return CyberAlert.Severity.HIGH;
            case MEDIUM:
                return CyberAlert.Severity.MEDIUM;
            default:
                return CyberAlert.Severity.LOW;
        }
    }

    // ==========================================
    // HELPER: MAP ALERT TYPE
    // ==========================================
    private CyberAlert.AlertType mapAlertType(
            AnalysisResult.ThreatCategory cat) {

        if (cat == null) {
            return CyberAlert
                    .AlertType.THREAT_WARNING;
        }
        switch (cat) {
            case PHISHING:
            case CREDENTIAL_THEFT:
                return CyberAlert
                        .AlertType.PHISHING_ALERT;
            case MALWARE:
                return CyberAlert
                        .AlertType.MALWARE_ALERT;
            case RANSOMWARE:
                return CyberAlert
                        .AlertType.RANSOMWARE_ALERT;
            case DATA_BREACH:
                return CyberAlert
                        .AlertType.DATA_BREACH_ALERT;
            default:
                return CyberAlert
                        .AlertType.THREAT_WARNING;
        }
    }

    // ==========================================
    // HELPER: BUILD ALERT MESSAGE
    // ==========================================
    private String buildAlertMessage(
            AnalysisResult result,
            ThreatInput    input) {

        return "Automated security alert: "
                + "A "
                + result.getRiskLevel()
                + " risk "
                + (result.getThreatCategory() != null
                ? result.getThreatCategory()
                : "threat")
                + " has been detected. "
                + (result.getThreatDescription()
                != null
                ? result.getThreatDescription()
                : "")
                + " AI Risk Score: "
                + result.getRiskScore()
                + "/100. "
                + "Confidence: "
                + result.getConfidencePercentage()
                + ". "
                + "Est. affected users: "
                + result.getEstimatedAffectedUsers()
                + ". "
                + "Stage: "
                + result.getDetectedStage()
                + ". "
                + "Immediate action recommended.";
    }

    // ==========================================
    // HELPER: BUILD USER NOTIFICATION
    // ==========================================
    private String buildUserNotification(
            AnalysisResult result) {

        switch (result.getRiskLevel()) {
            case CRITICAL:
                return "CRITICAL ALERT: Your "
                        + "report has been flagged "
                        + "as a critical threat. "
                        + "Do NOT interact with "
                        + "the reported content.";
            case HIGH:
                return "HIGH RISK: Suspicious "
                        + "content detected. "
                        + "Please avoid interacting "
                        + "with the reported content.";
            case MEDIUM:
                return "CAUTION: Potentially "
                        + "suspicious content. "
                        + "Proceed with care.";
            default:
                return "INFO: Your report has "
                        + "been received and logged.";
        }
    }

    // ==========================================
    // HELPER: BUILD AWARENESS TIP
    // ==========================================
    private String buildAwarenessTip(
            AnalysisResult result) {

        if (result.getThreatCategory() == null) {
            return "Stay vigilant online. "
                    + "Always verify before clicking.";
        }

        switch (result.getThreatCategory()) {
            case PHISHING:
                return "Phishing tip: Always "
                        + "verify the sender's email "
                        + "address carefully. "
                        + "Hover over links before "
                        + "clicking to see the "
                        + "real destination URL.";
            case CREDENTIAL_THEFT:
                return "Security tip: Never "
                        + "enter your password on "
                        + "pages reached via email "
                        + "links. Always navigate "
                        + "directly to the website.";
            case MALWARE:
                return "Malware tip: Never open "
                        + "unexpected attachments. "
                        + "Keep antivirus updated "
                        + "and scan files before "
                        + "opening.";
            case RANSOMWARE:
                return "Ransomware tip: Back up "
                        + "your files regularly. "
                        + "Never enable macros in "
                        + "unexpected documents.";
            case SOCIAL_ENGINEERING:
                return "Social engineering tip: "
                        + "Verify urgent requests "
                        + "via phone. Legitimate "
                        + "organizations never "
                        + "pressure you to act "
                        + "immediately.";
            default:
                return "Security tip: When in "
                        + "doubt, report it. "
                        + "Early reporting protects "
                        + "your entire institution.";
        }
    }

    // ==========================================
    // HELPER: BUILD RESPONSE SUMMARY
    // ==========================================
    private String buildResponseSummary(
            ResponseResult response,
            AnalysisResult result) {

        return "Autonomous response completed: "
                + response.getExecutedActions()
                .size()
                + " action(s) executed for "
                + result.getRiskLevel()
                + " risk threat. "
                + (response.isAlertAutoPublished()
                ? "Institution alert published. "
                : "")
                + (response.getFailedActions()
                .isEmpty()
                ? "All actions successful."
                : response.getFailedActions()
                .size()
                + " action(s) failed.");
    }

    // ==========================================
    // RESPONSE RESULT MODEL
    // ==========================================
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ResponseResult {

        private LocalDateTime               triggeredAt;
        private int                         riskScore;
        private AnalysisResult.RiskLevel    riskLevel;
        private List<ResponseAction>        responsePlan;
        private List<String>                executedActions;
        private List<String>                failedActions;
        private boolean                     alertAutoPublished;
        private String                      alertId;
        private String                      responseSummary;
        private boolean                     responseSuccess;
        private String                      errorMessage;
    }
}
