package com.project.intelligence.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

// ==========================================
// ANALYSIS RESULT — COMPLETE MODEL
// Holds all outputs from the full
// 10-stage intelligence pipeline
// ==========================================
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {

    // ==========================================
    // ENUMS
    // ==========================================

    public enum RiskLevel {
        NEGLIGIBLE,
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public enum ThreatCategory {
        SAFE,
        PHISHING,
        CREDENTIAL_THEFT,
        MALWARE,
        RANSOMWARE,
        SOCIAL_ENGINEERING,
        DATA_BREACH,
        INSIDER_THREAT,
        DENIAL_OF_SERVICE,
        ADVANCED_PERSISTENT_THREAT,
        ZERO_DAY,
        UNKNOWN
    }

    public enum ThreatSubCategory {
        NONE,
        SPEAR_PHISHING,
        GENERIC_PHISHING,
        WHALING,
        SMISHING,
        VISHING,
        KEYLOGGER,
        TROJAN,
        WORM,
        ROOTKIT,
        SPYWARE,
        ADWARE,
        CRYPTO_RANSOMWARE,
        LOCKER_RANSOMWARE,
        BEC,
        CEO_FRAUD,
        GIFT_CARD_SCAM,
        LOTTERY_SCAM,
        CRYPTO_FRAUD,
        SQL_INJECTION,
        XSS,
        BRUTE_FORCE,
        HOMOGRAPH,
        TYPOSQUATTING
    }

    public enum AttackStage {
        RECONNAISSANCE,
        WEAPONIZATION,
        DELIVERY,
        EXPLOITATION,
        INSTALLATION,
        COMMAND_AND_CONTROL,
        ACTIONS_ON_OBJECTIVES,
        EXFILTRATION,
        UNKNOWN
    }

    // ==========================================
    // SECTION 1: BASIC METADATA
    // ==========================================
    // FIX: Added resultId — called as
    // result.getResultId() in IntelligenceController
    // buildSuccessResponse() but was missing from model
    private String        resultId;
    private LocalDateTime analyzedAt;
    private String        inputType;
    private long          processingTimeMs;
    private boolean       processingSuccess;
    private String        processingError;

    // ==========================================
    // SECTION 2: RISK SCORING
    // ==========================================
    private int       riskScore;
    private RiskLevel riskLevel;
    private double    confidenceScore;
    private String    confidencePercentage;
    private String    riskJustification;
    private boolean   criticalThreat;

    // ==========================================
    // SECTION 3: THREAT CLASSIFICATION
    // ==========================================
    private ThreatCategory    threatCategory;
    private ThreatSubCategory threatSubCategory;
    private String            threatDescription;

    // ==========================================
    // SECTION 4: SCORE BREAKDOWN
    // ==========================================
    private Map<String, Integer>  scoreBreakdown;
    private Map<String, Double>   mlFeatureScores;
    private List<String>          redFlags;

    // ==========================================
    // SECTION 5: PATTERN MATCHING
    // ==========================================
    private boolean matchedKnownPattern;
    private String  matchedPatternId;
    private String  matchedPatternName;
    private double  patternSimilarity;
    private String  patternDescription;
    private String  patternMitigation;

    // ==========================================
    // SECTION 6: KILL CHAIN
    // ==========================================
    private AttackStage         detectedStage;
    private String              stageDescription;
    private List<AttackStage>   stageProgression;
    // FIX: renamed from isActiveAttack -> activeAttack
    // Lombok generates isIsActiveAttack()/setIsActiveAttack()
    // for "isActiveAttack" — both wrong. "activeAttack"
    // generates correct isActiveAttack()/setActiveAttack()
    private boolean             activeAttack;

    // ==========================================
    // SECTION 7: DIGITAL TWIN
    // ==========================================
    private boolean digitalTwinUpdated;
    private boolean anomalyDetected;
    private double  behaviorAnomalyScore;
    private String  anomalyDescription;

    // ==========================================
    // SECTION 8: SANDBOX ANALYSIS
    // ==========================================
    private boolean      sandboxCompleted;
    private boolean      sandboxFlaggedMalicious;
    private String       sandboxVerdict;
    private List<String> sandboxFindings;

    // ==========================================
    // SECTION 9: SPREAD SIMULATION
    // ==========================================
    private double       spreadProbability;
    private int          estimatedAffectedUsers;
    private int          spreadDepth;
    private List<String> affectedInstitutions;

    // ==========================================
    // SECTION 10: AUTONOMOUS RESPONSE
    // ==========================================
    private boolean      autoResponseTriggered;
    private List<String> autoResponseActions;
    private boolean      autoAlertPublished;
    private String       autoAlertId;

    // ==========================================
    // SECTION 11: EXTRACTED ARTIFACTS
    // ==========================================
    private List<String> extractedUrls;
    private List<String> extractedEmails;
    private List<String> extractedIps;
    private List<String> extractedKeywords;

    // ==========================================
    // SECTION 12: TIMELINE
    // ==========================================
    private List<?>      timelineEvents;
    private String       attackNarrative;
    private List<String> pipelineStagesCompleted;

    // ==========================================
    // SECTION 13: RECOMMENDATIONS
    // ==========================================
    private List<String> recommendations;

    // ==========================================
    // HELPER METHODS
    // ==========================================

    public boolean isSafe() {
        return riskScore < 20
                || threatCategory
                == ThreatCategory.SAFE;
    }

    public boolean isHighRisk() {
        return riskLevel == RiskLevel.HIGH
                || riskLevel == RiskLevel.CRITICAL;
    }

    public String getRiskEmoji() {
        if (riskLevel == null) return "⚪";
        switch (riskLevel) {
            case CRITICAL:   return "🔴";
            case HIGH:       return "🟠";
            case MEDIUM:     return "🟡";
            case LOW:        return "🟢";
            default:         return "⚪";
        }
    }

    public int getRedFlagCount() {
        return redFlags != null
                ? redFlags.size()
                : 0;
    }

    public String getTopRedFlag() {
        return (redFlags != null
                && !redFlags.isEmpty())
                ? redFlags.get(0)
                : "None";
    }
}