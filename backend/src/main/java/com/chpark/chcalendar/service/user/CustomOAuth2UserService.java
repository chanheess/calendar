package com.chpark.chcalendar.service.user;

import com.chpark.chcalendar.dto.user.UserDto;
import com.chpark.chcalendar.entity.UserEntity;
import com.chpark.chcalendar.entity.UserProviderEntity;
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

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserCalendarService userCalendarService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String email = oauth2User.getAttribute("email");
        
        // 세션에서 요청 타입 확인
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String type = (String) request.getSession().getAttribute("oauth2_type");

        UserEntity user = userRepository.findByEmail(email)
                .orElse(null);

        if (user != null) {
            if ("link".equals(type)) {
                // 계정 연동 요청인 경우 - 기존 사용자에 provider 추가
                addUserProvider(user, registrationId);
            } else {
                // 일반 로그인인 경우 - provider 확인
                validateUser(user, registrationId);
            }
        } else {
            // 신규 사용자인 경우
            if ("link".equals(type)) {
                throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_request", "연동할 계정이 존재하지 않습니다.", null)
                );
            }
            registerUser(email, registrationId);
        }

        return oauth2User;
    }

    private void validateUser(UserEntity user, String provider) {
        boolean matched = user.getProviders().stream()
                .anyMatch(p -> p.getProvider().equalsIgnoreCase(provider));

        if (!matched) {
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

    private void addUserProvider(UserEntity user, String provider) {
        boolean matched = user.getProviders().stream()
                .anyMatch(p -> p.getProvider().equalsIgnoreCase(provider));

        if (matched) {
            return; // 이미 연동된 경우
        }

        UserProviderEntity providerEntity = new UserProviderEntity();
        providerEntity.setProvider(provider);
        user.addProvider(providerEntity);

        userRepository.save(user);
    }
}