package com.weatherviewer.rest;

import com.weatherviewer.dto.CreateUserDto;
import com.weatherviewer.dto.UpdateUserDto;
import com.weatherviewer.dto.UpdateUserRoleDto;
import com.weatherviewer.dto.UserDto;
import com.weatherviewer.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing system user accounts.
 * <p>
 * This controller handles administrative actions for user provisioning and maintenance.
 * <b>Security Note:</b> Unlike other controllers, the entire controller is secured with
 * {@code @PreAuthorize("hasAuthority('users:write')")}, meaning all operations
 * (including read-only lists) require administrator-level privileges.
 * <p>
 * Provided administrative capabilities include:
 * <ul>
 * <li><b>User Provisioning:</b> Direct creation of new accounts with password validation.</li>
 * <li><b>Account Inspection:</b> Searching and listing active users by their unique ID or email address.</li>
 * <li><b>Profile Maintenance:</b> Modifying personal details and optionally resetting passwords.</li>
 * <li><b>Access Control:</b> Promoting or demoting user roles between {@code USER} and {@code ADMIN}.</li>
 * <li><b>Account Deletion:</b> Removing user profiles from the system.</li>
 * </ul>
 *
 * @author Lev Pidlisnyi
 * @version 1.0.0
 * @since 2026
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('users:write')")
@Tag(name = "Users", description = "User account management. Every endpoint requires the users:write authority")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Not authenticated - missing or invalid session cookie",
                content = @Content(mediaType = "text/plain",
                        examples = @ExampleObject(value = "Full authentication is required to access this resource"))),
        @ApiResponse(responseCode = "403", description = "Authenticated but missing the users:write authority",
                content = @Content(mediaType = "text/plain",
                        examples = @ExampleObject(value = "Access Denied"))),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded (60 requests/60s for this API by default)",
                content = @Content(mediaType = "text/plain",
                        examples = @ExampleObject(value = "Rate limit exceeded. Try again later.")))
})
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Create a user",
            description = "Creates a new user account. `password` and `repeatPassword` must match."
    )
    @ApiResponse(responseCode = "201", description = "User created; returns the new user's ID",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UUID.class)))
    @ApiResponse(responseCode = "400", description = "Validation failed (blank/oversized fields, invalid email, " +
            "weak or mismatched password, or the email is already in use)",
            content = @Content(mediaType = "text/plain",
                    examples = @ExampleObject(value = "Email is already in use")))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID createUser(@RequestBody @Valid CreateUserDto createUserDto) {
        log.info("Request: createUser called with email={}", createUserDto.getEmail());
        return userService.create(createUserDto);
    }

    @Operation(summary = "List all users")
    @ApiResponse(responseCode = "200", description = "All user accounts",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = UserDto.class))))
    @GetMapping
    public List<UserDto> getUsers() {
        log.info("Request: getUsers called");
        return userService.getUsers();
    }

    @Operation(summary = "Get a user by ID")
    @ApiResponse(responseCode = "200", description = "The requested user",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class)))
    @ApiResponse(responseCode = "404", description = "No user with the given ID",
            content = @Content(mediaType = "text/plain",
                    examples = @ExampleObject(value = "User not found: id=3fa85f64-5717-4562-b3fc-2c963f66afa6")))
    @GetMapping("/{id}")
    public UserDto getUserById(@Parameter(description = "User ID") @PathVariable UUID id) {
        log.info("Request: getUserById called with id={}", id);
        return userService.getById(id);
    }

    @Operation(summary = "Get a user by email")
    @ApiResponse(responseCode = "200", description = "The requested user",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class)))
    @ApiResponse(responseCode = "404", description = "No user with the given email",
            content = @Content(mediaType = "text/plain",
                    examples = @ExampleObject(value = "User not found: email=jane.doe@example.com")))
    @GetMapping("/email")
    public UserDto getUserByEmail(
            @Parameter(description = "Email address to search for", example = "jane.doe@example.com")
            @RequestParam @Email String email) {
        log.info("Request: getUserByEmail called with email={}", email);
        return userService.getByEmail(email);
    }

    @Operation(summary = "Update a user's profile fields", description = "Updates email, first/last name and " +
            "optionally the password. Omit `password` to leave it unchanged.")
    @ApiResponse(responseCode = "200", description = "The updated user",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content(mediaType = "text/plain"))
    @ApiResponse(responseCode = "404", description = "No user with the given ID",
            content = @Content(mediaType = "text/plain",
                    examples = @ExampleObject(value = "User not found: id=3fa85f64-5717-4562-b3fc-2c963f66afa6")))
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('users:write')")
    public UserDto updateUser(
            @Parameter(description = "User ID") @PathVariable UUID id,
            @RequestBody @Valid UpdateUserDto dto) {
        log.info("Request: updateUser called with id={}", id);
        return userService.update(id, dto);
    }

    @Operation(summary = "Delete a user")
    @ApiResponse(responseCode = "204", description = "User deleted")
    @ApiResponse(responseCode = "404", description = "No user with the given ID",
            content = @Content(mediaType = "text/plain",
                    examples = @ExampleObject(value = "User not found: id=3fa85f64-5717-4562-b3fc-2c963f66afa6")))
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserById(@Parameter(description = "User ID") @PathVariable UUID id) {
        log.info("Request: deleteUserById called with id={}", id);
        userService.delete(id);
        log.info("User deleted successfully: id={}", id);
    }

    @Operation(summary = "Change a user's role", description = "`newRole` must be either `USER` or `ADMIN`.")
    @ApiResponse(responseCode = "200", description = "Role updated")
    @ApiResponse(responseCode = "400", description = "Invalid role or blank value",
            content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "Invalid role type provided")))
    @ApiResponse(responseCode = "404", description = "No user or role with the given identifier",
            content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "Role not found: ADMIN")))
    @PutMapping("/role")
    public void updateUserRole(@RequestBody @Valid UpdateUserRoleDto dto) {
        log.info("Request: updateUserRole called for userId={} with newRole={}", dto.getUserId(), dto.getNewRole());
        userService.updateUserRole(dto);
        log.info("User role updated successfully: userId={}", dto.getUserId());
    }

}
