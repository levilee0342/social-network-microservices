package com.example.user_service.service.impl;

import com.example.user_service.dto.request.EmailMessageRequest;
import com.example.user_service.dto.request.LoginRequest;
import com.example.user_service.dto.request.RegisterRequest;
import com.example.user_service.dto.request.VerifyOtpRequest;
import com.example.user_service.dto.response.LoginResponse;
import com.example.user_service.dto.response.ProfileResponse;
import com.example.user_service.dto.response.PublicProfileResponse;
import com.example.user_service.entity.EventPresence;
import com.example.user_service.entity.OtpRecord;
import com.example.user_service.entity.User;
import com.example.user_service.enums.PresenceEventType;
import com.example.user_service.error.AuthErrorCode;
import com.example.user_service.error.InvalidErrorCode;
import com.example.user_service.error.NotExistedErrorCode;
import com.example.user_service.exception.AppException;
import com.example.user_service.kafka.producer.ProducerEmail;
import com.example.user_service.kafka.producer.ProducerLog;
import com.example.user_service.repository.*;
import com.example.user_service.service.ProfileService;
import com.example.user_service.service.interfaces.*;
import com.example.user_service.service.security.UserKeyService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.Map;

@Service
public class AuthServiceImpl implements IAuthService {

    private final KafkaTemplate<String, EventPresence> kafkaTemplate;
    private final ICouchbaseUserRepository ICouchbaseUserRepository;
    private final ICouchbaseTokenRepository ICouchbaseTokenRepository;
    private final ICouchbaseOtpRepository ICouchbaseOtpRepository;
    private final IRedisOtpGeneratorRepository IRedisOtpGeneratorRepository;
    private final IRedisProfileManagerRepository redisProfileManagerRepository;
    private final ProducerEmail emailSender;
    private final ITokenService ITokenService;
    private final PasswordEncoder passwordEncoder;
    private final ProducerLog logProducer;
    private final ProfileService profileService;
    private final UserKeyService userKeyService;
    private static final String USER_CACHE_PREFIX = "user::";
    private static final String OTP_CACHE_PREFIX = "otp::";

    public AuthServiceImpl(KafkaTemplate<String, EventPresence> kafkaTemplate,
                           ICouchbaseUserRepository ICouchbaseUserRepository,
                           ICouchbaseTokenRepository ICouchbaseTokenRepository,
                           ICouchbaseOtpRepository ICouchbaseOtpRepository,
                           IRedisOtpGeneratorRepository IRedisOtpGeneratorRepository,
                           IRedisOtpQueueManagerRepositoty IRedisOtpQueueManagerRepositoty,
                           IRedisProfileManagerRepository redisProfileManagerRepository,
                           ProducerEmail emailSender,
                           ITokenService ITokenService,
                           PasswordEncoder passwordEncoder,
                           ProfileService profileService,
                           ProducerLog logProducer,
                           UserKeyService userKeyService) {
        this.kafkaTemplate = kafkaTemplate;
        this.ICouchbaseUserRepository = ICouchbaseUserRepository;
        this.ICouchbaseTokenRepository = ICouchbaseTokenRepository;
        this.ICouchbaseOtpRepository = ICouchbaseOtpRepository;
        this.IRedisOtpGeneratorRepository = IRedisOtpGeneratorRepository;
        this.redisProfileManagerRepository = redisProfileManagerRepository;
        this.emailSender = emailSender;
        this.ITokenService = ITokenService;
        this.passwordEncoder = passwordEncoder;
        this.profileService = profileService;
        this.logProducer = logProducer;
        this.userKeyService = userKeyService;
        IRedisOtpQueueManagerRepositoty.initializeOtpQueue();
    }

    @Override
    public void requestOtp(@Validated RegisterRequest request) {
        String userKey = USER_CACHE_PREFIX + request.getEmail();
        String otpKey = OTP_CACHE_PREFIX + request.getEmail();
        try {
            if (ICouchbaseUserRepository.exists(userKey)) {
                logProducer.sendLog(
                        "user-service",
                        "ERROR",
                        "[User] User already exists: " + request.getEmail());
                throw new AppException(NotExistedErrorCode.USER_ALREADY_EXITS);
            }
            OtpRecord existingOtp = ICouchbaseOtpRepository.getOtp(otpKey);
            String otp;
            if (existingOtp != null && existingOtp.getExpiryTime() > Instant.now().getEpochSecond()) {
                otp = existingOtp.getOtp();
            } else {
                otp = IRedisOtpGeneratorRepository.generateAndStoreOtp();
                OtpRecord otpRecord = new OtpRecord(
                        request.getEmail(),
                        otp,
                        Instant.now().getEpochSecond() + 300
                );
                ICouchbaseOtpRepository.saveOtp(otpKey, otpRecord);
            }
            emailSender.sendOtpEmail(request.getEmail(), otp);
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[OTP] OTP sent to: " + request.getEmail());
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[OTP] Failed to send OTP to: " + request.getEmail() + ", error: " + e.getMessage());
        }
    }

    @Override
    public void register(@Validated VerifyOtpRequest request) {
        String otpKey = OTP_CACHE_PREFIX + request.getEmail();
        try {
            OtpRecord otpRecord = ICouchbaseOtpRepository.getOtp(otpKey);
            if (!otpRecord.getOtp().equals(request.getOtp()) ||
                    otpRecord.getExpiryTime() < Instant.now().getEpochSecond()) {
                logProducer.sendLog(
                        "user-service",
                        "ERROR",
                        "[OTP] Invalid or expired OTP for email: " + request.getEmail());
                throw new AppException(InvalidErrorCode.INVALID_OTP);
            }
            String userKey = USER_CACHE_PREFIX + request.getEmail();
            User user = new User();
            user.setUserId(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEmail(request.getEmail());
            ICouchbaseUserRepository.saveUser(userKey, user);
            ICouchbaseOtpRepository.removeOtp(otpKey);

            userKeyService.getPublicKeyBase64(user.getUserId());

            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[Register] User registered successfully: " + request.getEmail());
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Register] Failed to register user: " + request.getEmail() + ", error: " + e.getMessage());
        }
    }

    @Override
    public LoginResponse login(@Validated LoginRequest loginRequest) {
        String userKey = USER_CACHE_PREFIX + loginRequest.getEmail();
        try {
            User user = ICouchbaseUserRepository.getUser(userKey);
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                logProducer.sendLog(
                        "user-service",
                        "WARN",
                        "[Password] Invalid password for email: " + loginRequest.getEmail());
                throw new AppException(AuthErrorCode.INVALID_PASSWORD);
            }
            ProfileResponse profile = null;
            try {
                profile = profileService.getProfile(user.getUserId(), user.getUserId());
                //Cache Public Profile
                PublicProfileResponse publicProfile = new PublicProfileResponse();
                publicProfile.setFullName(profile.getFullName());
                publicProfile.setAvatarUrl(profile.getAvatarUrl());
                redisProfileManagerRepository.cachePublicProfile(user.getUserId(), publicProfile);
            } catch (Exception ex) {
                logProducer.sendLog(
                        "user-service",
                        "WARN",
                        "[Profile] Could not load profile for userId: " + user.getUserId());
            }
            String accessToken = ITokenService.generateAccessToken(user.getUserId());
            String refreshToken = ITokenService.generateRefreshToken(user.getUserId());
            String tokenKey = "token::" + user.getUserId();
            Map<String, String> tokens = Map.of("accessToken", accessToken, "refreshToken", refreshToken);
            ICouchbaseTokenRepository.saveTokens(tokenKey, tokens);
            //Pub sự kiện thông báo user online
            EventPresence event = new EventPresence();
            event.setUserId(user.getUserId());
            event.setEventType(PresenceEventType.ONLINE);
            kafkaTemplate.send("user-presence-topic", event);
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[ONLINE user-presence-topic] Successfully to pub event to kafka with event: " + event);
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[Login] User logged in: " + loginRequest.getEmail());
            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .userId(user.getUserId())
                    .profile(profile)
                    .build();
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Login] Login failed for email: " + loginRequest.getEmail() + ", error: " + e);
            throw new AppException(AuthErrorCode.FAILED_LOGIN);
        }
    }

    @Override
    public Map<String, String> refreshToken(String refreshToken) {
        String userId = null;
        try {
            if (ITokenService.isTokenExpired(refreshToken)) {
                logProducer.sendLog(
                        "user-service",
                        "ERROR",
                        "[Token] Refresh token expired");
                throw new AppException(InvalidErrorCode.REFRESH_TOKEN_EXPIRED);
            }
            userId = ITokenService.getUsernameFromToken(refreshToken);
            String newAccessToken = ITokenService.generateAccessToken(userId);
            String newRefreshToken = ITokenService.generateRefreshToken(userId);
            String tokenKey = "token::" + userId;
            Map<String, String> tokens = Map.of( "accessToken", newAccessToken, "refreshToken", newRefreshToken);
            ICouchbaseTokenRepository.saveTokens(tokenKey, tokens);
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[Token] Token refreshed for user: " + userId);
            return tokens;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Token] Failed to refresh token for user: " + userId + ", error: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_REFRESH_TOKEN);
        }
    }

    @Override
    public String getStoredToken(String userId) {
        String tokenKey = "token::" + userId;
        try {
            Map<String, String> tokens = ICouchbaseTokenRepository.getTokens(tokenKey);
            String accessToken = tokens.get("accessToken");
            if (ITokenService.isTokenExpired(accessToken)) {
                ICouchbaseTokenRepository.removeTokens(tokenKey);
                logProducer.sendLog(
                        "user-service",
                        "INFO",
                        "[Token] Expired token removed for user: " + userId);
                return null;
            }
            return accessToken;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Token] Failed to retrieve token for user: " + userId + ", error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void Logout(String userId) {
        String tokenKey = "token::" + userId;
        try {
            ICouchbaseTokenRepository.removeTokens(tokenKey);
            //Pub sự kiện thông báo user online
            EventPresence event = new EventPresence();
            event.setUserId(userId);
            event.setEventType(PresenceEventType.OFFLINE);
            kafkaTemplate.send("user-presence-topic", event);
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[OFFLINE user-presence-topic] Successfully to pub event to kafka with event: " + event);
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Logout] Failed to logout for user: " + userId + ", error: " + e
            );
        }
    }

    @Override
    public boolean CheckExitsUser(String userId) {
        String userKey = USER_CACHE_PREFIX + userId;
        try {
            return ICouchbaseUserRepository.exists(userKey);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Map<String, String> getMaintenanceStatus() {
        logProducer.sendLog(
                "user-service",
                "INFO",
                "Maintenance status requested");
        return Map.of("status", "maintenance", "message", "Server will be down from 1AM to 3AM on June 18, 2025");
    }
}
