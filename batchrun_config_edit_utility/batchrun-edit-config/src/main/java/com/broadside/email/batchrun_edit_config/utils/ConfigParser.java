package com.broadside.email.batchrun_edit_config.utils;

import org.springframework.stereotype.Component;

import com.broadside.email.batchrun_edit_config.model.ConfigView;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.*;

@Component
public class ConfigParser {

    public ConfigView parse(Path filePath, String campId) throws IOException {
        List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);

        ConfigView view = new ConfigView();
        view.setCampId(campId);

        String currentSection = null;
        List<String> metadataColumns = new ArrayList<>();
        Map<String, String> msgHeaders = new LinkedHashMap<>();

        for (String rawLine : lines) {
            String line = rawLine.trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            if (line.startsWith("[") && line.endsWith("]")) {
                currentSection = line.substring(1, line.length() - 1).toLowerCase();
                continue;
            }

            if (currentSection == null) {
                continue;
            }

            switch (currentSection) {
                case "constants": {
                    // e.g. "campid: 137783_FSN_BOUNCE_pdf"
                    String[] kv = line.split(":", 2);
                    if (kv.length == 2) {
                        String key = kv[0].trim();
                        String value = kv[1].trim();
                        if ("campid".equalsIgnoreCase(key)) {
                            view.setConstantCampId(value);
                        }
                    }
                    break;
                }
                case "metadata": {
                    // e.g. "column : USERID"
                    if (line.toLowerCase().startsWith("column")) {
                        String[] kv = line.split(":", 2);
                        if (kv.length == 2) {
                            metadataColumns.add(kv[1].trim());
                        }
                    }
                    break;
                }
                case "template": {
                    // e.g. "path : 137783_FSN_BOUNCE_pdf/137783_FSN.htm"
                    if (line.toLowerCase().startsWith("path")) {
                        String[] kv = line.split(":", 2);
                        if (kv.length == 2) {
                            view.setTemplatePath(kv[1].trim());
                        }
                    }
                    break;
                }
                case "msghdrs": {
                    // e.g. "From     :evoting@nsdl.com"
                    String[] kv = line.split(":", 2);
                    if (kv.length == 2) {
                        msgHeaders.put(kv[0].trim(), kv[1].trim());
                    }
                    break;
                }
                case "attachments": {
                    // e.g. "dir : nsdl/ajay"
                    if (line.toLowerCase().startsWith("dir")) {
                        String[] kv = line.split(":", 2);
                        if (kv.length == 2) {
                            view.setAttachmentDir(kv[1].trim());
                        }
                    }
                    break;
                }
                case "namespace": {
                    // e.g. "constant : campid"
                    String[] kv = line.split(":", 2);
                    if (kv.length == 2 && line.toLowerCase().startsWith("constant")) {
                        view.setNamespaceConstant(kv[1].trim());
                    }
                    break;
                }
                default:
                    // ignore unknown sections
                    break;
            }
        }

        view.setMetadataColumns(metadataColumns);
        view.setMsgHeaders(msgHeaders);

        // if constant campid missing, derive it from campId name
        if (view.getConstantCampId() == null) {
            view.setConstantCampId(campId);
        }
        if (view.getNamespaceConstant() == null) {
            view.setNamespaceConstant("campid");
        }

        return view;
    }
}
