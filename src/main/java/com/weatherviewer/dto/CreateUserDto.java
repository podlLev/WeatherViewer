package com.weatherviewer.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class CreateUserDto {

    private String firstName;

    private String lastName;

    private String email;

    private String password;

    private String repeatPassword;

}
