package org.renaissance.neo4j.ldbc;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for loading + parsing csv parameters
 */
public final class ParameterCsv {
    private ParameterCsv() {
    }

    public enum ParamType {
        STRING,
        LONG,
        INT,
        DOUBLE,
        BOOLEAN,
        TIMESTAMP
    }

    public record ParamColumn(
            String name,
            ParamType type
    ) {
    }

    private static final DateTimeFormatter FINBENCH_TIMESTAMP_FORMAT =
            new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd HH:mm")
                    .optionalStart()
                    .appendPattern(":ss")
                    .optionalStart()
                    .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
                    .optionalEnd()
                    .optionalEnd()
                    .toFormatter();

    public static List<Map<String, Object>> loadParameterSets(String resourcePath) {
        return loadParameterSetsFromLines(ResourceIO.readResourceLinesFlexible(resourcePath));
    }

    public static List<Map<String, Object>> loadParameterSetsFromLines(List<String> lines) {
        List<String> meaningfulLines = new ArrayList<>();

        for (String line : lines) {
            String trimmed = line.trim();

            if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                meaningfulLines.add(trimmed);
            }
        }

        if (meaningfulLines.isEmpty()) {
            return List.of();
        }

        String header = meaningfulLines.get(0);
        String[] headerParts = header.split("\\|", -1);

        List<ParamColumn> columns = new ArrayList<>();

        for (String headerPart : headerParts) {
            columns.add(parseParamColumn(headerPart));
        }

        List<Map<String, Object>> parameterSets = new ArrayList<>();

        for (int i = 1; i < meaningfulLines.size(); i++) {
            String line = meaningfulLines.get(i);
            String[] values = line.split("\\|", -1);

            Map<String, Object> params = new LinkedHashMap<>();

            for (int col = 0; col < columns.size(); col++) {
                ParamColumn column = columns.get(col);
                String rawValue = col < values.length ? values[col] : "";

                Object parsedValue = parseParamValue(rawValue, column.type());
                params.put(column.name(), parsedValue);
            }

            parameterSets.add(params);
        }

        return List.copyOf(parameterSets);
    }

    public static ParamColumn parseParamColumn(String rawColumn) {
        String spec = rawColumn.trim();

        if (spec.startsWith("$")) {
            spec = spec.substring(1);
        }

        String[] parts = spec.split(":", 2);

        String name = parts[0].trim();

        if (name.isEmpty()) {
            throw new IllegalArgumentException("Empty parameter name in column: " + rawColumn);
        }

        ParamType type = ParamType.STRING;

        if (parts.length == 2) {
            type = parseParamType(parts[1].trim());
        }

        return new ParamColumn(name, type);
    }

    public static ParamType parseParamType(String rawType) {
        String type = rawType.trim().toLowerCase();

        return switch (type) {
            case "string", "str" -> ParamType.STRING;
            case "long", "int64" -> ParamType.LONG;
            case "int", "integer", "int32" -> ParamType.INT;
            case "double", "float" -> ParamType.DOUBLE;
            case "bool", "boolean" -> ParamType.BOOLEAN;
            case "timestamp", "time", "datetime", "millis", "epoch_millis" -> ParamType.TIMESTAMP;
            default -> throw new IllegalArgumentException("Unsupported parameter type: " + rawType);
        };
    }

    public static Object parseParamValue(String rawValue, ParamType type) {
        String value = rawValue.trim();

        if (value.isEmpty()) {
            return null;
        }

        return switch (type) {
            case STRING -> value;
            case LONG -> Long.parseLong(value);
            case INT -> Integer.parseInt(value);
            case DOUBLE -> Double.parseDouble(value);
            case BOOLEAN -> Boolean.parseBoolean(value);
            case TIMESTAMP -> parseTimestampMillis(value);
        };
    }

    public static long parseTimestampMillis(String s) {
        String value = s.trim();

        if (value.isEmpty()) {
            throw new IllegalArgumentException("Empty timestamp value");
        }

        if (value.matches("-?\\d+")) {
            return Long.parseLong(value);
        }

        LocalDateTime dateTime = LocalDateTime.parse(value, FINBENCH_TIMESTAMP_FORMAT);
        return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public static String paramTypeForValue(Object value) {
        if (value instanceof Integer) {
            return "int";
        }

        if (value instanceof Long) {
            return "long";
        }

        if (value instanceof Float || value instanceof Double) {
            return "double";
        }

        if (value instanceof Boolean) {
            return "boolean";
        }

        return "string";
    }
}
