package com.broadside.email.batchrun_edit_config.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class TemplateUpdateRequest {

    private String htmlContent;
}
