package com.weatherviewer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.weatherviewer.dto.enums.TimeOfDay;
import com.weatherviewer.dto.enums.WeatherCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "A weather observation or forecast entry")
public class WeatherDto {

    @Schema(description = "Broad weather condition category, derived from the provider's condition code")
    @NotNull(message = "Weather condition cannot be null")
    private WeatherCondition weatherCondition;

    @Schema(description = "Whether this entry falls during daytime or nighttime hours")
    @NotNull(message = "Time of day cannot be null")
    private TimeOfDay timeOfDay;

    @Schema(description = "Human-readable weather description", example = "light rain", maxLength = 255)
    @NotBlank(message = "Description cannot be blank")
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    @Schema(description = "Temperature in degrees Celsius", example = "18.5", minimum = "-100", maximum = "60")
    @NotNull(message = "Temperature cannot be null")
    @Min(value = -100, message = "Temperature is below realistic physical bounds (-100°C)")
    @Max(value = 60, message = "Temperature is above realistic physical bounds (60°C)")
    private Double temperature;

    @Schema(description = "Perceived (\"feels like\") temperature in degrees Celsius", example = "17.9", minimum = "-100", maximum = "60")
    @NotNull(message = "Feels like temperature cannot be null")
    @Min(value = -100, message = "Feels like temperature is below realistic physical bounds (-100°C)")
    @Max(value = 60, message = "Feels like temperature is above realistic physical bounds (60°C)")
    private Double temperatureFeelsLike;

    @Schema(description = "Minimum temperature in degrees Celsius, when available", example = "15.2", minimum = "-100", maximum = "60")
    @Min(value = -100, message = "Minimum temperature is below realistic physical bounds (-100°C)")
    @Max(value = 60, message = "Minimum temperature is above realistic physical bounds (60°C)")
    private Double temperatureMinimum;

    @Schema(description = "Maximum temperature in degrees Celsius, when available", example = "21.0", minimum = "-100", maximum = "60")
    @Min(value = -100, message = "Maximum temperature is below realistic physical bounds (-100°C)")
    @Max(value = 60, message = "Maximum temperature is above realistic physical bounds (60°C)")
    private Double temperatureMaximum;

    @Schema(description = "Relative humidity, percent", example = "72", minimum = "0", maximum = "100")
    @Min(value = 0, message = "Humidity must be between 0 and 100")
    @Max(value = 100, message = "Humidity must be between 0 and 100")
    private Integer humidity;

    @Schema(description = "Atmospheric pressure, hPa", example = "1013")
    @Min(value = 0, message = "Pressure cannot be negative")
    private Integer pressure;

    @Schema(description = "Wind speed, meters/second", example = "3.6")
    @Min(value = 0, message = "Wind speed cannot be negative")
    private Double windSpeed;

    @Schema(description = "Wind direction, degrees from true north", example = "220", minimum = "0", maximum = "360")
    @Min(value = 0, message = "Wind direction must be between 0 and 360 degrees")
    @Max(value = 360, message = "Wind direction must be between 0 and 360 degrees")
    private Integer windDirection;

    @Schema(description = "Wind gust speed, meters/second, when available", example = "6.1")
    @Min(value = 0, message = "Wind gust cannot be negative")
    private Double windGust;

    @Schema(description = "Cloud cover, percent", example = "40", minimum = "0", maximum = "100")
    @Min(value = 0, message = "Cloudiness must be between 0 and 100")
    @Max(value = 100, message = "Cloudiness must be between 0 and 100")
    private Integer cloudiness;

    @Schema(description = "Observation/forecast timestamp, epoch milliseconds", type = "integer", format = "int64")
    @NotNull(message = "Date cannot be null")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date date;

    @Schema(description = "Sunrise time, epoch milliseconds, when available", type = "integer", format = "int64")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date sunrise;

    @Schema(description = "Sunset time, epoch milliseconds, when available", type = "integer", format = "int64")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date sunset;

}
