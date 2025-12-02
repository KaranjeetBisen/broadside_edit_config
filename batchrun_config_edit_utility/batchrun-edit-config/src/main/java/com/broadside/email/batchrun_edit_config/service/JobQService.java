package com.broadside.email.batchrun_edit_config.service;

@Service
@RequiredArgsConstructor
public class JobQService {

    private final JobQDao jobQDao;
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
        try {
            String responseJson = mapper.writeValueAsString(responseBody);
            jobQDao.updateEnd(id, responseJson, status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
