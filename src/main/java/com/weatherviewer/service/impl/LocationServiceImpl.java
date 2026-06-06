package com.weatherviewer.service.impl;

import com.weatherviewer.dto.AddLocationDto;
import com.weatherviewer.dto.LocationDto;
import com.weatherviewer.exception.notfound.LocationNotFoundException;
import com.weatherviewer.mapper.LocationMapper;
import com.weatherviewer.model.Location;
import com.weatherviewer.model.User;
import com.weatherviewer.repository.LocationRepository;
import com.weatherviewer.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    @Override
    @Transactional
    public UUID add(AddLocationDto addLocationDto) {
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
                .orElseThrow(() -> new LocationNotFoundException("Location not found"));
    }

    @Override
    public LocationDto getById(UUID id) {
        Location location = getEntityById(id);
        return locationMapper.toDto(location);
    }

    @Override
    public List<LocationDto> getByUserId(UUID userId) {
        List<Location> locations = locationRepository.findByUserId(userId);
        return locationMapper.toDtoList(locations);
    }

    @Override
    public LocationDto getByCoordinatesAndUserId(Double latitude, Double longitude, UUID userId) {
        Location location = locationRepository.findByLatitudeAndLongitudeAndUserId(latitude, longitude, userId);
        return locationMapper.toDto(location);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Location location = getEntityById(id);
        User user = location.getUser();
        if (user != null && user.getLocations() != null) {
            user.getLocations().remove(location);
        }
        locationRepository.delete(location);
    }

    @Override
    @Transactional
    public void deleteByUserId(UUID userId) {
        List<Location> locations = locationRepository.findByUserId(userId);
        for (Location loc : locations) {
            delete(loc.getId());
        }
    }

    @Override
    @Transactional
    public void deleteByNameAndUserId(String name, UUID userId) {
        List<Location> locations = locationRepository.findByUserId(userId).stream()
                .filter(loc -> name.equals(loc.getName()))
                .toList();
        for (Location loc : locations) {
            delete(loc.getId());
        }
    }

    @Override
    public boolean existsByNameAndUserId(String name, UUID userId) {
        return locationRepository.existsByNameAndUserId(name, userId);
    }

    @Override
    public boolean existsByCoordinatesAndUserId(Double latitude, Double longitude, UUID userId) {
        return locationRepository.existsByLatitudeAndLongitudeAndUserId(latitude, longitude, userId);
    }

    @Override
    @Transactional
    public void addToFavorite(UUID locationId, UUID userId) {
        setFavorite(locationId, userId, true);
    }

    private void setFavorite(UUID locationId, UUID userId, boolean favorite) {
        Location location = getEntityById(locationId);
        if (!Objects.equals(location.getUser().getId(), userId)) {
            throw new AccessDeniedException("You are not authorized to modify this location");
        }
        location.setFavorite(favorite);
        locationRepository.save(location);
    }

    @Override
    @Transactional
    public void removeFromFavorite(UUID locationId, UUID userId) {
        setFavorite(locationId, userId, false);
    }

    @Override
    public List<LocationDto> getFavoritesByUserId(UUID userId) {
        List<Location> locations = locationRepository.findByUserIdAndFavoriteTrueOrderByCreatedAtDesc(userId);
        return locationMapper.toDtoList(locations);
    }

    @Override
    public List<LocationDto> getByUserIdSortedByFavorite(UUID userId) {
        List<Location> locations = locationRepository.findByUserIdOrderByFavoriteDescCreatedAtDesc(userId);
        return locationMapper.toDtoList(locations);
    }

    @Override
    public List<LocationDto> getByUserIdSortedByDate(UUID userId) {
        List<Location> locations = locationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return locationMapper.toDtoList(locations);
    }

    @Override
    public List<LocationDto> getByUserIdSortedByNameAsc(UUID userId) {
        List<Location> locations = locationRepository.findByUserIdOrderByNameAsc(userId);
        return locationMapper.toDtoList(locations);
    }

    @Override
    public List<LocationDto> getByUserIdSortedByNameDesc(UUID userId) {
        List<Location> locations = locationRepository.findByUserIdOrderByNameDesc(userId);
        return locationMapper.toDtoList(locations);
    }

}
