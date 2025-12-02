package com.broadside.email.batchrun_edit_config.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class TemplateUpdateRequest {

    private String htmlContent;

    // Optional: Add metadata for better tracking
    private String description;
    private String version;
    private boolean createBackup = true; // Default to true for safety
}
