package com.project.sandbox.service;

import com.project.intelligence.model.AnalysisResult;
import com.project.intelligence.model.ThreatInput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// ==========================================
// SANDBOX ANALYSIS SERVICE
// Safe simulation environment that tests
// inputs without executing real threats.
// Performs static analysis to extract
// payloads, detect obfuscation, identify
// C2 patterns and classify threat behavior
// ==========================================
@Slf4j
@Service
public class SandboxAnalysisService {

    @Value("${intelligence.sandbox.timeout:5000}")
    private int sandboxTimeout;

    // ==========================================
    // OBFUSCATION PATTERNS
    // ==========================================
    private static final List<String>
            OBFUSCATION_INDICATORS = Arrays.asList(
            "eval(",
            "base64_decode(",
            "fromcharcode",
            "string.fromcharcode",
            "unescape(",
            "atob(",
            "btoa(",
            "\\x",
            "%u",
            "chr(",
            "charcode",
            "rot13",
            "xor",
            "hex2bin",
            "gzinflate",
            "gzuncompress",
            "str_rot13",
            "invoke-expression",
            "iex(",
            "-encodedcommand",
            "-enc ",
            "frombase64string"
    );

    // ==========================================
    // C2 COMMUNICATION PATTERNS
    // ==========================================
    private static final List<String>
            C2_PATTERNS = Arrays.asList(
            "wget ", "curl ",
            "invoke-webrequest",
            "invoke-restmethod",
            "net.webclient",
            "downloadfile",
            "downloadstring",
            "start-bitstransfer",
            "bitsadmin",
            "certutil -decode",
            "certutil -urlcache",
            "regsvr32 /s /n /u",
            "mshta http",
            "rundll32 javascript",
            "wscript.shell",
            "createobject",
            "xmlhttp",
            "winhttp",
            "connect(",
            "send(",
            "socket"
    );

    // ==========================================
    // PERSISTENCE MECHANISMS
    // ==========================================
    private static final List<String>
            PERSISTENCE_PATTERNS = Arrays.asList(
            "schtasks",
            "reg add",
            "hkey_current_user\\software\\microsoft\\windows\\currentversion\\run",
            "hkcu\\software\\microsoft\\windows\\currentversion\\run",
            "startup",
            "autorun",
            "autostart",
            "at.exe",
            "taskschd",
            "crontab",
            "rc.local",
            "profile",
            ".bashrc",
            ".bash_profile",
            "launchagent",
            "launchdaemon"
    );

    // ==========================================
    // PRIVILEGE ESCALATION PATTERNS
    // ==========================================
    private static final List<String>
            PRIVILEGE_PATTERNS = Arrays.asList(
            "sudo",
            "runas",
            "elevate",
            "bypass uac",
            "uac bypass",
            "token impersonation",
            "seimpersonateprivilege",
            "setokeninformation",
            "adjusttokenprivileges",
            "net localgroup administrators",
            "net user /add",
            "whoami /priv",
            "getsystem",
            "bypassamsi"
    );

    // ==========================================
    // SUSPICIOUS URL REDIRECT CHAINS
    // ==========================================
    private static final Pattern
            REDIRECT_PATTERN = Pattern.compile(
            "(redirect|goto|url|next|return|"
                    + "forward|destination|target)"
                    + "=[\"']?(https?://[^\"'&\\s]+)",
            Pattern.CASE_INSENSITIVE
    );

    // ==========================================
    // IP EXTRACTION PATTERN
    // ==========================================
    private static final Pattern
            IP_PATTERN = Pattern.compile(
            "\\b(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)"
                    + "\\.(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)"
                    + "\\.(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)"
                    + "\\.(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)"
                    + "\\b"
    );

    // ==========================================
    // URL EXTRACTION PATTERN
    // ==========================================
    private static final Pattern
            URL_PATTERN = Pattern.compile(
            "https?://[\\w\\-.]+(:\\d+)?"
                    + "(/[\\w\\-./?%&=#@]*)?",
            Pattern.CASE_INSENSITIVE
    );

    // ==========================================
    // MAIN SANDBOX ANALYSIS METHOD
    // ==========================================
    public SandboxResult analyze(
            ThreatInput    input,
            AnalysisResult partialResult) {

        log.info(
                "Sandbox analysis starting: {}",
                input.getInputType()
        );

        long startTime = System.currentTimeMillis();

        SandboxResult result = new SandboxResult();
        result.setInputType(input.getInputType().name());
        result.setAnalyzedAt(java.time.LocalDateTime.now());

        try {
            // Stage 1: Static Content Analysis
            performStaticAnalysis(input, result);

            // Stage 2: Behavioral Pattern Detection
            detectBehavioralPatterns(input, result);

            // Stage 3: Payload Extraction
            extractPayloads(input, result);

            // Stage 4: Network Indicator Analysis
            analyzeNetworkIndicators(input, result);

            // Stage 5: Obfuscation Detection
            detectObfuscation(input, result);

            // Stage 6: C2 Pattern Detection
            detectC2Patterns(input, result);

            // Stage 7: Persistence Detection
            detectPersistenceMechanisms(input, result);

            // Stage 8: Risk Verdict
            generateSandboxVerdict(result);

            long elapsed = System.currentTimeMillis() - startTime;
            result.setAnalysisDurationMs(elapsed);
            result.setAnalysisSuccess(true);

            log.info(
                    "Sandbox complete: malicious={} score={} time={}ms",
                    result.isFlaggedMalicious(),
                    result.getSandboxRiskScore(),
                    elapsed
            );

        } catch (Exception e) {
            log.error("Sandbox error: {}", e.getMessage());
            result.setAnalysisSuccess(false);
            result.setErrorMessage(e.getMessage());
            result.setSandboxRiskScore(0);
            result.setFlaggedMalicious(false);
        }

        return result;
    }

    // ==========================================
    // STAGE 1: STATIC CONTENT ANALYSIS
    // ==========================================
    private void performStaticAnalysis(
            ThreatInput   input,
            SandboxResult result) {

        log.debug("Sandbox Stage 1: Static analysis");

        String content = buildContentString(input);

        if (content == null || content.isEmpty()) {
            result.addFinding("No content to analyze");
            return;
        }

        result.setContentLength(content.length());

        switch (input.getInputType()) {
            case URL:
                analyzeUrlStatically(input, result);
                break;
            case EMAIL_CONTENT:
                analyzeEmailStatically(input, result);
                break;
            case PDF_DOCUMENT:
                analyzeDocumentStatically(input, result);
                break;
            case IMAGE_SCREENSHOT:
                analyzeImageStatically(input, result);
                break;
            default:
                break;
        }
    }

    // ==========================================
    // URL STATIC ANALYSIS
    // ==========================================
    private void analyzeUrlStatically(
            ThreatInput   input,
            SandboxResult result) {

        String url = input.getRawUrl();
        if (url == null) return;

        try {
            String urlLower = url.toLowerCase();

            if (urlLower.startsWith("data:")) {
                result.addFinding("Data URI scheme detected");
                result.setHasDataUri(true);
            }

            if (urlLower.startsWith("javascript:")) {
                result.addFinding("JavaScript URI — can execute code");
                result.setHasJavaScriptUri(true);
            }

            URL parsedUrl = new URI(
                    url.startsWith("http") ? url : "http://" + url
            ).toURL();

            result.setExtractedDomain(parsedUrl.getHost());
            result.setExtractedPath(parsedUrl.getPath());
            result.setExtractedProtocol(parsedUrl.getProtocol());

            Matcher redirectMatcher = REDIRECT_PATTERN.matcher(url);
            if (redirectMatcher.find()) {
                result.addFinding(
                        "Redirect chain detected: " + redirectMatcher.group(2)
                );
                result.setHasRedirectChain(true);
                result.getEmbeddedUrls().add(redirectMatcher.group(2));
            }

            int port = parsedUrl.getPort();
            if (port > 0 && port != 80 && port != 443
                    && port != 8080 && port != 8443) {
                result.addFinding("Unusual port detected: " + port);
            }

        } catch (Exception e) {
            result.addFinding("URL parsing failed — possible malformed URL");
        }
    }

    // ==========================================
    // EMAIL STATIC ANALYSIS
    // ==========================================
    private void analyzeEmailStatically(
            ThreatInput   input,
            SandboxResult result) {

        String body    = input.getEmailBody();
        String subject = input.getEmailSubject();
        String sender  = input.getEmailSender();

        if (body == null) return;

        if (body.contains("<html") || body.contains("<a href")) {
            result.setContainsHtml(true);
            result.addFinding("HTML content in email body");
        }

        if (body.contains("<form") || body.contains("<input")) {
            result.addFinding(
                    "HTML form detected in email — credential harvesting risk"
            );
        }

        if (sender != null && sender.contains("@")) {
            String domain = sender.split("@")[1].toLowerCase();
            result.setExtractedDomain(domain);

            if (domain.endsWith("gmail.com")
                    || domain.endsWith("yahoo.com")
                    || domain.endsWith("hotmail.com")
                    || domain.endsWith("outlook.com")) {
                result.addFinding("Sender using free email: " + domain);
            }
        }

        Pattern b64Pattern = Pattern.compile("[A-Za-z0-9+/]{40,}={0,2}");
        if (b64Pattern.matcher(body).find()) {
            result.setHasBase64Content(true);
            result.addFinding("Base64 encoded content detected");
        }
    }

    // ==========================================
    // DOCUMENT STATIC ANALYSIS
    // ==========================================
    private void analyzeDocumentStatically(
            ThreatInput   input,
            SandboxResult result) {

        String text = input.getExtractedText();
        if (text == null || text.isEmpty()) {
            result.addFinding("No extractable text — possible encryption");
            return;
        }

        result.setContentLength(text.length());

        String textLower = text.toLowerCase();
        if (textLower.contains("sub ")
                || textLower.contains("function ")
                || textLower.contains("dim as string")
                || textLower.contains("createobject")) {
            result.setHasMacroCode(true);
            result.addFinding("VBA macro code detected");
        }

        if (textLower.contains("powershell")
                || textLower.contains("invoke-expression")
                || textLower.contains("iex(")) {
            result.setHasPowerShellCode(true);
            result.addFinding("PowerShell code detected");
        }

        if (textLower.contains("cmd.exe")
                || textLower.contains("command.com")
                || textLower.contains("shell(")
                || textLower.contains("wscript")) {
            result.addFinding("Shell command execution detected");
        }
    }

    // ==========================================
    // IMAGE STATIC ANALYSIS
    // ==========================================
    private void analyzeImageStatically(
            ThreatInput   input,
            SandboxResult result) {

        String ocrText   = input.getExtractedImageText();
        String imageName = input.getImageName();

        if (imageName != null) {
            result.addFinding("Image file analyzed: " + imageName);
        }

        if (ocrText != null && !ocrText.isEmpty()) {
            result.setContentLength(ocrText.length());
            result.addFinding(
                    "OCR text extracted: " + ocrText.length() + " characters"
            );
        }

        if (input.getImageBytes() != null) {
            byte[] bytes = input.getImageBytes();
            String header = new String(
                    Arrays.copyOf(bytes, Math.min(bytes.length, 200))
            );
            if (header.contains("<script") || header.contains("<?php")) {
                result.addFinding("Embedded code in image file");
            }
        }
    }

    // ==========================================
    // STAGE 2: BEHAVIORAL PATTERN DETECTION
    // ==========================================
    private void detectBehavioralPatterns(
            ThreatInput   input,
            SandboxResult result) {

        log.debug("Sandbox Stage 2: Behavioral");

        String content = buildContentString(input).toLowerCase();

        List<String> socialBehaviors = Arrays.asList(
                "act now", "urgent",
                "verify immediately",
                "account suspended",
                "click here",
                "limited time",
                "expires today",
                "confirm your identity",
                "update your information"
        );

        List<String> foundBehaviors = new ArrayList<>();
        for (String behavior : socialBehaviors) {
            if (content.contains(behavior)) {
                foundBehaviors.add(behavior);
            }
        }

        if (foundBehaviors.size() >= 2) {
            result.addFinding(
                    "Social engineering behaviors: "
                            + String.join(", ", foundBehaviors)
            );
            result.setSocialEngineeringDetected(true);
        }

        List<String> authorityTerms = Arrays.asList(
                "it department", "security team",
                "helpdesk", "system administrator",
                "tech support", "your bank",
                "irs", "fbi", "police"
        );

        for (String term : authorityTerms) {
            if (content.contains(term)) {
                result.addFinding("Authority impersonation: " + term);
                result.setAuthorityImpersonation(true);
                break;
            }
        }
    }

    // ==========================================
    // STAGE 3: PAYLOAD EXTRACTION
    // ==========================================
    private void extractPayloads(
            ThreatInput   input,
            SandboxResult result) {

        log.debug("Sandbox Stage 3: Payload extraction");

        String content = buildContentString(input);

        Matcher urlMatcher = URL_PATTERN.matcher(content);
        while (urlMatcher.find()) {
            result.getEmbeddedUrls().add(urlMatcher.group());
        }

        Matcher ipMatcher = IP_PATTERN.matcher(content);
        while (ipMatcher.find()) {
            result.getExtractedIps().add(ipMatcher.group());
        }

        Pattern filePattern = Pattern.compile(
                "[\\w\\-]+\\.(exe|bat|cmd|ps1|vbs|js|jar|msi|dll|sh|hta|wsf)",
                Pattern.CASE_INSENSITIVE
        );
        Matcher fileMatcher = filePattern.matcher(content);
        while (fileMatcher.find()) {
            result.getMaliciousFileRefs().add(fileMatcher.group());
            result.addFinding("Malicious file reference: " + fileMatcher.group());
        }

        if (!result.getEmbeddedUrls().isEmpty()) {
            result.addFinding(result.getEmbeddedUrls().size() + " URL(s) extracted");
        }
        if (!result.getExtractedIps().isEmpty()) {
            result.addFinding(
                    result.getExtractedIps().size() + " IP address(es) extracted"
            );
        }
    }

    // ==========================================
    // STAGE 4: NETWORK INDICATOR ANALYSIS
    // ==========================================
    private void analyzeNetworkIndicators(
            ThreatInput   input,
            SandboxResult result) {

        log.debug("Sandbox Stage 4: Network indicators");

        for (String url : result.getEmbeddedUrls()) {
            String urlLower = url.toLowerCase();

            List<String> suspiciousTlds = Arrays.asList(
                    ".tk", ".ml", ".ga", ".cf", ".gq",
                    ".xyz", ".top", ".click", ".download"
            );
            for (String tld : suspiciousTlds) {
                if (urlLower.contains(tld)) {
                    result.addFinding("Suspicious TLD in URL: " + tld);
                    break;
                }
            }

            if (urlLower.matches(
                    "https?://(\\d{1,3}\\.){3}\\d{1,3}.*")) {
                result.addFinding("IP-based URL detected: " + url);
                result.setHasIpBasedUrl(true);
            }

            if (urlLower.matches(".*:\\d{4,5}/.*")) {
                result.addFinding("Non-standard port in URL");
            }
        }

        for (String ip : result.getExtractedIps()) {
            if (ip.startsWith("192.168.")
                    || ip.startsWith("10.")
                    || ip.startsWith("172.16.")
                    || ip.startsWith("172.17.")
                    || ip.startsWith("172.18.")
                    || ip.equals("127.0.0.1")) {
                result.addFinding(
                        "Private IP detected: " + ip
                                + " — possible internal recon"
                );
            }
        }
    }

    // ==========================================
    // STAGE 5: OBFUSCATION DETECTION
    // ==========================================
    private void detectObfuscation(
            ThreatInput   input,
            SandboxResult result) {

        log.debug("Sandbox Stage 5: Obfuscation");

        String content = buildContentString(input).toLowerCase();

        List<String> foundObfuscation = new ArrayList<>();
        for (String indicator : OBFUSCATION_INDICATORS) {
            if (content.contains(indicator.toLowerCase())) {
                foundObfuscation.add(indicator);
            }
        }

        if (!foundObfuscation.isEmpty()) {
            result.setHasObfuscation(true);
            result.setObfuscationIndicators(foundObfuscation);
            result.addFinding(
                    "Obfuscation detected: " + String.join(", ", foundObfuscation)
            );
        }

        long percentCount = content.chars().filter(c -> c == '%').count();
        if (content.length() > 0
                && (double) percentCount / content.length() > 0.15) {
            result.setHasExcessiveEncoding(true);
            result.addFinding("Excessive URL encoding detected");
        }
    }

    // ==========================================
    // STAGE 6: C2 PATTERN DETECTION
    // ==========================================
    private void detectC2Patterns(
            ThreatInput   input,
            SandboxResult result) {

        log.debug("Sandbox Stage 6: C2 detection");

        String content = buildContentString(input).toLowerCase();

        List<String> foundC2 = new ArrayList<>();
        for (String pattern : C2_PATTERNS) {
            if (content.contains(pattern.toLowerCase())) {
                foundC2.add(pattern.trim());
            }
        }

        if (!foundC2.isEmpty()) {
            result.setHasC2Indicators(true);
            result.setC2Indicators(foundC2);
            result.addFinding(
                    "C2 communication indicators: " + String.join(", ", foundC2)
            );
        }
    }

    // ==========================================
    // STAGE 7: PERSISTENCE DETECTION
    // ==========================================
    private void detectPersistenceMechanisms(
            ThreatInput   input,
            SandboxResult result) {

        log.debug("Sandbox Stage 7: Persistence");

        String content = buildContentString(input).toLowerCase();

        List<String> foundPersistence = new ArrayList<>();
        for (String pattern : PERSISTENCE_PATTERNS) {
            if (content.contains(pattern.toLowerCase())) {
                foundPersistence.add(pattern.trim());
            }
        }

        if (!foundPersistence.isEmpty()) {
            result.setHasPersistenceMechanism(true);
            result.setPersistenceIndicators(foundPersistence);
            result.addFinding(
                    "Persistence mechanism: "
                            + String.join(", ", foundPersistence)
            );
        }

        List<String> foundPrivilege = new ArrayList<>();
        for (String pattern : PRIVILEGE_PATTERNS) {
            if (content.contains(pattern.toLowerCase())) {
                foundPrivilege.add(pattern.trim());
            }
        }

        if (!foundPrivilege.isEmpty()) {
            result.setHasPrivilegeEscalation(true);
            result.addFinding(
                    "Privilege escalation attempt: "
                            + String.join(", ", foundPrivilege)
            );
        }
    }

    // ==========================================
    // STAGE 8: GENERATE VERDICT
    // ==========================================
    private void generateSandboxVerdict(SandboxResult result) {

        log.debug("Sandbox Stage 8: Verdict");

        int score = 0;

        if (result.isHasObfuscation())          score += 25;
        if (result.isHasC2Indicators())          score += 35;
        if (result.isHasPersistenceMechanism())  score += 30;
        if (result.isHasPrivilegeEscalation())   score += 30;
        if (result.isHasMacroCode())             score += 20;
        if (result.isHasPowerShellCode())        score += 25;
        if (result.isHasDataUri())               score += 15;
        if (result.isHasJavaScriptUri())         score += 20;
        if (result.isHasBase64Content())         score += 10;
        if (result.isHasIpBasedUrl())            score += 15;
        if (result.isHasRedirectChain())         score += 10;
        if (result.isSocialEngineeringDetected()) score += 15;
        if (result.isAuthorityImpersonation())   score += 10;
        if (!result.getMaliciousFileRefs().isEmpty()) {
            score += result.getMaliciousFileRefs().size() * 10;
        }
        if (result.isHasExcessiveEncoding())     score += 10;
        if (result.getFindings().size() > 5)     score += 10;

        result.setSandboxRiskScore(Math.min(score, 100));
        result.setFlaggedMalicious(score >= 30);
        result.setVerdict(
                score >= 70 ? "MALICIOUS"
                        : score >= 40 ? "SUSPICIOUS"
                        : score >= 20 ? "POTENTIALLY_UNSAFE"
                        : "CLEAN"
        );
        result.setVerdictReason(buildVerdictReason(result, score));

        log.info("Sandbox verdict: {} score={}",
                result.getVerdict(), score);
    }

    // ==========================================
    // BUILD VERDICT REASON
    // ==========================================
    private String buildVerdictReason(
            SandboxResult result, int score) {

        StringBuilder sb = new StringBuilder();
        sb.append("Verdict: ").append(result.getVerdict())
                .append(" (score: ").append(score).append("). ");

        if (result.isHasC2Indicators())
            sb.append("C2 communication patterns found. ");
        if (result.isHasPersistenceMechanism())
            sb.append("Persistence mechanism detected. ");
        if (result.isHasObfuscation())
            sb.append("Code obfuscation detected. ");
        if (result.isHasMacroCode())
            sb.append("Macro/script code found. ");
        if (!result.getFindings().isEmpty())
            sb.append(result.getFindings().size()).append(" total findings.");

        return sb.toString().trim();
    }

    // ==========================================
    // BUILD CONTENT STRING
    // ==========================================
    private String buildContentString(ThreatInput input) {

        StringBuilder sb = new StringBuilder();

        if (input.getRawUrl() != null)
            sb.append(input.getRawUrl()).append(" ");
        if (input.getEmailSubject() != null)
            sb.append(input.getEmailSubject()).append(" ");
        if (input.getEmailBody() != null)
            sb.append(input.getEmailBody()).append(" ");
        if (input.getExtractedText() != null)
            sb.append(input.getExtractedText()).append(" ");
        if (input.getExtractedImageText() != null)
            sb.append(input.getExtractedImageText()).append(" ");
        if (input.getCombinedAnalysisText() != null)
            sb.append(input.getCombinedAnalysisText());

        return sb.toString();
    }

    // ==========================================
    // SANDBOX RESULT MODEL
    // ==========================================
    // FIX: Added @lombok.Builder so that @lombok.Builder.Default
    //      can generate the $default$xxx() methods for initialized
    //      list fields. Without @Builder present, Lombok has nowhere
    //      to attach the default-value factory methods, causing:
    //      "cannot find symbol: method $default$embeddedUrls()"
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SandboxResult {

        // Input info
        private String                  inputType;
        private java.time.LocalDateTime analyzedAt;
        private long                    analysisDurationMs;
        private int                     contentLength;

        // Extracted artifacts
        @lombok.Builder.Default
        private List<String> embeddedUrls = new ArrayList<>();

        @lombok.Builder.Default
        private List<String> extractedIps = new ArrayList<>();

        @lombok.Builder.Default
        private List<String> maliciousFileRefs = new ArrayList<>();

        // URL analysis
        private String  extractedDomain;
        private String  extractedPath;
        private String  extractedProtocol;
        private boolean hasDataUri;
        private boolean hasJavaScriptUri;
        private boolean hasRedirectChain;
        private boolean hasIpBasedUrl;

        // Content flags
        private boolean containsHtml;
        private boolean hasBase64Content;
        private boolean hasMacroCode;
        private boolean hasPowerShellCode;

        // Behavioral flags
        private boolean socialEngineeringDetected;
        private boolean authorityImpersonation;

        // Obfuscation
        private boolean      hasObfuscation;
        private boolean      hasExcessiveEncoding;
        private List<String> obfuscationIndicators;

        // C2 indicators
        private boolean      hasC2Indicators;
        private List<String> c2Indicators;

        // Persistence
        private boolean      hasPersistenceMechanism;
        private boolean      hasPrivilegeEscalation;
        private List<String> persistenceIndicators;

        // Verdict
        private boolean flaggedMalicious;
        private String  verdict;
        private String  verdictReason;
        private int     sandboxRiskScore;

        // Findings list
        @lombok.Builder.Default
        private List<String> findings = new ArrayList<>();

        // Metadata
        private boolean analysisSuccess;
        private String  errorMessage;

        // Helper
        public void addFinding(String finding) {
            if (this.findings == null) {
                this.findings = new ArrayList<>();
            }
            this.findings.add(finding);
        }
    }
}