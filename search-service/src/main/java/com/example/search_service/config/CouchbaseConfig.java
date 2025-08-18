package com.example.search_service.config;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.codec.JacksonJsonSerializer;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CouchbaseConfig {

    @Value("${spring.data.couchbase.connection-string}")
    private String connectionString;

    //Search Bucket
    @Value("${spring.data.couchbase.username}")
    private String usernameSearchBucket;

    @Value("${spring.data.couchbase.password}")
    private String passwordSearchBucket;

    @Value("${spring.data.couchbase.bucket.name}")
    private String searchBucketName;

    //Profile Bucket
    @Value("${custom.couchbase.buckets.profile.username}")
    private String usernameProfileBucket;

    @Value("${custom.couchbase.buckets.profile.password}")
    private String passwordProfileBucket;

    @Value("${custom.couchbase.buckets.profile.name}")
    private String profileBucketName;

    //Friend Bucket
    @Value("${custom.couchbase.buckets.friend.username}")
    private String usernameFriendBucket;

    @Value("${custom.couchbase.buckets.friend.password}")
    private String passwordFriendBucket;

    @Value("${custom.couchbase.buckets.friend.name}")
    private String friendBucketName;

    // Cluster cho Search Bucket
    @Bean(name ="searchClusterEnvironment")
    public ClusterEnvironment searchClusterEnvironment() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return ClusterEnvironment.builder()
                .timeoutConfig(timeout -> timeout
                        .connectTimeout(Duration.ofSeconds(15))
                        .kvTimeout(Duration.ofSeconds(10))
                        .queryTimeout(Duration.ofSeconds(20))
                )
                .jsonSerializer(JacksonJsonSerializer.create(objectMapper))
                .securityConfig(config -> config.enableTls(true))
                .build();
    }

    @Bean(name = "searchCouchbaseCluster")
    public Cluster searchcouchbaseCluster(@Qualifier("searchClusterEnvironment") ClusterEnvironment userClusterEnvironment) {
        try {
            Cluster cluster = Cluster.connect(
                    connectionString,
                    ClusterOptions.clusterOptions(usernameSearchBucket, passwordSearchBucket)
                            .environment(userClusterEnvironment)
            );
            cluster.waitUntilReady(Duration.ofSeconds(15));
            return cluster;
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to Couchbase cluster", e);
        }
    }

    @Bean(name = "searchBucket")
    public Bucket userBucket(@Qualifier("searchCouchbaseCluster") Cluster userCouchbaseCluster) {
        Bucket bucket = userCouchbaseCluster.bucket(searchBucketName);
        bucket.waitUntilReady(Duration.ofSeconds(15));
        return bucket;
    }

    // Cluster cho Profile Bucket
    @Bean(name = "profileClusterEnvironment")
    public ClusterEnvironment profileClusterEnvironment() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return ClusterEnvironment.builder()
                .timeoutConfig(timeout -> timeout
                        .connectTimeout(Duration.ofSeconds(15))
                        .kvTimeout(Duration.ofSeconds(10))
                        .queryTimeout(Duration.ofSeconds(20))
                )
                .jsonSerializer(JacksonJsonSerializer.create(objectMapper))
                .securityConfig(config -> config.enableTls(true))
                .build();
    }

    @Bean(name = "profileCouchbaseCluster")
    public Cluster profilecouchbaseCluster(@Qualifier("profileClusterEnvironment") ClusterEnvironment clusterEnvironment) {
        try {
            Cluster cluster = Cluster.connect(
                    connectionString,
                    ClusterOptions.clusterOptions(usernameProfileBucket, passwordProfileBucket)
                            .environment(clusterEnvironment)
            );
            cluster.waitUntilReady(Duration.ofSeconds(15));
            return cluster;
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to Couchbase cluster", e);
        }
    }

    @Bean(name = "profileBucket")
    public Bucket profileBucket(@Qualifier("profileCouchbaseCluster") Cluster couchbaseCluster) {
        Bucket bucket = couchbaseCluster.bucket(profileBucketName);
        bucket.waitUntilReady(Duration.ofSeconds(15));
        return bucket;
    }

    // Cluster cho Friend Bucket
    @Bean(name = "friendClusterEnvironment")
    public ClusterEnvironment friendclusterEnvironment() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return ClusterEnvironment.builder()
                .timeoutConfig(timeout -> timeout
                        .connectTimeout(Duration.ofSeconds(15))
                        .kvTimeout(Duration.ofSeconds(10))
                        .queryTimeout(Duration.ofSeconds(20))
                )
                .jsonSerializer(JacksonJsonSerializer.create(objectMapper))
                .securityConfig(config -> config.enableTls(true))
                .build();
    }

    @Bean(name = "friendCouchbaseCluster")
    public Cluster friendcouchbaseCluster(@Qualifier("friendClusterEnvironment") ClusterEnvironment friendClusterEnvironment) {
        try {
            Cluster cluster = Cluster.connect(
                    connectionString,
                    ClusterOptions.clusterOptions(usernameFriendBucket, passwordFriendBucket)
                            .environment(friendClusterEnvironment)
            );
            cluster.waitUntilReady(Duration.ofSeconds(15));
            return cluster;
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to Couchbase cluster", e);
        }
    }

    @Bean(name = "friendBucket")
    public Bucket friendBucket(@Qualifier("friendCouchbaseCluster") Cluster friendCouchbaseCluster) {
        Bucket bucket = friendCouchbaseCluster.bucket(friendBucketName);
        bucket.waitUntilReady(Duration.ofSeconds(15));
        return bucket;
    }
}
