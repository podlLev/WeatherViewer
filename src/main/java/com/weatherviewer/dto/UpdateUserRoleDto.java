package com.weatherviewer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.UUID;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class UpdateUserRoleDto {

    @NotNull(message = "ID cannot be null")
    private UUID userId;

    @NotBlank(message = "Role cannot be blank")
    @Pattern(regexp = "^(USER|ADMIN)$", message = "Invalid role type provided")
    private String newRole;

}
