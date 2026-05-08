package com.project.digitaltwin.repository;

import com.project.digitaltwin.entity.UserDigitalTwin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// ==========================================
// USER DIGITAL TWIN REPOSITORY
// Handles all DB operations for behavioral
// profile tracking per user
// ==========================================
@Repository
public interface UserDigitalTwinRepository
        extends JpaRepository
                <UserDigitalTwin, Long> {

        // ==========================================
        // FIND BY USER ID
        // ==========================================
        Optional<UserDigitalTwin> findByUserId(
                Long userId
        );

        // ==========================================
        // FIND BY USER EMAIL
        // ==========================================
        Optional<UserDigitalTwin> findByUserEmail(
                String userEmail
        );

        // ==========================================
        // CHECK IF TWIN EXISTS
        // ==========================================
        boolean existsByUserId(Long userId);

        boolean existsByUserEmail(String userEmail);

        // ==========================================
        // FIND BY INSTITUTION
        // ==========================================
        List<UserDigitalTwin> findByInstitutionName(
                String institutionName
        );

        // ==========================================
        // FIND BY TRUST LEVEL
        // ==========================================
        List<UserDigitalTwin> findByTrustLevel(
                UserDigitalTwin.TrustLevel trustLevel
        );

        // ==========================================
        // FIND ALL HIGH RISK USERS
        // ==========================================
        List<UserDigitalTwin> findByIsHighRiskUserTrue();

        // ==========================================
        // FIND ALL FLAGGED USERS
        // ==========================================
        List<UserDigitalTwin> findByIsFlaggedTrue();

        // ==========================================
        // FIND USERS WITH ANOMALY ABOVE THRESHOLD
        // ==========================================
        @Query(
                "SELECT t FROM UserDigitalTwin t "
                        + "WHERE t.anomalyScore > :threshold "
                        + "ORDER BY t.anomalyScore DESC"
        )
        List<UserDigitalTwin> findByAnomalyScoreAbove(
                @Param("threshold") double threshold
        );

        // ==========================================
        // FIND TOP REPORTERS
        // ==========================================
        @Query(
                "SELECT t FROM UserDigitalTwin t "
                        + "ORDER BY t.totalReportsSubmitted DESC"
        )
        List<UserDigitalTwin> findTopReporters();

        // ==========================================
        // FIND RECENTLY ACTIVE TWINS
        // ==========================================
        @Query(
                "SELECT t FROM UserDigitalTwin t "
                        + "WHERE t.lastActiveAt >= :since "
                        + "ORDER BY t.lastActiveAt DESC"
        )
        List<UserDigitalTwin> findRecentlyActive(
                @Param("since") LocalDateTime since
        );

        // ==========================================
        // FIND TWINS WITH BASELINE ESTABLISHED
        // ==========================================
        @Query(
                "SELECT t FROM UserDigitalTwin t "
                        + "WHERE t.baselineEstablished = true "
                        + "ORDER BY t.learningIterations DESC"
        )
        List<UserDigitalTwin> findWithBaselineEstablished();

        // ==========================================
        // FIND HIGH RISK IN INSTITUTION
        // ==========================================
        @Query(
                "SELECT t FROM UserDigitalTwin t "
                        + "WHERE t.institutionName = :institution "
                        + "AND t.isHighRiskUser = true "
                        + "ORDER BY t.anomalyScore DESC"
        )
        List<UserDigitalTwin> findHighRiskByInstitution(
                @Param("institution") String institution
        );

        // ==========================================
        // FIND SUSPICIOUS USERS IN INSTITUTION
        // ==========================================
        @Query(
                "SELECT t FROM UserDigitalTwin t "
                        + "WHERE t.institutionName = :institution "
                        + "AND t.trustLevel IN "
                        + "('SUSPICIOUS', 'HIGH_RISK') "
                        + "ORDER BY t.anomalyScore DESC"
        )
        List<UserDigitalTwin> findSuspiciousByInstitution(
                @Param("institution") String institution
        );

        // ==========================================
        // FIND BY TRUST LEVEL AND INSTITUTION
        // ==========================================
        @Query(
                "SELECT t FROM UserDigitalTwin t "
                        + "WHERE t.institutionName = :institution "
                        + "AND t.trustLevel = :level"
        )
        List<UserDigitalTwin> findByTrustLevelAndInstitution(
                @Param("level")
                UserDigitalTwin.TrustLevel level,
                @Param("institution")
                String institution
        );

        // ==========================================
        // FIND TWINS WITH CRITICAL RISK COUNT
        // ==========================================
        @Query(
                "SELECT t FROM UserDigitalTwin t "
                        + "WHERE t.criticalRiskCount >= :minCount "
                        + "ORDER BY t.criticalRiskCount DESC"
        )
        List<UserDigitalTwin> findWithCriticalRiskAbove(
                @Param("minCount") int minCount
        );

        // ==========================================
        // FIND TWINS WITH HIGH AVG RISK SCORE
        // ==========================================
        @Query(
                "SELECT t FROM UserDigitalTwin t "
                        + "WHERE t.avgRiskScore >= :minScore "
                        + "ORDER BY t.avgRiskScore DESC"
        )
        List<UserDigitalTwin> findHighAvgRiskTwins(
                @Param("minScore") double minScore
        );

        // ==========================================
        // COUNT BY TRUST LEVEL
        // ==========================================
        @Query(
                "SELECT COUNT(t) FROM UserDigitalTwin t "
                        + "WHERE t.trustLevel = :level"
        )
        Long countByTrustLevel(
                @Param("level")
                UserDigitalTwin.TrustLevel level
        );

        // ==========================================
        // COUNT HIGH RISK USERS
        // ==========================================
        @Query(
                "SELECT COUNT(t) FROM UserDigitalTwin t "
                        + "WHERE t.isHighRiskUser = true"
        )
        Long countHighRiskUsers();

        // ==========================================
        // COUNT FLAGGED USERS
        // ==========================================
        @Query(
                "SELECT COUNT(t) FROM UserDigitalTwin t "
                        + "WHERE t.isFlagged = true"
        )
        Long countFlaggedUsers();

        // ==========================================
        // COUNT TWINS WITH ANOMALY DETECTED
        // ==========================================
        @Query(
                "SELECT COUNT(t) FROM UserDigitalTwin t "
                        + "WHERE t.anomalyDetectedCount > 0"
        )
        Long countTwinsWithAnomalies();

        // ==========================================
        // COUNT BY INSTITUTION
        // ==========================================
        @Query(
                "SELECT COUNT(t) FROM UserDigitalTwin t "
                        + "WHERE t.institutionName = :institution"
        )
        Long countByInstitution(
                @Param("institution") String institution
        );

        // ==========================================
        // GET AVERAGE ANOMALY SCORE BY INSTITUTION
        // ==========================================
        @Query(
                "SELECT AVG(t.anomalyScore) "
                        + "FROM UserDigitalTwin t "
                        + "WHERE t.institutionName = :institution"
        )
        Double avgAnomalyScoreByInstitution(
                @Param("institution") String institution
        );

        // ==========================================
        // GET AVERAGE TRUST SCORE BY INSTITUTION
        // ==========================================
        @Query(
                "SELECT AVG(t.trustScore) "
                        + "FROM UserDigitalTwin t "
                        + "WHERE t.institutionName = :institution"
        )
        Double avgTrustScoreByInstitution(
                @Param("institution") String institution
        );

        // ==========================================
        // GET AVERAGE RISK SCORE BY INSTITUTION
        // ==========================================
        @Query(
                "SELECT AVG(t.avgRiskScore) "
                        + "FROM UserDigitalTwin t "
                        + "WHERE t.institutionName = :institution"
        )
        Double avgRiskScoreByInstitution(
                @Param("institution") String institution
        );

        // ==========================================
        // FIND ALL TWINS ORDERED BY RISK SCORE
        // ==========================================
        @Query(
                "SELECT t FROM UserDigitalTwin t "
                        + "ORDER BY t.avgRiskScore DESC"
        )
        List<UserDigitalTwin> findAllOrderByRiskScore();

        // ==========================================
        // FIND ALL TWINS ORDERED BY ANOMALY SCORE
        // ==========================================
        @Query(
                "SELECT t FROM UserDigitalTwin t "
                        + "ORDER BY t.anomalyScore DESC"
        )
        List<UserDigitalTwin> findAllOrderByAnomalyScore();

        // ==========================================
        // FIND TWINS WITH ZERO ANOMALY (TRUSTED)
        // ==========================================
        @Query(
                "SELECT t FROM UserDigitalTwin t "
                        + "WHERE t.anomalyScore = 0.0 "
                        + "AND t.baselineEstablished = true "
                        + "ORDER BY t.totalReportsSubmitted DESC"
        )
        List<UserDigitalTwin> findTrustedTwins();

        // ==========================================
        // FIND TWINS NOT ACTIVE SINCE DATE
        // ==========================================
        @Query(
                "SELECT t FROM UserDigitalTwin t "
                        + "WHERE t.lastActiveAt < :since "
                        + "OR t.lastActiveAt IS NULL"
        )
        List<UserDigitalTwin> findInactiveSince(
                @Param("since") LocalDateTime since
        );

        // ==========================================
        // FIND TWINS BY LEARNING ITERATIONS
        // ==========================================
        @Query(
                "SELECT t FROM UserDigitalTwin t "
                        + "WHERE t.learningIterations >= :minIter "
                        + "ORDER BY t.learningIterations DESC"
        )
        List<UserDigitalTwin> findByMinIterations(
                @Param("minIter") int minIterations
        );

        // ==========================================
        // UPDATE ANOMALY SCORE DIRECTLY
        // ==========================================
        @Modifying
        @Transactional
        @Query(
                "UPDATE UserDigitalTwin t "
                        + "SET t.anomalyScore = :score, "
                        + "t.updatedAt = :now "
                        + "WHERE t.userId = :userId"
        )
        int updateAnomalyScore(
                @Param("userId") Long userId,
                @Param("score")  double score,
                @Param("now")    LocalDateTime now
        );

        // ==========================================
        // UPDATE TRUST LEVEL DIRECTLY
        // ==========================================
        @Modifying
        @Transactional
        @Query(
                "UPDATE UserDigitalTwin t "
                        + "SET t.trustLevel = :level, "
                        + "t.updatedAt = :now "
                        + "WHERE t.userId = :userId"
        )
        int updateTrustLevel(
                @Param("userId") Long userId,
                @Param("level")
                UserDigitalTwin.TrustLevel level,
                @Param("now")    LocalDateTime now
        );

        // ==========================================
        // FLAG USER TWIN
        // ==========================================
        @Modifying
        @Transactional
        @Query(
                "UPDATE UserDigitalTwin t "
                        + "SET t.isFlagged = true, "
                        + "t.flagReason = :reason, "
                        + "t.updatedAt = :now "
                        + "WHERE t.userId = :userId"
        )
        int flagUserTwin(
                @Param("userId") Long userId,
                @Param("reason") String reason,
                @Param("now")    LocalDateTime now
        );

        // ==========================================
        // UNFLAG USER TWIN
        // ==========================================
        @Modifying
        @Transactional
        @Query(
                "UPDATE UserDigitalTwin t "
                        + "SET t.isFlagged = false, "
                        + "t.flagReason = null, "
                        + "t.updatedAt = :now "
                        + "WHERE t.userId = :userId"
        )
        int unflagUserTwin(
                @Param("userId") Long userId,
                @Param("now")    LocalDateTime now
        );

        // ==========================================
        // RESET ANOMALY SCORE
        // ==========================================
        @Modifying
        @Transactional
        @Query(
                "UPDATE UserDigitalTwin t "
                        + "SET t.anomalyScore = 0.0, "
                        + "t.isHighRiskUser = false, "
                        + "t.trustLevel = 'TRUSTED', "
                        + "t.updatedAt = :now "
                        + "WHERE t.userId = :userId"
        )
        int resetAnomalyScore(
                @Param("userId") Long userId,
                @Param("now")    LocalDateTime now
        );

        // ==========================================
        // DASHBOARD STATS QUERY
        // ==========================================
        @Query(
                "SELECT "
                        + "COUNT(t) as total, "
                        + "SUM(CASE WHEN t.isHighRiskUser = true "
                        + "    THEN 1 ELSE 0 END) as highRisk, "
                        + "SUM(CASE WHEN t.isFlagged = true "
                        + "    THEN 1 ELSE 0 END) as flagged, "
                        + "AVG(t.trustScore) as avgTrust, "
                        + "AVG(t.anomalyScore) as avgAnomaly "
                        + "FROM UserDigitalTwin t"
        )
        Object[] getDashboardStats();
}
