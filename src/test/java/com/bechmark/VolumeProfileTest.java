package com.benchmark;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class VolumeProfileTest {

    String basePath = "src/test/resources/";

    @Test
    void testLoadFromCSV_ValidFile() throws IOException, ValidationException {
        String filePath = basePath + "valid_data.csv"; // Path to a valid CSV file
        VolumeProfile volumeProfile = new VolumeProfile(filePath);
        assertNotNull(volumeProfile);
    }

    @Test
    void testLoadFromCSV_FileNotFound() {
        String filePath = basePath + "non_existent_file.csv";
        assertThrows(FileNotFoundException.class, () -> {
            new VolumeProfile(filePath);
        });
    }

    @Test
    void testLoadFromCSV_InvalidHeader() {
        String filePath = basePath + "invalid_header.csv"; // Path to a CSV with an invalid header
        assertThrows(Exception.class, () -> {
            new VolumeProfile(filePath);
        });
    }

    @Test
    void testParseAndAddEntry_InvalidFormat() throws ValidationException, IOException {
        String line = "08:00,09:00,0.5"; // Missing type
        VolumeProfile volumeProfile = new VolumeProfile(basePath + "dummy.csv");
        assertThrows(IllegalArgumentException.class, () -> {
            volumeProfile.parseAndAddEntry(line, 2);
        });
    }

    @Test
    void testParseAndAddEntry_InvalidTime() throws ValidationException, IOException {
        String line = "08:00,07:00,0.5,type"; // End time before start time
        VolumeProfile volumeProfile = new VolumeProfile(basePath + "dummy.csv");
        assertThrows(IllegalArgumentException.class, () -> {
            volumeProfile.parseAndAddEntry(line, 2);
        });
    }

    @Test
    void testGetCumulativePercentage_Valid() throws IOException, ValidationException {
        String filePath = basePath + "valid_data.csv"; // Path to a valid CSV file
        VolumeProfile volumeProfile = new VolumeProfile(filePath);
        double cumulative = volumeProfile.getCumulativePercentage(LocalTime.parse("09:40"), LocalTime.parse("09:50"));
        assertEquals(0.034476, cumulative, 0.01);
    }

    @Test
    void testGetCumulativePercentage_InvalidTime() throws ValidationException, IOException {
        VolumeProfile volumeProfile = new VolumeProfile(basePath + "dummy.csv");
        assertThrows(IllegalArgumentException.class, () -> {
            volumeProfile.getCumulativePercentage(LocalTime.parse("09:00"), LocalTime.parse("08:00"));
        });
    }

    @Test
    void testGetNormalizedTargetPercent_Valid() throws IOException, ValidationException {
        String filePath = basePath + "valid_data.csv"; // Path to a valid CSV file
        VolumeProfile volumeProfile = new VolumeProfile(filePath);
        double normalized = volumeProfile.getNormalizedTargetPercent(LocalTime.parse("10:30"), LocalTime.parse("09:00"), LocalTime.parse("16:00"));
        assertEquals(0.25, normalized, 0.01); // Adjust based on expected calculation
    }

    @Test
    void testGetEntry_Valid() throws IOException, ValidationException {
        String filePath = basePath + "valid_data.csv"; // Path to a valid CSV file
        VolumeProfile volumeProfile = new VolumeProfile(filePath);
        String entry = volumeProfile.getEntry("08:00");
        assertNotNull(entry);
    }

    @Test
    void testGetEntry_NotFound() throws IOException, ValidationException {
        String filePath = basePath + "valid_data.csv"; // Path to a valid CSV file
        VolumeProfile volumeProfile = new VolumeProfile(filePath);
        String entry = volumeProfile.getEntry("09:05"); // Assuming this entry does not exist
        assertEquals("Entry not found at 09:05", entry);
    }
}