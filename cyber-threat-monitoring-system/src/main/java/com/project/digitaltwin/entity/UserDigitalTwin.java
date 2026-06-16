package com.project.digitaltwin.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

// ==========================================
// USER DIGITAL TWIN ENTITY
// Behavioral mirror for each user.
// Tracks risk history, anomaly scores,
// trust levels and learning iterations.
// ==========================================
@Entity
@Table(name = "user_digital_twins")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDigitalTwin {

    // ==========================================
    // TRUST LEVEL ENUM
    // ==========================================
    public enum TrustLevel {
        NEW,
        TRUSTED,
        NEUTRAL,
        SUSPICIOUS,
        HIGH_RISK
    }

    // ==========================================
    // PRIMARY KEY
    // ==========================================
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    // ==========================================
    // USER IDENTIFICATION
    // ==========================================
    @Column(
            name = "user_id",
            nullable = false,
            unique = true
    )
    private Long userId;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "institution_name")
    private String institutionName;

    // ==========================================
    // TRUST AND RISK METRICS
    // ==========================================
    @Enumerated(EnumType.STRING)
    @Column(
            name = "trust_level",
            nullable = false
    )
    private TrustLevel trustLevel =
            TrustLevel.NEW;

    @Column(name = "trust_score")
    private double trustScore = 50.0;

    @Column(name = "anomaly_score")
    private double anomalyScore = 0.0;

    @Column(name = "avg_risk_score")
    private double avgRiskScore = 0.0;

    @Column(name = "max_risk_score")
    private int maxRiskScore = 0;

    @Column(name = "min_risk_score")
    private int minRiskScore = 100;

    // ==========================================
    // REPORT COUNTERS
    // ==========================================
    @Column(
            name = "total_reports_submitted"
    )
    private int totalReportsSubmitted = 0;

    @Column(name = "high_risk_count")
    private int highRiskCount = 0;

    @Column(name = "critical_risk_count")
    private int criticalRiskCount = 0;

    @Column(name = "anomaly_detected_count")
    private int anomalyDetectedCount = 0;

    // ==========================================
    // LEARNING STATE
    // ==========================================
    @Column(name = "learning_iterations")
    private int learningIterations = 0;

    @Column(name = "baseline_established")
    private boolean baselineEstablished = false;

    // ==========================================
    // FLAGS
    // ==========================================
    @Column(name = "is_high_risk_user")
    private Boolean isHighRiskUser = false;

    @Column(name = "is_flagged")
    private Boolean isFlagged = false;

    @Column(
            name = "flag_reason",
            columnDefinition = "TEXT"
    )
    private String flagReason;

    // ==========================================
    // SCORE HISTORY
    // ==========================================
    @ElementCollection
    @CollectionTable(
            name = "twin_risk_score_history",
            joinColumns = @JoinColumn(name = "twin_id")
    )
    @OrderColumn(name = "history_index")
    @Column(name = "risk_score")
    private List<Integer> riskScoreHistory;

    // ==========================================
    // CATEGORY HISTORY
    // ==========================================
    @ElementCollection
    @CollectionTable(
            name = "twin_category_history",
            joinColumns = @JoinColumn(name = "twin_id")
    )
    @OrderColumn(name = "history_index")
    @Column(name = "category")
    private List<String> categoryHistory;

    // ==========================================
    // TIMESTAMPS
    // ==========================================
    @Column(
            name = "created_at",
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    @Column(name = "last_anomaly_at")
    private LocalDateTime lastAnomalyAt;

    // ==========================================
    // AUTO TIMESTAMPS
    // ==========================================
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================
    public boolean isHighRisk() {
        return Boolean.TRUE.equals(isHighRiskUser)
                || trustLevel == TrustLevel.HIGH_RISK;
    }

    public boolean isSuspicious() {
        return trustLevel == TrustLevel.SUSPICIOUS
                || trustLevel == TrustLevel.HIGH_RISK;
    }

    public boolean isTrusted() {
        return trustLevel == TrustLevel.TRUSTED
                && !Boolean.TRUE.equals(isFlagged);
    }

    public String getRiskSummary() {
        return "avg=" + avgRiskScore
                + " anomaly=" + anomalyScore
                + " trust=" + trustLevel
                + " reports=" + totalReportsSubmitted;
    }
}