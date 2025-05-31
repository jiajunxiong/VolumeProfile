# Volume Profile Analyzer

![Java](https://img.shields.io/badge/java-17%2B-blue)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

The Volume Profile Analyzer is a Java application for processing and analyzing financial volume profile data. It loads volume distribution data from CSV files, validates its integrity, and provides methods to calculate cumulative volumes and normalized target percentages for specific time periods.

## Key Features

- 📊 Load and validate volume profile data from CSV files
- ⏱️ Calculate cumulative volume between any two time points
- 📈 Compute normalized target percentages for intraday periods
- ✅ Comprehensive data validation (time continuity, percentage sums, format checks)
- 🚀 Simple and efficient implementation

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Installation
```bash
git clone https://github.com/jiajunxiong/VolumeProfile.git
cd VolumeProfile
mvn clean install
```

### Data Format
The CSV file must have the following header and format:
```
start,end,percentage,type
09:00,09:30,0.041247,POS
09:30,09:31,0.016967,CTS
09:31,09:32,0.007971,CTS
09:32,09:33,0.007238,CTS
09:33,09:34,0.004610,CTS
```

### Example Usage
```java
public static void main(String[] args) {
    try {
        // Load volume profile from CSV
        VolumeProfileHandler handler = new VolumeProfileHandler("data/volume_profile.csv");
        
        // Define analysis period
        LocalTime periodStart = LocalTime.parse("09:30", DateTimeFormatter.ofPattern("HH:mm"));
        LocalTime periodEnd = LocalTime.parse("11:30", DateTimeFormatter.ofPattern("HH:mm"));
        
        // Calculate cumulative volume
        double cumulativeVolume = handler.getCumulativeVolume(periodStart, periodEnd);
        System.out.printf("Cumulative volume: %.4f%n", cumulativeVolume);
        
        // Calculate normalized target percentage
        LocalTime currentTime = LocalTime.parse("10:15", DateTimeFormatter.ofPattern("HH:mm"));
        double normalizedTarget = handler.getNormalizedTargetPercent(currentTime, periodStart, periodEnd);
        System.out.printf("Normalized target: %.2f%%%n", normalizedTarget * 100);
        
    } catch (IOException e) {
        System.err.println("Error loading file: " + e.getMessage());
    } catch (Exception e) {
        System.err.println("Processing error: " + e.getMessage());
    }
}
```

## API Reference

### VolumeProfileHandler
| Method | Description |
|--------|-------------|
| `loadFromCSV(String filePath)` | Loads and validates volume profile data from CSV file(0700_HK.csv), if failed will load from market default CSV file(HK.csv), if both not available, will generate TWAP profile instead |
| `getCumulativeVolume(LocalTime start, LocalTime end)` | Calculates cumulative volume between two time points |
| `getNormalizedTargetPercent(LocalTime time, LocalTime periodStart, LocalTime periodEnd)` | Calculates normalized target percentage for a specific time within a period |

### Data Validation Rules
1. CSV must have proper header: `start,end,percentage,type`
2. Time ranges must be continuous (end time of previous entry = start time of next)
3. Total percentage must sum to 1.0 ± 0.0001
4. All time values must be in HH:mm format
5. Percentage values must be non-negative
6. Type field cannot be empty

## Project Structure
```
VolumeProfile/
├── src/
│   ├── main/java/com/benchmark/
│   │   └── VolumeProfile.java  # Core implementation
│   └── resources/                     # Sample data files
├── pom.xml                            # Maven configuration
└── README.md                          # This documentation
```

## Contributing
Contributions are welcome! Please follow these steps:
1. Fork the repository
2. Create a new branch (`git checkout -b feature/your-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin feature/your-feature`)
5. Open a pull request

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments
- Thanks to all contributors who helped improve this project
- Inspired by financial volume profile analysis techniques
