package com.project.intelligence.analyzer;

import com.project.intelligence.model.AnalysisResult;
import com.project.intelligence.model.ThreatInput;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// ==========================================
// DOCUMENT / PDF ANALYZER
// Extracts and analyzes content from
// PDF, DOCX, XLSX, PPTX, TXT files
// Detects embedded threats, malicious
// macros, suspicious metadata and content
// ==========================================
@Slf4j
@Component
public class DocumentAnalyzer {

    // ==========================================
    // SUPPORTED FILE TYPES
    // ==========================================
    private static final Map<String, String>
            MIME_TYPE_MAP = new HashMap<>() {{
        put("application/pdf",            "PDF");
        put("application/msword",         "DOC");
        put("application/vnd.openxmlformats"
                + "-officedocument"
                + ".wordprocessingml.document","DOCX");
        put("application/vnd.ms-excel",   "XLS");
        put("application/vnd.openxmlformats"
                + "-officedocument"
                + ".spreadsheetml.sheet",     "XLSX");
        put("application/vnd.ms-powerpoint","PPT");
        put("application/vnd.openxmlformats"
                        + "-officedocument"
                        + ".presentationml.presentation",
                "PPTX");
        put("text/plain",                 "TXT");
        put("text/html",                  "HTML");
        put("text/csv",                   "CSV");
    }};

    // ==========================================
    // MALICIOUS KEYWORDS IN DOCUMENTS
    // ==========================================
    private static final List<String>
            MALICIOUS_KEYWORDS = Arrays.asList(
            "autoopen", "auto_open",
            "workbook_open", "document_open",
            "shell", "powershell", "cmd.exe",
            "wscript", "cscript", "vbscript",
            "javascript:", "eval(", "exec(",
            "createobject", "getobject",
            "wmi", "winmgmts",
            "http://", "https://",
            "ftp://", "\\\\", "net use",
            "wget", "curl", "invoke-webrequest",
            "invoke-expression", "iex(",
            "downloadfile", "downloadstring",
            "temp\\", "appdata\\",
            "startup\\", "system32\\",
            "reg add", "reg delete",
            "schtasks", "at.exe",
            "regsvr32", "rundll32",
            "mshta", "certutil",
            "base64", "frombase64string",
            "chr(", "charcode",
            "string.fromcharcode",
            "unescape(", "atob(",
            "xor", "encrypt", "decrypt"
    );

    // ==========================================
    // PHISHING CONTENT KEYWORDS
    // ==========================================
    private static final List<String>
            PHISHING_CONTENT = Arrays.asList(
            "enable macros",
            "enable content",
            "enable editing",
            "allow content",
            "protected document",
            "protected view",
            "click enable",
            "must enable",
            "to view this document",
            "to read this file",
            "document is encrypted",
            "requires activation",
            "verify your account",
            "confirm your identity",
            "update your information",
            "click here to access",
            "your document is ready",
            "invoice attached",
            "payment receipt",
            "delivery notification",
            "package tracking"
    );

    // ==========================================
    // SENSITIVE DATA PATTERNS
    // ==========================================
    private static final Pattern
            CREDIT_CARD_PATTERN = Pattern.compile(
            "\\b(?:\\d{4}[\\s\\-]?){3}\\d{4}\\b"
    );

    private static final Pattern
            SSN_PATTERN = Pattern.compile(
            "\\b\\d{3}[\\s\\-]\\d{2}"
                    + "[\\s\\-]\\d{4}\\b"
    );

    private static final Pattern
            EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9._%+\\-]+"
                    + "@[a-zA-Z0-9.\\-]+"
                    + "\\.[a-zA-Z]{2,}",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern
            URL_PATTERN = Pattern.compile(
            "https?://[\\w\\-.]+"
                    + "(:\\d+)?(/[\\w\\-./?%&=#@]*)?",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern
            IP_PATTERN = Pattern.compile(
            "\\b(\\d{1,3}\\.){3}\\d{1,3}\\b"
    );

    private static final Pattern
            PHONE_PATTERN = Pattern.compile(
            "(\\+?\\d{1,3}[\\s\\-]?)?"
                    + "(\\(?\\d{3}\\)?[\\s\\-]?)"
                    + "\\d{3}[\\s\\-]?\\d{4}"
    );

    // ==========================================
    // DANGEROUS FILE EXTENSIONS
    // ==========================================
    private static final List<String>
            DANGEROUS_EXTENSIONS = Arrays.asList(
            ".exe", ".bat", ".cmd", ".com",
            ".scr", ".vbs", ".js",  ".jar",
            ".msi", ".dll", ".ps1", ".sh",
            ".hta", ".wsf", ".wsh"
    );

    // ==========================================
    // APACHE TIKA INSTANCE
    // ==========================================
    private final Tika tika = new Tika();

    // ==========================================
    // MAIN ANALYZE METHOD
    // Called by IntelligencePipeline Stage 1
    // ==========================================
    public AnalysisResult analyze(
            ThreatInput input) {

        log.info(
                "Document Analyzer starting: {}",
                input.getFileName()
        );

        AnalysisResult result =
                new AnalysisResult();
        result.setAnalyzedAt(LocalDateTime.now());
        result.setRedFlags(new ArrayList<>());

        byte[] fileBytes  = null;
        String fileName   = input.getFileName();

        // Try to get bytes from input
        if (input.getFileBytes() != null) {
            fileBytes = input.getFileBytes();
        }

        try {
            // Internal output object
            DocumentAnalysisOutput output =
                    new DocumentAnalysisOutput();
            output.setFileName(fileName);
            output.setFileSizeBytes(
                    fileBytes != null
                            ? fileBytes.length
                            : 0
            );
            output.setAnalyzedAt(
                    LocalDateTime.now()
            );

            if (fileBytes != null
                    && fileBytes.length > 0) {

                // Step 1: Detect file type
                detectFileType(
                        fileBytes, fileName,
                        input.getFileMimeType(), output
                );

                // Step 2: Validate file
                if (validateFile(
                        fileBytes, output)) {

                    // Step 3: Extract text
                    extractTextContent(
                            fileBytes, output
                    );

                    // Step 4: Extract metadata
                    extractMetadata(
                            fileBytes, output
                    );

                    // Step 5: Analyze content
                    if (output.getExtractedText()
                            != null
                            && !output
                            .getExtractedText()
                            .isEmpty()) {

                        scanForMaliciousKeywords(
                                output
                        );
                        scanForPhishingContent(
                                output
                        );
                        detectSensitiveData(output);
                        extractArtifacts(output);
                    }

                    // Step 6: Metadata analysis
                    analyzeMetadata(output);

                    // Step 7: Structure analysis
                    analyzeDocumentStructure(
                            fileBytes, fileName, output
                    );

                    // Step 8: Calculate score
                    calculateDocumentRiskScore(
                            output
                    );
                }

            } else {
                // No bytes — use extracted text
                // from ThreatInput if available
                if (input.getExtractedText()
                        != null) {
                    output.setExtractedText(
                            input.getExtractedText()
                    );
                    scanForMaliciousKeywords(output);
                    scanForPhishingContent(output);
                    detectSensitiveData(output);
                    extractArtifacts(output);
                    calculateDocumentRiskScore(output);
                }
            }

            // Map output → AnalysisResult
            mapToAnalysisResult(output, result);

            log.info(
                    "Document analysis complete: "
                            + "score={}",
                    result.getRiskScore()
            );

        } catch (Exception e) {
            log.error(
                    "Document analysis error: {}",
                    e.getMessage()
            );
            result.setRiskScore(40);
            result.setRiskLevel(
                    AnalysisResult.RiskLevel.MEDIUM
            );
            result.getRedFlags().add(
                    "Analysis error: " + e.getMessage()
            );
        }

        return result;
    }

    // ==========================================
    // MAP OUTPUT TO ANALYSIS RESULT
    // ==========================================
    private void mapToAnalysisResult(
            DocumentAnalysisOutput output,
            AnalysisResult         result) {

        result.setRiskScore(
                output.getDocumentRiskScore()
        );
        result.setRiskLevel(
                mapScoreToLevel(
                        output.getDocumentRiskScore()
                )
        );
        result.setConfidenceScore(0.75);
        result.setConfidencePercentage("75%");

        // Map red flags
        if (output.getRedFlags() != null) {
            result.setRedFlags(
                    output.getRedFlags()
            );
        }

        // Map artifacts
        result.setExtractedUrls(
                output.getExtractedUrls()
        );
        result.setExtractedEmails(
                output.getExtractedEmails()
        );
        result.setExtractedIps(
                output.getExtractedIps()
        );

        // Map score breakdown
        if (output.getScoreBreakdown() != null) {
            result.setScoreBreakdown(
                    output.getScoreBreakdown()
            );
        }

        // Set category
        // FIX: was output.isIsExecutableFile()
        //      and output.isIsMacroEnabledFormat()
        if (output.isExecutableFile()
                || output.isMacroEnabledFormat()) {
            result.setThreatCategory(
                    AnalysisResult
                            .ThreatCategory.MALWARE
            );
        } else if (
                output.isContainsPhishingContent()) {
            result.setThreatCategory(
                    AnalysisResult
                            .ThreatCategory.PHISHING
            );
        } else if (
                output.isContainsCreditCardData()
                        || output.isContainsSsnData()) {
            result.setThreatCategory(
                    AnalysisResult
                            .ThreatCategory.DATA_BREACH
            );
        }

        // Set description
        result.setThreatDescription(
                "Document analysis: "
                        + output.getRedFlagCount()
                        + " red flag(s) found in "
                        + (output.getDetectedFileType() != null
                        ? output.getDetectedFileType()
                        : "document")
                        + " file."
        );

        // Store extracted text in input
        result.setAttackNarrative(
                output.getExtractedText() != null
                        ? output.getExtractedText()
                        .substring(
                                0,
                                Math.min(
                                        500,
                                        output.getExtractedText()
                                                .length()
                                )
                        )
                        : ""
        );
    }

    // ==========================================
    // STEP 1: DETECT FILE TYPE
    // ==========================================
    private void detectFileType(
            byte[]                 fileBytes,
            String                 fileName,
            String                 providedMimeType,
            DocumentAnalysisOutput output) {

        try {
            String detectedMime =
                    tika.detect(fileBytes);
            output.setDetectedMimeType(detectedMime);
            output.setDetectedFileType(
                    MIME_TYPE_MAP.getOrDefault(
                            detectedMime, "UNKNOWN"
                    )
            );

            if (fileName != null) {
                String ext = fileName.contains(".")
                        ? fileName.substring(
                        fileName.lastIndexOf(".")
                ).toLowerCase()
                        : "";
                output.setFileExtension(ext);

                boolean mimeMatchesExt =
                        checkMimeExtensionMatch(
                                detectedMime, ext
                        );
                output.setHasExtensionMismatch(
                        !mimeMatchesExt
                );

                if (!mimeMatchesExt) {
                    output.addRedFlag(
                            "File extension mismatch: "
                                    + "declared as '" + ext
                                    + "' but content is '"
                                    + detectedMime + "'"
                    );
                }
            }

        } catch (Exception e) {
            log.warn(
                    "File type detection failed: {}",
                    e.getMessage()
            );
            output.setDetectedMimeType(
                    providedMimeType
            );
        }
    }

    // ==========================================
    // STEP 2: VALIDATE FILE
    // ==========================================
    private boolean validateFile(
            byte[]                 fileBytes,
            DocumentAnalysisOutput output) {

        if (fileBytes == null
                || fileBytes.length == 0) {
            output.addRedFlag("Empty file content");
            return false;
        }

        if (fileBytes.length
                > 10 * 1024 * 1024) {
            output.addRedFlag(
                    "File exceeds 10MB size limit"
            );
            return false;
        }

        // PE header check (MZ = 0x4D5A)
        if (fileBytes.length >= 2
                && fileBytes[0] == 0x4D
                && fileBytes[1] == 0x5A) {
            // FIX: was setIsExecutableFile(true)
            output.setExecutableFile(true);
            output.addRedFlag(
                    "Executable file detected "
                            + "(PE header) — "
                            + "disguised as document"
            );
        }

        boolean isZipBased =
                fileBytes.length >= 4
                        && fileBytes[0] == 0x50
                        && fileBytes[1] == 0x4B
                        && fileBytes[2] == 0x03
                        && fileBytes[3] == 0x04;
        // FIX: was setIsZipBasedFormat(isZipBased)
        output.setZipBasedFormat(isZipBased);

        return true;
    }

    // ==========================================
    // STEP 3: EXTRACT TEXT CONTENT
    // ==========================================
    private void extractTextContent(
            byte[]                 fileBytes,
            DocumentAnalysisOutput output) {

        try {
            BodyContentHandler handler =
                    new BodyContentHandler(
                            10 * 1024 * 1024
                    );
            Metadata    metadata = new Metadata();
            ParseContext context  =
                    new ParseContext();
            AutoDetectParser parser =
                    new AutoDetectParser();

            try (InputStream stream =
                         new ByteArrayInputStream(
                                 fileBytes
                         )) {
                parser.parse(
                        stream, handler,
                        metadata, context
                );
            }

            String text = handler.toString();
            output.setExtractedText(text);
            output.setExtractedTextLength(
                    text.length()
            );
            output.setWordCount(
                    text.trim().split("\\s+").length
            );

            Map<String, String> metaMap =
                    new LinkedHashMap<>();
            for (String name : metadata.names()) {
                metaMap.put(
                        name, metadata.get(name)
                );
            }
            output.setTikaMetadata(metaMap);

            log.info(
                    "Extracted {} chars",
                    text.length()
            );

        } catch (Exception e) {
            log.warn(
                    "Text extraction warning: {}",
                    e.getMessage()
            );
            output.setExtractedText("");
            output.addRedFlag(
                    "Failed to extract text — "
                            + "possible encryption"
            );
        }
    }

    // ==========================================
    // STEP 4: EXTRACT METADATA
    // ==========================================
    private void extractMetadata(
            byte[]                 fileBytes,
            DocumentAnalysisOutput output) {

        try {
            Metadata         metadata =
                    new Metadata();
            AutoDetectParser parser   =
                    new AutoDetectParser();
            ParseContext     context  =
                    new ParseContext();
            BodyContentHandler handler =
                    new BodyContentHandler(-1);

            try (InputStream stream =
                         new ByteArrayInputStream(
                                 fileBytes
                         )) {
                parser.parse(
                        stream, handler,
                        metadata, context
                );
            }

            output.setDocumentAuthor(
                    metadata.get("Author")
            );
            output.setDocumentCreator(
                    metadata.get("creator")
            );
            output.setDocumentTitle(
                    metadata.get("title")
            );
            output.setDocumentCreatedDate(
                    metadata.get("meta:creation-date")
            );
            output.setDocumentModifiedDate(
                    metadata.get("Last-Modified")
            );
            output.setDocumentProducer(
                    metadata.get(
                            "pdf:docinfo:producer"
                    )
            );
            output.setPageCount(
                    parseIntSafely(
                            metadata.get("xmpTPg:NPages")
                    )
            );

        } catch (Exception e) {
            log.warn(
                    "Metadata extraction warning: {}",
                    e.getMessage()
            );
        }
    }

    // ==========================================
    // STEP 5A: MALICIOUS KEYWORD SCAN
    // ==========================================
    private void scanForMaliciousKeywords(
            DocumentAnalysisOutput output) {

        String text = output.getExtractedText()
                .toLowerCase();
        List<String> found = new ArrayList<>();

        for (String kw : MALICIOUS_KEYWORDS) {
            if (text.contains(kw.toLowerCase())) {
                found.add(kw);
            }
        }

        output.setMaliciousKeywordsFound(found);
        output.setMaliciousKeywordCount(
                found.size()
        );

        if (!found.isEmpty()) {
            output.setContainsMaliciousKeywords(
                    true
            );
            output.addRedFlag(
                    "Malicious keywords found: "
                            + String.join(", ", found)
            );
        }
    }

    // ==========================================
    // STEP 5B: PHISHING CONTENT SCAN
    // ==========================================
    private void scanForPhishingContent(
            DocumentAnalysisOutput output) {

        String text = output.getExtractedText()
                .toLowerCase();
        List<String> found = new ArrayList<>();

        for (String phrase : PHISHING_CONTENT) {
            if (text.contains(phrase)) {
                found.add(phrase);
            }
        }

        output.setPhishingPhrasesFound(found);

        if (found.size() >= 2) {
            output.setContainsPhishingContent(true);
            output.addRedFlag(
                    "Phishing content: "
                            + String.join(", ", found)
            );
        }

        if (text.contains("enable macros")
                || text.contains("enable content")
                || text.contains(
                "enable editing")) {
            output.setHasMacroEnableInstruction(
                    true
            );
            output.addRedFlag(
                    "Macro enable instruction — "
                            + "classic malware delivery tactic"
            );
        }
    }

    // ==========================================
    // STEP 5C: SENSITIVE DATA DETECTION
    // ==========================================
    private void detectSensitiveData(
            DocumentAnalysisOutput output) {

        String text = output.getExtractedText();

        Matcher ccMatcher =
                CREDIT_CARD_PATTERN.matcher(text);
        if (ccMatcher.find()) {
            output.setContainsCreditCardData(true);
            output.addRedFlag(
                    "Credit card number pattern detected"
            );
        }

        Matcher ssnMatcher =
                SSN_PATTERN.matcher(text);
        if (ssnMatcher.find()) {
            output.setContainsSsnData(true);
            output.addRedFlag(
                    "SSN pattern detected"
            );
        }

        int count = 0;
        if (output.isContainsCreditCardData()) {
            count++;
        }
        if (output.isContainsSsnData()) {
            count++;
        }
        output.setSensitiveDataTypeCount(count);
    }

    // ==========================================
    // STEP 5D: EXTRACT ARTIFACTS
    // ==========================================
    private void extractArtifacts(
            DocumentAnalysisOutput output) {

        String text = output.getExtractedText();

        List<String> urls = new ArrayList<>();
        Matcher urlMatcher =
                URL_PATTERN.matcher(text);
        while (urlMatcher.find()) {
            urls.add(urlMatcher.group());
        }
        output.setExtractedUrls(urls);

        List<String> emails = new ArrayList<>();
        Matcher emailMatcher =
                EMAIL_PATTERN.matcher(text);
        while (emailMatcher.find()) {
            emails.add(emailMatcher.group());
        }
        output.setExtractedEmails(emails);

        List<String> ips = new ArrayList<>();
        Matcher ipMatcher = IP_PATTERN.matcher(text);
        while (ipMatcher.find()) {
            ips.add(ipMatcher.group());
        }
        output.setExtractedIps(ips);

        List<String> phones = new ArrayList<>();
        Matcher phoneMatcher =
                PHONE_PATTERN.matcher(text);
        while (phoneMatcher.find()) {
            phones.add(phoneMatcher.group());
        }
        output.setExtractedPhones(phones);

        List<String> dangerousRefs =
                new ArrayList<>();
        String textLower = text.toLowerCase();
        for (String ext : DANGEROUS_EXTENSIONS) {
            if (textLower.contains(ext)) {
                dangerousRefs.add(ext);
            }
        }
        output.setDangerousExtensionRefs(
                dangerousRefs
        );

        if (!dangerousRefs.isEmpty()) {
            output.addRedFlag(
                    "Dangerous file refs: "
                            + String.join(", ", dangerousRefs)
            );
        }
    }

    // ==========================================
    // STEP 6: METADATA ANALYSIS
    // ==========================================
    private void analyzeMetadata(
            DocumentAnalysisOutput output) {

        String author = output.getDocumentAuthor();
        if (author != null) {
            List<String> suspicious = Arrays.asList(
                    "user", "admin", "test",
                    "unknown", "anonymous",
                    "hacker", "attacker"
            );
            for (String s : suspicious) {
                if (author.toLowerCase()
                        .contains(s)) {
                    output.setHasSuspiciousAuthor(
                            true
                    );
                    output.addRedFlag(
                            "Suspicious author: " + author
                    );
                    break;
                }
            }
        }

        if (author == null
                && output.getDocumentTitle()
                == null
                && output.getDocumentCreatedDate()
                == null) {
            output.setHasEmptyMetadata(true);
            output.addRedFlag(
                    "No metadata — "
                            + "possible deliberate removal"
            );
        }

        if (output.getDocumentCreatedDate() != null
                && output.getDocumentModifiedDate()
                != null) {
            output.setMetadataConsistent(true);
        }
    }

    // ==========================================
    // STEP 7: DOCUMENT STRUCTURE ANALYSIS
    // ==========================================
    private void analyzeDocumentStructure(
            byte[]                 fileBytes,
            String                 fileName,
            DocumentAnalysisOutput output) {

        String fileType =
                output.getDetectedFileType();

        if ("PDF".equals(fileType)) {
            String text = output.getExtractedText();
            if (text == null
                    || text.trim().isEmpty()) {
                // FIX: was setIsEncryptedDocument(true)
                output.setEncryptedDocument(true);
                output.addRedFlag(
                        "PDF encrypted or "
                                + "content not extractable"
                );
            }
        }

        String ext = output.getFileExtension();
        if (ext != null) {
            boolean isMacro =
                    ext.equals(".docm")
                            || ext.equals(".xlsm")
                            || ext.equals(".pptm")
                            || ext.equals(".xltm")
                            || ext.equals(".dotm");

            // FIX: was setIsMacroEnabledFormat(isMacro)
            output.setMacroEnabledFormat(isMacro);
            if (isMacro) {
                output.addRedFlag(
                        "Macro-enabled format: "
                                + ext
                );
            }
        }

        long size = output.getFileSizeBytes();
        if (size < 1024) {
            output.addRedFlag(
                    "Unusually small: " + size + " bytes"
            );
        }
        if (size > 5 * 1024 * 1024) {
            // FIX: was setIsLargeFile(true)
            output.setLargeFile(true);
        }
    }

    // ==========================================
    // STEP 8: CALCULATE RISK SCORE
    // ==========================================
    private void calculateDocumentRiskScore(
            DocumentAnalysisOutput output) {

        int score = 0;
        Map<String, Integer> breakdown =
                new LinkedHashMap<>();

        // FIX: was output.isIsExecutableFile()
        if (output.isExecutableFile()) {
            score += 80;
            breakdown.put("EXECUTABLE_FILE", 80);
        }
        // FIX: was output.isIsMacroEnabledFormat()
        if (output.isMacroEnabledFormat()) {
            score += 30;
            breakdown.put(
                    "MACRO_ENABLED_FORMAT", 30
            );
        }
        if (output.isHasMacroEnableInstruction()) {
            score += 35;
            breakdown.put(
                    "MACRO_ENABLE_INSTRUCTION", 35
            );
        }
        if (output.getMaliciousKeywordCount() > 0) {
            int kw = Math.min(
                    output.getMaliciousKeywordCount()
                            * 5, 40
            );
            score += kw;
            breakdown.put("MALICIOUS_KEYWORDS", kw);
        }
        if (output.isContainsPhishingContent()) {
            score += 20;
            breakdown.put("PHISHING_CONTENT", 20);
        }
        if (output.isHasExtensionMismatch()) {
            score += 25;
            breakdown.put("EXTENSION_MISMATCH", 25);
        }
        if (output.isContainsCreditCardData()) {
            score += 15;
            breakdown.put("CREDIT_CARD_DATA", 15);
        }
        if (output.isContainsSsnData()) {
            score += 15;
            breakdown.put("SSN_DATA", 15);
        }
        // FIX: was output.isIsEncryptedDocument()
        if (output.isEncryptedDocument()) {
            score += 20;
            breakdown.put(
                    "ENCRYPTED_DOCUMENT", 20
            );
        }
        if (output.isHasSuspiciousAuthor()) {
            score += 10;
            breakdown.put("SUSPICIOUS_AUTHOR", 10);
        }
        if (output.isHasEmptyMetadata()) {
            score += 10;
            breakdown.put("EMPTY_METADATA", 10);
        }
        if (output.getDangerousExtensionRefs()
                != null
                && !output.getDangerousExtensionRefs()
                .isEmpty()) {
            int d = Math.min(
                    output.getDangerousExtensionRefs()
                            .size() * 5, 15
            );
            score += d;
            breakdown.put("DANGEROUS_REFS", d);
        }
        if (output.getExtractedUrls() != null
                && !output.getExtractedUrls()
                .isEmpty()) {
            int u = Math.min(
                    output.getExtractedUrls().size()
                            * 2, 10
            );
            score += u;
            breakdown.put("EMBEDDED_URLS", u);
        }

        output.setDocumentRiskScore(
                Math.min(score, 100)
        );
        output.setScoreBreakdown(breakdown);
        output.setRedFlagCount(
                output.getRedFlags() != null
                        ? output.getRedFlags().size()
                        : 0
        );
    }

    // ==========================================
    // HELPER: MAP SCORE TO RISK LEVEL
    // ==========================================
    private AnalysisResult.RiskLevel
    mapScoreToLevel(int score) {

        if (score >= 75) {
            return AnalysisResult.RiskLevel.CRITICAL;
        } else if (score >= 55) {
            return AnalysisResult.RiskLevel.HIGH;
        } else if (score >= 35) {
            return AnalysisResult.RiskLevel.MEDIUM;
        } else if (score >= 15) {
            return AnalysisResult.RiskLevel.LOW;
        } else {
            return AnalysisResult
                    .RiskLevel.NEGLIGIBLE;
        }
    }

    // ==========================================
    // HELPER: PARSE INT SAFELY
    // ==========================================
    private int parseIntSafely(String value) {
        if (value == null) return 0;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ==========================================
    // HELPER: CHECK MIME/EXTENSION MATCH
    // ==========================================
    private boolean checkMimeExtensionMatch(
            String mimeType,
            String extension) {

        Map<String, List<String>> map =
                new HashMap<>() {{
                    put("application/pdf",
                            Arrays.asList(".pdf"));
                    put("application/msword",
                            Arrays.asList(".doc", ".dot"));
                    put("application/vnd.openxmlformats"
                                    + "-officedocument"
                                    + ".wordprocessingml.document",
                            Arrays.asList(".docx",".docm"));
                    put("application/vnd.ms-excel",
                            Arrays.asList(".xls", ".xlt"));
                    put("application/vnd.openxmlformats"
                                    + "-officedocument"
                                    + ".spreadsheetml.sheet",
                            Arrays.asList(".xlsx",".xlsm"));
                    put("text/plain",
                            Arrays.asList(
                                    ".txt", ".log", ".csv"
                            ));
                    put("text/html",
                            Arrays.asList(".html",".htm"));
                }};

        List<String> valid = map.get(mimeType);
        if (valid == null) return true;
        return valid.contains(
                extension.toLowerCase()
        );
    }

    // ==========================================
    // DOCUMENT ANALYSIS OUTPUT MODEL
    // ==========================================
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentAnalysisOutput {

        private String  fileName;
        private String  fileExtension;
        private long    fileSizeBytes;
        private String  detectedMimeType;
        private String  detectedFileType;
        private String  providedMimeType;

        // FIX: renamed all is-prefixed boolean fields
        // isExecutableFile    -> executableFile
        // isZipBasedFormat    -> zipBasedFormat
        // isMacroEnabledFormat-> macroEnabledFormat
        // isEncryptedDocument -> encryptedDocument
        // isLargeFile         -> largeFile
        // Lombok on "isXxx" boolean generates
        // isIsXxx()/setIsXxx() — both broken.
        // Plain name generates correct isXxx()/setXxx()
        private boolean executableFile;
        private boolean zipBasedFormat;
        private boolean macroEnabledFormat;
        private boolean encryptedDocument;
        private boolean largeFile;
        private boolean hasExtensionMismatch;

        private String  extractedText;
        private int     extractedTextLength;
        private int     wordCount;
        private int     pageCount;

        private String  documentAuthor;
        private String  documentCreator;
        private String  documentTitle;
        private String  documentCreatedDate;
        private String  documentModifiedDate;
        private String  documentProducer;
        private Map<String, String> tikaMetadata;
        private boolean hasSuspiciousAuthor;
        private boolean hasEmptyMetadata;
        private boolean metadataConsistent;

        private boolean     containsMaliciousKeywords;
        private List<String>maliciousKeywordsFound;
        private int         maliciousKeywordCount;

        private boolean     containsPhishingContent;
        private List<String>phishingPhrasesFound;
        private boolean     hasMacroEnableInstruction;

        private boolean containsCreditCardData;
        private boolean containsSsnData;
        private int     sensitiveDataTypeCount;

        private List<String> extractedUrls;
        private List<String> extractedEmails;
        private List<String> extractedIps;
        private List<String> extractedPhones;
        private List<String> dangerousExtensionRefs;

        private int                  documentRiskScore;
        private int                  redFlagCount;
        private Map<String, Integer> scoreBreakdown;
        private List<String>         redFlags
                = new ArrayList<>();

        private LocalDateTime analyzedAt;
        private boolean       analysisSuccess;
        private String        errorMessage;

        public void addRedFlag(String flag) {
            if (this.redFlags == null) {
                this.redFlags = new ArrayList<>();
            }
            this.redFlags.add(flag);
        }
    }
}
