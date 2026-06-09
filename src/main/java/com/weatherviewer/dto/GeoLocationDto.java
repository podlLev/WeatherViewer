package com.weatherviewer.dto;

import com.weatherviewer.validation.annotation.Latitude;
import com.weatherviewer.validation.annotation.Longitude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class GeoLocationDto {

    @NotBlank(message = "Location name cannot be blank")
    private String name;

    @Latitude
    private Double latitude;

    @Longitude
    private Double longitude;

    @Pattern(regexp = "^\\s*$|.*\\S.*", message = "Country must not be blank if provided")
    private String country;

    private String state;

}
