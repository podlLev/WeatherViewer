package com.weatherviewer.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {

    PENDING("User has not been activated yet"),
    ACTIVE("User is activated and can use the system"),
    BLOCKED("User has been blocked by the system or an administrator"),
    DELETED("User account has been deleted (soft delete)"),
    EXPIRED("User account is expired and needs reactivation");

    private final String description;

}
