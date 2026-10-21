package vn.edu.fpt.fschool.service;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.fschool.dto.AdminDtos.ClassResponse;
import vn.edu.fpt.fschool.dto.AdminDtos.CourseResponse;
import vn.edu.fpt.fschool.dto.AdminDtos.CreateClassRequest;
import vn.edu.fpt.fschool.dto.AdminDtos.CreateCourseRequest;
import vn.edu.fpt.fschool.dto.AdminDtos.CreateUserRequest;
import vn.edu.fpt.fschool.dto.AdminDtos.UpdateUserStatusRequest;
import vn.edu.fpt.fschool.dto.AdminDtos.UserResponse;
import vn.edu.fpt.fschool.dto.RequestDtos.RequestResponse;
import vn.edu.fpt.fschool.dto.RequestDtos.UpdateRequestStatusDto;
import vn.edu.fpt.fschool.dto.ScheduleDtos.CreateScheduleRequest;
import vn.edu.fpt.fschool.dto.ScheduleDtos.ScheduleResponse;
import vn.edu.fpt.fschool.event.RequestProcessedEvent;
import vn.edu.fpt.fschool.mapper.AcademicMapper;
import vn.edu.fpt.fschool.mapper.UserMapper;
import vn.edu.fpt.fschool.model.ClassEntity;
import vn.edu.fpt.fschool.model.ClassEnrollment;
import vn.edu.fpt.fschool.model.Course;
import vn.edu.fpt.fschool.model.RequestEntity;
import vn.edu.fpt.fschool.model.RequestStatus;
import vn.edu.fpt.fschool.model.Schedule;
import vn.edu.fpt.fschool.model.User;
import vn.edu.fpt.fschool.repository.ClassEnrollmentRepository;
import vn.edu.fpt.fschool.repository.ClassRepository;
import vn.edu.fpt.fschool.repository.CourseRepository;
import vn.edu.fpt.fschool.repository.RequestRepository;
import vn.edu.fpt.fschool.repository.ScheduleRepository;
import vn.edu.fpt.fschool.repository.UserRepository;
import vn.edu.fpt.fschool.util.PostCommitExecutor;

@Service
@Slf4j
public class AdminService {

  private final UserRepository userRepository;
  private final CourseRepository courseRepository;
  private final ClassRepository classRepository;
  private final ClassEnrollmentRepository classEnrollmentRepository;
  private final ScheduleRepository scheduleRepository;
  private final RequestRepository requestRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;
  private final AcademicMapper academicMapper;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final TokenVersionService tokenVersionService;
  private final RefreshTokenService refreshTokenService;
  private final PostCommitExecutor postCommitExecutor;

  @Autowired
  public AdminService(
      UserRepository userRepository,
      CourseRepository courseRepository,
      ClassRepository classRepository,
      ClassEnrollmentRepository classEnrollmentRepository,
      ScheduleRepository scheduleRepository,
      RequestRepository requestRepository,
      PasswordEncoder passwordEncoder,
      UserMapper userMapper,
      AcademicMapper academicMapper,
      ApplicationEventPublisher applicationEventPublisher,
      TokenVersionService tokenVersionService,
      RefreshTokenService refreshTokenService,
      PostCommitExecutor postCommitExecutor) {
    this.userRepository = userRepository;
    this.courseRepository = courseRepository;
    this.classRepository = classRepository;
    this.classEnrollmentRepository = classEnrollmentRepository;
    this.scheduleRepository = scheduleRepository;
    this.requestRepository = requestRepository;
    this.passwordEncoder = passwordEncoder;
    this.userMapper = userMapper;
    this.academicMapper = academicMapper;
    this.applicationEventPublisher = applicationEventPublisher;
    this.tokenVersionService = tokenVersionService;
    this.refreshTokenService = refreshTokenService;
    this.postCommitExecutor = postCommitExecutor;
  }

  // --- USER MANAGEMENT ---
  @Transactional
  public UserResponse createUser(CreateUserRequest req) {
    log.info("Admin creating user with email: {}", req.email());
    if (userRepository.existsByEmail(req.email())) {
      throw new IllegalArgumentException("Email đã tồn tại: " + req.email());
    }
    if (req.rollNumber() != null && userRepository.existsByRollNumber(req.rollNumber())) {
      throw new IllegalArgumentException("Roll number đã tồn tại: " + req.rollNumber());
    }

    User user =
        User.builder()
            .email(req.email())
            .passwordHash(passwordEncoder.encode(req.password()))
            .fullName(req.fullName())
            .rollNumber(req.rollNumber())
            .phoneNumber(req.phoneNumber())
            .role(req.role().toUpperCase())
            .status("ACTIVE")
            .build();

    User saved = userRepository.save(user);
    return userMapper.toUserResponse(saved);
  }

  @Transactional(readOnly = true)
  public List<UserResponse> getAllUsers(Pageable pageable) {
    return findUsers(pageable);
  }

  @Transactional(readOnly = true)
  public List<UserResponse> getAllUsers() {
    return findUsers(PageRequest.of(0, 20));
  }

  private List<UserResponse> findUsers(Pageable pageable) {
    int safeSize = Math.clamp(pageable.getPageSize(), 1, 50);
    Pageable safePageable = PageRequest.of(pageable.getPageNumber(), safeSize, pageable.getSort());
    return userMapper.toUserResponseList(userRepository.findAll(safePageable).getContent());
  }

  @Transactional
  public UserResponse updateUserStatus(Long userId, UpdateUserStatusRequest req) {
    log.info("Admin updating status of user ID: {} to {}", userId, req.status());
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> new IllegalArgumentException("Không tìm thấy user với ID: " + userId));

    String newStatus = req.status().toUpperCase();
    user.setStatus(newStatus);
    if (!"ACTIVE".equals(newStatus)) {
      int newVersion = user.getTokenVersion() != null ? user.getTokenVersion() + 1 : 2;
      user.setTokenVersion(newVersion);
      String userEmail = user.getEmail();
      postCommitExecutor.execute(
          () -> {
            tokenVersionService.updateUserTokenVersion(userEmail, newVersion);
            refreshTokenService.revokeAllUserTokens(userEmail);
          });
    }
    return userMapper.toUserResponse(userRepository.save(user));
  }

  // --- ACADEMIC: COURSES & CLASSES ---
  @Transactional
  public CourseResponse createCourse(CreateCourseRequest req) {
    log.info("Admin creating course: {}", req.courseCode());
    if (courseRepository.existsByCourseCode(req.courseCode())) {
      throw new IllegalArgumentException("Mã môn đã tồn tại: " + req.courseCode());
    }

    Course course = new Course();
    course.setCourseCode(req.courseCode());
    course.setCourseName(req.courseName());
    course.setCredits(req.credits() != null ? req.credits() : 3);
    course.setDescription(req.description());

    Course saved = courseRepository.save(course);
    return academicMapper.toCourseResponse(saved);
  }

  @Transactional(readOnly = true)
  public List<CourseResponse> getAllCourses(Pageable pageable) {
    return findCourses(pageable);
  }

  @Transactional(readOnly = true)
  public List<CourseResponse> getAllCourses() {
    return findCourses(PageRequest.of(0, 50));
  }

  private List<CourseResponse> findCourses(Pageable pageable) {
    int safeSize = Math.clamp(pageable.getPageSize(), 1, 50);
    Pageable safePageable = PageRequest.of(pageable.getPageNumber(), safeSize, pageable.getSort());
    return academicMapper.toCourseResponseList(courseRepository.findAll(safePageable).getContent());
  }

  @Transactional
  public ClassResponse createClass(CreateClassRequest req) {
    log.info("Admin creating class: {}", req.classCode());
    if (classRepository.existsByClassCodeAndCourse_CourseIdAndSemester(
        req.classCode(), req.courseId(), req.semester())) {
      throw new IllegalArgumentException(
          "Lớp học phần đã tồn tại cho mã lớp " + req.classCode() + " với môn học và học kỳ này!");
    }

    Course course =
        courseRepository
            .findById(req.courseId())
            .orElseThrow(
                () -> new IllegalArgumentException("Không tìm thấy môn học ID: " + req.courseId()));

    User lecturer = null;
    if (req.lecturerId() != null) {
      lecturer =
          userRepository
              .findById(req.lecturerId())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "Không tìm thấy giảng viên ID: " + req.lecturerId()));
    }

    ClassEntity classEntity = new ClassEntity();
    classEntity.setClassCode(req.classCode());
    classEntity.setCourse(course);
    classEntity.setLecturer(lecturer);
    classEntity.setSemester(req.semester());
    classEntity.setAcademicYear(req.academicYear() != null ? req.academicYear() : 2026);
    classEntity.setRoomDefault(req.roomDefault());

    ClassEntity saved = classRepository.save(classEntity);
    return academicMapper.toClassResponse(saved);
  }

  @Transactional(readOnly = true)
  public List<ClassResponse> getAllClasses(Pageable pageable) {
    return findClasses(pageable);
  }

  @Transactional(readOnly = true)
  public List<ClassResponse> getAllClasses() {
    return findClasses(PageRequest.of(0, 50));
  }

  private List<ClassResponse> findClasses(Pageable pageable) {
    int safeSize = Math.clamp(pageable.getPageSize(), 1, 50);
    Pageable safePageable = PageRequest.of(pageable.getPageNumber(), safeSize, pageable.getSort());
    return academicMapper.toClassResponseList(classRepository.findAll(safePageable).getContent());
  }

  @Transactional(readOnly = true)
  public List<ClassResponse> getStudentClasses(Long studentId, String semester) {
    log.info("Admin fetching classes for student ID: {} semester: {}", studentId, semester);
    List<ClassEnrollment> enrollments;
    if (semester != null && !semester.isBlank()) {
      enrollments =
          classEnrollmentRepository.findByStudent_UserIdAndClassEntity_Semester(
              studentId, semester);
    } else {
      enrollments = classEnrollmentRepository.findByStudent_UserIdAndStatus(studentId, "ENROLLED");
    }
    List<ClassEntity> classes = enrollments.stream().map(ClassEnrollment::getClassEntity).toList();
    return academicMapper.toClassResponseList(classes);
  }

  @Transactional
  public ScheduleResponse createSchedule(CreateScheduleRequest req) {
    log.info("Admin creating schedule for class ID: {} slot: {}", req.classId(), req.slotNumber());
    ClassEntity classEntity =
        classRepository
            .findById(req.classId())
            .orElseThrow(
                () -> new IllegalArgumentException("Không tìm thấy lớp học ID: " + req.classId()));

    User lecturer = null;
    if (req.lecturerId() != null) {
      lecturer =
          userRepository
              .findById(req.lecturerId())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "Không tìm thấy giảng viên ID: " + req.lecturerId()));
    } else {
      lecturer = classEntity.getLecturer();
    }

    Schedule schedule = new Schedule();
    schedule.setClassEntity(classEntity);
    schedule.setSlotNumber(req.slotNumber());
    schedule.setDate(req.date());
    schedule.setStartTime(req.startTime());
    schedule.setEndTime(req.endTime());
    schedule.setRoom(req.room());
    schedule.setLecturer(lecturer);
    schedule.setNote(req.note());
    schedule.setStatus("UPCOMING");

    Schedule saved = scheduleRepository.save(schedule);
    return academicMapper.toScheduleResponse(saved);
  }

  @Transactional
  public void processRequest(Long requestId, String adminEmail, UpdateRequestStatusDto dto) {
    log.info("Admin {} processing request ID: {} -> {}", adminEmail, requestId, dto.status());
    RequestEntity entity =
        requestRepository
            .findById(requestId)
            .orElseThrow(
                () -> new IllegalArgumentException("Không tìm thấy đơn từ ID: " + requestId));
    User admin = userRepository.findByEmailIgnoreCase(adminEmail).orElse(null);

    RequestStatus currentStatus = RequestStatus.fromString(entity.getStatus());
    RequestStatus targetStatus = RequestStatus.fromString(dto.status());

    if (!currentStatus.canTransitionTo(targetStatus)) {
      throw new IllegalStateException(
          "Không thể chuyển trạng thái đơn từ từ " + currentStatus + " sang " + targetStatus);
    }

    entity.setStatus(targetStatus.name());
    entity.setHandler(admin);
    entity.setResponseNote(dto.responseNote());
    RequestEntity saved = requestRepository.save(entity);

    Long requesterId = entity.getRequester() != null ? entity.getRequester().getUserId() : null;
    if (requesterId != null) {
      applicationEventPublisher.publishEvent(
          new RequestProcessedEvent(
              saved.getRequestId(),
              requesterId,
              saved.getTitle(),
              currentStatus.name(),
              targetStatus.name(),
              dto.responseNote()));
    }
  }

  @Transactional(readOnly = true)
  public List<RequestResponse> getAllRequests(Pageable pageable) {
    return findRequests(pageable);
  }

  @Transactional(readOnly = true)
  public List<RequestResponse> getAllRequests() {
    return findRequests(PageRequest.of(0, 50));
  }

  private List<RequestResponse> findRequests(Pageable pageable) {
    int safeSize = Math.clamp(pageable.getPageSize(), 1, 50);
    Pageable safePageable = PageRequest.of(pageable.getPageNumber(), safeSize, pageable.getSort());
    return academicMapper.toRequestResponseList(
        requestRepository.findAll(safePageable).getContent());
  }
}
