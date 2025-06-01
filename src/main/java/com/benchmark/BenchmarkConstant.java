package com.benchmark;

import java.time.format.DateTimeFormatter;

public class BenchmarkConstant {
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    public static final String FILE_PATH = "src/main/resources/0700_HK.csv";
    public static final String DEFAULT_FILE_PATH = "src/main/resources/HK.csv";
    public static final int VALID_BUCKETS = 332; // HK volume profile buckets number
}
