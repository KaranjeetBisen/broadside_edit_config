package com.broadside.email.batchrun_edit_config.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Data
@Getter
@Setter
public class ConfigView {

    private String campId;

    // [constants]
    private String constantCampId;

    // [metadata] (list of "column : X")
    private List<String> metadataColumns;

    // [template]
    private String templatePath;

    // [msghdrs]
    private Map<String, String> msgHeaders;

    // [attachments]
    private String attachmentDir;

    // [namespace]
    private String namespaceConstant; // e.g. "campid"
}
