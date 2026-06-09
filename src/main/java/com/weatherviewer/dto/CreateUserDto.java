package com.weatherviewer.dto;

import com.weatherviewer.validation.annotation.Password;
import com.weatherviewer.validation.annotation.PasswordMatches;
import com.weatherviewer.validation.annotation.UniqueEmail;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@PasswordMatches
public class CreateUserDto {

    @NotBlank(message = "First name cannot be blank")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    private String lastName;

    @UniqueEmail
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email cannot be blank")
    private String email;

    @Password
    @NotBlank(message = "Password cannot be blank")
    private String password;

    @Password
    @NotBlank(message = "Repeat password cannot be blank")
    private String repeatPassword;

}
