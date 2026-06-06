package com.weatherviewer.service;

import com.weatherviewer.dto.CreateUserDto;
import com.weatherviewer.dto.UpdateUserDto;
import com.weatherviewer.dto.UpdateUserRoleDto;
import com.weatherviewer.dto.UserDto;
import com.weatherviewer.model.User;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UUID create(CreateUserDto createUserDto);

    List<UserDto> getUsers();

    User getEntityById(UUID id);

    UserDto getById(UUID id);

    User getEntityByEmail(String email);

    UserDto getByEmail(String email);

    void update(UUID id, UpdateUserDto updateUserDto);

    boolean existsByEmail(String email);

    void delete(UUID id);

    void updateUserRole(UpdateUserRoleDto dto);

}
