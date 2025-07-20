package com.chpark.chcalendar.controller.user;

import com.chpark.chcalendar.dto.user.UserProviderDto;
import com.chpark.chcalendar.enumClass.JwtTokenType;
import com.chpark.chcalendar.security.JwtTokenProvider;
import com.chpark.chcalendar.service.user.UserProviderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UserProviderController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserProviderService userProviderService;

    @GetMapping("/check/provider")
    public ResponseEntity<List<UserProviderDto>> findProvider(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        List<UserProviderDto> userProvider = userProviderService.findUserProvider(userId);

        return ResponseEntity.ok(userProvider);
    }
}
