package com.chpark.chcalendar.dto;

import com.chpark.chcalendar.enumClass.EmailType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmailDto {
    @Email(message = "Invalid email format")
    @NotEmpty(message = "Email cannot be empty")
    String email;

    @NotNull
    EmailType type;
}
