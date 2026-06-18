package com.weatherviewer.rest;

import com.weatherviewer.dto.CreateUserDto;
import com.weatherviewer.dto.UpdateUserDto;
import com.weatherviewer.dto.UpdateUserRoleDto;
import com.weatherviewer.dto.UserDto;
import com.weatherviewer.service.UserService;
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
 * @version 0.0.1
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('users:write')")
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID createUser(@RequestBody @Valid CreateUserDto createUserDto) {
        log.info("Request: createUser called with email={}", createUserDto.getEmail());
        return userService.create(createUserDto);
    }

    @GetMapping
    public List<UserDto> getUsers() {
        log.info("Request: getUsers called");
        return userService.getUsers();
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable UUID id) {
        log.info("Request: getUserById called with id={}", id);
        return userService.getById(id);
    }

    @GetMapping("/email")
    public UserDto getUserByEmail(@RequestParam @Email String email) {
        log.info("Request: getUserByEmail called with email={}", email);
        return userService.getByEmail(email);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('users:write')")
    public UserDto updateUser(@PathVariable UUID id, @RequestBody @Valid UpdateUserDto dto) {
        log.info("Request: updateUser called with id={}", id);
        return userService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserById(@PathVariable UUID id) {
        log.info("Request: deleteUserById called with id={}", id);
        userService.delete(id);
        log.info("User deleted successfully: id={}", id);
    }

    @PutMapping("/role")
    public void updateUserRole(@RequestBody @Valid UpdateUserRoleDto dto) {
        log.info("Request: updateUserRole called for userId={} with newRole={}", dto.getUserId(), dto.getNewRole());
        userService.updateUserRole(dto);
        log.info("User role updated successfully: userId={}", dto.getUserId());
    }

}
