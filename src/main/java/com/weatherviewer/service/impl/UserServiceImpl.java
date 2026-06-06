package com.weatherviewer.service.impl;

import com.weatherviewer.dto.CreateUserDto;
import com.weatherviewer.dto.UpdateUserDto;
import com.weatherviewer.dto.UpdateUserRoleDto;
import com.weatherviewer.dto.UserDto;
import com.weatherviewer.exception.notfound.UserNotFoundException;
import com.weatherviewer.mapper.UserMapper;
import com.weatherviewer.model.User;
import com.weatherviewer.model.enums.Role;
import com.weatherviewer.repository.UserRepository;
import com.weatherviewer.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UUID create(CreateUserDto createUserDto) {
        String hashedPassword = passwordEncoder.encode(createUserDto.getPassword());

        User user = userMapper.fromRecord(createUserDto);
        user.setPassword(hashedPassword);

        return userRepository.save(user).getId();
    }

    @Override
    public List<UserDto> getUsers() {
        return userRepository.findAll().stream().map(userMapper::toDto).toList();
    }

    @Override
    public User getEntityById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found by id"));
    }

    @Override
    public UserDto getById(UUID id) {
        User user = getEntityById(id);
        return userMapper.toDto(user);
    }

    @Override
    public User getEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found by email"));
    }

    @Override
    public UserDto getByEmail(String email) {
        User user = getEntityByEmail(email);
        return userMapper.toDto(user);
    }

    @Override
    public void update(UUID id, UpdateUserDto updateUserDto) {
        User user = getEntityById(id);

        user.setEmail(updateUserDto.getEmail())
                .setFirstName(updateUserDto.getFirstName())
                .setLastName(updateUserDto.getLastName());

        if (updateUserDto.getPassword() != null && !updateUserDto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(updateUserDto.getPassword()));
        }
        userRepository.save(user);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void updateUserRole(UpdateUserRoleDto dto) {
        User user = getEntityById(dto.getUserId());
        Role role = Role.getInstance(dto.getNewRole());

        user.setRole(role);
        userRepository.save(user);
    }

}
