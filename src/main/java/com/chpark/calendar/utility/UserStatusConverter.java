package com.chpark.calendar.utility;

import com.chpark.calendar.enumClass.UserStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)  // 모든 UserStatus에 자동 적용
public class UserStatusConverter implements AttributeConverter<UserStatus, String> {

    // DB에 저장될 때 Enum -> String 변환 (대문자 -> 소문자)
    @Override
    public String convertToDatabaseColumn(UserStatus status) {
        if (status == null) {
            return null;
        }
        return status.toString();  // 소문자로 변환하여 저장
    }

    // DB에서 조회될 때 String -> Enum 변환 (소문자 -> Enum)
    @Override
    public UserStatus convertToEntityAttribute(String dbValue) {
        if (dbValue == null) {
            return null;
        }
        return UserStatus.fromValue(dbValue);  // 소문자 -> Enum으로 변환
    }
}

