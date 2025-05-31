package com.benchmark;

import java.time.LocalTime;

import static com.benchmark.BenchmarkConstant.TIME_FORMATTER;

/**
 * Represents a single entry in the volume profile
 */
public record Entry(LocalTime startTime, LocalTime endTime, double percentage, String type) {
    @Override
    public String toString() {
        return String.format("[%s-%s] %.2f%% (%s)", startTime.format(TIME_FORMATTER), endTime.format(TIME_FORMATTER), percentage * 100, BucketType.valueOf(type));
    }
}