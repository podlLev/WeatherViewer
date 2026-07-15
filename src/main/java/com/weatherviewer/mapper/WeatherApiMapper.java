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

/**
 * MapStruct mapper that converts raw OpenWeatherMap JSON responses into
 * this application's DTOs.
 * <p>
 * Because the OpenWeatherMap API returns loosely-typed, nested JSON rather
 * than a fixed Java shape, each target field is extracted with a
 * {@link JsonNodeUtils} path expression (e.g. {@code "main.temp"},
 * {@code "weather[0].id"}) instead of a plain property mapping. The
 * implementation is generated at compile time.
 */
@Mapper(componentModel = "spring", imports = {
        JsonNodeUtils.class,
        WeatherCondition.class,
        TimeOfDay.class,
        StringUtils.class
})
public interface WeatherApiMapper {

    /**
     * Converts a "current weather" or single forecast-entry JSON node from
     * OpenWeatherMap into a {@link WeatherDto}, deriving the broad
     * {@link WeatherCondition} and {@link TimeOfDay} from the raw condition
     * code and timestamp.
     */
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

    /**
     * Converts a single geocoding-result JSON node from OpenWeatherMap's
     * Geocoding API into a {@link GeoLocationDto}.
     */
    @Mapping(target = "name", expression = "java(JsonNodeUtils.getString(jsonNode, \"name\"))")
    @Mapping(target = "latitude", expression = "java(JsonNodeUtils.getDouble(jsonNode, \"lat\"))")
    @Mapping(target = "longitude", expression = "java(JsonNodeUtils.getDouble(jsonNode, \"lon\"))")
    @Mapping(target = "country", expression = "java(JsonNodeUtils.getString(jsonNode, \"country\"))")
    @Mapping(target = "state", expression = "java(JsonNodeUtils.getString(jsonNode, \"state\"))")
    GeoLocationDto toGeoLocationDto(JsonNode jsonNode);

}
