package com.weatherviewer.dto;

import com.weatherviewer.model.enums.Role;
import com.weatherviewer.model.enums.UserStatus;
import com.weatherviewer.validation.annotation.Password;
import jakarta.validation.constraints.*;
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
public class UserDto {

    @NotNull(message = "ID cannot be null")
    private UUID id;

    @NotBlank(message = "Username cannot be blank")
    @Size(max = 100, message = "Username cannot exceed 100 characters")
    private String username;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email cannot be blank")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;

    @Password
    @NotBlank(message = "Password cannot be blank")
    @Size(max = 72, message = "Password cannot exceed 72 characters")
    private String password;

    @NotNull(message = "Status cannot be null")
    private UserStatus status;

    @NotNull(message = "Role cannot be null")
    private Role role;

    @NotNull(message = "Locations list cannot be null")
    private List<LocationDto> locations;

    @NotNull(message = "CreatedAt cannot be null")
    private LocalDateTime createdAt;

}
