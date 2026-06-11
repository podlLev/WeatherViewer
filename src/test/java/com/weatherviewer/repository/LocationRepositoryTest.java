package com.weatherviewer.repository;

import com.weatherviewer.model.Location;
import com.weatherviewer.model.User;
import com.weatherviewer.model.enums.Role;
import com.weatherviewer.model.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class LocationRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    LocationRepository locationRepository;

    private User user;
    private User otherUser;

    @BeforeEach
    void setUp() {
        user = entityManager.persistAndFlush(new User()
                .setEmail("john@example.com")
                .setFirstName("John")
                .setLastName("Doe")
                .setPassword("password")
                .setStatus(UserStatus.ACTIVE)
                .setRole(Role.USER));

        otherUser = entityManager.persistAndFlush(new User()
                .setEmail("jane@example.com")
                .setFirstName("Jane")
                .setLastName("Doe")
                .setPassword("password")
                .setStatus(UserStatus.ACTIVE)
                .setRole(Role.USER));
    }

    private Location location(String name, double lat, double lon, boolean favorite, User owner) {
        return new Location()
                .setName(name)
                .setLatitude(lat)
                .setLongitude(lon)
                .setFavorite(favorite)
                .setUser(owner);
    }

    @Test
    void findByUserId_returnsOnlyUserLocations() {
        entityManager.persistAndFlush(location("Kyiv", 50.45, 30.52, false, user));
        entityManager.persistAndFlush(location("Lviv", 49.84, 24.03, false, otherUser));

        List<Location> result = locationRepository.findByUserId(user.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Kyiv");
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_returnsInOrder() {
        entityManager.persistAndFlush(location("Kyiv", 50.45, 30.52, false, user));
        entityManager.persistAndFlush(location("Lviv", 49.84, 24.03, false, user));

        List<Location> result = locationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Lviv");
    }

    @Test
    void findByLatitudeAndLongitudeAndUserId_returnsCorrectLocation() {
        entityManager.persistAndFlush(location("Kyiv", 50.45, 30.52, false, user));

        Location result = locationRepository.findByLatitudeAndLongitudeAndUserId(50.45, 30.52, user.getId());

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Kyiv");
    }

    @Test
    void findByLatitudeAndLongitudeAndUserId_returnsNullWhenNotFound() {
        Location result = locationRepository.findByLatitudeAndLongitudeAndUserId(0.0, 0.0, user.getId());
        assertThat(result).isNull();
    }

    @Test
    void deleteByUserId_removesAllUserLocations() {
        entityManager.persistAndFlush(location("Kyiv", 50.45, 30.52, false, user));
        entityManager.persistAndFlush(location("Lviv", 49.84, 24.03, false, user));

        locationRepository.deleteByUserId(user.getId());

        assertThat(locationRepository.findByUserId(user.getId())).isEmpty();
    }

    @Test
    void deleteByNameAndUserId_removesCorrectLocation() {
        entityManager.persistAndFlush(location("Kyiv", 50.45, 30.52, false, user));
        entityManager.persistAndFlush(location("Lviv", 49.84, 24.03, false, user));

        locationRepository.deleteByNameAndUserId("Kyiv", user.getId());

        List<Location> result = locationRepository.findByUserId(user.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Lviv");
    }

    @Test
    void existsByNameAndUserId_returnsTrueWhenExists() {
        entityManager.persistAndFlush(location("Kyiv", 50.45, 30.52, false, user));

        assertThat(locationRepository.existsByNameAndUserId("Kyiv", user.getId())).isTrue();
    }

    @Test
    void existsByNameAndUserId_returnsFalseWhenNotExists() {
        assertThat(locationRepository.existsByNameAndUserId("Kyiv", user.getId())).isFalse();
    }

    @Test
    void existsByLatitudeAndLongitudeAndUserId_returnsTrueWhenExists() {
        entityManager.persistAndFlush(location("Kyiv", 50.45, 30.52, false, user));

        assertThat(locationRepository.existsByLatitudeAndLongitudeAndUserId(50.45, 30.52, user.getId())).isTrue();
    }

    @Test
    void existsByLatitudeAndLongitudeAndUserId_returnsFalseWhenNotExists() {
        assertThat(locationRepository.existsByLatitudeAndLongitudeAndUserId(0.0, 0.0, user.getId())).isFalse();
    }

    @Test
    void findByUserIdAndFavoriteTrueOrderByCreatedAtDesc_returnsOnlyFavorites() {
        entityManager.persistAndFlush(location("Kyiv", 50.45, 30.52, true, user));
        entityManager.persistAndFlush(location("Lviv", 49.84, 24.03, false, user));

        List<Location> result = locationRepository.findByUserIdAndFavoriteTrueOrderByCreatedAtDesc(user.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Kyiv");
    }

    @Test
    void findByUserIdOrderByFavoriteDescCreatedAtDesc_favoritesComeFirst() {
        entityManager.persistAndFlush(location("Kyiv", 50.45, 30.52, false, user));
        entityManager.persistAndFlush(location("Lviv", 49.84, 24.03, true, user));

        List<Location> result = locationRepository.findByUserIdOrderByFavoriteDescCreatedAtDesc(user.getId());

        assertThat(result.get(0).getName()).isEqualTo("Lviv");
    }

    @Test
    void findByUserIdOrderByNameAsc_returnsAlphabetically() {
        entityManager.persistAndFlush(location("Odesa", 46.48, 30.73, false, user));
        entityManager.persistAndFlush(location("Kyiv", 50.45, 30.52, false, user));
        entityManager.persistAndFlush(location("Lviv", 49.84, 24.03, false, user));

        List<Location> result = locationRepository.findByUserIdOrderByNameAsc(user.getId());

        assertThat(result).extracting(Location::getName)
                .containsExactly("Kyiv", "Lviv", "Odesa");
    }

    @Test
    void findByUserIdOrderByNameDesc_returnsReverseAlphabetically() {
        entityManager.persistAndFlush(location("Odesa", 46.48, 30.73, false, user));
        entityManager.persistAndFlush(location("Kyiv", 50.45, 30.52, false, user));
        entityManager.persistAndFlush(location("Lviv", 49.84, 24.03, false, user));

        List<Location> result = locationRepository.findByUserIdOrderByNameDesc(user.getId());

        assertThat(result).extracting(Location::getName)
                .containsExactly("Odesa", "Lviv", "Kyiv");
    }

}
