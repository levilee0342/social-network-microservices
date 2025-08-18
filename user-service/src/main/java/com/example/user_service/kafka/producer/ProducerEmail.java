package com.example.user_service.kafka.producer;

import com.example.user_service.dto.request.EmailMessageRequest;
import com.example.user_service.error.AuthErrorCode;
import com.example.user_service.exception.AppException;
import com.example.user_service.utils.EmailTemplateUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ProducerEmail{

    private final KafkaTemplate<String, EmailMessageRequest> kafkaTemplate;
    private final EmailTemplateUtil emailTemplateUtil;
    private static final String EMAIL_TOPIC = "emailTopic";

    private final ProducerLog logProducer;
    private static final Logger logger = LogManager.getLogger(ProducerEmail.class);

    public ProducerEmail(KafkaTemplate<String, EmailMessageRequest> kafkaTemplate,
                         EmailTemplateUtil emailTemplateUtil,
                         ProducerLog logProducer) {
        this.kafkaTemplate = kafkaTemplate;
        this.emailTemplateUtil = emailTemplateUtil;
        this.logProducer = logProducer;
    }

    public void sendOtpEmail(String email, String otp) {
        try {
            String htmlTemplate = emailTemplateUtil.loadEmailTemplate("templates/email.html");
            String htmlContent = htmlTemplate.replace("{{otp}}", otp);
            EmailMessageRequest message = new EmailMessageRequest(
                    email,
                    "[LoLSocial] Mã xác thực OTP",
                    htmlContent,
                    true
            );
            kafkaTemplate.send(EMAIL_TOPIC, message);
            logger.info("[Producer Email] Send email successfully to kafka with message : {}", message);
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[Producer Email] Send email successfully to kafka with message : " + message);
        } catch (IOException e) {
            logger.error("[Producer Email] Failed to send email to kafka: {}", e.getMessage());
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Producer Email] Failed to send email to kafka. Reason: " + e);
            throw new AppException(AuthErrorCode.FAILED_READ_TEMPLATE_EMAIL);
        }
    }
}