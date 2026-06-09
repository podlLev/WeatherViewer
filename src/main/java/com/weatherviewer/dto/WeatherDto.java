package com.weatherviewer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.weatherviewer.dto.enums.TimeOfDay;
import com.weatherviewer.dto.enums.WeatherCondition;
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
public class WeatherDto {

    @NotNull(message = "Weather condition cannot be null")
    private WeatherCondition weatherCondition;

    @NotNull(message = "Time of day cannot be null")
    private TimeOfDay timeOfDay;

    @NotBlank(message = "Description cannot be blank")
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    @NotNull(message = "Temperature cannot be null")
    @Min(value = -100, message = "Temperature is below realistic physical bounds (-100°C)")
    @Max(value = 60, message = "Temperature is above realistic physical bounds (60°C)")
    private Double temperature;

    @NotNull(message = "Feels like temperature cannot be null")
    @Min(value = -100, message = "Feels like temperature is below realistic physical bounds (-100°C)")
    @Max(value = 60, message = "Feels like temperature is above realistic physical bounds (60°C)")
    private Double temperatureFeelsLike;

    @Min(value = -100, message = "Minimum temperature is below realistic physical bounds (-100°C)")
    @Max(value = 60, message = "Minimum temperature is above realistic physical bounds (60°C)")
    private Double temperatureMinimum;

    @Min(value = -100, message = "Maximum temperature is below realistic physical bounds (-100°C)")
    @Max(value = 60, message = "Maximum temperature is above realistic physical bounds (60°C)")
    private Double temperatureMaximum;

    @Min(value = 0, message = "Humidity must be between 0 and 100")
    @Max(value = 100, message = "Humidity must be between 0 and 100")
    private Integer humidity;

    @Min(value = 0, message = "Pressure cannot be negative")
    private Integer pressure;

    @Min(value = 0, message = "Wind speed cannot be negative")
    private Double windSpeed;

    @Min(value = 0, message = "Wind direction must be between 0 and 360 degrees")
    @Max(value = 360, message = "Wind direction must be between 0 and 360 degrees")
    private Integer windDirection;

    @Min(value = 0, message = "Wind gust cannot be negative")
    private Double windGust;

    @Min(value = 0, message = "Cloudiness must be between 0 and 100")
    @Max(value = 100, message = "Cloudiness must be between 0 and 100")
    private Integer cloudiness;

    @NotNull(message = "Date cannot be null")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date date;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date sunrise;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date sunset;

}
