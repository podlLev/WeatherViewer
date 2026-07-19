package com.weatherviewer.dto;

import com.weatherviewer.validation.annotation.Password;
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
@Schema(description = "Payload for updating a user's profile")
public class UpdateUserDto {

    @Schema(description = "Email address", example = "jane.doe@example.com", maxLength = 150)
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email cannot be blank")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;

    @Schema(description = "First name", example = "Jane", maxLength = 50)
    @NotBlank(message = "First name cannot be blank")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @Schema(description = "Last name", example = "Doe", maxLength = 50)
    @NotBlank(message = "Last name cannot be blank")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    @Schema(description = "New password. Omit or leave blank to keep the current password.", maxLength = 72)
    @Password
    @Size(max = 72, message = "Password cannot exceed 72 characters")
    @ToString.Exclude
    private String password;

    @Schema(description = "Preferred unit system for displaying weather (\"METRIC\" or \"IMPERIAL\"). Omit to keep the current preference.", example = "IMPERIAL")
    private String units;

}
