package com.broadside.email.batchrun_edit_config.utils;

import org.springframework.stereotype.Component;

import com.broadside.email.batchrun_edit_config.model.ConfigView;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ConfigWriter {

    public void write(Path filePath, ConfigView cfg) throws IOException {
        List<String> out = new ArrayList<>();

        // constants
        out.add("# constants used by metadata section");
        out.add("[constants]");
        out.add("campid: " + cfg.getConstantCampId());
        out.add("");

        // metadata
        out.add("# the metadata section");
        out.add("[metadata]");
        if (cfg.getMetadataColumns() != null) {
            for (String col : cfg.getMetadataColumns()) {
                out.add("column : " + col);
            }
        }
        // if you want to always add constant :campid here, uncomment:
        // out.add("constant :campid");
        out.add("");

        // template
        out.add("# message template relative path");
        out.add("[template]");
        out.add("path : " + cfg.getTemplatePath());
        out.add("");

        // msg headers
        out.add("# message headers");
        out.add("[msghdrs]");
        if (cfg.getMsgHeaders() != null) {
            for (Map.Entry<String, String> e : cfg.getMsgHeaders().entrySet()) {
                out.add(String.format("%-8s:%s", e.getKey(), e.getValue()));
            }
        }
        out.add("");

        // attachments
        out.add("# attachment dir relative path");
        out.add("[attachments]");
        out.add("dir : " + cfg.getAttachmentDir());
        out.add("");

        // namespace
        out.add("# namespace for memcached");
        out.add("[namespace]");
        out.add("constant : " + cfg.getNamespaceConstant());
        out.add("");

        Files.write(filePath, out, StandardCharsets.UTF_8);
    }
}
