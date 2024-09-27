package com.chpark.calendar.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDto {

    @NotBlank
    @Email
    String email;
    @NotBlank
    String password;

    public UserDto(String email, String password) {
        this.email = email;
        this.password = password;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class RegisterRequest extends UserDto{
        @NotBlank
        String nickname;

        public RegisterRequest(String email, String password, String nickname) {
            super(email,password);
            this.nickname = nickname;
        }
    }
}
