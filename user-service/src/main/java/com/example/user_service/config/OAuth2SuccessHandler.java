package com.example.user_service.config;

import com.example.user_service.dto.response.LoginGoogleResponse;
import com.example.user_service.dto.response.ProfileResponse;
import com.example.user_service.dto.response.UserResponse;
import com.example.user_service.error.NotExistedErrorCode;
import com.example.user_service.exception.AppException;
import com.example.user_service.repository.ICouchbaseTokenRepository;
import com.example.user_service.service.AuthGoogleService;
import com.example.user_service.service.interfaces.ITokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthGoogleService authGoogleService;
    private final ITokenService tokenService;
    private final ICouchbaseTokenRepository tokenRepository;

    public OAuth2SuccessHandler(AuthGoogleService authGoogleService,
                                ITokenService tokenService,
                                ICouchbaseTokenRepository tokenRepository) {
        this.authGoogleService = authGoogleService;
        this.tokenService = tokenService;
        this.tokenRepository = tokenRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            throw new AppException(NotExistedErrorCode.EMAIL_OAUTH2_NOT_FOUND);
        }
        String name = oAuth2User.getAttribute("name") != null ? oAuth2User.getAttribute("name") : "Unknown";
        String avatar = oAuth2User.getAttribute("picture") != null ? oAuth2User.getAttribute("picture") : "";
        // Tạo hoặc lấy profile từ Google
        ProfileResponse profileResponse = authGoogleService.createUserFromGoogle(email, name, avatar);
        // Tạo access token
        String accessToken = tokenService.generateAccessToken(profileResponse.getUserId());
        String tokenKey = "token::" + profileResponse.getUserId();
        tokenRepository.saveTokens(tokenKey, Map.of("accessToken", accessToken));
        // Chuyển đổi ProfileResponse -> UserResponse
        UserResponse userResponse = UserResponse.builder()
                .userId(profileResponse.getUserId())
                .fullName(profileResponse.getFullName())
                .email(email)
                .phone(profileResponse.getPhone())
                .address(profileResponse.getAddress())
                .dob(profileResponse.getDateOfBirth() != null ? java.sql.Date.valueOf(profileResponse.getDateOfBirth()) : null)
                .avatar(profileResponse.getAvatarUrl())
                .build();
        // Tạo LoginGoogleResponse
        LoginGoogleResponse loginGoogleResponse = LoginGoogleResponse.builder()
                .accessToken(accessToken)
                .userData(userResponse)
                .isNewUser(false) // hoặc true nếu bạn có logic phân biệt
                .build();
        // Gửi response về client (JSON)
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        new ObjectMapper().writeValue(response.getWriter(), loginGoogleResponse);
    }

}