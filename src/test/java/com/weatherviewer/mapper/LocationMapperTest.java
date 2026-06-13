package com.weatherviewer.mapper;

import com.weatherviewer.dto.AddLocationDto;
import com.weatherviewer.dto.LocationDto;
import com.weatherviewer.model.Location;
import com.weatherviewer.model.User;
import com.weatherviewer.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationMapperTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LocationMapperImpl mapper;

    private User user() {
        return new User()
                .setEmail("john@example.com")
                .setFirstName("John")
                .setLastName("Doe");
    }

    @Test
    void fromDto_nullAddLocationDto_returnsNull() {
        assertThat(mapper.fromDto((AddLocationDto) null)).isNull();
    }

    @Test
    void fromDto_nullLocationDto_returnsNull() {
        assertThat(mapper.fromDto((LocationDto) null)).isNull();
    }

    @Test
    void toDto_nullLocation_returnsNull() {
        assertThat(mapper.toDto( null)).isNull();
    }

    @Test
    void fromDto_addLocationDto_mapsAllFields() {
        UUID userId = UUID.randomUUID();
        User user = user();
        AddLocationDto dto = new AddLocationDto()
                .setName("Kyiv")
                .setLatitude(50.45)
                .setLongitude(30.52)
                .setUserId(userId)
                .setFavorite(true);

        when(userRepository.getReferenceById(userId)).thenReturn(user);

        Location result = mapper.fromDto(dto);

        assertThat(result.getName()).isEqualTo("Kyiv");
        assertThat(result.getLatitude()).isEqualTo(50.45);
        assertThat(result.getLongitude()).isEqualTo(30.52);
        assertThat(result.isFavorite()).isTrue();
        assertThat(result.getUser()).isEqualTo(user);
    }

    @Test
    void fromDto_addLocationDto_nullFavorite_defaultsFalse() {
        UUID userId = UUID.randomUUID();
        AddLocationDto dto = new AddLocationDto()
                .setName("Kyiv")
                .setLatitude(50.45)
                .setLongitude(30.52)
                .setUserId(userId)
                .setFavorite(null);

        when(userRepository.getReferenceById(userId)).thenReturn(user());

        Location result = mapper.fromDto(dto);

        assertThat(result.isFavorite()).isFalse();
    }

    @Test
    void fromDto_locationDto_mapsAllFields() {
        LocationDto dto = new LocationDto()
                .setId(UUID.randomUUID())
                .setName("Kyiv")
                .setLatitude(50.45)
                .setLongitude(30.52)
                .setFavorite(false)
                .setCreatedAt(LocalDateTime.now());

        Location result = mapper.fromDto(dto);

        assertThat(result.getName()).isEqualTo("Kyiv");
        assertThat(result.getLatitude()).isEqualTo(50.45);
        assertThat(result.getLongitude()).isEqualTo(30.52);
        assertThat(result.isFavorite()).isFalse();
    }

    @Test
    void fromDto_locationDto_nullFavorite_doesNotSetFavorite() {
        LocationDto dto = new LocationDto()
                .setId(UUID.randomUUID())
                .setName("Kyiv")
                .setLatitude(50.45)
                .setLongitude(30.52)
                .setFavorite(null)
                .setCreatedAt(LocalDateTime.now());

        Location result = mapper.fromDto(dto);

        assertThat(result.isFavorite()).isFalse();
    }

    @Test
    void toDto_mapsAllFields() {
        UUID userId = UUID.randomUUID();
        User user = (User) user().setId(userId);
        Location location = new Location()
                .setName("Kyiv")
                .setLatitude(50.45)
                .setLongitude(30.52)
                .setFavorite(true)
                .setUser(user);

        LocationDto result = mapper.toDto(location);

        assertThat(result.getName()).isEqualTo("Kyiv");
        assertThat(result.getLatitude()).isEqualTo(50.45);
        assertThat(result.getLongitude()).isEqualTo(30.52);
        assertThat(result.getFavorite()).isTrue();
        assertThat(result.getUserId()).isEqualTo(userId);
    }

    @Test
    void toDto_nullUser_userIdIsNull() {
        Location location = new Location()
                .setName("Kyiv")
                .setLatitude(50.45)
                .setLongitude(30.52)
                .setUser(null);

        LocationDto result = mapper.toDto(location);

        assertThat(result.getUserId()).isNull();
    }

    @Test
    void toDtoList_mapsAllLocations() {
        UUID userId = UUID.randomUUID();
        User user = (User) user().setId(userId);
        List<Location> locations = List.of(
                new Location().setName("Kyiv").setLatitude(50.45).setLongitude(30.52).setUser(user),
                new Location().setName("Lviv").setLatitude(49.84).setLongitude(24.03).setUser(user)
        );

        List<LocationDto> result = mapper.toDtoList(locations);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(LocationDto::getName)
                .containsExactly("Kyiv", "Lviv");
    }

    @Test
    void toDtoList_null_returnsNull() {
        assertThat(mapper.toDtoList(null)).isNull();
    }

    @Test
    void toDtoList_empty_returnsEmpty() {
        assertThat(mapper.toDtoList(List.of())).isEmpty();
    }

}