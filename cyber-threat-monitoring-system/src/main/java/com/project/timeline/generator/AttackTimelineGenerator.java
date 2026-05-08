package com.project.timeline.generator;

import com.project.intelligence.model.AnalysisResult;
import com.project.intelligence.model.ThreatInput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

// ==========================================
// ATTACK TIMELINE GENERATOR
// Builds a comprehensive chronological
// attack story from all pipeline stages.
// Generates human-readable timeline events
// explaining what happened, when, and why.
// ==========================================
@Slf4j
@Component
public class AttackTimelineGenerator {

    private static final DateTimeFormatter
            FORMATTER = DateTimeFormatter
            .ofPattern("HH:mm:ss");

    // ==========================================
    // TIMELINE EVENT TYPES
    // ==========================================
    public enum EventType {
        INPUT_RECEIVED,
        SANDBOX_ANALYSIS,
        ML_DETECTION,
        PATTERN_MATCHED,
        THREAT_CLASSIFIED,
        KILL_CHAIN_MAPPED,
        DIGITAL_TWIN_UPDATED,
        ANOMALY_DETECTED,
        SPREAD_SIMULATED,
        RESPONSE_TRIGGERED,
        ALERT_PUBLISHED,
        ADMIN_NOTIFIED,
        USER_NOTIFIED,
        INCIDENT_LOGGED,
        EVIDENCE_EXTRACTED,
        RECOMMENDATION_GENERATED,
        ANALYSIS_COMPLETE
    }

    // ==========================================
    // SEVERITY LEVELS
    // ==========================================
    public enum EventSeverity {
        INFO,
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    // ==========================================
    // MAIN GENERATE METHOD
    // ==========================================
    public TimelineReport generate(
            ThreatInput    input,
            AnalysisResult result) {

        log.info(
                "Timeline Generator starting"
        );

        LocalDateTime baseTime =
                LocalDateTime.now();

        TimelineReport report =
                new TimelineReport();
        report.setGeneratedAt(baseTime);
        report.setInputType(
                input.getInputType().name()
        );
        report.setUserEmail(
                input.getUserEmail()
        );
        report.setInstitution(
                input.getInstitutionName()
        );
        report.setFinalRiskScore(
                result.getRiskScore()
        );
        report.setFinalRiskLevel(
                result.getRiskLevel() != null
                        ? result.getRiskLevel().name()
                        : "UNKNOWN"
        );
        report.setThreatCategory(
                result.getThreatCategory() != null
                        ? result.getThreatCategory().name()
                        : "UNKNOWN"
        );

        List<TimelineEvent> events =
                new ArrayList<>();

        // ==============================
        // EVENT 1: Input Received
        // ==============================
        events.add(buildEvent(
                "EVT-001",
                EventType.INPUT_RECEIVED,
                baseTime.minusSeconds(10),
                "Threat Report Received",
                buildInputDescription(input),
                EventSeverity.INFO,
                buildInputMetadata(input)
        ));

        // ==============================
        // EVENT 2: Sandbox Analysis
        // ==============================
        events.add(buildEvent(
                "EVT-002",
                EventType.SANDBOX_ANALYSIS,
                baseTime.minusSeconds(9),
                "Sandbox Analysis Executed",
                buildSandboxDescription(result),
                result.getRiskScore() >= 55
                        ? EventSeverity.HIGH
                        : EventSeverity.INFO,
                buildSandboxMetadata(result)
        ));

        // ==============================
        // EVENT 3: ML Detection
        // ==============================
        events.add(buildEvent(
                "EVT-003",
                EventType.ML_DETECTION,
                baseTime.minusSeconds(8),
                "ML Engine Analysis Complete",
                buildMlDescription(result),
                mapRiskToSeverity(
                        result.getRiskScore()
                ),
                buildMlMetadata(result)
        ));

        // ==============================
        // EVENT 4: Pattern Match
        // (if matched)
        // ==============================
        if (result.isMatchedKnownPattern()) {
            events.add(buildEvent(
                    "EVT-004",
                    EventType.PATTERN_MATCHED,
                    baseTime.minusSeconds(7),
                    "Known Attack Pattern Identified",
                    "This threat matches known "
                            + "pattern: '"
                            + result.getMatchedPatternName()
                            + "' with "
                            + String.format(
                            "%.0f%%",
                            result.getPatternSimilarity()
                                    * 100
                    )
                            + " similarity. "
                            + "Pattern library confirmed "
                            + "this attack signature.",
                    EventSeverity.HIGH,
                    buildPatternMetadata(result)
            ));
        }

        // ==============================
        // EVENT 5: Threat Classification
        // ==============================
        events.add(buildEvent(
                "EVT-005",
                EventType.THREAT_CLASSIFIED,
                baseTime.minusSeconds(6),
                "Threat Category Identified",
                buildClassificationDescription(result),
                result.isCriticalThreat()
                        ? EventSeverity.CRITICAL
                        : EventSeverity.MEDIUM,
                buildClassificationMetadata(result)
        ));

        // ==============================
        // EVENT 6: Kill Chain Mapping
        // ==============================
        if (result.getDetectedStage() != null
                && result.getDetectedStage()
                != AnalysisResult
                .AttackStage.UNKNOWN) {
            events.add(buildEvent(
                    "EVT-006",
                    EventType.KILL_CHAIN_MAPPED,
                    baseTime.minusSeconds(5),
                    "Cyber Kill Chain Stage Detected",
                    buildKillChainDescription(result),
                    // FIX: was result.isIsActiveAttack()
                    result.isActiveAttack()
                            ? EventSeverity.CRITICAL
                            : EventSeverity.HIGH,
                    buildKillChainMetadata(result)
            ));
        }

        // ==============================
        // EVENT 7: Digital Twin Update
        // ==============================
        if (result.isDigitalTwinUpdated()) {
            events.add(buildEvent(
                    "EVT-007",
                    EventType.DIGITAL_TWIN_UPDATED,
                    baseTime.minusSeconds(4),
                    "User Behavioral Profile Updated",
                    buildDigitalTwinDescription(result),
                    result.isAnomalyDetected()
                            ? EventSeverity.HIGH
                            : EventSeverity.INFO,
                    buildDigitalTwinMetadata(result)
            ));
        }

        // ==============================
        // EVENT 8: Anomaly Detected
        // (if any)
        // ==============================
        if (result.isAnomalyDetected()) {
            events.add(buildEvent(
                    "EVT-008",
                    EventType.ANOMALY_DETECTED,
                    baseTime.minusSeconds(4),
                    "Behavioral Anomaly Flagged",
                    "User behavior deviates from "
                            + "established baseline. "
                            + (result.getAnomalyDescription()
                            != null
                            ? result.getAnomalyDescription()
                            : "Unusual activity pattern "
                            + "detected.")
                            + " Digital twin anomaly score: "
                            + String.format(
                            "%.2f",
                            result.getBehaviorAnomalyScore()
                    ),
                    EventSeverity.HIGH,
                    Map.of(
                            "anomalyScore",
                            result.getBehaviorAnomalyScore(),
                            "description",
                            result.getAnomalyDescription()
                                    != null
                                    ? result.getAnomalyDescription()
                                    : "N/A"
                    )
            ));
        }

        // ==============================
        // EVENT 9: Evidence Extraction
        // ==============================
        if (hasExtractedEvidence(result)) {
            events.add(buildEvent(
                    "EVT-009",
                    EventType.EVIDENCE_EXTRACTED,
                    baseTime.minusSeconds(3),
                    "Digital Evidence Extracted",
                    buildEvidenceDescription(result),
                    EventSeverity.MEDIUM,
                    buildEvidenceMetadata(result)
            ));
        }

        // ==============================
        // EVENT 10: Spread Simulation
        // ==============================
        if (result.getSpreadProbability() > 0.1) {
            events.add(buildEvent(
                    "EVT-010",
                    EventType.SPREAD_SIMULATED,
                    baseTime.minusSeconds(3),
                    "Threat Spread Simulation Run",
                    buildSpreadDescription(result),
                    result.getSpreadProbability()
                            > 0.65
                            ? EventSeverity.HIGH
                            : EventSeverity.MEDIUM,
                    buildSpreadMetadata(result)
            ));
        }

        // ==============================
        // EVENT 11: Auto Response
        // ==============================
        if (result.isAutoResponseTriggered()) {
            events.add(buildEvent(
                    "EVT-011",
                    EventType.RESPONSE_TRIGGERED,
                    baseTime.minusSeconds(2),
                    "Autonomous Response Activated",
                    buildResponseDescription(result),
                    result.getRiskScore() >= 75
                            ? EventSeverity.CRITICAL
                            : EventSeverity.HIGH,
                    buildResponseMetadata(result)
            ));
        }

        // ==============================
        // EVENT 12: Alert Published
        // ==============================
        if (result.isAutoAlertPublished()) {
            events.add(buildEvent(
                    "EVT-012",
                    EventType.ALERT_PUBLISHED,
                    baseTime.minusSeconds(1),
                    "Security Alert Auto-Published",
                    "System automatically published "
                            + "a "
                            + (result.getRiskLevel() != null
                            ? result.getRiskLevel().name()
                            : "HIGH")
                            + " severity alert to "
                            + (input.getInstitutionName()
                            != null
                            ? input.getInstitutionName()
                            : "the institution")
                            + ". All users have been notified.",
                    EventSeverity.CRITICAL,
                    Map.of(
                            "alertId",
                            result.getAutoAlertId() != null
                                    ? result.getAutoAlertId()
                                    : "N/A",
                            "severity",
                            result.getRiskLevel() != null
                                    ? result.getRiskLevel()
                                    .name()
                                    : "HIGH"
                    )
            ));
        }

        // ==============================
        // EVENT 13: Recommendations
        // ==============================
        if (result.getRecommendations() != null
                && !result.getRecommendations()
                .isEmpty()) {
            events.add(buildEvent(
                    "EVT-013",
                    EventType.RECOMMENDATION_GENERATED,
                    baseTime,
                    "Security Recommendations Generated",
                    result.getRecommendations()
                            .size()
                            + " actionable security "
                            + "recommendation(s) generated "
                            + "based on threat analysis. "
                            + "Top recommendation: "
                            + result.getRecommendations()
                            .get(0),
                    EventSeverity.INFO,
                    Map.of(
                            "count",
                            result.getRecommendations()
                                    .size(),
                            "topRecommendation",
                            result.getRecommendations()
                                    .get(0)
                    )
            ));
        }

        // ==============================
        // EVENT 14: Analysis Complete
        // ==============================
        events.add(buildEvent(
                "EVT-014",
                EventType.ANALYSIS_COMPLETE,
                baseTime,
                "Intelligence Analysis Complete",
                buildCompletionDescription(result),
                EventSeverity.INFO,
                buildCompletionMetadata(result)
        ));

        report.setEvents(events);
        report.setTotalEvents(events.size());
        report.setAttackNarrative(
                buildAttackNarrative(
                        input, result, events
                )
        );
        report.setPipelineStages(
                result.getPipelineStagesCompleted()
        );
        report.setProcessingTimeMs(
                result.getProcessingTimeMs()
        );

        log.info(
                "Timeline generated: {} events",
                events.size()
        );

        return report;
    }

    // ==========================================
    // BUILD EVENT HELPER
    // ==========================================
    private TimelineEvent buildEvent(
            String            eventId,
            EventType         type,
            LocalDateTime     time,
            String            title,
            String            description,
            EventSeverity     severity,
            Map<String,Object>metadata) {

        TimelineEvent event =
                new TimelineEvent();
        event.setEventId(eventId);
        event.setEventType(type.name());
        event.setEventTime(time);
        event.setEventTimeFormatted(
                time.format(FORMATTER)
        );
        event.setTitle(title);
        event.setDescription(description);
        event.setSeverity(severity.name());
        event.setMetadata(metadata);
        return event;
    }

    // ==========================================
    // DESCRIPTION BUILDERS
    // ==========================================

    private String buildInputDescription(
            ThreatInput input) {

        StringBuilder sb = new StringBuilder();
        sb.append("User ")
                .append(input.getUserEmail())
                .append(" submitted a ")
                .append(input.getInputType())
                .append(" report");

        if (input.getInstitutionName() != null) {
            sb.append(" from ")
                    .append(input.getInstitutionName());
        }

        switch (input.getInputType()) {
            case URL:
                if (input.getRawUrl() != null) {
                    sb.append(". URL: ")
                            .append(
                                    input.getRawUrl()
                                            .substring(
                                                    0,
                                                    Math.min(
                                                            60,
                                                            input.getRawUrl()
                                                                    .length()
                                                    )
                                            )
                            )
                            .append("...");
                }
                break;
            case EMAIL_CONTENT:
                if (input.getEmailSubject()
                        != null) {
                    sb.append(
                                    ". Email subject: '"
                            )
                            .append(
                                    input.getEmailSubject()
                            )
                            .append("'");
                }
                break;
            case PDF_DOCUMENT:
                if (input.getFileName() != null) {
                    sb.append(". File: ")
                            .append(input.getFileName());
                }
                break;
            case IMAGE_SCREENSHOT:
                if (input.getImageName() != null) {
                    sb.append(". Image: ")
                            .append(input.getImageName());
                }
                break;
            default:
                break;
        }

        sb.append(
                ". Intelligence pipeline initiated."
        );
        return sb.toString();
    }

    private String buildSandboxDescription(
            AnalysisResult result) {

        return "Safe simulation environment "
                + "analyzed the content without "
                + "executing potentially malicious "
                + "code. Static analysis, behavioral "
                + "pattern detection, payload "
                + "extraction and obfuscation "
                + "checks completed. "
                + (result.getRiskScore() >= 55
                ? "Multiple suspicious indicators "
                + "found during sandbox analysis."
                : "No critical indicators "
                + "found in sandbox.");
    }

    private String buildMlDescription(
            AnalysisResult result) {

        return "Machine learning engine analyzed "
                + result.getScoreBreakdown()
                .size()
                + " feature(s) and calculated "
                + "a risk score of "
                + result.getRiskScore()
                + "/100 with "
                + result.getConfidencePercentage()
                + " confidence. "
                + "Primary risk contributors: "
                + buildTopScores(result)
                + ".";
    }

    private String buildClassificationDescription(
            AnalysisResult result) {

        return "AI classified this threat as "
                + (result.getThreatCategory() != null
                ? result.getThreatCategory()
                : "UNKNOWN")
                + " → "
                + (result.getThreatSubCategory()
                != null
                ? result.getThreatSubCategory()
                : "")
                + ". "
                + (result.getThreatDescription()
                != null
                ? result.getThreatDescription()
                : "")
                + " Risk level assigned: "
                + result.getRiskLevel()
                + " ("
                + result.getRiskScore()
                + "/100).";
    }

    private String buildKillChainDescription(
            AnalysisResult result) {

        return "Attack mapped to Cyber Kill Chain "
                + "stage: "
                + result.getDetectedStage()
                + ". "
                + (result.getStageDescription()
                != null
                ? result.getStageDescription()
                : "")
                + " Kill chain progression: "
                + buildProgressionString(result)
                + ". "
                // FIX: was result.isIsActiveAttack()
                + (result.isActiveAttack()
                ? "⚠️ ACTIVE ATTACK in progress!"
                : "Attack is in preparation phase.");
    }

    private String buildDigitalTwinDescription(
            AnalysisResult result) {

        if (result.isAnomalyDetected()) {
            return "User behavioral profile "
                    + "shows deviation from baseline. "
                    + "Anomaly score: "
                    + String.format(
                    "%.2f",
                    result.getBehaviorAnomalyScore()
            )
                    + ". "
                    + (result.getAnomalyDescription()
                    != null
                    ? result.getAnomalyDescription()
                    : "Unusual pattern detected.")
                    + " Digital twin updated "
                    + "with new behavioral data.";
        }

        return "User behavioral profile updated. "
                + "Activity is within normal baseline. "
                + "Digital twin learning iteration "
                + "recorded.";
    }

    private String buildEvidenceDescription(
            AnalysisResult result) {

        StringBuilder sb = new StringBuilder();
        sb.append("Digital forensics extracted: ");

        int urlCount = result.getExtractedUrls()
                != null
                ? result.getExtractedUrls().size()
                : 0;
        int emailCount =
                result.getExtractedEmails() != null
                        ? result.getExtractedEmails().size()
                        : 0;
        int ipCount = result.getExtractedIps()
                != null
                ? result.getExtractedIps().size()
                : 0;
        int kwCount =
                result.getExtractedKeywords() != null
                        ? result.getExtractedKeywords().size()
                        : 0;

        sb.append(urlCount).append(" URL(s), ");
        sb.append(emailCount)
                .append(" email(s), ");
        sb.append(ipCount).append(" IP(s), ");
        sb.append(kwCount)
                .append(" keyword(s). ");

        if (result.getRedFlags() != null
                && !result.getRedFlags().isEmpty()) {
            sb.append(
                            result.getRedFlags().size()
                    )
                    .append(" red flag(s) documented.");
        }

        return sb.toString();
    }

    private String buildSpreadDescription(
            AnalysisResult result) {

        return "Threat spread simulation "
                + "predicts "
                + String.format(
                "%.0f%%",
                result.getSpreadProbability()
                        * 100
        )
                + " probability of spreading "
                + "to approximately "
                + result.getEstimatedAffectedUsers()
                + " user(s) across "
                + (result.getAffectedInstitutions()
                != null
                ? result.getAffectedInstitutions()
                .size()
                : 1)
                + " institution(s). "
                + "Spread depth: "
                + result.getSpreadDepth()
                + " level(s). "
                + (result.getSpreadProbability()
                > 0.65
                ? "High spread risk — "
                + "immediate containment "
                + "recommended."
                : "Contained spread risk.");
    }

    private String buildResponseDescription(
            AnalysisResult result) {

        int actionCount =
                result.getAutoResponseActions()
                        != null
                        ? result.getAutoResponseActions()
                        .size()
                        : 0;

        return "Autonomous Response Engine "
                + "activated "
                + actionCount
                + " protective action(s). "
                + (result.getAutoResponseActions()
                != null
                && !result
                .getAutoResponseActions()
                .isEmpty()
                ? "Actions: "
                + String.join(
                " | ",
                result.getAutoResponseActions()
                        .subList(
                                0,
                                Math.min(
                                        3,
                                        result
                                                .getAutoResponseActions()
                                                .size()
                                )
                        )
        )
                : "")
                + (result.isAutoAlertPublished()
                ? " Institution alert published."
                : "");
    }

    private String buildCompletionDescription(
            AnalysisResult result) {

        return "9-stage intelligence pipeline "
                + "completed in "
                + result.getProcessingTimeMs()
                + "ms. Final verdict: "
                + result.getRiskLevel()
                + " risk ("
                + result.getRiskScore()
                + "/100) with "
                + result.getConfidencePercentage()
                + " confidence. "
                + (result.isMatchedKnownPattern()
                ? "Matched known pattern: "
                + result.getMatchedPatternName()
                + ". "
                : "")
                + (result.getRecommendations() != null
                ? result.getRecommendations()
                .size()
                + " recommendation(s) provided."
                : "");
    }

    // ==========================================
    // METADATA BUILDERS
    // ==========================================

    private Map<String, Object>
    buildInputMetadata(ThreatInput input) {

        Map<String, Object> meta =
                new LinkedHashMap<>();
        meta.put(
                "inputType",
                input.getInputType().name()
        );
        meta.put("userId",    input.getUserId());
        meta.put("userEmail", input.getUserEmail());
        meta.put(
                "institution",
                input.getInstitutionName()
        );
        if (input.getRawUrl() != null) {
            meta.put("url", input.getRawUrl());
        }
        if (input.getEmailSubject() != null) {
            meta.put(
                    "emailSubject",
                    input.getEmailSubject()
            );
        }
        if (input.getFileName() != null) {
            meta.put("fileName", input.getFileName());
        }
        return meta;
    }

    private Map<String, Object>
    buildSandboxMetadata(
            AnalysisResult result) {

        Map<String, Object> meta =
                new LinkedHashMap<>();
        meta.put(
                "completed",
                result.isSandboxCompleted()
        );
        meta.put(
                "flaggedMalicious",
                result.isSandboxFlaggedMalicious()
        );
        if (result.getSandboxFindings() != null) {
            meta.put(
                    "findingsCount",
                    result.getSandboxFindings().size()
            );
        }
        return meta;
    }

    private Map<String, Object>
    buildMlMetadata(AnalysisResult result) {

        Map<String, Object> meta =
                new LinkedHashMap<>();
        meta.put("riskScore", result.getRiskScore());
        meta.put(
                "confidence",
                result.getConfidencePercentage()
        );
        meta.put(
                "riskLevel",
                result.getRiskLevel() != null
                        ? result.getRiskLevel().name()
                        : "UNKNOWN"
        );
        if (result.getScoreBreakdown() != null) {
            meta.put(
                    "scoreBreakdown",
                    result.getScoreBreakdown()
            );
        }
        return meta;
    }

    private Map<String, Object>
    buildPatternMetadata(
            AnalysisResult result) {

        Map<String, Object> meta =
                new LinkedHashMap<>();
        meta.put(
                "patternId",
                result.getMatchedPatternId()
        );
        meta.put(
                "patternName",
                result.getMatchedPatternName()
        );
        meta.put(
                "similarity",
                String.format(
                        "%.0f%%",
                        result.getPatternSimilarity()
                                * 100
                )
        );
        return meta;
    }

    private Map<String, Object>
    buildClassificationMetadata(
            AnalysisResult result) {

        Map<String, Object> meta =
                new LinkedHashMap<>();
        meta.put(
                "category",
                result.getThreatCategory() != null
                        ? result.getThreatCategory().name()
                        : "UNKNOWN"
        );
        meta.put(
                "subCategory",
                result.getThreatSubCategory() != null
                        ? result.getThreatSubCategory()
                        .name()
                        : "NONE"
        );
        meta.put(
                "description",
                result.getThreatDescription()
        );
        meta.put(
                "isCritical",
                result.isCriticalThreat()
        );
        return meta;
    }

    private Map<String, Object>
    buildKillChainMetadata(
            AnalysisResult result) {

        Map<String, Object> meta =
                new LinkedHashMap<>();
        meta.put(
                "detectedStage",
                result.getDetectedStage() != null
                        ? result.getDetectedStage().name()
                        : "UNKNOWN"
        );
        // FIX: was result.isIsActiveAttack()
        meta.put(
                "isActiveAttack",
                result.isActiveAttack()
        );
        meta.put(
                "stageDescription",
                result.getStageDescription()
        );
        if (result.getStageProgression() != null) {
            meta.put(
                    "progressionLength",
                    result.getStageProgression().size()
            );
        }
        return meta;
    }

    private Map<String, Object>
    buildDigitalTwinMetadata(
            AnalysisResult result) {

        Map<String, Object> meta =
                new LinkedHashMap<>();
        meta.put(
                "anomalyDetected",
                result.isAnomalyDetected()
        );
        meta.put(
                "anomalyScore",
                result.getBehaviorAnomalyScore()
        );
        meta.put(
                "description",
                result.getAnomalyDescription()
        );
        return meta;
    }

    private Map<String, Object>
    buildEvidenceMetadata(
            AnalysisResult result) {

        Map<String, Object> meta =
                new LinkedHashMap<>();
        meta.put(
                "urlsFound",
                result.getExtractedUrls() != null
                        ? result.getExtractedUrls().size()
                        : 0
        );
        meta.put(
                "emailsFound",
                result.getExtractedEmails() != null
                        ? result.getExtractedEmails().size()
                        : 0
        );
        meta.put(
                "ipsFound",
                result.getExtractedIps() != null
                        ? result.getExtractedIps().size()
                        : 0
        );
        meta.put(
                "redFlagCount",
                result.getRedFlags() != null
                        ? result.getRedFlags().size()
                        : 0
        );
        return meta;
    }

    private Map<String, Object>
    buildSpreadMetadata(
            AnalysisResult result) {

        Map<String, Object> meta =
                new LinkedHashMap<>();
        meta.put(
                "probability",
                String.format(
                        "%.0f%%",
                        result.getSpreadProbability()
                                * 100
                )
        );
        meta.put(
                "estimatedUsers",
                result.getEstimatedAffectedUsers()
        );
        meta.put(
                "spreadDepth",
                result.getSpreadDepth()
        );
        meta.put(
                "affectedInstitutions",
                result.getAffectedInstitutions()
        );
        return meta;
    }

    private Map<String, Object>
    buildResponseMetadata(
            AnalysisResult result) {

        Map<String, Object> meta =
                new LinkedHashMap<>();
        meta.put(
                "actionsTriggered",
                result.getAutoResponseActions()
                        != null
                        ? result.getAutoResponseActions()
                        .size()
                        : 0
        );
        meta.put(
                "alertPublished",
                result.isAutoAlertPublished()
        );
        meta.put(
                "alertId",
                result.getAutoAlertId()
        );
        return meta;
    }

    private Map<String, Object>
    buildCompletionMetadata(
            AnalysisResult result) {

        Map<String, Object> meta =
                new LinkedHashMap<>();
        meta.put(
                "processingTimeMs",
                result.getProcessingTimeMs()
        );
        meta.put(
                "stagesCompleted",
                result.getPipelineStagesCompleted()
                        != null
                        ? result
                        .getPipelineStagesCompleted()
                        .size()
                        : 0
        );
        meta.put(
                "processingSuccess",
                result.isProcessingSuccess()
        );
        meta.put(
                "finalScore",
                result.getRiskScore()
        );
        meta.put(
                "finalLevel",
                result.getRiskLevel() != null
                        ? result.getRiskLevel().name()
                        : "UNKNOWN"
        );
        return meta;
    }

    // ==========================================
    // ATTACK NARRATIVE BUILDER
    // ==========================================
    private String buildAttackNarrative(
            ThreatInput        input,
            AnalysisResult     result,
            List<TimelineEvent> events) {

        StringBuilder narrative =
                new StringBuilder();

        narrative.append("═══ ATTACK NARRATIVE ═══\n\n");

        narrative.append("At ")
                .append(events.get(0)
                        .getEventTimeFormatted())
                .append(", user ")
                .append(input.getUserEmail())
                .append(" from ")
                .append(
                        input.getInstitutionName()
                                != null
                                ? input.getInstitutionName()
                                : "unknown institution"
                )
                .append(" reported a suspicious ")
                .append(
                        input.getInputType().name()
                                .toLowerCase()
                                .replace("_", " ")
                )
                .append(".\n\n");

        if (result.getThreatCategory() != null
                && result.getThreatCategory()
                != AnalysisResult
                .ThreatCategory.SAFE) {
            narrative.append("Intelligence "
                            + "analysis identified this as a ")
                    .append(
                            result.getThreatCategory()
                    )
                    .append(" attack");

            if (result.getThreatSubCategory()
                    != null
                    && result.getThreatSubCategory()
                    != AnalysisResult
                    .ThreatSubCategory.NONE) {
                narrative.append(" — specifically ")
                        .append(
                                result.getThreatSubCategory()
                                        .name()
                                        .replace("_", " ")
                                        .toLowerCase()
                        );
            }
            narrative.append(".\n\n");
        }

        if (result.getDetectedStage() != null
                && result.getDetectedStage()
                != AnalysisResult
                .AttackStage.UNKNOWN) {
            narrative.append(
                            "The attack is currently in the ")
                    .append(
                            result.getDetectedStage()
                                    .name()
                                    .replace("_", " ")
                                    .toLowerCase()
                    )
                    .append(" stage of the "
                            + "Cyber Kill Chain. ")
                    // FIX: was result.isIsActiveAttack()
                    .append(
                            result.isActiveAttack()
                                    ? "This is an ACTIVE attack "
                                    + "requiring immediate action."
                                    : "The attack is in "
                                    + "preparation phase."
                    )
                    .append("\n\n");
        }

        if (result.isMatchedKnownPattern()) {
            narrative.append(
                            "This threat matches the known "
                                    + "attack pattern '")
                    .append(result.getMatchedPatternName())
                    .append("' with ")
                    .append(String.format(
                            "%.0f%%",
                            result.getPatternSimilarity()
                                    * 100
                    ))
                    .append(" similarity, "
                            + "confirming the attack "
                            + "signature.\n\n");
        }

        if (result.isAnomalyDetected()) {
            narrative.append(
                            "The user's digital twin "
                                    + "detected behavioral anomalies: ")
                    .append(result.getAnomalyDescription())
                    .append(" This deviation from "
                            + "normal behavior patterns "
                            + "raises additional concern.\n\n");
        }

        if (result.getSpreadProbability() > 0.4) {
            narrative.append(
                            "Threat spread simulation "
                                    + "indicates a ")
                    .append(String.format(
                            "%.0f%%",
                            result.getSpreadProbability()
                                    * 100
                    ))
                    .append(
                            " probability of affecting "
                    )
                    .append(
                            result.getEstimatedAffectedUsers()
                    )
                    .append(
                            " additional users "
                                    + "if not contained.\n\n"
                    );
        }

        if (result.isAutoResponseTriggered()) {
            narrative.append(
                            "The autonomous response system "
                                    + "automatically executed ")
                    .append(
                            result.getAutoResponseActions()
                                    != null
                                    ? result
                                    .getAutoResponseActions()
                                    .size()
                                    : 0
                    )
                    .append(
                            " protective action(s). "
                    );

            if (result.isAutoAlertPublished()) {
                narrative.append(
                        "A security alert was "
                                + "automatically published "
                                + "to protect all users "
                                + "at the institution. "
                );
            }
            narrative.append("\n\n");
        }

        narrative.append("═══ VERDICT ═══\n")
                .append("Risk Score: ")
                .append(result.getRiskScore())
                .append("/100 | Level: ")
                .append(result.getRiskLevel())
                .append(" | Confidence: ")
                .append(result.getConfidencePercentage())
                .append("\n")
                .append(result.getRiskJustification()
                        != null
                        ? result.getRiskJustification()
                        : "")
                .append("\n\n═══ END ═══");

        return narrative.toString();
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    private EventSeverity mapRiskToSeverity(
            int score) {

        if (score >= 75) {
            return EventSeverity.CRITICAL;
        } else if (score >= 55) {
            return EventSeverity.HIGH;
        } else if (score >= 35) {
            return EventSeverity.MEDIUM;
        } else if (score >= 15) {
            return EventSeverity.LOW;
        } else {
            return EventSeverity.INFO;
        }
    }

    private String buildTopScores(
            AnalysisResult result) {

        if (result.getScoreBreakdown() == null
                || result.getScoreBreakdown()
                .isEmpty()) {
            return "N/A";
        }

        StringBuilder sb = new StringBuilder();
        result.getScoreBreakdown()
                .entrySet()
                .stream()
                .sorted(Map.Entry
                        .<String, Integer>
                                comparingByValue()
                        .reversed())
                .limit(3)
                .forEach(e ->
                        sb.append(e.getKey())
                                .append("(+")
                                .append(e.getValue())
                                .append(") ")
                );

        return sb.toString().trim();
    }

    private String buildProgressionString(
            AnalysisResult result) {

        if (result.getStageProgression() == null
                || result.getStageProgression()
                .isEmpty()) {
            return "Unknown";
        }

        StringBuilder sb = new StringBuilder();
        result.getStageProgression()
                .forEach(stage ->
                        sb.append(
                                stage.name()
                                        .replace("_", " ")
                        ).append(" → ")
                );

        String str = sb.toString();
        return str.endsWith(" → ")
                ? str.substring(
                0, str.length() - 3
        )
                : str;
    }

    private boolean hasExtractedEvidence(
            AnalysisResult result) {

        return (result.getExtractedUrls() != null
                && !result.getExtractedUrls()
                .isEmpty())
                || (result.getExtractedEmails()
                != null
                && !result.getExtractedEmails()
                .isEmpty())
                || (result.getExtractedIps() != null
                && !result.getExtractedIps()
                .isEmpty())
                || (result.getRedFlags() != null
                && !result.getRedFlags().isEmpty());
    }

    // ==========================================
    // TIMELINE EVENT MODEL
    // ==========================================
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TimelineEvent {
        private String              eventId;
        private String              eventType;
        private LocalDateTime       eventTime;
        private String              eventTimeFormatted;
        private String              title;
        private String              description;
        private String              severity;
        private Map<String, Object> metadata;
    }

    // ==========================================
    // TIMELINE REPORT MODEL
    // ==========================================
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TimelineReport {
        private LocalDateTime       generatedAt;
        private String              inputType;
        private String              userEmail;
        private String              institution;
        private int                 finalRiskScore;
        private String              finalRiskLevel;
        private String              threatCategory;
        private List<TimelineEvent> events;
        private int                 totalEvents;
        private String              attackNarrative;
        private List<String>        pipelineStages;
        private long                processingTimeMs;
    }
}