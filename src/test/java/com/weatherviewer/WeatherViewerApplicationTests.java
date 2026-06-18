package com.weatherviewer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WeatherViewerApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void mainMethodStartsApplication() {
        WeatherViewerApplication.main(new String[] {});
    }

}
