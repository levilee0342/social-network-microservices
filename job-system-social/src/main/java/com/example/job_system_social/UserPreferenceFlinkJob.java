package com.example.job_system_social;

import com.example.job_system_social.model.UserInteractionEventRequest;
import com.example.job_system_social.user_preference_score.JacksonDeserializationSchema;
import com.example.job_system_social.user_preference_score.PreferenceAggregator;
import com.example.job_system_social.user_preference_score.PreferencePostgresSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class UserPreferenceFlinkJob {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(4);

        Properties props = new Properties();
        props.setProperty("bootstrap.servers", "kafka:29092");
        props.setProperty("group.id", "user-preference-updater");

        FlinkKafkaConsumer<UserInteractionEventRequest> consumer = new FlinkKafkaConsumer<>(
                "user-interactions",
                new JacksonDeserializationSchema<>(UserInteractionEventRequest.class),
                props
        );

        DataStream<UserInteractionEventRequest> stream = env.addSource(consumer)
                .name("KafkaSource");

        stream
                .flatMap((UserInteractionEventRequest event, Collector<UserInteractionEventRequest> out) -> {
                    List<Long> typeIds = new ArrayList<>(event.getTypeIds());
                    for (Long typeId : typeIds) {
                        UserInteractionEventRequest single = new UserInteractionEventRequest();
                        single.setUserId(event.getUserId());
                        single.setEventType(event.getEventType());
                        single.setTypeId(typeId);
                        single.setTimestamp(event.getTimestamp());
                        out.collect(single);
                    }
                })
                .returns(UserInteractionEventRequest.class)
                .keyBy(e -> e.getUserId() + "-" + e.getTypeId())
                .window(TumblingProcessingTimeWindows.of(Time.seconds(10)))
                .aggregate(new PreferenceAggregator())
                .name("PreferenceAggregator")
                .addSink(new PreferencePostgresSink())
                .name("PreferencePostgresSink");

        env.execute("User Preference Score Updater");
        System.out.println("[DEBUG] Completed execution...");
    }
}