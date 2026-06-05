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

    LocationDto getByCoordinates(Double latitude, Double longitude);

    void delete(UUID id);

    void deleteByName(String name);

    boolean existsByName(String name);

    boolean existsByCoordinates(Double latitude, Double longitude);

    List<LocationDto> getLocationsSortedByDate();

    List<LocationDto> getLocationsSortedByNameAsc();

    List<LocationDto> getLocationsSortedByNameDesc();

}
