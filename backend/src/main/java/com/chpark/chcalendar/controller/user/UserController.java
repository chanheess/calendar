package com.chpark.chcalendar.controller.user;

import com.chpark.chcalendar.dto.EmailDto;
import com.chpark.chcalendar.dto.user.UserDto;
import com.chpark.chcalendar.dto.security.JwtAuthenticationResponseDto;
import com.chpark.chcalendar.enumClass.JwtTokenType;
import com.chpark.chcalendar.enumClass.RequestType;
import com.chpark.chcalendar.security.JwtTokenProvider;
import com.chpark.chcalendar.service.OAuthTokenService;
import com.chpark.chcalendar.service.notification.FirebaseService;
import com.chpark.chcalendar.service.redis.RedisService;
import com.chpark.chcalendar.service.user.UserService;
import com.chpark.chcalendar.utility.CookieUtility;
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
    private final FirebaseService firebaseService;
    private final JwtTokenProvider jwtTokenProvider;
    private final OAuthTokenService oAuthTokenService;

    @PostMapping("/auth/login")
    public ResponseEntity<String> loginUser(@Validated @RequestBody UserDto userRequest, HttpServletRequest request, HttpServletResponse response) {

        String ipAddress = getIpAddress(request);
        JwtAuthenticationResponseDto responseTokenDto = userService.login(userRequest, ipAddress);

        CookieUtility.setCookie(JwtTokenType.ACCESS, responseTokenDto.getAccessToken(), 60 * 60, response);
        CookieUtility.setCookie(JwtTokenType.REFRESH, responseTokenDto.getRefreshToken(), 14 * 24 * 60 * 60, response);

        return ResponseEntity.ok().body(responseTokenDto.getMessage());
    }

    @PostMapping("/auth/logout/{fcmToken}")
    public ResponseEntity<String> logoutUser(@PathVariable("fcmToken") String fcmToken,
                                            HttpServletRequest request,
                                            HttpServletResponse response) {
        revokeTokens(request, response);
        firebaseService.deleteToken(fcmToken);

        return ResponseEntity.ok().body("Logged out successfully");
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<String> logoutUser(HttpServletRequest request,
                                             HttpServletResponse response) {
        revokeTokens(request, response);

        return ResponseEntity.ok().body("Logged out successfully");
    }

    public void revokeTokens(HttpServletRequest request, HttpServletResponse response) {
        CookieUtility.setCookie(JwtTokenType.ACCESS, null, 0, response);
        CookieUtility.setCookie(JwtTokenType.REFRESH, null, 0, response);

        CookieUtility.setCookie(JwtTokenType.GOOGLE_ACCESS, null, 0, response);
        CookieUtility.setCookie(JwtTokenType.GOOGLE_REFRESH, null, 0, response);
    }

    @GetMapping("/auth/check/{fcmToken}")
    public ResponseEntity<Boolean> checkLogin(@PathVariable("fcmToken") String fcmToken,
                                              HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());

        if (token != null) {
            return ResponseEntity.ok(true);
        } else {
            firebaseService.deleteToken(fcmToken);
            return ResponseEntity.ok(false);
        }
    }

    @GetMapping("/auth/check")
    public ResponseEntity<Boolean> checkLogin(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());

        if (token != null) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.ok(false);
        }
    }

    @GetMapping("/user/nickname")
    public ResponseEntity<String> getUserNickname(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        return ResponseEntity.ok().body(userService.findNickname(userId));
    }

    @GetMapping("/user/info")
    public ResponseEntity<UserDto.UserInfo> getUserInfo(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        return ResponseEntity.ok().body(userService.findUserInfo(userId));
    }

    @GetMapping("/user/id")
    public ResponseEntity<Long> getUserId(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        return ResponseEntity.ok().body(userId);
    }

    @PatchMapping("/user/info")
    public ResponseEntity<String> updateUserInfo(@RequestBody UserDto.UserInfo userInfo, HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        userService.updateUserInfo(userId, userInfo);
        return ResponseEntity.ok().body("Edit successfully");
    }

    @PatchMapping("/user/password")
    public ResponseEntity<String> updatePassword(@Validated @RequestBody UserDto.ChangePassword changePassword, HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        userService.updatePassword(userId, changePassword);
        return ResponseEntity.ok().body("비밀번호가 변경되었습니다.");
    }

    @PostMapping("/auth/register")
    public ResponseEntity<String> createUser(@Validated @RequestBody UserDto.RegisterRequest userRequest) {
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

    @DeleteMapping("/user")
    public ResponseEntity<String> deleteAccount(HttpServletRequest request, HttpServletResponse response) {
        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        userService.deleteAccount(userId);
        revokeTokens(request, response);

        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }

    private String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}
