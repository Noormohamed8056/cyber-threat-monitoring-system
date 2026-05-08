package com.project.digitaltwin.service;

import com.project.digitaltwin.entity.UserDigitalTwin;
import com.project.digitaltwin.repository.UserDigitalTwinRepository;
import com.project.intelligence.model.AnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

// ==========================================
// DIGITAL TWIN SERVICE — COMPLETE
// Creates and maintains a behavioral
// mirror for each user. Learns normal
// patterns and detects anomalies when
// behavior deviates from baseline.
// ==========================================
@Slf4j
@Service
@RequiredArgsConstructor
public class DigitalTwinService {

    private final UserDigitalTwinRepository
            twinRepository;

    // ==========================================
    // ANOMALY THRESHOLDS
    // ==========================================
    private static final double
            ANOMALY_THRESHOLD       = 2.5;
    private static final double
            HIGH_RISK_THRESHOLD     = 4.0;
    private static final int
            MIN_ITERATIONS_BASELINE = 3;
    private static final double
            LEARNING_RATE           = 0.15;
    private static final double
            DECAY_RATE              = 0.05;

    // ==========================================
    // MAIN METHOD — UPDATE AND ANALYZE
    // Called by IntelligencePipeline Stage 7
    // ==========================================
    @Transactional
    public TwinUpdateResult updateAndAnalyze(
            Long                          userId,
            int                           riskScore,
            AnalysisResult.ThreatCategory category) {

        log.info(
                "Digital Twin: updating user={} "
                        + "score={} category={}",
                userId, riskScore, category
        );

        TwinUpdateResult result =
                new TwinUpdateResult();

        try {
            // Get or create twin
            UserDigitalTwin twin =
                    getOrCreateTwin(userId);

            // Capture previous state
            double previousAvgScore =
                    twin.getAvgRiskScore();
            int previousReportCount =
                    twin.getTotalReportsSubmitted();

            // Update twin with new data
            updateTwinData(
                    twin, riskScore, category
            );

            // Analyze for anomalies
            analyzeForAnomalies(
                    twin,
                    riskScore,
                    previousAvgScore,
                    result
            );

            // Update trust level
            updateTrustLevel(twin);

            // Save updated twin
            twinRepository.save(twin);

            result.setTwinUpdated(true);
            result.setLearningIteration(
                    twin.getLearningIterations()
            );
            result.setCurrentTrustLevel(
                    twin.getTrustLevel()
            );
            result.setCurrentAvgScore(
                    twin.getAvgRiskScore()
            );
            result.setTotalReports(
                    twin.getTotalReportsSubmitted()
            );
            result.setBaselineEstablished(
                    twin.isBaselineEstablished()
            );

            log.info(
                    "Twin updated: anomaly={} "
                            + "score={} trust={}",
                    result.isAnomalyDetected(),
                    twin.getAvgRiskScore(),
                    twin.getTrustLevel()
            );

        } catch (Exception e) {
            log.error(
                    "Digital twin error: {}",
                    e.getMessage()
            );
            result.setTwinUpdated(false);
            result.setAnomalyDetected(false);
            result.setAnomalyScore(0.0);
            result.setAnomalyDescription(
                    "Twin update failed"
            );
        }

        return result;
    }

    // ==========================================
    // GET OR CREATE TWIN
    // ==========================================
    @Transactional
    public UserDigitalTwin getOrCreateTwin(
            Long userId) {

        return twinRepository
                .findByUserId(userId)
                .orElseGet(() -> {
                    log.info(
                            "Creating new twin "
                                    + "for user: {}",
                            userId
                    );
                    UserDigitalTwin newTwin =
                            new UserDigitalTwin();
                    newTwin.setUserId(userId);
                    newTwin.setCreatedAt(
                            LocalDateTime.now()
                    );
                    newTwin.setUpdatedAt(
                            LocalDateTime.now()
                    );
                    newTwin.setTrustLevel(
                            UserDigitalTwin
                                    .TrustLevel.NEW
                    );
                    newTwin.setTrustScore(50.0);
                    newTwin.setAnomalyScore(0.0);
                    newTwin.setAvgRiskScore(0.0);
                    newTwin.setMaxRiskScore(0);
                    newTwin.setMinRiskScore(100);
                    newTwin.setTotalReportsSubmitted(0);
                    newTwin.setHighRiskCount(0);
                    newTwin.setCriticalRiskCount(0);
                    newTwin.setAnomalyDetectedCount(0);
                    newTwin.setLearningIterations(0);
                    newTwin.setBaselineEstablished(
                            false
                    );
                    newTwin.setIsHighRiskUser(false);
                    newTwin.setIsFlagged(false);
                    newTwin.setRiskScoreHistory(
                            new ArrayList<>()
                    );
                    newTwin.setCategoryHistory(
                            new ArrayList<>()
                    );
                    return twinRepository
                            .save(newTwin);
                });
    }

    // ==========================================
    // UPDATE TWIN DATA
    // ==========================================
    private void updateTwinData(
            UserDigitalTwin               twin,
            int                           riskScore,
            AnalysisResult.ThreatCategory category) {

        // Increment report count
        twin.setTotalReportsSubmitted(
                twin.getTotalReportsSubmitted() + 1
        );

        // Update risk score history
        List<Integer> history =
                twin.getRiskScoreHistory();
        if (history == null) {
            history = new ArrayList<>();
            twin.setRiskScoreHistory(history);
        }
        history.add(riskScore);

        // Keep last 20 scores only
        if (history.size() > 20) {
            history.remove(0);
        }

        // Update category history
        List<String> catHistory =
                twin.getCategoryHistory();
        if (catHistory == null) {
            catHistory = new ArrayList<>();
            twin.setCategoryHistory(catHistory);
        }
        if (category != null) {
            catHistory.add(category.name());
            if (catHistory.size() > 20) {
                catHistory.remove(0);
            }
        }

        // Update max/min risk scores
        if (riskScore > twin.getMaxRiskScore()) {
            twin.setMaxRiskScore(riskScore);
        }
        if (riskScore < twin.getMinRiskScore()) {
            twin.setMinRiskScore(riskScore);
        }

        // Recalculate average risk score
        double avgScore = history.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
        twin.setAvgRiskScore(
                Math.round(avgScore * 100.0) / 100.0
        );

        // Update high risk count
        if (riskScore >= 55) {
            twin.setHighRiskCount(
                    twin.getHighRiskCount() + 1
            );
        }

        // Update critical risk count
        if (riskScore >= 75) {
            twin.setCriticalRiskCount(
                    twin.getCriticalRiskCount() + 1
            );
        }

        // Increment learning iterations
        twin.setLearningIterations(
                twin.getLearningIterations() + 1
        );

        // Establish baseline after
        // MIN_ITERATIONS_BASELINE reports
        if (!twin.isBaselineEstablished()
                && twin.getLearningIterations()
                >= MIN_ITERATIONS_BASELINE) {
            twin.setBaselineEstablished(true);
            log.info(
                    "Baseline established "
                            + "for user: {}",
                    twin.getUserId()
            );
        }

        // Update last active timestamp
        twin.setLastActiveAt(
                LocalDateTime.now()
        );
        twin.setUpdatedAt(
                LocalDateTime.now()
        );

        // Update high risk user flag
        twin.setIsHighRiskUser(
                twin.getAvgRiskScore() >= 60
                        || twin.getCriticalRiskCount() >= 2
        );
    }

    // ==========================================
    // ANALYZE FOR ANOMALIES
    // ==========================================
    private void analyzeForAnomalies(
            UserDigitalTwin twin,
            int             currentScore,
            double          previousAvgScore,
            TwinUpdateResult result) {

        // Cannot detect anomaly without baseline
        if (!twin.isBaselineEstablished()) {
            result.setAnomalyDetected(false);
            result.setAnomalyScore(0.0);
            result.setAnomalyDescription(
                    "Baseline not yet established — "
                            + "learning user patterns"
            );
            return;
        }

        double anomalyScore = 0.0;
        List<String> anomalyReasons =
                new ArrayList<>();

        // Check 1: Score spike from average
        double scoreDelta =
                currentScore - previousAvgScore;
        if (scoreDelta > 30) {
            anomalyScore += scoreDelta / 15.0;
            anomalyReasons.add(
                    "Risk score spike of +"
                            + (int) scoreDelta
                            + " above user baseline"
            );
        }

        // Check 2: Sudden high risk from low avg
        if (currentScore >= 55
                && previousAvgScore < 25) {
            anomalyScore += 2.0;
            anomalyReasons.add(
                    "Sudden high-risk report "
                            + "from typically low-risk user"
            );
        }

        // Check 3: Repeated high risk reports
        List<Integer> history =
                twin.getRiskScoreHistory();
        if (history != null
                && history.size() >= 3) {
            long recentHighCount =
                    history.subList(
                                    Math.max(
                                            0,
                                            history.size() - 5
                                    ),
                                    history.size()
                            )
                            .stream()
                            .filter(s -> s >= 55)
                            .count();

            if (recentHighCount >= 3) {
                anomalyScore += 1.5;
                anomalyReasons.add(
                        recentHighCount
                                + " high-risk reports "
                                + "in last 5 submissions"
                );
            }
        }

        // Check 4: Critical risk count
        if (twin.getCriticalRiskCount() >= 3) {
            anomalyScore += 1.0;
            anomalyReasons.add(
                    twin.getCriticalRiskCount()
                            + " critical risk reports "
                            + "in history"
            );
        }

        // Check 5: Current score is critical
        if (currentScore >= 75) {
            anomalyScore += 1.0;
            anomalyReasons.add(
                    "Current report is "
                            + "CRITICAL risk level"
            );
        }

        // Round anomaly score
        anomalyScore = Math.round(
                anomalyScore * 100.0
        ) / 100.0;

        boolean isAnomaly =
                anomalyScore >= ANOMALY_THRESHOLD;

        // Update twin anomaly data
        twin.setAnomalyScore(anomalyScore);
        if (isAnomaly) {
            twin.setAnomalyDetectedCount(
                    twin.getAnomalyDetectedCount() + 1
            );
            twin.setLastAnomalyAt(
                    LocalDateTime.now()
            );
        }

        // Build result
        result.setAnomalyDetected(isAnomaly);
        result.setAnomalyScore(anomalyScore);
        result.setAnomalyDescription(
                anomalyReasons.isEmpty()
                        ? "No anomaly detected"
                        : String.join(
                        "; ", anomalyReasons
                )
        );

        if (isAnomaly) {
            log.warn(
                    "ANOMALY detected for user {}: "
                            + "score={} reasons={}",
                    twin.getUserId(),
                    anomalyScore,
                    anomalyReasons
            );
        }
    }

    // ==========================================
    // UPDATE TRUST LEVEL
    // ==========================================
    private void updateTrustLevel(
            UserDigitalTwin twin) {

        double avgScore = twin.getAvgRiskScore();
        double anomaly  = twin.getAnomalyScore();
        int    total    =
                twin.getTotalReportsSubmitted();
        int    critical =
                twin.getCriticalRiskCount();

        UserDigitalTwin.TrustLevel level;

        if (!twin.isBaselineEstablished()) {
            level = UserDigitalTwin.TrustLevel.NEW;
        } else if (anomaly >= HIGH_RISK_THRESHOLD
                || critical >= 3
                || avgScore >= 70) {
            level =
                    UserDigitalTwin.TrustLevel
                            .HIGH_RISK;
        } else if (anomaly >= ANOMALY_THRESHOLD
                || avgScore >= 50
                || critical >= 1) {
            level =
                    UserDigitalTwin.TrustLevel
                            .SUSPICIOUS;
        } else if (avgScore >= 25
                || total < 5) {
            level =
                    UserDigitalTwin.TrustLevel
                            .NEUTRAL;
        } else {
            level =
                    UserDigitalTwin.TrustLevel
                            .TRUSTED;
        }

        twin.setTrustLevel(level);

        // Update trust score (0-100)
        // Higher = more trusted
        double trustScore =
                100.0
                        - (avgScore * 0.5)
                        - (anomaly * 10)
                        - (critical * 5);
        twin.setTrustScore(
                Math.max(
                        0,
                        Math.min(
                                100,
                                Math.round(
                                        trustScore * 100.0
                                ) / 100.0
                        )
                )
        );

        log.info(
                "Trust level: {} score={}",
                level,
                twin.getTrustScore()
        );
    }

    // ==========================================
    // GET TWIN SUMMARY (for admin dashboard)
    // ==========================================
    public TwinSummary getTwinSummary(
            Long userId) {

        Optional<UserDigitalTwin> twinOpt =
                twinRepository.findByUserId(userId);

        if (twinOpt.isEmpty()) {
            return TwinSummary.builder()
                    .userId(userId)
                    .exists(false)
                    .build();
        }

        UserDigitalTwin twin = twinOpt.get();

        return TwinSummary.builder()
                .userId(userId)
                .exists(true)
                .trustLevel(
                        twin.getTrustLevel().name()
                )
                .trustScore(twin.getTrustScore())
                .avgRiskScore(twin.getAvgRiskScore())
                .maxRiskScore(twin.getMaxRiskScore())
                .minRiskScore(twin.getMinRiskScore())
                .totalReports(
                        twin.getTotalReportsSubmitted()
                )
                .highRiskCount(
                        twin.getHighRiskCount()
                )
                .criticalRiskCount(
                        twin.getCriticalRiskCount()
                )
                .anomalyScore(twin.getAnomalyScore())
                .anomalyDetectedCount(
                        twin.getAnomalyDetectedCount()
                )
                .isHighRiskUser(
                        twin.getIsHighRiskUser()
                )
                .isFlagged(twin.getIsFlagged())
                .flagReason(twin.getFlagReason())
                .baselineEstablished(
                        twin.isBaselineEstablished()
                )
                .learningIterations(
                        twin.getLearningIterations()
                )
                .lastActiveAt(twin.getLastActiveAt())
                .lastAnomalyAt(
                        twin.getLastAnomalyAt()
                )
                .riskScoreHistory(
                        twin.getRiskScoreHistory()
                )
                .build();
    }

    // ==========================================
    // GET ALL HIGH RISK TWINS (admin view)
    // ==========================================
    public List<TwinSummary> getAllHighRiskTwins() {

        return twinRepository
                .findByIsHighRiskUserTrue()
                .stream()
                .map(twin ->
                        getTwinSummary(twin.getUserId())
                )
                .collect(
                        java.util.stream.Collectors
                                .toList()
                );
    }

    // ==========================================
    // GET INSTITUTION STATS
    // ==========================================
    public InstitutionTwinStats
    getInstitutionStats(
            String institutionName) {

        Long total =
                twinRepository.countByInstitution(
                        institutionName
                );
        Long highRisk =
                twinRepository.countHighRiskUsers();
        Long flagged =
                twinRepository.countFlaggedUsers();
        Double avgAnomaly =
                twinRepository
                        .avgAnomalyScoreByInstitution(
                                institutionName
                        );
        Double avgTrust =
                twinRepository
                        .avgTrustScoreByInstitution(
                                institutionName
                        );

        return InstitutionTwinStats.builder()
                .institutionName(institutionName)
                .totalTwins(
                        total != null ? total : 0L
                )
                .highRiskUsers(
                        highRisk != null ? highRisk : 0L
                )
                .flaggedUsers(
                        flagged != null ? flagged : 0L
                )
                .avgAnomalyScore(
                        avgAnomaly != null
                                ? Math.round(
                                avgAnomaly * 100.0
                        ) / 100.0
                                : 0.0
                )
                .avgTrustScore(
                        avgTrust != null
                                ? Math.round(
                                avgTrust * 100.0
                        ) / 100.0
                                : 0.0
                )
                .build();
    }

    // ==========================================
    // MANUALLY FLAG USER TWIN
    // ==========================================
    @Transactional
    public boolean flagTwin(
            Long   userId,
            String reason) {

        int updated =
                twinRepository.flagUserTwin(
                        userId,
                        reason,
                        LocalDateTime.now()
                );
        log.warn(
                "Twin flagged: user={} reason={}",
                userId, reason
        );
        return updated > 0;
    }

    // ==========================================
    // UNFLAG USER TWIN
    // ==========================================
    @Transactional
    public boolean unflagTwin(Long userId) {

        int updated =
                twinRepository.unflagUserTwin(
                        userId,
                        LocalDateTime.now()
                );
        log.info(
                "Twin unflagged: user={}", userId
        );
        return updated > 0;
    }

    // ==========================================
    // RESET ANOMALY SCORE
    // ==========================================
    @Transactional
    public boolean resetAnomaly(Long userId) {

        int updated =
                twinRepository.resetAnomalyScore(
                        userId,
                        LocalDateTime.now()
                );
        log.info(
                "Twin anomaly reset: user={}",
                userId
        );
        return updated > 0;
    }

    // ==========================================
    // TWIN UPDATE RESULT MODEL
    // ==========================================
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class TwinUpdateResult {

        private boolean                     twinUpdated;
        private boolean                     anomalyDetected;
        private double                      anomalyScore;
        private String                      anomalyDescription;
        private UserDigitalTwin.TrustLevel  currentTrustLevel;
        private double                      currentAvgScore;
        private int                         totalReports;
        private int                         learningIteration;
        private boolean                     baselineEstablished;
    }

    // ==========================================
    // TWIN SUMMARY MODEL
    // ==========================================
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class TwinSummary {

        private Long                userId;
        private boolean             exists;
        private String              trustLevel;
        private double              trustScore;
        private double              avgRiskScore;
        private int                 maxRiskScore;
        private int                 minRiskScore;
        private int                 totalReports;
        private int                 highRiskCount;
        private int                 criticalRiskCount;
        private double              anomalyScore;
        private int                 anomalyDetectedCount;
        private Boolean             isHighRiskUser;
        private Boolean             isFlagged;
        private String              flagReason;
        private boolean             baselineEstablished;
        private int                 learningIterations;
        private java.time.LocalDateTime lastActiveAt;
        private java.time.LocalDateTime lastAnomalyAt;
        private List<Integer>       riskScoreHistory;
    }

    // ==========================================
    // INSTITUTION TWIN STATS MODEL
    // ==========================================
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class InstitutionTwinStats {

        private String institutionName;
        private Long   totalTwins;
        private Long   highRiskUsers;
        private Long   flaggedUsers;
        private double avgAnomalyScore;
        private double avgTrustScore;
    }
}
