package com.example.notification_service.service.interfaces;

import java.util.List;

public interface IContentGenerator {
    String generateGroupedContent(String eventType, List<String> actorIds);
}