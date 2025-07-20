package com.chpark.chcalendar.utility;

import java.security.SecureRandom;
import java.util.UUID;

public class KeyGeneratorUtility {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private KeyGeneratorUtility() {
        // 유틸리티 클래스는 인스턴스화 방지
    }

    public static String generateRandomString(int length) {
        if (length <= 0) throw new IllegalArgumentException("Length must be positive.");

        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            result.append(CHARACTERS.charAt(index));
        }
        return result.toString();
    }

    public static String generateNumericMailCode(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive.");
        }

        StringBuilder result = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            result.append(RANDOM.nextInt(10));
        }
        return result.toString();
    }
}
