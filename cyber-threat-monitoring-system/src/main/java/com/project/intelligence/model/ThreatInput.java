package com.project.intelligence.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// ==========================================
// THREAT INPUT — COMPLETE MODEL
// Unified input model for all 4 types:
// URL / Email / Document / Image
// ==========================================
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreatInput {

    // ==========================================
    // INPUT TYPE ENUM
    // ==========================================
    public enum InputType {
        URL,
        EMAIL_CONTENT,
        PDF_DOCUMENT,
        IMAGE_SCREENSHOT,
        MIXED
    }

    // ==========================================
    // SECTION 1: COMMON FIELDS
    // ==========================================
    // FIX: Added inputId, receivedAt, ipAddress —
    // all called on the builder in IntelligenceController
    // but missing from the original model, causing:
    // "cannot find symbol: method inputId(String)"
    private String        inputId;
    private LocalDateTime receivedAt;
    private String        ipAddress;

    private InputType inputType;
    private Long      userId;
    private String    userEmail;
    private String    institutionName;
    private String    reportTitle;
    private String    reportDescription;
    private String    combinedAnalysisText;

    // ==========================================
    // SECTION 2: URL INPUT
    // ==========================================
    private String rawUrl;
    private String normalizedUrl;
    private String urlDomain;
    private String urlPath;
    private String urlProtocol;
    private String urlTld;
    private int    urlLength;

    // ==========================================
    // SECTION 3: EMAIL INPUT
    // ==========================================
    private String emailSubject;
    private String emailBody;
    private String emailSender;
    private String emailRecipient;
    private String emailHeaders;
    private String emailBodyHtml;

    // ==========================================
    // SECTION 4: DOCUMENT INPUT
    // ==========================================
    private String  fileName;
    private String  fileExtension;
    // FIX: Added fileType and fileBytes —
    // used in analyzeDocument() and analyzeImage()
    // builder calls but absent from original model
    private String  fileType;
    private byte[]  fileBytes;
    private String  fileMimeType;
    // FIX: long -> Long so builder accepts null;
    // MultipartFile.getSize() returns long but
    // builder field must be nullable wrapper type
    private Long    fileSizeBytes;
    private String  extractedText;
    private int     pageCount;
    private String  author;
    private String  creationDate;
    private boolean hasMacros;
    private boolean isEncrypted;
    private boolean isPasswordProtected;

    // ==========================================
    // SECTION 5: IMAGE INPUT
    // ==========================================
    private String imageName;
    private String imageFormat;
    private int    imageWidth;
    private int    imageHeight;
    // FIX: long -> Long (same reason as fileSizeBytes)
    private Long   imageSizeBytes;
    private byte[] imageBytes;
    private String extractedImageText;
    private String ocrLanguage;

    // ==========================================
    // HELPER METHODS
    // ==========================================

    public boolean isUrl() {
        return inputType == InputType.URL;
    }

    public boolean isEmail() {
        return inputType == InputType.EMAIL_CONTENT;
    }

    public boolean isDocument() {
        return inputType == InputType.PDF_DOCUMENT;
    }

    public boolean isImage() {
        return inputType == InputType.IMAGE_SCREENSHOT;
    }

    public String getPrimaryContent() {
        if (rawUrl != null)             return rawUrl;
        if (emailBody != null)          return emailBody;
        if (extractedText != null)      return extractedText;
        if (extractedImageText != null) return extractedImageText;
        return combinedAnalysisText;
    }

    public boolean hasContent() {
        return getPrimaryContent() != null
                && !getPrimaryContent().isEmpty();
    }
}