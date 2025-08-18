package com.example.job_system_social;

import com.example.job_system_social.model.TrendingEventRequest;
import com.example.job_system_social.trending_score.TrendingScoreCalculator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import java.util.Properties;

public class TrendingFlinkJob {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "kafka:29092");
        properties.setProperty("group.id", "trending-group");

        FlinkKafkaConsumer<String> kafkaConsumer = new FlinkKafkaConsumer<>(
                "notification-events",
                new SimpleStringSchema(),
                properties
        );

        DataStream<String> kafkaStream = env.addSource(kafkaConsumer);

        kafkaStream
                .map(json -> mapper.readValue(json, TrendingEventRequest.class))
                .filter(event ->
                        "CREATE_POST".equals(event.getEventType()) ||
                                !event.getUserId().equals(event.getOwnerId())
                )
                .map(new TrendingScoreCalculator())
                .name("TrendingScoreCalculator");

        env.execute("Trending Score Calculator Job");
    }
}

