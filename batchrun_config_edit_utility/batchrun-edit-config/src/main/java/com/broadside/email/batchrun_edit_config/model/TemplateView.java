package com.broadside.email.batchrun_edit_config.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class TemplateView {

    private String campId;
    private String htmlContent;
    private String filePath;
    private boolean exists;
    private long fileSize;
    private String lastModified;
}
