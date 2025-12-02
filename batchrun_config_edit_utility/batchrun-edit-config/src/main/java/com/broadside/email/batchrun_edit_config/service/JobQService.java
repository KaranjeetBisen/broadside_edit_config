package com.broadside.email.batchrun_edit_config.service;

import tools.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.broadside.email.batchrun_edit_config.dao.JobQDao;

import java.util.UUID;

@Service
public class JobQService {

    @Autowired
    private JobQDao jobQDao;

    private final ObjectMapper mapper = new ObjectMapper();

    public int start(String jobType, String mode, Object requestBody) {
        try {
            String uuid = UUID.randomUUID().toString();
            String requestJson = mapper.writeValueAsString(requestBody);
            return jobQDao.insertStart(jobType, mode, requestJson, uuid);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void end(int id, Object responseBody, String status) {
        if (id == 0) return;

        try {
            String responseJson = mapper.writeValueAsString(responseBody);
            jobQDao.updateEnd(id, responseJson, status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
