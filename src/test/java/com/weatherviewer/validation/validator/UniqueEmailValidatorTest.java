package com.weatherviewer.validation.validator;

import com.weatherviewer.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UniqueEmailValidatorTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UniqueEmailValidator validator;

    @Test
    void null_returnsTrue() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    void blank_returnsTrue() {
        assertThat(validator.isValid("", null)).isTrue();
    }

    @Test
    void whitespaceOnly_returnsTrue() {
        assertThat(validator.isValid("   ", null)).isTrue();
    }

    @Test
    void emailNotExists_returnsTrue() {
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        assertThat(validator.isValid("new@example.com", null)).isTrue();
    }

    @Test
    void emailAlreadyExists_returnsFalse() {
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);
        assertThat(validator.isValid("taken@example.com", null)).isFalse();
    }

}
