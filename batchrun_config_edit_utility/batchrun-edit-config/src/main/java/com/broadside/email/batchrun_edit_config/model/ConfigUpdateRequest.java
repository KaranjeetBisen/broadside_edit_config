package com.broadside.email.batchrun_edit_config.model;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Data
@Getter
@Setter
public class ConfigUpdateRequest {

    // if null → keep old value
    private List<String> metadataColumns;
    private String templatePath;
    private Map<String, String> msgHeaders;
    private String attachmentDir;
    private String namespaceConstant;
}
