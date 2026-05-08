package com.project.detection.pattern;

import com.project.intelligence.model.AnalysisResult;
import com.project.intelligence.model.ThreatInput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// ==========================================
// THREAT PATTERN MATCHER
// Maintains a signature library of known
// attack patterns and matches incoming
// threats against them for identification
// ==========================================
@Slf4j
@Component
public class ThreatPatternMatcher {

    // ==========================================
    // PATTERN SIGNATURE LIBRARY
    // Each pattern has:
    // - ID, Name, Category
    // - Required keywords (must match)
    // - Optional keywords (boost score)
    // - Minimum match threshold
    // - Severity weight
    // ==========================================
    private static final List<ThreatSignature>
            SIGNATURE_LIBRARY = buildSignatureLibrary();

    // ==========================================
    // MAIN MATCH METHOD
    // ==========================================
    public PatternMatchResult match(
            ThreatInput    input,
            AnalysisResult result) {

        log.info(
                "Pattern matcher starting for: {}",
                input.getInputType()
        );

        PatternMatchResult matchResult =
                new PatternMatchResult();

        try {
            // Build combined text for matching
            String analysisText =
                    buildAnalysisText(input, result);

            // Match against all signatures
            List<SignatureMatch> matches =
                    matchAllSignatures(
                            analysisText, result
                    );

            // Sort by similarity score
            matches.sort(
                    Comparator.comparingDouble(
                            SignatureMatch::getSimilarity
                    ).reversed()
            );

            // Set best match
            if (!matches.isEmpty()) {
                SignatureMatch best = matches.get(0);

                if (best.getSimilarity() >= 0.35) {
                    matchResult.setMatched(true);
                    matchResult.setPatternId(
                            best.getSignature()
                                    .getPatternId()
                    );
                    matchResult.setPatternName(
                            best.getSignature()
                                    .getName()
                    );
                    matchResult.setCategory(
                            best.getSignature()
                                    .getCategory()
                    );
                    matchResult.setSimilarity(
                            best.getSimilarity()
                    );
                    matchResult.setMatchedKeywords(
                            best.getMatchedKeywords()
                    );
                    matchResult.setSeverityWeight(
                            best.getSignature()
                                    .getSeverityWeight()
                    );
                    matchResult.setDescription(
                            best.getSignature()
                                    .getDescription()
                    );
                    matchResult.setMitigation(
                            best.getSignature()
                                    .getMitigation()
                    );

                    // All matches above threshold
                    matchResult.setAllMatches(
                            matches.stream()
                                    .filter(m ->
                                            m.getSimilarity()
                                                    >= 0.25
                                    )
                                    .collect(
                                            Collectors.toList()
                                    )
                    );

                    log.info(
                            "Pattern matched: {} "
                                    + "(similarity: {})",
                            best.getSignature()
                                    .getName(),
                            best.getSimilarity()
                    );
                } else {
                    matchResult.setMatched(false);
                    log.info(
                            "No pattern matched "
                                    + "(best: {})",
                            best.getSimilarity()
                    );
                }
            } else {
                matchResult.setMatched(false);
            }

            matchResult.setMatchSuccess(true);

        } catch (Exception e) {
            log.error(
                    "Pattern matching error: {}",
                    e.getMessage()
            );
            matchResult.setMatched(false);
            matchResult.setMatchSuccess(false);
        }

        return matchResult;
    }

    // ==========================================
    // MATCH ALL SIGNATURES
    // ==========================================
    private List<SignatureMatch> matchAllSignatures(
            String         text,
            AnalysisResult result) {

        List<SignatureMatch> matches =
                new ArrayList<>();
        String textLower = text.toLowerCase();

        for (ThreatSignature signature
                : SIGNATURE_LIBRARY) {

            // Count required keyword matches
            long requiredMatches =
                    signature.getRequiredKeywords()
                            .stream()
                            .filter(kw ->
                                    textLower.contains(
                                            kw.toLowerCase()
                                    )
                            )
                            .count();

            double requiredRatio =
                    (double) requiredMatches
                            / signature.getRequiredKeywords()
                            .size();

            // Must meet minimum threshold
            if (requiredRatio
                    < signature.getMinThreshold()) {
                continue;
            }

            // Count optional keyword matches
            long optionalMatches = 0;
            List<String> matchedOptional =
                    new ArrayList<>();

            if (!signature.getOptionalKeywords()
                    .isEmpty()) {
                for (String kw
                        : signature
                        .getOptionalKeywords()) {
                    if (textLower.contains(
                            kw.toLowerCase())) {
                        optionalMatches++;
                        matchedOptional.add(kw);
                    }
                }
            }

            // Calculate similarity score
            double optionalBoost = 0;
            if (!signature.getOptionalKeywords()
                    .isEmpty()) {
                optionalBoost =
                        (double) optionalMatches
                                / signature.getOptionalKeywords()
                                .size()
                                * 0.3;
            }

            double similarity =
                    (requiredRatio * 0.7)
                            + optionalBoost;

            // Apply red flag boost
            if (result.getRedFlags() != null) {
                String flagText = String.join(
                        " ", result.getRedFlags()
                ).toLowerCase();

                long flagMatches =
                        signature.getRequiredKeywords()
                                .stream()
                                .filter(kw ->
                                        flagText.contains(
                                                kw.toLowerCase()
                                        )
                                )
                                .count();

                if (flagMatches > 0) {
                    similarity = Math.min(
                            similarity + 0.1, 1.0
                    );
                }
            }

            // Collect all matched keywords
            List<String> allMatched =
                    new ArrayList<>();
            signature.getRequiredKeywords()
                    .stream()
                    .filter(kw ->
                            textLower.contains(
                                    kw.toLowerCase()
                            )
                    )
                    .forEach(allMatched::add);
            allMatched.addAll(matchedOptional);

            SignatureMatch match =
                    new SignatureMatch();
            match.setSignature(signature);
            match.setSimilarity(
                    Math.round(similarity * 100.0)
                            / 100.0
            );
            match.setMatchedKeywords(allMatched);
            matches.add(match);
        }

        return matches;
    }

    // ==========================================
    // BUILD ANALYSIS TEXT
    // ==========================================
    private String buildAnalysisText(
            ThreatInput    input,
            AnalysisResult result) {

        StringBuilder sb = new StringBuilder();

        // Add input content
        if (input.getRawUrl() != null) {
            sb.append(input.getRawUrl())
                    .append(" ");
        }
        if (input.getEmailSubject() != null) {
            sb.append(input.getEmailSubject())
                    .append(" ");
        }
        if (input.getEmailBody() != null) {
            sb.append(
                    input.getEmailBody()
                            .substring(
                                    0,
                                    Math.min(
                                            500,
                                            input.getEmailBody()
                                                    .length()
                                    )
                            )
            ).append(" ");
        }
        if (input.getExtractedText() != null) {
            sb.append(
                    input.getExtractedText()
                            .substring(
                                    0,
                                    Math.min(
                                            500,
                                            input.getExtractedText()
                                                    .length()
                                    )
                            )
            ).append(" ");
        }
        if (input.getCombinedAnalysisText()
                != null) {
            sb.append(
                    input.getCombinedAnalysisText()
            ).append(" ");
        }

        // Add red flags text
        if (result.getRedFlags() != null) {
            sb.append(
                    String.join(
                            " ", result.getRedFlags()
                    )
            );
        }

        return sb.toString();
    }

    // ==========================================
    // BUILD SIGNATURE LIBRARY
    // ==========================================
    private static List<ThreatSignature>
    buildSignatureLibrary() {

        List<ThreatSignature> lib =
                new ArrayList<>();

        // PTN-001: PayPal Phishing
        lib.add(ThreatSignature.builder()
                .patternId("PTN-001")
                .name("PayPal Phishing")
                .category(
                        AnalysisResult
                                .ThreatCategory.PHISHING
                )
                .description(
                        "Fake PayPal login page or email "
                                + "designed to steal credentials"
                )
                .mitigation(
                        "Do not click links. Go directly "
                                + "to paypal.com. Report to PayPal."
                )
                .requiredKeywords(Arrays.asList(
                        "paypal", "account", "verify"
                ))
                .optionalKeywords(Arrays.asList(
                        "login", "suspend", "security",
                        "urgent", "password", "confirm"
                ))
                .minThreshold(0.60)
                .severityWeight(0.85)
                .build()
        );

        // PTN-002: Banking Credential Theft
        lib.add(ThreatSignature.builder()
                .patternId("PTN-002")
                .name("Banking Credential Theft")
                .category(
                        AnalysisResult
                                .ThreatCategory.CREDENTIAL_THEFT
                )
                .description(
                        "Attempt to steal banking "
                                + "credentials via phishing"
                )
                .mitigation(
                        "Contact your bank directly. "
                                + "Change passwords immediately."
                )
                .requiredKeywords(Arrays.asList(
                        "bank", "account", "password"
                ))
                .optionalKeywords(Arrays.asList(
                        "verify", "urgent", "credential",
                        "login", "secure", "transfer"
                ))
                .minThreshold(0.60)
                .severityWeight(0.90)
                .build()
        );

        // PTN-003: Ransomware Delivery
        lib.add(ThreatSignature.builder()
                .patternId("PTN-003")
                .name("Ransomware Delivery")
                .category(
                        AnalysisResult
                                .ThreatCategory.RANSOMWARE
                )
                .description(
                        "Document or email delivering "
                                + "ransomware payload via macros"
                )
                .mitigation(
                        "Never enable macros. Disconnect "
                                + "from network. Contact IT."
                )
                .requiredKeywords(Arrays.asList(
                        "macro", "enable", "document"
                ))
                .optionalKeywords(Arrays.asList(
                        "invoice", "attachment",
                        "open", "content",
                        "protected", "encrypted"
                ))
                .minThreshold(0.60)
                .severityWeight(0.95)
                .build()
        );

        // PTN-004: CEO Fraud / BEC
        lib.add(ThreatSignature.builder()
                .patternId("PTN-004")
                .name("CEO Fraud / BEC")
                .category(
                        AnalysisResult
                                .ThreatCategory
                                .SOCIAL_ENGINEERING
                )
                .description(
                        "Business Email Compromise "
                                + "impersonating executives"
                )
                .mitigation(
                        "Verify via phone. Never wire "
                                + "funds without dual approval."
                )
                .requiredKeywords(Arrays.asList(
                        "wire", "transfer", "urgent"
                ))
                .optionalKeywords(Arrays.asList(
                        "ceo", "confidential",
                        "immediately", "payment",
                        "president", "boss", "director"
                ))
                .minThreshold(0.60)
                .severityWeight(0.88)
                .build()
        );

        // PTN-005: Gift Card Scam
        lib.add(ThreatSignature.builder()
                .patternId("PTN-005")
                .name("Gift Card Scam")
                .category(
                        AnalysisResult
                                .ThreatCategory
                                .SOCIAL_ENGINEERING
                )
                .description(
                        "Scam requesting purchase of "
                                + "gift cards as payment"
                )
                .mitigation(
                        "Legitimate organizations never "
                                + "request gift card payments."
                )
                .requiredKeywords(Arrays.asList(
                        "gift card", "urgent"
                ))
                .optionalKeywords(Arrays.asList(
                        "itunes", "google play",
                        "purchase", "send codes",
                        "amazon", "steam", "walmart"
                ))
                .minThreshold(0.50)
                .severityWeight(0.75)
                .build()
        );

        // PTN-006: Fake Microsoft Alert
        lib.add(ThreatSignature.builder()
                .patternId("PTN-006")
                .name("Fake Microsoft Security Alert")
                .category(
                        AnalysisResult
                                .ThreatCategory.PHISHING
                )
                .description(
                        "Fake Microsoft support scam "
                                + "to gain remote access"
                )
                .mitigation(
                        "Microsoft never calls unsolicited. "
                                + "Do not call the number shown."
                )
                .requiredKeywords(Arrays.asList(
                        "microsoft", "virus", "call"
                ))
                .optionalKeywords(Arrays.asList(
                        "windows", "support", "alert",
                        "infected", "hacked",
                        "toll free", "1-800"
                ))
                .minThreshold(0.60)
                .severityWeight(0.80)
                .build()
        );

        // PTN-007: Lottery Prize Scam
        lib.add(ThreatSignature.builder()
                .patternId("PTN-007")
                .name("Lottery / Prize Scam")
                .category(
                        AnalysisResult
                                .ThreatCategory
                                .SOCIAL_ENGINEERING
                )
                .description(
                        "Fake lottery or prize scam "
                                + "to steal personal information"
                )
                .mitigation(
                        "Never pay fees to claim prizes. "
                                + "Legitimate lotteries don't email."
                )
                .requiredKeywords(Arrays.asList(
                        "winner", "prize", "claim"
                ))
                .optionalKeywords(Arrays.asList(
                        "lottery", "million",
                        "congratulations",
                        "selected", "chosen",
                        "reward", "inheritance"
                ))
                .minThreshold(0.60)
                .severityWeight(0.70)
                .build()
        );

        // PTN-008: Crypto Investment Fraud
        lib.add(ThreatSignature.builder()
                .patternId("PTN-008")
                .name("Crypto Investment Fraud")
                .category(
                        AnalysisResult
                                .ThreatCategory
                                .SOCIAL_ENGINEERING
                )
                .description(
                        "Fraudulent cryptocurrency "
                                + "investment opportunity"
                )
                .mitigation(
                        "Guaranteed returns don't exist. "
                                + "Never invest based on emails."
                )
                .requiredKeywords(Arrays.asList(
                        "bitcoin", "invest", "return"
                ))
                .optionalKeywords(Arrays.asList(
                        "crypto", "guaranteed",
                        "profit", "ethereum",
                        "blockchain", "double",
                        "trading", "wallet"
                ))
                .minThreshold(0.60)
                .severityWeight(0.78)
                .build()
        );

        // PTN-009: Spear Phishing
        lib.add(ThreatSignature.builder()
                .patternId("PTN-009")
                .name("Spear Phishing Attack")
                .category(
                        AnalysisResult
                                .ThreatCategory.PHISHING
                )
                .description(
                        "Targeted phishing using "
                                + "personal information"
                )
                .mitigation(
                        "Verify sender via separate "
                                + "channel. Report to IT."
                )
                .requiredKeywords(Arrays.asList(
                        "credential", "login",
                        "verify"
                ))
                .optionalKeywords(Arrays.asList(
                        "account", "suspended",
                        "access", "confirm",
                        "identity", "unusual activity"
                ))
                .minThreshold(0.55)
                .severityWeight(0.88)
                .build()
        );

        // PTN-010: Malware Attachment
        lib.add(ThreatSignature.builder()
                .patternId("PTN-010")
                .name("Malware Email Attachment")
                .category(
                        AnalysisResult
                                .ThreatCategory.MALWARE
                )
                .description(
                        "Email with malicious attachment "
                                + "delivering malware payload"
                )
                .mitigation(
                        "Do not open attachments from "
                                + "unknown senders. Scan files."
                )
                .requiredKeywords(Arrays.asList(
                        "attachment", "open", "file"
                ))
                .optionalKeywords(Arrays.asList(
                        "invoice", "receipt",
                        "delivery", "package",
                        "document", "download",
                        "exe", "zip", "rar"
                ))
                .minThreshold(0.55)
                .severityWeight(0.85)
                .build()
        );

        // PTN-011: Data Exfiltration
        lib.add(ThreatSignature.builder()
                .patternId("PTN-011")
                .name("Data Exfiltration Attempt")
                .category(
                        AnalysisResult
                                .ThreatCategory.DATA_BREACH
                )
                .description(
                        "Attempt to extract sensitive "
                                + "data from organization"
                )
                .mitigation(
                        "Isolate affected systems. "
                                + "Alert security team immediately."
                )
                .requiredKeywords(Arrays.asList(
                        "data", "export", "sensitive"
                ))
                .optionalKeywords(Arrays.asList(
                        "credit card", "ssn",
                        "personal information",
                        "database", "download all",
                        "confidential"
                ))
                .minThreshold(0.55)
                .severityWeight(0.92)
                .build()
        );

        // PTN-012: Fake Login Page
        lib.add(ThreatSignature.builder()
                .patternId("PTN-012")
                .name("Fake Login Page")
                .category(
                        AnalysisResult
                                .ThreatCategory.CREDENTIAL_THEFT
                )
                .description(
                        "Cloned login page to harvest "
                                + "user credentials"
                )
                .mitigation(
                        "Check URL carefully. Use "
                                + "password manager. Enable 2FA."
                )
                .requiredKeywords(Arrays.asList(
                        "login", "password", "enter"
                ))
                .optionalKeywords(Arrays.asList(
                        "username", "email",
                        "sign in", "signin",
                        "remember me", "forgot",
                        "fake login", "password field"
                ))
                .minThreshold(0.55)
                .severityWeight(0.87)
                .build()
        );

        // PTN-013: Homograph Attack
        lib.add(ThreatSignature.builder()
                .patternId("PTN-013")
                .name("Homograph / Lookalike Attack")
                .category(
                        AnalysisResult
                                .ThreatCategory.PHISHING
                )
                .description(
                        "Uses lookalike characters to "
                                + "spoof legitimate domains"
                )
                .mitigation(
                        "Check domain carefully character "
                                + "by character. Use bookmarks."
                )
                .requiredKeywords(Arrays.asList(
                        "homograph", "unicode",
                        "lookalike"
                ))
                .optionalKeywords(Arrays.asList(
                        "domain", "brand", "spoof",
                        "impersonation", "fake"
                ))
                .minThreshold(0.55)
                .severityWeight(0.82)
                .build()
        );

        // PTN-014: Whaling Attack
        lib.add(ThreatSignature.builder()
                .patternId("PTN-014")
                .name("Whaling Attack (Executive)")
                .category(
                        AnalysisResult
                                .ThreatCategory.PHISHING
                )
                .description(
                        "Highly targeted phishing aimed "
                                + "at senior executives"
                )
                .mitigation(
                        "Verify all executive requests "
                                + "via secondary channel."
                )
                .requiredKeywords(Arrays.asList(
                        "executive", "ceo", "wire"
                ))
                .optionalKeywords(Arrays.asList(
                        "board", "chairman",
                        "president", "urgent",
                        "confidential", "transfer",
                        "immediately"
                ))
                .minThreshold(0.55)
                .severityWeight(0.93)
                .build()
        );

        // PTN-015: Smishing
        lib.add(ThreatSignature.builder()
                .patternId("PTN-015")
                .name("Smishing (SMS Phishing)")
                .category(
                        AnalysisResult
                                .ThreatCategory.PHISHING
                )
                .description(
                        "SMS-based phishing to steal "
                                + "credentials or deliver malware"
                )
                .mitigation(
                        "Do not click SMS links. "
                                + "Call official numbers directly."
                )
                .requiredKeywords(Arrays.asList(
                        "sms", "text", "click"
                ))
                .optionalKeywords(Arrays.asList(
                        "verify", "account",
                        "delivery", "package",
                        "prize", "bank"
                ))
                .minThreshold(0.55)
                .severityWeight(0.75)
                .build()
        );

        log.info(
                "Pattern library built: {} signatures",
                lib.size()
        );

        return lib;
    }

    // ==========================================
    // THREAT SIGNATURE MODEL
    // ==========================================
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ThreatSignature {
        private String                          patternId;
        private String                          name;
        private AnalysisResult.ThreatCategory   category;
        private String                          description;
        private String                          mitigation;
        private List<String>                    requiredKeywords;
        private List<String>                    optionalKeywords;
        private double                          minThreshold;
        private double                          severityWeight;
    }

    // ==========================================
    // SIGNATURE MATCH MODEL
    // ==========================================
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SignatureMatch {
        private ThreatSignature signature;
        private double          similarity;
        private List<String>    matchedKeywords;
    }

    // ==========================================
    // PATTERN MATCH RESULT MODEL
    // ==========================================
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PatternMatchResult {
        private boolean                     matched;
        private String                      patternId;
        private String                      patternName;
        private AnalysisResult.ThreatCategory category;
        private double                      similarity;
        private double                      severityWeight;
        private List<String>                matchedKeywords;
        private String                      description;
        private String                      mitigation;
        private List<SignatureMatch>         allMatches;
        private boolean                     matchSuccess;
    }
}
