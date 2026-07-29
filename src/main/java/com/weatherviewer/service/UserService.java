package com.weatherviewer.service;

import com.weatherviewer.dto.CreateUserDto;
import com.weatherviewer.dto.UpdateUserDto;
import com.weatherviewer.dto.UpdateUserRoleDto;
import com.weatherviewer.dto.UserDto;
import com.weatherviewer.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Business operations for creating, reading, updating, and deleting user
 * accounts, including admin-only role management.
 */
public interface UserService {

    /**
     * Registers a new user account. The password is hashed before
     * persisting and the account is created with the default {@code USER}
     * role and {@code ACTIVE} status.
     *
     * @return the ID of the newly created account
     * @throws com.weatherviewer.exception.EmailAlreadyInUseException if the email is already taken
     */
    UUID create(CreateUserDto createUserDto);

    /**
     * Returns registered user accounts a page at a time (admin use).
     * Unbounded {@code findAll()} doesn't scale once the user base grows
     * past a trivial size, so this is the only way callers can list users.
     */
    Page<UserDto> getUsers(Pageable pageable);

    /**
     * Fetches the raw {@link User} entity by ID.
     *
     * @throws com.weatherviewer.exception.notfound.UserNotFoundException if no account has that ID
     */
    User getEntityById(UUID id);

    /**
     * Fetches a user by ID as a DTO.
     *
     * @throws com.weatherviewer.exception.notfound.UserNotFoundException if no account has that ID
     */
    UserDto getById(UUID id);

    /**
     * Fetches the raw {@link User} entity by email/login.
     *
     * @throws com.weatherviewer.exception.notfound.UserNotFoundException if no account has that email
     */
    User getEntityByEmail(String email);

    /**
     * Fetches a user by email/login as a DTO.
     *
     * @throws com.weatherviewer.exception.notfound.UserNotFoundException if no account has that email
     */
    UserDto getByEmail(String email);

    /**
     * Updates an existing account's profile fields. Leaving the password
     * blank in {@code updateUserDto} keeps the current password unchanged.
     *
     * @return the updated account as a DTO
     */
    UserDto update(UUID id, UpdateUserDto updateUserDto);

    /** Checks whether an account with this email already exists. */
    boolean existsByEmail(String email);

    /** Permanently removes a user account and all of its saved locations. */
    void delete(UUID id);

    /**
     * Changes another user's role (admin-only operation).
     *
     * @throws com.weatherviewer.exception.notfound.RoleNotFoundException if the requested role name is invalid
     */
    void updateUserRole(UpdateUserRoleDto dto);

}
