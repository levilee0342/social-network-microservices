package com.example.user_service.repository.impl;

import com.couchbase.client.java.query.QueryResult;
import com.example.user_service.dto.response.ProfileResponse;
import com.example.user_service.error.AuthErrorCode;
import com.example.user_service.exception.AppException;
import com.example.user_service.kafka.producer.ProducerLog;
import com.example.user_service.repository.ICouchbaseSearchProfileRepository;
import com.example.user_service.service.interfaces.IProfileMapperService;
import com.couchbase.client.java.Cluster;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class CouchbaseSearchProfileRepositoryImpl implements ICouchbaseSearchProfileRepository {

    private final Cluster cluster;
    private final IProfileMapperService profileMapper;
    private final ProducerLog logProducer;

    public CouchbaseSearchProfileRepositoryImpl(@Qualifier("profileCouchbaseCluster") Cluster cluster,
                                                IProfileMapperService profileMapper,
                                                ProducerLog logProducer) {
        this.cluster = cluster;
        this.profileMapper = profileMapper;
        this.logProducer = logProducer;
    }

    @Override
    public List<ProfileResponse> search(String fullName, String address, String phone) {
        String fName = fullName != null ? fullName.toLowerCase() : "";
        String addr = address != null ? address.toLowerCase() : "";
        String ph = phone != null ? phone : "";
        String query = String.format("""
            SELECT META(p).id AS id, p.*
            FROM `profile_bucket`.`user_information`.`profiles` p
            WHERE LOWER(p.fullName) LIKE '%%%s%%'
              AND LOWER(p.address) LIKE '%%%s%%'
              AND p.phone LIKE '%%%s%%'
        """, fName, addr, ph);
        try {
            QueryResult result = cluster.query(query);
            List<ProfileResponse> responses = result.rowsAsObject().stream()
                    .map(profileMapper::mapJsonToProfile)
                    .collect(Collectors.toList());
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[Search Profile] Found " + responses.size() + " profiles");
            return responses;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Search Profile] Failed to search profiles. Reason: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_SEARCH_PROFILE);
        }
    }
}
