package com.weatherviewer.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.UUID;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class AddLocationDto {

    private String name;

    private Double latitude;

    private Double longitude;

    private UUID userId;

    private Boolean favorite;

}
