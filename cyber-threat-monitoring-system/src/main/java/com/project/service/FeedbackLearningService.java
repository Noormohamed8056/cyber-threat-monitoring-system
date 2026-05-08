package com.project.service;

import com.project.entity.IncidentReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Slf4j
@Service
public class FeedbackLearningService {

    @Value("${intelligence.storage.upload-dir:uploads/}")
    private String uploadDir;

    public void appendConfirmedThreat(IncidentReport report) {
        try {
            Path mlDir = Paths.get(uploadDir, "ml-feedback");
            Files.createDirectories(mlDir);
            Path dataset = mlDir.resolve("feedback_dataset.csv");

            if (!Files.exists(dataset)) {
                String header = "timestamp,reportId,type,text,prediction,riskLevel,label\n";
                Files.writeString(dataset, header, StandardCharsets.UTF_8);
            }

            String text = sanitize(
                    report.getTextContent() != null
                            ? report.getTextContent()
                            : report.getDescription()
            );
            String line = String.format(
                    "%s,%d,%s,\"%s\",%s,%s,%s%n",
                    LocalDateTime.now(),
                    report.getId(),
                    sanitize(report.getType()),
                    text,
                    sanitize(report.getPrediction()),
                    sanitize(report.getRiskLevel() != null ? report.getRiskLevel().name() : ""),
                    "1"
            );
            Files.writeString(dataset, line, StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException ex) {
            log.warn("Failed to append feedback dataset: {}", ex.getMessage());
        }
    }

    private String sanitize(String value) {
        if (value == null) return "";
        return value.replace("\"", "'").replace("\n", " ").replace("\r", " ");
    }
}
