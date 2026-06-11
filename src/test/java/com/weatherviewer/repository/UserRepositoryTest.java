package com.weatherviewer.repository;

import com.weatherviewer.model.User;
import com.weatherviewer.model.enums.Role;
import com.weatherviewer.model.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = entityManager.persistAndFlush(new User()
                .setEmail("john@example.com")
                .setFirstName("John")
                .setLastName("Doe")
                .setPassword("password")
                .setStatus(UserStatus.ACTIVE)
                .setRole(Role.USER));
    }

    @Test
    void findByEmail_returnsUserWhenExists() {
        Optional<User> result = userRepository.findByEmail("john@example.com");
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(user);
    }

    @Test
    void findByEmail_returnsEmptyWhenNotExists() {
        Optional<User> result = userRepository.findByEmail("nobody@example.com");
        assertThat(result).isEmpty();
    }

    @Test
    void existsByEmail_returnsTrueWhenExists() {
        assertThat(userRepository.existsByEmail("john@example.com")).isTrue();
    }

    @Test
    void existsByEmail_returnsFalseWhenNotExists() {
        assertThat(userRepository.existsByEmail("nobody@example.com")).isFalse();
    }

    @Test
    void existsByEmail_casesSensitive_returnsFalse() {
        assertThat(userRepository.existsByEmail("JOHN@EXAMPLE.COM")).isFalse();
    }

}
