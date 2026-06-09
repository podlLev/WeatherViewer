package com.weatherviewer.dto;

import com.weatherviewer.validation.annotation.Password;
import com.weatherviewer.validation.annotation.PasswordMatches;
import com.weatherviewer.validation.annotation.UniqueEmail;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    @UniqueEmail
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email cannot be blank")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;

    @Password
    @NotBlank(message = "Password cannot be blank")
    @Size(max = 72, message = "Password cannot exceed 72 characters")
    private String password;

    @Password
    @NotBlank(message = "Repeat password cannot be blank")
    @Size(max = 72, message = "Password cannot exceed 72 characters")
    private String repeatPassword;

}
