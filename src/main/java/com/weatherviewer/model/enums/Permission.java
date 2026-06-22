package com.weatherviewer.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Permission {

    USERS_READ("users:read"),
    USERS_WRITE("users:write");

    private final String permission;

}
