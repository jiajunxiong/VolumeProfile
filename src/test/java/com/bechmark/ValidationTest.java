package com.bechmark;

import com.benchmark.Entry;
import com.benchmark.Validation;
import com.benchmark.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ValidationTest {

    @Test
    void testValidatePercentage_Valid() throws ValidationException {
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(LocalTime.now(), LocalTime.now().plusHours(1), 0.5, "POS"));
        entries.add(new Entry(LocalTime.now().plusHours(1), LocalTime.now().plusHours(2), 0.5, "CAS"));

        Validation.validatePercentage(entries, 1.0);
    }

    @Test
    void testValidatePercentage_Invalid() {
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(LocalTime.now(), LocalTime.now().plusHours(1), 0.5, "POS"));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            Validation.validatePercentage(entries, 1.1);
        });
        assertEquals("Total percentage doesn't sum to 1.0: 1.1", exception.getMessage());
    }

    @Test
    void testValidatePercentage_HighPercentageWarning() throws ValidationException {
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(LocalTime.now(), LocalTime.now().plusHours(1), 0.4, "POS"));

        // Capture the logger output (requires additional setup or a logging framework)
        Validation.validatePercentage(entries, 1.0);
        // Verify that the logger output contains the expected warning
    }

    @Test
    void testValidateEntries_EmptyList() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            Validation.validateEntries(new ArrayList<>());
        });
        assertEquals("No data entries found in the file", exception.getMessage());
    }

    @Test
    void testValidateEntries_GapInEntries() {
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(LocalTime.now(), LocalTime.now().plusMinutes(30), 0.5, "POS"));
        entries.add(new Entry(LocalTime.now().plusMinutes(40), LocalTime.now().plusMinutes(60), 0.5, "CAS"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            Validation.validateEntries(entries);
        });
        assertTrue(exception.getMessage().contains("Gap detected between entries"));
    }

    @Test
    void testValidateEntries_InvalidPOSDuration() {
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(LocalTime.now(), LocalTime.now().plusMinutes(25), 0.5, "POS"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            Validation.validateEntries(entries);
        });
        assertEquals("Invalid POS duration: 25. Expected 30 minutes.", exception.getMessage());
    }

    // Additional test cases for CAS, L, and CTS durations can be added similarly
}
