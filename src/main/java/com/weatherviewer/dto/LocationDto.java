package com.weatherviewer.dto;

import com.weatherviewer.validation.annotation.Latitude;
import com.weatherviewer.validation.annotation.Longitude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "ID cannot be null")
    private UUID id;

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @Latitude
    private Double latitude;

    @Longitude
    private Double longitude;

    @NotNull(message = "User ID cannot be null")
    private UUID userId;

    @NotNull(message = "Favorite cannot be null")
    private Boolean favorite;

    @NotNull(message = "CreatedAt cannot be null")
    private LocalDateTime createdAt;

}
