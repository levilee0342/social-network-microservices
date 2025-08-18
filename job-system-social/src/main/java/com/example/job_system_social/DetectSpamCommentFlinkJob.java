package com.example.job_system_social;

import com.example.job_system_social.detect_spam_comment.CommentEventDeserializationSchema;
import com.example.job_system_social.detect_spam_comment.DetectSpamComment;
import com.example.job_system_social.model.CommentEventRequest;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import java.time.Duration;
import java.util.Properties;

public class DetectSpamCommentFlinkJob {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties kafkaProps = new Properties();
        kafkaProps.setProperty("bootstrap.servers", "kafka:29092");
        kafkaProps.setProperty("group.id", "spam-comment-detector");

        FlinkKafkaConsumer<CommentEventRequest> consumer = new FlinkKafkaConsumer<>(
                "user-interactions",
                new CommentEventDeserializationSchema(),
                kafkaProps
        );

        env
            .addSource(consumer)
            .filter(event -> "COMMENT".equals(event.getEventType()))
            .assignTimestampsAndWatermarks(WatermarkStrategy
                    .<CommentEventRequest>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                    .withTimestampAssigner((event, timestamp) -> event.getTimestamp()))
            .keyBy(CommentEventRequest::getUserId)
            .window(SlidingEventTimeWindows.of(Time.minutes(1), Time.seconds(10))) // kiểm tra mỗi 10s trong 1 phút
            .process(new DetectSpamComment())
            .print(); // hoặc gửi đến Kafka topic khác

        env.execute("Spam Comment Detector Job");
    }
}
