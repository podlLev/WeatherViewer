package com.weatherviewer.dto;

import com.weatherviewer.validation.annotation.Latitude;
import com.weatherviewer.validation.annotation.Longitude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @Latitude
    @NotNull(message = "Latitude is required")
    private Double latitude;

    @Longitude
    @NotNull(message = "Longitude is required")
    private Double longitude;

    @Pattern(regexp = ".*\\S.*", message = "Country must not be blank if provided")
    private String country;

    private String state;

}
