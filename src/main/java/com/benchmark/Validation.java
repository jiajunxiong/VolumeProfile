package com.benchmark;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.benchmark.BenchmarkConstant.TIME_FORMATTER;

@Getter
@Setter
public class Validation {
    private static final Logger logger = LoggerFactory.getLogger(Validation.class);

    public static void validatePercentage(List<Entry> entryList, double totalPercentage) throws ValidationException {
        // Verify total percentage is approximately 1.0 (allowing for minor floating point errors)
        if (Math.abs(totalPercentage - 1.0) > 0.0001) {
            throw new ValidationException("Total percentage doesn't sum to 1.0: " + totalPercentage);
        }

        entryList.forEach(entry -> {
            String timeRange = entry.startTime().format(TIME_FORMATTER) + "-" + entry.endTime().format(TIME_FORMATTER);
            double percentage = entry.percentage();
            if (percentage > 0.3) {
                String warning = String.format("%s percentage(%.2f) is greater than 0.3", timeRange, percentage * 100);
                logger.info(warning);
            }
        });
    }

    private static Map<String, Long> sumDurationsByType(List<Entry> entryList) {
        return entryList.stream().collect(Collectors.groupingBy(Entry::type, Collectors.summingLong(entry -> {
            long minutes = Duration.between(entry.startTime(), entry.endTime()).toMinutes();
            String timeRange = entry.startTime().format(TIME_FORMATTER) + "-" + entry.endTime().format(TIME_FORMATTER);
            if (minutes < 0) {
                String error = String.format("%s minutes is less than 0", timeRange);
                throw new RuntimeException(error);
            }
            return minutes;
        })));
    }

    /**
     * Validates the entire profile for consistency
     */
    public static void validateEntries(List<Entry> entryList) {
        if (entryList.isEmpty()) {
            throw new RuntimeException("No data entries found in the file");
        }

        // Check for continuity: each end time should match next start time
        for (int i = 0; i < entryList.size() - 1; i++) {
            Entry current = entryList.get(i);
            Entry next = entryList.get(i + 1);
            if (!current.endTime().equals(next.startTime())) {
                throw new RuntimeException("Gap detected between entries: " + current.endTime().format(TIME_FORMATTER));
            }
        }

        Map<String, Long> durationsByType = sumDurationsByType(entryList);
        durationsByType.forEach((type, duration) -> {
            switch (type) {
                case "POS" -> {
                    if (duration != 30) {
                        throw new RuntimeException("Invalid POS duration: " + duration + ". Expected 30 minutes.");
                    }
                }
                case "CAS" -> {
                    if (duration != 10) {
                        throw new RuntimeException("Invalid CAS duration: " + duration + ". Expected 10 minutes.");
                    }
                }
                case "L" -> {
                    if (duration != 60) {
                        throw new RuntimeException("Invalid L duration: " + duration + ". Expected 60 minutes.");
                    }
                }
                case "CTS" -> {
                    if (duration != 330) {
                        throw new RuntimeException("Invalid CTS duration: " + duration + ". Expected 330 minutes.");
                    }
                }
            }
        });
    }
}
