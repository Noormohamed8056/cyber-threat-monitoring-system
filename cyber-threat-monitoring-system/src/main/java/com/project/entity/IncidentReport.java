package com.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "incident_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==========================================
    // REPORTER INFORMATION
    // ==========================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by", nullable = false)
    private User reportedBy;

    // ==========================================
    // INCIDENT DETAILS
    // ==========================================
    @NotBlank(message = "Title is required")
    @Column(name = "title", nullable = false)
    private String title;

    @NotBlank(message = "Description is required")
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "incident_type", nullable = false)
    private IncidentType incidentType;

    @Column(name = "type")
    private String type;

    @Column(name = "text_content", columnDefinition = "TEXT")
    private String textContent;

    @Column(name = "suspicious_url", columnDefinition = "TEXT")
    private String suspiciousUrl;

    @Column(name = "suspicious_email", columnDefinition = "TEXT")
    private String suspiciousEmail;

    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "document_path")
    private String documentPath;

    // ==========================================
    // AI RISK SCORING
    // ==========================================
    @Column(name = "prediction")
    private String prediction;

    @Column(name = "ml_score")
    private Double mlScore;

    @Column(name = "ai_score")
    private Double aiScore;

    @Column(name = "final_score")
    private Double finalScore;

    @Column(name = "confidence")
    private Double confidence;

    @Column(name = "risk_score")
    private Integer riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level")
    private RiskLevel riskLevel;

    @Column(name = "ai_analysis", columnDefinition = "TEXT")
    private String aiAnalysis;

    @Column(name = "current_spread")
    private Integer currentSpread;

    @Column(name = "predicted_spread")
    private Integer predictedSpread;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_status")
    private AlertStatus alertStatus = AlertStatus.NONE;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status")
    private VerificationStatus verificationStatus =
            VerificationStatus.PENDING_ADMIN;

    @Enumerated(EnumType.STRING)
    @Column(name = "admin_decision")
    private AdminDecision adminDecision;

    @Column(name = "review_time")
    private LocalDateTime reviewTime;

    // ==========================================
    // ADMIN VERIFICATION
    // ==========================================
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportStatus status = ReportStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private User verifiedBy;

    @Column(name = "admin_remarks", columnDefinition = "TEXT")
    private String adminRemarks;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

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
    public enum IncidentType {
        PHISHING_EMAIL,
        SUSPICIOUS_URL,
        MALWARE,
        RANSOMWARE,
        UNAUTHORIZED_ACCESS,
        DATA_BREACH,
        SOCIAL_ENGINEERING,
        OTHER
    }

    public enum RiskLevel {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public enum ReportStatus {
        PENDING,
        UNDER_REVIEW,
        VERIFIED,
        DISMISSED
    }

    public enum AlertStatus {
        NONE,
        AI_PROVISIONAL,
        OFFICIAL,
        REJECTED
    }

    public enum VerificationStatus {
        PENDING_ADMIN,
        APPROVED,
        REJECTED,
        MONITORING,
        NOT_REQUIRED
    }

    public enum AdminDecision {
        CONFIRM_THREAT,
        MARK_SAFE,
        MONITOR
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
    // RULE-BASED AI RISK SCORING METHOD
    // ==========================================
    public void calculateRiskScore() {
        int score = 0;
        StringBuilder analysis = new StringBuilder();

        // --- Score by Incident Type ---
        switch (this.incidentType) {
            case RANSOMWARE:
                score += 40;
                analysis.append("Ransomware detected: +40 points. ");
                break;
            case DATA_BREACH:
                score += 35;
                analysis.append("Data breach reported: +35 points. ");
                break;
            case MALWARE:
                score += 30;
                analysis.append("Malware threat: +30 points. ");
                break;
            case UNAUTHORIZED_ACCESS:
                score += 25;
                analysis.append("Unauthorized access attempt: +25 points. ");
                break;
            case PHISHING_EMAIL:
                score += 20;
                analysis.append("Phishing email detected: +20 points. ");
                break;
            case SUSPICIOUS_URL:
                score += 15;
                analysis.append("Suspicious URL reported: +15 points. ");
                break;
            case SOCIAL_ENGINEERING:
                score += 20;
                analysis.append("Social engineering attempt: +20 points. ");
                break;
            default:
                score += 10;
                analysis.append("Other incident type: +10 points. ");
        }

        // --- Score by Suspicious URL Keywords ---
        if (this.suspiciousUrl != null && !this.suspiciousUrl.isEmpty()) {
            String url = this.suspiciousUrl.toLowerCase();
            if (url.contains("login") || url.contains("signin")) {
                score += 15;
                analysis.append("URL contains login/signin keyword: +15 points. ");
            }
            if (url.contains("bank") || url.contains("payment") || url.contains("verify")) {
                score += 20;
                analysis.append("URL contains financial keyword: +20 points. ");
            }
            if (url.contains(".exe") || url.contains(".zip") || url.contains(".bat")) {
                score += 25;
                analysis.append("URL contains executable file extension: +25 points. ");
            }
            if (url.contains("free") || url.contains("prize") || url.contains("winner")) {
                score += 10;
                analysis.append("URL contains scam keyword: +10 points. ");
            }
        }

        // --- Score by Suspicious Email Keywords ---
        if (this.suspiciousEmail != null && !this.suspiciousEmail.isEmpty()) {
            String email = this.suspiciousEmail.toLowerCase();
            if (email.contains("urgent") || email.contains("immediate")) {
                score += 15;
                analysis.append("Email contains urgency keyword: +15 points. ");
            }
            if (email.contains("password") || email.contains("credentials")) {
                score += 20;
                analysis.append("Email requests credentials: +20 points. ");
            }
            if (email.contains("click here") || email.contains("verify now")) {
                score += 10;
                analysis.append("Email contains suspicious call-to-action: +10 points. ");
            }
            if (email.contains("lottery") || email.contains("inheritance")
                    || email.contains("million")) {
                score += 15;
                analysis.append("Email contains scam indicators: +15 points. ");
            }
        }

        // --- Score by Description Keywords ---
        if (this.description != null) {
            String desc = this.description.toLowerCase();
            if (desc.contains("data loss") || desc.contains("files encrypted")) {
                score += 20;
                analysis.append("Description mentions data loss/encryption: +20 points. ");
            }
            if (desc.contains("system crash") || desc.contains("network down")) {
                score += 15;
                analysis.append("Description mentions system impact: +15 points. ");
            }
        }

        // --- Cap score at 100 ---
        this.riskScore = Math.min(score, 100);

        // --- Assign Risk Level ---
        if (this.riskScore >= 81) {
            this.riskLevel = RiskLevel.CRITICAL;
            analysis.append("=> RISK LEVEL: CRITICAL (Score: ").append(this.riskScore).append(")");
        } else if (this.riskScore >= 61) {
            this.riskLevel = RiskLevel.HIGH;
            analysis.append("=> RISK LEVEL: HIGH (Score: ").append(this.riskScore).append(")");
        } else if (this.riskScore >= 31) {
            this.riskLevel = RiskLevel.MEDIUM;
            analysis.append("=> RISK LEVEL: MEDIUM (Score: ").append(this.riskScore).append(")");
        } else {
            this.riskLevel = RiskLevel.LOW;
            analysis.append("=> RISK LEVEL: LOW (Score: ").append(this.riskScore).append(")");
        }

        this.aiAnalysis = analysis.toString();
    }
}
