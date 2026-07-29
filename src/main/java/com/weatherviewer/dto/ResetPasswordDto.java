package com.weatherviewer.dto;

import com.weatherviewer.validation.annotation.Password;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Payload for redeeming a password-reset link")
public class ResetPasswordDto {

    @Schema(description = "Password reset token from the emailed link", accessMode = Schema.AccessMode.WRITE_ONLY)
    @NotBlank(message = "Reset link is invalid")
    @ToString.Exclude
    private String token;

    @Schema(description = "New password (up to 72 characters, bcrypt-hashed server-side)", maxLength = 72)
    @Password
    @NotBlank(message = "Password cannot be blank")
    @Size(max = 72, message = "Password cannot exceed 72 characters")
    @ToString.Exclude
    private String password;

    @Schema(description = "Must match `password`", maxLength = 72)
    @NotBlank(message = "Repeat password cannot be blank")
    @Size(max = 72, message = "Password cannot exceed 72 characters")
    @ToString.Exclude
    private String repeatPassword;

}
