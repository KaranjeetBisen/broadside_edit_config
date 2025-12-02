package com.broadside.email.batchrun_edit_config.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

@Repository

public class JobQDao {

    private DataSource dataSource;

    public int insertStart(String jobType, String mode, String request, String uuid) throws Exception {
        String sql = """
            INSERT INTO jobq (jobtype, mode, starttime, request, status, uuid)
            VALUES (?, ?, now(), ?, ?, ?)
            RETURNING id
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, jobType);
            ps.setString(2, mode);
            ps.setString(3, request);
            ps.setString(4, "STARTED");
            ps.setString(5, uuid);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public void updateEnd(int id, String response, String status) throws Exception {
        String sql = """
            UPDATE jobq
               SET endtime = now(),
                   response = ?,
                   status = ?
             WHERE id = ?
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, response);
            ps.setString(2, status);
            ps.setInt(3, id);

            ps.executeUpdate();
        }
    }
}
