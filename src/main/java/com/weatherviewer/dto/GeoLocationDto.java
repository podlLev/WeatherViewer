package com.weatherviewer.dto;

import com.weatherviewer.validation.annotation.Latitude;
import com.weatherviewer.validation.annotation.Longitude;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "A geocoded candidate location returned by city search")
public class GeoLocationDto {

    @Schema(description = "Location name", example = "London", maxLength = 100)
    @NotBlank(message = "Location name cannot be blank")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @Schema(description = "Latitude in decimal degrees", example = "51.5074")
    @Latitude
    @NotNull(message = "Latitude is required")
    private Double latitude;

    @Schema(description = "Longitude in decimal degrees", example = "-0.1278")
    @Longitude
    @NotNull(message = "Longitude is required")
    private Double longitude;

    @Schema(description = "ISO 3166 country code", example = "GB")
    @Pattern(regexp = ".*\\S.*", message = "Country must not be blank if provided")
    private String country;

    @Schema(description = "State or region, when available", example = "England")
    private String state;

}
