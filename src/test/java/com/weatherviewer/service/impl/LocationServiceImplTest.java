package com.weatherviewer.service.impl;

import com.weatherviewer.dto.AddLocationDto;
import com.weatherviewer.dto.LocationDto;
import com.weatherviewer.exception.notfound.LocationNotFoundException;
import com.weatherviewer.mapper.LocationMapper;
import com.weatherviewer.model.Location;
import com.weatherviewer.model.User;
import com.weatherviewer.repository.LocationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationServiceImplTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private LocationMapper locationMapper;

    @InjectMocks
    private LocationServiceImpl service;

    private Location location(UUID id, UUID userId) {
        User user = new User();
        user.setId(userId);
        Location location = new Location();
        location.setId(id);
        location.setUser(user);
        return location;
    }

    @Test
    void add_savesAndReturnsId() {
        UUID id = UUID.randomUUID();
        AddLocationDto dto = new AddLocationDto();
        Location location = new Location();
        location.setId(id);

        when(locationMapper.fromDto(dto)).thenReturn(location);
        when(locationRepository.save(location)).thenReturn(location);

        UUID result = service.add(dto);

        assertThat(result).isEqualTo(id);
    }

    @Test
    void getLocations_returnsAllMapped() {
        List<Location> locations = List.of(new Location());
        List<LocationDto> dtos = List.of(new LocationDto());

        when(locationRepository.findAll()).thenReturn(locations);
        when(locationMapper.toDtoList(locations)).thenReturn(dtos);

        assertThat(service.getLocations()).isEqualTo(dtos);
    }

    @Test
    void getEntityById_returnsLocation() {
        UUID id = UUID.randomUUID();
        Location location = new Location();
        when(locationRepository.findById(id)).thenReturn(Optional.of(location));

        assertThat(service.getEntityById(id)).isEqualTo(location);
    }

    @Test
    void getEntityById_notFound_throwsLocationNotFoundException() {
        UUID id = UUID.randomUUID();
        when(locationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEntityById(id))
                .isInstanceOf(LocationNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void getById_returnsMappedDto() {
        UUID id = UUID.randomUUID();
        Location location = new Location();
        LocationDto dto = new LocationDto();

        when(locationRepository.findById(id)).thenReturn(Optional.of(location));
        when(locationMapper.toDto(location)).thenReturn(dto);

        assertThat(service.getById(id)).isEqualTo(dto);
    }

    @Test
    void getByUserId_returnsMappedList() {
        UUID userId = UUID.randomUUID();
        List<Location> locations = List.of(new Location());
        List<LocationDto> dtos = List.of(new LocationDto());

        when(locationRepository.findByUserId(userId)).thenReturn(locations);
        when(locationMapper.toDtoList(locations)).thenReturn(dtos);

        assertThat(service.getByUserId(userId)).isEqualTo(dtos);
    }

    @Test
    void getByCoordinatesAndUserId_returnsMappedDto() {
        UUID userId = UUID.randomUUID();
        Location location = new Location();
        LocationDto dto = new LocationDto();

        when(locationRepository.findByLatitudeAndLongitudeAndUserId(50.45, 30.52, userId)).thenReturn(location);
        when(locationMapper.toDto(location)).thenReturn(dto);

        assertThat(service.getByCoordinatesAndUserId(50.45, 30.52, userId)).isEqualTo(dto);
    }

    @Test
    void getByCoordinatesAndUserId_notFound_throwsLocationNotFoundException() {
        UUID userId = UUID.randomUUID();
        when(locationRepository.findByLatitudeAndLongitudeAndUserId(50.45, 30.52, userId)).thenReturn(null);

        assertThatThrownBy(() -> service.getByCoordinatesAndUserId(50.45, 30.52, userId))
                .isInstanceOf(LocationNotFoundException.class);
    }

    @Test
    void deleteByIdAndUserId_ownLocation_deletesSuccessfully() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Location location = new Location();
        location.setId(id);

        User user = new User();
        user.setId(userId);
        user.setLocations(new ArrayList<>(List.of(location)));
        location.setUser(user);

        when(locationRepository.findById(id)).thenReturn(Optional.of(location));

        service.deleteByIdAndUserId(id, userId);

        verify(locationRepository).delete(location);
        assertThat(user.getLocations()).doesNotContain(location);
    }

    @Test
    void deleteByIdAndUserId_wrongUser_throwsAccessDeniedException() {
        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID wrongUserId = UUID.randomUUID();
        Location location = location(id, ownerId);

        when(locationRepository.findById(id)).thenReturn(Optional.of(location));

        assertThatThrownBy(() -> service.deleteByIdAndUserId(id, wrongUserId))
                .isInstanceOf(AccessDeniedException.class);

        verify(locationRepository, never()).delete(any());
    }

    @Test
    void deleteByIdAndUserId_notFound_throwsLocationNotFoundException() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(locationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteByIdAndUserId(id, userId))
                .isInstanceOf(LocationNotFoundException.class);
    }

    @Test
    void deleteByUserId_callsRepository() {
        UUID userId = UUID.randomUUID();
        service.deleteByUserId(userId);
        verify(locationRepository).deleteByUserId(userId);
    }

    @Test
    void deleteByNameAndUserId_callsRepository() {
        UUID userId = UUID.randomUUID();
        service.deleteByNameAndUserId("Kyiv", userId);
        verify(locationRepository).deleteByNameAndUserId("Kyiv", userId);
    }

    @Test
    void existsByNameAndUserId_returnsTrue() {
        UUID userId = UUID.randomUUID();
        when(locationRepository.existsByNameAndUserId("Kyiv", userId)).thenReturn(true);
        assertThat(service.existsByNameAndUserId("Kyiv", userId)).isTrue();
    }

    @Test
    void existsByNameAndUserId_returnsFalse() {
        UUID userId = UUID.randomUUID();
        when(locationRepository.existsByNameAndUserId("Kyiv", userId)).thenReturn(false);
        assertThat(service.existsByNameAndUserId("Kyiv", userId)).isFalse();
    }

    @Test
    void existsByCoordinatesAndUserId_returnsTrue() {
        UUID userId = UUID.randomUUID();
        when(locationRepository.existsByLatitudeAndLongitudeAndUserId(50.45, 30.52, userId)).thenReturn(true);
        assertThat(service.existsByCoordinatesAndUserId(50.45, 30.52, userId)).isTrue();
    }

    @Test
    void existsByCoordinatesAndUserId_returnsFalse() {
        UUID userId = UUID.randomUUID();
        when(locationRepository.existsByLatitudeAndLongitudeAndUserId(50.45, 30.52, userId)).thenReturn(false);
        assertThat(service.existsByCoordinatesAndUserId(50.45, 30.52, userId)).isFalse();
    }

    @Test
    void addToFavorite_setsFavoriteTrue() {
        UUID locationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Location location = location(locationId, userId);

        when(locationRepository.findById(locationId)).thenReturn(Optional.of(location));

        service.addToFavorite(locationId, userId);

        assertThat(location.isFavorite()).isTrue();
        verify(locationRepository).save(location);
    }

    @Test
    void addToFavorite_wrongUser_throwsAccessDeniedException() {
        UUID locationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        Location location = location(locationId, otherUserId);

        when(locationRepository.findById(locationId)).thenReturn(Optional.of(location));

        assertThatThrownBy(() -> service.addToFavorite(locationId, userId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void removeFromFavorite_setsFavoriteFalse() {
        UUID locationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Location location = location(locationId, userId);
        location.setFavorite(true);

        when(locationRepository.findById(locationId)).thenReturn(Optional.of(location));

        service.removeFromFavorite(locationId, userId);

        assertThat(location.isFavorite()).isFalse();
        verify(locationRepository).save(location);
    }

    @Test
    void getFavoritesByUserId_returnsMappedList() {
        UUID userId = UUID.randomUUID();
        List<Location> locations = List.of(new Location());
        List<LocationDto> dtos = List.of(new LocationDto());

        when(locationRepository.findByUserIdAndFavoriteTrueOrderByCreatedAtDesc(userId)).thenReturn(locations);
        when(locationMapper.toDtoList(locations)).thenReturn(dtos);

        assertThat(service.getFavoritesByUserId(userId)).isEqualTo(dtos);
    }

    @Test
    void getByUserIdSortedByFavorite_returnsMappedList() {
        UUID userId = UUID.randomUUID();
        List<Location> locations = List.of(new Location());
        List<LocationDto> dtos = List.of(new LocationDto());

        when(locationRepository.findByUserIdOrderByFavoriteDescCreatedAtDesc(userId)).thenReturn(locations);
        when(locationMapper.toDtoList(locations)).thenReturn(dtos);

        assertThat(service.getByUserIdSortedByFavorite(userId)).isEqualTo(dtos);
    }

    @Test
    void getByUserIdSortedByDate_returnsMappedList() {
        UUID userId = UUID.randomUUID();
        List<Location> locations = List.of(new Location());
        List<LocationDto> dtos = List.of(new LocationDto());

        when(locationRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(locations);
        when(locationMapper.toDtoList(locations)).thenReturn(dtos);

        assertThat(service.getByUserIdSortedByDate(userId)).isEqualTo(dtos);
    }

    @Test
    void getByUserIdSortedByNameAsc_returnsMappedList() {
        UUID userId = UUID.randomUUID();
        List<Location> locations = List.of(new Location());
        List<LocationDto> dtos = List.of(new LocationDto());

        when(locationRepository.findByUserIdOrderByNameAsc(userId)).thenReturn(locations);
        when(locationMapper.toDtoList(locations)).thenReturn(dtos);

        assertThat(service.getByUserIdSortedByNameAsc(userId)).isEqualTo(dtos);
    }

    @Test
    void getByUserIdSortedByNameDesc_returnsMappedList() {
        UUID userId = UUID.randomUUID();
        List<Location> locations = List.of(new Location());
        List<LocationDto> dtos = List.of(new LocationDto());

        when(locationRepository.findByUserIdOrderByNameDesc(userId)).thenReturn(locations);
        when(locationMapper.toDtoList(locations)).thenReturn(dtos);

        assertThat(service.getByUserIdSortedByNameDesc(userId)).isEqualTo(dtos);
    }

}
