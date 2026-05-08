package com.project.intelligence.analyzer;

import com.project.intelligence.model.AnalysisResult;
import com.project.intelligence.model.ThreatInput;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// ==========================================
// EMAIL CONTENT ANALYZER
// Deep NLP-style analysis of email content
// to detect phishing, social engineering,
// credential theft and malicious intent
// ==========================================
@Slf4j
@Component
public class EmailAnalyzer {

    // ==========================================
    // URGENCY AND PRESSURE PHRASES
    // ==========================================
    private static final List<String>
            URGENCY_PHRASES = Arrays.asList(
            "act now", "urgent", "immediately",
            "within 24 hours", "expires today",
            "limited time", "action required",
            "respond immediately",
            "time sensitive",
            "account will be closed",
            "account suspended", "suspended",
            "verify now", "confirm now",
            "last warning", "final notice",
            "your account has been",
            "unusual activity", "unauthorized",
            "security breach", "compromised",
            "click here immediately",
            "failure to respond",
            "do not ignore",
            "expiring soon",
            "act immediately",
            "your response is needed"
    );

    // ==========================================
    // CREDENTIAL HARVESTING PHRASES
    // ==========================================
    private static final List<String>
            CREDENTIAL_PHRASES = Arrays.asList(
            "enter your password",
            "confirm your password",
            "provide your username",
            "update your credentials",
            "verify your identity",
            "re-enter your details",
            "login credentials",
            "username and password",
            "reset your password",
            "enter your pin",
            "enter your otp",
            "provide your social security",
            "confirm your credit card",
            "enter card details",
            "bank account number",
            "provide your date of birth",
            "mother maiden name",
            "security question answer",
            "enter your ssn"
    );

    // ==========================================
    // FINANCIAL FRAUD PHRASES
    // ==========================================
    private static final List<String>
            FINANCIAL_PHRASES = Arrays.asList(
            "wire transfer", "send money",
            "gift card", "purchase gift cards",
            "itunes card", "google play card",
            "amazon gift card", "bitcoin",
            "cryptocurrency", "transfer funds",
            "bank transfer", "money order",
            "western union", "moneygram",
            "you have won", "lottery winner",
            "prize money", "claim your prize",
            "inheritance", "million dollars",
            "investment opportunity",
            "double your money",
            "guaranteed return"
    );

    // ==========================================
    // SOCIAL ENGINEERING PHRASES
    // ==========================================
    private static final List<String>
            SOCIAL_ENG_PHRASES = Arrays.asList(
            "dear customer", "dear user",
            "dear account holder",
            "dear valued member",
            "hello friend", "greetings",
            "this is a notification",
            "your account has been flagged",
            "we have detected",
            "suspicious login attempt",
            "click the link below",
            "click here to verify",
            "follow the link",
            "update your information",
            "confirm your account",
            "do not share this",
            "keep this confidential",
            "do not forward",
            "strictly confidential",
            "secret", "private matter"
    );

    // ==========================================
    // MALWARE DELIVERY PHRASES
    // ==========================================
    private static final List<String>
            MALWARE_PHRASES = Arrays.asList(
            "download the attachment",
            "open the attached file",
            "see attached document",
            "attached invoice",
            "attached receipt",
            "scan the qr code",
            "enable macros",
            "enable content",
            "allow editing",
            "install the software",
            "download and run",
            "execute the file",
            "unzip the file",
            "extract and run"
    );

    // ==========================================
    // LEGITIMATE SENDER DOMAINS
    // ==========================================
    private static final List<String>
            LEGITIMATE_DOMAINS = Arrays.asList(
            "@paypal.com",      "@amazon.com",
            "@apple.com",       "@microsoft.com",
            "@google.com",      "@facebook.com",
            "@instagram.com",   "@twitter.com",
            "@linkedin.com",    "@netflix.com",
            "@spotify.com",     "@ebay.com",
            "@chase.com",       "@wellsfargo.com",
            "@bankofamerica.com","@citibank.com",
            "@dropbox.com",     "@adobe.com",
            "@gov.in", "@gov.uk", "@gov.us",
            "@edu", "@ac.uk", "@ac.in"
    );

    // ==========================================
    // SUSPICIOUS SENDER PATTERNS
    // ==========================================
    private static final List<String>
            SUSPICIOUS_SENDER_PATTERNS =
            Arrays.asList(
                    "no-reply@",    "noreply@",
                    "do-not-reply@","donotreply@",
                    "security@",    "alert@",
                    "notification@","support@",
                    "admin@",       "helpdesk@",
                    "service@",     "account@",
                    "verify@",      "confirm@",
                    "update@",      "info@"
            );

    // ==========================================
    // REGEX PATTERNS
    // ==========================================
    private static final Pattern URL_PATTERN =
            Pattern.compile(
                    "https?://[\\w\\-.]+(:\\d+)?"
                            + "(/[\\w\\-./?%&=#@]*)?",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile(
                    "[a-zA-Z0-9._%+\\-]+"
                            + "@[a-zA-Z0-9.\\-]+"
                            + "\\.[a-zA-Z]{2,}",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern PHONE_PATTERN =
            Pattern.compile(
                    "(\\+?\\d{1,3}[\\s\\-]?)?"
                            + "(\\(?\\d{3}\\)?[\\s\\-]?)"
                            + "\\d{3}[\\s\\-]?\\d{4}"
            );

    private static final Pattern IP_PATTERN =
            Pattern.compile(
                    "\\b(\\d{1,3}\\.){3}\\d{1,3}\\b"
            );

    // ==========================================
    // MAIN ANALYZE METHOD
    // Called by IntelligencePipeline Stage 1
    // ==========================================
    public AnalysisResult analyze(
            ThreatInput input) {

        log.info(
                "Email Analyzer starting. Subject: {}",
                input.getEmailSubject()
        );

        AnalysisResult result =
                new AnalysisResult();
        result.setAnalyzedAt(LocalDateTime.now());
        result.setRedFlags(new ArrayList<>());

        try {
            // Run internal analysis
            EmailAnalysisOutput output =
                    analyzeEmail(
                            input.getEmailSubject(),
                            input.getEmailBody(),
                            input.getEmailSender()
                    );

            // Map to AnalysisResult
            mapToAnalysisResult(output, result);

            log.info(
                    "Email analysis complete: "
                            + "score={}",
                    result.getRiskScore()
            );

        } catch (Exception e) {
            log.error(
                    "Email analysis error: {}",
                    e.getMessage()
            );
            result.setRiskScore(25);
            result.setRiskLevel(
                    AnalysisResult.RiskLevel.LOW
            );
            result.getRedFlags().add(
                    "Analysis error: "
                            + e.getMessage()
            );
        }

        return result;
    }

    // ==========================================
    // INTERNAL ANALYSIS METHOD
    // ==========================================
    public EmailAnalysisOutput analyzeEmail(
            String subject,
            String body,
            String senderEmail) {

        // Use builder so @Builder.Default works
        EmailAnalysisOutput output =
                EmailAnalysisOutput.builder()
                        .subject(subject)
                        .senderEmail(senderEmail)
                        .analyzedAt(LocalDateTime.now())
                        .build();

        try {
            String fullText = buildFullText(
                    subject, body, senderEmail
            );
            output.setFullAnalyzedText(fullText);

            analyzeSender(senderEmail, output);
            analyzeSubject(subject, output);
            analyzeBody(body, output);
            detectUrgency(fullText, output);
            detectCredentialHarvesting(
                    fullText, output
            );
            detectFinancialFraud(fullText, output);
            detectSocialEngineering(
                    fullText, output
            );
            detectMalwareDelivery(fullText, output);
            extractArtifacts(fullText, output);
            performLinguisticAnalysis(
                    fullText, output
            );
            calculateEmailRiskScore(output);

            output.setAnalysisSuccess(true);
            log.info(
                    "Email internal analysis: score={}",
                    output.getEmailRiskScore()
            );

        } catch (Exception e) {
            log.error(
                    "Email internal error: {}",
                    e.getMessage()
            );
            output.setAnalysisSuccess(false);
            output.setErrorMessage(e.getMessage());
            output.setEmailRiskScore(25);
        }

        return output;
    }

    // ==========================================
    // STEP 1: SENDER ANALYSIS
    // ==========================================
    private void analyzeSender(
            String              sender,
            EmailAnalysisOutput output) {

        if (sender == null || sender.isEmpty()) {
            output.setHasNoSender(true);
            output.addRedFlag(
                    "No sender email address provided"
            );
            return;
        }

        String senderLower = sender.toLowerCase();

        // Suspicious sender prefix
        for (String pattern
                : SUSPICIOUS_SENDER_PATTERNS) {
            if (senderLower.startsWith(pattern)) {
                output.setHasSuspiciousSenderPrefix(
                        true
                );
                output.addRedFlag(
                        "Suspicious sender prefix: "
                                + pattern
                );
                break;
            }
        }

        // Brand impersonation in sender
        List<String> brands = Arrays.asList(
                "paypal", "amazon", "apple",
                "microsoft", "google", "facebook",
                "bank", "chase", "netflix"
        );

        for (String brand : brands) {
            if (senderLower.contains(brand)) {
                boolean isLegit = false;
                for (String legit
                        : LEGITIMATE_DOMAINS) {
                    if (senderLower.endsWith(
                            legit)) {
                        isLegit = true;
                        break;
                    }
                }
                if (!isLegit) {
                    // FIX: was setIsSenderImpersonation()
                    // field renamed to senderImpersonation
                    output.setSenderImpersonation(
                            true
                    );
                    output.setImpersonatedBrand(
                            brand
                    );
                    output.addRedFlag(
                            "Sender impersonates '"
                                    + brand + "': " + sender
                    );
                }
                break;
            }
        }

        // Free email provider
        List<String> freeProviders =
                Arrays.asList(
                        "@gmail.com",  "@yahoo.com",
                        "@hotmail.com","@outlook.com",
                        "@aol.com",    "@protonmail.com",
                        "@mail.com",   "@yandex.com"
                );
        for (String p : freeProviders) {
            if (senderLower.endsWith(p)) {
                output.setUsesFreeEmailProvider(
                        true
                );
                // FIX: was output.isIsSenderImpersonation()
                if (output.isSenderImpersonation()) {
                    output.addRedFlag(
                            "Brand impersonation via "
                                    + "free email provider"
                    );
                }
                break;
            }
        }

        // Random sender pattern
        String local =
                senderLower.contains("@")
                        ? senderLower.split("@")[0]
                        : senderLower;
        if (local.matches(
                ".*\\d{4,}.*"
                        + "|.*[a-z]{2}\\d{2}[a-z]{2}.*"
        )) {
            output.setHasRandomSenderPattern(true);
            output.addRedFlag(
                    "Randomly generated sender address"
            );
        }
    }

    // ==========================================
    // STEP 2: SUBJECT ANALYSIS
    // ==========================================
    private void analyzeSubject(
            String              subject,
            EmailAnalysisOutput output) {

        if (subject == null
                || subject.isEmpty()) {
            output.setHasEmptySubject(true);
            return;
        }

        String subjectLower =
                subject.toLowerCase();

        // All caps check
        long capsCount = subject.chars()
                .filter(Character::isUpperCase)
                .count();
        long letterCount = subject.chars()
                .filter(Character::isLetter)
                .count();
        if (letterCount > 5
                && (double) capsCount
                / letterCount > 0.6) {
            output.setHasAllCapsSubject(true);
            output.addRedFlag(
                    "ALL CAPS subject — pressure tactic"
            );
        }

        // Excessive exclamation
        long exclamCount = subject.chars()
                .filter(c -> c == '!').count();
        if (exclamCount > 2) {
            output.addRedFlag(
                    "Excessive '!' in subject: "
                            + exclamCount
            );
        }

        // Urgency in subject
        for (String phrase : URGENCY_PHRASES) {
            if (subjectLower.contains(phrase)) {
                output.setHasUrgentSubject(true);
                output.addRedFlag(
                        "Urgency in subject: '"
                                + phrase + "'"
                );
                break;
            }
        }

        // RE:/FWD: spoofing
        if (subjectLower.startsWith("re:")
                || subjectLower.startsWith("fwd:")
                || subjectLower.startsWith("fw:")) {
            output.setHasReplySpoof(true);
            output.addRedFlag(
                    "RE:/FW: prefix — "
                            + "possible reply chain spoofing"
            );
        }

        // Emoji check
        boolean hasEmoji = subject.codePoints()
                .anyMatch(cp ->
                        (cp >= 0x1F600 && cp <= 0x1F64F)
                                || (cp >= 0x1F300 && cp <= 0x1F5FF)
                                || (cp >= 0x1F680 && cp <= 0x1F6FF)
                );
        if (hasEmoji) {
            output.setHasEmojiInSubject(true);
        }

        output.setSubjectLength(subject.length());
    }

    // ==========================================
    // STEP 3: BODY CONTENT ANALYSIS
    // ==========================================
    private void analyzeBody(
            String              body,
            EmailAnalysisOutput output) {

        if (body == null || body.isEmpty()) {
            output.setHasEmptyBody(true);
            output.addRedFlag(
                    "Empty email body — suspicious"
            );
            return;
        }

        output.setBodyLength(body.length());

        // Short body with link
        if (body.length() < 200
                && URL_PATTERN.matcher(body)
                .find()) {
            // FIX: was setIsShortBodyWithLink()
            // field renamed to shortBodyWithLink
            output.setShortBodyWithLink(true);
            output.addRedFlag(
                    "Short body with URL — "
                            + "classic phishing pattern"
            );
        }

        // HTML content
        if (body.contains("<html")
                || body.contains("<a href")
                || body.contains("<form")
                || body.contains("<input")) {
            output.setContainsHtmlContent(true);
        }

        // Mismatched links
        Pattern linkPattern = Pattern.compile(
                "<a[^>]+href=[\"']([^\"']+)[\"']"
                        + "[^>]*>([^<]+)</a>",
                Pattern.CASE_INSENSITIVE
        );
        Matcher linkMatcher =
                linkPattern.matcher(body);
        while (linkMatcher.find()) {
            String href    = linkMatcher.group(1);
            String visible = linkMatcher.group(2);
            if (!href.contains(visible)
                    && visible.startsWith("http")) {
                output.setHasMismatchedLinks(true);
                output.addRedFlag(
                        "Mismatched link text and URL"
                );
                break;
            }
        }
    }

    // ==========================================
    // STEP 4: URGENCY DETECTION
    // ==========================================
    private void detectUrgency(
            String              text,
            EmailAnalysisOutput output) {

        String tl = text.toLowerCase();
        List<String> found = new ArrayList<>();
        for (String phrase : URGENCY_PHRASES) {
            if (tl.contains(phrase)) {
                found.add(phrase);
            }
        }
        output.setUrgencyPhraseCount(found.size());
        output.setFoundUrgencyPhrases(found);
        if (found.size() >= 2) {
            output.setHighUrgencyDetected(true);
            output.addRedFlag(
                    "High urgency: "
                            + found.size() + " pressure phrases"
            );
        }
    }

    // ==========================================
    // STEP 5: CREDENTIAL HARVESTING
    // ==========================================
    private void detectCredentialHarvesting(
            String              text,
            EmailAnalysisOutput output) {

        String tl = text.toLowerCase();
        List<String> found = new ArrayList<>();
        for (String phrase : CREDENTIAL_PHRASES) {
            if (tl.contains(phrase)) {
                found.add(phrase);
            }
        }
        output.setCredentialPhraseCount(
                found.size()
        );
        output.setFoundCredentialPhrases(found);
        if (!found.isEmpty()) {
            output.setCredentialHarvestingDetected(
                    true
            );
            output.addRedFlag(
                    "Credential harvesting: "
                            + String.join(", ", found)
            );
        }
    }

    // ==========================================
    // STEP 6: FINANCIAL FRAUD
    // ==========================================
    private void detectFinancialFraud(
            String              text,
            EmailAnalysisOutput output) {

        String tl = text.toLowerCase();
        List<String> found = new ArrayList<>();
        for (String phrase : FINANCIAL_PHRASES) {
            if (tl.contains(phrase)) {
                found.add(phrase);
            }
        }
        output.setFinancialPhrasesFound(found);
        if (found.size() >= 2) {
            output.setFinancialFraudDetected(true);
            output.addRedFlag(
                    "Financial fraud: "
                            + String.join(", ", found)
            );
        }

        // Currency amount check
        Pattern cp = Pattern.compile(
                "\\$\\d+[,.]?\\d*"
                        + "|USD\\s*\\d+"
                        + "|\\d+\\s*(dollars?|million|billion)",
                Pattern.CASE_INSENSITIVE
        );
        if (cp.matcher(text).find()) {
            output.setContainsCurrencyAmount(true);
        }
    }

    // ==========================================
    // STEP 7: SOCIAL ENGINEERING
    // ==========================================
    private void detectSocialEngineering(
            String              text,
            EmailAnalysisOutput output) {

        String tl = text.toLowerCase();
        List<String> found = new ArrayList<>();
        for (String phrase : SOCIAL_ENG_PHRASES) {
            if (tl.contains(phrase)) {
                found.add(phrase);
            }
        }
        output.setSocialEngPhrasesFound(found);
        if (found.size() >= 2) {
            output.setSocialEngineeringDetected(
                    true
            );
            output.addRedFlag(
                    "Social engineering: "
                            + found.size() + " indicators"
            );
        }

        // Generic greeting
        List<String> greetings = Arrays.asList(
                "dear customer", "dear user",
                "dear account holder",
                "dear valued member",
                "to whom it may concern",
                "dear sir/madam", "hello friend"
        );
        for (String g : greetings) {
            if (tl.contains(g)) {
                output.setHasGenericGreeting(true);
                output.addRedFlag(
                        "Generic greeting: '" + g + "'"
                );
                break;
            }
        }
    }

    // ==========================================
    // STEP 8: MALWARE DELIVERY
    // ==========================================
    private void detectMalwareDelivery(
            String              text,
            EmailAnalysisOutput output) {

        String tl = text.toLowerCase();
        List<String> found = new ArrayList<>();
        for (String phrase : MALWARE_PHRASES) {
            if (tl.contains(phrase)) {
                found.add(phrase);
            }
        }
        output.setMalwarePhrasesFound(found);
        if (!found.isEmpty()) {
            output.setMalwareDeliveryDetected(true);
            output.addRedFlag(
                    "Malware delivery: "
                            + String.join(", ", found)
            );
        }

        List<String> attachKws = Arrays.asList(
                "attached", "attachment",
                "see attached", "find attached",
                "enclosed", "document enclosed"
        );
        for (String kw : attachKws) {
            if (tl.contains(kw)) {
                output.setMentionsAttachment(true);
                break;
            }
        }
    }

    // ==========================================
    // STEP 9: EXTRACT ARTIFACTS
    // ==========================================
    private void extractArtifacts(
            String              text,
            EmailAnalysisOutput output) {

        List<String> urls = new ArrayList<>();
        Matcher um = URL_PATTERN.matcher(text);
        while (um.find()) {
            urls.add(um.group());
        }
        output.setExtractedUrls(urls);
        output.setUrlCount(urls.size());

        List<String> emails = new ArrayList<>();
        Matcher em = EMAIL_PATTERN.matcher(text);
        while (em.find()) {
            emails.add(em.group());
        }
        output.setExtractedEmails(emails);

        List<String> ips = new ArrayList<>();
        Matcher im = IP_PATTERN.matcher(text);
        while (im.find()) {
            ips.add(im.group());
        }
        output.setExtractedIps(ips);

        List<String> phones = new ArrayList<>();
        Matcher pm = PHONE_PATTERN.matcher(text);
        while (pm.find()) {
            phones.add(pm.group());
        }
        output.setExtractedPhones(phones);

        if (urls.size() > 3) {
            output.addRedFlag(
                    "Excessive URLs: " + urls.size()
            );
        }
    }

    // ==========================================
    // STEP 10: LINGUISTIC ANALYSIS
    // ==========================================
    private void performLinguisticAnalysis(
            String              text,
            EmailAnalysisOutput output) {

        if (text == null || text.isEmpty()) return;

        String[] words = text.trim().split("\\s+");
        output.setWordCount(words.length);

        String[] sentences = text.split("[.!?]+");
        output.setSentenceCount(sentences.length);

        double avgLen = Arrays.stream(words)
                .mapToInt(String::length)
                .average()
                .orElse(0);
        output.setAverageWordLength(avgLen);

        List<String> misspellings = Arrays.asList(
                "recieve", "seperate", "occured",
                "accomodation", "definately",
                "untill", "priviledge", "occurance",
                "correspondance", "existance",
                "beleive", "recieved",
                "truely", "necesary"
        );

        int spellingErrors = 0;
        String tl = text.toLowerCase();
        for (String m : misspellings) {
            if (tl.contains(m)) spellingErrors++;
        }
        output.setSpellingErrorCount(spellingErrors);
        if (spellingErrors >= 2) {
            output.addRedFlag(
                    "Spelling errors: "
                            + spellingErrors
            );
        }

        List<String> grammarPatterns =
                Arrays.asList(
                        "kindly do the needful",
                        "revert back", "please to",
                        "we are waiting your",
                        "attached herewith",
                        "do the needful",
                        "i want to inform you that",
                        "i am writing to you about"
                );
        for (String p : grammarPatterns) {
            if (tl.contains(p)) {
                output.setHasGrammarIssues(true);
                break;
            }
        }
    }

    // ==========================================
    // STEP 11: CALCULATE EMAIL RISK SCORE
    // ==========================================
    private void calculateEmailRiskScore(
            EmailAnalysisOutput output) {

        int score = 0;
        Map<String, Integer> bd =
                new LinkedHashMap<>();

        // FIX: was output.isIsSenderImpersonation()
        if (output.isSenderImpersonation()) {
            score += 30;
            bd.put("SENDER_IMPERSONATION", 30);
        }
        if (output
                .isCredentialHarvestingDetected()) {
            score += 30;
            bd.put("CREDENTIAL_HARVESTING", 30);
        }
        if (output.isMalwareDeliveryDetected()) {
            score += 25;
            bd.put("MALWARE_DELIVERY", 25);
        }
        if (output.isFinancialFraudDetected()) {
            score += 25;
            bd.put("FINANCIAL_FRAUD", 25);
        }
        if (output
                .isSocialEngineeringDetected()) {
            score += 20;
            bd.put("SOCIAL_ENGINEERING", 20);
        }
        if (output.isHighUrgencyDetected()) {
            score += 20;
            bd.put("HIGH_URGENCY", 20);
        }
        if (output.isHasGenericGreeting()) {
            score += 10;
            bd.put("GENERIC_GREETING", 10);
        }
        if (output
                .isHasSuspiciousSenderPrefix()) {
            score += 10;
            bd.put("SUSPICIOUS_SENDER", 10);
        }
        if (output.isHasAllCapsSubject()) {
            score += 5;
            bd.put("ALL_CAPS_SUBJECT", 5);
        }
        // FIX: was output.isIsShortBodyWithLink()
        if (output.isShortBodyWithLink()) {
            score += 15;
            bd.put("SHORT_BODY_WITH_LINK", 15);
        }
        if (output.isHasMismatchedLinks()) {
            score += 20;
            bd.put("MISMATCHED_LINKS", 20);
        }
        if (output.isHasReplySpoof()) {
            score += 10;
            bd.put("REPLY_SPOOF", 10);
        }
        if (output.getSpellingErrorCount() > 0) {
            int sp = Math.min(
                    output.getSpellingErrorCount()
                            * 5, 15
            );
            score += sp;
            bd.put("SPELLING_ERRORS", sp);
        }
        if (output.getUrlCount() > 3) {
            int us = Math.min(
                    (output.getUrlCount() - 3) * 5, 10
            );
            score += us;
            bd.put("EXCESSIVE_URLS", us);
        }
        if (output.isHasGrammarIssues()) {
            score += 5;
            bd.put("GRAMMAR_ISSUES", 5);
        }
        if (output.isContainsCurrencyAmount()
                && output
                .isFinancialFraudDetected()) {
            score += 5;
            bd.put("CURRENCY_WITH_FRAUD", 5);
        }

        output.setEmailRiskScore(
                Math.min(score, 100)
        );
        output.setScoreBreakdown(bd);
        output.setRedFlagCount(
                output.getRedFlags() != null
                        ? output.getRedFlags().size() : 0
        );

        log.info(
                "Email risk score: {} flags: {}",
                output.getEmailRiskScore(),
                output.getRedFlagCount()
        );
    }

    // ==========================================
    // MAP OUTPUT TO ANALYSIS RESULT
    // ==========================================
    private void mapToAnalysisResult(
            EmailAnalysisOutput output,
            AnalysisResult      result) {

        result.setRiskScore(
                output.getEmailRiskScore()
        );
        result.setRiskLevel(
                mapScoreToLevel(
                        output.getEmailRiskScore()
                )
        );
        result.setConfidenceScore(0.82);
        result.setConfidencePercentage("82%");

        if (output.getRedFlags() != null) {
            result.setRedFlags(
                    output.getRedFlags()
            );
        }
        if (output.getScoreBreakdown() != null) {
            result.setScoreBreakdown(
                    output.getScoreBreakdown()
            );
        }
        if (output.getExtractedUrls() != null) {
            result.setExtractedUrls(
                    output.getExtractedUrls()
            );
        }
        if (output.getExtractedEmails() != null) {
            result.setExtractedEmails(
                    output.getExtractedEmails()
            );
        }
        if (output.getExtractedIps() != null) {
            result.setExtractedIps(
                    output.getExtractedIps()
            );
        }

        // Threat category
        // FIX: was output.isIsSenderImpersonation()
        if (output.isSenderImpersonation()
                || output.isHighUrgencyDetected()) {
            result.setThreatCategory(
                    AnalysisResult
                            .ThreatCategory.PHISHING
            );
        } else if (
                output
                        .isCredentialHarvestingDetected()
        ) {
            result.setThreatCategory(
                    AnalysisResult
                            .ThreatCategory.CREDENTIAL_THEFT
            );
        } else if (
                output.isFinancialFraudDetected()) {
            result.setThreatCategory(
                    AnalysisResult
                            .ThreatCategory.SOCIAL_ENGINEERING
            );
        } else if (
                output.isMalwareDeliveryDetected()) {
            result.setThreatCategory(
                    AnalysisResult
                            .ThreatCategory.MALWARE
            );
        }

        result.setThreatDescription(
                "Email analysis: "
                        + output.getRedFlagCount()
                        + " red flag(s). "
                        // FIX: was output.isIsSenderImpersonation()
                        + (output.isSenderImpersonation()
                        ? "Sender impersonates '"
                        + output.getImpersonatedBrand()
                        + "'. "
                        : "")
        );
    }

    // ==========================================
    // HELPER: BUILD FULL TEXT
    // ==========================================
    private String buildFullText(
            String subject,
            String body,
            String sender) {

        StringBuilder sb = new StringBuilder();
        if (sender  != null) {
            sb.append(sender).append(" ");
        }
        if (subject != null) {
            sb.append(subject).append(" ");
        }
        if (body    != null) {
            sb.append(body);
        }
        return sb.toString();
    }

    // ==========================================
    // HELPER: SCORE TO RISK LEVEL
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
        }
        return AnalysisResult.RiskLevel.NEGLIGIBLE;
    }

    // ==========================================
    // EMAIL ANALYSIS OUTPUT MODEL
    // ==========================================
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailAnalysisOutput {

        private String subject;
        private String senderEmail;
        private String fullAnalyzedText;
        private int    bodyLength;
        private int    subjectLength;

        // Sender flags
        private boolean hasNoSender;
        private boolean hasSuspiciousSenderPrefix;
        // FIX: renamed from isSenderImpersonation -> senderImpersonation
        // Lombok on "isSenderImpersonation" generates:
        //   isIsSenderImpersonation() + setIsSenderImpersonation() — both wrong
        // Renamed to "senderImpersonation" generates correct:
        //   isSenderImpersonation()   + setSenderImpersonation()
        private boolean senderImpersonation;
        private boolean usesFreeEmailProvider;
        private boolean hasRandomSenderPattern;
        private String  impersonatedBrand;

        // Subject flags
        private boolean hasEmptySubject;
        private boolean hasAllCapsSubject;
        private boolean hasUrgentSubject;
        private boolean hasReplySpoof;
        private boolean hasEmojiInSubject;

        // Body flags
        private boolean hasEmptyBody;
        // FIX: renamed from isShortBodyWithLink -> shortBodyWithLink
        // Same Lombok is-prefix rule:
        //   "isShortBodyWithLink" → isIsShortBodyWithLink() broken
        //   "shortBodyWithLink"   → isShortBodyWithLink()   correct
        private boolean shortBodyWithLink;
        private boolean containsHtmlContent;
        private boolean hasMismatchedLinks;
        private boolean mentionsAttachment;

        // Urgency
        private boolean      highUrgencyDetected;
        private int          urgencyPhraseCount;
        private List<String> foundUrgencyPhrases;

        // Credential harvesting
        private boolean      credentialHarvestingDetected;
        private int          credentialPhraseCount;
        private List<String> foundCredentialPhrases;

        // Financial fraud
        private boolean      financialFraudDetected;
        private List<String> financialPhrasesFound;
        private boolean      containsCurrencyAmount;

        // Social engineering
        private boolean      socialEngineeringDetected;
        private List<String> socialEngPhrasesFound;
        private boolean      hasGenericGreeting;

        // Malware delivery
        private boolean      malwareDeliveryDetected;
        private List<String> malwarePhrasesFound;

        // Linguistic
        private int     wordCount;
        private int     sentenceCount;
        private double  averageWordLength;
        private int     spellingErrorCount;
        private boolean hasGrammarIssues;

        // Extracted artifacts
        private List<String> extractedUrls;
        private List<String> extractedEmails;
        private List<String> extractedIps;
        private List<String> extractedPhones;
        private int          urlCount;

        // Risk output
        private int                  emailRiskScore;
        private int                  redFlagCount;
        private Map<String, Integer> scoreBreakdown;

        @Builder.Default
        private List<String> redFlags =
                new ArrayList<>();

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