package com.broadside.email.batchrun_edit_config.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.broadside.email.batchrun_edit_config.model.TemplateUpdateRequest;
import com.broadside.email.batchrun_edit_config.model.TemplateView;

@Service
public class TemplateService {

    private static final Logger logger = LoggerFactory.getLogger(TemplateService.class);

    // Configuration constants
    private static final long MAX_TEMPLATE_SIZE = 2 * 1024 * 1024; // 2MB
    private static final Pattern HTML_BASIC_PATTERN = Pattern.compile("(?i)<html[^>]*>.*</html>", Pattern.DOTALL);
    private static final DateTimeFormatter BACKUP_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Value("${templ.storage.path}")
    private String templBasePath;

    private Path resolveTemplateFile(String campId) {
        return Paths.get(templBasePath, campId, campId + ".html");
    }

    private Path resolveTemplateDirectory(String campId) {
        return Paths.get(templBasePath, campId);
    }

    private Path resolveBackupFile(String campId, String timestamp) {
        return Paths.get(templBasePath, campId, "backups", campId + "_" + timestamp + ".html");
    }

    /**
     * Validates campaign ID for security
     */
    private void validateCampId(String campId) {
        if (campId == null || campId.trim().isEmpty()) {
            throw new IllegalArgumentException("Campaign ID cannot be null or empty");
        }

        // Prevent directory traversal attacks
        if (campId.contains("..") || campId.contains("/") || campId.contains("\\")) {
            throw new IllegalArgumentException("Invalid campaign ID: contains illegal characters");
        }

        // Ensure reasonable length
        if (campId.length() > 50) {
            throw new IllegalArgumentException("Campaign ID too long (max 50 characters)");
        }
    }

    /**
     * Validates HTML content
     */
    private void validateHtmlContent(String htmlContent) {
        if (htmlContent == null) {
            throw new IllegalArgumentException("HTML content cannot be null");
        }

        if (htmlContent.trim().isEmpty()) {
            throw new IllegalArgumentException("HTML content cannot be empty");
        }

        if (htmlContent.length() > MAX_TEMPLATE_SIZE) {
            throw new IllegalArgumentException(
                    "HTML content exceeds maximum size of " + (MAX_TEMPLATE_SIZE / 1024 / 1024) + "MB");
        }

        // Basic HTML structure validation
        if (!HTML_BASIC_PATTERN.matcher(htmlContent.trim()).matches()) {
            logger.warn("HTML content doesn't appear to have proper HTML structure");
        }
    }

    /**
     * Calculates SHA-256 hash of content for integrity checking
     */
    private String calculateHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            logger.error("Error calculating hash", e);
            return "";
        }
    }

    /**
     * Creates a backup of existing template
     */
    private String createBackup(String campId, Path templateFile) throws IOException {
        if (!Files.exists(templateFile)) {
            return ""; // No existing file to backup
        }

        String timestamp = LocalDateTime.now().format(BACKUP_TIMESTAMP_FORMAT);
        Path backupDir = resolveTemplateDirectory(campId).resolve("backups");
        Path backupFile = resolveBackupFile(campId, timestamp);

        // Create backup directory if it doesn't exist
        Files.createDirectories(backupDir);

        // Copy existing file to backup
        Files.copy(templateFile, backupFile, StandardCopyOption.REPLACE_EXISTING);

        logger.info("Created backup for campaign {} at {}", campId, backupFile);
        return backupFile.toString();
    }

    public TemplateView getTemplate(String campId) throws IOException {
        validateCampId(campId);

        Path templateFile = resolveTemplateFile(campId);
        TemplateView view = new TemplateView();
        view.setCampId(campId);
        view.setFilePath(templateFile.toString());

        if (Files.exists(templateFile)) {
            view.setExists(true);
            String htmlContent = Files.readString(templateFile, StandardCharsets.UTF_8);
            view.setHtmlContent(htmlContent);
            view.setFileSize(Files.size(templateFile));
            view.setContentHash(calculateHash(htmlContent));

            FileTime lastModified = Files.getLastModifiedTime(templateFile);
            view.setLastModified(lastModified.toInstant().toString());

            // Check for HTML validity
            view.setValidHtml(HTML_BASIC_PATTERN.matcher(htmlContent.trim()).matches());
            view.setValidationMessage(
                    view.isValidHtml() ? "Valid HTML structure" : "Warning: HTML structure may be incomplete");

            // Check if backups exist
            Path backupDir = resolveTemplateDirectory(campId).resolve("backups");
            view.setHasBackup(Files.exists(backupDir) && Files.list(backupDir).findAny().isPresent());
            if (view.isHasBackup()) {
                view.setBackupPath(backupDir.toString());
            }

        } else {
            view.setExists(false);
            view.setHtmlContent("");
            view.setFileSize(0);
            view.setLastModified("");
            view.setContentHash("");
            view.setValidHtml(false);
            view.setValidationMessage("Template file does not exist");
            view.setHasBackup(false);
        }

        return view;
    }

    public TemplateView updateTemplate(String campId, TemplateUpdateRequest request) throws IOException {
        validateCampId(campId);
        validateHtmlContent(request.getHtmlContent());

        Path templateDir = resolveTemplateDirectory(campId);
        Path templateFile = resolveTemplateFile(campId);

        // Create directory if it doesn't exist
        if (!Files.exists(templateDir)) {
            Files.createDirectories(templateDir);
            logger.info("Created template directory for campaign: {}", campId);
        }

        // Create backup if requested and file exists
        String backupPath = "";
        if (request.isCreateBackup() && Files.exists(templateFile)) {
            try {
                backupPath = createBackup(campId, templateFile);
            } catch (IOException e) {
                logger.error("Failed to create backup for campaign {}: {}", campId, e.getMessage());
                throw new IOException("Failed to create backup: " + e.getMessage());
            }
        }

        try {
            // Write the HTML content to the file (this will override existing file)
            Files.writeString(templateFile, request.getHtmlContent(), StandardCharsets.UTF_8);
            logger.info("Successfully updated template for campaign: {}", campId);

            // Return the updated template view
            TemplateView result = getTemplate(campId);

            // Add metadata from request
            result.setDescription(request.getDescription());
            result.setVersion(request.getVersion());
            if (!backupPath.isEmpty()) {
                result.setBackupPath(backupPath);
                result.setHasBackup(true);
            }

            return result;

        } catch (IOException e) {
            logger.error("Failed to write template for campaign {}: {}", campId, e.getMessage());
            throw new IOException("Failed to write template file: " + e.getMessage());
        }
    }

    /**
     * Validates template without saving (useful for pre-upload validation)
     */
    public boolean validateTemplate(String htmlContent) {
        try {
            validateHtmlContent(htmlContent);
            return true;
        } catch (IllegalArgumentException e) {
            logger.debug("Template validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Gets the template file path (for file operations)
     */
    public Path getTemplateFilePath(String campId) {
        validateCampId(campId);
        return resolveTemplateFile(campId);
    }

    /**
     * Updates template from file content
     */
    public TemplateView updateTemplateFromFile(String campId, String htmlContent, boolean createBackup)
            throws IOException {
        TemplateUpdateRequest request = new TemplateUpdateRequest();
        request.setHtmlContent(htmlContent);
        request.setCreateBackup(createBackup);
        return updateTemplate(campId, request);
    }
}
