package com.weatherviewer.mapper;

import com.weatherviewer.dto.CreateUserDto;
import com.weatherviewer.dto.UserDto;
import com.weatherviewer.model.User;
import com.weatherviewer.model.enums.Role;
import com.weatherviewer.model.enums.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    @Mock
    @SuppressWarnings("unused")
    private LocationMapper locationMapper;

    @InjectMocks
    private UserMapperImpl mapper;

    @Test
    void fromRecord_null_returnsNull() {
        assertThat(mapper.fromRecord(null)).isNull();
    }

    @Test
    void toDto_null_returnsNull() {
        assertThat(mapper.toDto( null)).isNull();
    }

    @Test
    void fromRecord_mapsAllFields() {
        CreateUserDto dto = new CreateUserDto()
                .setFirstName("John")
                .setLastName("Doe")
                .setEmail("john@example.com")
                .setPassword("Secure1@")
                .setRepeatPassword("Secure1@");

        User result = mapper.fromRecord(dto);

        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getPassword()).isEqualTo("Secure1@");
    }

    @Test
    void fromRecord_setsDefaultStatusActive() {
        CreateUserDto dto = new CreateUserDto()
                .setFirstName("John")
                .setLastName("Doe")
                .setEmail("john@example.com")
                .setPassword("Secure1@");

        User result = mapper.fromRecord(dto);

        assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void fromRecord_setsDefaultRoleUser() {
        CreateUserDto dto = new CreateUserDto()
                .setFirstName("John")
                .setLastName("Doe")
                .setEmail("john@example.com")
                .setPassword("Secure1@");

        User result = mapper.fromRecord(dto);

        assertThat(result.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void toDto_mapsAllFields() {
        User user = new User()
                .setFirstName("John")
                .setLastName("Doe")
                .setEmail("john@example.com")
                .setPassword("Secure1@")
                .setStatus(UserStatus.ACTIVE)
                .setRole(Role.USER)
                .setLocations(List.of());

        UserDto result = mapper.toDto(user);

        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(result.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void toDto_username_isFullName() {
        User user = new User()
                .setFirstName("John")
                .setLastName("Doe")
                .setEmail("john@example.com")
                .setPassword("Secure1@")
                .setStatus(UserStatus.ACTIVE)
                .setRole(Role.USER)
                .setLocations(List.of());

        UserDto result = mapper.toDto(user);

        assertThat(result.getUsername()).isEqualTo("John Doe");
    }

    @Test
    void toDto_nullLastName_usernameIsFirstNameOnly() {
        User user = new User()
                .setFirstName("John")
                .setLastName(null)
                .setEmail("john@example.com")
                .setPassword("Secure1@")
                .setStatus(UserStatus.ACTIVE)
                .setRole(Role.USER)
                .setLocations(List.of());

        UserDto result = mapper.toDto(user);

        assertThat(result.getUsername()).isEqualTo("John null");
    }

}
