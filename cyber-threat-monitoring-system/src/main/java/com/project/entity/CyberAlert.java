package com.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cyber_alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CyberAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==========================================
    // ALERT CONTENT
    // ==========================================
    @NotBlank(message = "Alert title is required")
    @Column(name = "title", nullable = false)
    private String title;

    @NotBlank(message = "Alert message is required")
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private Severity severity;

    // ==========================================
    // ALERT TARGETING
    // ==========================================
    @Column(name = "target_institution")
    private String targetInstitution;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic = true;

    // ==========================================
    // ALERT STATUS
    // ==========================================
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AlertStatus status = AlertStatus.ACTIVE;

    // ==========================================
    // PUBLISHED BY (ADMIN)
    // ==========================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_by", nullable = false)
    private User publishedBy;

    // ==========================================
    // LINKED INCIDENT REPORT (OPTIONAL)
    // ==========================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_incident_id")
    private IncidentReport relatedIncident;

    // ==========================================
    // EXPIRY
    // ==========================================
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // ==========================================
    // TIMESTAMPS
    // ==========================================
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ==========================================
    // ENUMS
    // ==========================================
    public enum AlertType {
        THREAT_WARNING,
        PHISHING_ALERT,
        MALWARE_ALERT,
        RANSOMWARE_ALERT,
        DATA_BREACH_ALERT,
        AWARENESS_TIP,
        SYSTEM_MAINTENANCE,
        GENERAL_NOTICE
    }

    public enum Severity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public enum AlertStatus {
        ACTIVE,
        EXPIRED,
        WITHDRAWN
    }

    // ==========================================
    // AUTO-SET TIMESTAMPS
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
    // CHECK IF ALERT IS EXPIRED
    // ==========================================
    public boolean isExpired() {
        if (this.expiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
}
