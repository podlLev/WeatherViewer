package com.weatherviewer.service.impl;

import com.weatherviewer.dto.AddLocationDto;
import com.weatherviewer.dto.LocationDto;
import com.weatherviewer.exception.notfound.LocationNotFoundException;
import com.weatherviewer.mapper.LocationMapper;
import com.weatherviewer.model.Location;
import com.weatherviewer.repository.LocationRepository;
import com.weatherviewer.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    @Override
    @Transactional
    public UUID add(AddLocationDto addLocationDto) {
        if (existsByCoordinates(addLocationDto.getLatitude(), addLocationDto.getLongitude())) {
            throw new IllegalStateException("Location with these coordinates is already registered");
        }
        Location location = locationMapper.fromDto(addLocationDto);
        return locationRepository.save(location).getId();
    }

    @Override
    public List<LocationDto> getLocations() {
        List<Location> locations = locationRepository.findAll();
        return locationMapper.toDtoList(locations);
    }

    @Override
    public Location getEntityById(UUID id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException("Location not found with ID: " + id));
    }

    @Override
    public LocationDto getById(UUID id) {
        Location location = getEntityById(id);
        return locationMapper.toDto(location);
    }

    @Override
    public LocationDto getByCoordinates(Double latitude, Double longitude) {
        Location location = locationRepository.findByLatitudeAndLongitude(latitude, longitude)
                .orElseThrow(() -> new LocationNotFoundException("Location not found for the given coordinates"));
        return locationMapper.toDto(location);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!locationRepository.existsById(id)) {
            throw new LocationNotFoundException("Cannot delete. Location not found with ID: " + id);
        }
        locationRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteByName(String name) {
        if (!locationRepository.existsByName(name)) {
            throw new LocationNotFoundException("Cannot delete. No locations found named: " + name);
        }
        locationRepository.deleteByName(name);
    }

    @Override
    public boolean existsByName(String name) {
        return locationRepository.existsByName(name);
    }

    @Override
    public boolean existsByCoordinates(Double latitude, Double longitude) {
        return locationRepository.existsByLatitudeAndLongitude(latitude, longitude);
    }

    @Override
    public List<LocationDto> getLocationsSortedByDate() {
        List<Location> locations = locationRepository.findAllByOrderByCreatedAtDesc();
        return locationMapper.toDtoList(locations);
    }

    @Override
    public List<LocationDto> getLocationsSortedByNameAsc() {
        List<Location> locations = locationRepository.findAllByOrderByNameAsc();
        return locationMapper.toDtoList(locations);
    }

    @Override
    public List<LocationDto> getLocationsSortedByNameDesc() {
        List<Location> locations = locationRepository.findAllByOrderByNameDesc();
        return locationMapper.toDtoList(locations);
    }

}