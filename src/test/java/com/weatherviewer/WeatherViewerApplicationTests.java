package com.weatherviewer;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

class WeatherViewerApplicationTests {

    @Test
    void mainStartsApplication() {
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {

            WeatherViewerApplication.main(new String[0]);

            mocked.verify(() ->
                    SpringApplication.run(
                            eq(WeatherViewerApplication.class),
                            eq(new String[0])
                    ));
        }
    }
}
