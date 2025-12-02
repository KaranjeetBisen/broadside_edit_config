package com.broadside.email.batchrun_edit_config.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.broadside.email.batchrun_edit_config.model.TemplateUpdateRequest;
import com.broadside.email.batchrun_edit_config.model.TemplateView;

@Service
public class TemplateService {

    @Value("${templ.storage.path}")
    private String templBasePath;

    private Path resolveTemplateFile(String campId) {
        // Full path: templ.storage.path/campaignId/campaignId.html
        return Paths.get(templBasePath, campId, campId + ".html");
    }

    private Path resolveTemplateDirectory(String campId) {
        return Paths.get(templBasePath, campId);
    }

    public TemplateView getTemplate(String campId) throws IOException {
        Path templateFile = resolveTemplateFile(campId);
        TemplateView view = new TemplateView();
        view.setCampId(campId);
        view.setFilePath(templateFile.toString());

        if (Files.exists(templateFile)) {
            view.setExists(true);
            view.setHtmlContent(Files.readString(templateFile, StandardCharsets.UTF_8));
            view.setFileSize(Files.size(templateFile));

            FileTime lastModified = Files.getLastModifiedTime(templateFile);
            view.setLastModified(lastModified.toInstant().toString());
        } else {
            view.setExists(false);
            view.setHtmlContent("");
            view.setFileSize(0);
            view.setLastModified("");
        }

        return view;
    }

    public TemplateView updateTemplate(String campId, TemplateUpdateRequest request) throws IOException {
        Path templateDir = resolveTemplateDirectory(campId);
        Path templateFile = resolveTemplateFile(campId);

        // Create directory if it doesn't exist
        if (!Files.exists(templateDir)) {
            Files.createDirectories(templateDir);
        }

        // Write the HTML content to the file (this will override existing file)
        Files.writeString(templateFile, request.getHtmlContent(), StandardCharsets.UTF_8);

        // Return the updated template view
        return getTemplate(campId);
    }
}
