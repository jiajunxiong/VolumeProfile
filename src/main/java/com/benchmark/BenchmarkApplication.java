package com.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalTime;

import static com.benchmark.BenchmarkConstant.FILE_PATH;
import static com.benchmark.BenchmarkConstant.TIME_FORMATTER;

public class BenchmarkApplication {
    private static final Logger logger = LoggerFactory.getLogger(BenchmarkApplication.class);

    public static void main(String[] args) {
        try {
            // Load volume profile from CSV
            VolumeProfile volumeProfile = new VolumeProfile(FILE_PATH);

            // Example usage: calculate cumulative volume
            LocalTime start = LocalTime.parse("09:30", TIME_FORMATTER);
            LocalTime end = LocalTime.parse("11:30", TIME_FORMATTER);
            logger.info(volumeProfile.getEntry(start));
            double cumulativePercentage = volumeProfile.getCumulativePercentage(start, end);
            logger.info("Cumulative volume from {} to {}: {}", start, end, String.format("%.2f%%", cumulativePercentage * 100));

            // Example usage: calculate normalized target
            LocalTime currentTime = LocalTime.parse("10:15", TIME_FORMATTER);
            double normalizedTarget = volumeProfile.getNormalizedTargetPercent(currentTime, start, end);
            logger.info("Normalized target for {} (between {} and {}): {}", currentTime, start, end, String.format("%.2f%%", normalizedTarget * 100));

        } catch (IOException e) {
            logger.error("Error reading file: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Error parsing: {}", e.getMessage());
        } catch (ValidationException e) {
            logger.error("validation failed: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("unexpected error: {}", e.getMessage());
        }
    }
}
