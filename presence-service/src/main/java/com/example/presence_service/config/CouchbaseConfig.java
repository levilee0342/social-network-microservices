package com.example.presence_service.config;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.codec.JacksonJsonSerializer;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.core.convert.CouchbaseCustomConversions;

import java.time.Duration;
import java.util.Collections;

@Configuration
public class CouchbaseConfig extends AbstractCouchbaseConfiguration {

    @Value("${spring.data.couchbase.connection-string}")
    private String connectionString;

    @Value("${spring.data.couchbase.username}")
    private String username;

    @Value("${spring.data.couchbase.password}")
    private String password;

    @Value("${spring.data.couchbase.bucket.name}")
    private String presenceBucketName;

    @Override
    public String getConnectionString() {
        return connectionString;
    }

    @Override
    public String getUserName() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getBucketName() {
        return presenceBucketName;
    }

    @Bean
    public ClusterEnvironment clusterEnvironment() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return ClusterEnvironment.builder()
                .timeoutConfig(timeout -> timeout
                        .connectTimeout(Duration.ofSeconds(15))
                        .kvTimeout(Duration.ofSeconds(10))
                        .queryTimeout(Duration.ofSeconds(20))
                )
                .jsonSerializer(JacksonJsonSerializer.create(objectMapper))
                .securityConfig(securityConfig -> securityConfig.enableTls(true))
                .build();
    }

    @Bean(name = "couchbaseCluster")
    public Cluster couchbaseCluster(ClusterEnvironment clusterEnvironment) {
        try {
            Cluster cluster = Cluster.connect(
                    getConnectionString(),
                    ClusterOptions.clusterOptions(getUserName(), getPassword())
                            .environment(clusterEnvironment)
            );
            cluster.waitUntilReady(Duration.ofSeconds(5));
            return cluster;
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to Couchbase cluster", e);
        }
    }

    @Bean
    public Bucket presenceBucket(Cluster couchbaseCluster) {
        Bucket bucket = couchbaseCluster.bucket(presenceBucketName);
        bucket.waitUntilReady(Duration.ofSeconds(5));
        return bucket;
    }

    @Override
    @Bean(name = "couchbaseCustomConversions")
    @Primary
    public CouchbaseCustomConversions customConversions() {
        return new CouchbaseCustomConversions(Collections.emptyList());
    }
}
