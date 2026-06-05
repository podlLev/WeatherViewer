package com.weatherviewer.repository;

import com.weatherviewer.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {

    Optional<Location> findByLatitudeAndLongitude(Double latitude, Double longitude);

    boolean existsByName(String name);

    void deleteByName(String name);

    boolean existsByLatitudeAndLongitude(Double latitude, Double longitude);

    List<Location> findAllByOrderByCreatedAtDesc();

    List<Location> findAllByOrderByNameAsc();

    List<Location> findAllByOrderByNameDesc();

}
