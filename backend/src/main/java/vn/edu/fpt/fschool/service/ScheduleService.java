package vn.edu.fpt.fschool.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.fschool.dto.ScheduleDtos.ScheduleResponse;
import vn.edu.fpt.fschool.mapper.AcademicMapper;
import vn.edu.fpt.fschool.model.Schedule;
import vn.edu.fpt.fschool.repository.ScheduleRepository;

@Service
@RequiredArgsConstructor
public class ScheduleService {

  private final ScheduleRepository scheduleRepository;
  private final AcademicMapper academicMapper;

  @Transactional(readOnly = true)
  public List<ScheduleResponse> getSchedules(LocalDate date) {
    List<Schedule> schedules =
        (date != null) ? scheduleRepository.findByDate(date) : scheduleRepository.findAll();

    return academicMapper.toScheduleResponseList(schedules);
  }

  @Transactional(readOnly = true)
  public ScheduleResponse getScheduleDetail(Long id) {
    Schedule schedule =
        scheduleRepository
            .findById(id)
            .orElseThrow(
                () -> new IllegalArgumentException("Không tìm thấy buổi học với ID: " + id));
    return academicMapper.toScheduleResponse(schedule);
  }
}
