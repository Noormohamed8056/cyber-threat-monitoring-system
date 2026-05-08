package com.project.intelligence.engine;

import com.project.intelligence.model.AnalysisResult;
import com.project.intelligence.model.ThreatInput;
import com.project.intelligence.pipeline.IntelligencePipeline;
import com.project.security.JwtUtil;
import com.project.service.UserService;
import com.project.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

// ==========================================
// INTELLIGENCE CONTROLLER
// Unified REST API for all threat inputs:
// URL, Email, Document, Image
// ==========================================
@Slf4j
@RestController
@RequestMapping("/api/intelligence")
@RequiredArgsConstructor
public class IntelligenceController {

    private final IntelligencePipeline pipeline;
    private final JwtUtil              jwtUtil;
    private final UserService          userService;

    // ==========================================
    // ENDPOINT 1: ANALYZE URL
    // POST /api/intelligence/analyze/url
    // ==========================================
    @PostMapping("/analyze/url")
    public ResponseEntity<?> analyzeUrl(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {

        try {
            User currentUser = extractUser(authHeader);

            String url = request.get("url");
            if (url == null || url.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(errorResponse("URL is required"));
            }

            log.info("URL analysis request from: {}", currentUser.getEmail());

            ThreatInput input = ThreatInput.builder()
                    // FIX: ThreatInput must have @Builder on the class.
                    // If inputId() still fails, rename field from
                    // "inputId" to something without ambiguity, or
                    // confirm @Builder is present on ThreatInput.
                    .inputId(generateInputId())
                    .inputType(ThreatInput.InputType.URL)
                    .receivedAt(LocalDateTime.now())
                    .userId(currentUser.getId())
                    .userEmail(currentUser.getEmail())
                    .institutionName(currentUser.getInstitutionName())
                    .ipAddress(request.get("ipAddress"))
                    .rawUrl(url)
                    .build();

            AnalysisResult result = pipeline.process(input);

            return ResponseEntity.ok(buildSuccessResponse(result, "URL"));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("URL analysis error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Analysis failed: " + e.getMessage()));
        }
    }

    // ==========================================
    // ENDPOINT 2: ANALYZE EMAIL CONTENT
    // POST /api/intelligence/analyze/email
    // ==========================================
    @PostMapping("/analyze/email")
    public ResponseEntity<?> analyzeEmail(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {

        try {
            User currentUser = extractUser(authHeader);

            String subject = request.get("emailSubject");
            String body    = request.get("emailBody");
            String sender  = request.get("emailSender");

            if ((body == null || body.isBlank())
                    && (subject == null || subject.isBlank())) {
                return ResponseEntity.badRequest()
                        .body(errorResponse("Email subject or body is required"));
            }

            log.info("Email analysis request from: {}", currentUser.getEmail());

            ThreatInput input = ThreatInput.builder()
                    .inputId(generateInputId())
                    .inputType(ThreatInput.InputType.EMAIL_CONTENT)
                    .receivedAt(LocalDateTime.now())
                    .userId(currentUser.getId())
                    .userEmail(currentUser.getEmail())
                    .institutionName(currentUser.getInstitutionName())
                    .emailSubject(subject)
                    .emailBody(body)
                    .emailSender(sender)
                    .build();

            AnalysisResult result = pipeline.process(input);

            return ResponseEntity.ok(buildSuccessResponse(result, "EMAIL"));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Email analysis error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Analysis failed: " + e.getMessage()));
        }
    }

    // ==========================================
    // ENDPOINT 3: ANALYZE DOCUMENT
    // POST /api/intelligence/analyze/document
    // ==========================================
    @PostMapping(
            value    = "/analyze/document",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> analyzeDocument(
            @RequestHeader("Authorization") String authHeader,
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "description", required = false) String description) {

        try {
            User currentUser = extractUser(authHeader);

            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(errorResponse("Document file is required"));
            }

            String contentType = file.getContentType();
            if (!isValidDocumentType(contentType)) {
                return ResponseEntity.badRequest()
                        .body(errorResponse(
                                "Invalid document type. Supported: PDF, DOC, DOCX, XLS, XLSX, TXT"
                        ));
            }

            log.info("Document analysis: {} | Size: {} bytes | User: {}",
                    file.getOriginalFilename(), file.getSize(), currentUser.getEmail());

            ThreatInput input = ThreatInput.builder()
                    .inputId(generateInputId())
                    .inputType(ThreatInput.InputType.PDF_DOCUMENT)
                    .receivedAt(LocalDateTime.now())
                    .userId(currentUser.getId())
                    .userEmail(currentUser.getEmail())
                    .institutionName(currentUser.getInstitutionName())
                    .fileName(file.getOriginalFilename())
                    .fileType(contentType)
                    .fileSizeBytes(file.getSize())
                    .fileBytes(file.getBytes())
                    .build();

            AnalysisResult result = pipeline.process(input);

            return ResponseEntity.ok(buildSuccessResponse(result, "DOCUMENT"));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Document analysis error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Document analysis failed: " + e.getMessage()));
        }
    }

    // ==========================================
    // ENDPOINT 4: ANALYZE IMAGE
    // POST /api/intelligence/analyze/image
    // ==========================================
    @PostMapping(
            value    = "/analyze/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> analyzeImage(
            @RequestHeader("Authorization") String authHeader,
            @RequestPart("image") MultipartFile image,
            @RequestPart(value = "description", required = false) String description) {

        try {
            User currentUser = extractUser(authHeader);

            if (image == null || image.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(errorResponse("Image file is required"));
            }

            String contentType = image.getContentType();
            if (!isValidImageType(contentType)) {
                return ResponseEntity.badRequest()
                        .body(errorResponse(
                                "Invalid image type. Supported: JPG, PNG, GIF, BMP, WEBP"
                        ));
            }

            log.info("Image analysis: {} | Size: {} bytes | User: {}",
                    image.getOriginalFilename(), image.getSize(), currentUser.getEmail());

            ThreatInput input = ThreatInput.builder()
                    .inputId(generateInputId())
                    .inputType(ThreatInput.InputType.IMAGE_SCREENSHOT)
                    .receivedAt(LocalDateTime.now())
                    .userId(currentUser.getId())
                    .userEmail(currentUser.getEmail())
                    .institutionName(currentUser.getInstitutionName())
                    .imageName(image.getOriginalFilename())
                    .fileType(contentType)
                    .imageSizeBytes(image.getSize())
                    .imageBytes(image.getBytes())
                    .extractedImageText(description)
                    .build();

            AnalysisResult result = pipeline.process(input);

            return ResponseEntity.ok(buildSuccessResponse(result, "IMAGE"));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Image analysis error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Image analysis failed: " + e.getMessage()));
        }
    }

    // ==========================================
    // ENDPOINT 5: MIXED ANALYSIS
    // POST /api/intelligence/analyze/mixed
    // ==========================================
    @PostMapping(
            value    = "/analyze/mixed",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> analyzeMixed(
            @RequestHeader("Authorization") String authHeader,
            @RequestPart(value = "url",          required = false) String url,
            @RequestPart(value = "emailSubject", required = false) String emailSubject,
            @RequestPart(value = "emailBody",    required = false) String emailBody,
            @RequestPart(value = "emailSender",  required = false) String emailSender,
            @RequestPart(value = "file",         required = false) MultipartFile file) {

        try {
            User currentUser = extractUser(authHeader);

            boolean hasUrl   = url != null && !url.isBlank();
            boolean hasEmail = emailBody != null && !emailBody.isBlank();
            boolean hasFile  = file != null && !file.isEmpty();

            if (!hasUrl && !hasEmail && !hasFile) {
                return ResponseEntity.badRequest()
                        .body(errorResponse(
                                "At least one input required: URL, email, or file"
                        ));
            }

            log.info("Mixed analysis from: {} | URL:{} Email:{} File:{}",
                    currentUser.getEmail(), hasUrl, hasEmail, hasFile);

            ThreatInput.ThreatInputBuilder builder = ThreatInput.builder()
                    .inputId(generateInputId())
                    .inputType(ThreatInput.InputType.MIXED)
                    .receivedAt(LocalDateTime.now())
                    .userId(currentUser.getId())
                    .userEmail(currentUser.getEmail())
                    .institutionName(currentUser.getInstitutionName());

            if (hasUrl)   builder.rawUrl(url);
            if (hasEmail) builder.emailSubject(emailSubject)
                    .emailBody(emailBody)
                    .emailSender(emailSender);
            if (hasFile)  builder.fileName(file.getOriginalFilename())
                    .fileType(file.getContentType())
                    .fileSizeBytes(file.getSize())
                    .fileBytes(file.getBytes());

            AnalysisResult result = pipeline.process(builder.build());

            return ResponseEntity.ok(buildSuccessResponse(result, "MIXED"));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Mixed analysis error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Mixed analysis failed: " + e.getMessage()));
        }
    }

    // ==========================================
    // ENDPOINT 6: QUICK SCAN
    // POST /api/intelligence/quick-scan
    // ==========================================
    @PostMapping("/quick-scan")
    public ResponseEntity<?> quickScan(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {

        try {
            User currentUser = extractUser(authHeader);

            String content = request.get("content");
            String type    = request.get("type");

            if (content == null || content.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(errorResponse("Content is required"));
            }

            ThreatInput.InputType inputType =
                    autoDetectInputType(content, type);

            ThreatInput.ThreatInputBuilder builder = ThreatInput.builder()
                    .inputId(generateInputId())
                    .inputType(inputType)
                    .receivedAt(LocalDateTime.now())
                    .userId(currentUser.getId())
                    .userEmail(currentUser.getEmail())
                    .institutionName(currentUser.getInstitutionName());

            switch (inputType) {
                case URL:
                    builder.rawUrl(content);
                    break;
                case EMAIL_CONTENT:
                default:
                    builder.emailBody(content);
                    break;
            }

            AnalysisResult result = pipeline.process(builder.build());

            Map<String, Object> quickResult = new LinkedHashMap<>();
            quickResult.put("success",    true);
            quickResult.put("inputType",  inputType.name());
            quickResult.put("riskScore",  result.getRiskScore());
            quickResult.put("riskLevel",  result.getRiskLevel());
            quickResult.put("threatCategory",   result.getThreatCategory());
            quickResult.put("isSafe",           result.isSafe());
            quickResult.put("topRedFlag",
                    result.getRedFlags() != null && !result.getRedFlags().isEmpty()
                            ? result.getRedFlags().get(0) : "None");
            quickResult.put("topRecommendation",
                    result.getRecommendations() != null && !result.getRecommendations().isEmpty()
                            ? result.getRecommendations().get(0) : "No action needed");
            quickResult.put("processingTimeMs", result.getProcessingTimeMs());

            return ResponseEntity.ok(quickResult);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Quick scan failed: " + e.getMessage()));
        }
    }

    // ==========================================
    // ENDPOINT 7: GET SUPPORTED INPUT TYPES
    // GET /api/intelligence/input-types
    // ==========================================
    @GetMapping("/input-types")
    public ResponseEntity<?> getInputTypes() {

        List<Map<String, Object>> types = new ArrayList<>();

        types.add(buildTypeInfo("URL",
                "Analyze suspicious URLs and links",
                "POST /api/intelligence/analyze/url",
                Arrays.asList("Full URL analysis", "Domain reputation check",
                        "Brand impersonation detection", "Malware link detection")));

        types.add(buildTypeInfo("EMAIL",
                "Analyze suspicious email content",
                "POST /api/intelligence/analyze/email",
                Arrays.asList("Phishing detection", "Sender verification",
                        "Urgency/pressure analysis", "Credential harvesting detection")));

        types.add(buildTypeInfo("DOCUMENT",
                "Analyze PDF, Word, Excel files",
                "POST /api/intelligence/analyze/document",
                Arrays.asList("Macro detection", "Malicious keyword scan",
                        "Metadata analysis", "Sensitive data detection")));

        types.add(buildTypeInfo("IMAGE",
                "Analyze screenshots and images",
                "POST /api/intelligence/analyze/image",
                Arrays.asList("Fake login page detection", "Brand impersonation in images",
                        "Steganography detection", "Visual threat analysis")));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success",    true);
        response.put("inputTypes", types);
        response.put("totalTypes", types.size());

        return ResponseEntity.ok(response);
    }

    // ==========================================
    // ENDPOINT 8: PIPELINE HEALTH CHECK
    // GET /api/intelligence/health
    // ==========================================
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {

        Map<String, Object> health = new LinkedHashMap<>();
        health.put("status",    "OPERATIONAL");
        health.put("timestamp", LocalDateTime.now().toString());
        health.put("version",   "2.0.0");

        Map<String, String> components = new LinkedHashMap<>();
        components.put("urlAnalyzer",   "UP");
        components.put("emailAnalyzer", "UP");
        components.put("docAnalyzer",   "UP");
        components.put("imageAnalyzer", "UP");
        components.put("pipeline",      "UP");
        health.put("components", components);

        Map<String, Object> capabilities = new LinkedHashMap<>();
        capabilities.put("urlAnalysis",      true);
        capabilities.put("emailAnalysis",    true);
        capabilities.put("documentAnalysis", true);
        capabilities.put("imageAnalysis",    true);
        capabilities.put("mixedAnalysis",    true);
        capabilities.put("quickScan",        true);
        health.put("capabilities", capabilities);

        return ResponseEntity.ok(health);
    }

    // ==========================================
    // HELPERS
    // ==========================================

    private User extractUser(String authHeader) {
        String token  = authHeader.substring(7);
        Long   userId = jwtUtil.extractUserId(token);
        return userService.findById(userId);
    }

    private String generateInputId() {
        return "INP-" + System.currentTimeMillis()
                + "-" + (int)(Math.random() * 9000 + 1000);
    }

    private ThreatInput.InputType autoDetectInputType(
            String content, String typeHint) {

        if (typeHint != null) {
            try {
                return ThreatInput.InputType.valueOf(typeHint.toUpperCase());
            } catch (Exception ignored) {}
        }

        String contentLower = content.toLowerCase().trim();
        if (contentLower.startsWith("http://")
                || contentLower.startsWith("https://")
                || contentLower.startsWith("www.")) {
            return ThreatInput.InputType.URL;
        }

        return ThreatInput.InputType.EMAIL_CONTENT;
    }

    private boolean isValidDocumentType(String contentType) {
        if (contentType == null) return false;
        return contentType.contains("pdf")
                || contentType.contains("msword")
                || contentType.contains("wordprocessingml")
                || contentType.contains("excel")
                || contentType.contains("spreadsheetml")
                || contentType.contains("powerpoint")
                || contentType.contains("presentationml")
                || contentType.contains("text/plain")
                || contentType.contains("text/csv");
    }

    private boolean isValidImageType(String contentType) {
        if (contentType == null) return false;
        return contentType.startsWith("image/");
    }

    private Map<String, Object> buildSuccessResponse(
            AnalysisResult result, String inputType) {

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success",   true);
        response.put("inputType", inputType);

        Map<String, Object> analysis = new LinkedHashMap<>();
        analysis.put("resultId",             result.getResultId());
        analysis.put("riskScore",            result.getRiskScore());
        analysis.put("riskLevel",            result.getRiskLevel());
        analysis.put("confidenceScore",      result.getConfidenceScore());
        analysis.put("confidencePercentage", result.getConfidencePercentage());
        analysis.put("riskJustification",    result.getRiskJustification());

        // Threat info
        Map<String, Object> threat = new LinkedHashMap<>();
        threat.put("category",    result.getThreatCategory());
        threat.put("subCategory", result.getThreatSubCategory());
        threat.put("description", result.getThreatDescription());
        threat.put("isCritical",  result.isCriticalThreat());
        threat.put("isSafe",      result.isSafe());
        analysis.put("threat", threat);

        // Attack stage
        Map<String, Object> attackStage = new LinkedHashMap<>();
        attackStage.put("detectedStage",  result.getDetectedStage());
        attackStage.put("stageDescription", result.getStageDescription());
        // FIX: was result.isIsActiveAttack() — Lombok boolean field must be named
        // "activeAttack" (not "isActiveAttack") so the getter is isActiveAttack().
        // If AnalysisResult has field "isActiveAttack", rename it to "activeAttack".
        attackStage.put("isActiveAttack",   result.isActiveAttack());
        attackStage.put("stageProgression", result.getStageProgression());
        analysis.put("attackStage", attackStage);

        // Pattern matching
        Map<String, Object> pattern = new LinkedHashMap<>();
        pattern.put("matchedPattern", result.isMatchedKnownPattern());
        pattern.put("patternName",    result.getMatchedPatternName());
        pattern.put("similarity",     result.getPatternSimilarity());
        analysis.put("patternMatch", pattern);

        // Spread simulation
        Map<String, Object> spread = new LinkedHashMap<>();
        spread.put("spreadProbability",      result.getSpreadProbability());
        spread.put("estimatedAffectedUsers", result.getEstimatedAffectedUsers());
        spread.put("spreadDepth",            result.getSpreadDepth());
        spread.put("affectedInstitutions",   result.getAffectedInstitutions());
        analysis.put("spreadSimulation", spread);

        // Auto response
        Map<String, Object> autoResponse = new LinkedHashMap<>();
        autoResponse.put("triggered",      result.isAutoResponseTriggered());
        autoResponse.put("actions",        result.getAutoResponseActions());
        autoResponse.put("alertPublished", result.isAutoAlertPublished());
        analysis.put("autonomousResponse", autoResponse);

        // Evidence
        Map<String, Object> evidence = new LinkedHashMap<>();
        evidence.put("redFlags",         result.getRedFlags());
        evidence.put("extractedUrls",    result.getExtractedUrls());
        evidence.put("extractedKeywords",result.getExtractedKeywords());
        evidence.put("scoreBreakdown",   result.getScoreBreakdown());
        analysis.put("evidence", evidence);

        analysis.put("timeline",        result.getTimelineEvents());
        analysis.put("recommendations", result.getRecommendations());

        // Processing info
        Map<String, Object> processing = new LinkedHashMap<>();
        processing.put("stages",           result.getPipelineStagesCompleted());
        processing.put("processingTimeMs", result.getProcessingTimeMs());
        processing.put("success",          result.isProcessingSuccess());
        analysis.put("processing", processing);

        response.put("analysis", analysis);
        return response;
    }

    private Map<String, Object> buildTypeInfo(
            String type, String description,
            String endpoint, List<String> capabilities) {

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("type",         type);
        info.put("description",  description);
        info.put("endpoint",     endpoint);
        info.put("capabilities", capabilities);
        return info;
    }

    private Map<String, Object> errorResponse(String message) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("success", false);
        error.put("message", message);
        return error;
    }
}
