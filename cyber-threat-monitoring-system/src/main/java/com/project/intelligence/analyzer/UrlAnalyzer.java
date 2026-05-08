package com.project.intelligence.analyzer;

import com.project.intelligence.model.AnalysisResult;
import com.project.intelligence.model.ThreatInput;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class UrlAnalyzer {

    private static final List<String>
            PHISHING_KEYWORDS = Arrays.asList(
            "login","signin","sign-in","verify",
            "verification","validate","account",
            "update","confirm","secure","security",
            "alert","bank","payment","paypal",
            "amazon","apple","microsoft","google",
            "facebook","instagram","password",
            "credential","wallet","urgent",
            "suspend","limited","click","access",
            "recovery"
    );

    private static final List<String>
            MALWARE_EXTENSIONS = Arrays.asList(
            ".exe",".bat",".cmd",".com",".scr",
            ".vbs",".js",".jar",".msi",".dll",
            ".ps1",".sh",".zip",".rar",".7z",
            ".tar",".iso",".img",".dmg"
    );

    private static final List<String>
            SUSPICIOUS_TLDS = Arrays.asList(
            ".tk",".ml",".ga",".cf",".gq",
            ".xyz",".top",".club",".online",
            ".site",".web",".info",".biz",
            ".link",".click",".download",".win"
    );

    private static final List<String>
            LEGITIMATE_BRANDS = Arrays.asList(
            "paypal","amazon","apple","microsoft",
            "google","facebook","instagram",
            "twitter","linkedin","netflix",
            "spotify","ebay","bank","chase",
            "wellsfargo","citibank","dropbox",
            "adobe"
    );

    private static final List<String>
            URL_SHORTENERS = Arrays.asList(
            "bit.ly","tinyurl.com","goo.gl",
            "t.co","ow.ly","buff.ly","short.link",
            "rb.gy","cutt.ly","is.gd","tiny.cc",
            "shorturl.at"
    );

    private static final List<String>
            FREE_HOSTING = Arrays.asList(
            "000webhostapp.com","netlify.app",
            "github.io","vercel.app","pages.dev",
            "web.app","firebaseapp.com",
            "herokuapp.com","glitch.me","repl.co",
            "weebly.com","wixsite.com"
    );

    private static final List<String>
            FINANCIAL_KEYWORDS = Arrays.asList(
            "bank","payment","pay","billing",
            "invoice","transfer","wire","checkout",
            "wallet","crypto","bitcoin","card",
            "credit","debit"
    );

    private static final List<String>
            LOGIN_KEYWORDS = Arrays.asList(
            "login","signin","sign-in","logon",
            "log-on","auth","authenticate",
            "session","password","credential",
            "enter"
    );

    private static final Pattern IP_PATTERN =
            Pattern.compile(
                    "^(\\d{1,3}\\.){3}\\d{1,3}$"
            );

    private static final Pattern URL_PATTERN =
            Pattern.compile(
                    "https?://[\\w\\-.]+(:\\d+)?"
                            + "(/[\\w\\-./?%&=#@]*)?",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern ENCODED_PATTERN =
            Pattern.compile("%[0-9A-Fa-f]{2}");

    private static final Pattern HOMOGRAPH_PATTERN =
            Pattern.compile(
                    "[\\u00C0-\\u024F\\u0370-\\u03FF"
                            + "\\u0400-\\u04FF]"
            );

    // ==========================================
    // MAIN METHOD — called by pipeline
    // ==========================================
    public AnalysisResult analyze(ThreatInput input) {

        AnalysisResult result = new AnalysisResult();
        result.setAnalyzedAt(LocalDateTime.now());
        result.setRedFlags(new ArrayList<>());

        try {
            UrlAnalysisOutput out = runAnalysis(input.getRawUrl(), input);
            mapResult(out, result);
        } catch (Exception e) {
            log.error("URL error: {}", e.getMessage());
            result.setRiskScore(30);
            result.setRiskLevel(AnalysisResult.RiskLevel.LOW);
        }
        return result;
    }

    private UrlAnalysisOutput runAnalysis(String rawUrl, ThreatInput input) {

        UrlAnalysisOutput out = UrlAnalysisOutput.builder()
                .rawUrl(rawUrl)
                .analyzedAt(LocalDateTime.now())
                .build();
        try {
            parseUrl(rawUrl, out);
            checkDomain(out);
            checkPath(out);
            checkBrand(out);
            checkPatterns(out);
            calcScore(out);
            extractUrls(rawUrl, out);
            out.setAnalysisSuccess(true);
        } catch (Exception e) {
            log.error("Internal URL error: {}", e.getMessage());
            out.setAnalysisSuccess(false);
            out.setErrorMessage(e.getMessage());
            out.setUrlRiskScore(30);
        }
        return out;
    }

    private void parseUrl(String rawUrl, UrlAnalysisOutput out) {
        try {
            String u = rawUrl.trim();
            if (!u.startsWith("http://") && !u.startsWith("https://")) {
                u = "http://" + u;
            }
            URL p = new URI(u).toURL();
            out.setNormalizedUrl(u);
            out.setProtocol(p.getProtocol());
            out.setDomain(p.getHost());
            out.setPort(p.getPort());
            out.setPath(p.getPath());
            out.setQuery(p.getQuery());
            out.setFragment(p.getRef());
            out.setUsesHttps("https".equals(p.getProtocol()));

            String host = p.getHost();
            if (host != null && host.contains(".")) {
                String[] pts = host.split("\\.");
                out.setTld("." + pts[pts.length - 1]);
                if (pts.length >= 2) {
                    out.setRegisteredDomain(
                            pts[pts.length - 2] + "." + pts[pts.length - 1]
                    );
                }
                out.setSubdomainCount(Math.max(0, pts.length - 2));
            }
        } catch (Exception e) {
            log.warn("Parse warning: {}", e.getMessage());
            out.setDomain(rawUrl);
            out.setParseError(true);
        }
    }

    private void checkDomain(UrlAnalysisOutput out) {
        String d = out.getDomain();
        if (d == null) return;
        String dl = d.toLowerCase();

        // FIX: use setIpAddress (Lombok strips 'is' prefix for boolean getters/setters)
        if (IP_PATTERN.matcher(d).matches()) {
            out.setIpAddress(true);
            out.addRedFlag("IP used as domain");
        }
        for (String s : URL_SHORTENERS) {
            if (dl.contains(s)) {
                out.setUrlShortener(true);
                out.addRedFlag("URL shortener: " + s);
                break;
            }
        }
        for (String h : FREE_HOSTING) {
            if (dl.contains(h)) {
                out.setFreeDomain(true);
                out.addRedFlag("Free hosting: " + h);
                break;
            }
        }
        String tld = out.getTld();
        if (tld != null) {
            for (String t : SUSPICIOUS_TLDS) {
                if (tld.equalsIgnoreCase(t)) {
                    out.setHasSuspiciousTld(true);
                    out.addRedFlag("Suspicious TLD: " + tld);
                    break;
                }
            }
        }
        if (d.length() > 40) {
            out.setHasLongDomain(true);
            out.addRedFlag("Long domain");
        }
        if (!out.isUsesHttps()) {
            out.addRedFlag("No HTTPS");
        }
        int port = out.getPort();
        if (port > 0 && port != 80 && port != 443
                && port != 8080 && port != 8443) {
            out.setHasSuspiciousPort(true);
            out.addRedFlag("Non-standard port: " + port);
        }
        if (HOMOGRAPH_PATTERN.matcher(d).find()) {
            out.setHasHomographChars(true);
            out.addRedFlag("Homograph chars");
        }
    }

    private void checkPath(UrlAnalysisOutput out) {
        String path  = out.getPath();
        String query = out.getQuery();
        String full  = out.getNormalizedUrl() != null
                ? out.getNormalizedUrl().toLowerCase() : "";
        if (path == null) return;
        String pl = path.toLowerCase();

        for (String kw : LOGIN_KEYWORDS) {
            if (pl.contains(kw) || full.contains(kw)) {
                out.setHasLoginKeywords(true);
                out.addRedFlag("Login keywords in URL");
                break;
            }
        }
        for (String kw : FINANCIAL_KEYWORDS) {
            if (full.contains(kw)) {
                out.setHasFinancialKeywords(true);
                out.addRedFlag("Financial keywords in URL");
                break;
            }
        }
        for (String ext : MALWARE_EXTENSIONS) {
            if (pl.endsWith(ext)
                    || (query != null && query.toLowerCase().contains(ext))) {
                out.setHasMaliciousExtension(true);
                out.addRedFlag("Malicious extension: " + ext);
                break;
            }
        }
        if (query != null) {
            Matcher m = ENCODED_PATTERN.matcher(query);
            int cnt = 0;
            while (m.find()) cnt++;
            if (cnt > 5) {
                out.setHasEncodedContent(true);
                out.addRedFlag("Heavily encoded params");
            }
            String ql = query.toLowerCase();
            if (ql.contains("redirect=") || ql.contains("url=")
                    || ql.contains("next=") || ql.contains("return=")
                    || ql.contains("goto=")) {
                out.setHasRedirectParam(true);
                out.addRedFlag("Redirect parameter");
            }
        }
        int kwCount = 0;
        List<String> matched = new ArrayList<>();
        for (String kw : PHISHING_KEYWORDS) {
            if (full.contains(kw)) {
                kwCount++;
                matched.add(kw);
            }
        }
        out.setPhishingKeywordCount(kwCount);
        out.setMatchedKeywords(matched);
        if (kwCount >= 2) {
            out.setHasPhishingKeywords(true);
            out.addRedFlag("Phishing keywords: " + String.join(",", matched));
        }
        long slashes = path.chars().filter(c -> c == '/').count();
        if (slashes > 5) {
            out.setHasDeepPath(true);
            out.addRedFlag("Deep path: " + slashes);
        }
    }

    private void checkBrand(UrlAnalysisOutput out) {
        String d = out.getDomain();
        if (d == null) return;
        String dl = d.toLowerCase();
        String rd = out.getRegisteredDomain() != null
                ? out.getRegisteredDomain().toLowerCase() : dl;

        for (String brand : LEGITIMATE_BRANDS) {
            if (dl.contains(brand)) {
                boolean real = rd.equals(brand + ".com")
                        || rd.equals(brand + ".net")
                        || rd.equals(brand + ".org");
                if (!real) {
                    out.setBrandImpersonation(true);
                    out.setImpersonatedBrand(brand);
                    out.addRedFlag("Brand impersonation: '" + brand + "'");
                    break;
                }
            }
        }
    }

    private void checkPatterns(UrlAnalysisOutput out) {
        String d = out.getDomain();
        if (d == null) return;
        String dl = d.toLowerCase();

        if (dl.contains("--")) {
            out.addRedFlag("Consecutive hyphens");
        }
        long dots = dl.chars().filter(c -> c == '.').count();
        if (dots > 4) {
            out.addRedFlag("Excessive dots: " + dots);
        }
        long digits = dl.chars().filter(Character::isDigit).count();
        if (dl.length() > 0 && (double) digits / dl.length() > 0.4) {
            out.addRedFlag("Digit-heavy domain");
        }
        out.setSuspiciousIndicatorCount(out.getRedFlags().size());
    }

    private void calcScore(UrlAnalysisOutput out) {
        int s = 0;
        Map<String, Integer> bd = new LinkedHashMap<>();

        if (out.isIpAddress()) {
            s += 20; bd.put("IP_AS_DOMAIN", 20);
        }
        if (out.isBrandImpersonation()) {
            s += 30; bd.put("BRAND_IMPERSONATION", 30);
        }
        if (out.isHasMaliciousExtension()) {
            s += 25; bd.put("MALICIOUS_EXT", 25);
        }
        if (!out.isUsesHttps()) {
            s += 8; bd.put("NO_HTTPS", 8);
        }
        if (out.isHasSuspiciousTld()) {
            s += 10; bd.put("SUSPICIOUS_TLD", 10);
        }
        if (out.isUrlShortener()) {
            s += 10; bd.put("URL_SHORTENER", 10);
        }
        if (out.isFreeDomain()) {
            s += 8; bd.put("FREE_HOSTING", 8);
        }
        if (out.isHasHomographChars()) {
            s += 18; bd.put("HOMOGRAPH", 18);
        }
        if (out.isHasPhishingKeywords()) {
            int kw = Math.min(out.getPhishingKeywordCount() * 5, 25);
            s += kw; bd.put("PHISHING_KW", kw);
        }
        if (out.isHasLoginKeywords()) {
            s += 15; bd.put("LOGIN_KW", 15);
        }
        if (out.isHasFinancialKeywords()) {
            s += 20; bd.put("FINANCIAL_KW", 20);
        }
        if (out.isHasEncodedContent()) {
            s += 8; bd.put("ENCODED", 8);
        }
        if (out.isHasRedirectParam()) {
            s += 10; bd.put("REDIRECT", 10);
        }
        if (out.isHasSuspiciousPort()) {
            s += 10; bd.put("SUSP_PORT", 10);
        }
        if (out.isHasDeepPath()) {
            s += 5; bd.put("DEEP_PATH", 5);
        }
        if (out.getSubdomainCount() >= 3) {
            s += 10; bd.put("SUBDOMAINS", 10);
        }
        if (out.isHasLongDomain()) {
            s += 5; bd.put("LONG_DOMAIN", 5);
        }
        out.setUrlRiskScore(Math.min(s, 100));
        out.setScoreBreakdown(bd);
        out.setRedFlagCount(out.getRedFlags() != null ? out.getRedFlags().size() : 0);
    }

    private void extractUrls(String rawUrl, UrlAnalysisOutput out) {
        List<String> emb = new ArrayList<>();
        Matcher m = URL_PATTERN.matcher(rawUrl);
        while (m.find()) {
            String f = m.group();
            if (!f.equals(rawUrl)) emb.add(f);
        }
        out.setEmbeddedUrls(emb);
    }

    private void mapResult(UrlAnalysisOutput out, AnalysisResult result) {
        result.setRiskScore(out.getUrlRiskScore());
        result.setRiskLevel(toLevel(out.getUrlRiskScore()));
        result.setConfidenceScore(0.80);
        result.setConfidencePercentage("80%");

        if (out.getRedFlags() != null) {
            result.setRedFlags(out.getRedFlags());
        }
        if (out.getScoreBreakdown() != null) {
            result.setScoreBreakdown(out.getScoreBreakdown());
        }
        if (out.getEmbeddedUrls() != null) {
            result.setExtractedUrls(out.getEmbeddedUrls());
        }
        if (out.isBrandImpersonation()) {
            result.setThreatCategory(AnalysisResult.ThreatCategory.PHISHING);
            result.setThreatDescription(
                    "Brand impersonation: '" + out.getImpersonatedBrand() + "'"
            );
        } else if (out.isHasMaliciousExtension()) {
            result.setThreatCategory(AnalysisResult.ThreatCategory.MALWARE);
        } else if (out.isHasLoginKeywords() || out.isHasPhishingKeywords()) {
            result.setThreatCategory(AnalysisResult.ThreatCategory.CREDENTIAL_THEFT);
        }
    }

    private AnalysisResult.RiskLevel toLevel(int score) {
        if (score >= 75) return AnalysisResult.RiskLevel.CRITICAL;
        if (score >= 55) return AnalysisResult.RiskLevel.HIGH;
        if (score >= 35) return AnalysisResult.RiskLevel.MEDIUM;
        if (score >= 15) return AnalysisResult.RiskLevel.LOW;
        return AnalysisResult.RiskLevel.NEGLIGIBLE;
    }

    // ==========================================
    // OUTPUT MODEL
    // ==========================================
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UrlAnalysisOutput {

        private String rawUrl;
        private String normalizedUrl;
        private String protocol;
        private String domain;
        private String registeredDomain;
        private String tld;
        private int    port;
        private String path;
        private String query;
        private String fragment;
        private Map<String, String> queryParameters;

        private boolean usesHttps;

        // FIX: Renamed from isIpAddress -> ipAddress
        // Lombok @Data generates: isIpAddress() getter + setIpAddress() setter
        private boolean ipAddress;

        // FIX: Renamed from isUrlShortener -> urlShortener
        private boolean urlShortener;

        // FIX: Renamed from isFreeDomain -> freeDomain
        private boolean freeDomain;

        private boolean hasSuspiciousTld;
        private boolean hasLongDomain;
        private boolean hasSuspiciousPort;
        private boolean hasHomographChars;
        private boolean parseError;
        private int     subdomainCount;

        private boolean hasLoginKeywords;
        private boolean hasFinancialKeywords;
        private boolean hasPhishingKeywords;
        private boolean hasMaliciousExtension;
        private boolean hasEncodedContent;
        private boolean hasRedirectParam;
        private boolean hasDeepPath;
        private boolean hasDataUri;
        private boolean hasJavaScriptUri;

        // FIX: Renamed from isBrandImpersonation -> brandImpersonation
        private boolean brandImpersonation;
        private String  impersonatedBrand;

        private int          phishingKeywordCount;
        private List<String> matchedKeywords;
        private List<String> embeddedUrls;

        private int                 urlRiskScore;
        private int                 redFlagCount;
        private int                 suspiciousIndicatorCount;
        private Map<String,Integer> scoreBreakdown;

        @Builder.Default
        private List<String> redFlags = new ArrayList<>();

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