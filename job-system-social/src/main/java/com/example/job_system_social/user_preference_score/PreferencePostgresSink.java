package com.example.job_system_social.user_preference_score;

import com.example.job_system_social.model.UserPreferenceScoreRequest;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class PreferencePostgresSink extends RichSinkFunction<UserPreferenceScoreRequest> {
    private transient Connection connection;
    private transient PreparedStatement insertOrUpdateStatement;

    private static final String DB_URL = "jdbc:postgresql://ep-purple-leaf-a1s2f3su-pooler.ap-southeast-1.aws.neon.tech:5432/post_db?sslmode=require";
    private static final String DB_USER = "neondb_owner";
    private static final String DB_PASSWORD = "npg_mUWfhpe3y0Mj";

    @Override
    public void open(Configuration parameters) throws Exception {
        Class.forName("org.postgresql.Driver");
        connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        connection.setAutoCommit(false);
        insertOrUpdateStatement = connection.prepareStatement(
                "INSERT INTO user_type_preference (user_id, type_id, score) VALUES (?, ?, ?) " +
                        "ON CONFLICT (user_id, type_id) DO UPDATE SET score = user_type_preference.score + EXCLUDED.score"
        );
    }

    @Override
    public void invoke(UserPreferenceScoreRequest score, Context context) throws Exception {
        try {
            insertOrUpdateStatement.setString(1, score.getUserId());
            insertOrUpdateStatement.setLong(2, score.getTypeId());
            insertOrUpdateStatement.setInt(3, score.getScore());
            insertOrUpdateStatement.executeUpdate();
            connection.commit();
        } catch (Exception e) {
            System.err.println("❌ Error updating preference score for user " + score.getUserId() + ", type " + score.getTypeId() + ": " + e.getMessage());
            connection.rollback();
        }
    }

    @Override
    public void close() throws Exception {
        if (insertOrUpdateStatement != null) {
            insertOrUpdateStatement.close();
        }
        if (connection != null) {
            connection.close();
        }
    }
}