package com.ecommerce.shared.exception;

// BC References: BC-046, BC-047, BC-054

import com.ecommerce.shared.util.CorrelationIdUtils;
import com.ecommerce.shared.util.DateUtils;
import com.ecommerce.shared.util.StringUtils;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class UtilsTest {

    // ── CorrelationIdUtils ────────────────────────────────────────────────────

    @Test
    void correlationId_generate_returnsNonBlankUuid() {
        String id = CorrelationIdUtils.generate();
        assertThat(id).isNotBlank();
        assertThat(id).matches("[0-9a-f-]{36}");
    }

    @Test
    void correlationId_resolveOrGenerate_returnsExistingWhenNonBlank() {
        String result = CorrelationIdUtils.resolveOrGenerate("existing-id");
        assertThat(result).isEqualTo("existing-id");
    }

    @Test
    void correlationId_resolveOrGenerate_generatesWhenNull() {
        String result = CorrelationIdUtils.resolveOrGenerate(null);
        assertThat(result).isNotBlank();
    }

    @Test
    void correlationId_resolveOrGenerate_generatesWhenBlank() {
        String result = CorrelationIdUtils.resolveOrGenerate("   ");
        assertThat(result).isNotBlank();
    }

    @Test
    void correlationId_setAndGetMdc_roundTrips() {
        CorrelationIdUtils.setMdc("test-correlation");
        assertThat(CorrelationIdUtils.fromMdc()).isEqualTo("test-correlation");
        CorrelationIdUtils.clearMdc();
    }

    @Test
    void correlationId_fromMdc_returnsUnknownWhenNotSet() {
        CorrelationIdUtils.clearMdc();
        assertThat(CorrelationIdUtils.fromMdc()).isEqualTo("unknown");
    }

    // ── DateUtils ─────────────────────────────────────────────────────────────

    @Test
    void dateUtils_format_instant_returnsMmDdYyyy() {
        LocalDate date = LocalDate.of(2025, 4, 12);
        Instant instant = date.atStartOfDay(DateUtils.DEFAULT_ZONE).toInstant();
        assertThat(DateUtils.format(instant)).isEqualTo("04/12/2025");
    }

    @Test
    void dateUtils_format_localDate_returnsMmDdYyyy() {
        assertThat(DateUtils.format(LocalDate.of(2025, 1, 5))).isEqualTo("01/05/2025");
    }

    @Test
    void dateUtils_format_null_returnsNull() {
        assertThat(DateUtils.format((Instant) null)).isNull();
        assertThat(DateUtils.format((LocalDate) null)).isNull();
    }

    @Test
    void dateUtils_parseDisplayDate_parsesCorrectly() {
        LocalDate result = DateUtils.parseDisplayDate("04/12/2025");
        assertThat(result).isEqualTo(LocalDate.of(2025, 4, 12));
    }

    @Test
    void dateUtils_parseDisplayDate_nullOrBlank_returnsNull() {
        assertThat(DateUtils.parseDisplayDate(null)).isNull();
        assertThat(DateUtils.parseDisplayDate("   ")).isNull();
    }

    @Test
    void dateUtils_toInstantAndBack_roundTrips() {
        java.util.Date original = new java.util.Date();
        Instant instant = DateUtils.toInstant(original);
        java.util.Date back = DateUtils.toDate(instant);
        assertThat(back.getTime()).isEqualTo(original.getTime());
    }

    // ── StringUtils ───────────────────────────────────────────────────────────

    @Test
    void stringUtils_isBlank_nullAndEmpty() {
        assertThat(StringUtils.isBlank(null)).isTrue();
        assertThat(StringUtils.isBlank("")).isTrue();
        assertThat(StringUtils.isBlank("  ")).isTrue();
        assertThat(StringUtils.isBlank("hello")).isFalse();
    }

    @Test
    void stringUtils_trimToNull_returnsNullForBlank() {
        assertThat(StringUtils.trimToNull("  ")).isNull();
        assertThat(StringUtils.trimToNull(null)).isNull();
        assertThat(StringUtils.trimToNull("  hello  ")).isEqualTo("hello");
    }

    @Test
    void stringUtils_truncate_shortString_unchanged() {
        assertThat(StringUtils.truncate("hi", 10)).isEqualTo("hi");
    }

    @Test
    void stringUtils_truncate_longString_appendsEllipsis() {
        String result = StringUtils.truncate("abcdefghij", 7);
        assertThat(result).hasSize(7);
        assertThat(result).endsWith("...");
    }

    @Test
    void stringUtils_mask_showsOnlyLastFour() {
        assertThat(StringUtils.mask("1234567890")).isEqualTo("******7890");
    }

    @Test
    void stringUtils_mask_shortValue_returnsStars() {
        assertThat(StringUtils.mask("ab")).isEqualTo("****");
    }
}
