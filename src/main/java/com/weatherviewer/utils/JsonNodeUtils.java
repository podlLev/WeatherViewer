package com.weatherviewer.utils;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class JsonNodeUtils {

    private JsonNodeUtils() {
    }

    public static Double getDouble(JsonNode node, String path) {
        JsonNode value = getNodeByPath(node, path);
        return (value != null && value.isNumber()) ? value.asDouble() : null;
    }

    public static Integer getInt(JsonNode node, String path) {
        JsonNode value = getNodeByPath(node, path);
        return (value != null && value.isNumber()) ? value.asInt() : null;
    }

    public static Long getLong(JsonNode node, String path) {
        JsonNode value = getNodeByPath(node, path);
        return (value != null && value.isNumber()) ? value.asLong() : null;
    }

    public static String getString(JsonNode node, String path) {
        JsonNode value = getNodeByPath(node, path);
        return (value != null && value.isTextual()) ? value.asText() : null;
    }

    public static LocalDateTime getLocalDateTime(JsonNode node, String path) {
        Long epoch = getLong(node, path);
        return (epoch != null)
                ? LocalDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneId.systemDefault())
                : null;
    }

    public static Date getDate(JsonNode node, String path) {
        Long epoch = getLong(node, path);
        return (epoch != null) ? new Date(epoch * 1000) : null;
    }

    /**
     * Traverses a JsonNode structure using dot notation and optional array indexes.
     * Example paths:
     *  - "weather[0].description"
     *  - "main.temp"
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
