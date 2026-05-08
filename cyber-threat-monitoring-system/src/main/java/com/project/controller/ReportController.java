package com.project.controller;

import com.project.digitaltwin.service.IntelligentReportService;
import com.project.entity.IncidentReport;
import com.project.entity.User;
import com.project.security.JwtUtil;
import com.project.service.ReportService;
import com.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final IntelligentReportService intelligentReportService;

    // ==========================================
    // SUBMIT NEW INCIDENT REPORT (STUDENT)
    // POST /api/reports/submit
    // ==========================================
    @PostMapping("/submit")
    public ResponseEntity<?> submitReport(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {
        try {

            // Extract current user from token
            User student = extractUserFromToken(authHeader);

            // Extract fields
            String title = request.get("title");
            String description = request.get("description");
            String incidentTypeStr = request.get("incidentType");
            String suspiciousUrl = request.get("suspiciousUrl");
            String suspiciousEmail = request.get("suspiciousEmail");

            if (suspiciousUrl != null && !suspiciousUrl.isBlank()) {
                String domain = extractDomain(suspiciousUrl);
                if (intelligentReportService.isDomainBlocked(domain)) {
                    return ResponseEntity
                            .status(HttpStatus.FORBIDDEN)
                            .body(errorResponse("Access blocked for security"));
                }
            }

            // Validate required fields
            if (title == null || title.isBlank()) {
                return ResponseEntity
                        .badRequest()
                        .body(errorResponse("Title is required"));
            }
            if (description == null || description.isBlank()) {
                return ResponseEntity
                        .badRequest()
                        .body(errorResponse(
                                "Description is required"));
            }
            if (incidentTypeStr == null
                    || incidentTypeStr.isBlank()) {
                return ResponseEntity
                        .badRequest()
                        .body(errorResponse(
                                "Incident type is required"));
            }

            // Parse incident type
            IncidentReport.IncidentType incidentType;
            try {
                incidentType = IncidentReport.IncidentType
                        .valueOf(incidentTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity
                        .badRequest()
                        .body(errorResponse(
                                "Invalid incident type: "
                                        + incidentTypeStr));
            }

            // Submit report
            IncidentReport report = reportService.submitReport(
                    student,
                    title,
                    description,
                    incidentType,
                    suspiciousUrl,
                    suspiciousEmail
            );

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message",
                    "Incident report submitted successfully");
            response.put("report", buildReportResponse(report));

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse(
                            "Failed to submit report."
                                    + " Please try again"));
        }
    }

    // ==========================================
    // GET MY REPORTS (STUDENT)
    // GET /api/reports/my-reports
    // ==========================================
    @GetMapping("/my-reports")
    public ResponseEntity<?> getMyReports(
            @RequestHeader("Authorization") String authHeader) {
        try {

            // Extract current user from token
            User student = extractUserFromToken(authHeader);

            // Get reports
            List<IncidentReport> reports = reportService
                    .getReportsByStudent(student);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", reports.size());
            response.put("reports", reports.stream()
                    .map(this::buildReportResponse)
                    .collect(Collectors.toList()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse(
                            "Failed to fetch your reports"));
        }
    }

    // ==========================================
    // GET REPORT BY ID
    // GET /api/reports/{id}
    // ==========================================
    @GetMapping("/{id}")
    public ResponseEntity<?> getReportById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        try {

            // Extract current user from token
            User currentUser = extractUserFromToken(authHeader);

            // Get report
            IncidentReport report = reportService
                    .getReportById(id);

            // Students can only view their own reports
            if (currentUser.getRole() == User.Role.STUDENT
                    && !report.getReportedBy()
                    .getId().equals(currentUser.getId())) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(errorResponse(
                                "You can only view your own reports"));
            }

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("report", buildReportResponse(report));

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse(
                            "Failed to fetch report"));
        }
    }

    // ==========================================
    // GET ALL REPORTS (ADMIN)
    // GET /api/reports/all
    // ==========================================
    @GetMapping("/all")
    public ResponseEntity<?> getAllReports(
            @RequestHeader("Authorization") String authHeader) {
        try {

            // Verify admin role
            verifyAdminRole(authHeader);

            // Get all reports
            List<IncidentReport> reports = reportService
                    .getAllReports();

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", reports.size());
            response.put("reports", reports.stream()
                    .map(this::buildReportResponse)
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
                            "Failed to fetch reports"));
        }
    }

    // ==========================================
    // GET REPORTS BY STATUS (ADMIN)
    // GET /api/reports/status/{status}
    // ==========================================
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getReportsByStatus(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String status) {
        try {

            // Verify admin role
            verifyAdminRole(authHeader);

            // Parse status
            IncidentReport.ReportStatus reportStatus =
                    IncidentReport.ReportStatus
                            .valueOf(status.toUpperCase());

            // Get reports
            List<IncidentReport> reports = reportService
                    .getReportsByStatus(reportStatus);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("status", status.toUpperCase());
            response.put("count", reports.size());
            response.put("reports", reports.stream()
                    .map(this::buildReportResponse)
                    .collect(Collectors.toList()));

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(errorResponse(
                            "Invalid status: " + status));
        } catch (SecurityException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse(
                            "Failed to fetch reports by status"));
        }
    }

    // ==========================================
    // GET REPORTS BY RISK LEVEL (ADMIN)
    // GET /api/reports/risk/{riskLevel}
    // ==========================================
    @GetMapping("/risk/{riskLevel}")
    public ResponseEntity<?> getReportsByRiskLevel(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String riskLevel) {
        try {

            // Verify admin role
            verifyAdminRole(authHeader);

            // Parse risk level
            IncidentReport.RiskLevel level =
                    IncidentReport.RiskLevel
                            .valueOf(riskLevel.toUpperCase());

            // Get reports
            List<IncidentReport> reports = reportService
                    .getReportsByRiskLevel(level);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("riskLevel", riskLevel.toUpperCase());
            response.put("count", reports.size());
            response.put("reports", reports.stream()
                    .map(this::buildReportResponse)
                    .collect(Collectors.toList()));

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(errorResponse(
                            "Invalid risk level: " + riskLevel));
        } catch (SecurityException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse(
                            "Failed to fetch reports by risk level"));
        }
    }

    // ==========================================
    // GET HIGH RISK PENDING REPORTS (ADMIN)
    // GET /api/reports/high-risk-pending
    // ==========================================
    @GetMapping("/high-risk-pending")
    public ResponseEntity<?> getHighRiskPendingReports(
            @RequestHeader("Authorization") String authHeader) {
        try {

            // Verify admin role
            verifyAdminRole(authHeader);

            // Get reports
            List<IncidentReport> reports = reportService
                    .getHighRiskPendingReports();

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", reports.size());
            response.put("reports", reports.stream()
                    .map(this::buildReportResponse)
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
                            "Failed to fetch high risk reports"));
        }
    }

    // ==========================================
    // START REVIEW (ADMIN)
    // PUT /api/reports/{id}/start-review
    // ==========================================
    @PutMapping("/{id}/start-review")
    public ResponseEntity<?> startReview(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        try {

            // Extract admin user
            User admin = extractUserFromToken(authHeader);
            verifyAdminRole(authHeader);

            // Start review
            IncidentReport report = reportService
                    .startReview(id, admin);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message",
                    "Report review started successfully");
            response.put("report", buildReportResponse(report));

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
                            "Failed to start review"));
        }
    }

    // ==========================================
    // VERIFY REPORT (ADMIN)
    // PUT /api/reports/{id}/verify
    // ==========================================
    @PutMapping("/{id}/verify")
    public ResponseEntity<?> verifyReport(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {

            // Extract admin user
            User admin = extractUserFromToken(authHeader);
            verifyAdminRole(authHeader);

            String adminRemarks = request.get("adminRemarks");

            // Verify report
            IncidentReport report = reportService
                    .verifyReport(id, admin, adminRemarks);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message",
                    "Report verified successfully");
            response.put("report", buildReportResponse(report));

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
                            "Failed to verify report"));
        }
    }

    // ==========================================
    // DISMISS REPORT (ADMIN)
    // PUT /api/reports/{id}/dismiss
    // ==========================================
    @PutMapping("/{id}/dismiss")
    public ResponseEntity<?> dismissReport(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {

            // Extract admin user
            User admin = extractUserFromToken(authHeader);
            verifyAdminRole(authHeader);

            String adminRemarks = request.get("adminRemarks");

            // Dismiss report
            IncidentReport report = reportService
                    .dismissReport(id, admin, adminRemarks);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message",
                    "Report dismissed successfully");
            response.put("report", buildReportResponse(report));

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
                            "Failed to dismiss report"));
        }
    }

    // ==========================================
    // MONITOR REPORT (ADMIN)
    // PUT /api/reports/{id}/monitor
    // ==========================================
    @PutMapping("/{id}/monitor")
    public ResponseEntity<?> monitorReport(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            User admin = extractUserFromToken(authHeader);
            verifyAdminRole(authHeader);

            String adminRemarks = request.get("adminRemarks");
            IncidentReport report = reportService.monitorReport(
                    id,
                    admin,
                    adminRemarks
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Report marked for monitoring");
            response.put("report", buildReportResponse(report));
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
                    .body(errorResponse("Failed to mark report for monitoring"));
        }
    }

    // ==========================================
    // SEARCH REPORTS (ADMIN)
    // GET /api/reports/search?keyword=phishing
    // ==========================================
    @GetMapping("/search")
    public ResponseEntity<?> searchReports(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String keyword) {
        try {

            // Verify admin role
            verifyAdminRole(authHeader);

            // Search reports
            List<IncidentReport> reports = reportService
                    .searchReports(keyword);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("keyword", keyword);
            response.put("count", reports.size());
            response.put("reports", reports.stream()
                    .map(this::buildReportResponse)
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
                            "Search failed"));
        }
    }

    // ==========================================
    // GET REPORT STATS (ADMIN)
    // GET /api/reports/stats
    // ==========================================
    @GetMapping("/stats")
    public ResponseEntity<?> getReportStats(
            @RequestHeader("Authorization") String authHeader) {
        try {

            // Verify admin role
            verifyAdminRole(authHeader);

            // Get stats
            ReportService.ReportStats stats = reportService
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
                            "Failed to fetch stats"));
        }
    }

    // ==========================================
    // GET RECENT REPORTS (ADMIN)
    // GET /api/reports/recent
    // ==========================================
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentReports(
            @RequestHeader("Authorization") String authHeader) {
        try {

            // Verify admin role
            verifyAdminRole(authHeader);

            // Get recent reports
            List<IncidentReport> reports = reportService
                    .getRecentReports();

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", reports.size());
            response.put("reports", reports.stream()
                    .map(this::buildReportResponse)
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
                            "Failed to fetch recent reports"));
        }
    }

    // ==========================================
    // CHECK DOMAIN BLOCK STATUS
    // GET /api/reports/domain-check?url=
    // ==========================================
    @GetMapping("/domain-check")
    public ResponseEntity<?> checkDomainStatus(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String url) {
        try {
            extractUserFromToken(authHeader);
            String domain = extractDomain(url);
            boolean blocked = intelligentReportService.isDomainBlocked(domain);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("domain", domain);
            response.put("blocked", blocked);
            response.put(
                    "message",
                    blocked
                            ? "Access blocked for security"
                            : "Domain is allowed"
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(errorResponse("Failed to check domain"));
        }
    }

    // ==========================================
    // GET BLOCKED DOMAINS
    // GET /api/reports/blocked-domains
    // ==========================================
    @GetMapping("/blocked-domains")
    public ResponseEntity<?> getBlockedDomains(
            @RequestHeader("Authorization") String authHeader) {
        try {
            verifyAdminRole(authHeader);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("domains", intelligentReportService.getBlockedDomains());
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Failed to fetch blocked domains"));
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
    // HELPER - BUILD REPORT RESPONSE MAP
    // ==========================================
    private Map<String, Object> buildReportResponse(
            IncidentReport report) {

        Map<String, Object> map = new HashMap<>();
        map.put("id", report.getId());
        map.put("title", report.getTitle());
        map.put("description", report.getDescription());
        map.put("type", report.getType());
        map.put("textContent", report.getTextContent());
        map.put("inputContent",
                formatInputContent(report));
        map.put("incidentType", report.getIncidentType());
        map.put("suspiciousUrl", report.getSuspiciousUrl());
        map.put("suspiciousEmail", report.getSuspiciousEmail());
        map.put("imagePath", report.getImagePath());
        map.put("documentPath", report.getDocumentPath());
        map.put("prediction", report.getPrediction());
        map.put("mlScore", report.getMlScore());
        map.put("aiScore", report.getAiScore());
        map.put("finalScore", report.getFinalScore());
        map.put("confidence", report.getConfidence());
        map.put("riskScore", report.getRiskScore());
        map.put("riskLevel", report.getRiskLevel());
        map.put("aiAnalysis", report.getAiAnalysis());
        map.put("currentSpread", report.getCurrentSpread());
        map.put("predictedSpread", report.getPredictedSpread());
        map.put("alertStatus", report.getAlertStatus());
        map.put("verificationStatus", report.getVerificationStatus());
        map.put("adminStatus", report.getVerificationStatus());
        map.put("adminDecision", report.getAdminDecision());
        map.put("reviewTime", report.getReviewTime());
        map.put("status", report.getStatus());
        map.put("adminRemarks", report.getAdminRemarks());
        map.put("createdAt", report.getCreatedAt());
        map.put("updatedAt", report.getUpdatedAt());
        map.put("verifiedAt", report.getVerifiedAt());
        map.put("riskBreakdown", buildRiskBreakdown(report));
        map.put("investigationTimeline", buildInvestigationTimeline(report));

        // Reporter info
        if (report.getReportedBy() != null) {
            Map<String, Object> reporter = new HashMap<>();
            reporter.put("id",
                    report.getReportedBy().getId());
            reporter.put("fullName",
                    report.getReportedBy().getFullName());
            reporter.put("email",
                    report.getReportedBy().getEmail());
            reporter.put("institutionName",
                    report.getReportedBy().getInstitutionName());
            map.put("reportedBy", reporter);
        }

        // Verifier info
        if (report.getVerifiedBy() != null) {
            Map<String, Object> verifier = new HashMap<>();
            verifier.put("id",
                    report.getVerifiedBy().getId());
            verifier.put("fullName",
                    report.getVerifiedBy().getFullName());
            verifier.put("email",
                    report.getVerifiedBy().getEmail());
            map.put("verifiedBy", verifier);
        }

        return map;
    }

    private Map<String, Object> buildRiskBreakdown(IncidentReport report) {
        Map<String, Object> breakdown = new HashMap<>();
        double ml = report.getMlScore() == null ? 0.0 : report.getMlScore();
        double ai = report.getAiScore() == null ? 0.0 : report.getAiScore();
        double weightedMl = ml * 0.7;
        double weightedAi = ai * 0.3;
        breakdown.put("mlScore", ml);
        breakdown.put("aiScore", ai);
        breakdown.put("indicatorWeights", Map.of("mlWeight", 0.7, "aiWeight", 0.3));
        breakdown.put("weightedMl", Math.round(weightedMl * 100.0) / 100.0);
        breakdown.put("weightedAi", Math.round(weightedAi * 100.0) / 100.0);
        breakdown.put(
                "finalScore",
                report.getFinalScore() == null
                        ? report.getRiskScore()
                        : report.getFinalScore()
        );
        return breakdown;
    }

    private List<Map<String, Object>> buildInvestigationTimeline(IncidentReport report) {
        LocalDateTime base = report.getCreatedAt() == null
                ? LocalDateTime.now()
                : report.getCreatedAt();
        List<Map<String, Object>> timeline = new java.util.ArrayList<>();
        timeline.add(step("Input received", base, "Submission captured"));
        timeline.add(step("Text extraction", base.plusSeconds(1), "Input normalized by type"));
        timeline.add(step("ML prediction", base.plusSeconds(2), "ML confidence: " + (report.getMlScore() == null ? 0 : Math.round(report.getMlScore()))));
        timeline.add(step("AI indicators", base.plusSeconds(3), "AI score: " + (report.getAiScore() == null ? 0 : Math.round(report.getAiScore()))));
        timeline.add(step("Hybrid score", base.plusSeconds(4), "Final score: " + (report.getFinalScore() == null ? report.getRiskScore() : Math.round(report.getFinalScore()))));
        timeline.add(step("Risk assigned", base.plusSeconds(5), "Risk level: " + report.getRiskLevel()));
        timeline.add(step("Alert generated", base.plusSeconds(6), "Alert status: " + report.getAlertStatus()));
        return timeline;
    }

    private Map<String, Object> step(String label, LocalDateTime time, String detail) {
        Map<String, Object> item = new HashMap<>();
        item.put("step", label);
        item.put("timestamp", time);
        item.put("detail", detail);
        return item;
    }

    private String formatInputContent(IncidentReport report) {
        if (report == null) return "N/A";

        String type = report.getType() == null
                ? ""
                : report.getType().trim().toUpperCase();

        if ("URL".equals(type)) {
            return cleanAndLimit(report.getSuspiciousUrl(), 220);
        }
        if ("EMAIL".equals(type)) {
            String sender = cleanAndLimit(report.getSuspiciousEmail(), 100);
            String subject = extractToken(report.getTextContent(), "Subject:");
            if (subject == null || subject.isBlank()) {
                subject = "N/A";
            }
            return "Sender: " + sender + " | Subject: " + cleanAndLimit(subject, 120);
        }
        if ("DOCUMENT".equals(type)) {
            String extracted = cleanNarrative(
                    extractToken(report.getTextContent(), "Extracted Text:")
            );
            return "Document Link: " + cleanAndLimit(report.getDocumentPath(), 180)
                    + " | Reported By: " + cleanAndLimit(
                    report.getReportedBy() != null
                            ? report.getReportedBy().getEmail()
                            : null,
                    120
            )
                    + " | Reported Time: " + (
                    report.getCreatedAt() == null
                            ? "N/A"
                            : report.getCreatedAt().toString()
            )
                    + " | Attack Details: " + cleanAndLimit(extracted, 220);
        }
        if ("IMAGE".equals(type)) {
            String file = extractFileName(report.getImagePath());
            return "Image: " + cleanAndLimit(file, 100) + " | OCR Text: "
                    + cleanAndLimit(report.getTextContent(), 220);
        }

        if (report.getSuspiciousUrl() != null && !report.getSuspiciousUrl().isBlank()) {
            return cleanAndLimit(report.getSuspiciousUrl(), 220);
        }
        if (report.getTextContent() != null && !report.getTextContent().isBlank()) {
            return cleanAndLimit(report.getTextContent(), 220);
        }
        return cleanAndLimit(report.getDescription(), 220);
    }

    private String extractToken(String text, String key) {
        if (text == null || key == null) return null;
        String[] parts = text.split("\\|");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.toLowerCase().startsWith(key.toLowerCase())) {
                return trimmed.substring(key.length()).trim();
            }
        }
        return null;
    }

    private String extractFileName(String filePath) {
        if (filePath == null || filePath.isBlank()) return "N/A";
        int idx = filePath.lastIndexOf('/');
        if (idx >= 0 && idx < filePath.length() - 1) {
            return filePath.substring(idx + 1);
        }
        return filePath;
    }

    private String cleanAndLimit(String value, int maxLen) {
        if (value == null || value.isBlank()) return "N/A";
        String cleaned = value
                .replace("\n", " ")
                .replace("\r", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (cleaned.length() <= maxLen) return cleaned;
        return cleaned.substring(0, maxLen) + "...";
    }

    private String cleanNarrative(String value) {
        if (value == null || value.isBlank()) return "N/A";
        return value
                .replaceAll("(?i)=+\\s*ATTACK\\s*NARRATIVE\\s*=+", "")
                .replaceAll("(?i)=+\\s*END\\s*=+", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String extractDomain(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        try {
            String candidate = value.trim();
            if (!candidate.matches("^[a-zA-Z][a-zA-Z0-9+.-]*://.*")) {
                candidate = "https://" + candidate;
            }
            URI uri = URI.create(candidate);
            String host = uri.getHost();
            return host == null ? "" : host.toLowerCase();
        } catch (Exception ex) {
            return value.trim().toLowerCase();
        }
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
