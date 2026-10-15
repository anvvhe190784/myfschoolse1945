package vn.edu.fpt.fschool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class FschoolBackendApplication {

  private static final Logger log = LoggerFactory.getLogger(FschoolBackendApplication.class);

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
      if (Files.exists(path) && tryLoadEnvFile(path)) {
        break;
      }
    }
  }

  private static boolean tryLoadEnvFile(Path path) {
    try {
      List<String> lines = Files.readAllLines(path);
      for (String line : lines) {
        parseAndApplyEnvLine(line);
      }
      log.info("[ENV] Loaded environment variables from: {}", path.toAbsolutePath());
      return true;
    } catch (IOException e) {
      log.debug("Could not read candidate env file {}: {}", path, e.getMessage());
      return false;
    }
  }

  private static void parseAndApplyEnvLine(String line) {
    String trimmed = line.trim();
    if (trimmed.startsWith("#") || !trimmed.contains("=")) {
      return;
    }
    int eqIndex = trimmed.indexOf('=');
    String key = trimmed.substring(0, eqIndex).trim();
    String val = trimmed.substring(eqIndex + 1).trim();
    if (System.getProperty(key) == null) {
      String envVal = System.getenv(key);
      System.setProperty(key, envVal != null ? envVal : val);
    }
  }
}
