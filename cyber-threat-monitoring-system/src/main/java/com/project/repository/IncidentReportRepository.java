package com.project.repository;

import com.project.entity.IncidentReport;
import com.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IncidentReportRepository extends JpaRepository<IncidentReport, Long> {

    // ==========================================
    // FIND BY REPORTED USER
    // ==========================================
    List<IncidentReport> findByReportedBy(User reportedBy);

    // ==========================================
    // FIND BY STATUS
    // ==========================================
    List<IncidentReport> findByStatus(IncidentReport.ReportStatus status);

    // ==========================================
    // FIND BY RISK LEVEL
    // ==========================================
    List<IncidentReport> findByRiskLevel(IncidentReport.RiskLevel riskLevel);

    // ==========================================
    // FIND BY INCIDENT TYPE
    // ==========================================
    List<IncidentReport> findByIncidentType(IncidentReport.IncidentType incidentType);

    // ==========================================
    // FIND BY REPORTED USER AND STATUS
    // ==========================================
    List<IncidentReport> findByReportedByAndStatus(
            User reportedBy,
            IncidentReport.ReportStatus status);

    // ==========================================
    // FIND BY RISK LEVEL AND STATUS
    // ==========================================
    List<IncidentReport> findByRiskLevelAndStatus(
            IncidentReport.RiskLevel riskLevel,
            IncidentReport.ReportStatus status);

    // ==========================================
    // FIND ALL ORDERED BY CREATED DATE
    // ==========================================
    List<IncidentReport> findAllByOrderByCreatedAtDesc();

    // ==========================================
    // FIND BY USER ORDERED BY CREATED DATE
    // ==========================================
    List<IncidentReport> findByReportedByOrderByCreatedAtDesc(User reportedBy);

    // ==========================================
    // FIND HIGH RISK PENDING REPORTS
    // ==========================================
    @Query("SELECT r FROM IncidentReport r WHERE " +
            "(r.riskLevel = 'HIGH' OR r.riskLevel = 'CRITICAL') AND " +
            "r.status = 'PENDING' " +
            "ORDER BY r.createdAt DESC")
    List<IncidentReport> findHighRiskPendingReports();

    // ==========================================
    // FIND REPORTS WITHIN DATE RANGE
    // ==========================================
    @Query("SELECT r FROM IncidentReport r WHERE " +
            "r.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY r.createdAt DESC")
    List<IncidentReport> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // ==========================================
    // SEARCH REPORTS BY KEYWORD
    // ==========================================
    @Query("SELECT r FROM IncidentReport r WHERE " +
            "LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<IncidentReport> searchByKeyword(@Param("keyword") String keyword);

    // ==========================================
    // COUNT BY STATUS
    // ==========================================
    @Query("SELECT COUNT(r) FROM IncidentReport r WHERE r.status = :status")
    Long countByStatus(@Param("status") IncidentReport.ReportStatus status);

    // ==========================================
    // COUNT BY RISK LEVEL
    // ==========================================
    @Query("SELECT COUNT(r) FROM IncidentReport r WHERE r.riskLevel = :riskLevel")
    Long countByRiskLevel(@Param("riskLevel") IncidentReport.RiskLevel riskLevel);

    // ==========================================
    // FIND REPORTS BY VERIFIED ADMIN
    // ==========================================
    List<IncidentReport> findByVerifiedBy(User verifiedBy);

    // ==========================================
    // DASHBOARD STATS - COUNT BY RISK LEVEL AND STATUS
    // ==========================================
    @Query("SELECT r.riskLevel, COUNT(r) FROM IncidentReport r " +
            "GROUP BY r.riskLevel")
    List<Object[]> countGroupByRiskLevel();

    // ==========================================
    // GRAPH AGGREGATION - COUNT BY INCIDENT TYPE
    // ==========================================
    @Query("SELECT r.incidentType, COUNT(r) FROM IncidentReport r " +
            "GROUP BY r.incidentType")
    List<Object[]> countGroupByIncidentType();

    // ==========================================
    // GRAPH AGGREGATION - MONTHLY COUNTS
    // threats, alerts, resolved
    // ==========================================
    @Query("SELECT YEAR(r.createdAt), MONTH(r.createdAt), COUNT(r), " +
            "SUM(CASE WHEN r.alertStatus <> 'NONE' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN r.status = 'VERIFIED' THEN 1 ELSE 0 END) " +
            "FROM IncidentReport r " +
            "GROUP BY YEAR(r.createdAt), MONTH(r.createdAt) " +
            "ORDER BY YEAR(r.createdAt), MONTH(r.createdAt)")
    List<Object[]> countMonthlyTrend();

    // ==========================================
    // GRAPH AGGREGATION - COUNT BY ALERT STATUS
    // ==========================================
    @Query("SELECT r.alertStatus, COUNT(r) FROM IncidentReport r " +
            "GROUP BY r.alertStatus")
    List<Object[]> countGroupByAlertStatus();

    // ==========================================
    // FIND RECENT REPORTS (LAST 7 DAYS)
    // ==========================================
    @Query("SELECT r FROM IncidentReport r WHERE " +
            "r.createdAt >= :sevenDaysAgo " +
            "ORDER BY r.createdAt DESC")
    List<IncidentReport> findRecentReports(
            @Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);

    // ==========================================
    // FIND REPORTS BY INSTITUTION
    // ==========================================
    @Query("SELECT r FROM IncidentReport r WHERE " +
            "r.reportedBy.institutionName = :institutionName " +
            "ORDER BY r.createdAt DESC")
    List<IncidentReport> findByInstitution(
            @Param("institutionName") String institutionName);

    Long countByCreatedAtAfter(LocalDateTime since);

    Long countBySuspiciousUrlAndCreatedAtAfter(
            String suspiciousUrl,
            LocalDateTime since);

    @Query("SELECT DISTINCT r.reportedBy.institutionName FROM IncidentReport r " +
            "WHERE r.createdAt >= :since AND r.reportedBy.institutionName IS NOT NULL")
    List<String> findDistinctInstitutionsSince(
            @Param("since") LocalDateTime since);
}
