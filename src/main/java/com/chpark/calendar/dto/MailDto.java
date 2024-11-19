package com.chpark.calendar.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MailDto {
    @NotNull
    private String email;
    @NotNull
    private String code;

    public MailDto(String email, String vCode) {
        this.email = email;
        this.code = vCode;
    }
}

