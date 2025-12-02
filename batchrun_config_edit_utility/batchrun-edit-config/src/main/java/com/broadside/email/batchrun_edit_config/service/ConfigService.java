package com.broadside.email.batchrun_edit_config.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.broadside.email.batchrun_edit_config.model.ConfigUpdateRequest;
import com.broadside.email.batchrun_edit_config.model.ConfigView;
import com.broadside.email.batchrun_edit_config.utils.ConfigParser;
import com.broadside.email.batchrun_edit_config.utils.ConfigWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ConfigService {

    private final ConfigParser parser = new ConfigParser();
    private final ConfigWriter writer = new ConfigWriter();

    @Value("${config.storage.path}")
    private String basePath;

    private Path resolveFile(String campId) {
        return Paths.get(basePath, campId + ".conf");
    }

    public ConfigView getConfig(String campId) throws IOException {
        Path file = resolveFile(campId);
        if (!Files.exists(file)) {
            throw new IOException("Config file not found for campId: " + campId);
        }
        return parser.parse(file, campId);
    }

    public ConfigView updateConfig(String campId, ConfigUpdateRequest req) throws IOException {
        Path file = resolveFile(campId);
        if (!Files.exists(file)) {
            throw new IOException("Config file not found for campId: " + campId);
        }

        ConfigView current = parser.parse(file, campId);

        // Overlay updates: if field != null, replace
        if (req.getMetadataColumns() != null) {
            current.setMetadataColumns(req.getMetadataColumns());
        }
        if (req.getTemplatePath() != null) {
            current.setTemplatePath(req.getTemplatePath());
        }
        if (req.getMsgHeaders() != null) {
            current.setMsgHeaders(req.getMsgHeaders());
        }
        if (req.getAttachmentDir() != null) {
            current.setAttachmentDir(req.getAttachmentDir());
        }
        if (req.getNamespaceConstant() != null) {
            current.setNamespaceConstant(req.getNamespaceConstant());
        }

        writer.write(file, current);
        return current;
    }
}

