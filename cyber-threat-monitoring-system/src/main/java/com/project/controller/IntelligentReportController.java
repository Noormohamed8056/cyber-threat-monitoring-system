package com.project.controller;

import com.project.entity.User;
import com.project.digitaltwin.service.IntelligentReportService;
import com.project.security.JwtUtil;
import com.project.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

// ==========================================
// INTELLIGENT REPORT CONTROLLER
// Extends existing report system with
// multi-input AI-powered analysis
// All endpoints under /api/reports/intelligent
// ==========================================
@Slf4j
@RestController
@RequestMapping("/api/reports/intelligent")
@RequiredArgsConstructor
public class IntelligentReportController {

    private final IntelligentReportService
            intelligentReportService;
    private final JwtUtil     jwtUtil;
    private final UserService userService;

    // ==========================================
    // POST: Submit URL Report
    // /api/reports/intelligent/url
    // ==========================================
    @PostMapping("/url")
    public ResponseEntity<?> submitUrlReport(
            @RequestHeader("Authorization")
            String authHeader,
            @RequestBody
            Map<String, String> request) {

        try {
            User reporter = extractUser(authHeader);

            String url = request.get(
                    "suspiciousUrl"
            );
            if (url == null || url.isBlank()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "success", false,
                                "message",
                                "Suspicious URL is required"
                        ));
            }

            Map<String, Object> result =
                    intelligentReportService
                            .submitIntelligentUrlReport(
                                    reporter,
                                    request.getOrDefault(
                                            "title",
                                            "Suspicious URL Report"
                                    ),
                                    request.getOrDefault(
                                            "description", ""
                                    ),
                                    url,
                                    request.getOrDefault(
                                            "incidentType",
                                            "PHISHING"
                                    )
                            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error(
                    "URL report error: {}",
                    e.getMessage()
            );
            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    // ==========================================
    // POST: Submit Email Report
    // /api/reports/intelligent/email
    // ==========================================
    @PostMapping("/email")
    public ResponseEntity<?> submitEmailReport(
            @RequestHeader("Authorization")
            String authHeader,
            @RequestBody
            Map<String, String> request) {

        try {
            User reporter = extractUser(authHeader);

            String body = request.get("emailBody");
            if (body == null || body.isBlank()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "success", false,
                                "message",
                                "Email body is required"
                        ));
            }

            Map<String, Object> result =
                    intelligentReportService
                            .submitIntelligentEmailReport(
                                    reporter,
                                    request.getOrDefault(
                                            "title",
                                            "Suspicious Email Report"
                                    ),
                                    request.get("emailSubject"),
                                    body,
                                    request.get("emailSender"),
                                    request.getOrDefault(
                                            "description", ""
                                    )
                            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error(
                    "Email report error: {}",
                    e.getMessage()
            );
            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    // ==========================================
    // POST: Submit Document Report
    // /api/reports/intelligent/document
    // ==========================================
    @PostMapping(
            value    = "/document",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> submitDocumentReport(
            @RequestHeader("Authorization")
            String authHeader,
            @RequestPart("file")
            MultipartFile file,
            @RequestPart(
                    value    = "title",
                    required = false
            )
            String title,
            @RequestPart(
                    value    = "description",
                    required = false
            )
            String description) {

        try {
            User reporter = extractUser(authHeader);

            if (file == null || file.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "success", false,
                                "message",
                                "Document file is required"
                        ));
            }

            Map<String, Object> result =
                    intelligentReportService
                            .submitIntelligentDocumentReport(
                                    reporter,
                                    title != null
                                            ? title
                                            : "Suspicious Document: "
                                            + file
                                            .getOriginalFilename(),
                                    description != null
                                            ? description
                                            : "",
                                    file
                            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error(
                    "Document report error: {}",
                    e.getMessage()
            );
            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    // ==========================================
    // POST: Submit Image Report
    // /api/reports/intelligent/image
    // ==========================================
    @PostMapping(
            value    = "/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> submitImageReport(
            @RequestHeader("Authorization")
            String authHeader,
            @RequestPart("image")
            MultipartFile image,
            @RequestPart(
                    value    = "title",
                    required = false
            )
            String title,
            @RequestPart(
                    value    = "description",
                    required = false
            )
            String description) {

        try {
            User reporter = extractUser(authHeader);

            if (image == null || image.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "success", false,
                                "message",
                                "Image file is required"
                        ));
            }

            Map<String, Object> result =
                    intelligentReportService
                            .submitIntelligentImageReport(
                                    reporter,
                                    title != null
                                            ? title
                                            : "Suspicious Screenshot: "
                                            + image
                                            .getOriginalFilename(),
                                    description != null
                                            ? description
                                            : "",
                                    image
                            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error(
                    "Image report error: {}",
                    e.getMessage()
            );
            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    // ==========================================
    // PUT: Re-Analyze Existing Report
    // /api/reports/intelligent/{id}/reanalyze
    // ==========================================
    @PutMapping("/{id}/reanalyze")
    public ResponseEntity<?> reAnalyzeReport(
            @RequestHeader("Authorization")
            String authHeader,
            @PathVariable Long id) {

        try {
            User admin = extractUser(authHeader);

            Map<String, Object> result =
                    intelligentReportService
                            .reAnalyzeReport(id, admin);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error(
                    "Re-analysis error: {}",
                    e.getMessage()
            );
            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    // ==========================================
    // GET: Intelligence Summary for Report
    // /api/reports/intelligent/{id}/summary
    // ==========================================
    @GetMapping("/{id}/summary")
    public ResponseEntity<?> getIntelSummary(
            @RequestHeader("Authorization")
            String authHeader,
            @PathVariable Long id) {

        try {
            extractUser(authHeader);
            Map<String, Object> summary =
                    intelligentReportService
                            .getIntelligenceSummary(id);
            return ResponseEntity.ok(summary);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    // ==========================================
    // HELPER: EXTRACT USER FROM TOKEN
    // ==========================================
    private User extractUser(String authHeader) {
        String token   =
                authHeader.substring(7);
        Long   userId  =
                jwtUtil.extractUserId(token);
        return userService.findById(userId);
    }
}
