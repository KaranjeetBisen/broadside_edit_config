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

    /**
     * Finds existing HTML file in the template directory
     * Returns the path of the first .html file found, or null if none exists
     */
    private Path findExistingHtmlFile(String campId) throws IOException {
        Path templateDir = resolveTemplateDirectory(campId);

        if (!Files.exists(templateDir)) {
            return null;
        }

        // Search for any .html file in the directory
        try (var stream = Files.list(templateDir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".html"))
                    .findFirst()
                    .orElse(null);
        }
    }

    /**
     * Creates a backup of existing HTML file with its original name
     */
    private String createBackupWithOriginalName(String campId, Path existingFile) throws IOException {
        if (!Files.exists(existingFile)) {
            return ""; // No existing file to backup
        }

        String timestamp = LocalDateTime.now().format(BACKUP_TIMESTAMP_FORMAT);
        String originalFileName = existingFile.getFileName().toString();
        String fileNameWithoutExt = originalFileName.substring(0, originalFileName.lastIndexOf(".html"));

        Path backupDir = resolveTemplateDirectory(campId).resolve("backups");
        Path backupFile = backupDir.resolve(fileNameWithoutExt + "_" + timestamp + ".html");

        // Create backup directory if it doesn't exist
        Files.createDirectories(backupDir);

        // Copy existing file to backup with timestamp
        Files.copy(existingFile, backupFile, StandardCopyOption.REPLACE_EXISTING);

        logger.info("Created backup for campaign {} from {} to {}", campId, existingFile.getFileName(),
                backupFile.getFileName());
        return backupFile.toString();
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

    public TemplateView getTemplate(String campId) throws IOException {
        validateCampId(campId);

        // First, try to find any existing HTML file in the directory
        Path existingHtmlFile = findExistingHtmlFile(campId);
        Path templateFile = existingHtmlFile != null ? existingHtmlFile : resolveTemplateFile(campId);

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

        // Create directory if it doesn't exist
        if (!Files.exists(templateDir)) {
            Files.createDirectories(templateDir);
            logger.info("Created template directory for campaign: {}", campId);
        }

        // Find existing HTML file in the directory
        Path existingHtmlFile = findExistingHtmlFile(campId);
        Path targetFile;

        if (existingHtmlFile != null) {
            // Use the existing file's name and location
            targetFile = existingHtmlFile;
            logger.info("Found existing HTML file for campaign {}: {}", campId, existingHtmlFile.getFileName());
        } else {
            // No existing file, use default naming convention
            targetFile = resolveTemplateFile(campId);
            logger.info("No existing HTML file found for campaign {}, using default: {}", campId,
                    targetFile.getFileName());
        }

        // Create backup if requested and existing file exists
        String backupPath = "";
        if (request.isCreateBackup() && existingHtmlFile != null) {
            try {
                backupPath = createBackupWithOriginalName(campId, existingHtmlFile);
            } catch (IOException e) {
                logger.error("Failed to create backup for campaign {}: {}", campId, e.getMessage());
                throw new IOException("Failed to create backup: " + e.getMessage());
            }
        }

        try {
            // Write the new HTML content to the target file (this will override existing
            // file)
            Files.writeString(targetFile, request.getHtmlContent(), StandardCharsets.UTF_8);
            logger.info("Successfully updated template for campaign {} at: {}", campId, targetFile.getFileName());

            // Return the updated template view (note: this will read from the updated file)
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
     * Returns existing HTML file if found, otherwise returns default path
     */
    public Path getTemplateFilePath(String campId) throws IOException {
        validateCampId(campId);

        // Try to find existing HTML file first
        Path existingFile = findExistingHtmlFile(campId);
        if (existingFile != null) {
            return existingFile;
        }

        // Return default path if no existing file
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
