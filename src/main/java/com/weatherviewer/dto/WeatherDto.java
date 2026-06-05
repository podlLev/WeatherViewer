package com.weatherviewer.dto;

import com.weatherviewer.dto.enums.TimeOfDay;
import com.weatherviewer.dto.enums.WeatherCondition;
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

    private WeatherCondition weatherCondition;
    private TimeOfDay timeOfDay;
    private String description;
    private Double temperature;
    private Double temperatureFeelsLike;
    private Double temperatureMinimum;
    private Double temperatureMaximum;
    private Integer humidity;
    private Integer pressure;
    private Double windSpeed;
    private Integer windDirection;
    private Double windGust;
    private Integer cloudiness;
    private Date date;
    private Date sunrise;
    private Date sunset;

}
