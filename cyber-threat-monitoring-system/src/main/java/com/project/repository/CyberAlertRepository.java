package com.project.repository;

import com.project.entity.CyberAlert;
import com.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CyberAlertRepository extends JpaRepository<CyberAlert, Long> {

    // ==========================================
    // FIND BY STATUS
    // ==========================================
    List<CyberAlert> findByStatus(CyberAlert.AlertStatus status);

    // ==========================================
    // FIND BY SEVERITY
    // ==========================================
    List<CyberAlert> findBySeverity(CyberAlert.Severity severity);

    // ==========================================
    // FIND BY ALERT TYPE
    // ==========================================
    List<CyberAlert> findByAlertType(CyberAlert.AlertType alertType);

    // ==========================================
    // FIND BY PUBLISHED BY (ADMIN)
    // ==========================================
    List<CyberAlert> findByPublishedBy(User publishedBy);

    // ==========================================
    // FIND ALL ACTIVE ALERTS
    // ==========================================
    List<CyberAlert> findByStatusOrderByCreatedAtDesc(
            CyberAlert.AlertStatus status);

    // ==========================================
    // FIND ALL PUBLIC ACTIVE ALERTS
    // ==========================================
    List<CyberAlert> findByIsPublicTrueAndStatus(
            CyberAlert.AlertStatus status);

    // ==========================================
    // FIND BY SEVERITY AND STATUS
    // ==========================================
    List<CyberAlert> findBySeverityAndStatus(
            CyberAlert.Severity severity,
            CyberAlert.AlertStatus status);

    // ==========================================
    // FIND ALL ORDERED BY CREATED DATE
    // ==========================================
    List<CyberAlert> findAllByOrderByCreatedAtDesc();

    // ==========================================
    // FIND ALERTS FOR SPECIFIC INSTITUTION
    // ==========================================
    @Query("SELECT a FROM CyberAlert a WHERE " +
            "(a.targetInstitution = :institutionName OR a.isPublic = true) " +
            "AND a.status = 'ACTIVE' " +
            "ORDER BY a.createdAt DESC")
    List<CyberAlert> findAlertsForInstitution(
            @Param("institutionName") String institutionName);

    // ==========================================
    // FIND CRITICAL AND HIGH SEVERITY ACTIVE ALERTS
    // ==========================================
    @Query("SELECT a FROM CyberAlert a WHERE " +
            "a.severity IN ('HIGH', 'CRITICAL') AND " +
            "a.status = 'ACTIVE' " +
            "ORDER BY a.createdAt DESC")
    List<CyberAlert> findCriticalAndHighAlerts();

    // ==========================================
    // FIND ALERTS WITHIN DATE RANGE
    // ==========================================
    @Query("SELECT a FROM CyberAlert a WHERE " +
            "a.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY a.createdAt DESC")
    List<CyberAlert> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // ==========================================
    // FIND EXPIRED ALERTS (FOR CLEANUP)
    // ==========================================
    @Query("SELECT a FROM CyberAlert a WHERE " +
            "a.expiresAt IS NOT NULL AND " +
            "a.expiresAt < :now AND " +
            "a.status = 'ACTIVE'")
    List<CyberAlert> findExpiredAlerts(
            @Param("now") LocalDateTime now);

    // ==========================================
    // SEARCH ALERTS BY KEYWORD
    // ==========================================
    @Query("SELECT a FROM CyberAlert a WHERE " +
            "LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.message) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<CyberAlert> searchByKeyword(
            @Param("keyword") String keyword);

    // ==========================================
    // COUNT BY STATUS
    // ==========================================
    @Query("SELECT COUNT(a) FROM CyberAlert a WHERE a.status = :status")
    Long countByStatus(@Param("status") CyberAlert.AlertStatus status);

    // ==========================================
    // COUNT BY SEVERITY
    // ==========================================
    @Query("SELECT COUNT(a) FROM CyberAlert a WHERE a.severity = :severity")
    Long countBySeverity(@Param("severity") CyberAlert.Severity severity);

    // ==========================================
    // FIND RECENT ALERTS (LAST 7 DAYS)
    // ==========================================
    @Query("SELECT a FROM CyberAlert a WHERE " +
            "a.createdAt >= :sevenDaysAgo AND " +
            "a.status = 'ACTIVE' " +
            "ORDER BY a.createdAt DESC")
    List<CyberAlert> findRecentAlerts(
            @Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);

    // ==========================================
    // FIND ALERTS LINKED TO INCIDENT REPORT
    // ==========================================
    @Query("SELECT a FROM CyberAlert a WHERE " +
            "a.relatedIncident.id = :incidentId")
    List<CyberAlert> findByRelatedIncidentId(
            @Param("incidentId") Long incidentId);

    @Query("SELECT a FROM CyberAlert a WHERE a.relatedIncident.id = :incidentId " +
            "AND a.status = 'ACTIVE' ORDER BY a.createdAt DESC")
    List<CyberAlert> findActiveByRelatedIncidentId(
            @Param("incidentId") Long incidentId);

    // ==========================================
    // DASHBOARD STATS - COUNT BY SEVERITY
    // ==========================================
    @Query("SELECT a.severity, COUNT(a) FROM CyberAlert a " +
            "GROUP BY a.severity")
    List<Object[]> countGroupBySeverity();
}
