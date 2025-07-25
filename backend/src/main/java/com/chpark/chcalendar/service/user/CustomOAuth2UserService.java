package com.chpark.chcalendar.service.user;

import com.chpark.chcalendar.dto.user.UserDto;
import com.chpark.chcalendar.entity.UserEntity;
import com.chpark.chcalendar.entity.UserProviderEntity;
import com.chpark.chcalendar.enumClass.OAuthLoginType;
import com.chpark.chcalendar.repository.user.UserProviderRepository;
import com.chpark.chcalendar.repository.user.UserRepository;
import com.chpark.chcalendar.service.calendar.UserCalendarService;
import com.chpark.chcalendar.utility.KeyGeneratorUtility;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final UserProviderRepository userProviderRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserCalendarService userCalendarService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        String providerId = userRequest.getClientRegistration().getRegistrationId();
        String email = oauth2User.getAttribute("email");
        
        // 세션에서 요청 타입 확인
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        
        // 세션에서 OAuthLoginType 안전하게 가져오기
        Object sessionAttribute = request.getSession().getAttribute("oauth_login_type");
        OAuthLoginType type;
        
        if (sessionAttribute instanceof OAuthLoginType) {
            type = (OAuthLoginType) sessionAttribute;
        } else if (sessionAttribute instanceof String) {
            // String인 경우 enum으로 변환 시도
            try {
                type = OAuthLoginType.valueOf(((String) sessionAttribute).toUpperCase());
            } catch (IllegalArgumentException e) {
                // 잘못된 값인 경우 기본값 사용
                type = OAuthLoginType.OAUTH;
            }
        } else {
            // null이거나 다른 타입인 경우 기본값 사용
            type = OAuthLoginType.OAUTH;
        }

        List<UserProviderEntity> userProviderList = userProviderRepository.findByProviderEmail(email);

        if (!userProviderList.isEmpty()) {
            switch (type) {
                case LINK -> {
                    validateUser(userProviderList, providerId, true); //있으면 실패
                    String userEmail = (String) request.getSession().getAttribute("oauth_login_email");
                    addUserProvider(userEmail, email, providerId);
                }
                case LOCAL, OAUTH -> {
                    validateUser(userProviderList, providerId, false); //있으면 통과
                }
                default -> throw new OAuth2AuthenticationException("잘못된 요청입니다.");
            }
        } else {
            switch (type) {
                case LINK -> {
                    String userEmail = (String) request.getSession().getAttribute("oauth_login_email");
                    addUserProvider(userEmail, email, providerId);
                }
                case OAUTH ->  registerUser(email, providerId);
                default -> throw new OAuth2AuthenticationException(
                        new OAuth2Error("invalid_request", "연동할 계정이 존재하지 않습니다.", null)
                );
            }
        }

        return oauth2User;
    }

    private void validateUser(List<UserProviderEntity> userProviderList, String providerId, boolean exists) {
        boolean hasProvider = userProviderList.stream()
                .anyMatch(up -> providerId.equalsIgnoreCase(up.getProvider()));

        if (hasProvider == exists) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_provider", "해당 이메일은 다른 로그인 방식으로 이미 가입되어 있습니다.", null)
            );
        }
    }

    private void registerUser(String email, String provider) {
        String nickname = KeyGeneratorUtility.generateRandomString(20);
        String password = KeyGeneratorUtility.generateRandomString(20);

        UserDto.RegisterRequest registerDto = UserDto.RegisterRequest.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .emailCode("")
                .provider(provider)
                .build();

        UserEntity user = userRepository.save(UserEntity.createWithEncodedPassword(registerDto, passwordEncoder));
        userCalendarService.create(user.getId(), "내 캘린더");
    }

    private void addUserProvider(String email, String providerEmail, String provider) {
        UserEntity user = userRepository.findByEmail(email).orElseThrow(
                () -> new OAuth2AuthenticationException(
                        new OAuth2Error("invalid_request", "연동할 계정이 존재하지 않습니다.", null)
                )
        );

        UserProviderEntity userProviderEntity = UserProviderEntity.builder()
                .provider(provider)
                .providerEmail(providerEmail)
                .user(user)
                .build();

        user.addProvider(userProviderEntity);

        userRepository.save(user);
    }
}