package com.broadside.email.batchrun_edit_config.controller;


import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.broadside.email.batchrun_edit_config.model.ConfigUpdateRequest;
import com.broadside.email.batchrun_edit_config.model.ConfigView;
import com.broadside.email.batchrun_edit_config.service.ConfigService;
import com.broadside.email.batchrun_edit_config.service.JobQService;

@RestController
@RequestMapping("/campaign")
public class ConfigController {

    @Autowired
    private  ConfigService configService;

    @Autowired
    private JobQService jobQService;

    @GetMapping("/{campId}/config")
    public ResponseEntity<?> getConfig(@PathVariable String campId) {
        int jobId = jobQService.start("CONFIG", "GET", campId);

        try {
            ConfigView view = configService.getConfig(campId);
            jobQService.end(jobId, view, "SUCCESS");
            return ResponseEntity.ok(view);
        } catch (Exception e) {
            jobQService.end(jobId, e.getMessage(), "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{campId}/config")
    public ResponseEntity<?> updateConfig(
            @PathVariable String campId,
            @RequestBody ConfigUpdateRequest request) {

        int jobId = jobQService.start("CONFIG", "UPDATE", request);

        try {
            ConfigView updated = configService.updateConfig(campId, request);
            jobQService.end(jobId, updated, "SUCCESS");
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            jobQService.end(jobId, e.getMessage(), "FAILED");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        }
    }
}
