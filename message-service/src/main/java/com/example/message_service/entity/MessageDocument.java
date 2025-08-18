package com.example.message_service.entity;

import com.couchbase.client.java.json.JsonObject;
import com.example.message_service.kafka.producer.ProducerLog;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Document
public class MessageDocument {
    @Id
    private String idMessage;
    private String conversationId;
    private String senderId;
    private String text;
    private String cipherText;
    private Long timestamp;
    private List<String> isReadBy;
    private List<String> deletedBy;
    private Map<String, String> encryptedKeys; // <userId, encrypted AES key>
    private String encryptionAlg;

    public JsonObject toJsonObject() {
        return JsonObject.create()
                .put("idMessage", idMessage)
                .put("conversationId", conversationId)
                .put("senderId", senderId)
                .put("cipherText", cipherText)
                .put("timestamp", timestamp)
                .put("isReadBy", isReadBy)
                .put("deletedBy", deletedBy)
                .put("encryptedKeys", encryptedKeys)
                .put("encryptionAlg", encryptionAlg);
    }

    public static MessageDocument fromJsonObject(JsonObject json) {
        MessageDocument message = new MessageDocument();
        message.setIdMessage(json.getString("idMessage"));
        message.setConversationId(json.getString("conversationId"));
        message.setSenderId(json.getString("senderId"));
        message.setText(json.getString("text"));
        message.setCipherText(json.getString("cipherText"));
        message.setTimestamp(json.getLong("timestamp"));
        message.setEncryptionAlg(json.getString("encryptionAlg"));

        if (json.getArray("isReadBy") != null) {
            message.setIsReadBy(json.getArray("isReadBy").toList().stream()
                    .map(Object::toString).collect(Collectors.toList()));
        }

        if (json.getArray("deletedBy") != null) {
            message.setDeletedBy(json.getArray("deletedBy").toList().stream()
                    .map(Object::toString).collect(Collectors.toList()));
        }

        if (json.getObject("encryptedKeys") != null) {
            message.setEncryptedKeys(json.getObject("encryptedKeys").toMap()
                    .entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> e.getKey(),
                            e -> e.getValue().toString()
                    )));
        }
        return message;
    }
}
