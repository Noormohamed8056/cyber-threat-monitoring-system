package com.project.service;

import com.project.entity.CyberAlert;
import com.project.entity.IncidentReport;
import com.project.entity.User;
import com.project.repository.CyberAlertRepository;
import com.project.repository.IncidentReportRepository;
import com.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminAnalyticsService {

    private final IncidentReportRepository incidentReportRepository;
    private final CyberAlertRepository cyberAlertRepository;
    private final UserRepository userRepository;

    public GraphData getGraphData() {
        List<IncidentReport> reports = incidentReportRepository.findAll();
        List<CyberAlert> alerts = cyberAlertRepository.findAll();
        List<User> users = userRepository.findAll();

        return new GraphData(
                buildThreatAnalysis(reports),
                buildAnalyticsDashboard(reports, alerts, users)
        );
    }

    private Map<String, Object> buildThreatAnalysis(List<IncidentReport> reports) {
        Map<String, Object> data = new HashMap<>();
        data.put("weeklyTrendData", buildWeeklyTrendData(reports));
        data.put("incidentTypeData", buildIncidentTypeData(reports));
        data.put("riskScoreData", buildRiskScoreData(reports));
        data.put("monthlyData", buildMonthlyRiskData(reports));
        data.put("radarData", buildRadarData(reports));
        data.put("responseTimeData", buildResponseTimeData(reports));
        return data;
    }

    private Map<String, Object> buildAnalyticsDashboard(
            List<IncidentReport> reports,
            List<CyberAlert> alerts,
            List<User> users) {
        Map<String, Object> data = new HashMap<>();
        data.put("userGrowthData", buildUserGrowthData(users));
        data.put("reportActivityData", buildReportActivityData(reports));
        data.put("alertEffectivenessData", buildAlertEffectivenessData(alerts));
        data.put("institutionData", buildInstitutionData(reports));
        data.put("systemHealthData", buildSystemHealthData(reports, alerts));
        data.put("topThreatsData", buildTopThreatsData(reports));
        return data;
    }

    private List<Map<String, Object>> buildWeeklyTrendData(List<IncidentReport> reports) {
        List<Map<String, Object>> points = new ArrayList<>();
        String[] dayLabels = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            long submitted = reports.stream()
                    .filter(r -> sameDate(r.getCreatedAt(), date))
                    .count();
            long verified = reports.stream()
                    .filter(r -> r.getStatus() == IncidentReport.ReportStatus.VERIFIED)
                    .filter(r -> sameDate(coalesce(r.getVerifiedAt(), r.getUpdatedAt()), date))
                    .count();
            long dismissed = reports.stream()
                    .filter(r -> r.getStatus() == IncidentReport.ReportStatus.DISMISSED)
                    .filter(r -> sameDate(coalesce(r.getVerifiedAt(), r.getUpdatedAt()), date))
                    .count();

            Map<String, Object> point = new HashMap<>();
            point.put("day", dayLabels[date.getDayOfWeek().getValue() % 7]);
            point.put("reports", submitted);
            point.put("verified", verified);
            point.put("dismissed", dismissed);
            points.add(point);
        }

        return points;
    }

    private List<Map<String, Object>> buildIncidentTypeData(List<IncidentReport> reports) {
        Map<String, Long> counts = reports.stream()
                .filter(r -> r.getIncidentType() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getIncidentType().name(),
                        Collectors.counting()
                ));

        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        if (total == 0) {
            total = 1;
        }

        List<Map<String, Object>> data = new ArrayList<>();
        data.add(incidentTypePoint("Phishing", counts.getOrDefault("PHISHING_EMAIL", 0L), total, "#ff4757"));
        data.add(incidentTypePoint("Malware", counts.getOrDefault("MALWARE", 0L), total, "#ffd32a"));
        data.add(incidentTypePoint("Ransomware", counts.getOrDefault("RANSOMWARE", 0L), total, "#a55eea"));
        data.add(incidentTypePoint("Data Breach", counts.getOrDefault("DATA_BREACH", 0L), total, "#00d4ff"));
        data.add(incidentTypePoint("Unauth Access", counts.getOrDefault("UNAUTHORIZED_ACCESS", 0L), total, "#00ff88"));
        long other = counts.getOrDefault("SUSPICIOUS_URL", 0L)
                + counts.getOrDefault("SOCIAL_ENGINEERING", 0L)
                + counts.getOrDefault("OTHER", 0L);
        data.add(incidentTypePoint("Other", other, total, "#ff6b35"));

        return data;
    }

    private Map<String, Object> incidentTypePoint(String name, long count, long total, String color) {
        Map<String, Object> point = new HashMap<>();
        long percent = Math.round((count * 100.0) / total);
        point.put("name", name);
        point.put("value", percent);
        point.put("color", color);
        return point;
    }

    private List<Map<String, Object>> buildRiskScoreData(List<IncidentReport> reports) {
        long b1 = reports.stream().filter(r -> inRange(r.getRiskScore(), 0, 20)).count();
        long b2 = reports.stream().filter(r -> inRange(r.getRiskScore(), 21, 40)).count();
        long b3 = reports.stream().filter(r -> inRange(r.getRiskScore(), 41, 60)).count();
        long b4 = reports.stream().filter(r -> inRange(r.getRiskScore(), 61, 80)).count();
        long b5 = reports.stream().filter(r -> inRange(r.getRiskScore(), 81, 100)).count();

        List<Map<String, Object>> data = new ArrayList<>();
        data.add(riskBucket("0-20", b1));
        data.add(riskBucket("21-40", b2));
        data.add(riskBucket("41-60", b3));
        data.add(riskBucket("61-80", b4));
        data.add(riskBucket("81-100", b5));
        return data;
    }

    private Map<String, Object> riskBucket(String range, long count) {
        Map<String, Object> point = new HashMap<>();
        point.put("range", range);
        point.put("count", count);
        return point;
    }

    private List<Map<String, Object>> buildMonthlyRiskData(List<IncidentReport> reports) {
        Map<YearMonth, long[]> byMonth = new HashMap<>();
        for (IncidentReport r : reports) {
            if (r.getCreatedAt() == null || r.getRiskLevel() == null) {
                continue;
            }
            YearMonth ym = YearMonth.from(r.getCreatedAt());
            long[] arr = byMonth.computeIfAbsent(ym, k -> new long[3]);
            switch (r.getRiskLevel()) {
                case LOW:
                    arr[2]++;
                    break;
                case MEDIUM:
                    arr[1]++;
                    break;
                case HIGH:
                case CRITICAL:
                    arr[0]++;
                    break;
                default:
                    break;
            }
        }

        List<Map<String, Object>> data = new ArrayList<>();
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);
        YearMonth now = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = now.minusMonths(i);
            long[] arr = byMonth.getOrDefault(ym, new long[3]);
            Map<String, Object> point = new HashMap<>();
            point.put("month", ym.atDay(1).format(monthFmt));
            point.put("high", arr[0]);
            point.put("medium", arr[1]);
            point.put("low", arr[2]);
            data.add(point);
        }
        return data;
    }

    private List<Map<String, Object>> buildRadarData(List<IncidentReport> reports) {
        Map<String, Long> counts = reports.stream()
                .filter(r -> r.getIncidentType() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getIncidentType().name(),
                        Collectors.counting()
                ));
        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        if (total == 0) {
            total = 1;
        }

        List<Map<String, Object>> data = new ArrayList<>();
        data.add(radarPoint("Phishing", pct(counts.getOrDefault("PHISHING_EMAIL", 0L), total)));
        data.add(radarPoint("Malware", pct(counts.getOrDefault("MALWARE", 0L), total)));
        data.add(radarPoint("Ransomware", pct(counts.getOrDefault("RANSOMWARE", 0L), total)));
        data.add(radarPoint("Social Eng", pct(counts.getOrDefault("SOCIAL_ENGINEERING", 0L), total)));
        data.add(radarPoint("Data Breach", pct(counts.getOrDefault("DATA_BREACH", 0L), total)));
        data.add(radarPoint("WiFi Threats", pct(counts.getOrDefault("UNAUTHORIZED_ACCESS", 0L), total)));
        return data;
    }

    private Map<String, Object> radarPoint(String subject, long value) {
        Map<String, Object> point = new HashMap<>();
        point.put("subject", subject);
        point.put("A", value);
        return point;
    }

    private List<Map<String, Object>> buildResponseTimeData(List<IncidentReport> reports) {
        LocalDate weekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        List<Map<String, Object>> data = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            LocalDate start = weekStart.minusWeeks(i);
            LocalDate end = start.plusDays(6);
            List<IncidentReport> bucket = reports.stream()
                    .filter(r -> r.getVerifiedAt() != null)
                    .filter(r -> isBetweenDates(r.getVerifiedAt().toLocalDate(), start, end))
                    .collect(Collectors.toList());

            double avgHours = 0.0;
            if (!bucket.isEmpty()) {
                avgHours = bucket.stream()
                        .mapToDouble(r -> {
                            LocalDateTime from = coalesce(r.getCreatedAt(), r.getUpdatedAt());
                            LocalDateTime to = r.getVerifiedAt();
                            if (from == null || to == null || to.isBefore(from)) {
                                return 0.0;
                            }
                            return Duration.between(from, to).toMinutes() / 60.0;
                        })
                        .average()
                        .orElse(0.0);
            }

            Map<String, Object> point = new HashMap<>();
            point.put("week", "W" + (6 - i));
            point.put("avgTime", round1(avgHours));
            data.add(point);
        }

        return data;
    }

    private List<Map<String, Object>> buildUserGrowthData(List<User> users) {
        List<Map<String, Object>> data = new ArrayList<>();
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);
        YearMonth now = YearMonth.now();

        for (int i = 6; i >= 0; i--) {
            YearMonth ym = now.minusMonths(i);
            LocalDateTime endOfMonth = ym.atEndOfMonth().atTime(23, 59, 59);

            long students = users.stream()
                    .filter(u -> u.getRole() == User.Role.STUDENT)
                    .filter(u -> u.getCreatedAt() != null && !u.getCreatedAt().isAfter(endOfMonth))
                    .count();
            long admins = users.stream()
                    .filter(u -> u.getRole() == User.Role.ADMIN)
                    .filter(u -> u.getCreatedAt() != null && !u.getCreatedAt().isAfter(endOfMonth))
                    .count();

            Map<String, Object> point = new HashMap<>();
            point.put("month", ym.atDay(1).format(monthFmt));
            point.put("students", students);
            point.put("admins", admins);
            data.add(point);
        }

        return data;
    }

    private List<Map<String, Object>> buildReportActivityData(List<IncidentReport> reports) {
        LocalDate weekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        List<Map<String, Object>> data = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            LocalDate start = weekStart.minusWeeks(i);
            LocalDate end = start.plusDays(6);

            long submitted = reports.stream()
                    .filter(r -> sameDateRange(r.getCreatedAt(), start, end))
                    .count();

            long reviewed = reports.stream()
                    .filter(r -> r.getStatus() != IncidentReport.ReportStatus.PENDING)
                    .filter(r -> sameDateRange(coalesce(r.getUpdatedAt(), r.getVerifiedAt()), start, end))
                    .count();

            long resolved = reports.stream()
                    .filter(r -> r.getStatus() == IncidentReport.ReportStatus.VERIFIED
                            || r.getStatus() == IncidentReport.ReportStatus.DISMISSED)
                    .filter(r -> sameDateRange(coalesce(r.getVerifiedAt(), r.getUpdatedAt()), start, end))
                    .count();

            Map<String, Object> point = new HashMap<>();
            point.put("week", "W" + (6 - i));
            point.put("submitted", submitted);
            point.put("reviewed", reviewed);
            point.put("resolved", resolved);
            data.add(point);
        }

        return data;
    }

    private List<Map<String, Object>> buildAlertEffectivenessData(List<CyberAlert> alerts) {
        List<Map<String, Object>> data = new ArrayList<>();
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);
        YearMonth now = YearMonth.now();

        for (int i = 5; i >= 0; i--) {
            YearMonth ym = now.minusMonths(i);
            long published = alerts.stream()
                    .filter(a -> a.getCreatedAt() != null)
                    .filter(a -> YearMonth.from(a.getCreatedAt()).equals(ym))
                    .count();
            long withdrawn = alerts.stream()
                    .filter(a -> a.getStatus() == CyberAlert.AlertStatus.WITHDRAWN)
                    .filter(a -> {
                        LocalDateTime changedAt = coalesce(a.getUpdatedAt(), a.getCreatedAt());
                        return changedAt != null && YearMonth.from(changedAt).equals(ym);
                    })
                    .count();

            Map<String, Object> point = new HashMap<>();
            point.put("month", ym.atDay(1).format(monthFmt));
            point.put("published", published);
            point.put("withdrawn", withdrawn);
            data.add(point);
        }

        return data;
    }

    private List<Map<String, Object>> buildInstitutionData(List<IncidentReport> reports) {
        Map<String, Long> counts = reports.stream()
                .map(r -> r.getReportedBy() != null ? r.getReportedBy().getInstitutionName() : null)
                .filter(name -> name != null && !name.isBlank())
                .collect(Collectors.groupingBy(name -> name, Collectors.counting()));

        List<Map.Entry<String, Long>> sorted = counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toList());

        String[] colors = {"#00d4ff", "#00ff88", "#ffd32a", "#ff4757", "#a55eea"};
        List<Map<String, Object>> data = new ArrayList<>();

        long others = 0L;
        for (int i = 0; i < sorted.size(); i++) {
            Map.Entry<String, Long> e = sorted.get(i);
            if (i < 4) {
                Map<String, Object> point = new HashMap<>();
                point.put("name", e.getKey());
                point.put("reports", e.getValue());
                point.put("color", colors[i]);
                data.add(point);
            } else {
                others += e.getValue();
            }
        }

        if (others > 0 || data.isEmpty()) {
            Map<String, Object> otherPoint = new HashMap<>();
            otherPoint.put("name", "Others");
            otherPoint.put("reports", others);
            otherPoint.put("color", colors[4]);
            data.add(otherPoint);
        }

        return data;
    }

    private List<Map<String, Object>> buildSystemHealthData(
            List<IncidentReport> reports,
            List<CyberAlert> alerts) {
        String[] labels = {"00:00", "04:00", "08:00", "12:00", "16:00", "20:00", "23:59"};
        int[] bucketStartHours = {0, 4, 8, 12, 16, 20, 23};
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> data = new ArrayList<>();

        for (int i = 0; i < labels.length; i++) {
            int hour = bucketStartHours[i];
            int nextHour = (i < labels.length - 1) ? bucketStartHours[i + 1] : 24;

            long reportEvents = reports.stream()
                    .filter(r -> r.getCreatedAt() != null && r.getCreatedAt().toLocalDate().equals(today))
                    .filter(r -> inHourRange(r.getCreatedAt().getHour(), hour, nextHour))
                    .count();

            long alertEvents = alerts.stream()
                    .filter(a -> a.getCreatedAt() != null && a.getCreatedAt().toLocalDate().equals(today))
                    .filter(a -> inHourRange(a.getCreatedAt().getHour(), hour, nextHour))
                    .count();

            double avgMinutes = reports.stream()
                    .filter(r -> r.getVerifiedAt() != null && r.getCreatedAt() != null)
                    .filter(r -> r.getVerifiedAt().toLocalDate().equals(today))
                    .filter(r -> inHourRange(r.getVerifiedAt().getHour(), hour, nextHour))
                    .mapToDouble(r -> Math.max(0, Duration.between(r.getCreatedAt(), r.getVerifiedAt()).toMinutes()))
                    .average()
                    .orElse(120.0);

            long totalEvents = reportEvents + alertEvents;
            double uptime = totalEvents > 0 ? 100.0 : 99.0;

            Map<String, Object> point = new HashMap<>();
            point.put("time", labels[i]);
            point.put("uptime", uptime);
            point.put("response", Math.round(avgMinutes));
            data.add(point);
        }

        return data;
    }

    private List<Map<String, Object>> buildTopThreatsData(List<IncidentReport> reports) {
        Map<IncidentReport.IncidentType, Long> total = reports.stream()
                .filter(r -> r.getIncidentType() != null)
                .collect(Collectors.groupingBy(
                        IncidentReport::getIncidentType,
                        Collectors.counting()
                ));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last30 = now.minusDays(30);
        LocalDateTime prev30 = now.minusDays(60);

        Map<IncidentReport.IncidentType, Long> current = reports.stream()
                .filter(r -> r.getIncidentType() != null && r.getCreatedAt() != null)
                .filter(r -> !r.getCreatedAt().isBefore(last30))
                .collect(Collectors.groupingBy(
                        IncidentReport::getIncidentType,
                        Collectors.counting()
                ));

        Map<IncidentReport.IncidentType, Long> previous = reports.stream()
                .filter(r -> r.getIncidentType() != null && r.getCreatedAt() != null)
                .filter(r -> !r.getCreatedAt().isBefore(prev30) && r.getCreatedAt().isBefore(last30))
                .collect(Collectors.groupingBy(
                        IncidentReport::getIncidentType,
                        Collectors.counting()
                ));

        List<Map.Entry<IncidentReport.IncidentType, Long>> sorted = total.entrySet().stream()
                .sorted(Map.Entry.<IncidentReport.IncidentType, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());

        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            IncidentReport.IncidentType type = sorted.get(i).getKey();
            long count = sorted.get(i).getValue();
            long cNow = current.getOrDefault(type, 0L);
            long cPrev = previous.getOrDefault(type, 0L);
            boolean up = cNow >= cPrev;
            long changePct = cPrev == 0 ? (cNow > 0 ? 100 : 0) : Math.round(((cNow - cPrev) * 100.0) / cPrev);

            Map<String, Object> row = new HashMap<>();
            row.put("rank", i + 1);
            row.put("type", formatIncidentType(type));
            row.put("count", count);
            row.put("trend", up ? "up" : "down");
            row.put("change", (changePct >= 0 ? "+" : "") + changePct + "%");
            row.put("color", threatColor(type));
            data.add(row);
        }

        return data;
    }

    private String formatIncidentType(IncidentReport.IncidentType type) {
        switch (type) {
            case PHISHING_EMAIL:
                return "Phishing Email";
            case SUSPICIOUS_URL:
                return "Suspicious URL";
            case MALWARE:
                return "Malware";
            case RANSOMWARE:
                return "Ransomware";
            case UNAUTHORIZED_ACCESS:
                return "Unauthorized Access";
            case DATA_BREACH:
                return "Data Breach";
            case SOCIAL_ENGINEERING:
                return "Social Engineering";
            case OTHER:
            default:
                return "Other";
        }
    }

    private String threatColor(IncidentReport.IncidentType type) {
        switch (type) {
            case PHISHING_EMAIL:
                return "#ff4757";
            case SUSPICIOUS_URL:
                return "#ffd32a";
            case MALWARE:
                return "#a55eea";
            case RANSOMWARE:
                return "#00d4ff";
            case DATA_BREACH:
                return "#00ff88";
            case UNAUTHORIZED_ACCESS:
                return "#ff6b35";
            case SOCIAL_ENGINEERING:
                return "#f368e0";
            case OTHER:
            default:
                return "#94a3b8";
        }
    }

    private long pct(long value, long total) {
        return Math.round((value * 100.0) / total);
    }

    private boolean inRange(Integer value, int min, int max) {
        if (value == null) {
            return false;
        }
        return value >= min && value <= max;
    }

    private double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private boolean sameDate(LocalDateTime dt, LocalDate d) {
        return dt != null && dt.toLocalDate().equals(d);
    }

    private boolean isBetweenDates(LocalDate date, LocalDate start, LocalDate end) {
        return !date.isBefore(start) && !date.isAfter(end);
    }

    private boolean sameDateRange(LocalDateTime dt, LocalDate start, LocalDate end) {
        return dt != null && isBetweenDates(dt.toLocalDate(), start, end);
    }

    private boolean inHourRange(int hour, int startHour, int endHourExclusive) {
        return hour >= startHour && hour < endHourExclusive;
    }

    private <T> T coalesce(T first, T second) {
        return first != null ? first : second;
    }

    public static class GraphData {
        public Map<String, Object> threatAnalysis;
        public Map<String, Object> analyticsDashboard;

        public GraphData(Map<String, Object> threatAnalysis,
                         Map<String, Object> analyticsDashboard) {
            this.threatAnalysis = threatAnalysis;
            this.analyticsDashboard = analyticsDashboard;
        }
    }
}
