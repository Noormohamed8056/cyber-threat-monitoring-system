package com.project.intelligence.pipeline;

import com.project.digitaltwin.service.DigitalTwinService;
import com.project.detection.ml.MLDetectionEngine;
import com.project.detection.pattern.ThreatPatternMatcher;
import com.project.intelligence.analyzer.EmailAnalyzer;
import com.project.intelligence.analyzer.DocumentAnalyzer;
import com.project.intelligence.analyzer.ImageAnalyzer;
import com.project.intelligence.analyzer.UrlAnalyzer;
import com.project.intelligence.model.AnalysisResult;
import com.project.intelligence.model.ThreatInput;
import com.project.response.autonomous.AutonomousResponseEngine;
import com.project.sandbox.service.SandboxAnalysisService;
import com.project.timeline.generator.AttackTimelineGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

// ==========================================
// INTELLIGENCE PIPELINE — FULLY WIRED
// Orchestrates all 9 stages:
// 1. Input Routing
// 2. Sandbox Analysis
// 3. ML Detection
// 4. Pattern Matching
// 5. Threat Classification
// 6. Kill Chain Detection
// 7. Digital Twin Update
// 8. Spread Simulation
// 9. Autonomous Response
// + Timeline Generation
// ==========================================
@Slf4j
@Service
@RequiredArgsConstructor
public class IntelligencePipeline {

    // ==========================================
    // ALL INJECTED MODULES
    // ==========================================
    private final UrlAnalyzer urlAnalyzer;
    private final EmailAnalyzer            emailAnalyzer;
    private final DocumentAnalyzer         documentAnalyzer;
    private final ImageAnalyzer            imageAnalyzer;
    private final MLDetectionEngine        mlDetectionEngine;
    private final ThreatPatternMatcher     patternMatcher;
    private final SandboxAnalysisService   sandboxService;
    private final AutonomousResponseEngine responseEngine;
    private final AttackTimelineGenerator  timelineGenerator;
    private final DigitalTwinService       digitalTwinService;

    // ==========================================
    // FIX 1: IntelligenceController calls
    // pipeline.process(input) but only run()
    // existed — added process() as bridge
    // ==========================================
    public AnalysisResult process(ThreatInput input) {
        return run(input, null);
    }

    // ==========================================
    // MAIN PIPELINE METHOD
    // ==========================================
    public AnalysisResult run(
            ThreatInput input,
            Long        reportId) {

        long pipelineStart =
                System.currentTimeMillis();

        log.info(
                "═══ PIPELINE START ═══ "
                        + "type={} user={}",
                input.getInputType(),
                input.getUserEmail()
        );

        AnalysisResult result =
                new AnalysisResult();
        result.setAnalyzedAt(
                LocalDateTime.now()
        );
        result.setInputType(
                input.getInputType().name()
        );

        List<String> stages =
                new ArrayList<>();

        try {

            // ==============================
            // STAGE 1: INPUT ROUTING
            // ==============================
            log.info("Stage 1: Input Routing");
            runStage1InputRouting(
                    input, result
            );
            stages.add(
                    "STAGE_1_INPUT_ROUTING"
            );

            // ==============================
            // STAGE 2: SANDBOX ANALYSIS
            // ==============================
            log.info("Stage 2: Sandbox");
            runStage2Sandbox(input, result);
            stages.add(
                    "STAGE_2_SANDBOX_ANALYSIS"
            );

            // ==============================
            // STAGE 3: ML DETECTION
            // ==============================
            log.info("Stage 3: ML Detection");
            runStage3MlDetection(input, result);
            stages.add(
                    "STAGE_3_ML_DETECTION"
            );

            // ==============================
            // STAGE 4: PATTERN MATCHING
            // ==============================
            log.info("Stage 4: Pattern Match");
            runStage4PatternMatching(
                    input, result
            );
            stages.add(
                    "STAGE_4_PATTERN_MATCHING"
            );

            // ==============================
            // STAGE 5: THREAT CLASSIFICATION
            // ==============================
            log.info("Stage 5: Classification");
            runStage5Classification(result);
            stages.add(
                    "STAGE_5_CLASSIFICATION"
            );

            // ==============================
            // STAGE 6: KILL CHAIN DETECTION
            // ==============================
            log.info("Stage 6: Kill Chain");
            runStage6KillChain(result);
            stages.add(
                    "STAGE_6_KILL_CHAIN"
            );

            // ==============================
            // STAGE 7: DIGITAL TWIN UPDATE
            // ==============================
            log.info("Stage 7: Digital Twin");
            runStage7DigitalTwin(
                    input, result
            );
            stages.add(
                    "STAGE_7_DIGITAL_TWIN"
            );

            // ==============================
            // STAGE 8: SPREAD SIMULATION
            // ==============================
            log.info("Stage 8: Spread");
            runStage8SpreadSimulation(
                    input, result
            );
            stages.add(
                    "STAGE_8_SPREAD_SIMULATION"
            );

            // ==============================
            // STAGE 9: AUTONOMOUS RESPONSE
            // ==============================
            log.info("Stage 9: Response");
            runStage9AutonomousResponse(
                    input, result, reportId
            );
            stages.add(
                    "STAGE_9_AUTONOMOUS_RESPONSE"
            );

            // ==============================
            // STAGE 10: TIMELINE GENERATION
            // ==============================
            log.info("Stage 10: Timeline");
            runStage10Timeline(input, result);
            stages.add(
                    "STAGE_10_TIMELINE"
            );

            // ==============================
            // FINALIZE
            // ==============================
            long elapsed =
                    System.currentTimeMillis()
                            - pipelineStart;
            result.setProcessingTimeMs(elapsed);
            result.setPipelineStagesCompleted(
                    stages
            );
            result.setProcessingSuccess(true);

            log.info(
                    "═══ PIPELINE COMPLETE ═══ "
                            + "score={} level={} time={}ms",
                    result.getRiskScore(),
                    result.getRiskLevel(),
                    elapsed
            );

        } catch (Exception e) {
            log.error(
                    "Pipeline error: {}",
                    e.getMessage(),
                    e
            );
            result.setProcessingSuccess(false);
            result.setProcessingError(
                    e.getMessage()
            );
            result.setPipelineStagesCompleted(
                    stages
            );

            // Set safe defaults on error
            if (result.getRiskScore() == 0) {
                result.setRiskScore(25);
            }
            if (result.getRiskLevel() == null) {
                result.setRiskLevel(
                        AnalysisResult.RiskLevel.LOW
                );
            }
        }

        return result;
    }

    // ==========================================
    // STAGE 1: INPUT ROUTING
    // ==========================================
    private void runStage1InputRouting(
            ThreatInput    input,
            AnalysisResult result) {

        try {
            switch (input.getInputType()) {

                case URL:
                    AnalysisResult urlResult =
                            urlAnalyzer.analyze(input);
                    mergeAnalyzerResult(
                            result, urlResult
                    );
                    break;

                case EMAIL_CONTENT:
                    AnalysisResult emailResult =
                            emailAnalyzer
                                    .analyze(input);
                    mergeAnalyzerResult(
                            result, emailResult
                    );
                    break;

                case PDF_DOCUMENT:
                    AnalysisResult docResult =
                            documentAnalyzer
                                    .analyze(input);
                    mergeAnalyzerResult(
                            result, docResult
                    );
                    break;

                case IMAGE_SCREENSHOT:
                    AnalysisResult imgResult =
                            imageAnalyzer
                                    .analyze(input);
                    mergeAnalyzerResult(
                            result, imgResult
                    );
                    break;

                default:
                    log.warn(
                            "Unknown input type: {}",
                            input.getInputType()
                    );
                    result.setRiskScore(10);
                    result.setRiskLevel(
                            AnalysisResult
                                    .RiskLevel.LOW
                    );
            }

            // Ensure red flags initialized
            if (result.getRedFlags() == null) {
                result.setRedFlags(
                        new ArrayList<>()
                );
            }

        } catch (Exception e) {
            log.error(
                    "Stage 1 error: {}",
                    e.getMessage()
            );
            result.setRiskScore(20);
            result.setRiskLevel(
                    AnalysisResult.RiskLevel.LOW
            );
            result.setRedFlags(new ArrayList<>());
        }
    }

    // ==========================================
    // STAGE 2: SANDBOX ANALYSIS
    // ==========================================
    private void runStage2Sandbox(
            ThreatInput    input,
            AnalysisResult result) {

        try {
            SandboxAnalysisService.SandboxResult
                    sandboxResult =
                    sandboxService.analyze(
                            input, result
                    );

            result.setSandboxCompleted(true);
            result.setSandboxFlaggedMalicious(
                    sandboxResult.isFlaggedMalicious()
            );
            result.setSandboxVerdict(
                    sandboxResult.getVerdict()
            );

            if (sandboxResult.getFindings()
                    != null) {
                result.setSandboxFindings(
                        sandboxResult.getFindings()
                );

                // Add sandbox findings to red flags
                sandboxResult.getFindings()
                        .stream()
                        .filter(f -> !f.startsWith(
                                "No content"
                        ))
                        .forEach(f ->
                                result.getRedFlags().add(
                                        "[SANDBOX] " + f
                                )
                        );
            }

            // Boost score if sandbox flagged
            if (sandboxResult
                    .isFlaggedMalicious()) {
                int boost = Math.min(
                        sandboxResult
                                .getSandboxRiskScore() / 3,
                        25
                );
                result.setRiskScore(
                        Math.min(
                                result.getRiskScore()
                                        + boost,
                                100
                        )
                );
                log.info(
                        "Sandbox boost: +{} "
                                + "→ score={}",
                        boost,
                        result.getRiskScore()
                );
            }

            // Extract artifacts
            if (!sandboxResult
                    .getEmbeddedUrls()
                    .isEmpty()) {
                if (result.getExtractedUrls()
                        == null) {
                    result.setExtractedUrls(
                            new ArrayList<>()
                    );
                }
                result.getExtractedUrls()
                        .addAll(
                                sandboxResult
                                        .getEmbeddedUrls()
                        );
            }

            if (!sandboxResult
                    .getExtractedIps()
                    .isEmpty()) {
                if (result.getExtractedIps()
                        == null) {
                    result.setExtractedIps(
                            new ArrayList<>()
                    );
                }
                result.getExtractedIps()
                        .addAll(
                                sandboxResult
                                        .getExtractedIps()
                        );
            }

        } catch (Exception e) {
            log.warn(
                    "Stage 2 sandbox error: {}",
                    e.getMessage()
            );
            result.setSandboxCompleted(false);
        }
    }

    // ==========================================
    // STAGE 3: ML DETECTION
    // ==========================================
    private void runStage3MlDetection(
            ThreatInput    input,
            AnalysisResult result) {

        try {
            MLDetectionEngine.MLDetectionResult
                    mlResult =
                    mlDetectionEngine.detect(
                            input, result
                    );

            if (mlResult.isDetectionSuccess()) {

                // ML score blended with
                // analyzer score
                int blendedScore = (int)(
                        (result.getRiskScore() * 0.6)
                                + (mlResult
                                .getNormalizedScore()
                                * 0.4)
                );
                result.setRiskScore(
                        Math.min(blendedScore, 100)
                );

                // Update confidence
                result.setConfidenceScore(
                        mlResult.getConfidence()
                );
                result.setConfidencePercentage(
                        String.format(
                                "%.0f%%",
                                mlResult.getConfidence()
                                        * 100
                        )
                );

                // Store ML feature scores
                if (mlResult.getFeatureScores()
                        != null) {
                    result.setMlFeatureScores(
                            mlResult.getFeatureScores()
                    );
                }

                // Store score breakdown
                Map<String, Integer> breakdown =
                        new LinkedHashMap<>();
                if (mlResult.getFeatureImportance()
                        != null) {
                    mlResult.getFeatureImportance()
                            .forEach((k, v) ->
                                    breakdown.put(
                                            k,
                                            (int)(v * 100)
                                    )
                            );
                }
                result.setScoreBreakdown(
                        breakdown
                );

                // Add ML explanation to red flags
                if (mlResult.getExplanation()
                        != null) {
                    result.getRedFlags().add(
                            "[ML] "
                                    + mlResult.getExplanation()
                    );
                }

                log.info(
                        "ML blended score: {} "
                                + "(confidence: {})",
                        blendedScore,
                        mlResult.getConfidence()
                );
            }

        } catch (Exception e) {
            log.warn(
                    "Stage 3 ML error: {}",
                    e.getMessage()
            );
        }
    }

    // ==========================================
    // STAGE 4: PATTERN MATCHING
    // ==========================================
    private void runStage4PatternMatching(
            ThreatInput    input,
            AnalysisResult result) {

        try {
            ThreatPatternMatcher
                    .PatternMatchResult pmResult =
                    patternMatcher.match(
                            input, result
                    );

            if (pmResult.isMatchSuccess()
                    && pmResult.isMatched()) {

                result.setMatchedKnownPattern(
                        true
                );
                result.setMatchedPatternId(
                        pmResult.getPatternId()
                );
                result.setMatchedPatternName(
                        pmResult.getPatternName()
                );
                result.setPatternSimilarity(
                        pmResult.getSimilarity()
                );
                result.setPatternDescription(
                        pmResult.getDescription()
                );
                result.setPatternMitigation(
                        pmResult.getMitigation()
                );

                // Pattern match boosts score
                int patternBoost = (int)(
                        pmResult.getSeverityWeight()
                                * pmResult.getSimilarity()
                                * 20
                );
                result.setRiskScore(
                        Math.min(
                                result.getRiskScore()
                                        + patternBoost,
                                100
                        )
                );

                // Override category if high
                // similarity match
                if (pmResult.getSimilarity()
                        >= 0.7
                        && pmResult.getCategory()
                        != null) {
                    result.setThreatCategory(
                            pmResult.getCategory()
                    );
                }

                result.getRedFlags().add(
                        "[PATTERN] Matched: "
                                + pmResult.getPatternName()
                                + " ("
                                + String.format(
                                "%.0f%%",
                                pmResult.getSimilarity()
                                        * 100
                        )
                                + " similarity)"
                );

                log.info(
                        "Pattern matched: {} "
                                + "boost: +{}",
                        pmResult.getPatternName(),
                        patternBoost
                );

            } else {
                result.setMatchedKnownPattern(
                        false
                );
            }

        } catch (Exception e) {
            log.warn(
                    "Stage 4 pattern error: {}",
                    e.getMessage()
            );
            result.setMatchedKnownPattern(false);
        }
    }

    // ==========================================
    // STAGE 5: THREAT CLASSIFICATION
    // ==========================================
    private void runStage5Classification(
            AnalysisResult result) {

        try {
            // Category already set by analyzer
            // or pattern match — enrich it
            if (result.getThreatCategory()
                    == null) {
                result.setThreatCategory(
                        inferCategoryFromScore(result)
                );
            }

            // Set sub-category
            if (result.getThreatSubCategory()
                    == null) {
                result.setThreatSubCategory(
                        inferSubCategory(result)
                );
            }

            // Set description
            if (result.getThreatDescription()
                    == null
                    || result.getThreatDescription()
                    .isEmpty()) {
                result.setThreatDescription(
                        buildThreatDescription(result)
                );
            }

            // Set critical flag
            result.setCriticalThreat(
                    result.getRiskScore() >= 75
                            || result.getThreatCategory()
                            == AnalysisResult
                            .ThreatCategory.RANSOMWARE
                            || result.getThreatCategory()
                            == AnalysisResult
                            .ThreatCategory.DATA_BREACH
            );

            // Update risk level from final score
            result.setRiskLevel(
                    mapScoreToRiskLevel(
                            result.getRiskScore()
                    )
            );

            // Set risk justification
            result.setRiskJustification(
                    buildRiskJustification(result)
            );

            log.info(
                    "Classification: {} → {} | "
                            + "score={}",
                    result.getThreatCategory(),
                    result.getThreatSubCategory(),
                    result.getRiskScore()
            );

        } catch (Exception e) {
            log.warn(
                    "Stage 5 classification "
                            + "error: {}",
                    e.getMessage()
            );
        }
    }

    // ==========================================
    // STAGE 6: KILL CHAIN DETECTION
    // ==========================================
    private void runStage6KillChain(
            AnalysisResult result) {

        try {
            AnalysisResult.AttackStage stage =
                    detectKillChainStage(result);
            result.setDetectedStage(stage);

            // Stage description
            result.setStageDescription(
                    getStageDescription(stage)
            );

            // Build progression
            List<AnalysisResult.AttackStage>
                    progression =
                    buildStageProgression(stage);
            result.setStageProgression(
                    progression
            );

            // Active attack = stage 3+
            boolean isActive =
                    stage.ordinal() >= 3;
            // FIX 2: was result.setIsActiveAttack(isActive)
            // field renamed to activeAttack in AnalysisResult
            // so Lombok generates setActiveAttack() not setIsActiveAttack()
            result.setActiveAttack(isActive);

            if (isActive) {
                result.getRedFlags().add(
                        "[KILL CHAIN] Active attack "
                                + "at stage: "
                                + stage.name()
                );
            }

            log.info(
                    "Kill chain: {} active={}",
                    stage,
                    isActive
            );

        } catch (Exception e) {
            log.warn(
                    "Stage 6 kill chain error: {}",
                    e.getMessage()
            );
            result.setDetectedStage(
                    AnalysisResult.AttackStage.UNKNOWN
            );
        }
    }

    // ==========================================
    // STAGE 7: DIGITAL TWIN UPDATE
    // ==========================================
    private void runStage7DigitalTwin(
            ThreatInput    input,
            AnalysisResult result) {

        try {
            if (input.getUserId() == null) {
                log.warn(
                        "Stage 7: No userId — "
                                + "skipping twin update"
                );
                result.setDigitalTwinUpdated(
                        false
                );
                return;
            }

            // Update twin and get anomaly info
            DigitalTwinService.TwinUpdateResult
                    twinResult =
                    digitalTwinService
                            .updateAndAnalyze(
                                    input.getUserId(),
                                    result.getRiskScore(),
                                    result.getThreatCategory()
                            );

            result.setDigitalTwinUpdated(true);
            result.setAnomalyDetected(
                    twinResult.isAnomalyDetected()
            );
            result.setBehaviorAnomalyScore(
                    twinResult.getAnomalyScore()
            );
            result.setAnomalyDescription(
                    twinResult.getAnomalyDescription()
            );

            // Anomaly boosts score
            if (twinResult.isAnomalyDetected()) {
                int anomalyBoost = (int)(
                        twinResult.getAnomalyScore()
                                * 10
                );
                result.setRiskScore(
                        Math.min(
                                result.getRiskScore()
                                        + anomalyBoost,
                                100
                        )
                );
                result.getRedFlags().add(
                        "[TWIN] Behavioral anomaly: "
                                + twinResult
                                .getAnomalyDescription()
                );
                log.info(
                        "Anomaly boost: +{} "
                                + "→ score={}",
                        anomalyBoost,
                        result.getRiskScore()
                );
            }

        } catch (Exception e) {
            log.warn(
                    "Stage 7 twin error: {}",
                    e.getMessage()
            );
            result.setDigitalTwinUpdated(false);
            result.setAnomalyDetected(false);
        }
    }

    // ==========================================
    // STAGE 8: SPREAD SIMULATION
    // ==========================================
    private void runStage8SpreadSimulation(
            ThreatInput    input,
            AnalysisResult result) {

        try {
            int score = result.getRiskScore();

            // Spread probability from score
            double spreadProb = Math.min(
                    score / 100.0 * 1.2,
                    0.95
            );

            // Pattern match boosts spread
            if (result.isMatchedKnownPattern()) {
                spreadProb = Math.min(
                        spreadProb + 0.15,
                        0.95
                );
            }

            // Active attack boosts spread
            // FIX 3: was result.isIsActiveAttack()
            // correct getter after field rename is
            // result.isActiveAttack()
            if (result.isActiveAttack()) {
                spreadProb = Math.min(
                        spreadProb + 0.10,
                        0.95
                );
            }

            result.setSpreadProbability(
                    Math.round(
                            spreadProb * 100.0
                    ) / 100.0
            );

            // Estimated affected users
            int baseUsers = 5;
            if (score >= 75)      baseUsers = 150;
            else if (score >= 55) baseUsers = 75;
            else if (score >= 35) baseUsers = 25;
            else                  baseUsers = 5;

            int estimatedUsers = (int)(
                    baseUsers
                            * (0.5 + spreadProb * 0.5)
            );
            result.setEstimatedAffectedUsers(
                    estimatedUsers
            );

            // Spread depth
            int depth = 1;
            if (spreadProb >= 0.75) depth = 4;
            else if (spreadProb >= 0.55) depth = 3;
            else if (spreadProb >= 0.35) depth = 2;
            result.setSpreadDepth(depth);

            // Affected institutions
            List<String> institutions =
                    new ArrayList<>();
            if (input.getInstitutionName()
                    != null) {
                institutions.add(
                        input.getInstitutionName()
                );
            }
            if (spreadProb > 0.65) {
                institutions.add(
                        "Partner institutions"
                );
            }
            if (spreadProb > 0.80) {
                institutions.add(
                        "Regional network"
                );
            }
            result.setAffectedInstitutions(
                    institutions
            );

            log.info(
                    "Spread: prob={} users={} "
                            + "depth={}",
                    spreadProb,
                    estimatedUsers,
                    depth
            );

        } catch (Exception e) {
            log.warn(
                    "Stage 8 spread error: {}",
                    e.getMessage()
            );
            result.setSpreadProbability(0.1);
            result.setEstimatedAffectedUsers(5);
            result.setSpreadDepth(1);
        }
    }

    // ==========================================
    // STAGE 9: AUTONOMOUS RESPONSE
    // ==========================================
    private void runStage9AutonomousResponse(
            ThreatInput    input,
            AnalysisResult result,
            Long           reportId) {

        try {
            AutonomousResponseEngine
                    .ResponseResult responseResult =
                    responseEngine.respond(
                            input, result, reportId
                    );

            result.setAutoResponseTriggered(
                    responseResult.isResponseSuccess()
                            && responseResult
                            .getExecutedActions() != null
                            && !responseResult
                            .getExecutedActions().isEmpty()
            );

            if (responseResult
                    .getExecutedActions()
                    != null) {
                result.setAutoResponseActions(
                        responseResult
                                .getExecutedActions()
                );
            }

            result.setAutoAlertPublished(
                    responseResult.isAlertAutoPublished()
            );

            if (responseResult.getAlertId()
                    != null) {
                result.setAutoAlertId(
                        responseResult.getAlertId()
                );
            }

            log.info(
                    "Response: actions={} "
                            + "alert={}",
                    responseResult
                            .getExecutedActions() != null
                            ? responseResult
                            .getExecutedActions()
                            .size()
                            : 0,
                    responseResult
                            .isAlertAutoPublished()
            );

        } catch (Exception e) {
            log.warn(
                    "Stage 9 response error: {}",
                    e.getMessage()
            );
            result.setAutoResponseTriggered(
                    false
            );
        }
    }

    // ==========================================
    // STAGE 10: TIMELINE GENERATION
    // ==========================================
    private void runStage10Timeline(
            ThreatInput    input,
            AnalysisResult result) {

        try {
            AttackTimelineGenerator.TimelineReport
                    timeline =
                    timelineGenerator.generate(
                            input, result
                    );

            // Store timeline events
            if (timeline.getEvents() != null) {
                result.setTimelineEvents(
                        timeline.getEvents()
                );
            }

            // Store attack narrative
            result.setAttackNarrative(
                    timeline.getAttackNarrative()
            );

            // Build recommendations
            if (result.getRecommendations()
                    == null
                    || result.getRecommendations()
                    .isEmpty()) {
                result.setRecommendations(
                        buildRecommendations(result)
                );
            }

            log.info(
                    "Timeline: {} events generated",
                    timeline.getTotalEvents()
            );

        } catch (Exception e) {
            log.warn(
                    "Stage 10 timeline error: {}",
                    e.getMessage()
            );
        }
    }

    // ==========================================
    // HELPER: MERGE ANALYZER RESULT
    // ==========================================
    private void mergeAnalyzerResult(
            AnalysisResult target,
            AnalysisResult source) {

        if (source == null) return;

        target.setRiskScore(
                source.getRiskScore()
        );
        target.setRiskLevel(
                source.getRiskLevel()
        );
        target.setConfidenceScore(
                source.getConfidenceScore()
        );
        target.setConfidencePercentage(
                source.getConfidencePercentage()
        );
        target.setThreatCategory(
                source.getThreatCategory()
        );
        target.setThreatSubCategory(
                source.getThreatSubCategory()
        );
        target.setThreatDescription(
                source.getThreatDescription()
        );

        if (source.getRedFlags() != null) {
            target.setRedFlags(
                    new ArrayList<>(
                            source.getRedFlags()
                    )
            );
        }

        if (source.getScoreBreakdown() != null) {
            target.setScoreBreakdown(
                    source.getScoreBreakdown()
            );
        }
        if (source.getExtractedUrls() != null) {
            target.setExtractedUrls(
                    source.getExtractedUrls()
            );
        }
        if (source.getExtractedEmails() != null) {
            target.setExtractedEmails(
                    source.getExtractedEmails()
            );
        }
        if (source.getExtractedIps() != null) {
            target.setExtractedIps(
                    source.getExtractedIps()
            );
        }
        if (source.getExtractedKeywords()
                != null) {
            target.setExtractedKeywords(
                    source.getExtractedKeywords()
            );
        }
        if (source.getRecommendations() != null) {
            target.setRecommendations(
                    source.getRecommendations()
            );
        }
    }

    // ==========================================
    // HELPER: INFER CATEGORY FROM SCORE
    // ==========================================
    private AnalysisResult.ThreatCategory
    inferCategoryFromScore(
            AnalysisResult result) {

        if (result.getRiskScore() < 20) {
            return AnalysisResult
                    .ThreatCategory.SAFE;
        }
        if (result.getRiskScore() < 40) {
            return AnalysisResult
                    .ThreatCategory.UNKNOWN;
        }

        // Check red flags for hints
        String flags = result.getRedFlags() != null
                ? String.join(
                " ", result.getRedFlags()
        ).toLowerCase()
                : "";

        if (flags.contains("phishing")
                || flags.contains(
                "brand impersonation")) {
            return AnalysisResult
                    .ThreatCategory.PHISHING;
        }
        if (flags.contains("credential")
                || flags.contains("password")) {
            return AnalysisResult
                    .ThreatCategory.CREDENTIAL_THEFT;
        }
        if (flags.contains("macro")
                || flags.contains("executable")) {
            return AnalysisResult
                    .ThreatCategory.MALWARE;
        }
        if (flags.contains("ransomware")) {
            return AnalysisResult
                    .ThreatCategory.RANSOMWARE;
        }
        if (flags.contains("social")) {
            return AnalysisResult
                    .ThreatCategory.SOCIAL_ENGINEERING;
        }

        return AnalysisResult
                .ThreatCategory.UNKNOWN;
    }

    // ==========================================
    // HELPER: INFER SUB-CATEGORY
    // ==========================================
    private AnalysisResult.ThreatSubCategory
    inferSubCategory(AnalysisResult result) {

        if (result.getThreatCategory() == null) {
            return AnalysisResult
                    .ThreatSubCategory.NONE;
        }

        switch (result.getThreatCategory()) {
            case PHISHING:
                return result.isMatchedKnownPattern()
                        ? AnalysisResult
                        .ThreatSubCategory
                        .SPEAR_PHISHING
                        : AnalysisResult
                        .ThreatSubCategory
                        .GENERIC_PHISHING;
            case MALWARE:
                return AnalysisResult
                        .ThreatSubCategory
                        .TROJAN;
            case RANSOMWARE:
                return AnalysisResult
                        .ThreatSubCategory
                        .CRYPTO_RANSOMWARE;
            case CREDENTIAL_THEFT:
                return AnalysisResult
                        .ThreatSubCategory
                        .KEYLOGGER;
            default:
                return AnalysisResult
                        .ThreatSubCategory.NONE;
        }
    }

    // ==========================================
    // HELPER: BUILD THREAT DESCRIPTION
    // ==========================================
    private String buildThreatDescription(
            AnalysisResult result) {

        if (result.getThreatCategory() == null
                || result.getThreatCategory()
                == AnalysisResult
                .ThreatCategory.SAFE) {
            return "No significant threat detected.";
        }

        return "Detected "
                + result.getThreatCategory()
                .name()
                .replace("_", " ")
                .toLowerCase()
                + " attack with "
                + result.getRiskScore()
                + "/100 risk score. "
                + (result.isMatchedKnownPattern()
                ? "Matches pattern: "
                + result.getMatchedPatternName()
                + ". "
                : "")
                + (result.getPatternMitigation()
                != null
                ? result.getPatternMitigation()
                : "");
    }

    // ==========================================
    // HELPER: DETECT KILL CHAIN STAGE
    // ==========================================
    private AnalysisResult.AttackStage
    detectKillChainStage(
            AnalysisResult result) {

        int score = result.getRiskScore();
        AnalysisResult.ThreatCategory cat =
                result.getThreatCategory();

        if (score < 20) {
            return AnalysisResult
                    .AttackStage.RECONNAISSANCE;
        }

        if (cat == AnalysisResult
                .ThreatCategory.RANSOMWARE
                || result.isSandboxFlaggedMalicious()
        ) {
            return score >= 75
                    ? AnalysisResult
                    .AttackStage.ACTIONS_ON_OBJECTIVES
                    : AnalysisResult
                    .AttackStage.EXPLOITATION;
        }

        if (cat == AnalysisResult
                .ThreatCategory.MALWARE) {
            return AnalysisResult
                    .AttackStage.DELIVERY;
        }

        if (cat == AnalysisResult
                .ThreatCategory.CREDENTIAL_THEFT
                || cat == AnalysisResult
                .ThreatCategory.PHISHING) {
            return AnalysisResult
                    .AttackStage.DELIVERY;
        }

        if (cat == AnalysisResult
                .ThreatCategory.DATA_BREACH) {
            return AnalysisResult
                    .AttackStage.EXFILTRATION;
        }

        if (score >= 75) {
            return AnalysisResult
                    .AttackStage.EXPLOITATION;
        } else if (score >= 55) {
            return AnalysisResult
                    .AttackStage.DELIVERY;
        } else if (score >= 35) {
            return AnalysisResult
                    .AttackStage.WEAPONIZATION;
        } else {
            return AnalysisResult
                    .AttackStage.RECONNAISSANCE;
        }
    }

    // ==========================================
    // HELPER: STAGE DESCRIPTION
    // ==========================================
    private String getStageDescription(
            AnalysisResult.AttackStage stage) {

        switch (stage) {
            case RECONNAISSANCE:
                return "Attacker is gathering "
                        + "information about targets.";
            case WEAPONIZATION:
                return "Attacker is preparing "
                        + "malicious payload for delivery.";
            case DELIVERY:
                return "Malicious content is being "
                        + "delivered to the target.";
            case EXPLOITATION:
                return "Attacker is exploiting "
                        + "vulnerabilities in the system.";
            case INSTALLATION:
                return "Malware is being installed "
                        + "on the target system.";
            case COMMAND_AND_CONTROL:
                return "Attacker has established "
                        + "remote control of the system.";
            case ACTIONS_ON_OBJECTIVES:
                return "Attacker is executing "
                        + "final objectives — "
                        + "data theft or destruction.";
            case EXFILTRATION:
                return "Sensitive data is being "
                        + "extracted from the system.";
            default:
                return "Attack stage "
                        + "could not be determined.";
        }
    }

    // ==========================================
    // HELPER: BUILD STAGE PROGRESSION
    // ==========================================
    private List<AnalysisResult.AttackStage>
    buildStageProgression(
            AnalysisResult.AttackStage current) {

        List<AnalysisResult.AttackStage> all =
                Arrays.asList(
                        AnalysisResult.AttackStage
                                .RECONNAISSANCE,
                        AnalysisResult.AttackStage
                                .WEAPONIZATION,
                        AnalysisResult.AttackStage
                                .DELIVERY,
                        AnalysisResult.AttackStage
                                .EXPLOITATION,
                        AnalysisResult.AttackStage
                                .INSTALLATION,
                        AnalysisResult.AttackStage
                                .COMMAND_AND_CONTROL,
                        AnalysisResult.AttackStage
                                .ACTIONS_ON_OBJECTIVES
                );

        List<AnalysisResult.AttackStage>
                progression = new ArrayList<>();

        for (AnalysisResult.AttackStage stage
                : all) {
            progression.add(stage);
            if (stage == current) break;
        }

        return progression;
    }

    // ==========================================
    // HELPER: MAP SCORE TO RISK LEVEL
    // ==========================================
    private AnalysisResult.RiskLevel
    mapScoreToRiskLevel(int score) {

        if (score >= 75) {
            return AnalysisResult.RiskLevel.CRITICAL;
        } else if (score >= 55) {
            return AnalysisResult.RiskLevel.HIGH;
        } else if (score >= 35) {
            return AnalysisResult.RiskLevel.MEDIUM;
        } else if (score >= 15) {
            return AnalysisResult.RiskLevel.LOW;
        } else {
            return AnalysisResult
                    .RiskLevel.NEGLIGIBLE;
        }
    }

    // ==========================================
    // HELPER: BUILD RISK JUSTIFICATION
    // ==========================================
    private String buildRiskJustification(
            AnalysisResult result) {

        StringBuilder sb = new StringBuilder();
        sb.append("Risk score ")
                .append(result.getRiskScore())
                .append("/100 based on: ");

        if (result.getRedFlags() != null) {
            sb.append(
                    result.getRedFlags().size()
            ).append(" red flag(s)");
        }
        if (result.isMatchedKnownPattern()) {
            sb.append(", matched pattern '")
                    .append(result.getMatchedPatternName())
                    .append("'");
        }
        if (result.isAnomalyDetected()) {
            sb.append(", behavioral anomaly");
        }
        if (result.isSandboxFlaggedMalicious()) {
            sb.append(", sandbox flagged");
        }

        sb.append(".");
        return sb.toString();
    }

    // ==========================================
    // HELPER: BUILD RECOMMENDATIONS
    // ==========================================
    private List<String> buildRecommendations(
            AnalysisResult result) {

        List<String> recs = new ArrayList<>();

        if (result.getThreatCategory() == null
                || result.getThreatCategory()
                == AnalysisResult
                .ThreatCategory.SAFE) {
            recs.add(
                    "Continue monitoring for "
                            + "suspicious activity."
            );
            return recs;
        }

        switch (result.getThreatCategory()) {
            case PHISHING:
                recs.add(
                        "Do not click any links "
                                + "in the reported content."
                );
                recs.add(
                        "Verify sender identity "
                                + "through official channels."
                );
                recs.add(
                        "Report to IT security team "
                                + "immediately."
                );
                break;
            case CREDENTIAL_THEFT:
                recs.add(
                        "Change your password "
                                + "immediately on all accounts."
                );
                recs.add(
                        "Enable two-factor "
                                + "authentication."
                );
                recs.add(
                        "Check for unauthorized "
                                + "account access."
                );
                break;
            case MALWARE:
                recs.add(
                        "Do not open or execute "
                                + "the suspicious file."
                );
                recs.add(
                        "Run a full antivirus scan "
                                + "on your device."
                );
                recs.add(
                        "Disconnect from network "
                                + "if file was already opened."
                );
                break;
            case RANSOMWARE:
                recs.add(
                        "Disconnect from network "
                                + "immediately."
                );
                recs.add(
                        "Do NOT pay the ransom — "
                                + "contact IT security."
                );
                recs.add(
                        "Restore from clean backup "
                                + "if available."
                );
                recs.add(
                        "Report to law enforcement "
                                + "and cybersecurity team."
                );
                break;
            case DATA_BREACH:
                recs.add(
                        "Alert your IT security "
                                + "team immediately."
                );
                recs.add(
                        "Identify what data "
                                + "may have been exposed."
                );
                recs.add(
                        "Notify affected parties "
                                + "per breach protocols."
                );
                break;
            case SOCIAL_ENGINEERING:
                recs.add(
                        "Verify all requests via "
                                + "phone before acting."
                );
                recs.add(
                        "Never share sensitive info "
                                + "based on email requests."
                );
                break;
            default:
                recs.add(
                        "Report to IT security team."
                );
                recs.add(
                        "Avoid interacting with "
                                + "the suspicious content."
                );
        }

        // Add pattern mitigation if available
        if (result.getPatternMitigation() != null
                && !result.getPatternMitigation()
                .isEmpty()) {
            recs.add(
                    result.getPatternMitigation()
            );
        }

        return recs;
    }
}