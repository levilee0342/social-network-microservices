package com.example.job_system_social.detect_spam_comment;

import com.example.job_system_social.model.BlockUserEvent;
import com.example.job_system_social.model.CommentEventRequest;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DetectSpamComment extends ProcessWindowFunction<CommentEventRequest, String, String, TimeWindow> {

    private static final Properties producerProps = new Properties();
    private static final BlockUserKafkaProducer blockProducer;

    static {
        producerProps.put("bootstrap.servers", "kafka:29092");
        producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        blockProducer = new BlockUserKafkaProducer(producerProps);
    }

    @Override
    public void process(String userId, Context context, Iterable<CommentEventRequest> events, Collector<String> out) {
        Map<String, Integer> contentCount = new HashMap<>();
        int total = 0;
        for (CommentEventRequest event : events) {
            total++;
            contentCount.merge(event.getContent(), 1, Integer::sum);
        }
        //Rule 1: Gửi hơn 10 comment trong 1 phút
        //Rule 2: Gửi cùng 1 nội dung 5 lần trở lên
        boolean isSpam = total > 10 || contentCount.values().stream().anyMatch(count -> count >= 5);
        if (isSpam) {
            String reason = "SPAM_COMMENT: " + total + " comments, repeated content";
            // In ra log
            out.collect("🚨 SPAM detected: user=" + userId + ", reason=" + reason);
            // Gửi cảnh báo về hệ thống
            blockProducer.send(new BlockUserEvent(userId, reason, System.currentTimeMillis()));
            //Gửi cảnh báo cho user
            blockProducer.sendBlockUserNotification(userId);
        }
    }
}

