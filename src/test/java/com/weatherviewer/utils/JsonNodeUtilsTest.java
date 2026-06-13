package com.weatherviewer.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JsonNodeUtilsTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    private JsonNode json(String raw) throws Exception {
        return mapper.readTree(raw);
    }

    @Test
    void getNodeByPath_null_root_returnsNull() {
        assertThat(JsonNodeUtils.getNodeByPath(null, "key")).isNull();
    }

    @Test
    void getNodeByPath_null_path_returnsNull() throws Exception {
        JsonNode node = json("{\"key\": 1}");
        assertThat(JsonNodeUtils.getNodeByPath(node, null)).isNull();
    }

    @Test
    void getNodeByPath_blank_path_returnsNull() throws Exception {
        JsonNode node = json("{\"key\": 1}");
        assertThat(JsonNodeUtils.getNodeByPath(node, "   ")).isNull();
    }

    @Test
    void getNodeByPath_simpleKey_returnsNode() throws Exception {
        JsonNode node = json("{\"key\": 42}");
        assertThat(JsonNodeUtils.getNodeByPath(node, "key").asInt()).isEqualTo(42);
    }

    @Test
    void getNodeByPath_nestedKey_returnsNode() throws Exception {
        JsonNode node = json("{\"main\": {\"temp\": 25.5}}");
        assertThat(JsonNodeUtils.getNodeByPath(node, "main.temp").asDouble()).isEqualTo(25.5);
    }

    @Test
    void getNodeByPath_arrayIndex_returnsNode() throws Exception {
        JsonNode node = json("{\"weather\": [{\"description\": \"clear\"}]}");
        assertThat(JsonNodeUtils.getNodeByPath(node, "weather[0].description").asText()).isEqualTo("clear");
    }

    @Test
    void getNodeByPath_outOfBoundsIndex_returnsNull() throws Exception {
        JsonNode node = json("{\"weather\": [{\"description\": \"clear\"}]}");
        assertThat(JsonNodeUtils.getNodeByPath(node, "weather[5].description")).isNull();
    }

    @Test
    void getNodeByPath_missingKey_returnsNull() throws Exception {
        JsonNode node = json("{\"key\": 1}");
        assertThat(JsonNodeUtils.getNodeByPath(node, "missing")).isNull();
    }

    @Test
    void getNodeByPath_missingNestedKey_returnsNull() throws Exception {
        JsonNode node = json("{\"main\": {\"temp\": 25.5}}");
        assertThat(JsonNodeUtils.getNodeByPath(node, "main.missing")).isNull();
    }

    @Test
    void getNodeByPath_arrayFieldNotArray_returnsNull() throws Exception {
        JsonNode node = json("{\"weather\": \"not-an-array\"}");
        assertThat(JsonNodeUtils.getNodeByPath(node, "weather[0].description")).isNull();
    }

    @Test
    void getNodeByPath_negativeIndex_returnsNull() throws Exception {
        JsonNode node = json("{\"weather\": [{\"description\": \"clear\"}]}");
        assertThat(JsonNodeUtils.getNodeByPath(node, "weather[-1].description")).isNull();
    }

    @Test
    void getNodeByPath_arrayFieldMissing_returnsNull() throws Exception {
        JsonNode node = json("{\"other\": \"value\"}");
        assertThat(JsonNodeUtils.getNodeByPath(node, "weather[0].description")).isNull();
    }

    @Test
    void getDouble_validPath_returnsValue() throws Exception {
        JsonNode node = json("{\"main\": {\"temp\": 25.5}}");
        assertThat(JsonNodeUtils.getDouble(node, "main.temp")).isEqualTo(25.5);
    }

    @Test
    void getDouble_missingPath_returnsNull() throws Exception {
        JsonNode node = json("{\"main\": {}}");
        assertThat(JsonNodeUtils.getDouble(node, "main.temp")).isNull();
    }

    @Test
    void getDouble_nonNumericValue_returnsNull() throws Exception {
        JsonNode node = json("{\"main\": {\"temp\": \"hot\"}}");
        assertThat(JsonNodeUtils.getDouble(node, "main.temp")).isNull();
    }

    @Test
    void getInt_validPath_returnsValue() throws Exception {
        JsonNode node = json("{\"humidity\": 80}");
        assertThat(JsonNodeUtils.getInt(node, "humidity")).isEqualTo(80);
    }

    @Test
    void getInt_missingPath_returnsNull() throws Exception {
        JsonNode node = json("{}");
        assertThat(JsonNodeUtils.getInt(node, "humidity")).isNull();
    }

    @Test
    void getInt_nonNumericValue_returnsNull() throws Exception {
        JsonNode node = json("{\"humidity\": \"high\"}");
        assertThat(JsonNodeUtils.getInt(node, "humidity")).isNull();
    }

    @Test
    void getLong_nonNumericValue_returnsNull() throws Exception {
        JsonNode node = json("{\"dt\": \"yesterday\"}");
        assertThat(JsonNodeUtils.getLong(node, "dt")).isNull();
    }

    @Test
    void getLong_validPath_returnsValue() throws Exception {
        JsonNode node = json("{\"dt\": 1700000000}");
        assertThat(JsonNodeUtils.getLong(node, "dt")).isEqualTo(1700000000L);
    }

    @Test
    void getLong_missingPath_returnsNull() throws Exception {
        JsonNode node = json("{}");
        assertThat(JsonNodeUtils.getLong(node, "dt")).isNull();
    }

    @Test
    void getString_validPath_returnsValue() throws Exception {
        JsonNode node = json("{\"weather\": [{\"description\": \"clear sky\"}]}");
        assertThat(JsonNodeUtils.getString(node, "weather[0].description")).isEqualTo("clear sky");
    }

    @Test
    void getString_missingPath_returnsNull() throws Exception {
        JsonNode node = json("{}");
        assertThat(JsonNodeUtils.getString(node, "description")).isNull();
    }

    @Test
    void getString_nonTextualValue_returnsNull() throws Exception {
        JsonNode node = json("{\"description\": 123}");
        assertThat(JsonNodeUtils.getString(node, "description")).isNull();
    }

    @Test
    void getLocalDateTime_validEpoch_returnsLocalDateTime() throws Exception {
        JsonNode node = json("{\"dt\": 1700000000}");
        LocalDateTime result = JsonNodeUtils.getLocalDateTime(node, "dt");
        assertThat(result).isNotNull().isInstanceOf(LocalDateTime.class);
    }

    @Test
    void getLocalDateTime_missingPath_returnsNull() throws Exception {
        JsonNode node = json("{}");
        assertThat(JsonNodeUtils.getLocalDateTime(node, "dt")).isNull();
    }

    @Test
    void getDate_validEpoch_returnsDate() throws Exception {
        JsonNode node = json("{\"dt\": 1700000000}");
        Date result = JsonNodeUtils.getDate(node, "dt");
        assertThat(result).isNotNull();
        assertThat(result.getTime()).isEqualTo(1700000000L * 1000);
    }

    @Test
    void getDate_missingPath_returnsNull() throws Exception {
        JsonNode node = json("{}");
        assertThat(JsonNodeUtils.getDate(node, "dt")).isNull();
    }

}
