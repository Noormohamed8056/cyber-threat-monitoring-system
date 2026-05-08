package com.project.intelligence.analyzer;

import com.project.intelligence.model.AnalysisResult;
import com.project.intelligence.model.ThreatInput;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// ==========================================
// IMAGE / SCREENSHOT ANALYZER
// Analyzes phishing screenshots, fake
// login pages, visual deception tactics
// and extracts intelligence from images
// ==========================================
@Slf4j
@Component
public class ImageAnalyzer {

    // ==========================================
    // SUPPORTED IMAGE TYPES
    // ==========================================
    private static final List<String>
            SUPPORTED_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg",
            "image/png",  "image/gif",
            "image/bmp",  "image/webp",
            "image/tiff"
    );

    // ==========================================
    // PHISHING BRAND INDICATORS
    // ==========================================
    private static final List<String>
            PHISHING_BRAND_KEYWORDS = Arrays.asList(
            "paypal", "amazon", "apple",
            "microsoft", "google", "facebook",
            "instagram", "twitter", "linkedin",
            "netflix", "spotify", "ebay",
            "chase", "wellsfargo", "citibank",
            "bankofamerica", "bank of america",
            "american express", "visa",
            "mastercard", "discover",
            "dropbox", "adobe", "office365",
            "outlook", "onedrive", "icloud"
    );

    // ==========================================
    // FAKE LOGIN PAGE INDICATORS
    // ==========================================
    private static final List<String>
            LOGIN_PAGE_KEYWORDS = Arrays.asList(
            "sign in", "signin", "log in",
            "login", "username", "password",
            "email address", "enter your",
            "forgot password", "remember me",
            "create account", "new account",
            "verify identity", "confirm email",
            "two-factor", "2fa", "otp",
            "authentication", "security code"
    );

    // ==========================================
    // URGENCY VISUAL KEYWORDS
    // ==========================================
    private static final List<String>
            URGENCY_KEYWORDS = Arrays.asList(
            "urgent", "warning", "alert",
            "action required", "immediate",
            "expires", "limited time",
            "account suspended", "verify now",
            "confirm now", "update required",
            "security alert", "breach detected",
            "unusual activity", "locked",
            "suspended", "blocked", "restricted"
    );

    // ==========================================
    // FINANCIAL SCAM KEYWORDS
    // ==========================================
    private static final List<String>
            FINANCIAL_KEYWORDS = Arrays.asList(
            "payment", "invoice", "receipt",
            "transaction", "transfer",
            "refund", "claim", "reward",
            "prize", "winner", "lottery",
            "gift card", "bitcoin", "crypto",
            "wire transfer", "western union",
            "free money", "earn money"
    );

    // ==========================================
    // SUSPICIOUS URL PATTERNS IN TEXT
    // ==========================================
    private static final Pattern
            URL_PATTERN = Pattern.compile(
            "https?://[\\w\\-.]+(:\\d+)?"
                    + "(/[\\w\\-./?%&=#@]*)?",
            Pattern.CASE_INSENSITIVE
    );

    // ==========================================
    // EMAIL PATTERN
    // ==========================================
    private static final Pattern
            EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9._%+\\-]+"
                    + "@[a-zA-Z0-9.\\-]+"
                    + "\\.[a-zA-Z]{2,}",
            Pattern.CASE_INSENSITIVE
    );

    // ==========================================
    // PHONE PATTERN
    // ==========================================
    private static final Pattern
            PHONE_PATTERN = Pattern.compile(
            "(\\+?\\d{1,3}[\\s\\-]?)?"
                    + "(\\(?\\d{3}\\)?[\\s\\-]?)"
                    + "\\d{3}[\\s\\-]?\\d{4}"
    );

    // ==========================================
    // analyze(ThreatInput) overload
    // ==========================================
    public AnalysisResult analyze(ThreatInput input) {

        ImageAnalysisOutput out = analyze(
                input.getImageBytes(),
                input.getImageName(),
                input.getFileType(),
                input.getExtractedImageText()
        );

        return mapToAnalysisResult(out);
    }

    // ==========================================
    // MAIN ANALYZE METHOD
    // ==========================================
    public ImageAnalysisOutput analyze(
            byte[]  imageBytes,
            String  imageName,
            String  mimeType,
            String  extractedOcrText) {

        log.info(
                "Image Analyzer starting: {}",
                imageName
        );

        ImageAnalysisOutput output =
                new ImageAnalysisOutput();
        output.setImageName(imageName);
        output.setMimeType(mimeType);
        output.setImageSizeBytes(
                imageBytes != null
                        ? imageBytes.length
                        : 0
        );
        output.setAnalyzedAt(
                LocalDateTime.now()
        );

        try {
            // Step 1: Validate image
            validateImage(imageBytes, output);

            // Step 2: Analyze image properties
            analyzeImageProperties(
                    imageBytes, output
            );

            // Step 3: Analyze file header
            analyzeFileHeader(imageBytes, output);

            // Step 4: Detect image manipulation
            detectImageManipulation(
                    imageBytes, output
            );

            // Step 5: Analyze OCR text
            if (extractedOcrText != null
                    && !extractedOcrText
                    .isEmpty()) {
                output.setExtractedText(
                        extractedOcrText
                );
                analyzeExtractedText(output);
            } else {
                analyzeFilenameForThreats(
                        imageName, output
                );
            }

            // Step 6: Visual threat detection
            detectVisualThreatPatterns(output);

            // Step 7: Extract artifacts
            extractArtifactsFromText(output);

            // Step 8: Calculate image risk score
            calculateImageRiskScore(output);

            output.setAnalysisSuccess(true);
            log.info(
                    "Image analysis complete. "
                            + "Score: {} | File: {}",
                    output.getImageRiskScore(),
                    imageName
            );

        } catch (Exception e) {
            log.error(
                    "Image analysis error: {}",
                    e.getMessage()
            );
            output.setAnalysisSuccess(false);
            output.setErrorMessage(
                    e.getMessage()
            );
            output.setImageRiskScore(20);
        }

        return output;
    }

    // ==========================================
    // MAP OUTPUT TO AnalysisResult
    // ==========================================
    private AnalysisResult mapToAnalysisResult(
            ImageAnalysisOutput out) {

        AnalysisResult result = new AnalysisResult();
        result.setAnalyzedAt(out.getAnalyzedAt());
        result.setRiskScore(out.getImageRiskScore());
        result.setRiskLevel(
                toRiskLevel(out.getImageRiskScore())
        );
        result.setConfidenceScore(0.75);
        result.setConfidencePercentage("75%");

        if (out.getRedFlags() != null) {
            result.setRedFlags(out.getRedFlags());
        }
        if (out.getScoreBreakdown() != null) {
            result.setScoreBreakdown(
                    out.getScoreBreakdown()
            );
        }
        if (out.getExtractedUrls() != null) {
            result.setExtractedUrls(
                    out.getExtractedUrls()
            );
        }
        if (out.getExtractedEmails() != null) {
            result.setExtractedEmails(
                    out.getExtractedEmails()
            );
        }

        if ("PHISHING_LOGIN_PAGE".equals(
                out.getDetectedThreatType())) {
            result.setThreatCategory(
                    AnalysisResult.ThreatCategory.PHISHING
            );
            result.setThreatDescription(
                    "Fake login page detected "
                            + "in image screenshot"
            );
        } else if ("FINANCIAL_SCAM".equals(
                out.getDetectedThreatType())) {
            result.setThreatCategory(
                    AnalysisResult.ThreatCategory.SOCIAL_ENGINEERING
            );
        } else if ("MALWARE_DISGUISE".equals(
                out.getDetectedThreatType())) {
            result.setThreatCategory(
                    AnalysisResult.ThreatCategory.MALWARE
            );
        } else if ("SOCIAL_ENGINEERING".equals(
                out.getDetectedThreatType())) {
            result.setThreatCategory(
                    AnalysisResult.ThreatCategory.SOCIAL_ENGINEERING
            );
        }

        return result;
    }

    private AnalysisResult.RiskLevel toRiskLevel(
            int score) {
        if (score >= 75) return AnalysisResult.RiskLevel.CRITICAL;
        if (score >= 55) return AnalysisResult.RiskLevel.HIGH;
        if (score >= 35) return AnalysisResult.RiskLevel.MEDIUM;
        if (score >= 15) return AnalysisResult.RiskLevel.LOW;
        return AnalysisResult.RiskLevel.NEGLIGIBLE;
    }

    // ==========================================
    // STEP 1: VALIDATE IMAGE
    // ==========================================
    private void validateImage(
            byte[]              imageBytes,
            ImageAnalysisOutput output) {

        if (imageBytes == null
                || imageBytes.length == 0) {
            output.addRedFlag(
                    "Empty image content"
            );
            // FIX: was setIsValidImage(false)
            output.setValidImage(false);
            return;
        }

        if (imageBytes.length
                > 10 * 1024 * 1024) {
            output.addRedFlag(
                    "Image exceeds 10MB size limit"
            );
        }

        if (imageBytes.length < 100) {
            output.addRedFlag(
                    "Suspiciously small image file"
            );
        }

        // FIX: was setIsValidImage(true)
        output.setValidImage(true);
    }

    // ==========================================
    // STEP 2: ANALYZE IMAGE PROPERTIES
    // ==========================================
    private void analyzeImageProperties(
            byte[]              imageBytes,
            ImageAnalysisOutput output) {

        try {
            BufferedImage image = ImageIO.read(
                    new ByteArrayInputStream(imageBytes)
            );

            if (image == null) {
                output.addRedFlag(
                        "Cannot read image data "
                                + "— possible corruption "
                                + "or disguised file"
                );
                return;
            }

            int width  = image.getWidth();
            int height = image.getHeight();

            output.setImageWidth(width);
            output.setImageHeight(height);
            output.setImageType(
                    image.getType()
            );
            output.setHasAlphaChannel(
                    image.getColorModel()
                            .hasAlpha()
            );

            double ratio = (double) width / height;
            output.setAspectRatio(ratio);

            boolean isScreenshot =
                    (width  == 1920 && height == 1080)
                            || (width  == 1366 && height == 768)
                            || (width  == 1440 && height == 900)
                            || (width  == 2560 && height == 1440)
                            || (width  == 1280 && height == 720)
                            || (width  == 1280 && height == 800)
                            || (width  >= 1200 && height >= 600
                            && ratio > 1.3 && ratio < 2.5);

            // FIX: was setIsLikelyScreenshot(isScreenshot)
            output.setLikelyScreenshot(isScreenshot);

            if (isScreenshot) {
                output.addRedFlag(
                        "Image matches screenshot "
                                + "dimensions: "
                                + width + "x" + height
                                + " — possible phishing "
                                + "screenshot"
                );
            }

            analyzeColors(image, output);

            log.info(
                    "Image dimensions: {}x{} | "
                            + "Screenshot: {}",
                    width,
                    height,
                    isScreenshot
            );

        } catch (Exception e) {
            log.warn(
                    "Image properties error: {}",
                    e.getMessage()
            );
        }
    }

    // ==========================================
    // STEP 2B: ANALYZE COLORS
    // ==========================================
    private void analyzeColors(
            BufferedImage       image,
            ImageAnalysisOutput output) {

        try {
            int width  = Math.min(
                    image.getWidth(), 100
            );
            int height = Math.min(
                    image.getHeight(), 100
            );

            long whitePixels = 0;
            long totalPixels = width * height;

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int rgb = image.getRGB(x, y);
                    int r   = (rgb >> 16) & 0xFF;
                    int g   = (rgb >>  8) & 0xFF;
                    int b   =  rgb        & 0xFF;

                    if (r > 220
                            && g > 220
                            && b > 220) {
                        whitePixels++;
                    }
                }
            }

            double whiteRatio =
                    (double) whitePixels / totalPixels;
            output.setWhitePixelRatio(whiteRatio);

            if (whiteRatio > 0.5) {
                output.setHasHighWhiteBackground(
                        true
                );
            }

        } catch (Exception e) {
            log.warn(
                    "Color analysis error: {}",
                    e.getMessage()
            );
        }
    }

    // ==========================================
    // STEP 3: ANALYZE FILE HEADER
    // ==========================================
    private void analyzeFileHeader(
            byte[]              imageBytes,
            ImageAnalysisOutput output) {

        if (imageBytes.length < 4) return;

        boolean isJpeg =
                imageBytes[0] == (byte) 0xFF
                        && imageBytes[1] == (byte) 0xD8;

        boolean isPng =
                imageBytes[0] == (byte) 0x89
                        && imageBytes[1] == (byte) 0x50
                        && imageBytes[2] == (byte) 0x4E
                        && imageBytes[3] == (byte) 0x47;

        boolean isGif =
                imageBytes[0] == 'G'
                        && imageBytes[1] == 'I'
                        && imageBytes[2] == 'F';

        boolean isBmp =
                imageBytes[0] == 'B'
                        && imageBytes[1] == 'M';

        boolean isExecutable =
                imageBytes[0] == (byte) 0x4D
                        && imageBytes[1] == (byte) 0x5A;

        // FIX: was setIsJpeg / setIsPng / setIsGif / setIsBmp / setIsValidImageHeader
        output.setJpeg(isJpeg);
        output.setPng(isPng);
        output.setGif(isGif);
        output.setBmp(isBmp);
        output.setValidImageHeader(
                isJpeg || isPng || isGif || isBmp
        );

        if (isExecutable) {
            // FIX: was setIsExecutableDisguised(true)
            output.setExecutableDisguised(true);
            output.addRedFlag(
                    "CRITICAL: Executable file "
                            + "disguised as image "
                            + "(PE header detected)"
            );
        }

        boolean isZip =
                imageBytes[0] == (byte) 0x50
                        && imageBytes[1] == (byte) 0x4B;
        if (isZip) {
            output.addRedFlag(
                    "ZIP archive disguised as image"
            );
        }
    }

    // ==========================================
    // STEP 4: DETECT IMAGE MANIPULATION
    // ==========================================
    private void detectImageManipulation(
            byte[]              imageBytes,
            ImageAnalysisOutput output) {

        try {
            // FIX: was output.isIsJpeg()
            if (output.isJpeg()
                    && imageBytes.length > 4) {
                int lastFFD9 = -1;
                for (int i =
                     imageBytes.length - 2;
                     i >= 0; i--) {
                    if (imageBytes[i]
                            == (byte) 0xFF
                            && imageBytes[i + 1]
                            == (byte) 0xD9) {
                        lastFFD9 = i;
                        break;
                    }
                }

                if (lastFFD9 > 0
                        && lastFFD9
                        < imageBytes.length - 2) {
                    int extraBytes =
                            imageBytes.length
                                    - lastFFD9 - 2;
                    if (extraBytes > 100) {
                        output.setHasExtraData(
                                true
                        );
                        output.setExtraDataBytes(
                                extraBytes
                        );
                        output.addRedFlag(
                                "Possible steganography: "
                                        + extraBytes
                                        + " extra bytes after "
                                        + "JPEG end marker"
                        );
                    }
                }
            }

            String imageStr = new String(
                    Arrays.copyOf(imageBytes,
                            Math.min(imageBytes.length,
                                    500))
            );

            if (imageStr.contains("<script")
                    || imageStr.contains("<?php")
                    || imageStr.contains("#!/")) {
                // FIX: was setIsPolyglotFile(true)
                output.setPolyglotFile(true);
                output.addRedFlag(
                        "Polyglot file detected: "
                                + "image contains "
                                + "embedded code"
                );
            }

        } catch (Exception e) {
            log.warn(
                    "Manipulation detection error: {}",
                    e.getMessage()
            );
        }
    }

    // ==========================================
    // STEP 5: ANALYZE EXTRACTED TEXT (FROM OCR)
    // ==========================================
    private void analyzeExtractedText(
            ImageAnalysisOutput output) {

        String text = output.getExtractedText();
        if (text == null || text.isEmpty()) {
            return;
        }

        String textLower = text.toLowerCase();

        List<String> foundBrands =
                new ArrayList<>();
        for (String brand
                : PHISHING_BRAND_KEYWORDS) {
            if (textLower.contains(brand)) {
                foundBrands.add(brand);
            }
        }
        output.setDetectedBrands(foundBrands);

        if (!foundBrands.isEmpty()) {
            output.setHasBrandContent(true);
            output.addRedFlag(
                    "Brand keywords in image: "
                            + String.join(", ", foundBrands)
                            + " — possible brand impersonation"
            );
        }

        List<String> loginKeywordsFound =
                new ArrayList<>();
        int loginScore = 0;
        for (String kw : LOGIN_PAGE_KEYWORDS) {
            if (textLower.contains(kw)) {
                loginKeywordsFound.add(kw);
                loginScore++;
            }
        }
        output.setLoginKeywordsFound(
                loginKeywordsFound
        );

        if (loginScore >= 3) {
            // FIX: was setIsLikelyFakeLoginPage(true)
            output.setLikelyFakeLoginPage(true);
            output.addRedFlag(
                    "Fake login page detected: "
                            + loginScore
                            + " login indicators found"
            );
        }

        List<String> urgencyFound =
                new ArrayList<>();
        for (String kw : URGENCY_KEYWORDS) {
            if (textLower.contains(kw)) {
                urgencyFound.add(kw);
            }
        }
        output.setUrgencyKeywordsFound(urgencyFound);

        if (urgencyFound.size() >= 2) {
            output.setHasUrgencyContent(true);
            output.addRedFlag(
                    "Urgency language in image: "
                            + String.join(", ", urgencyFound)
            );
        }

        List<String> financialFound =
                new ArrayList<>();
        for (String kw : FINANCIAL_KEYWORDS) {
            if (textLower.contains(kw)) {
                financialFound.add(kw);
            }
        }
        output.setFinancialKeywordsFound(
                financialFound
        );

        if (financialFound.size() >= 2) {
            output.setHasFinancialContent(true);
            output.addRedFlag(
                    "Financial scam indicators: "
                            + String.join(", ", financialFound)
            );
        }

        List<String> urlsInImage =
                new ArrayList<>();
        Matcher urlMatcher =
                URL_PATTERN.matcher(text);
        while (urlMatcher.find()) {
            urlsInImage.add(urlMatcher.group());
        }
        output.setExtractedUrls(urlsInImage);

        if (!urlsInImage.isEmpty()) {
            output.addRedFlag(
                    "URLs found in image text: "
                            + urlsInImage.size()
            );
        }

        if (textLower.contains("password")
                || textLower.contains("••••")
                || textLower.contains("****")) {
            output.setHasPasswordField(true);
            output.addRedFlag(
                    "Password field detected "
                            + "in image — possible "
                            + "login page screenshot"
            );
        }

        List<String> formIndicators =
                Arrays.asList(
                        "[ ]", "[ ]", "input",
                        "submit", "button",
                        "enter here", "type here"
                );
        for (String indicator : formIndicators) {
            if (textLower.contains(indicator)) {
                output.setHasFormElements(true);
                break;
            }
        }
    }

    // ==========================================
    // STEP 5B: ANALYZE FILENAME FOR THREATS
    // ==========================================
    private void analyzeFilenameForThreats(
            String              imageName,
            ImageAnalysisOutput output) {

        if (imageName == null) return;

        String nameLower = imageName.toLowerCase();

        List<String> brandsInName =
                new ArrayList<>();
        for (String brand
                : PHISHING_BRAND_KEYWORDS) {
            if (nameLower.contains(brand)) {
                brandsInName.add(brand);
            }
        }

        if (!brandsInName.isEmpty()) {
            output.setHasBrandContent(true);
            output.addRedFlag(
                    "Brand name in filename: "
                            + String.join(", ", brandsInName)
            );
        }

        List<String> phishingFilenames =
                Arrays.asList(
                        "login", "signin", "verify",
                        "account", "security",
                        "password", "reset",
                        "invoice", "receipt",
                        "payment", "urgent",
                        "alert", "screenshot",
                        "capture", "screen"
                );

        for (String fname : phishingFilenames) {
            if (nameLower.contains(fname)) {
                output.addRedFlag(
                        "Suspicious filename: "
                                + imageName
                );
                break;
            }
        }

        String[] parts = imageName.split("\\.");
        if (parts.length > 2) {
            output.setHasDoubleExtension(true);
            output.addRedFlag(
                    "Double extension in filename: "
                            + imageName
                            + " — possible disguise"
            );
        }
    }

    // ==========================================
    // STEP 6: VISUAL THREAT PATTERNS
    // ==========================================
    private void detectVisualThreatPatterns(
            ImageAnalysisOutput output) {

        int threatSignals = 0;

        // FIX: all isIsXxx() → isXxx()
        if (output.isLikelyScreenshot())      threatSignals++;
        if (output.isHasBrandContent())        threatSignals++;
        if (output.isLikelyFakeLoginPage())    threatSignals += 2;
        if (output.isHasUrgencyContent())      threatSignals++;
        if (output.isHasPasswordField())       threatSignals++;
        if (output.isHasHighWhiteBackground()) threatSignals++;
        if (output.isHasFormElements())        threatSignals++;

        output.setThreatSignalCount(threatSignals);

        if (output.isLikelyFakeLoginPage()
                && output.isHasBrandContent()) {
            output.setDetectedThreatType(
                    "PHISHING_LOGIN_PAGE"
            );
        } else if (output.isHasFinancialContent()) {
            output.setDetectedThreatType(
                    "FINANCIAL_SCAM"
            );
        } else if (output.isHasUrgencyContent()) {
            output.setDetectedThreatType(
                    "SOCIAL_ENGINEERING"
            );
        } else if (output.isExecutableDisguised()) {
            output.setDetectedThreatType(
                    "MALWARE_DISGUISE"
            );
        } else if (threatSignals >= 2) {
            output.setDetectedThreatType(
                    "SUSPICIOUS_CONTENT"
            );
        } else {
            output.setDetectedThreatType("SAFE");
        }
    }

    // ==========================================
    // STEP 7: EXTRACT ARTIFACTS FROM TEXT
    // ==========================================
    private void extractArtifactsFromText(
            ImageAnalysisOutput output) {

        String text = output.getExtractedText();
        if (text == null || text.isEmpty()) return;

        List<String> emails = new ArrayList<>();
        Matcher emailMatcher =
                EMAIL_PATTERN.matcher(text);
        while (emailMatcher.find()) {
            emails.add(emailMatcher.group());
        }
        output.setExtractedEmails(emails);

        List<String> phones = new ArrayList<>();
        Matcher phoneMatcher =
                PHONE_PATTERN.matcher(text);
        while (phoneMatcher.find()) {
            phones.add(phoneMatcher.group());
        }
        output.setExtractedPhones(phones);
    }

    // ==========================================
    // STEP 8: CALCULATE IMAGE RISK SCORE
    // ==========================================
    private void calculateImageRiskScore(
            ImageAnalysisOutput output) {

        int score = 0;
        Map<String, Integer> breakdown =
                new LinkedHashMap<>();

        // FIX: all isIsXxx() → isXxx()
        if (output.isExecutableDisguised()) {
            score += 80;
            breakdown.put("EXECUTABLE_DISGUISE", 80);
        }
        if (output.isPolyglotFile()) {
            score += 40;
            breakdown.put("POLYGLOT_FILE", 40);
        }
        if (output.isLikelyFakeLoginPage()) {
            score += 35;
            breakdown.put("FAKE_LOGIN_PAGE", 35);
        }
        if (output.isHasBrandContent()
                && output.isLikelyScreenshot()) {
            score += 30;
            breakdown.put("BRAND_SCREENSHOT", 30);
        } else if (output.isHasBrandContent()) {
            score += 15;
            breakdown.put("BRAND_CONTENT", 15);
        }
        if (output.isHasPasswordField()) {
            score += 25;
            breakdown.put("PASSWORD_FIELD", 25);
        }
        if (output.isHasUrgencyContent()) {
            score += 20;
            breakdown.put("URGENCY_CONTENT", 20);
        }
        if (output.isHasFinancialContent()) {
            score += 20;
            breakdown.put("FINANCIAL_CONTENT", 20);
        }
        if (output.isHasExtraData()) {
            score += 25;
            breakdown.put("STEGANOGRAPHY", 25);
        }
        if (output.isHasDoubleExtension()) {
            score += 15;
            breakdown.put("DOUBLE_EXTENSION", 15);
        }
        if (output.isHasFormElements()) {
            score += 10;
            breakdown.put("FORM_ELEMENTS", 10);
        }
        if (output.getExtractedUrls() != null
                && !output.getExtractedUrls().isEmpty()) {
            int urlScore = Math.min(
                    output.getExtractedUrls().size() * 5, 15
            );
            score += urlScore;
            breakdown.put("URLS_IN_IMAGE", urlScore);
        }
        int signalScore = Math.min(
                output.getThreatSignalCount() * 3, 15
        );
        if (signalScore > 0) {
            score += signalScore;
            breakdown.put("THREAT_SIGNALS", signalScore);
        }

        output.setImageRiskScore(Math.min(score, 100));
        output.setScoreBreakdown(breakdown);
        output.setRedFlagCount(
                output.getRedFlags().size()
        );

        log.info(
                "Image risk score: {} | "
                        + "Threat type: {}",
                output.getImageRiskScore(),
                output.getDetectedThreatType()
        );
    }

    // ==========================================
    // IMAGE ANALYSIS OUTPUT MODEL
    // ==========================================
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageAnalysisOutput {

        // Image info
        private String          imageName;
        private String          mimeType;
        private long            imageSizeBytes;
        private int             imageWidth;
        private int             imageHeight;
        private int             imageType;
        private double          aspectRatio;

        // FIX: renamed all is-prefixed boolean fields
        // isValidImage          -> validImage
        // isValidImageHeader    -> validImageHeader
        // isJpeg                -> jpeg
        // isPng                 -> png
        // isGif                 -> gif
        // isBmp                 -> bmp
        // isExecutableDisguised -> executableDisguised
        // isPolyglotFile        -> polyglotFile
        // isLikelyScreenshot    -> likelyScreenshot
        // isLikelyFakeLoginPage -> likelyFakeLoginPage
        // Lombok on "isXxx" generates isIsXxx()/setIsXxx() — broken
        // Plain name generates correct isXxx()/setXxx()
        private boolean         validImage;
        private boolean         validImageHeader;
        private boolean         jpeg;
        private boolean         png;
        private boolean         gif;
        private boolean         bmp;

        // Threat flags
        private boolean         executableDisguised;
        private boolean         polyglotFile;
        private boolean         likelyScreenshot;
        private boolean         hasDoubleExtension;
        private boolean         hasAlphaChannel;
        private boolean         hasExtraData;
        private int             extraDataBytes;

        // Visual analysis
        private double          whitePixelRatio;
        private boolean         hasHighWhiteBackground;

        // Content analysis
        private boolean         hasBrandContent;
        private List<String>    detectedBrands;
        private boolean         likelyFakeLoginPage;
        private boolean         hasPasswordField;
        private boolean         hasFormElements;
        private boolean         hasUrgencyContent;
        private boolean         hasFinancialContent;

        // Keyword analysis
        private List<String>    loginKeywordsFound;
        private List<String>    urgencyKeywordsFound;
        private List<String>    financialKeywordsFound;

        // OCR text
        private String          extractedText;

        // Extracted artifacts
        private List<String>    extractedUrls;
        private List<String>    extractedEmails;
        private List<String>    extractedPhones;

        // Threat classification
        private String          detectedThreatType;
        private int             threatSignalCount;

        // Risk output
        private int             imageRiskScore;
        private int             redFlagCount;
        private Map<String,
                Integer>        scoreBreakdown;

        private List<String>    redFlags
                = new ArrayList<>();

        // Analysis metadata
        private LocalDateTime   analyzedAt;
        private boolean         analysisSuccess;
        private String          errorMessage;

        // Helper
        public void addRedFlag(String flag) {
            if (this.redFlags == null) {
                this.redFlags = new ArrayList<>();
            }
            this.redFlags.add(flag);
        }
    }
}