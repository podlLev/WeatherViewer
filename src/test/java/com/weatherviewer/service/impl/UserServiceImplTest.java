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
import com.weatherviewer.model.enums.UserStatus;
import com.weatherviewer.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl service;

    private User user(UUID id) {
        return (User) new User()
                .setEmail("john@example.com")
                .setFirstName("John")
                .setLastName("Doe")
                .setPassword("hashed")
                .setStatus(UserStatus.ACTIVE)
                .setRole(Role.USER)
                .setId(id);
    }

    @Test
    void create_encodesPasswordAndSaves() {
        UUID id = UUID.randomUUID();
        CreateUserDto dto = new CreateUserDto()
                .setEmail("john@example.com")
                .setPassword("Secure1@");
        User user = user(id);

        when(passwordEncoder.encode("Secure1@")).thenReturn("hashed");
        when(userMapper.fromRecord(dto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        UUID result = service.create(dto);

        assertThat(result).isEqualTo(id);
        verify(passwordEncoder).encode("Secure1@");
        verify(userRepository).save(user);
    }

    @Test
    void getUsers_returnsMappedPage() {
        UUID id = UUID.randomUUID();
        User user = user(id);
        UserDto dto = new UserDto();
        Pageable pageable = PageRequest.of(0, 20);

        when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(user)));
        when(userMapper.toDto(user)).thenReturn(dto);

        assertThat(service.getUsers(pageable).getContent()).containsExactly(dto);
    }

    @Test
    void getEntityById_returnsUser() {
        UUID id = UUID.randomUUID();
        User user = user(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        assertThat(service.getEntityById(id)).isEqualTo(user);
    }

    @Test
    void getEntityById_notFound_throwsUserNotFoundException() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEntityById(id))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void getById_returnsMappedDto() {
        UUID id = UUID.randomUUID();
        User user = user(id);
        UserDto dto = new UserDto();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(dto);

        assertThat(service.getById(id)).isEqualTo(dto);
    }

    @Test
    void getEntityByEmail_returnsUser() {
        UUID id = UUID.randomUUID();
        User user = user(id);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        assertThat(service.getEntityByEmail("john@example.com")).isEqualTo(user);
    }

    @Test
    void getEntityByEmail_notFound_throwsUserNotFoundException() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEntityByEmail("nobody@example.com"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("nobody@example.com");
    }

    @Test
    void getByEmail_returnsMappedDto() {
        UUID id = UUID.randomUUID();
        User user = user(id);
        UserDto dto = new UserDto();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(dto);

        assertThat(service.getByEmail("john@example.com")).isEqualTo(dto);
    }

    @Test
    void update_sameEmail_updatesUser() {
        UUID id = UUID.randomUUID();
        User user = user(id);
        UpdateUserDto dto = new UpdateUserDto()
                .setEmail("john@example.com")
                .setFirstName("Jane")
                .setLastName("Smith")
                .setPassword(null);
        UserDto userDto = new UserDto();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);

        UserDto result = service.update(id, dto);

        assertThat(result).isEqualTo(userDto);
        assertThat(user.getFirstName()).isEqualTo("Jane");
        assertThat(user.getLastName()).isEqualTo("Smith");
    }

    @Test
    void update_newEmailNotTaken_updatesEmail() {
        UUID id = UUID.randomUUID();
        User user = user(id);
        UpdateUserDto dto = new UpdateUserDto()
                .setEmail("new@example.com")
                .setFirstName("John")
                .setLastName("Doe")
                .setPassword(null);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userMapper.toDto(user)).thenReturn(new UserDto());

        service.update(id, dto);

        assertThat(user.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void update_newEmailAlreadyTaken_throwsEmailAlreadyInUseException() {
        UUID id = UUID.randomUUID();
        User user = user(id);
        UpdateUserDto dto = new UpdateUserDto()
                .setEmail("taken@example.com")
                .setFirstName("John")
                .setLastName("Doe");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.update(id, dto))
                .isInstanceOf(EmailAlreadyInUseException.class);
    }

    @Test
    void update_withNewPassword_encodesPassword() {
        UUID id = UUID.randomUUID();
        User user = user(id);
        UpdateUserDto dto = new UpdateUserDto()
                .setEmail("john@example.com")
                .setFirstName("John")
                .setLastName("Doe")
                .setPassword("NewSecure1@");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewSecure1@")).thenReturn("newHashed");
        when(userMapper.toDto(user)).thenReturn(new UserDto());

        service.update(id, dto);

        verify(passwordEncoder).encode("NewSecure1@");
        assertThat(user.getPassword()).isEqualTo("newHashed");
    }

    @Test
    void update_blankPassword_doesNotEncodePassword() {
        UUID id = UUID.randomUUID();
        User user = user(id);
        UpdateUserDto dto = new UpdateUserDto()
                .setEmail("john@example.com")
                .setFirstName("John")
                .setLastName("Doe")
                .setPassword("   ");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(new UserDto());

        service.update(id, dto);

        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void existsByEmail_returnsTrue() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);
        assertThat(service.existsByEmail("john@example.com")).isTrue();
    }

    @Test
    void existsByEmail_returnsFalse() {
        when(userRepository.existsByEmail("nobody@example.com")).thenReturn(false);
        assertThat(service.existsByEmail("nobody@example.com")).isFalse();
    }

    @Test
    void delete_callsRepository() {
        UUID id = UUID.randomUUID();
        service.delete(id);
        verify(userRepository).deleteById(id);
    }

    @Test
    void updateUserRole_setsRole() {
        UUID id = UUID.randomUUID();
        User user = user(id);
        UpdateUserRoleDto dto = new UpdateUserRoleDto()
                .setUserId(id)
                .setNewRole("ADMIN");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        service.updateUserRole(dto);

        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void updateUserRole_invalidRole_throwsRoleNotFoundException() {
        UUID id = UUID.randomUUID();
        User user = user(id);
        UpdateUserRoleDto dto = new UpdateUserRoleDto()
                .setUserId(id)
                .setNewRole("MODERATOR");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.updateUserRole(dto))
                .isInstanceOf(com.weatherviewer.exception.notfound.RoleNotFoundException.class);
    }

}
