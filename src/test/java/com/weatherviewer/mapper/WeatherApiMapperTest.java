package com.weatherviewer.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weatherviewer.dto.GeoLocationDto;
import com.weatherviewer.dto.WeatherDto;
import com.weatherviewer.dto.enums.TimeOfDay;
import com.weatherviewer.dto.enums.WeatherCondition;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class WeatherApiMapperTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final WeatherApiMapperImpl mapper = new WeatherApiMapperImpl();

    private JsonNode json(String raw) throws Exception {
        return objectMapper.readTree(raw);
    }

    @Test
    void toWeatherDto_null_returnsNull() {
        assertThat(mapper.toWeatherDto(null)).isNull();
    }

    @Test
    void toGeoLocationDto_null_returnsNull() {
        assertThat(mapper.toGeoLocationDto(null)).isNull();
    }

    @Test
    void toWeatherDto_mapsAllFields() throws Exception {
        JsonNode node = json("""
                {
                    "weather": [{"id": 800, "description": "clear sky"}],
                    "main": {
                        "temp": 25.0,
                        "feels_like": 24.0,
                        "temp_min": 20.0,
                        "temp_max": 30.0,
                        "humidity": 60,
                        "pressure": 1013
                    },
                    "wind": {"speed": 5.0, "deg": 180, "gust": 7.0},
                    "clouds": {"all": 0},
                    "dt": 1700000000,
                    "sys": {"sunrise": 1699980000, "sunset": 1700020000}
                }
                """);

        WeatherDto result = mapper.toWeatherDto(node);

        assertThat(result.getWeatherCondition()).isEqualTo(WeatherCondition.CLEAR);
        assertThat(result.getDescription()).isEqualTo("Clear sky");
        assertThat(result.getTemperature()).isEqualTo(25.0);
        assertThat(result.getTemperatureFeelsLike()).isEqualTo(24.0);
        assertThat(result.getTemperatureMinimum()).isEqualTo(20.0);
        assertThat(result.getTemperatureMaximum()).isEqualTo(30.0);
        assertThat(result.getHumidity()).isEqualTo(60);
        assertThat(result.getPressure()).isEqualTo(1013);
        assertThat(result.getWindSpeed()).isEqualTo(5.0);
        assertThat(result.getWindDirection()).isEqualTo(180);
        assertThat(result.getWindGust()).isEqualTo(7.0);
        assertThat(result.getCloudiness()).isEqualTo(0);
        assertThat(result.getDate()).isEqualTo(new Date(1700000000L * 1000));
        assertThat(result.getSunrise()).isEqualTo(new Date(1699980000L * 1000));
        assertThat(result.getSunset()).isEqualTo(new Date(1700020000L * 1000));
    }

    @Test
    void toWeatherDto_weatherCondition_thunderstorm() throws Exception {
        JsonNode node = json("""
                {
                    "weather": [{"id": 200, "description": "thunderstorm"}],
                    "main": {"temp": 15.0, "feels_like": 14.0},
                    "dt": 1700000000
                }
                """);

        WeatherDto result = mapper.toWeatherDto(node);

        assertThat(result.getWeatherCondition()).isEqualTo(WeatherCondition.THUNDERSTORM);
    }

    @Test
    void toWeatherDto_timeOfDay_night() throws Exception {
        JsonNode node = json("""
                {
                    "weather": [{"id": 800, "description": "clear"}],
                    "main": {"temp": 15.0, "feels_like": 14.0},
                    "dt": 1700000000
                }
                """);

        WeatherDto result = mapper.toWeatherDto(node);

        assertThat(result.getTimeOfDay()).isIn(TimeOfDay.DAY, TimeOfDay.NIGHT);
    }

    @Test
    void toWeatherDto_missingOptionalFields_returnsNull() throws Exception {
        JsonNode node = json("""
                {
                    "weather": [{"id": 800, "description": "clear"}],
                    "main": {"temp": 25.0, "feels_like": 24.0},
                    "dt": 1700000000
                }
                """);

        WeatherDto result = mapper.toWeatherDto(node);

        assertThat(result.getWindSpeed()).isNull();
        assertThat(result.getWindGust()).isNull();
        assertThat(result.getCloudiness()).isNull();
        assertThat(result.getSunrise()).isNull();
        assertThat(result.getSunset()).isNull();
    }

    @Test
    void toGeoLocationDto_mapsAllFields() throws Exception {
        JsonNode node = json("""
                {
                    "name": "Kyiv",
                    "lat": 50.45,
                    "lon": 30.52,
                    "country": "UA",
                    "state": "Kyiv Oblast"
                }
                """);

        GeoLocationDto result = mapper.toGeoLocationDto(node);

        assertThat(result.getName()).isEqualTo("Kyiv");
        assertThat(result.getLatitude()).isEqualTo(50.45);
        assertThat(result.getLongitude()).isEqualTo(30.52);
        assertThat(result.getCountry()).isEqualTo("UA");
        assertThat(result.getState()).isEqualTo("Kyiv Oblast");
    }

    @Test
    void toGeoLocationDto_missingOptionalFields_returnsNull() throws Exception {
        JsonNode node = json("""
                {
                    "name": "Kyiv",
                    "lat": 50.45,
                    "lon": 30.52
                }
                """);

        GeoLocationDto result = mapper.toGeoLocationDto(node);

        assertThat(result.getCountry()).isNull();
        assertThat(result.getState()).isNull();
    }

    @Test
    void toGeoLocationDto_description_isCapitalized() throws Exception {
        JsonNode node = json("""
                {
                    "weather": [{"id": 800, "description": "clear sky"}],
                    "main": {"temp": 25.0, "feels_like": 24.0},
                    "dt": 1700000000
                }
                """);

        WeatherDto result = mapper.toWeatherDto(node);

        assertThat(result.getDescription()).isEqualTo("Clear sky");
    }

}
