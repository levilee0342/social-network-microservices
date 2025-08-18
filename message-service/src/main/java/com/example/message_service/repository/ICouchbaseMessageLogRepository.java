package com.example.message_service.repository;

import com.example.message_service.entity.MessageDocument;
import org.springframework.stereotype.Repository;


public interface ICouchbaseMessageLogRepository {
    void saveMessageLog(MessageDocument message);;
}
