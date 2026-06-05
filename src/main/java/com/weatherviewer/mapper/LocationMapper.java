package com.weatherviewer.mapper;

import com.weatherviewer.dto.AddLocationDto;
import com.weatherviewer.dto.LocationDto;
import com.weatherviewer.model.Location;
import org.mapstruct.Mapper;

import java.util.List;


@Mapper(componentModel = "spring")
public abstract class LocationMapper {

    public abstract Location fromDto(AddLocationDto addLocationDto);

    public abstract Location fromDto(LocationDto locationDto);

    public abstract LocationDto toDto(Location location);

    public List<LocationDto> toDtoList(List<Location> locations) {
        if(locations == null) return null;
        return locations.stream()
                .map(this::toDto)
                .toList();
    }

}
