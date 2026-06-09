package com.weatherviewer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.weatherviewer.dto.enums.TimeOfDay;
import com.weatherviewer.dto.enums.WeatherCondition;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    private String description;

    @NotNull(message = "Temperature cannot be null")
    private Double temperature;

    @NotNull(message = "Feels like temperature cannot be null")
    private Double temperatureFeelsLike;

    private Double temperatureMinimum;

    private Double temperatureMaximum;

    @Min(value = 0, message = "Humidity must be between 0 and 100")
    @Max(value = 100, message = "Humidity must be between 0 and 100")
    private Integer humidity;

    private Integer pressure;

    private Double windSpeed;

    private Integer windDirection;

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
