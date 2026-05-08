package com.project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "threat_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThreatHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==========================================
    // ACTION TYPE
    // ==========================================
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;

    // ==========================================
    // PERFORMED BY
    // ==========================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by", nullable = false)
    private User performedBy;

    // ==========================================
    // LINKED INCIDENT REPORT (OPTIONAL)
    // ==========================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_report_id")
    private IncidentReport incidentReport;

    // ==========================================
    // LINKED CYBER ALERT (OPTIONAL)
    // ==========================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cyber_alert_id")
    private CyberAlert cyberAlert;

    // ==========================================
    // ACTIVITY DETAILS
    // ==========================================
    @Column(name = "action_description", nullable = false, columnDefinition = "TEXT")
    private String actionDescription;

    @Column(name = "previous_status")
    private String previousStatus;

    @Column(name = "new_status")
    private String newStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level_at_time")
    private IncidentReport.RiskLevel riskLevelAtTime;

    @Column(name = "risk_score_at_time")
    private Integer riskScoreAtTime;

    // ==========================================
    // ADDITIONAL METADATA
    // ==========================================
    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    // ==========================================
    // TIMESTAMP
    // ==========================================
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ==========================================
    // ACTION TYPE ENUM
    // ==========================================
    public enum ActionType {

        // Incident Report Actions
        REPORT_SUBMITTED,
        REPORT_UPDATED,
        REPORT_UNDER_REVIEW,
        REPORT_VERIFIED,
        REPORT_DISMISSED,
        REPORT_REANALYZED,
        INTELLIGENT_ANALYSIS,

        // Risk Scoring Actions
        RISK_SCORE_CALCULATED,
        RISK_LEVEL_UPDATED,

        // Alert Actions
        ALERT_PUBLISHED,
        ALERT_UPDATED,
        ALERT_WITHDRAWN,
        ALERT_EXPIRED,

        // User Actions
        USER_REGISTERED,
        USER_LOGIN,
        USER_LOGOUT,
        USER_DEACTIVATED,

        // Admin Actions
        ADMIN_REVIEW_STARTED,
        ADMIN_REMARKS_ADDED
    }

    // ==========================================
    // AUTO-SET TIMESTAMP
    // ==========================================
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ==========================================
    // STATIC FACTORY METHODS
    // ==========================================

    // Create history for incident report actions
    public static ThreatHistory forIncident(
            ActionType actionType,
            User performedBy,
            IncidentReport incidentReport,
            String previousStatus,
            String newStatus,
            String description) {

        ThreatHistory history = new ThreatHistory();
        history.setActionType(actionType);
        history.setPerformedBy(performedBy);
        history.setIncidentReport(incidentReport);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setActionDescription(description);
        history.setRiskLevelAtTime(incidentReport.getRiskLevel());
        history.setRiskScoreAtTime(incidentReport.getRiskScore());
        return history;
    }

    // Create history for alert actions
    public static ThreatHistory forAlert(
            ActionType actionType,
            User performedBy,
            CyberAlert cyberAlert,
            String description) {

        ThreatHistory history = new ThreatHistory();
        history.setActionType(actionType);
        history.setPerformedBy(performedBy);
        history.setCyberAlert(cyberAlert);
        history.setActionDescription(description);
        return history;
    }

    // Create history for user actions
    public static ThreatHistory forUser(
            ActionType actionType,
            User performedBy,
            String description) {

        ThreatHistory history = new ThreatHistory();
        history.setActionType(actionType);
        history.setPerformedBy(performedBy);
        history.setActionDescription(description);
        return history;
    }
}
