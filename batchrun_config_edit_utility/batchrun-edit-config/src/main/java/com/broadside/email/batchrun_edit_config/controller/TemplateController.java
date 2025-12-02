package com.broadside.email.batchrun_edit_config.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
