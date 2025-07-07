package com.chpark.chcalendar.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;


@RequiredArgsConstructor
@Service
public class OAuthTokenService {

    private final RestTemplate restTemplate;
    private static final Logger log = LoggerFactory.getLogger(OAuthTokenService.class);

    public void revokeGoogleToken(String token) {
        String revokeEndpoint = "https://accounts.google.com/o/oauth2/revoke?token=" + token;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    revokeEndpoint, HttpMethod.POST, request, String.class
            );
            log.info("구글 토큰 폐기 요청 성공 (token 일부: {})", token.substring(0, 8));
        } catch (HttpClientErrorException.BadRequest e) {
            log.warn("이미 만료되었거나 잘못된 토큰: {}", e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("토큰 폐기 요청 중 기타 에러: {}", e.getMessage());
        }
    }
}
