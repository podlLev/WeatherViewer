package com.weatherviewer.mapper;

import com.weatherviewer.dto.CreateUserDto;
import com.weatherviewer.dto.UserDto;
import com.weatherviewer.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper between {@link User} entities and their DTOs.
 * Delegates nested {@code Location} conversion to {@link LocationMapper}.
 * The implementation is generated at compile time.
 */
@Mapper(componentModel = "spring", uses = LocationMapper.class)
public interface UserMapper {

    /**
     * Builds a new {@link User} from a registration payload. New accounts
     * are always created with {@code status = PENDING} (until the owner
     * confirms their email address via the verification link — see
     * {@link com.weatherviewer.service.VerificationService}) and
     * {@code role = USER}; the password field is copied as-is and must
     * already be hashed by the caller before persisting.
     */
    @Mapping(target = "status", expression = "java(com.weatherviewer.model.enums.UserStatus.PENDING)")
    @Mapping(target = "role", expression = "java(com.weatherviewer.model.enums.Role.USER)")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "units", ignore = true)
    @Mapping(target = "locations", ignore = true)
    User fromRecord(CreateUserDto createUserDto);

    /**
     * Converts a {@link User} to its DTO, deriving {@code username} as
     * {@code "firstName lastName"}.
     */
    @Mapping(target = "username", expression = "java(user.getFirstName() + \" \" + user.getLastName())")
    UserDto toDto(User user);

}
