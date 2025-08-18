package com.example.log_service.repository;

import com.example.log_service.entity.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LogRepository extends JpaRepository<Log, Long> {

    @Query(value = "SELECT * FROM logs l WHERE " +
            "(:serviceName IS NULL OR l.service_name = :serviceName) AND " +
            "(:logLevel IS NULL OR l.log_level = :logLevel) AND " +
            "(:keyword IS NULL OR LOWER(CAST(l.message AS TEXT)) LIKE LOWER(CONCAT('%', :keyword, '%'))) ",
            nativeQuery = true)
    Page<Log> findLogs(
            @Param("serviceName") String serviceName,
            @Param("logLevel") String logLevel,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query(value = "SELECT COUNT(l) FROM logs l WHERE l.log_level = :logLevel AND " +
            "(:serviceName IS NULL OR l.service_name = :serviceName) AND " +
            "(:keyword IS NULL OR LOWER(CAST(l.message AS TEXT)) LIKE LOWER(CONCAT('%', :keyword, '%'))) ",
            nativeQuery = true)
    Long countByLevel(
            @Param("logLevel") String logLevel,
            @Param("serviceName") String serviceName,
            @Param("keyword") String keyword);

    @Query(value = "SELECT l.log_level AS logLevel, COUNT(l) AS count FROM logs l WHERE " +
            "(:serviceName IS NULL OR l.service_name = :serviceName) AND " +
            "(:keyword IS NULL OR LOWER(CAST(l.message AS TEXT)) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "GROUP BY l.log_level",
            nativeQuery = true)
    List<Object[]> countByAllLevels(
            @Param("serviceName") String serviceName,
            @Param("keyword") String keyword);
}