package com.rkisuru.url_shortner.dto;

import java.time.LocalDate;

/**
 * Single data point in the daily click breakdown.
 */
public record DailyClickCount(
        LocalDate date,
        Long count
) {
}
