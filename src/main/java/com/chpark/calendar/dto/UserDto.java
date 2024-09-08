package com.chpark.calendar.dto;

import com.chpark.calendar.exception.ValidGroup;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UserDto {

    @Getter
    @NoArgsConstructor
    public static class PostRequest {
        @NotBlank(groups = ValidGroup.CreateGroup.class)
        @Email
        String email;
        @NotBlank(groups = ValidGroup.CreateGroup.class)
        String password;
        @NotBlank(groups = ValidGroup.CreateGroup.class)
        String nickname;

        public PostRequest(String email, String password, String nickname) {
            this.email = email;
            this.password = password;
            this.nickname = nickname;
        }
    }

}
