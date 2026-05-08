package com.project.controller;

import com.project.entity.CyberAlert;
import com.project.entity.IncidentReport;
import com.project.entity.User;
import com.project.security.JwtUtil;
import com.project.service.AlertService;
import com.project.service.ReportService;
import com.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;
    private final UserService userService;
    private final ReportService reportService;
    private final JwtUtil jwtUtil;

    // ==========================================
    // PUBLISH NEW ALERT (ADMIN)
    // POST /api/alerts/publish
    // ==========================================
    @PostMapping("/publish")
    public ResponseEntity<?> publishAlert(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> request) {
        try {

            // Extract admin user
            User admin = extractUserFromToken(authHeader);
            verifyAdminRole(authHeader);

            // Extract fields
            String title = (String) request.get("title");
            String message = (String) request.get("message");
            String alertTypeStr = (String) request.get("alertType");
            String severityStr = (String) request.get("severity");
            String targetInstitution =
                    (String) request.get("targetInstitution");
            Boolean isPublic = request.get("isPublic") != null
                    ? (Boolean) request.get("isPublic") : true;
            String expiresAtStr =
                    (String) request.get("expiresAt");

            // Validate required fields
            if (title == null || title.isBlank()) {
                return ResponseEntity
                        .badRequest()
                        .body(errorResponse("Title is required"));
            }
            if (message == null || message.isBlank()) {
                return ResponseEntity
                        .badRequest()
                        .body(errorResponse("Message is required"));
            }
            if (alertTypeStr == null || alertTypeStr.isBlank()) {
                return ResponseEntity
                        .badRequest()
                        .body(errorResponse(
                                "Alert type is required"));
            }
            if (severityStr == null || severityStr.isBlank()) {
                return ResponseEntity
                        .badRequest()
                        .body(errorResponse("Severity is required"));
            }

            // Parse alert type
            CyberAlert.AlertType alertType;
            try {
                alertType = CyberAlert.AlertType
                        .valueOf(alertTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity
                        .badRequest()
                        .body(errorResponse(
                                "Invalid alert type: "
                                        + alertTypeStr));
            }

            // Parse severity
            CyberAlert.Severity severity;
            try {
                severity = CyberAlert.Severity
                        .valueOf(severityStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity
                        .badRequest()
                        .body(errorResponse(
                                "Invalid severity: "
                                        + severityStr));
            }

            // Parse expiry date if provided
            LocalDateTime expiresAt = null;
            if (expiresAtStr != null && !expiresAtStr.isBlank()) {
                try {
                    expiresAt = LocalDateTime.parse(expiresAtStr);
                } catch (Exception e) {
                    return ResponseEntity
                            .badRequest()
                            .body(errorResponse(
                                    "Invalid expiresAt format."
                                            + " Use: yyyy-MM-ddTHH:mm:ss"));
                }
            }

            // Publish alert
            CyberAlert alert = alertService.publishAlert(
                    admin,
                    title,
                    message,
                    alertType,
                    severity,
                    targetInstitution,
                    isPublic,
                    expiresAt,
                    null
            );

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message",
                    "Alert published successfully");
            response.put("alert", buildAlertResponse(alert));

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);

        } catch (SecurityException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(errorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse(
                            "Failed to publish alert."
                                    + " Please try again"));
        }
    }

    // ==========================================
    // PUBLISH ALERT FROM INCIDENT (ADMIN)
    // POST /api/alerts/publish-from-incident/{incidentId}
    // ==========================================
    @PostMapping("/publish-from-incident/{incidentId}")
    public ResponseEntity<?> publishAlertFromIncident(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long incidentId,
            @RequestBody Map<String, Object> request) {
        try {

            // Extract admin user
            User admin = extractUserFromToken(authHeader);
            verifyAdminRole(authHeader);

            // Get verified incident
            IncidentReport incident = reportService
                    .getReportById(incidentId);

            // Extract fields
            String customMessage =
                    (String) request.get("customMessage");
            String severityStr =
                    (String) request.get("severity");
            String expiresAtStr =
                    (String) request.get("expiresAt");

            // Parse severity
            CyberAlert.Severity severity =
                    CyberAlert.Severity.HIGH;
            if (severityStr != null && !severityStr.isBlank()) {
                try {
                    severity = CyberAlert.Severity
                            .valueOf(severityStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity
                            .badRequest()
                            .body(errorResponse(
                                    "Invalid severity: "
                                            + severityStr));
                }
            }

            // Parse expiry date
            LocalDateTime expiresAt = null;
            if (expiresAtStr != null && !expiresAtStr.isBlank()) {
                try {
                    expiresAt = LocalDateTime.parse(expiresAtStr);
                } catch (Exception e) {
                    return ResponseEntity
                            .badRequest()
                            .body(errorResponse(
                                    "Invalid expiresAt format."
                                            + " Use: yyyy-MM-ddTHH:mm:ss"));
                }
            }

            // Publish alert from incident
            CyberAlert alert = alertService
                    .publishAlertFromIncident(
                            admin,
                            incident,
                            customMessage,
                            severity,
                            expiresAt
                    );

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message",
                    "Alert published from incident successfully");
            response.put("alert", buildAlertResponse(alert));

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);

        } catch (SecurityException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(errorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse(
                            "Failed to publish alert"
                                    + " from incident"));
        }
    }

    // ==========================================
    // GET ALL ALERTS (ADMIN)
    // GET /api/alerts/all
    // ==========================================
    @GetMapping("/all")
    public ResponseEntity<?> getAllAlerts(
            @RequestHeader("Authorization") String authHeader) {
        try {

            // Verify admin role
            verifyAdminRole(authHeader);

            // Get all alerts
            List<CyberAlert> alerts = alertService.getAllAlerts();

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", alerts.size());
            response.put("alerts", alerts.stream()
                    .map(this::buildAlertResponse)
                    .collect(Collectors.toList()));

            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse(
                            "Failed to fetch alerts"));
        }
    }

    // ==========================================
    // GET ACTIVE ALERTS (STUDENT)
    // GET /api/alerts/active
    // ==========================================
    @GetMapping("/active")
    public ResponseEntity<?> getActiveAlerts(
            @RequestHeader("Authorization") String authHeader) {
        try {

            // Get active alerts
            List<CyberAlert> alerts = alertService
                    .getActiveAlerts();

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", alerts.size());
            response.put("alerts", alerts.stream()
                    .map(this::buildAlertResponse)
                    .collect(Collectors.toList()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse(
                            "Failed to fetch active alerts"));
        }
    }

    // ==========================================
    // GET MY INSTITUTION ALERTS (STUDENT)
    // GET /api/alerts/my-institution
    // ==========================================
    @GetMapping("/my-institution")
    public ResponseEntity<?> getMyInstitutionAlerts(
            @RequestHeader("Authorization") String authHeader) {
        try {

            // Extract current user
            User currentUser = extractUserFromToken(authHeader);
            String institutionName =
                    currentUser.getInstitutionName();

            // Get alerts for institution
            List<CyberAlert> alerts = institutionName != null
                    ? alertService
                    .getAlertsForInstitution(institutionName)
                    : alertService.getPublicActiveAlerts();

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("institution", institutionName);
            response.put("count", alerts.size());
            response.put("alerts", alerts.stream()
                    .map(this::buildAlertResponse)
                    .collect(Collectors.toList()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse(
                            "Failed to fetch institution alerts"));
        }
    }

    // ==========================================
    // GET ALERT BY ID
    // GET /api/alerts/{id}
    // ==========================================
    @GetMapping("/{id}")
    public ResponseEntity<?> getAlertById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        try {

            // Get alert
            CyberAlert alert = alertService.getAlertById(id);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("alert", buildAlertResponse(alert));

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse(
                            "Failed to fetch alert"));
        }
    }

    // ==========================================
    // GET CRITICAL AND HIGH ALERTS
    // GET /api/alerts/critical
    // ==========================================
    @GetMapping("/critical")
    public ResponseEntity<?> getCriticalAlerts(
            @RequestHeader("Authorization") String authHeader) {
        try {

            // Get critical alerts
            List<CyberAlert> alerts = alertService
                    .getCriticalAndHighAlerts();

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", alerts.size());
            response.put("alerts", alerts.stream()
                    .map(this::buildAlertResponse)
                    .collect(Collectors.toList()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse(
                            "Failed to fetch critical alerts"));
        }
    }

    // ==========================================
    // GET RECENT ALERTS (LAST 7 DAYS)
    // GET /api/alerts/recent
    // ==========================================
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentAlerts(
            @RequestHeader("Authorization") String authHeader) {
        try {

            // Get recent alerts
            List<CyberAlert> alerts = alertService
                    .getRecentAlerts();

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", alerts.size());
            response.put("alerts", alerts.stream()
                    .map(this::buildAlertResponse)
                    .collect(Collectors.toList()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse(
                            "Failed to fetch recent alerts"));
        }
    }

    // ==========================================
    // GET ALERTS BY SEVERITY
    // GET /api/alerts/severity/{severity}
    // ==========================================
    @GetMapping("/severity/{severity}")
    public ResponseEntity<?> getAlertsBySeverity(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String severity) {
        try {

            // Parse severity
            CyberAlert.Severity alertSeverity;
            try {
                alertSeverity = CyberAlert.Severity
                        .valueOf(severity.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity
                        .badRequest()
                        .body(errorResponse(
                                "Invalid severity: " + severity));
            }

            // Get alerts
            List<CyberAlert> alerts = alertService
                    .getAlertsBySeverity(alertSeverity);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("severity", severity.toUpperCase());
            response.put("count", alerts.size());
            response.put("alerts", alerts.stream()
                    .map(this::buildAlertResponse)
                    .collect(Collectors.toList()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse(
                            "Failed to fetch alerts by severity"));
        }
    }

    // ==========================================
    // UPDATE ALERT (ADMIN)
    // PUT /api/alerts/{id}/update
    // ==========================================
    @PutMapping("/{id}/update")
    public ResponseEntity<?> updateAlert(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {

            // Extract admin user
            User admin = extractUserFromToken(authHeader);
            verifyAdminRole(authHeader);

            // Extract fields
            String title = (String) request.get("title");
            String message = (String) request.get("message");
            String severityStr = (String) request.get("severity");
            String targetInstitution =
                    (String) request.get("targetInstitution");
            Boolean isPublic = request.get("isPublic") != null
                    ? (Boolean) request.get("isPublic") : true;
            String expiresAtStr =
                    (String) request.get("expiresAt");

            // Validate required fields
            if (title == null || title.isBlank()) {
                return ResponseEntity
                        .badRequest()
                        .body(errorResponse("Title is required"));
            }
            if (message == null || message.isBlank()) {
                return ResponseEntity
                        .badRequest()
                        .body(errorResponse("Message is required"));
            }

            // Parse severity
            CyberAlert.Severity severity =
                    CyberAlert.Severity.MEDIUM;
            if (severityStr != null && !severityStr.isBlank()) {
                try {
                    severity = CyberAlert.Severity
                            .valueOf(severityStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity
                            .badRequest()
                            .body(errorResponse(
                                    "Invalid severity: "
                                            + severityStr));
                }
            }

            // Parse expiry date
            LocalDateTime expiresAt = null;
            if (expiresAtStr != null && !expiresAtStr.isBlank()) {
                try {
                    expiresAt = LocalDateTime.parse(expiresAtStr);
                } catch (Exception e) {
                    return ResponseEntity
                            .badRequest()
                            .body(errorResponse(
                                    "Invalid expiresAt format."
                                            + " Use: yyyy-MM-ddTHH:mm:ss"));
                }
            }

            // Update alert
            CyberAlert alert = alertService.updateAlert(
                    id,
                    admin,
                    title,
                    message,
                    severity,
                    targetInstitution,
                    isPublic,
                    expiresAt
            );

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message",
                    "Alert updated successfully");
            response.put("alert", buildAlertResponse(alert));

            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(errorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse(
                            "Failed to update alert"));
        }
    }

    // ==========================================
    // WITHDRAW ALERT (ADMIN)
    // PUT /api/alerts/{id}/withdraw
    // ==========================================
    @PutMapping("/{id}/withdraw")
    public ResponseEntity<?> withdrawAlert(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        try {

            // Extract admin user
            User admin = extractUserFromToken(authHeader);
            verifyAdminRole(authHeader);

            // Withdraw alert
            CyberAlert alert = alertService
                    .withdrawAlert(id, admin);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message",
                    "Alert withdrawn successfully");
            response.put("alert", buildAlertResponse(alert));

            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(errorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse(
                            "Failed to withdraw alert"));
        }
    }

    // ==========================================
    // SEARCH ALERTS
    // GET /api/alerts/search?keyword=phishing
    // ==========================================
    @GetMapping("/search")
    public ResponseEntity<?> searchAlerts(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String keyword) {
        try {

            // Search alerts
            List<CyberAlert> alerts = alertService
                    .searchAlerts(keyword);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("keyword", keyword);
            response.put("count", alerts.size());
            response.put("alerts", alerts.stream()
                    .map(this::buildAlertResponse)
                    .collect(Collectors.toList()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Search failed"));
        }
    }

    // ==========================================
    // GET ALERT STATS (ADMIN)
    // GET /api/alerts/stats
    // ==========================================
    @GetMapping("/stats")
    public ResponseEntity<?> getAlertStats(
            @RequestHeader("Authorization") String authHeader) {
        try {

            // Verify admin role
            verifyAdminRole(authHeader);

            // Get stats
            AlertService.AlertStats stats = alertService
                    .getDashboardStats();

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);

            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse(
                            "Failed to fetch alert stats"));
        }
    }

    // ==========================================
    // EXPIRE OUTDATED ALERTS (ADMIN)
    // PUT /api/alerts/expire-outdated
    // ==========================================
    @PutMapping("/expire-outdated")
    public ResponseEntity<?> expireOutdatedAlerts(
            @RequestHeader("Authorization") String authHeader) {
        try {

            // Verify admin role
            verifyAdminRole(authHeader);

            // Expire outdated alerts
            int count = alertService.expireOutdatedAlerts();

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message",
                    count + " alert(s) marked as expired");
            response.put("expiredCount", count);

            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse(
                            "Failed to expire alerts"));
        }
    }

    // ==========================================
    // HELPER - EXTRACT USER FROM TOKEN
    // ==========================================
    private User extractUserFromToken(String authHeader) {
        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);
        return userService.findById(userId);
    }

    // ==========================================
    // HELPER - VERIFY ADMIN ROLE
    // ==========================================
    private void verifyAdminRole(String authHeader) {
        String token = authHeader.substring(7);
        String role = jwtUtil.extractRole(token);
        if (!"ADMIN".equals(role)) {
            throw new SecurityException(
                    "Access denied. Admin role required");
        }
    }

    // ==========================================
    // HELPER - BUILD ALERT RESPONSE MAP
    // ==========================================
    private Map<String, Object> buildAlertResponse(
            CyberAlert alert) {

        Map<String, Object> map = new HashMap<>();
        map.put("id", alert.getId());
        map.put("title", alert.getTitle());
        map.put("message", alert.getMessage());
        map.put("alertType", alert.getAlertType());
        map.put("severity", alert.getSeverity());
        map.put("status", alert.getStatus());
        map.put("targetInstitution",
                alert.getTargetInstitution());
        map.put("isPublic", alert.isPublic());
        map.put("expiresAt", alert.getExpiresAt());
        map.put("isExpired", alert.isExpired());
        map.put("createdAt", alert.getCreatedAt());
        map.put("updatedAt", alert.getUpdatedAt());

        // Publisher info
        if (alert.getPublishedBy() != null) {
            Map<String, Object> publisher = new HashMap<>();
            publisher.put("id",
                    alert.getPublishedBy().getId());
            publisher.put("fullName",
                    alert.getPublishedBy().getFullName());
            publisher.put("email",
                    alert.getPublishedBy().getEmail());
            map.put("publishedBy", publisher);
        }

        // Related incident info
        if (alert.getRelatedIncident() != null) {
            Map<String, Object> incident = new HashMap<>();
            incident.put("id",
                    alert.getRelatedIncident().getId());
            incident.put("title",
                    alert.getRelatedIncident().getTitle());
            incident.put("riskLevel",
                    alert.getRelatedIncident().getRiskLevel());
            map.put("relatedIncident", incident);
        }

        return map;
    }

    // ==========================================
    // HELPER - BUILD ERROR RESPONSE MAP
    // ==========================================
    private Map<String, Object> errorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        return error;
    }
}