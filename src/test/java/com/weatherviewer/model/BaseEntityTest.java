package com.weatherviewer.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class BaseEntityTest {

    static class TestEntity extends BaseEntity {}

    @Test
    void id_isNullBeforePersist() {
        TestEntity entity = new TestEntity();
        assertThat(entity.getId()).isNull();
    }

    @Test
    void createdAt_isNullBeforePersist() {
        TestEntity entity = new TestEntity();
        assertThat(entity.getCreatedAt()).isNull();
    }

    @Test
    void onCreate_setsCreatedAt() {
        TestEntity entity = new TestEntity();
        entity.onCreate();
        assertThat(entity.getCreatedAt()).isNotNull();
    }

    @Test
    void onCreate_setsCreatedAtToNow() {
        TestEntity entity = new TestEntity();
        entity.onCreate();
        assertThat(entity.getCreatedAt())
                .isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
    }

    @Test
    void chainedSetters_returnSameInstance() {
        TestEntity entity = new TestEntity();
        assertThat(entity.setId(UUID.randomUUID())).isSameAs(entity);
    }

}
