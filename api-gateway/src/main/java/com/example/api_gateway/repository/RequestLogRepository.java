package com.example.api_gateway.repository;

import com.example.api_gateway.entity.RequestLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestLogRepository extends JpaRepository<RequestLog, Long> {
}
