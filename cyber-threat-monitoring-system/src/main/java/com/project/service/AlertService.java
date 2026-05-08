package com.project.service;

import com.project.entity.CyberAlert;
import com.project.entity.IncidentReport;
import com.project.entity.ThreatHistory;
import com.project.entity.User;
import com.project.repository.CyberAlertRepository;
import com.project.repository.ThreatHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final CyberAlertRepository cyberAlertRepository;
    private final ThreatHistoryRepository threatHistoryRepository;

    // ==========================================
    // PUBLISH NEW ALERT (ADMIN)
    // ==========================================
    @Transactional
    public CyberAlert publishAlert(
            User admin,
            String title,
            String message,
            CyberAlert.AlertType alertType,
            CyberAlert.Severity severity,
            String targetInstitution,
            boolean isPublic,
            LocalDateTime expiresAt,
            IncidentReport relatedIncident) {

        LocalDateTime resolvedExpiresAt = resolveExpiry(severity, expiresAt);
        CyberAlert.AlertStatus initialStatus =
                severity == CyberAlert.Severity.LOW
                        ? CyberAlert.AlertStatus.WITHDRAWN
                        : CyberAlert.AlertStatus.ACTIVE;

        // Create new alert
        CyberAlert alert = new CyberAlert();
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setAlertType(alertType);
        alert.setSeverity(severity);
        alert.setTargetInstitution(targetInstitution);
        alert.setPublic(isPublic);
        alert.setExpiresAt(resolvedExpiresAt);
        alert.setPublishedBy(admin);
        alert.setRelatedIncident(relatedIncident);
        alert.setStatus(initialStatus);

        // Save alert
        CyberAlert savedAlert = cyberAlertRepository.save(alert);

        // Log to threat history
        ThreatHistory history = ThreatHistory.forAlert(
                ThreatHistory.ActionType.ALERT_PUBLISHED,
                admin,
                savedAlert,
                "New cyber alert published by admin: "
                        + admin.getEmail()
                        + " | Title: " + title
                        + " | Severity: " + severity
                        + " | Type: " + alertType
                        + " | Status: " + initialStatus
        );
        threatHistoryRepository.save(history);

        return savedAlert;
    }

    // ==========================================
    // GET ALL ALERTS (ADMIN)
    // ==========================================
    public List<CyberAlert> getAllAlerts() {
        return cyberAlertRepository.findAllByOrderByCreatedAtDesc();
    }

    // ==========================================
    // GET ALERT BY ID
    // ==========================================
    public CyberAlert getAlertById(Long id) {
        return cyberAlertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Alert not found with id: " + id));
    }

    // ==========================================
    // GET ALL ACTIVE ALERTS (STUDENT)
    // ==========================================
    public List<CyberAlert> getActiveAlerts() {
        return cyberAlertRepository
                .findByStatusOrderByCreatedAtDesc(
                        CyberAlert.AlertStatus.ACTIVE);
    }

    // ==========================================
    // GET PUBLIC ACTIVE ALERTS (STUDENT)
    // ==========================================
    public List<CyberAlert> getPublicActiveAlerts() {
        return cyberAlertRepository
                .findByIsPublicTrueAndStatus(
                        CyberAlert.AlertStatus.ACTIVE)
                .stream()
                .filter(this::isPublishableToUsers)
                .collect(Collectors.toList());
    }

    // ==========================================
    // GET ALERTS FOR INSTITUTION (STUDENT)
    // ==========================================
    public List<CyberAlert> getAlertsForInstitution(
            String institutionName) {
        return cyberAlertRepository
                .findAlertsForInstitution(institutionName)
                .stream()
                .filter(this::isPublishableToUsers)
                .collect(Collectors.toList());
    }

    // ==========================================
    // GET ALERTS BY SEVERITY
    // ==========================================
    public List<CyberAlert> getAlertsBySeverity(
            CyberAlert.Severity severity) {
        return cyberAlertRepository.findBySeverity(severity);
    }

    // ==========================================
    // GET ALERTS BY TYPE
    // ==========================================
    public List<CyberAlert> getAlertsByType(
            CyberAlert.AlertType alertType) {
        return cyberAlertRepository.findByAlertType(alertType);
    }

    // ==========================================
    // GET CRITICAL AND HIGH ALERTS
    // ==========================================
    public List<CyberAlert> getCriticalAndHighAlerts() {
        return cyberAlertRepository.findCriticalAndHighAlerts();
    }

    // ==========================================
    // GET RECENT ALERTS (LAST 7 DAYS)
    // ==========================================
    public List<CyberAlert> getRecentAlerts() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return cyberAlertRepository.findRecentAlerts(sevenDaysAgo);
    }

    // ==========================================
    // GET ALERTS BY DATE RANGE
    // ==========================================
    public List<CyberAlert> getAlertsByDateRange(
            LocalDateTime startDate,
            LocalDateTime endDate) {
        return cyberAlertRepository.findByDateRange(startDate, endDate);
    }

    // ==========================================
    // SEARCH ALERTS BY KEYWORD
    // ==========================================
    public List<CyberAlert> searchAlerts(String keyword) {
        return cyberAlertRepository.searchByKeyword(keyword);
    }

    // ==========================================
    // GET ALERTS BY PUBLISHED ADMIN
    // ==========================================
    public List<CyberAlert> getAlertsByAdmin(User admin) {
        return cyberAlertRepository.findByPublishedBy(admin);
    }

    // ==========================================
    // GET ALERTS LINKED TO INCIDENT
    // ==========================================
    public List<CyberAlert> getAlertsByIncident(Long incidentId) {
        return cyberAlertRepository.findByRelatedIncidentId(incidentId);
    }

    // ==========================================
    // UPDATE ALERT (ADMIN)
    // ==========================================
    @Transactional
    public CyberAlert updateAlert(
            Long alertId,
            User admin,
            String title,
            String message,
            CyberAlert.Severity severity,
            String targetInstitution,
            boolean isPublic,
            LocalDateTime expiresAt) {

        CyberAlert alert = getAlertById(alertId);

        // Validate — only active alerts can be updated
        if (alert.getStatus() != CyberAlert.AlertStatus.ACTIVE) {
            throw new RuntimeException(
                    "Only ACTIVE alerts can be updated");
        }

        // Update fields
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setSeverity(severity);
        alert.setTargetInstitution(targetInstitution);
        alert.setPublic(isPublic);
        alert.setExpiresAt(expiresAt);

        CyberAlert updatedAlert = cyberAlertRepository.save(alert);

        // Log to threat history
        ThreatHistory history = ThreatHistory.forAlert(
                ThreatHistory.ActionType.ALERT_UPDATED,
                admin,
                updatedAlert,
                "Alert updated by admin: " + admin.getEmail()
                        + " | Alert ID: " + alertId
                        + " | New Severity: " + severity
        );
        threatHistoryRepository.save(history);

        return updatedAlert;
    }

    // ==========================================
    // WITHDRAW ALERT (ADMIN)
    // ==========================================
    @Transactional
    public CyberAlert withdrawAlert(Long alertId, User admin) {

        CyberAlert alert = getAlertById(alertId);

        // Validate — only active alerts can be withdrawn
        if (alert.getStatus() != CyberAlert.AlertStatus.ACTIVE) {
            throw new RuntimeException(
                    "Only ACTIVE alerts can be withdrawn");
        }

        // Update status
        alert.setStatus(CyberAlert.AlertStatus.WITHDRAWN);
        CyberAlert withdrawnAlert = cyberAlertRepository.save(alert);

        // Log to threat history
        ThreatHistory history = ThreatHistory.forAlert(
                ThreatHistory.ActionType.ALERT_WITHDRAWN,
                admin,
                withdrawnAlert,
                "Alert withdrawn by admin: " + admin.getEmail()
                        + " | Alert ID: " + alertId
                        + " | Title: " + alert.getTitle()
        );
        threatHistoryRepository.save(history);

        return withdrawnAlert;
    }

    // ==========================================
    // EXPIRE OUTDATED ALERTS (SCHEDULED CLEANUP)
    // ==========================================
    @Transactional
    public int expireOutdatedAlerts() {

        List<CyberAlert> expiredAlerts = cyberAlertRepository
                .findExpiredAlerts(LocalDateTime.now());

        int count = 0;
        for (CyberAlert alert : expiredAlerts) {
            alert.setStatus(CyberAlert.AlertStatus.EXPIRED);
            cyberAlertRepository.save(alert);

            // Log to threat history
            ThreatHistory history = ThreatHistory.forAlert(
                    ThreatHistory.ActionType.ALERT_EXPIRED,
                    alert.getPublishedBy(),
                    alert,
                    "Alert auto-expired: " + alert.getTitle()
                            + " | Expired at: " + alert.getExpiresAt()
            );
            threatHistoryRepository.save(history);
            count++;
        }

        return count;
    }

    // ==========================================
    // SCHEDULED EXPIRY SWEEP (EVERY 5 MINUTES)
    // ==========================================
    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void runAutoExpirySweep() {
        int expired = expireOutdatedAlerts();
        if (expired > 0) {
            log.info("Auto-expiry sweep marked {} alert(s) as EXPIRED", expired);
        }
    }

    // ==========================================
    // PUBLISH ALERT FROM VERIFIED INCIDENT (ADMIN)
    // ==========================================
    @Transactional
    public CyberAlert publishAlertFromIncident(
            User admin,
            IncidentReport verifiedIncident,
            String customMessage,
            CyberAlert.Severity severity,
            LocalDateTime expiresAt) {

        // Validate — only verified incidents can generate alerts
        if (verifiedIncident.getStatus()
                != IncidentReport.ReportStatus.VERIFIED) {
            throw new RuntimeException(
                    "Only VERIFIED incidents can generate alerts");
        }

        // Auto-determine alert type from incident type
        CyberAlert.AlertType alertType = mapIncidentTypeToAlertType(
                verifiedIncident.getIncidentType());

        // Build alert message
        String alertMessage = customMessage != null
                && !customMessage.isEmpty()
                ? customMessage
                : "Security alert based on verified incident: "
                + verifiedIncident.getTitle()
                + ". " + verifiedIncident.getDescription();

        return publishAlert(
                admin,
                "ALERT: " + verifiedIncident.getTitle(),
                alertMessage,
                alertType,
                severity,
                verifiedIncident.getReportedBy().getInstitutionName(),
                true,
                expiresAt,
                verifiedIncident
        );
    }

    @Transactional
    public CyberAlert publishProvisionalAlert(
            User admin,
            IncidentReport incident,
            String message,
            CyberAlert.Severity severity) {

        return publishAlert(
                admin,
                "[AI PROVISIONAL] " + incident.getTitle(),
                message,
                mapIncidentTypeToAlertType(incident.getIncidentType()),
                severity,
                incident.getReportedBy() != null
                        ? incident.getReportedBy().getInstitutionName()
                        : null,
                true,
                null,
                incident
        );
    }

    @Transactional
    public void promoteProvisionalAlert(User admin, IncidentReport incident) {
        boolean highOrCritical =
                incident.getRiskLevel() == IncidentReport.RiskLevel.HIGH
                        || incident.getRiskLevel() == IncidentReport.RiskLevel.CRITICAL;
        if (!highOrCritical) {
            return;
        }

        List<CyberAlert> activeAlerts =
                cyberAlertRepository.findActiveByRelatedIncidentId(incident.getId());

        if (activeAlerts.isEmpty()) {
            return;
        }

        CyberAlert provisional = activeAlerts.get(0);
        String formattedInputContent = formatInputContentForAlert(incident);
        provisional.setTitle(("ALERT: " + incident.getTitle()));
        provisional.setMessage(
                "ALERT: Suspicious threat verified\n\n"
                        + "Threat Type: " + (incident.getType() == null ? incident.getIncidentType() : incident.getType()) + "\n"
                        + "Input Content: " + formattedInputContent + "\n"
                        + "Final Score: " + (incident.getFinalScore() == null
                        ? incident.getRiskScore()
                        : String.format("%.0f", incident.getFinalScore())) + "/100\n"
                        + "Risk Level: " + incident.getRiskLevel() + "\n\n"
                        + "Reason: Threat confirmed by admin review.\n"
                        + "Recommended Action: Avoid interaction and follow security guidance."
        );
        provisional.setSeverity(mapRiskToSeverity(incident.getRiskLevel()));
        provisional.setStatus(CyberAlert.AlertStatus.ACTIVE);
        cyberAlertRepository.save(provisional);

        ThreatHistory history = ThreatHistory.forAlert(
                ThreatHistory.ActionType.ALERT_UPDATED,
                admin,
                provisional,
                "Provisional alert promoted to official for incident: "
                        + incident.getId()
        );
        threatHistoryRepository.save(history);
    }

    private String formatInputContentForAlert(IncidentReport incident) {
        if (incident == null) return "N/A";

        String type = incident.getType() == null ? "" : incident.getType().trim().toUpperCase();
        if ("URL".equals(type)) {
            return safeLine(incident.getSuspiciousUrl(), 180);
        }
        if ("EMAIL".equals(type)) {
            String sender = safeLine(incident.getSuspiciousEmail(), 100);
            String subject = extractFromTextContent(incident.getTextContent(), "Subject:");
            if (subject == null || subject.isBlank()) subject = "N/A";
            return "Sender: " + sender + " | Subject: " + safeLine(subject, 120);
        }
        if ("DOCUMENT".equals(type)) {
            String details = cleanNarrative(
                    extractFromTextContent(incident.getTextContent(), "Extracted Text:")
            );
            return "Document Link: " + safeLine(incident.getDocumentPath(), 180)
                    + " | Reported By: " + safeLine(
                    incident.getReportedBy() != null
                            ? incident.getReportedBy().getEmail()
                            : null,
                    120
            )
                    + " | Reported Time: " + (
                    incident.getCreatedAt() == null
                            ? "N/A"
                            : incident.getCreatedAt().toString()
            )
                    + " | Attack Details: " + safeLine(details, 220);
        }
        if ("IMAGE".equals(type)) {
            String fileName = extractFileNameFromPath(incident.getImagePath());
            String text = safeLine(incident.getTextContent(), 140);
            return "Image: " + safeLine(fileName, 80) + " | Extracted Text: " + text;
        }
        return safeLine(incident.getTextContent(), 180);
    }

    private String extractFromTextContent(String textContent, String key) {
        if (textContent == null || key == null) return null;
        String[] parts = textContent.split("\\|");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.toLowerCase().startsWith(key.toLowerCase())) {
                return trimmed.substring(key.length()).trim();
            }
        }
        return null;
    }

    private String extractFileNameFromPath(String filePath) {
        if (filePath == null || filePath.isBlank()) return "N/A";
        int idx = filePath.lastIndexOf('/');
        if (idx >= 0 && idx < filePath.length() - 1) {
            return filePath.substring(idx + 1);
        }
        return filePath;
    }

    private String safeLine(String value, int maxLength) {
        if (value == null || value.isBlank()) return "N/A";
        String cleaned = value
                .replace("\n", " ")
                .replace("\r", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (cleaned.length() <= maxLength) {
            return cleaned;
        }
        return cleaned.substring(0, maxLength) + "...";
    }

    private String cleanNarrative(String value) {
        if (value == null || value.isBlank()) return "N/A";
        return value
                .replaceAll("(?i)=+\\s*ATTACK\\s*NARRATIVE\\s*=+", "")
                .replaceAll("(?i)=+\\s*END\\s*=+", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    @Transactional
    public void rejectProvisionalAlert(User admin, IncidentReport incident, String reason) {
        List<CyberAlert> activeAlerts =
                cyberAlertRepository.findActiveByRelatedIncidentId(incident.getId());

        for (CyberAlert alert : activeAlerts) {
            alert.setStatus(CyberAlert.AlertStatus.WITHDRAWN);
            cyberAlertRepository.save(alert);

            ThreatHistory history = ThreatHistory.forAlert(
                    ThreatHistory.ActionType.ALERT_WITHDRAWN,
                    admin,
                    alert,
                    "Provisional alert rejected and withdrawn for incident: "
                            + incident.getId()
                            + " | Reason: "
                            + (reason == null ? "" : reason)
            );
            threatHistoryRepository.save(history);
        }
    }

    // ==========================================
    // MAP INCIDENT TYPE TO ALERT TYPE
    // ==========================================
    private CyberAlert.AlertType mapIncidentTypeToAlertType(
            IncidentReport.IncidentType incidentType) {

        switch (incidentType) {
            case PHISHING_EMAIL:
                return CyberAlert.AlertType.PHISHING_ALERT;
            case MALWARE:
                return CyberAlert.AlertType.MALWARE_ALERT;
            case RANSOMWARE:
                return CyberAlert.AlertType.RANSOMWARE_ALERT;
            case DATA_BREACH:
                return CyberAlert.AlertType.DATA_BREACH_ALERT;
            case SUSPICIOUS_URL:
                return CyberAlert.AlertType.THREAT_WARNING;
            case UNAUTHORIZED_ACCESS:
                return CyberAlert.AlertType.THREAT_WARNING;
            case SOCIAL_ENGINEERING:
                return CyberAlert.AlertType.AWARENESS_TIP;
            default:
                return CyberAlert.AlertType.GENERAL_NOTICE;
        }
    }

    private CyberAlert.Severity mapRiskToSeverity(IncidentReport.RiskLevel level) {
        if (level == null) return CyberAlert.Severity.MEDIUM;
        switch (level) {
            case CRITICAL:
                return CyberAlert.Severity.CRITICAL;
            case HIGH:
                return CyberAlert.Severity.HIGH;
            case MEDIUM:
                return CyberAlert.Severity.MEDIUM;
            case LOW:
            default:
                return CyberAlert.Severity.LOW;
        }
    }

    private LocalDateTime resolveExpiry(
            CyberAlert.Severity severity,
            LocalDateTime requestedExpiry) {
        if (requestedExpiry != null) {
            return requestedExpiry;
        }
        if (severity == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        switch (severity) {
            case CRITICAL:
            case HIGH:
                return now.plusHours(24);
            case MEDIUM:
                return now.plusHours(48);
            case LOW:
            default:
                return null;
        }
    }

    private boolean isPublishableToUsers(CyberAlert alert) {
        if (alert == null || alert.getSeverity() == null) {
            return false;
        }
        return alert.getSeverity() == CyberAlert.Severity.HIGH
                || alert.getSeverity() == CyberAlert.Severity.CRITICAL;
    }

    // ==========================================
    // GET DASHBOARD STATS
    // ==========================================
    public AlertStats getDashboardStats() {

        Long totalAlerts = cyberAlertRepository.count();
        Long activeAlerts = cyberAlertRepository
                .countByStatus(CyberAlert.AlertStatus.ACTIVE);
        Long expiredAlerts = cyberAlertRepository
                .countByStatus(CyberAlert.AlertStatus.EXPIRED);
        Long withdrawnAlerts = cyberAlertRepository
                .countByStatus(CyberAlert.AlertStatus.WITHDRAWN);
        Long criticalAlerts = cyberAlertRepository
                .countBySeverity(CyberAlert.Severity.CRITICAL);
        Long highAlerts = cyberAlertRepository
                .countBySeverity(CyberAlert.Severity.HIGH);
        Long mediumAlerts = cyberAlertRepository
                .countBySeverity(CyberAlert.Severity.MEDIUM);
        Long lowAlerts = cyberAlertRepository
                .countBySeverity(CyberAlert.Severity.LOW);

        return new AlertStats(
                totalAlerts,
                activeAlerts,
                expiredAlerts,
                withdrawnAlerts,
                criticalAlerts,
                highAlerts,
                mediumAlerts,
                lowAlerts
        );
    }

    // ==========================================
    // ALERT STATS INNER CLASS
    // ==========================================
    public static class AlertStats {
        public Long totalAlerts;
        public Long activeAlerts;
        public Long expiredAlerts;
        public Long withdrawnAlerts;
        public Long criticalAlerts;
        public Long highAlerts;
        public Long mediumAlerts;
        public Long lowAlerts;

        public AlertStats(Long totalAlerts,
                          Long activeAlerts,
                          Long expiredAlerts,
                          Long withdrawnAlerts,
                          Long criticalAlerts,
                          Long highAlerts,
                          Long mediumAlerts,
                          Long lowAlerts) {
            this.totalAlerts = totalAlerts;
            this.activeAlerts = activeAlerts;
            this.expiredAlerts = expiredAlerts;
            this.withdrawnAlerts = withdrawnAlerts;
            this.criticalAlerts = criticalAlerts;
            this.highAlerts = highAlerts;
            this.mediumAlerts = mediumAlerts;
            this.lowAlerts = lowAlerts;
        }
    }
}

