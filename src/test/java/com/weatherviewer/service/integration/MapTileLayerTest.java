package com.weatherviewer.service.integration;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class MapTileLayerTest {

    @Test
    void fromRequestValue_knownLayer_resolves() {
        assertThat(MapTileLayer.fromRequestValue("precipitation")).contains(MapTileLayer.PRECIPITATION);
        assertThat(MapTileLayer.fromRequestValue("clouds")).contains(MapTileLayer.CLOUDS);
        assertThat(MapTileLayer.fromRequestValue("temperature")).contains(MapTileLayer.TEMPERATURE);
        assertThat(MapTileLayer.fromRequestValue("wind")).contains(MapTileLayer.WIND);
    }

    @Test
    void fromRequestValue_isCaseInsensitive() {
        assertThat(MapTileLayer.fromRequestValue("PreCIPitation")).contains(MapTileLayer.PRECIPITATION);
    }

    @Test
    void fromRequestValue_unknownLayer_returnsEmpty() {
        Optional<MapTileLayer> result = MapTileLayer.fromRequestValue("radar");
        assertThat(result).isEmpty();
    }

    @Test
    void fromRequestValue_rejectsOwmCodeDirectly() {
        Optional<MapTileLayer> result = MapTileLayer.fromRequestValue("precipitation_new");
        assertThat(result).isEmpty();
    }

    @Test
    void everyLayerHasAnOwmCode() {
        for (MapTileLayer layer : MapTileLayer.values()) {
            assertThat(layer.getOwmCode()).isNotBlank();
        }
    }

}
