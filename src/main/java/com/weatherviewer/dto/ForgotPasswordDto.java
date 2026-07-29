package com.weatherviewer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@Schema(description = "Payload for requesting a password reset email")
public class ForgotPasswordDto {

    @Schema(description = "Email address of the account to reset", example = "jane.doe@example.com", maxLength = 150)
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email cannot be blank")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;

}
