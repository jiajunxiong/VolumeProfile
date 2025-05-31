package com.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;

import static com.benchmark.BenchmarkConstant.*;
import static com.benchmark.BucketType.isBucketType;
import static com.benchmark.Validation.validateEntries;
import static com.benchmark.Validation.validatePercentage;

/**
 * A class for handling volume profile data from CSV files.
 * Provides functionality to load, validate and handle volume profiles.
 */
public class VolumeProfile {
    private final static Logger logger = LoggerFactory.getLogger(VolumeProfile.class);

    // Data structure to hold the volume profile data
    private final List<Entry> entryList = new ArrayList<>();
    private final TreeMap<LocalTime, Entry> entryMap = new TreeMap<>();
    double totalPercentage = 0.0;

    /**
     * Constructor that loads data from a CSV file
     *
     * @param filePath path to the CSV file
     * @throws IOException if an I/O error occurs
     */
    public VolumeProfile(String filePath) throws IOException, ValidationException {
        try {
            loadFromCSV(filePath);
        } catch (FileNotFoundException | ValidationException e) {
            logger.error(e.getMessage());
            logger.info("Loading default volume profile {}", DEFAULT_FILE_PATH);
            try {
                loadFromCSV(DEFAULT_FILE_PATH);
            } catch (FileNotFoundException | ValidationException e1) {
                // generate TWAP profile
                logger.info("Generating TWAP profile");
                generateTwapProfile();
            }
        }
    }

    /**
     * Loads volume profile data from a CSV file
     *
     * @param filePath path to the CSV file
     * @throws IOException if an I/O error occurs
     */
    public void loadFromCSV(String filePath) throws IOException, ValidationException {

        // Check if file exists
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("File not found: " + filePath);
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String headerLine = reader.readLine();

            // Validate header
            if (headerLine == null || !headerLine.equals("start,end,percentage,type")) {
                throw new ValidationException("Invalid or missing header: " + headerLine + ". Expected 'start,end,percentage,type'");
            }

            String line;
            int lineNumber = 1; // Start with 1 for the header

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                parseAndAddEntry(line, lineNumber);
            }

            // Post-loading validations
            validateEntries(entryList);
            validatePercentage(entryList, totalPercentage);
        } catch (ValidationException e) {
            throw new ValidationException(e);
        }
    }

    /**
     * Generate TWAP profile while both symbol based and market default profile not valid
     */
    public void generateTwapProfile() {
        double percentage = (double) 1 /VALID_BUCKETS;
        entryList.add(new Entry(LocalTime.of(9,0), LocalTime.of(9,30), percentage, "POS"));
        entryMap.put(LocalTime.of(9,0), new Entry(LocalTime.of(9,0), LocalTime.of(9,30), percentage, "POS"));
        LocalTime morningStart = LocalTime.of(9,30);
        for (int i = 0; i <= 150; i++) {
            LocalTime current = morningStart.plusMinutes(i);
            LocalTime next = morningStart.plusMinutes(i+1);
            Entry entry = new Entry(current, next, percentage, "CTS");
            entryList.add(entry);
            entryMap.put(current, entry);
        }
        entryList.add(new Entry(LocalTime.of(12,0), LocalTime.of(13,0), 0, "L"));
        entryMap.put(LocalTime.of(12,0), new Entry(LocalTime.of(12,0), LocalTime.of(13,0), 0, "L"));
        LocalTime afternoonStart = LocalTime.of(13,0);
        for (int i = 0; i <= 180; i++) {
            LocalTime current = afternoonStart.plusMinutes(i);
            LocalTime next = afternoonStart.plusMinutes(i+1);
            Entry entry = new Entry(current, next, percentage, "CTS");
            entryList.add(entry);
            entryMap.put(current, entry);
        }

        entryList.add(new Entry(LocalTime.of(16,0), LocalTime.of(16,10), percentage, "CAS"));
        entryMap.put(LocalTime.of(16,0), new Entry(LocalTime.of(16,0), LocalTime.of(16,10), percentage, "CAS"));
        totalPercentage = 1.0;
    }

    /**
     * Parses a CSV line and adds the entry to the profile
     *
     * @param line       CSV line
     * @param lineNumber line number for error reporting
     */
    void parseAndAddEntry(String line, int lineNumber) {
        String[] parts = line.split(",");

        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid format: expected 4 fields but found " + parts.length);
        }

        // Parse start time
        LocalTime startTime;
        try {
            startTime = LocalTime.parse(parts[0], TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid start time: " + parts[0] + " at line " + lineNumber + ". Expected format: hh:mm");
        }

        // Parse end time
        LocalTime endTime;
        try {
            endTime = LocalTime.parse(parts[1], TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid end time: " + parts[1] + " at line " + lineNumber + ". Expected format: hh:mm");
        }

        // Validate time interval
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time: " + parts[0] + " - " + parts[1]);
        }

        // Parse percentage
        double percentage;
        try {
            percentage = Double.parseDouble(parts[2]);
            if (percentage < 0) {
                throw new IllegalArgumentException("Percentage must be non-negative: " + parts[2] + " at line " + lineNumber + ".");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid percentage format: " + parts[2] + " at line " + lineNumber + ".");
        }

        // Parse type
        String type = parts[3].trim();
        if (type.isEmpty()) {
            throw new IllegalArgumentException("Type cannot be empty" + " at line " + lineNumber + ".");
        }

        if (!isBucketType(type, BucketType.class)) {
            throw new IllegalArgumentException("Invalid bucket type: " + type + " at line " + lineNumber + ".");
        }

        // Create and add entry
        Entry entry = new Entry(startTime, endTime, percentage, type);
        entryList.add(entry);
        entryMap.put(startTime, entry);
        totalPercentage += percentage;
    }

    /**
     * Returns cumulative volume profile elapsed between two time points
     *
     * @param startTime the start time
     * @param endTime   the end time
     * @return the cumulative volume percentage between the times
     * @throws IllegalArgumentException if the times are invalid
     */
    public double getCumulativePercentage(LocalTime startTime, LocalTime endTime) {
        // Validate inputs
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start time and end time cannot be null");
        }

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        // Calculate cumulative volume
        double cumulativeVolume = 0.0;

        for (Entry entry : entryList) {
            // Skip entries entirely before startTime or after endTime
            if (entry.endTime().isBefore(startTime) || entry.startTime().isAfter(endTime)) {
                continue;
            }

            if (entry.startTime().isBefore(startTime) && entry.endTime().isAfter(startTime)) {
                // Entry spans startTime - calculate partial contribution
                long totalSeconds = Duration.between(entry.startTime(), entry.endTime()).getSeconds();
                long includedSeconds = Duration.between(startTime, entry.endTime()).getSeconds();
                if (totalSeconds > 0) {
                    cumulativeVolume += entry.percentage() * includedSeconds / totalSeconds;
                }
            } else if (entry.startTime().isBefore(endTime) && entry.endTime().isAfter(endTime)) {
                // Entry spans endTime - calculate partial contribution
                long totalSeconds = Duration.between(entry.startTime(), entry.endTime()).getSeconds();
                long includedSeconds = Duration.between(entry.startTime(), endTime).getSeconds();
                if (totalSeconds > 0) {
                    cumulativeVolume += entry.percentage() * includedSeconds / totalSeconds;
                }
            } else if (!entry.startTime().isBefore(startTime) && !entry.endTime().isAfter(endTime)) {
                // Entry is fully contained - add full percentage
                cumulativeVolume += entry.percentage();
            }
        }

        return cumulativeVolume;
    }

    /**
     * Calculates normalized target percentage for a given time
     *
     * @param time      the time to calculate for
     * @param startTime the period start time
     * @param endTime   the period end time
     * @return normalized target percentage
     * @throws IllegalArgumentException if the times are invalid
     */
    public double getNormalizedTargetPercent(LocalTime time, LocalTime startTime, LocalTime endTime) {
        // Validate inputs
        if (time == null || startTime == null || endTime == null) {
            throw new IllegalArgumentException("Time parameters cannot be null");
        }

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        if (time.isBefore(startTime) || time.isAfter(endTime)) {
            throw new IllegalArgumentException("Time must be between start and end times");
        }

        // Calculate elapsed percentage
        double totalVolumeInPeriod = getCumulativePercentage(startTime, endTime);
        if (totalVolumeInPeriod == 0) {
            return 0.0; // Avoid division by zero
        }

        double elapsedVolume = getCumulativePercentage(startTime, time);

        // Normalize
        return elapsedVolume / totalVolumeInPeriod;
    }

    public String getEntry(String startTime) {
        Entry entry = entryMap.getOrDefault(LocalTime.parse(startTime, TIME_FORMATTER), null);
        String msg = String.format("Entry not found at %s", startTime);
        return Objects.isNull(entry) ? msg : entry.toString();
    }

    public String getEntry(LocalTime startTime) {
        Entry entry = entryMap.getOrDefault(startTime, null);
        String msg = String.format("Entry not found at %s", startTime.toString());
        return Objects.isNull(entry) ? msg : entry.toString();
    }
}