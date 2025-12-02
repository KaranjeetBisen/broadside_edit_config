package com.broadside.email.batchrun_edit_config.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @Autowired
    private TemplateService templateService;

    @Autowired
    private JobQService jobQService;

    @GetMapping("/{campId}/template")
    public ResponseEntity<?> getTemplate(@PathVariable String campId) {
        int jobId = jobQService.start("TEMPLATE", "GET", campId);

        try {
            TemplateView view = templateService.getTemplate(campId);
            jobQService.end(jobId, view, "SUCCESS");
            return ResponseEntity.ok(view);
        } catch (Exception e) {
            jobQService.end(jobId, e.getMessage(), "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{campId}/template")
    public ResponseEntity<?> updateTemplate(
            @PathVariable String campId,
            @RequestBody TemplateUpdateRequest request) {

        int jobId = jobQService.start("TEMPLATE", "UPDATE", request);

        try {
            TemplateView updated = templateService.updateTemplate(campId, request);
            jobQService.end(jobId, updated, "SUCCESS");
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            jobQService.end(jobId, e.getMessage(), "FAILED");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        }
    }
}
