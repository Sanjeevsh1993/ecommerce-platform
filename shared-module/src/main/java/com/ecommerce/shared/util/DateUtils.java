package com.ecommerce.shared.util;

// STRANGLER FIG - Phase: 3 - Domain: Shared
// Migrated from: No equivalent — old system used java.util.Date throughout
// BC References: BC-054 (orderDate, workQueueDueDate fields on OrderHistoryVO)

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Date/time utility methods.
 *
 * The old system used java.util.Date for orderDate and workQueueDueDate (BC-054).
 * New system uses java.time.* (Instant, LocalDate) throughout.
 * These helpers bridge the gap during migration.
 */
public final class DateUtils {

    public static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public static final ZoneId DEFAULT_ZONE = ZoneId.of("America/New_York");

    private DateUtils() {}

    /** Convert legacy java.util.Date to Instant */
    public static Instant toInstant(Date date) {
        return date == null ? null : date.toInstant();
    }

    /** Convert Instant to legacy java.util.Date (for backward-compatible mapping) */
    public static Date toDate(Instant instant) {
        return instant == null ? null : Date.from(instant);
    }

    /** Format Instant for display: MM/dd/yyyy */
    public static String format(Instant instant) {
        if (instant == null) return null;
        return ZonedDateTime.ofInstant(instant, DEFAULT_ZONE)
                .format(DISPLAY_DATE_FORMAT);
    }

    /** Format LocalDate for display: MM/dd/yyyy */
    public static String format(LocalDate date) {
        return date == null ? null : date.format(DISPLAY_DATE_FORMAT);
    }

    /** Parse MM/dd/yyyy string to LocalDate */
    public static LocalDate parseDisplayDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        return LocalDate.parse(dateStr.trim(), DISPLAY_DATE_FORMAT);
    }

    /** Current date as Instant */
    public static Instant now() {
        return Instant.now();
    }
}
