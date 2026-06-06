package com.weatherviewer.service;

import com.weatherviewer.dto.AddLocationDto;
import com.weatherviewer.dto.LocationDto;
import com.weatherviewer.model.Location;

import java.util.List;
import java.util.UUID;

public interface LocationService {

    UUID add(AddLocationDto addLocationDto);

    List<LocationDto> getLocations();

    Location getEntityById(UUID id);

    LocationDto getById(UUID id);

    List<LocationDto> getByUserId(UUID userId);

    LocationDto getByCoordinatesAndUserId(Double latitude, Double longitude, UUID userId);

    void delete(UUID id);

    void deleteByUserId(UUID userId);

    void deleteByNameAndUserId(String name, UUID userId);

    boolean existsByNameAndUserId(String name, UUID userId);

    boolean existsByCoordinatesAndUserId(Double latitude, Double longitude, UUID userId);

    void addToFavorite(UUID locationId, UUID userId);

    void removeFromFavorite(UUID locationId, UUID userId);

    List<LocationDto> getFavoritesByUserId(UUID userId);

    List<LocationDto> getByUserIdSortedByFavorite(UUID userId);

    List<LocationDto> getByUserIdSortedByDate(UUID userId);

    List<LocationDto> getByUserIdSortedByNameAsc(UUID userId);

    List<LocationDto> getByUserIdSortedByNameDesc(UUID userId);

}
