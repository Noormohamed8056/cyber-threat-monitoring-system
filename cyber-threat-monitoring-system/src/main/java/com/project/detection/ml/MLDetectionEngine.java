package com.project.detection.ml;

import com.project.intelligence.model.AnalysisResult;
import com.project.intelligence.model.ThreatInput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

// ==========================================
// ML DETECTION ENGINE
// Rule-based weighted ML simulation
// Performs multi-feature threat scoring
// with confidence calibration
// ==========================================
@Slf4j
@Component
public class MLDetectionEngine {

    // ==========================================
    // FEATURE WEIGHTS
    // Each feature contributes to final score
    // ==========================================
    private static final Map<String, Double>
            FEATURE_WEIGHTS =
            new LinkedHashMap<>() {{
                put("BRAND_IMPERSONATION",  0.30);
                put("CREDENTIAL_HARVESTING",0.28);
                put("MALICIOUS_EXTENSION",  0.25);
                put("EXECUTABLE_FILE",      0.40);
                put("MACRO_ENABLED",        0.30);
                put("PHISHING_KEYWORDS",    0.15);
                put("URGENCY_LANGUAGE",     0.12);
                put("FAKE_LOGIN_PAGE",      0.28);
                put("URL_SHORTENER",        0.10);
                put("NO_HTTPS",             0.08);
                put("SUSPICIOUS_TLD",       0.10);
                put("IP_AS_DOMAIN",         0.20);
                put("HOMOGRAPH_ATTACK",     0.18);
                put("SOCIAL_ENGINEERING",   0.15);
                put("FINANCIAL_FRAUD",      0.22);
                put("MALWARE_KEYWORDS",     0.25);
                put("STEGANOGRAPHY",        0.20);
                put("POLYGLOT_FILE",        0.35);
                put("EXTENSION_MISMATCH",   0.22);
                put("ENCRYPTED_DOC",        0.15);
                put("SENSITIVE_DATA",       0.18);
                put("BEHAVIORAL_ANOMALY",   0.20);
                put("PATTERN_MATCH",        0.25);
            }};

    // ==========================================
    // THREAT CLASS THRESHOLDS
    // ==========================================
    private static final double
            CRITICAL_THRESHOLD = 0.75;
    private static final double
            HIGH_THRESHOLD     = 0.55;
    private static final double
            MEDIUM_THRESHOLD   = 0.35;
    private static final double
            LOW_THRESHOLD      = 0.15;

    // ==========================================
    // MAIN DETECTION METHOD
    // ==========================================
    public MLDetectionResult detect(
            ThreatInput    input,
            AnalysisResult partialResult) {

        log.info(
                "ML Detection starting for: {}",
                input.getInputType()
        );

        MLDetectionResult mlResult =
                new MLDetectionResult();

        try {
            // Step 1: Extract features from
            //         existing red flags
            Map<String, Double> features =
                    extractFeatures(
                            input, partialResult
                    );

            // Step 2: Calculate weighted score
            double rawScore =
                    calculateWeightedScore(features);

            // Step 3: Apply input type modifier
            rawScore = applyInputTypeModifier(
                    rawScore, input
            );

            // Step 4: Calibrate confidence
            double confidence =
                    calibrateConfidence(
                            rawScore, features
                    );

            // Step 5: Classify threat
            AnalysisResult.ThreatCategory category =
                    classifyThreat(
                            rawScore, features, input
                    );

            // Step 6: Map to risk level
            AnalysisResult.RiskLevel riskLevel =
                    mapToRiskLevel(rawScore);

            // Step 7: Build feature importance
            Map<String, Double> importance =
                    buildFeatureImportance(features);

            // Populate result
            mlResult.setRawScore(rawScore);
            mlResult.setNormalizedScore(
                    (int)(rawScore * 100)
            );
            mlResult.setConfidence(confidence);
            mlResult.setThreatCategory(category);
            mlResult.setRiskLevel(riskLevel);
            mlResult.setFeatureScores(features);
            mlResult.setFeatureImportance(
                    importance
            );
            mlResult.setDetectionSuccess(true);

            // Generate explanation
            mlResult.setExplanation(
                    generateExplanation(
                            rawScore,
                            confidence,
                            category,
                            features
                    )
            );

            log.info(
                    "ML Detection complete: "
                            + "score={} confidence={} "
                            + "category={}",
                    mlResult.getNormalizedScore(),
                    confidence,
                    category
            );

        } catch (Exception e) {
            log.error(
                    "ML Detection error: {}",
                    e.getMessage()
            );
            mlResult.setDetectionSuccess(false);
            mlResult.setRawScore(0.3);
            mlResult.setNormalizedScore(30);
            mlResult.setConfidence(0.4);
        }

        return mlResult;
    }

    // ==========================================
    // STEP 1: EXTRACT FEATURES FROM RED FLAGS
    // ==========================================
    private Map<String, Double> extractFeatures(
            ThreatInput    input,
            AnalysisResult result) {

        Map<String, Double> features =
                new LinkedHashMap<>();

        List<String> redFlags =
                result.getRedFlags();
        if (redFlags == null || redFlags.isEmpty()) {
            return features;
        }

        String flagText = String
                .join(" ", redFlags)
                .toLowerCase();

        // Check each feature keyword
        if (flagText.contains(
                "brand impersonation")) {
            features.put(
                    "BRAND_IMPERSONATION", 1.0
            );
        }
        if (flagText.contains("credential")
                || flagText.contains("password")) {
            features.put(
                    "CREDENTIAL_HARVESTING", 1.0
            );
        }
        if (flagText.contains(
                "malicious extension")
                || flagText.contains(".exe")
                || flagText.contains(".bat")) {
            features.put(
                    "MALICIOUS_EXTENSION", 1.0
            );
        }
        if (flagText.contains("executable")) {
            features.put("EXECUTABLE_FILE", 1.0);
        }
        if (flagText.contains("macro")) {
            features.put("MACRO_ENABLED", 1.0);
        }
        if (flagText.contains(
                "phishing keyword")) {
            double density = Math.min(
                    redFlags.stream()
                            .filter(f -> f.toLowerCase()
                                    .contains("phishing"))
                            .count() / 5.0,
                    1.0
            );
            features.put(
                    "PHISHING_KEYWORDS", density
            );
        }
        if (flagText.contains("urgency")
                || flagText.contains("urgent")) {
            features.put("URGENCY_LANGUAGE", 1.0);
        }
        if (flagText.contains("fake login")
                || flagText.contains("login page")) {
            features.put("FAKE_LOGIN_PAGE", 1.0);
        }
        if (flagText.contains("url shortener")) {
            features.put("URL_SHORTENER", 1.0);
        }
        if (flagText.contains("no https")
                || flagText.contains("not secure")) {
            features.put("NO_HTTPS", 1.0);
        }
        if (flagText.contains("suspicious tld")) {
            features.put("SUSPICIOUS_TLD", 1.0);
        }
        if (flagText.contains("ip address")) {
            features.put("IP_AS_DOMAIN", 1.0);
        }
        if (flagText.contains("homograph")) {
            features.put("HOMOGRAPH_ATTACK", 1.0);
        }
        if (flagText.contains(
                "social engineering")) {
            features.put(
                    "SOCIAL_ENGINEERING", 1.0
            );
        }
        if (flagText.contains("financial")
                || flagText.contains("wire transfer")
                || flagText.contains("gift card")) {
            features.put("FINANCIAL_FRAUD", 1.0);
        }
        if (flagText.contains("malware")
                || flagText.contains("malicious")) {
            features.put("MALWARE_KEYWORDS", 1.0);
        }
        if (flagText.contains("steganography")) {
            features.put("STEGANOGRAPHY", 1.0);
        }
        if (flagText.contains("polyglot")) {
            features.put("POLYGLOT_FILE", 1.0);
        }
        if (flagText.contains(
                "extension mismatch")) {
            features.put(
                    "EXTENSION_MISMATCH", 1.0
            );
        }
        if (flagText.contains("encrypted")) {
            features.put("ENCRYPTED_DOC", 1.0);
        }
        if (flagText.contains("credit card")
                || flagText.contains("ssn")) {
            features.put("SENSITIVE_DATA", 1.0);
        }
        if (flagText.contains(
                "behavioral anomaly")
                || result.isAnomalyDetected()) {
            features.put(
                    "BEHAVIORAL_ANOMALY", 1.0
            );
        }
        if (result.isMatchedKnownPattern()) {
            features.put("PATTERN_MATCH",
                    result.getPatternSimilarity()
            );
        }

        return features;
    }

    // ==========================================
    // STEP 2: CALCULATE WEIGHTED SCORE
    // ==========================================
    private double calculateWeightedScore(
            Map<String, Double> features) {

        if (features.isEmpty()) return 0.0;

        double totalWeight      = 0;
        double weightedSum      = 0;

        for (Map.Entry<String, Double> entry
                : features.entrySet()) {

            Double weight = FEATURE_WEIGHTS
                    .get(entry.getKey());

            if (weight != null) {
                weightedSum +=
                        entry.getValue() * weight;
                totalWeight += weight;
            }
        }

        if (totalWeight == 0) return 0.0;

        // Normalize to 0.0-1.0
        double score = weightedSum / totalWeight;

        // Apply non-linear boost for
        // multiple high-weight features
        long highWeightCount = features.keySet()
                .stream()
                .filter(k -> {
                    Double w = FEATURE_WEIGHTS.get(k);
                    return w != null && w >= 0.25;
                })
                .count();

        if (highWeightCount >= 3) {
            score = Math.min(score * 1.25, 1.0);
        } else if (highWeightCount >= 2) {
            score = Math.min(score * 1.10, 1.0);
        }

        return score;
    }

    // ==========================================
    // STEP 3: APPLY INPUT TYPE MODIFIER
    // ==========================================
    private double applyInputTypeModifier(
            double      score,
            ThreatInput input) {

        if (input.getInputType() == null) {
            return score;
        }

        switch (input.getInputType()) {
            case IMAGE_SCREENSHOT:
                // Images harder to analyze —
                // slight uncertainty boost
                return score * 0.95;
            case PDF_DOCUMENT:
                // Documents can hide malware —
                // slight boost
                return Math.min(score * 1.05, 1.0);
            case EMAIL_CONTENT:
                // Email is primary attack vector
                return Math.min(score * 1.02, 1.0);
            case URL:
                // URLs well-analyzed — keep as-is
                return score;
            default:
                return score;
        }
    }

    // ==========================================
    // STEP 4: CALIBRATE CONFIDENCE
    // ==========================================
    private double calibrateConfidence(
            double              rawScore,
            Map<String, Double> features) {

        // More features = higher confidence
        int featureCount = features.size();

        double baseConfidence;
        if (featureCount >= 6) {
            baseConfidence = 0.92;
        } else if (featureCount >= 4) {
            baseConfidence = 0.82;
        } else if (featureCount >= 2) {
            baseConfidence = 0.70;
        } else if (featureCount == 1) {
            baseConfidence = 0.55;
        } else {
            baseConfidence = 0.40;
        }

        // High score with many features =
        // very confident
        if (rawScore > 0.7 && featureCount >= 3) {
            baseConfidence = Math.min(
                    baseConfidence + 0.05, 0.97
            );
        }

        // Low score = lower confidence
        if (rawScore < 0.2) {
            baseConfidence *= 0.85;
        }

        return Math.round(
                baseConfidence * 100.0
        ) / 100.0;
    }

    // ==========================================
    // STEP 5: CLASSIFY THREAT
    // ==========================================
    private AnalysisResult.ThreatCategory
    classifyThreat(
            double              rawScore,
            Map<String, Double> features,
            ThreatInput         input) {

        // Priority-based classification
        if (features.containsKey("EXECUTABLE_FILE")
                || features.containsKey(
                "POLYGLOT_FILE")) {
            return AnalysisResult
                    .ThreatCategory.MALWARE;
        }

        if (features.containsKey("MACRO_ENABLED")
                && features.containsKey(
                "MALWARE_KEYWORDS")) {
            return AnalysisResult
                    .ThreatCategory.RANSOMWARE;
        }

        if (features.containsKey(
                "CREDENTIAL_HARVESTING")
                || features.containsKey(
                "FAKE_LOGIN_PAGE")) {
            return AnalysisResult
                    .ThreatCategory.CREDENTIAL_THEFT;
        }

        if (features.containsKey(
                "BRAND_IMPERSONATION")) {
            return AnalysisResult
                    .ThreatCategory.PHISHING;
        }

        if (features.containsKey(
                "FINANCIAL_FRAUD")) {
            return AnalysisResult
                    .ThreatCategory.SOCIAL_ENGINEERING;
        }

        if (features.containsKey("SENSITIVE_DATA")) {
            return AnalysisResult
                    .ThreatCategory.DATA_BREACH;
        }

        if (features.containsKey(
                "BEHAVIORAL_ANOMALY")) {
            return AnalysisResult
                    .ThreatCategory.INSIDER_THREAT;
        }

        if (rawScore > HIGH_THRESHOLD) {
            return AnalysisResult
                    .ThreatCategory.UNKNOWN;
        }

        return AnalysisResult
                .ThreatCategory.SAFE;
    }

    // ==========================================
    // STEP 6: MAP TO RISK LEVEL
    // ==========================================
    private AnalysisResult.RiskLevel
    mapToRiskLevel(double rawScore) {

        if (rawScore >= CRITICAL_THRESHOLD) {
            return AnalysisResult
                    .RiskLevel.CRITICAL;
        } else if (rawScore >= HIGH_THRESHOLD) {
            return AnalysisResult.RiskLevel.HIGH;
        } else if (rawScore >= MEDIUM_THRESHOLD) {
            return AnalysisResult.RiskLevel.MEDIUM;
        } else if (rawScore >= LOW_THRESHOLD) {
            return AnalysisResult.RiskLevel.LOW;
        } else {
            return AnalysisResult
                    .RiskLevel.NEGLIGIBLE;
        }
    }

    // ==========================================
    // STEP 7: FEATURE IMPORTANCE
    // ==========================================
    private Map<String, Double>
    buildFeatureImportance(
            Map<String, Double> features) {

        Map<String, Double> importance =
                new LinkedHashMap<>();

        features.forEach((key, value) -> {
            Double weight =
                    FEATURE_WEIGHTS.getOrDefault(
                            key, 0.1
                    );
            importance.put(
                    key,
                    Math.round(
                            weight * value * 100.0
                    ) / 100.0
            );
        });

        // Sort by importance descending
        Map<String, Double> sorted =
                new LinkedHashMap<>();
        importance.entrySet()
                .stream()
                .sorted(Map.Entry
                        .<String, Double>
                                comparingByValue()
                        .reversed())
                .forEach(e ->
                        sorted.put(e.getKey(), e.getValue())
                );

        return sorted;
    }

    // ==========================================
    // GENERATE EXPLANATION
    // ==========================================
    private String generateExplanation(
            double                      score,
            double                      confidence,
            AnalysisResult.ThreatCategory category,
            Map<String, Double>         features) {

        StringBuilder sb = new StringBuilder();
        sb.append("ML Engine detected ")
                .append(category)
                .append(" with ")
                .append(
                        String.format("%.0f%%", score * 100)
                )
                .append(" risk score and ")
                .append(
                        String.format(
                                "%.0f%%", confidence * 100
                        )
                )
                .append(" confidence. ");

        if (!features.isEmpty()) {
            sb.append("Key indicators: ");
            features.keySet()
                    .stream()
                    .limit(3)
                    .forEach(k ->
                            sb.append(
                                    k.replace("_", " ")
                                            .toLowerCase()
                            ).append(", ")
                    );
        }

        return sb.toString()
                .replaceAll(", $", ".");
    }

    // ==========================================
    // ML DETECTION RESULT MODEL
    // ==========================================
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MLDetectionResult {

        private double                          rawScore;
        private int                             normalizedScore;
        private double                          confidence;
        private AnalysisResult.ThreatCategory   threatCategory;
        private AnalysisResult.RiskLevel        riskLevel;
        private Map<String, Double>             featureScores;
        private Map<String, Double>             featureImportance;
        private String                          explanation;
        private boolean                         detectionSuccess;
    }
}