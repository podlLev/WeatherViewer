package com.weatherviewer.utils;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Null-safe helpers for pulling typed values out of a Jackson {@link JsonNode}
 * tree using a simple dot/bracket path syntax (e.g. {@code "main.temp"},
 * {@code "weather[0].description"}).
 * <p>
 * Used by {@link com.weatherviewer.mapper.WeatherApiMapper} to extract only
 * the fields this application needs from OpenWeatherMap's larger,
 * loosely-typed JSON responses, without failing on missing/differently-typed
 * fields — every getter returns {@code null} instead of throwing when the
 * path doesn't resolve or the value isn't the expected type.
 */
public class JsonNodeUtils {

    private JsonNodeUtils() {
    }

    /** Resolves {@code path} and returns it as a {@code Double}, or {@code null} if missing/non-numeric. */
    public static Double getDouble(JsonNode node, String path) {
        JsonNode value = getNodeByPath(node, path);
        return (value != null && value.isNumber()) ? value.asDouble() : null;
    }

    /** Resolves {@code path} and returns it as an {@code Integer}, or {@code null} if missing/non-numeric. */
    public static Integer getInt(JsonNode node, String path) {
        JsonNode value = getNodeByPath(node, path);
        return (value != null && value.isNumber()) ? value.asInt() : null;
    }

    /** Resolves {@code path} and returns it as a {@code Long}, or {@code null} if missing/non-numeric. */
    public static Long getLong(JsonNode node, String path) {
        JsonNode value = getNodeByPath(node, path);
        return (value != null && value.isNumber()) ? value.asLong() : null;
    }

    /** Resolves {@code path} and returns it as a {@code String}, or {@code null} if missing/non-textual. */
    public static String getString(JsonNode node, String path) {
        JsonNode value = getNodeByPath(node, path);
        return (value != null && value.isTextual()) ? value.asText() : null;
    }

    /** Resolves {@code path} as a Unix epoch-seconds value and converts it to a {@link LocalDateTime} in the server's default zone. */
    public static LocalDateTime getLocalDateTime(JsonNode node, String path) {
        Long epoch = getLong(node, path);
        return (epoch != null)
                ? LocalDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneId.systemDefault())
                : null;
    }

    /** Resolves {@code path} as a Unix epoch-seconds value and converts it to a {@link Date}. */
    public static Date getDate(JsonNode node, String path) {
        Long epoch = getLong(node, path);
        return (epoch != null) ? new Date(epoch * 1000) : null;
    }

    /**
     * Traverses a JsonNode structure using dot notation and optional array indexes.
     * Example paths:
     *  - "weather[0].description"
     *  - "main.temp"
     *
     * @param root the root node to search from
     * @param path dot/bracket path as described above
     * @return the resolved node, or {@code null} if {@code root}/{@code path}
     *         is {@code null}/blank, or any segment of the path doesn't exist
     */
    public static JsonNode getNodeByPath(JsonNode root, String path) {
        if (root == null || path == null || path.isBlank()) {
            return null;
        }

        String[] parts = path.split("\\.");
        JsonNode current = root;

        for (String part : parts) {
            if (current == null) return null;

            if (part.contains("[")) {
                String fieldName = part.substring(0, part.indexOf("["));
                int index = Integer.parseInt(part.replaceAll(".*\\[(-?\\d+)]", "$1"));

                if (current.has(fieldName) && current.get(fieldName).isArray()) {
                    JsonNode arrayNode = current.get(fieldName);
                    current = (index >= 0 && index < arrayNode.size()) ? arrayNode.get(index) : null;
                } else {
                    return null;
                }
            } else {
                current = current.has(part) ? current.get(part) : null;
            }
        }

        return current;
    }

}
