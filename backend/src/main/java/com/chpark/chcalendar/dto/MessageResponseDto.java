package com.chpark.chcalendar.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageResponseDto {
    private int status;
    private String message;

    public MessageResponseDto(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
