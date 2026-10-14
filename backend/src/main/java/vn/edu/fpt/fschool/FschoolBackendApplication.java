package vn.edu.fpt.fschool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FschoolBackendApplication {

  static {
    loadEnv();
  }

  public static void main(String[] args) {
    SpringApplication.run(FschoolBackendApplication.class, args);
  }

  static void loadEnv() {
    Path[] candidatePaths =
        new Path[] {
          Paths.get(".env"), Paths.get("backend", ".env"), Paths.get("..", "backend", ".env")
        };

    for (Path path : candidatePaths) {
      if (Files.exists(path)) {
        try {
          List<String> lines = Files.readAllLines(path);
          for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("#") && trimmed.contains("=")) {
              int eqIndex = trimmed.indexOf('=');
              String key = trimmed.substring(0, eqIndex).trim();
              String val = trimmed.substring(eqIndex + 1).trim();
              if (System.getProperty(key) == null) {
                String envVal = System.getenv(key);
                System.setProperty(key, envVal != null ? envVal : val);
              }
            }
          }
          System.out.println("[ENV] Loaded environment variables from: " + path.toAbsolutePath());
          break;
        } catch (IOException ignored) {
        }
      }
    }
  }
}
