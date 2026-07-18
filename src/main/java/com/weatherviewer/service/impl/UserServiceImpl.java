package com.weatherviewer.service.impl;

import com.weatherviewer.dto.CreateUserDto;
import com.weatherviewer.dto.UpdateUserDto;
import com.weatherviewer.dto.UpdateUserRoleDto;
import com.weatherviewer.dto.UserDto;
import com.weatherviewer.exception.EmailAlreadyInUseException;
import com.weatherviewer.exception.notfound.UserNotFoundException;
import com.weatherviewer.mapper.UserMapper;
import com.weatherviewer.model.User;
import com.weatherviewer.model.enums.Role;
import com.weatherviewer.repository.UserRepository;
import com.weatherviewer.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Default {@link UserService} implementation backed by {@link UserRepository}.
 * Owns password hashing on create/update and the uniqueness check that
 * guards email changes.
 */
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
    @Transactional(readOnly = true)
    public Page<UserDto> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toDto);
    }

    @Override
    public User getEntityById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found by id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getById(UUID id) {
        User user = getEntityById(id);
        return userMapper.toDto(user);
    }

    @Override
    public User getEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found by email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getByEmail(String email) {
        User user = getEntityByEmail(email);
        return userMapper.toDto(user);
    }

    /**
     * Updates profile fields. If the email is being changed, re-checks
     * uniqueness first (the current email is exempt from its own
     * uniqueness check). The password is only re-hashed and updated when a
     * non-blank new password is supplied; otherwise the existing hash is
     * left untouched.
     */
    @Override
    @Transactional
    public UserDto update(UUID id, UpdateUserDto updateUserDto) {
        User user = getEntityById(id);

        if (!user.getEmail().equals(updateUserDto.getEmail())
                && existsByEmail(updateUserDto.getEmail())) {
            throw new EmailAlreadyInUseException(updateUserDto.getEmail());
        }

        user.setEmail(updateUserDto.getEmail())
                .setFirstName(updateUserDto.getFirstName())
                .setLastName(updateUserDto.getLastName());

        if (updateUserDto.getPassword() != null && !updateUserDto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(updateUserDto.getPassword()));
        }

        userRepository.save(user);
        return userMapper.toDto(user);
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
    }

}
