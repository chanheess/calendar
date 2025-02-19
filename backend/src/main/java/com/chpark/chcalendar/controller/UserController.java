package com.chpark.chcalendar.controller;

import com.chpark.chcalendar.dto.EmailDto;
import com.chpark.chcalendar.dto.JwtAuthenticationResponseDto;
import com.chpark.chcalendar.dto.UserDto;
import com.chpark.chcalendar.enumClass.RequestType;
import com.chpark.chcalendar.security.JwtTokenProvider;
import com.chpark.chcalendar.service.RedisService;
import com.chpark.chcalendar.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final RedisService redisService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/auth/login")
    public ResponseEntity<JwtAuthenticationResponseDto> loginUser(@Validated @RequestBody UserDto userRequest, HttpServletRequest request, HttpServletResponse response) {

        String ipAddress = getIpAddress(request);
        String token = userService.login(userRequest, ipAddress);

        // JWT 토큰을 HttpOnly 쿠키로 저장
        ResponseCookie cookie = ResponseCookie.from("jwtToken", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(24 * 60 * 60)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok().body(new JwtAuthenticationResponseDto(token, "Login successful!"));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<String> logoutUser(HttpServletResponse response) {
        // jwtToken 쿠키 삭제
        ResponseCookie cookie = ResponseCookie.from("jwtToken", null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)  // 만료 시간 0으로 설정하여 쿠키 삭제
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok().body("Logged out successfully");
    }

    @GetMapping("/auth/check")
    public ResponseEntity<Boolean> checkLogin(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);

        if(token != null && jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.ok().body(true);
        } else {
            return ResponseEntity.ok().body(false);
        }
    }

    @GetMapping("/user/nickname")
    public ResponseEntity<String> getUserNickname(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        return ResponseEntity.ok().body(userService.findNickname(userId));
    }

    @GetMapping("/user/info")
    public ResponseEntity<UserDto.UserInfo> getUserInfo(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        return ResponseEntity.ok().body(userService.findUserInfo(userId));
    }

    @PatchMapping("/user/info")
    public ResponseEntity<String> updateUserInfo(@RequestBody UserDto.UserInfo userInfo, HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        userService.updateUserInfo(userId, userInfo);
        return ResponseEntity.ok().body("Edit successfully");
    }

    @PatchMapping("/user/password")
    public ResponseEntity<String> updatePassword(@Validated @RequestBody UserDto.ChangePassword changePassword, HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        userService.updatePassword(userId, changePassword);
        return ResponseEntity.ok().body("Password updated successfully.");
    }

    @PostMapping("/auth/register")
    public ResponseEntity<String> createUser(@Validated @RequestBody UserDto.RegisterRequest userRequest) {
        EmailDto emailDto = new EmailDto(userRequest.getEmail(), RequestType.REGISTER);

        redisService.verificationEmail(emailDto, userRequest.getEmailCode());
        userService.create(userRequest);

        return ResponseEntity.ok("회원가입이 성공적으로 완료되었습니다.");
    }

    @PatchMapping("/auth/change-password")
    public ResponseEntity<String> resetPassword(@Validated @RequestBody UserDto.ResetPassword userRequest, HttpServletRequest request) {
        EmailDto emailDto = new EmailDto(userRequest.getEmail(), RequestType.REGISTER);

        String ipAddress = getIpAddress(request);
        redisService.verificationEmail(emailDto, userRequest.getEmailCode());
        userService.resetPassword(userRequest, ipAddress);

        return ResponseEntity.ok("비밀번호 변경이 완료되었습니다.");
    }

    private String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}
