package com.weatherviewer.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Accessors(chain = true)
public class LocationDto {

    private UUID id;

    private String name;

    private Double latitude;

    private Double longitude;

    private LocalDateTime createdAt;

}
