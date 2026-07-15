package com.weatherviewer.mapper;

import com.weatherviewer.dto.AddLocationDto;
import com.weatherviewer.dto.LocationDto;
import com.weatherviewer.model.Location;
import com.weatherviewer.repository.UserRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


/**
 * MapStruct mapper between {@link Location} entities and their DTO
 * representations ({@link AddLocationDto}, {@link LocationDto}).
 * <p>
 * Declared as an abstract class rather than an interface so it can hold an
 * injected {@link UserRepository}, needed to resolve the owning
 * {@link com.weatherviewer.model.User} reference when creating a location
 * from an {@link AddLocationDto}. The implementation is generated at
 * compile time by the MapStruct annotation processor.
 */
@Mapper(componentModel = "spring")
public abstract class LocationMapper {

    @Autowired
    protected UserRepository userRepository;

    /**
     * Builds a new {@link Location} from a creation payload. The owner is
     * resolved to a lazy JPA reference (no extra query) from
     * {@code addLocationDto.getUserId()}, and {@code favorite} defaults to
     * {@code false} when not supplied.
     */
    @Mapping(target = "user", expression = "java(userRepository.getReferenceById(addLocationDto.getUserId()))")
    @Mapping(target = "favorite", expression = "java(addLocationDto.getFavorite() != null ? addLocationDto.getFavorite() : false)")
    public abstract Location fromDto(AddLocationDto addLocationDto);

    /** Builds a {@link Location} from its full DTO representation (e.g. for updates). */
    public abstract Location fromDto(LocationDto locationDto);

    /**
     * Converts a {@link Location} to its DTO, flattening the owning user
     * down to just {@code userId}.
     */
    @Mapping(target = "userId", expression = "java(location.getUser() != null ? location.getUser().getId() : null)")
    public abstract LocationDto toDto(Location location);

    /**
     * Maps a list of locations to DTOs.
     *
     * @param locations the locations to convert; may be {@code null}
     * @return the converted list, or {@code null} if {@code locations} was {@code null}
     */
    public List<LocationDto> toDtoList(List<Location> locations) {
        if(locations == null) return null;
        return locations.stream()
                .map(this::toDto)
                .toList();
    }

}
