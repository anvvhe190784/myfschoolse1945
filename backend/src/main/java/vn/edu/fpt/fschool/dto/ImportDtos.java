package vn.edu.fpt.fschool.dto;

import java.util.List;

public class ImportDtos {

  public record ImportResultDto(
      int totalRows, int successCount, int failedCount, List<String> errors) {
    public static ImportResultDto of(int totalRows, int successCount, List<String> errors) {
      return new ImportResultDto(totalRows, successCount, errors.size(), errors);
    }
  }
}
