package com.weatherviewer.dto;

import com.weatherviewer.model.enums.Role;
import com.weatherviewer.model.enums.UserStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class UserDto {

    private UUID id;

    private String username;

    private String email;

    private String password;

    private UserStatus status;

    private Role role;

    private List<LocationDto> locations;

    private LocalDateTime createdAt;

}
