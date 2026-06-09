package com.weatherviewer.dto;

import com.weatherviewer.validation.annotation.Latitude;
import com.weatherviewer.validation.annotation.Longitude;
import com.weatherviewer.validation.annotation.UniqueLocation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.UUID;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@UniqueLocation
public class AddLocationDto {

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @Latitude
    private Double latitude;

    @Longitude
    private Double longitude;

    @NotNull(message = "User ID cannot be null")
    private UUID userId;

    private Boolean favorite;

}
