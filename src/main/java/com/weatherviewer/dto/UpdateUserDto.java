package com.weatherviewer.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class UpdateUserDto {

    private String email;

    private String firstName;

    private String lastName;

    private String password;

}
