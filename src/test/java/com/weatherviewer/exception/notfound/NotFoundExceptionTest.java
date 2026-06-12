package com.weatherviewer.exception.notfound;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotFoundExceptionTest {

    static Stream<NotFoundException> subclasses() {
        return Stream.of(
                new LocationNotFoundException("Location not found"),
                new UserNotFoundException("User not found"),
                new RoleNotFoundException("Role not found")
        );
    }

    @Test
    void constructor_setsMessage() {
        NotFoundException ex = new NotFoundException("Not found");
        assertThat(ex.getMessage()).isEqualTo("Not found");
    }

    @Test
    void isInstanceOf_RuntimeException() {
        NotFoundException ex = new NotFoundException("Not found");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @ParameterizedTest
    @MethodSource("subclasses")
    void subclass_isInstanceOf_NotFoundException(NotFoundException ex) {
        assertThat(ex).isInstanceOf(NotFoundException.class);
    }

    @ParameterizedTest
    @MethodSource("subclasses")
    void subclass_isInstanceOf_RuntimeException(NotFoundException ex) {
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @ParameterizedTest
    @MethodSource("subclasses")
    void subclass_thrown_canBeCaughtAsNotFoundException(NotFoundException ex) {
        assertThatThrownBy(() -> { throw ex; })
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ex.getMessage());
    }

}
