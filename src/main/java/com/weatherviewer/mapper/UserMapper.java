package com.weatherviewer.mapper;

import com.weatherviewer.dto.CreateUserDto;
import com.weatherviewer.dto.UserDto;
import com.weatherviewer.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = LocationMapper.class)
public interface UserMapper {

    @Mapping(target = "status", expression = "java(com.weatherviewer.model.enums.UserStatus.ACTIVE)")
    @Mapping(target = "role", expression = "java(com.weatherviewer.model.enums.Role.USER)")
    User fromRecord(CreateUserDto createUserDto);

    @Mapping(target = "username", expression = "java(user.getFirstName() + \" \" + user.getLastName())")
    UserDto toDto(User user);

}
