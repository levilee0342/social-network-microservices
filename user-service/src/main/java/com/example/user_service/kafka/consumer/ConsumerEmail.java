package com.example.user_service.kafka.consumer;

import com.example.user_service.dto.request.EmailMessageRequest;
import com.example.user_service.kafka.producer.ProducerLog;
import jakarta.mail.internet.MimeMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class ConsumerEmail {
    private final JavaMailSender mailSender;
    private final ProducerLog logProducer;
    private static final Logger logger = LogManager.getLogger(ConsumerEmail.class);

    public ConsumerEmail(JavaMailSender mailSender, ProducerLog logProducer) {
        this.mailSender = mailSender;
        this.logProducer = logProducer;
    }

    @KafkaListener(topics = "emailTopic", groupId = "email-group")
    public void receiveEmail(EmailMessageRequest dto) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setTo(dto.getTo());
            helper.setSubject(dto.getSubject());
            helper.setText(dto.getText(), dto.isHtml());
            mailSender.send(mimeMessage);
            logger.info("[Consumer Email] Send email successfully to user: {}", dto.getTo());
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[Consumer Email] Send email successfully to user : " + dto.getTo());
        } catch (Exception e) {
            logger.error("[Consumer Email] Failed to send email to kafka: {}", e.getMessage());
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Consumer Email] Failed to send email to user : " + dto.getTo() + ". Reason: " + e);
        }
    }

}