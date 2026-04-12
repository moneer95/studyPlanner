package com.studyplanner.web;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component("displayTime")
public class DisplayTimeFormatter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("EEEE, MMMM d, yyyy 'at' h:mm:ss a 'UTC'", Locale.US)
            .withZone(ZoneOffset.UTC);

    public String format(Instant instant) {
        if (instant == null) {
            return "—";
        }
        return FORMATTER.format(instant);
    }
}
