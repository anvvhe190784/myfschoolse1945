package vn.edu.fpt.fschool.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.fschool.dto.ImportDtos.ImportResultDto;
import vn.edu.fpt.fschool.model.ClassEnrollment;
import vn.edu.fpt.fschool.model.ClassEntity;
import vn.edu.fpt.fschool.model.Grade;
import vn.edu.fpt.fschool.model.Schedule;
import vn.edu.fpt.fschool.model.User;
import vn.edu.fpt.fschool.repository.ClassEnrollmentRepository;
import vn.edu.fpt.fschool.repository.ClassRepository;
import vn.edu.fpt.fschool.repository.GradeRepository;
import vn.edu.fpt.fschool.repository.ScheduleRepository;
import vn.edu.fpt.fschool.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminImportService {

  private static final Map<Integer, LocalTime[]> SLOT_TIMES =
      Map.of(
          1, new LocalTime[] {LocalTime.of(7, 30), LocalTime.of(9, 50)},
          2, new LocalTime[] {LocalTime.of(10, 0), LocalTime.of(12, 20)},
          3, new LocalTime[] {LocalTime.of(12, 50), LocalTime.of(15, 10)},
          4, new LocalTime[] {LocalTime.of(15, 20), LocalTime.of(17, 40)},
          5, new LocalTime[] {LocalTime.of(17, 50), LocalTime.of(20, 10)},
          6, new LocalTime[] {LocalTime.of(20, 20), LocalTime.of(22, 40)});
  private static final String MSG_EMPTY_FILE = "Tệp rỗng hoặc không có dữ liệu";
  private final UserRepository userRepository;
  private final ClassRepository classRepository;
  private final ClassEnrollmentRepository classEnrollmentRepository;
  private final GradeRepository gradeRepository;
  private final ScheduleRepository scheduleRepository;
  private final PasswordEncoder passwordEncoder;

  private String lineError(int lineNum, String msg) {
    return "Dòng " + lineNum + ": " + msg;
  }

  private String classCourseNotFoundError(int lineNum, String classCode, String courseCode) {
    return lineError(
        lineNum, "Không tìm thấy lớp học phần với mã lớp " + classCode + " và môn " + courseCode);
  }

  private boolean isRowEmpty(List<String> row) {
    return row == null || row.isEmpty() || row.stream().allMatch(String::isBlank);
  }

  @Transactional
  public ImportResultDto importUsers(MultipartFile file) {
    List<List<String>> rows = parseFileRows(file);
    if (rows.isEmpty()) {
      return ImportResultDto.of(0, 0, List.of(MSG_EMPTY_FILE));
    }

    // Row 0 is header: rollNumber, email, fullName, phoneNumber, role, status
    List<String> errors = new ArrayList<>();
    List<User> toSave = new ArrayList<>();
    String defaultPasswordHash = passwordEncoder.encode("123456");

    for (int i = 1; i < rows.size(); i++) {
      List<String> row = rows.get(i);
      if (isRowEmpty(row)) {
        continue;
      }
      processUserRow(row, i + 1, toSave, errors, defaultPasswordHash);
    }

    if (!toSave.isEmpty()) {
      userRepository.saveAll(toSave);
      log.info("Admin bulk imported {} users successfully", toSave.size());
    }

    return ImportResultDto.of(rows.size() - 1, toSave.size(), errors);
  }

  private void processUserRow(
      List<String> row,
      int lineNum,
      List<User> toSave,
      List<String> errors,
      String defaultPasswordHash) {
    String rollNumber = getCell(row, 0);
    String email = getCell(row, 1);
    String fullName = getCell(row, 2);
    String phone = getCell(row, 3);
    String role = getCell(row, 4, "STUDENT").toUpperCase();
    String status = getCell(row, 5, "ACTIVE").toUpperCase();

    if (email.isBlank()) {
      errors.add(lineError(lineNum, "Email không được để trống"));
      return;
    }
    if (fullName.isBlank()) {
      errors.add(lineError(lineNum, "Họ và tên không được để trống"));
      return;
    }
    if (userRepository.existsByEmail(email)) {
      errors.add(lineError(lineNum, "Email " + email + " đã tồn tại trong hệ thống"));
      return;
    }
    if (!rollNumber.isBlank() && userRepository.existsByRollNumber(rollNumber)) {
      errors.add(lineError(lineNum, "Mã số " + rollNumber + " đã tồn tại"));
      return;
    }

    User user =
        User.builder()
            .rollNumber(rollNumber.isBlank() ? null : rollNumber)
            .email(email.toLowerCase())
            .passwordHash(defaultPasswordHash)
            .fullName(fullName)
            .phoneNumber(phone.isBlank() ? null : phone)
            .role(role)
            .status(status)
            .tokenVersion(1)
            .avatarUrl(
                "https://api.dicebear.com/7.x/avataaars/svg?seed="
                    + (rollNumber.isBlank() ? email : rollNumber))
            .build();

    toSave.add(user);
  }

  @Transactional
  public ImportResultDto importGrades(MultipartFile file) {
    List<List<String>> rows = parseFileRows(file);
    if (rows.isEmpty()) {
      return ImportResultDto.of(0, 0, List.of(MSG_EMPTY_FILE));
    }

    // Header: rollNumber, classCode, assessmentName, weight, score, feedback, status
    List<String> errors = new ArrayList<>();
    List<Grade> toSave = new ArrayList<>();

    for (int i = 1; i < rows.size(); i++) {
      List<String> row = rows.get(i);
      if (isRowEmpty(row)) {
        continue;
      }
      processGradeRow(row, i + 1, toSave, errors);
    }

    if (!toSave.isEmpty()) {
      gradeRepository.saveAll(toSave);
      log.info("Admin bulk imported {} grades successfully", toSave.size());
    }

    return ImportResultDto.of(rows.size() - 1, toSave.size(), errors);
  }

  private void processGradeRow(
      List<String> row, int lineNum, List<Grade> toSave, List<String> errors) {
    String rollNumber = getCell(row, 0);
    String classCode = getCell(row, 1);
    String courseCode = getCell(row, 2);
    String assessmentName = getCell(row, 3);
    String weightStr = getCell(row, 4);
    String scoreStr = getCell(row, 5);
    String feedback = getCell(row, 6);
    String status = getCell(row, 7, "PUBLISHED");

    if (rollNumber.isBlank()
        || classCode.isBlank()
        || courseCode.isBlank()
        || assessmentName.isBlank()) {
      errors.add(lineError(lineNum, "Thiếu mã sinh viên, mã lớp, mã môn hoặc tên đầu điểm"));
      return;
    }

    Optional<User> studentOpt = userRepository.findByRollNumber(rollNumber);
    if (studentOpt.isEmpty()) {
      errors.add(lineError(lineNum, "Không tìm thấy sinh viên có mã " + rollNumber));
      return;
    }

    Optional<ClassEntity> classOpt =
        classRepository.findByClassCodeAndCourse_CourseCode(classCode, courseCode);
    if (classOpt.isEmpty()) {
      errors.add(classCourseNotFoundError(lineNum, classCode, courseCode));
      return;
    }

    BigDecimal weight;
    BigDecimal score = null;
    try {
      weight = new BigDecimal(weightStr);
      if (!scoreStr.isBlank()) {
        score = new BigDecimal(scoreStr);
      }
    } catch (NumberFormatException e) {
      errors.add(lineError(lineNum, "Trọng số hoặc điểm số không hợp lệ"));
      return;
    }

    Grade grade = new Grade();
    grade.setStudent(studentOpt.get());
    grade.setClassEntity(classOpt.get());
    grade.setAssessmentName(assessmentName);
    grade.setWeight(weight);
    grade.setScore(score);
    grade.setFeedback(feedback.isBlank() ? null : feedback);
    grade.setStatus(status.toUpperCase());
    grade.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

    toSave.add(grade);
  }

  @Transactional
  public ImportResultDto importSchedules(MultipartFile file) {
    List<List<String>> rows = parseFileRows(file);
    if (rows.isEmpty()) {
      return ImportResultDto.of(0, 0, List.of(MSG_EMPTY_FILE));
    }

    // Header: classCode, courseCode, date, slotNumber, room, sessionNo, note, status
    List<String> errors = new ArrayList<>();
    List<Schedule> toSave = new ArrayList<>();

    for (int i = 1; i < rows.size(); i++) {
      List<String> row = rows.get(i);
      if (isRowEmpty(row)) {
        continue;
      }
      processScheduleRow(row, i + 1, toSave, errors);
    }

    if (!toSave.isEmpty()) {
      scheduleRepository.saveAll(toSave);
      log.info("Admin bulk imported {} schedules successfully", toSave.size());
    }

    return ImportResultDto.of(rows.size() - 1, toSave.size(), errors);
  }

  private void processScheduleRow(
      List<String> row, int lineNum, List<Schedule> toSave, List<String> errors) {
    String classCode = getCell(row, 0);
    String courseCode = getCell(row, 1);
    String dateStr = getCell(row, 2);
    String slotStr = getCell(row, 3);
    String room = getCell(row, 4);
    String sessionStr = getCell(row, 5, "1");
    String note = getCell(row, 6);
    String status = getCell(row, 7, "UPCOMING");

    if (classCode.isBlank()
        || courseCode.isBlank()
        || dateStr.isBlank()
        || slotStr.isBlank()
        || room.isBlank()) {
      errors.add(lineError(lineNum, "Thiếu mã lớp, mã môn, ngày học, slot học hoặc phòng học"));
      return;
    }

    Optional<ClassEntity> classOpt =
        classRepository.findByClassCodeAndCourse_CourseCode(classCode, courseCode);
    if (classOpt.isEmpty()) {
      errors.add(classCourseNotFoundError(lineNum, classCode, courseCode));
      return;
    }

    LocalDate date;
    int slot;
    int sessionNo = 1;
    try {
      date = LocalDate.parse(dateStr);
      slot = Integer.parseInt(slotStr);
      if (!sessionStr.isBlank()) {
        sessionNo = Integer.parseInt(sessionStr);
      }
    } catch (Exception e) {
      errors.add(
          lineError(lineNum, "Ngày (YYYY-MM-DD), slot (1-6) hoặc số thứ tự buổi không hợp lệ"));
      return;
    }

    LocalTime[] slotTime =
        SLOT_TIMES.getOrDefault(slot, new LocalTime[] {LocalTime.of(7, 30), LocalTime.of(9, 50)});

    ClassEntity classEntity = classOpt.get();
    Schedule schedule = new Schedule();
    schedule.setClassEntity(classEntity);
    schedule.setDate(date);
    schedule.setSlotNumber(slot);
    schedule.setStartTime(slotTime[0]);
    schedule.setEndTime(slotTime[1]);
    schedule.setRoom(room);
    schedule.setSessionNo(sessionNo);
    schedule.setLecturer(classEntity.getLecturer());
    schedule.setNote(note.isBlank() ? null : note);
    schedule.setStatus(status.toUpperCase());

    toSave.add(schedule);
  }

  @Transactional
  public ImportResultDto importEnrollments(MultipartFile file) {
    List<List<String>> rows = parseFileRows(file);
    if (rows.isEmpty()) {
      return ImportResultDto.of(0, 0, List.of(MSG_EMPTY_FILE));
    }

    // Header: rollNumber, classCode, courseCode, status
    List<String> errors = new ArrayList<>();
    List<ClassEnrollment> toSave = new ArrayList<>();

    for (int i = 1; i < rows.size(); i++) {
      List<String> row = rows.get(i);
      if (isRowEmpty(row)) {
        continue;
      }
      processEnrollmentRow(row, i + 1, toSave, errors);
    }

    if (!toSave.isEmpty()) {
      classEnrollmentRepository.saveAll(toSave);
      log.info("Admin bulk imported {} enrollments successfully", toSave.size());
    }

    return ImportResultDto.of(rows.size() - 1, toSave.size(), errors);
  }

  private void processEnrollmentRow(
      List<String> row, int lineNum, List<ClassEnrollment> toSave, List<String> errors) {
    String rollNumber = getCell(row, 0);
    String classCode = getCell(row, 1);
    String courseCode = getCell(row, 2);
    String status = getCell(row, 3, "ENROLLED").toUpperCase();

    if (rollNumber.isBlank() || classCode.isBlank() || courseCode.isBlank()) {
      errors.add(lineError(lineNum, "Thiếu mã sinh viên, mã lớp hoặc mã môn"));
      return;
    }

    Optional<User> studentOpt = userRepository.findByRollNumber(rollNumber);
    if (studentOpt.isEmpty()) {
      errors.add(lineError(lineNum, "Không tìm thấy sinh viên có mã " + rollNumber));
      return;
    }

    Optional<ClassEntity> classOpt =
        classRepository.findByClassCodeAndCourse_CourseCode(classCode, courseCode);
    if (classOpt.isEmpty()) {
      errors.add(classCourseNotFoundError(lineNum, classCode, courseCode));
      return;
    }

    ClassEntity classEntity = classOpt.get();
    User student = studentOpt.get();

    if (classEnrollmentRepository
        .findByClassEntity_ClassIdAndStudent_UserId(classEntity.getClassId(), student.getUserId())
        .isPresent()) {
      errors.add(
          lineError(
              lineNum,
              "Sinh viên "
                  + rollNumber
                  + " đã ghi danh vào lớp "
                  + classCode
                  + " môn "
                  + courseCode));
      return;
    }

    ClassEnrollment enrollment = new ClassEnrollment();
    enrollment.setClassEntity(classEntity);
    enrollment.setStudent(student);
    enrollment.setStatus(status);
    enrollment.setEnrolledAt(OffsetDateTime.now(ZoneOffset.UTC));

    toSave.add(enrollment);
  }

  public String getTemplateContent(String type) {
    if ("users".equalsIgnoreCase(type)) {
      return """
          rollNumber,email,fullName,phoneNumber,role,status
          SE194510,sv10@fpt.edu.vn,Nguyen Van Muoi,0912345678,STUDENT,ACTIVE
          SE194511,sv11@fpt.edu.vn,Tran Thi Muoi Mot,0987654321,STUDENT,ACTIVE
          """;
    } else if ("grades".equalsIgnoreCase(type)) {
      return """
          rollNumber,classCode,courseCode,assessmentName,weight,score,feedback,status
          HE190784,SE1945,PRM393,Lab 2,10.00,9.0,Lam tot lam,PUBLISHED
          SE194501,SE1945,SWR302,Lab 2,10.00,8.5,Dat yeu cau,PUBLISHED
          """;
    } else if ("schedules".equalsIgnoreCase(type)) {
      return """
          classCode,courseCode,date,slotNumber,room,sessionNo,note,status
          SE1945,PRM393,2026-10-20,1,BE-301,13,Session 13: Project Demo,UPCOMING
          SE1945,SWR302,2026-10-22,2,BE-301,14,Session 14: Final Review,UPCOMING
          """;
    } else if ("enrollments".equalsIgnoreCase(type)) {
      return """
          rollNumber,classCode,courseCode,status
          SE194501,SE1945,PRM393,ENROLLED
          SE194501,SE1945,SWR302,ENROLLED
          SE194501,SE1940,MAE101,ENROLLED
          """;
    }
    return "type,not,found\n";
  }

  private List<List<String>> parseFileRows(MultipartFile file) {
    String filename =
        file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
    try (InputStream is = file.getInputStream()) {
      if (filename.endsWith(".xlsx")) {
        return parseExcelRows(is);
      } else {
        return parseCsvRows(is);
      }
    } catch (Exception e) {
      log.error("Failed to parse import file {}: {}", filename, e.getMessage());
      return Collections.emptyList();
    }
  }

  private List<List<String>> parseCsvRows(InputStream is) throws IOException {
    List<List<String>> result = new ArrayList<>();
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String trimmed = line.trim();
        if (trimmed.isEmpty()) continue;
        String[] parts = trimmed.split(",", -1);
        List<String> row = Arrays.stream(parts).map(p -> p.trim().replace("\"", "")).toList();
        result.add(row);
      }
    }
    return result;
  }

  private List<List<String>> parseExcelRows(InputStream is) throws IOException {
    List<List<String>> result = new ArrayList<>();
    try (Workbook workbook = new XSSFWorkbook(is)) {
      Sheet sheet = workbook.getSheetAt(0);
      for (Row row : sheet) {
        List<String> rowData = new ArrayList<>();
        int lastCol = row.getLastCellNum();
        for (int c = 0; c < lastCol; c++) {
          Cell cell = row.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
          rowData.add(getCellValueAsString(cell).trim());
        }
        result.add(rowData);
      }
    }
    return result;
  }

  private String getCellValueAsString(Cell cell) {
    if (cell == null) return "";
    return switch (cell.getCellType()) {
      case STRING -> cell.getStringCellValue();
      case NUMERIC -> {
        if (DateUtil.isCellDateFormatted(cell)) {
          yield cell.getLocalDateTimeCellValue().toLocalDate().toString();
        }
        double val = cell.getNumericCellValue();
        if (val == Math.floor(val)) {
          yield String.valueOf((long) val);
        }
        yield String.valueOf(val);
      }
      case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
      default -> "";
    };
  }

  private String getCell(List<String> row, int index) {
    return getCell(row, index, "");
  }

  private String getCell(List<String> row, int index, String defaultValue) {
    if (index < row.size() && row.get(index) != null && !row.get(index).isBlank()) {
      return row.get(index).trim();
    }
    return defaultValue;
  }
}
