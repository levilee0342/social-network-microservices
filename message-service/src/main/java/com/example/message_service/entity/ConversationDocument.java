package com.example.message_service.entity;

import com.couchbase.client.java.json.JsonObject;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Document
public class ConversationDocument {
    @Id
    private String idConversation;
    private List<String> participants;
    private MessageDocument lastMessage;
    private Long updatedAt;
    private boolean isGroup;
    private String groupName;
    private String groupAvatar;

    public JsonObject toJsonObject() {
        JsonObject json = JsonObject.create()
                .put("idConversation", idConversation)
                .put("participants", participants)
                .put("updatedAt", updatedAt)
                .put("isGroup", isGroup);

        if (lastMessage != null) {
            json.put("lastMessage", lastMessage.toJsonObject());
        }
        if (groupName != null) {
            json.put("groupName", groupName);
        }
        if (groupAvatar != null) {
            json.put("groupAvatar", groupAvatar);
        }
        return json;
    }

    public static ConversationDocument fromJsonObject(JsonObject json) {
        ConversationDocument conversation = new ConversationDocument();
        conversation.setIdConversation(json.getString("idConversation"));
        // Convert List<Object> to List<String> for participants
        List<Object> participantsObj = json.getArray("participants").toList();
        conversation.setParticipants(participantsObj.stream()
                .map(Object::toString)
                .collect(Collectors.toList()));
        conversation.setUpdatedAt(json.getLong("updatedAt"));
        conversation.setGroup(json.getBoolean("isGroup"));
        if (json.containsKey("lastMessage")) {
            conversation.setLastMessage(MessageDocument.fromJsonObject(json.getObject("lastMessage")));
        }
        if (json.containsKey("groupName")) {
            conversation.setGroupName(json.getString("groupName"));
        }
        if (json.containsKey("groupAvatar")) {
            conversation.setGroupAvatar(json.getString("groupAvatar"));
        }
        return conversation;
    }
}