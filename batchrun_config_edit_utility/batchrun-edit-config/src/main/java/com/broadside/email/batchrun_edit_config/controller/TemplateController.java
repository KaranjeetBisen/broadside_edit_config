package com.broadside.email.batchrun_edit_config.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.broadside.email.batchrun_edit_config.model.TemplateUpdateRequest;
import com.broadside.email.batchrun_edit_config.model.TemplateView;
import com.broadside.email.batchrun_edit_config.service.JobQService;
import com.broadside.email.batchrun_edit_config.service.TemplateService;

@RestController
@RequestMapping("/campaign")
public class TemplateController {

    private static final Logger logger = LoggerFactory.getLogger(TemplateController.class);

    @Autowired
    private TemplateService templateService;

    @Autowired
    private JobQService jobQService;

    /**
     * Download/Get template for a campaign
     */
    @GetMapping(value = "/{campId}/template", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getTemplate(@PathVariable String campId) {
        logger.info("GET template request for campaign: {}", campId);
        int jobId = jobQService.start("TEMPLATE", "GET", campId);

        try {
            TemplateView view = templateService.getTemplate(campId);
            jobQService.end(jobId, view, "SUCCESS");

            if (!view.isExists()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Template not found for campaign: " + campId);
                response.put("campId", campId);
                response.put("exists", false);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            return ResponseEntity.ok(view);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request for campaign {}: {}", campId, e.getMessage());
            jobQService.end(jobId, e.getMessage(), "FAILED");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Invalid request", e.getMessage()));
        } catch (IOException e) {
            logger.error("IO error for campaign {}: {}", campId, e.getMessage());
            jobQService.end(jobId, e.getMessage(), "FAILED");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("File system error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error for campaign {}: {}", campId, e.getMessage(), e);
            jobQService.end(jobId, e.getMessage(), "FAILED");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal server error", "An unexpected error occurred"));
        }
    }

    /**
     * Upload/Update template for a campaign
     */
    @PutMapping(value = "/{campId}/template", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateTemplate(
            @PathVariable String campId,
            @RequestBody TemplateUpdateRequest request) {

        logger.info("PUT template request for campaign: {}", campId);
        int jobId = jobQService.start("TEMPLATE", "UPDATE", request);

        try {
            TemplateView updated = templateService.updateTemplate(campId, request);
            jobQService.end(jobId, updated, "SUCCESS");
            logger.info("Successfully updated template for campaign: {}", campId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request for campaign {}: {}", campId, e.getMessage());
            jobQService.end(jobId, e.getMessage(), "FAILED");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Validation error", e.getMessage()));
        } catch (IOException e) {
            logger.error("IO error for campaign {}: {}", campId, e.getMessage());
            jobQService.end(jobId, e.getMessage(), "FAILED");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("File system error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error for campaign {}: {}", campId, e.getMessage(), e);
            jobQService.end(jobId, e.getMessage(), "FAILED");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal server error", "An unexpected error occurred"));
        }
    }

    /**
     * Validate template content without saving (optional endpoint for
     * pre-validation)
     */
    @PostMapping(value = "/{campId}/template/validate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> validateTemplate(
            @PathVariable String campId,
            @RequestBody TemplateUpdateRequest request) {

        logger.info("Validate template request for campaign: {}", campId);

        try {
            boolean isValid = templateService.validateTemplate(request.getHtmlContent());

            Map<String, Object> response = new HashMap<>();
            response.put("campId", campId);
            response.put("valid", isValid);
            response.put("message", isValid ? "Template is valid" : "Template validation failed");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error validating template for campaign {}: {}", campId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Validation error", e.getMessage()));
        }
    }

    // ==================== FILE DOWNLOAD & UPLOAD ENDPOINTS ====================

    /**
     * Download HTML template file
     */
    @GetMapping(value = "/{campId}/template/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> downloadTemplate(@PathVariable String campId) {
        logger.info("Download template file request for campaign: {}", campId);
        int jobId = jobQService.start("TEMPLATE", "DOWNLOAD", campId);

        try {
            Path templateFile = templateService.getTemplateFilePath(campId);

            if (!Files.exists(templateFile)) {
                jobQService.end(jobId, "Template file not found", "FAILED");
                return ResponseEntity.notFound().build();
            }

            byte[] fileContent = Files.readAllBytes(templateFile);
            ByteArrayResource resource = new ByteArrayResource(fileContent);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + campId + ".html\"");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE);
            headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileContent.length));

            jobQService.end(jobId, "File downloaded successfully", "SUCCESS");
            logger.info("Successfully downloaded template file for campaign: {}", campId);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(fileContent.length)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid download request for campaign {}: {}", campId, e.getMessage());
            jobQService.end(jobId, e.getMessage(), "FAILED");
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            logger.error("IO error downloading template for campaign {}: {}", campId, e.getMessage());
            jobQService.end(jobId, e.getMessage(), "FAILED");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            logger.error("Unexpected error downloading template for campaign {}: {}", campId, e.getMessage(), e);
            jobQService.end(jobId, e.getMessage(), "FAILED");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Upload HTML template file
     */
    @PostMapping(value = "/{campId}/template/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadTemplate(
            @PathVariable String campId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "createBackup", defaultValue = "true") boolean createBackup) {

        logger.info("Upload template file request for campaign: {} (file: {})", campId, file.getOriginalFilename());
        int jobId = jobQService.start("TEMPLATE", "UPLOAD", campId + " - " + file.getOriginalFilename());

        try {
            // Validate file
            if (file.isEmpty()) {
                jobQService.end(jobId, "Empty file", "FAILED");
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Validation error", "File is empty"));
            }

            // Check file type
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".html")) {
                jobQService.end(jobId, "Invalid file type", "FAILED");
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Validation error", "File must be an HTML file (.html extension)"));
            }

            // Read file content
            String htmlContent = new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);

            // Update template using service
            TemplateView updated = templateService.updateTemplateFromFile(campId, htmlContent, createBackup);

            jobQService.end(jobId, updated, "SUCCESS");
            logger.info("Successfully uploaded template file for campaign: {}", campId);

            return ResponseEntity.ok(updated);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid upload request for campaign {}: {}", campId, e.getMessage());
            jobQService.end(jobId, e.getMessage(), "FAILED");
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Validation error", e.getMessage()));
        } catch (IOException e) {
            logger.error("IO error uploading template for campaign {}: {}", campId, e.getMessage());
            jobQService.end(jobId, e.getMessage(), "FAILED");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("File system error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error uploading template for campaign {}: {}", campId, e.getMessage(), e);
            jobQService.end(jobId, e.getMessage(), "FAILED");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal server error", "An unexpected error occurred"));
        }
    }

    /**
     * Helper method to create consistent error responses
     */
    private Map<String, Object> createErrorResponse(String error, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", System.currentTimeMillis());
        return errorResponse;
    }
}
