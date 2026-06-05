package com.weatherviewer.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class GeoLocationDto {

    private String name;

    private Double latitude;

    private Double longitude;

    private String country;

    private String state;

}
