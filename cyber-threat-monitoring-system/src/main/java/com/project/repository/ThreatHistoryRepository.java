package com.project.repository;

import com.project.entity.IncidentReport;
import com.project.entity.ThreatHistory;
import com.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ThreatHistoryRepository extends JpaRepository<ThreatHistory, Long> {

    // ==========================================
    // FIND BY PERFORMED BY (USER)
    // ==========================================
    List<ThreatHistory> findByPerformedBy(User performedBy);

    // ==========================================
    // FIND BY ACTION TYPE
    // ==========================================
    List<ThreatHistory> findByActionType(ThreatHistory.ActionType actionType);

    // ==========================================
    // FIND BY INCIDENT REPORT
    // ==========================================
    List<ThreatHistory> findByIncidentReport(IncidentReport incidentReport);

    // ==========================================
    // FIND BY INCIDENT REPORT ID
    // ==========================================
    List<ThreatHistory> findByIncidentReportId(Long incidentReportId);

    // ==========================================
    // FIND BY CYBER ALERT ID
    // ==========================================
    List<ThreatHistory> findByCyberAlertId(Long cyberAlertId);

    // ==========================================
    // FIND ALL ORDERED BY DATE (NEWEST FIRST)
    // ==========================================
    List<ThreatHistory> findAllByOrderByCreatedAtDesc();

    // ==========================================
    // FIND BY USER ORDERED BY DATE
    // ==========================================
    List<ThreatHistory> findByPerformedByOrderByCreatedAtDesc(User performedBy);

    // ==========================================
    // FIND HISTORY WITHIN DATE RANGE
    // ==========================================
    @Query("SELECT h FROM ThreatHistory h WHERE " +
            "h.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY h.createdAt DESC")
    List<ThreatHistory> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // ==========================================
    // FIND RECENT HISTORY (LAST 7 DAYS)
    // ==========================================
    @Query("SELECT h FROM ThreatHistory h WHERE " +
            "h.createdAt >= :sevenDaysAgo " +
            "ORDER BY h.createdAt DESC")
    List<ThreatHistory> findRecentHistory(
            @Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);

    // ==========================================
    // FIND HISTORY BY RISK LEVEL
    // ==========================================
    @Query("SELECT h FROM ThreatHistory h WHERE " +
            "h.riskLevelAtTime = :riskLevel " +
            "ORDER BY h.createdAt DESC")
    List<ThreatHistory> findByRiskLevel(
            @Param("riskLevel") IncidentReport.RiskLevel riskLevel);

    // ==========================================
    // FIND ALL REPORT SUBMISSION HISTORY
    // ==========================================
    @Query("SELECT h FROM ThreatHistory h WHERE " +
            "h.actionType = 'REPORT_SUBMITTED' " +
            "ORDER BY h.createdAt DESC")
    List<ThreatHistory> findAllReportSubmissions();

    // ==========================================
    // FIND ALL ADMIN ACTIONS
    // ==========================================
    @Query("SELECT h FROM ThreatHistory h WHERE " +
            "h.actionType IN (" +
            "'REPORT_VERIFIED', " +
            "'REPORT_DISMISSED', " +
            "'REPORT_UNDER_REVIEW', " +
            "'ADMIN_REVIEW_STARTED', " +
            "'ADMIN_REMARKS_ADDED', " +
            "'ALERT_PUBLISHED', " +
            "'ALERT_WITHDRAWN') " +
            "ORDER BY h.createdAt DESC")
    List<ThreatHistory> findAllAdminActions();

    // ==========================================
    // FIND HISTORY BY INSTITUTION
    // ==========================================
    @Query("SELECT h FROM ThreatHistory h WHERE " +
            "h.performedBy.institutionName = :institutionName " +
            "ORDER BY h.createdAt DESC")
    List<ThreatHistory> findByInstitution(
            @Param("institutionName") String institutionName);

    // ==========================================
    // COUNT ACTIONS BY TYPE
    // ==========================================
    @Query("SELECT COUNT(h) FROM ThreatHistory h " +
            "WHERE h.actionType = :actionType")
    Long countByActionType(
            @Param("actionType") ThreatHistory.ActionType actionType);

    // ==========================================
    // COUNT ACTIONS BY USER
    // ==========================================
    @Query("SELECT COUNT(h) FROM ThreatHistory h " +
            "WHERE h.performedBy = :user")
    Long countByUser(@Param("user") User user);

    // ==========================================
    // DASHBOARD - ACTIVITY SUMMARY BY ACTION TYPE
    // ==========================================
    @Query("SELECT h.actionType, COUNT(h) FROM ThreatHistory h " +
            "GROUP BY h.actionType " +
            "ORDER BY COUNT(h) DESC")
    List<Object[]> countGroupByActionType();

    // ==========================================
    // FIND LATEST ACTIVITY FOR EACH INCIDENT
    // ==========================================
    @Query("SELECT h FROM ThreatHistory h WHERE " +
            "h.incidentReport.id = :incidentId " +
            "ORDER BY h.createdAt DESC")
    List<ThreatHistory> findIncidentTimeline(
            @Param("incidentId") Long incidentId);

    // ==========================================
    // FIND HIGH RISK HISTORY ENTRIES
    // ==========================================
    @Query("SELECT h FROM ThreatHistory h WHERE " +
            "(h.riskLevelAtTime = 'HIGH' OR h.riskLevelAtTime = 'CRITICAL') AND " +
            "h.riskScoreAtTime >= :minScore " +
            "ORDER BY h.riskScoreAtTime DESC")
    List<ThreatHistory> findHighRiskHistory(
            @Param("minScore") Integer minScore);

    // ==========================================
    // FIND LOGIN HISTORY FOR USER
    // ==========================================
    @Query("SELECT h FROM ThreatHistory h WHERE " +
            "h.performedBy = :user AND " +
            "h.actionType = 'USER_LOGIN' " +
            "ORDER BY h.createdAt DESC")
    List<ThreatHistory> findLoginHistory(@Param("user") User user);
}
