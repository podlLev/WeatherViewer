package com.weatherviewer.dto;

import com.weatherviewer.validation.annotation.Latitude;
import com.weatherviewer.validation.annotation.Longitude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Schema(description = "A saved location")
public class LocationDto {

    @Schema(description = "Location ID", accessMode = Schema.AccessMode.READ_ONLY)
    @NotNull(message = "ID cannot be null")
    private UUID id;

    @Schema(description = "Display name", example = "Kyiv", maxLength = 100)
    @NotBlank(message = "Name cannot be blank")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @Schema(description = "Latitude in decimal degrees", example = "50.4501")
    @Latitude
    @NotNull(message = "Latitude is required")
    private Double latitude;

    @Schema(description = "Longitude in decimal degrees", example = "30.5234")
    @Longitude
    @NotNull(message = "Longitude is required")
    private Double longitude;

    @Schema(description = "Owning user's ID", accessMode = Schema.AccessMode.READ_ONLY)
    @NotNull(message = "User ID cannot be null")
    private UUID userId;

    @Schema(description = "Whether this location is marked as a favorite")
    @NotNull(message = "Favorite cannot be null")
    private Boolean favorite;

    @Schema(description = "Timestamp the location was created", accessMode = Schema.AccessMode.READ_ONLY)
    @NotNull(message = "CreatedAt cannot be null")
    private LocalDateTime createdAt;

}
