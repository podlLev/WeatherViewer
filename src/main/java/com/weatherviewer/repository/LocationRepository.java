package com.weatherviewer.repository;

import com.weatherviewer.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {

    List<Location> findByUserId(UUID userId);
    List<Location> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Location findByLatitudeAndLongitudeAndUserId(Double latitude, Double longitude, UUID userId);

    void deleteByUserId(UUID userId);
    void deleteByNameAndUserId(String name,UUID userId);

    boolean existsByNameAndUserId(String name, UUID userId);
    boolean existsByLatitudeAndLongitudeAndUserId(Double latitude, Double longitude, UUID userId);

    List<Location> findByUserIdAndFavoriteTrueOrderByCreatedAtDesc(UUID userId);
    List<Location> findByUserIdOrderByFavoriteDescCreatedAtDesc(UUID userId);
    List<Location> findByUserIdOrderByNameAsc(UUID userId);
    List<Location> findByUserIdOrderByNameDesc(UUID userId);

}
