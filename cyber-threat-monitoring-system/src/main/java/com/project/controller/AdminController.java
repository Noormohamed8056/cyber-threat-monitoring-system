package com.project.controller;

import com.project.entity.ThreatHistory;
import com.project.entity.User;
import com.project.security.JwtUtil;
import com.project.service.AdminAnalyticsService;
import com.project.service.AlertService;
import com.project.service.ReportService;
import com.project.service.ThreatHistoryService;
import com.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final ReportService reportService;
    private final AlertService alertService;
    private final AdminAnalyticsService adminAnalyticsService;
    private final ThreatHistoryService threatHistoryService;
    private final JwtUtil jwtUtil;

    // ==========================================
    // GET FULL DASHBOARD STATS
    // GET /api/admin/dashboard
    // ==========================================
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(
            @RequestHeader("Authorization") String authHeader) {
        try {

            // Verify admin
            verifyAdminRole(authHeader);

            // Collect all stats
            UserService.UserStats userStats =
                    userService.getDashboardStats();
            ReportService.ReportStats reportStats =
                    reportService.getDashboardStats();
            AlertService.AlertStats alertStats =
                    alertService.getDashboardStats();
            ThreatHistoryService.HistoryStats historyStats =
                    threatHistoryService.getDashboardStats();

            // Get recent activity feed (last 7 days)
            List<ThreatHistory> recentActivity =
                    threatHistoryService.getRecentActivityFeed(7);

            // Build dashboard response
            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("userStats", userStats);
            dashboard.put("reportStats", reportStats);
            dashboard.put("alertStats", alertStats);
            dashboard.put("historyStats", historyStats);
            dashboard.put("recentActivity",
                    recentActivity.stream()
                            .limit(10)
                            .map(this::buildHistoryResponse)
                            .collect(Collectors.toList()));

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("dashboard", dashboard);

            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse(
                            "Failed to fetch dashboard data"));
        }
    }

    // ==========================================
    // GET GRAPH DATA FOR ANALYTICS PAGES
    // GET /api/admin/analytics/graph-data
    // ==========================================
    @GetMapping("/analytics/graph-data")
    public ResponseEntity<?> getAnalyticsGraphData(
            @RequestHeader("Authorization") String authHeader) {
        try {

            // Verify admin
            verifyAdminRole(authHeader);

            AdminAnalyticsService.GraphData graphData =
                    adminAnalyticsService.getGraphData();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", graphData);

            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse(
                            "Failed to fetch analytics graph data"));
        }
    }

    // ==========================================
    // GET ADMIN DASHBOARD GRAPH STATS
    // GET /api/admin/dashboard/graph-stats
    // ==========================================
    @GetMapping("/dashboard/graph-stats")
    public ResponseEntity<?> getDashboardGraphStats(
            @RequestHeader("Authorization") String authHeader) {
        try {

            // Verify admin
            verifyAdminRole(authHeader);

            // Aggregated chart stats from incident reports
            ReportService.GraphStats graphStats =
                    reportService.getGraphStats();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("graphStats", graphStats);

            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse(
                            "Failed to fetch graph stats"));
        }
    }

    // ==========================================
    // GET ALL USERS (ADMIN)
    // GET /api/admin/users
    // ==========================================
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(
            @RequestHeader("Authorization") String authHeader) {
        try {

            // Verify admin
            verifyAdminRole(authHeader);

            // Get all users
            List<User> users = userService.getAllUsers();

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", users.size());
            response.put("users", users.stream()
                    .map(this::buildUserResponse)
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
                            "Failed to fetch users"));
        }
    }

    // ==========================================
    // GET ALL STUDENTS (ADMIN)
    // GET /api/admin/users/students
    // ==========================================
    @GetMapping("/users/students")
    public ResponseEntity<?> getAllStudents(
            @RequestHeader("Authorization") String authHeader) {
        try {

            // Verify admin
            verifyAdminRole(authHeader);

            // Get all students
            List<User> students = userService.getAllStudents();

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", students.size());
            response.put("students", students.stream()
                    .map(this::buildUserResponse)
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
                            "Failed to fetch students"));
        }
    }

    // ==========================================
    // GET USER BY ID (ADMIN)
    // GET /api/admin/users/{id}
    // ==========================================
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        try {

            // Verify admin
            verifyAdminRole(authHeader);

            // Get user
            User user = userService.findById(id);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", buildUserResponse(user));

            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(errorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse(
                            "Failed to fetch user"));
        }
    }

    // ==========================================
    // SEARCH USERS (ADMIN)
    // GET /api/admin/users/search?keyword=john
    // ==========================================
    @GetMapping("/users/search")
    public ResponseEntity<?> searchUsers(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String keyword) {
        try {

            // Verify admin
            verifyAdminRole(authHeader);

            // Search users
            List<User> users = userService.searchUsers(keyword);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("keyword", keyword);
            response.put("count", users.size());
            response.put("users", users.stream()
                    .map(this::buildUserResponse)
                    .collect(Collectors.toList()));

            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Search failed"));
        }
    }

    // ==========================================
    // DEACTIVATE USER (ADMIN)
    // PUT /api/admin/users/{id}/deactivate
    // ==========================================
    @PutMapping("/users/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        try {

            // Extract admin user
            User admin = extractUserFromToken(authHeader);
            verifyAdminRole(authHeader);

            // Deactivate user
            User deactivatedUser = userService
                    .deactivateUser(id, admin);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message",
                    "User deactivated successfully");
            response.put("user",
                    buildUserResponse(deactivatedUser));

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
                            "Failed to deactivate user"));
        }
    }

    // ==========================================
    // REACTIVATE USER (ADMIN)
    // PUT /api/admin/users/{id}/reactivate
    // ==========================================
    @PutMapping("/users/{id}/reactivate")
    public ResponseEntity<?> reactivateUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        try {

            // Verify admin
            verifyAdminRole(authHeader);

            // Reactivate user
            User reactivatedUser = userService.reactivateUser(id);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message",
                    "User reactivated successfully");
            response.put("user",
                    buildUserResponse(reactivatedUser));

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
                            "Failed to reactivate user"));
        }
    }

    // ==========================================
    // GET ALL THREAT HISTORY (ADMIN)
    // GET /api/admin/history
    // ==========================================
    @GetMapping("/history")
    public ResponseEntity<?> getAllHistory(
            @RequestHeader("Authorization") String authHeader) {
        try {

            // Verify admin
            verifyAdminRole(authHeader);

            // Get all history
            List<ThreatHistory> history = threatHistoryService
                    .getAllHistory();

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", history.size());
            response.put("history", history.stream()
                    .map(this::buildHistoryResponse)
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
                            "Failed to fetch history"));
        }
    }

    // ==========================================
    // GET INCIDENT TIMELINE (ADMIN)
    // GET /api/admin/history/incident/{incidentId}
    // ==========================================
    @GetMapping("/history/incident/{incidentId}")
    public ResponseEntity<?> getIncidentTimeline(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long incidentId) {
        try {

            // Verify admin
            verifyAdminRole(authHeader);

            // Get incident timeline
            List<ThreatHistory> timeline = threatHistoryService
                    .getIncidentTimeline(incidentId);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("incidentId", incidentId);
            response.put("count", timeline.size());
            response.put("timeline", timeline.stream()
                    .map(this::buildHistoryResponse)
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
                            "Failed to fetch incident timeline"));
        }
    }

    // ==========================================
    // GET ADMIN ACTIONS AUDIT LOG
    // GET /api/admin/history/admin-actions
    // ==========================================
    @GetMapping("/history/admin-actions")
    public ResponseEntity<?> getAdminActionsLog(
            @RequestHeader("Authorization") String authHeader) {
        try {

            // Verify admin
            verifyAdminRole(authHeader);

            // Get admin actions
            List<ThreatHistory> actions = threatHistoryService
                    .getAllAdminActions();

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", actions.size());
            response.put("actions", actions.stream()
                    .map(this::buildHistoryResponse)
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
                            "Failed to fetch admin actions"));
        }
    }

    // ==========================================
    // GET RECENT ACTIVITY FEED
    // GET /api/admin/history/recent?days=7
    // ==========================================
    @GetMapping("/history/recent")
    public ResponseEntity<?> getRecentActivity(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "7") int days) {
        try {

            // Verify admin
            verifyAdminRole(authHeader);

            // Get recent activity
            List<ThreatHistory> activity = threatHistoryService
                    .getRecentActivityFeed(days);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("days", days);
            response.put("count", activity.size());
            response.put("activity", activity.stream()
                    .map(this::buildHistoryResponse)
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
                            "Failed to fetch recent activity"));
        }
    }

    // ==========================================
    // GET ACTIVITY SUMMARY (ADMIN)
    // GET /api/admin/history/summary
    // ==========================================
    @GetMapping("/history/summary")
    public ResponseEntity<?> getActivitySummary(
            @RequestHeader("Authorization") String authHeader) {
        try {

            // Verify admin
            verifyAdminRole(authHeader);

            // Get activity summary
            Map<String, Long> summary = threatHistoryService
                    .getActivitySummary();

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("summary", summary);

            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse(
                            "Failed to fetch activity summary"));
        }
    }

    // ==========================================
    // GET HIGH RISK HISTORY (ADMIN)
    // GET /api/admin/history/high-risk
    // ==========================================
    @GetMapping("/history/high-risk")
    public ResponseEntity<?> getHighRiskHistory(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "70") int minScore) {
        try {

            // Verify admin
            verifyAdminRole(authHeader);

            // Get high risk history
            List<ThreatHistory> history = threatHistoryService
                    .getHighRiskHistory(minScore);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("minScore", minScore);
            response.put("count", history.size());
            response.put("history", history.stream()
                    .map(this::buildHistoryResponse)
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
                            "Failed to fetch high risk history"));
        }
    }

    // ==========================================
    // GET USERS BY INSTITUTION (ADMIN)
    // GET /api/admin/users/institution?name=MIT
    // ==========================================
    @GetMapping("/users/institution")
    public ResponseEntity<?> getUsersByInstitution(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String name) {
        try {

            // Verify admin
            verifyAdminRole(authHeader);

            // Get users by institution
            List<User> users = userService
                    .getUsersByInstitution(name);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("institution", name);
            response.put("count", users.size());
            response.put("users", users.stream()
                    .map(this::buildUserResponse)
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
                            "Failed to fetch users by institution"));
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
    // HELPER - BUILD USER RESPONSE MAP
    // ==========================================
    private Map<String, Object> buildUserResponse(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("fullName", user.getFullName());
        map.put("email", user.getEmail());
        map.put("role", user.getRole().name());
        map.put("institutionName", user.getInstitutionName());
        map.put("isActive", user.isActive());
        map.put("createdAt", user.getCreatedAt());
        map.put("updatedAt", user.getUpdatedAt());
        return map;
    }

    // ==========================================
    // HELPER - BUILD HISTORY RESPONSE MAP
    // ==========================================
    private Map<String, Object> buildHistoryResponse(
            ThreatHistory history) {

        Map<String, Object> map = new HashMap<>();
        map.put("id", history.getId());
        map.put("actionType", history.getActionType());
        map.put("actionDescription",
                history.getActionDescription());
        map.put("previousStatus", history.getPreviousStatus());
        map.put("newStatus", history.getNewStatus());
        map.put("riskLevelAtTime", history.getRiskLevelAtTime());
        map.put("riskScoreAtTime", history.getRiskScoreAtTime());
        map.put("ipAddress", history.getIpAddress());
        map.put("remarks", history.getRemarks());
        map.put("createdAt", history.getCreatedAt());

        // Performed by info
        if (history.getPerformedBy() != null) {
            Map<String, Object> performer = new HashMap<>();
            performer.put("id",
                    history.getPerformedBy().getId());
            performer.put("fullName",
                    history.getPerformedBy().getFullName());
            performer.put("email",
                    history.getPerformedBy().getEmail());
            performer.put("role",
                    history.getPerformedBy().getRole().name());
            map.put("performedBy", performer);
        }

        // Incident report info
        if (history.getIncidentReport() != null) {
            Map<String, Object> incident = new HashMap<>();
            incident.put("id",
                    history.getIncidentReport().getId());
            incident.put("title",
                    history.getIncidentReport().getTitle());
            incident.put("riskLevel",
                    history.getIncidentReport().getRiskLevel());
            map.put("incidentReport", incident);
        }

        // Cyber alert info
        if (history.getCyberAlert() != null) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("id",
                    history.getCyberAlert().getId());
            alert.put("title",
                    history.getCyberAlert().getTitle());
            alert.put("severity",
                    history.getCyberAlert().getSeverity());
            map.put("cyberAlert", alert);
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
