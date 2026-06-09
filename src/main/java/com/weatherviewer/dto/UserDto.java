package com.weatherviewer.dto;

import com.weatherviewer.model.enums.Role;
import com.weatherviewer.model.enums.UserStatus;
import com.weatherviewer.validation.annotation.Password;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    private String username;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email cannot be blank")
    private String email;

    @Password
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
