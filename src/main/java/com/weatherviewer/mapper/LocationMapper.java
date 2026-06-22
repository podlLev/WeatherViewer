package com.weatherviewer.mapper;

import com.weatherviewer.dto.AddLocationDto;
import com.weatherviewer.dto.LocationDto;
import com.weatherviewer.model.Location;
import com.weatherviewer.repository.UserRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


@Mapper(componentModel = "spring")
public abstract class LocationMapper {

    @Autowired
    protected UserRepository userRepository;

    @Mapping(target = "user", expression = "java(userRepository.getReferenceById(addLocationDto.getUserId()))")
    @Mapping(target = "favorite", expression = "java(addLocationDto.getFavorite() != null ? addLocationDto.getFavorite() : false)")
    public abstract Location fromDto(AddLocationDto addLocationDto);

    public abstract Location fromDto(LocationDto locationDto);

    @Mapping(target = "userId", expression = "java(location.getUser() != null ? location.getUser().getId() : null)")
    public abstract LocationDto toDto(Location location);

    public List<LocationDto> toDtoList(List<Location> locations) {
        if(locations == null) return null;
        return locations.stream()
                .map(this::toDto)
                .toList();
    }

}
