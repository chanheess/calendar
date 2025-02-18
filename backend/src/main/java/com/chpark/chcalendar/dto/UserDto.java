package com.chpark.chcalendar.dto;

import com.chpark.chcalendar.entity.UserEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDto {

    @Email(message = "Invalid email format")
    @NotEmpty(message = "Email cannot be empty")
    private String email;
    @NotBlank
    private String password;

    public UserDto(String email, String password) {
        this.email = email;
        this.password = password;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class RegisterRequest extends UserDto {
        @NotBlank
        private String nickname;
        @NotBlank
        private String emailCode;

        public RegisterRequest(String email, String password, String nickname, String emailCode) {
            super(email,password);
            this.nickname = nickname;
            this.emailCode = emailCode;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ResetPassword extends UserDto {
        @NotBlank
        private String emailCode;

        public ResetPassword(String email, String password, String emailCode) {
            super(email,password);
            this.emailCode = emailCode;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class UserInfo {
        @Email
        private String email;
        private String nickname;

        public UserInfo(String email, String nickname) {
            this.email = email;
            this.nickname = nickname;
        }

        public UserInfo(UserEntity userEntity) {
            this.email = userEntity.getEmail();
            this.nickname = userEntity.getNickname();
        }
    }

    @Getter
    @NoArgsConstructor
    public static class ChangePassword {
        @NotBlank
        private String currentPassword;
        @NotBlank
        private String newPassword;

        public ChangePassword(String currentPassword, String newPassword) {
            this.currentPassword = currentPassword;
            this.newPassword = newPassword;
        }
    }
}
