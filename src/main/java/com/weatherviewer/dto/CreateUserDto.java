package com.weatherviewer.dto;

import com.weatherviewer.validation.annotation.Password;
import com.weatherviewer.validation.annotation.PasswordMatches;
import com.weatherviewer.validation.annotation.UniqueEmail;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Payload for registering a new user")
public class CreateUserDto {

    @Schema(description = "First name", example = "Jane", maxLength = 50)
    @NotBlank(message = "First name cannot be blank")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @Schema(description = "Last name", example = "Doe", maxLength = 50)
    @NotBlank(message = "Last name cannot be blank")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    @Schema(description = "Email address; must be unique across all accounts", example = "jane.doe@example.com", maxLength = 150)
    @UniqueEmail
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email cannot be blank")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;

    @Schema(description = "Password (up to 72 characters, bcrypt-hashed server-side)", example = "S3curePass!", maxLength = 72)
    @Password
    @NotBlank(message = "Password cannot be blank")
    @Size(max = 72, message = "Password cannot exceed 72 characters")
    @ToString.Exclude
    private String password;

    @Schema(description = "Must match `password`", example = "S3curePass!", maxLength = 72)
    @Password
    @NotBlank(message = "Repeat password cannot be blank")
    @Size(max = 72, message = "Password cannot exceed 72 characters")
    @ToString.Exclude
    private String repeatPassword;

}
