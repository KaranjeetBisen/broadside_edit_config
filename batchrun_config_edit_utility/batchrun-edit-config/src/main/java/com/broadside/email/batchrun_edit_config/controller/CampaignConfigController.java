package com.broadside.email.batchrun_edit_config.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/edit")
public class CampaignConfigController {

    private final CampaignConfigService configService;

    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> getConfig(@PathVariable String campId) {
        try {
            return ResponseEntity.ok(configService.readConfig(campId));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PutMapping("/{campId}/config")
    public ResponseEntity<String> updateConfig(
            @PathVariable String campId,
            @RequestBody Map<String, String> updates) {

        try {
            configService.updateConfig(campId, updates);
            return ResponseEntity.ok("Config updated successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Config file not found");
        }
    }
}
