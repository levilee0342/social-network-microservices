package com.example.message_service.kafka.consumer;

import com.example.message_service.dto.request.AnonymousChatRequest;
import com.example.message_service.entity.AnonymousConversationDocument;
import com.example.message_service.kafka.producer.ProducerLog;
import com.example.message_service.repository.IRedisAnonymousChatRepository;
import com.example.message_service.service.interfaces.IChatMatchingStrategyService;
import com.example.message_service.service.interfaces.IConversationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ConsumerAnonymousChatMatching {

    private final IRedisAnonymousChatRepository IAnonymousChatRepository;
    private final IChatMatchingStrategyService chatMatchingStrategyService;
    private final IConversationService conversationService;
    private final ProducerLog producerLog;

    private static final String CHAT_MATCHED_TOPIC = "chat-matched-events";
    private static final int MATCH_SEARCH_LIMIT = 50;

    public ConsumerAnonymousChatMatching(IRedisAnonymousChatRepository IAnonymousChatRepository,
                                         IChatMatchingStrategyService chatMatchingStrategyService,
                                         IConversationService conversationService,
                                         ProducerLog producerLog) {
        this.IAnonymousChatRepository = IAnonymousChatRepository;
        this.chatMatchingStrategyService = chatMatchingStrategyService;
        this.conversationService = conversationService;
        this.producerLog = producerLog;
    }

    @KafkaListener(topics = "anonymous-chat-requests",
            groupId = "anonymous_chat_matching_group",
            containerFactory = "anonymousChatKafkaListenerContainerFactory")
    public void listenForChatRequests(AnonymousChatRequest newRequest) {
        Optional<AnonymousChatRequest> existingRequestOpt = IAnonymousChatRepository.findById(newRequest.getUserId());
        if (existingRequestOpt.isPresent() && existingRequestOpt.get().isMatched()) {
            producerLog.sendLog(
                    "message-service",
                    "WARN",
                    "[KafkaConsumer] Request for user " + newRequest.getUserId() + " already matched. Skipping.");
            return;
        }

        IAnonymousChatRepository.save(newRequest);
        producerLog.sendLog(
                "message-service",
                "DEBUG",
                "[KafkaConsumer] Saved/Updated request in Redis for user: " + newRequest.getUserId());

        List<AnonymousChatRequest> potentialPartners = IAnonymousChatRepository.findPotentialMatches(newRequest, MATCH_SEARCH_LIMIT);

        if (potentialPartners.isEmpty()) {
            producerLog.sendLog(
                    "message-service",
                    "WARN",
                    "[KafkaConsumer] No potential partners found for user: " + newRequest.getUserId());
            return;
        }

        Optional<AnonymousChatRequest> matchedPartner = Optional.empty();
        for (AnonymousChatRequest partner : potentialPartners) {
            Optional<AnonymousChatRequest> currentPartnerOpt = IAnonymousChatRepository.findById(partner.getUserId());
            if (currentPartnerOpt.isPresent() && !currentPartnerOpt.get().isMatched()) {
                if (chatMatchingStrategyService.isMatching(newRequest, currentPartnerOpt.get())) {
                    matchedPartner = currentPartnerOpt;
                    break;
                }
            }
        }

        if (matchedPartner.isPresent()) {
            AnonymousChatRequest req1 = newRequest;
            AnonymousChatRequest req2 = matchedPartner.get();

            AnonymousChatRequest latestReq1 = IAnonymousChatRepository.findById(req1.getUserId()).orElse(null);
            AnonymousChatRequest latestReq2 = IAnonymousChatRepository.findById(req2.getUserId()).orElse(null);

            if (latestReq1 != null && !latestReq1.isMatched() && latestReq2 != null && !latestReq2.isMatched()) {
                AnonymousConversationDocument savedConversation = conversationService.createAnonymousConversation(List.of(req1.getUserId(), req2.getUserId()));

                req1.setMatched(true);
                req2.setMatched(true);
                IAnonymousChatRepository.save(req1);
                IAnonymousChatRepository.save(req2);

                producerLog.sendLog(
                        "message-service",
                        "INFO",
                        "[KafkaConsumer] Matched users: " + req1.getUserId() + " and " + req2.getUserId() + ". Conversation ID: " + savedConversation.getIdConversation());

                String message = String.format("{\"conversationId\": \"%s\", \"user1\": \"%s\", \"user2\": \"%s\"}",
                        savedConversation.getIdConversation(), req1.getUserId(), req2.getUserId());
                //kafkaTemplate.send(CHAT_MATCHED_TOPIC, savedConversation.getIdConversation(), message);
            } else {
                producerLog.sendLog(
                        "message-service",
                        "WARN",
                        "[KafkaConsumer] Race condition detected or already matched. User1: " + req1.getUserId() + ", User2: " + req2.getUserId());
            }
        } else {
            producerLog.sendLog(
                    "message-service",
                    "DEBUG",
                    "[KafkaConsumer] No match found for user: " + newRequest.getUserId());
        }
    }
}