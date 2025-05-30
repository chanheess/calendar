package com.chpark.chcalendar.utility;

import com.chpark.chcalendar.enumClass.JwtTokenType;
import org.springframework.http.ResponseCookie;

import java.security.SecureRandom;
import java.util.UUID;

public class KeyGeneratorUtility {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateRandomString(int length) {
        if (length <= 0) throw new IllegalArgumentException("Length must be positive.");

        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            result.append(CHARACTERS.charAt(index));
        }
        return result.toString();
    }

    public static String generateMailCode(int length) {
        if (length <= 0 || length > 32) {
            throw new IllegalArgumentException("Length must be between 1 and 32 (UUID without dashes has 32 characters).");
        }

        String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return uuid.substring(0, length);
    }
}
