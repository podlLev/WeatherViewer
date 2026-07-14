package com.weatherviewer.dto;

import com.weatherviewer.validation.annotation.Latitude;
import com.weatherviewer.validation.annotation.Longitude;
import com.weatherviewer.validation.annotation.UniqueLocation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Payload for creating a saved location")
public class AddLocationDto {

    @Schema(description = "Display name for the location", example = "Kyiv", maxLength = 100)
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

    @Schema(description = "Owning user's ID. On /api/v1/locations/my this is set automatically from the " +
            "authenticated caller and any value sent here is ignored.", accessMode = Schema.AccessMode.READ_WRITE)
    private UUID userId;

    @Schema(description = "Whether the location should be marked as a favorite", defaultValue = "false")
    private Boolean favorite;

}
