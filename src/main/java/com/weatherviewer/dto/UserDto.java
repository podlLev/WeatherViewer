package com.weatherviewer.dto;

import com.weatherviewer.model.enums.Role;
import com.weatherviewer.model.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@Schema(description = "A user account")
public class UserDto {

    @Schema(description = "User ID", accessMode = Schema.AccessMode.READ_ONLY)
    @NotNull(message = "ID cannot be null")
    private UUID id;

    @Schema(description = "Username", maxLength = 100)
    @NotBlank(message = "Username cannot be blank")
    @Size(max = 100, message = "Username cannot exceed 100 characters")
    private String username;

    @Schema(description = "Email address", example = "jane.doe@example.com", maxLength = 150)
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email cannot be blank")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;

    @Schema(description = "Account status")
    @NotNull(message = "Status cannot be null")
    private UserStatus status;

    @Schema(description = "Account role")
    @NotNull(message = "Role cannot be null")
    private Role role;

    @Schema(description = "Locations saved by this user")
    @NotNull(message = "Locations list cannot be null")
    private List<LocationDto> locations;

    @Schema(description = "Timestamp the account was created", accessMode = Schema.AccessMode.READ_ONLY)
    @NotNull(message = "CreatedAt cannot be null")
    private LocalDateTime createdAt;

}
