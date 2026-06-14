package com.weatherviewer.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weatherviewer.client.WeatherApiClient;
import com.weatherviewer.dto.GeoLocationDto;
import com.weatherviewer.dto.LocationDto;
import com.weatherviewer.dto.WeatherDto;
import com.weatherviewer.mapper.LocationMapper;
import com.weatherviewer.mapper.WeatherApiMapper;
import com.weatherviewer.model.Location;
import com.weatherviewer.service.helper.WeatherAggregatorHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherApiServiceImplTest {

    @Mock
    private WeatherApiClient weatherApiClient;

    @Mock
    private WeatherAggregatorHelper weatherAggregatorHelper;

    @Mock
    private WeatherApiMapper weatherApiMapper;

    @Mock
    private LocationMapper locationMapper;

    @InjectMocks
    private WeatherApiServiceImpl service;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private JsonNode json(String raw) throws Exception {
        return objectMapper.readTree(raw);
    }

    @Test
    void getWeatherByLocation_locationDto_delegatesToLocation() throws Exception {
        LocationDto dto = new LocationDto()
                .setLatitude(50.45)
                .setLongitude(30.52);
        Location location = new Location()
                .setLatitude(50.45)
                .setLongitude(30.52);
        WeatherDto weatherDto = new WeatherDto();
        JsonNode node = json("{}");

        when(locationMapper.fromDto(dto)).thenReturn(location);
        when(weatherApiClient.fetchCurrentWeatherByCoordinates(50.45, 30.52)).thenReturn(node);
        when(weatherApiMapper.toWeatherDto(node)).thenReturn(weatherDto);

        WeatherDto result = service.getWeatherByLocation(dto);

        assertThat(result).isEqualTo(weatherDto);
    }

    @Test
    void getWeatherByLocation_withCoordinates_fetchesByCoordinates() throws Exception {
        Location location = new Location()
                .setLatitude(50.45)
                .setLongitude(30.52);
        WeatherDto weatherDto = new WeatherDto();
        JsonNode node = json("{}");

        when(weatherApiClient.fetchCurrentWeatherByCoordinates(50.45, 30.52)).thenReturn(node);
        when(weatherApiMapper.toWeatherDto(node)).thenReturn(weatherDto);

        WeatherDto result = service.getWeatherByLocation(location);

        assertThat(result).isEqualTo(weatherDto);
        verify(weatherApiClient).fetchCurrentWeatherByCoordinates(50.45, 30.52);
        verify(weatherApiClient, never()).fetchCurrentWeatherByCity(any());
    }

    @Test
    void getWeatherByLocation_withoutCoordinates_fetchesByCity() throws Exception {
        Location location = new Location()
                .setName("Kyiv")
                .setLatitude(null)
                .setLongitude(null);
        WeatherDto weatherDto = new WeatherDto();
        JsonNode node = json("{}");

        when(weatherApiClient.fetchCurrentWeatherByCity("Kyiv")).thenReturn(node);
        when(weatherApiMapper.toWeatherDto(node)).thenReturn(weatherDto);

        WeatherDto result = service.getWeatherByLocation(location);

        assertThat(result).isEqualTo(weatherDto);
        verify(weatherApiClient).fetchCurrentWeatherByCity("Kyiv");
    }

    @Test
    void getWeatherByLocation_onlyLatitudeNull_fetchesByCity() throws Exception {
        Location location = new Location()
                .setName("Kyiv")
                .setLatitude(null)
                .setLongitude(30.52);
        WeatherDto weatherDto = new WeatherDto();
        JsonNode node = json("{}");

        when(weatherApiClient.fetchCurrentWeatherByCity("Kyiv")).thenReturn(node);
        when(weatherApiMapper.toWeatherDto(node)).thenReturn(weatherDto);

        WeatherDto result = service.getWeatherByLocation(location);

        assertThat(result).isEqualTo(weatherDto);
    }

    @Test
    void getWeatherByLocation_onlyLongitudeNull_fetchesByCity() throws Exception {
        Location location = new Location()
                .setName("Kyiv")
                .setLatitude(50.45)
                .setLongitude(null);
        WeatherDto weatherDto = new WeatherDto();
        JsonNode node = json("{}");

        when(weatherApiClient.fetchCurrentWeatherByCity("Kyiv")).thenReturn(node);
        when(weatherApiMapper.toWeatherDto(node)).thenReturn(weatherDto);

        WeatherDto result = service.getWeatherByLocation(location);

        assertThat(result).isEqualTo(weatherDto);
    }

    @Test
    void getWeatherByCity_returnsWeatherDto() throws Exception {
        JsonNode node = json("{}");
        WeatherDto weatherDto = new WeatherDto();

        when(weatherApiClient.fetchCurrentWeatherByCity("Kyiv")).thenReturn(node);
        when(weatherApiMapper.toWeatherDto(node)).thenReturn(weatherDto);

        WeatherDto result = service.getWeatherByCity("Kyiv");

        assertThat(result).isEqualTo(weatherDto);
    }

    @Test
    void getWeatherByCoordinates_returnsWeatherDto() throws Exception {
        JsonNode node = json("{}");
        WeatherDto weatherDto = new WeatherDto();

        when(weatherApiClient.fetchCurrentWeatherByCoordinates(50.45, 30.52)).thenReturn(node);
        when(weatherApiMapper.toWeatherDto(node)).thenReturn(weatherDto);

        WeatherDto result = service.getWeatherByCoordinates(50.45, 30.52);

        assertThat(result).isEqualTo(weatherDto);
    }

    @Test
    void getDailyForecastByCity_aggregatesHourlyForecast() throws Exception {
        JsonNode listNode = json("{\"list\": [{}, {}]}");
        WeatherDto w1 = new WeatherDto();
        WeatherDto w2 = new WeatherDto();
        List<WeatherDto> daily = List.of(w1);

        when(weatherApiClient.fetchForecastByCity("Kyiv")).thenReturn(listNode);
        when(weatherApiMapper.toWeatherDto(any())).thenReturn(w1).thenReturn(w2);
        when(weatherAggregatorHelper.aggregateDailyForecast(anyList())).thenReturn(daily);

        List<WeatherDto> result = service.getDailyForecastByCity("Kyiv");

        assertThat(result).isEqualTo(daily);
        verify(weatherAggregatorHelper).aggregateDailyForecast(anyList());
    }

    @Test
    void getDailyForecastByCoordinates_aggregatesHourlyForecast() throws Exception {
        JsonNode listNode = json("{\"list\": [{}, {}]}");
        WeatherDto w1 = new WeatherDto();
        List<WeatherDto> daily = List.of(w1);

        when(weatherApiClient.fetchForecastByCoordinates(50.45, 30.52)).thenReturn(listNode);
        when(weatherApiMapper.toWeatherDto(any())).thenReturn(w1);
        when(weatherAggregatorHelper.aggregateDailyForecast(anyList())).thenReturn(daily);

        List<WeatherDto> result = service.getDailyForecastByCoordinates(50.45, 30.52);

        assertThat(result).isEqualTo(daily);
    }

    @Test
    void getHourlyForecastByCity_returnsMappedList() throws Exception {
        JsonNode listNode = json("{\"list\": [{}, {}]}");
        WeatherDto w1 = new WeatherDto();
        WeatherDto w2 = new WeatherDto();

        when(weatherApiClient.fetchForecastByCity("Kyiv")).thenReturn(listNode);
        when(weatherApiMapper.toWeatherDto(any())).thenReturn(w1).thenReturn(w2);

        List<WeatherDto> result = service.getHourlyForecastByCity("Kyiv");

        assertThat(result).hasSize(2);
    }

    @Test
    void getHourlyForecastByCoordinates_returnsMappedList() throws Exception {
        JsonNode listNode = json("{\"list\": [{}, {}]}");
        WeatherDto w1 = new WeatherDto();

        when(weatherApiClient.fetchForecastByCoordinates(50.45, 30.52)).thenReturn(listNode);
        when(weatherApiMapper.toWeatherDto(any())).thenReturn(w1);

        List<WeatherDto> result = service.getHourlyForecastByCoordinates(50.45, 30.52);

        assertThat(result).hasSize(2);
    }

    @Test
    void getCitiesByName_returnsMappedList() throws Exception {
        JsonNode arrayNode = json("[{\"name\": \"Kyiv\"}, {\"name\": \"Lviv\"}]");
        GeoLocationDto g1 = new GeoLocationDto().setName("Kyiv");
        GeoLocationDto g2 = new GeoLocationDto().setName("Lviv");

        when(weatherApiClient.fetchGeocodingByCity("Kyiv")).thenReturn(arrayNode);
        when(weatherApiMapper.toGeoLocationDto(any())).thenReturn(g1).thenReturn(g2);

        List<GeoLocationDto> result = service.getCitiesByName("Kyiv");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(GeoLocationDto::getName)
                .containsExactly("Kyiv", "Lviv");
    }

}
