package vn.edu.fpt.fschool.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.fpt.fschool.dto.AdminDtos.ClassResponse;
import vn.edu.fpt.fschool.dto.AdminDtos.CourseResponse;
import vn.edu.fpt.fschool.dto.ClubDtos.ClubResponse;
import vn.edu.fpt.fschool.dto.GradeDtos.GradeResponse;
import vn.edu.fpt.fschool.dto.RequestDtos.RequestResponse;
import vn.edu.fpt.fschool.dto.ScheduleDtos.ScheduleResponse;
import vn.edu.fpt.fschool.model.ClassEntity;
import vn.edu.fpt.fschool.model.Club;
import vn.edu.fpt.fschool.model.Course;
import vn.edu.fpt.fschool.model.Grade;
import vn.edu.fpt.fschool.model.RequestEntity;
import vn.edu.fpt.fschool.model.Schedule;

@Mapper(componentModel = "spring")
public interface AcademicMapper {

  CourseResponse toCourseResponse(Course course);

  List<CourseResponse> toCourseResponseList(List<Course> courses);

  @Mapping(target = "courseCode", source = "course.courseCode")
  @Mapping(target = "courseName", source = "course.courseName")
  @Mapping(
      target = "lecturerName",
      expression =
          "java(entity.getLecturer() != null ? entity.getLecturer().getFullName() : \"Chưa phân công\")")
  ClassResponse toClassResponse(ClassEntity entity);

  List<ClassResponse> toClassResponseList(List<ClassEntity> entities);

  @Mapping(target = "classId", source = "classEntity.classId")
  @Mapping(target = "classCode", source = "classEntity.classCode")
  @Mapping(
      target = "courseCode",
      expression =
          "java(schedule.getClassEntity() != null && schedule.getClassEntity().getCourse() != null ? schedule.getClassEntity().getCourse().getCourseCode() : \"N/A\")")
  @Mapping(
      target = "courseName",
      expression =
          "java(schedule.getClassEntity() != null && schedule.getClassEntity().getCourse() != null ? schedule.getClassEntity().getCourse().getCourseName() : \"N/A\")")
  @Mapping(target = "sessionNo", source = "sessionNo")
  @Mapping(
      target = "lecturerName",
      expression =
          "java(schedule.getLecturer() != null ? schedule.getLecturer().getFullName() : \"N/A\")")
  @Mapping(
      target = "semester",
      expression =
          "java(schedule.getClassEntity() != null ? schedule.getClassEntity().getSemester() : null)")
  ScheduleResponse toScheduleResponse(Schedule schedule);

  List<ScheduleResponse> toScheduleResponseList(List<Schedule> schedules);

  @Mapping(target = "classId", source = "classEntity.classId")
  @Mapping(
      target = "classCode",
      expression = "java(g.getClassEntity() != null ? g.getClassEntity().getClassCode() : \"N/A\")")
  @Mapping(
      target = "courseName",
      expression =
          "java(g.getClassEntity() != null && g.getClassEntity().getCourse() != null ? g.getClassEntity().getCourse().getCourseName() : \"N/A\")")
  GradeResponse toGradeResponse(Grade g);

  List<GradeResponse> toGradeResponseList(List<Grade> grades);

  @Mapping(
      target = "studentId",
      expression = "java(r.getStudent() != null ? r.getStudent().getUserId() : null)")
  @Mapping(
      target = "studentName",
      expression = "java(r.getStudent() != null ? r.getStudent().getFullName() : \"N/A\")")
  @Mapping(
      target = "studentRollNumber",
      expression = "java(r.getStudent() != null ? r.getStudent().getRollNumber() : \"N/A\")")
  RequestResponse toRequestResponse(RequestEntity r);

  List<RequestResponse> toRequestResponseList(List<RequestEntity> requests);

  @Mapping(
      target = "leaderName",
      expression = "java(c.getLeader() != null ? c.getLeader().getFullName() : \"N/A\")")
  ClubResponse toClubResponse(Club c);

  List<ClubResponse> toClubResponseList(List<Club> clubs);
}
