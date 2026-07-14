package com.weatherviewer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Payload for changing a user's role")
public class UpdateUserRoleDto {

    @Schema(description = "ID of the user whose role is being changed")
    @NotNull(message = "ID cannot be null")
    private UUID userId;

    @Schema(description = "New role", allowableValues = {"USER", "ADMIN"}, example = "ADMIN")
    @NotBlank(message = "Role cannot be blank")
    @Pattern(regexp = "^(USER|ADMIN)$", message = "Invalid role type provided")
    private String newRole;

}
