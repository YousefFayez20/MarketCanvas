package org.workshop.marketcanvas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
@EnableScheduling
public class MarketCanvasApplication {

    public static void main(String[] args) {
        loadEnvFile();
        SpringApplication.run(MarketCanvasApplication.class, args);
    }

    private static void loadEnvFile() {
        Path envPath = Path.of(".env");
        if (Files.exists(envPath)) {
            try (var lines = Files.lines(envPath)) {
                lines.map(String::trim)
                        .filter(line -> !line.isEmpty() && !line.startsWith("#") && line.contains("="))
                        .forEach(line -> {
                            int eqIdx = line.indexOf('=');
                            String key = line.substring(0, eqIdx).trim();
                            String value = line.substring(eqIdx + 1).trim();
                            if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
                                value = value.substring(1, value.length() - 1);
                            }
                            if (System.getProperty(key) == null && System.getenv(key) == null) {
                                System.setProperty(key, value);
                            }
                        });
            } catch (Exception ignored) {
            }
        }
    }

}
