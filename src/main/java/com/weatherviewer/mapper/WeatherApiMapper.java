package com.weatherviewer.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.weatherviewer.dto.GeoLocationDto;
import com.weatherviewer.dto.WeatherDto;
import com.weatherviewer.dto.enums.TimeOfDay;
import com.weatherviewer.dto.enums.WeatherCondition;
import com.weatherviewer.utils.JsonNodeUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = {
        JsonNodeUtils.class,
        WeatherCondition.class,
        TimeOfDay.class,
        StringUtils.class
})
public interface WeatherApiMapper {

    @Mapping(target = "weatherCondition", expression = "java(WeatherCondition.getWeatherConditionForCode(JsonNodeUtils.getInt(jsonNode, \"weather[0].id\")))")
    @Mapping(target = "timeOfDay", expression = "java(TimeOfDay.getTimeOfDayForTime(JsonNodeUtils.getLocalDateTime(jsonNode, \"dt\")))")
    @Mapping(target = "description", expression = "java(StringUtils.capitalize(JsonNodeUtils.getString(jsonNode, \"weather[0].description\")))")
    @Mapping(target = "temperature", expression = "java(JsonNodeUtils.getDouble(jsonNode, \"main.temp\"))")
    @Mapping(target = "temperatureFeelsLike", expression = "java(JsonNodeUtils.getDouble(jsonNode, \"main.feels_like\"))")
    @Mapping(target = "temperatureMinimum", expression = "java(JsonNodeUtils.getDouble(jsonNode, \"main.temp_min\"))")
    @Mapping(target = "temperatureMaximum", expression = "java(JsonNodeUtils.getDouble(jsonNode, \"main.temp_max\"))")
    @Mapping(target = "humidity", expression = "java(JsonNodeUtils.getInt(jsonNode, \"main.humidity\"))")
    @Mapping(target = "pressure", expression = "java(JsonNodeUtils.getInt(jsonNode, \"main.pressure\"))")
    @Mapping(target = "windSpeed", expression = "java(JsonNodeUtils.getDouble(jsonNode, \"wind.speed\"))")
    @Mapping(target = "windDirection", expression = "java(JsonNodeUtils.getInt(jsonNode, \"wind.deg\"))")
    @Mapping(target = "windGust", expression = "java(JsonNodeUtils.getDouble(jsonNode, \"wind.gust\"))")
    @Mapping(target = "cloudiness", expression = "java(JsonNodeUtils.getInt(jsonNode, \"clouds.all\"))")
    @Mapping(target = "date", expression = "java(JsonNodeUtils.getDate(jsonNode, \"dt\"))")
    @Mapping(target = "sunrise", expression = "java(JsonNodeUtils.getDate(jsonNode, \"sys.sunrise\"))")
    @Mapping(target = "sunset", expression = "java(JsonNodeUtils.getDate(jsonNode, \"sys.sunset\"))")
    WeatherDto toWeatherDto(JsonNode jsonNode);

    @Mapping(target = "name", expression = "java(JsonNodeUtils.getString(jsonNode, \"name\"))")
    @Mapping(target = "latitude", expression = "java(JsonNodeUtils.getDouble(jsonNode, \"lat\"))")
    @Mapping(target = "longitude", expression = "java(JsonNodeUtils.getDouble(jsonNode, \"lon\"))")
    @Mapping(target = "country", expression = "java(JsonNodeUtils.getString(jsonNode, \"country\"))")
    @Mapping(target = "state", expression = "java(JsonNodeUtils.getString(jsonNode, \"state\"))")
    GeoLocationDto toGeoLocationDto(JsonNode jsonNode);

}
