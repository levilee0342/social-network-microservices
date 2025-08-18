package com.example.message_service.entity;

import com.couchbase.client.java.json.JsonObject;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Document
public class AnonymousConversationDocument {

    @Id
    private String idConversation;
    private List<String> participants;
    private Long createdAt;
    private Long updatedAt;
    private boolean isAnonymous = true;

    public JsonObject toJsonObject() {
        JsonObject json = JsonObject.create()
                .put("idConversation", idConversation)
                .put("participants", participants)
                .put("createdAt", createdAt)
                .put("updatedAt", updatedAt)
                .put("isAnonymous", isAnonymous);

        return json;
    }

    public static AnonymousConversationDocument fromJsonObject(JsonObject json) {
        AnonymousConversationDocument doc = new AnonymousConversationDocument();
        doc.setIdConversation(json.getString("idConversation"));

        List<Object> participantsObj = json.getArray("participants").toList();
        doc.setParticipants(participantsObj.stream()
                .map(Object::toString)
                .collect(Collectors.toList()));

        doc.setCreatedAt(json.getLong("createdAt"));
        doc.setUpdatedAt(json.getLong("updatedAt"));

        if (json.containsKey("isAnonymous")) {
            doc.setAnonymous(json.getBoolean("isAnonymous"));
        }

        return doc;
    }
}