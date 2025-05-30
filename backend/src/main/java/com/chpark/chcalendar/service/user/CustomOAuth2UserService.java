package com.chpark.chcalendar.service.user;

import com.chpark.chcalendar.dto.UserDto;
import com.chpark.chcalendar.entity.UserEntity;
import com.chpark.chcalendar.repository.user.UserRepository;
import com.chpark.chcalendar.service.calendar.UserCalendarService;
import com.chpark.chcalendar.utility.KeyGeneratorUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        UserEntity user = userRepository.findByEmail(email)
                .orElse(null);

        if (user != null) {
            validateUser(user, registrationId);
        } else {
            registerUser(email, registrationId);
        }

        return oauth2User;
    }

    private void validateUser(UserEntity user, String provider) {
        boolean matched = user.getProviders().stream()
                .anyMatch(p -> p.getProvider().equalsIgnoreCase(provider));

        if (!matched) {
            throw new OAuth2AuthenticationException("해당 이메일은 다른 로그인 방식으로 이미 가입되어 있습니다.");
        }
    }

    private void registerUser(String email, String provider) {
        //랜덤 닉네임, 패스워드 생성
        String nickname = KeyGeneratorUtility.generateRandomString(20);
        String password = KeyGeneratorUtility.generateRandomString(100);

        UserDto.RegisterRequest registerDto = UserDto.RegisterRequest.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .emailCode("") // 이메일 인증 코드 없이 가입
                .provider(provider)
                .build();

        UserEntity user = userRepository.save(UserEntity.createWithEncodedPassword(registerDto, passwordEncoder));

        userCalendarService.create(user.getId(), "내 캘린더");
    }
}