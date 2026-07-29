package com.weatherviewer.integration;

import com.weatherviewer.dto.WeatherDto;
import com.weatherviewer.dto.enums.WeatherCondition;
import com.weatherviewer.exception.ExternalHttpCallException;
import com.weatherviewer.service.WeatherApiService;
import com.weatherviewer.service.integration.WeatherApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
class WeatherApiIntegrationTest {

    @Autowired
    private RestClient.Builder restClientBuilder;

    @Autowired
    private WeatherApiService weatherApiService;

    private MockRestServiceServer mockServer;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private WeatherApiClient weatherApiClient;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        RestClient mockedClient = restClientBuilder.build();
        ReflectionTestUtils.setField(weatherApiClient, "restClient", mockedClient);
        cacheManager.getCacheNames()
                .forEach(name -> Objects.requireNonNull(cacheManager.getCache(name)).clear());
    }

    private final String weatherJson = """
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
                "wind": {"speed": 5.0, "deg": 180},
                "clouds": {"all": 0},
                "dt": 1700000000,
                "sys": {"sunrise": 1699980000, "sunset": 1700020000},
                "name": "Kyiv"
            }
            """;

    @Test
    void getWeatherByCity_realHttpFlow_returnsMappedWeatherDto() {
        mockServer.expect(method(GET))
                .andRespond(withSuccess(weatherJson, MediaType.APPLICATION_JSON));

        WeatherDto result = weatherApiService.getWeatherByCity("Kyiv");

        assertThat(result.getWeatherCondition()).isEqualTo(WeatherCondition.CLEAR);
        assertThat(result.getTemperature()).isEqualTo(25.0);
        assertThat(result.getDescription()).isEqualTo("Clear sky");
        mockServer.verify();
    }

    @Test
    void getWeatherByCoordinates_realHttpFlow_returnsMappedWeatherDto() {
        mockServer.expect(method(GET))
                .andRespond(withSuccess(weatherJson, MediaType.APPLICATION_JSON));

        WeatherDto result = weatherApiService.getWeatherByCoordinates(50.45, 30.52);

        assertThat(result.getTemperature()).isEqualTo(25.0);
        mockServer.verify();
    }

    @Test
    void getWeatherByCity_apiReturnsError_throwsExternalHttpCallException() {
        mockServer.expect(ExpectedCount.times(3), method(GET))
                .andRespond(MockRestResponseCreators.withServerError());

        assertThatThrownBy(() -> weatherApiService.getWeatherByCity("Kyiv"))
                .isInstanceOf(ExternalHttpCallException.class);

        mockServer.verify();
    }

    @Test
    void getWeatherByCity_malformedJson_throwsExternalHttpCallException() {
        mockServer.expect(ExpectedCount.times(3), method(GET))
                .andRespond(withSuccess("not-valid-json{{{", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> weatherApiService.getWeatherByCity("Kyiv"))
                .isInstanceOf(ExternalHttpCallException.class);

        mockServer.verify();
    }

    @Test
    void getDailyForecastByCity_realHttpFlow_aggregatesCorrectly() {
        String forecastJson = """
                {
                    "list": [
                        {
                            "weather": [{"id": 800, "description": "clear"}],
                            "main": {"temp": 20.0, "feels_like": 19.0, "temp_min": 18.0, "temp_max": 22.0},
                            "dt": 1700000000
                        },
                        {
                            "weather": [{"id": 800, "description": "clear"}],
                            "main": {"temp": 24.0, "feels_like": 23.0, "temp_min": 22.0, "temp_max": 26.0},
                            "dt": 1700003600
                        }
                    ]
                }
                """;

        mockServer.expect(method(GET))
                .andRespond(withSuccess(forecastJson, MediaType.APPLICATION_JSON));

        List<WeatherDto> result = weatherApiService.getDailyForecastByCity("Kyiv");

        assertThat(result).isNotEmpty();
        mockServer.verify();
    }

}
