package com.campus.report.dto.v2;

import com.campus.report.exception.BadRequestException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public enum ReportCategory {
    ELECTRICAL("LISTRIK", "KELISTRIKAN"),
    PLUMBING,
    NETWORK("WIFI", "WI-FI", "INTERNET"),
    HVAC("AC", "AIR CONDITIONING"),
    FURNITURE("MEJA", "KURSI"),
    EQUIPMENT("PROYEKTOR", "PROJECTOR"),
    OTHER;

    public static final String INPUT_PATTERN = "ELECTRICAL|PLUMBING|NETWORK|HVAC|FURNITURE|EQUIPMENT|OTHER";
    private final List<String> readValues;

    ReportCategory(String... aliases) {
        var values = new java.util.ArrayList<>(List.of(aliases));
        values.add(name());
        readValues = List.copyOf(values);
    }

    public List<String> readValues() {
        return readValues;
    }

    public static ReportCategory fromStored(String value) {
        String normalized = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values()).filter(category -> category.readValues.contains(normalized))
                .findFirst().orElse(OTHER);
    }

    public static ReportCategory parseInput(String value) {
        try {
            return valueOf(value);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("category must be an exact official V2 category code");
        }
    }
}
