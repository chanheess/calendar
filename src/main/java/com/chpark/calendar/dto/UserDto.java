package com.chpark.calendar.dto;

import com.chpark.calendar.entity.UserEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDto {

    @NotBlank()
    @Email
    private String email;
    @NotBlank()
    private String password;

    public UserDto(String email, String password) {
        this.email = email;
        this.password = password;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class RegisterRequest extends UserDto{
        @NotBlank
        private String nickname;

        public RegisterRequest(String email, String password, String nickname) {
            super(email,password);
            this.nickname = nickname;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class UserInfo {
        @Email
        private String email;
        private String nickname;

        public UserInfo(UserEntity userInfo) {
            this.email = userInfo.getEmail();
            this.nickname = userInfo.getNickname();
        }
    }
}
