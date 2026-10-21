package vn.edu.fpt.fschool.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
import vn.edu.fpt.fschool.model.RequestEntity;
import vn.edu.fpt.fschool.model.Schedule;
import vn.edu.fpt.fschool.repository.RequestRepository;
import vn.edu.fpt.fschool.repository.ScheduleRepository;

@ExtendWith(MockitoExtension.class)
class AcademicLifecycleSchedulerTest {

  @Mock private RequestRepository requestRepository;
  @Mock private ScheduleRepository scheduleRepository;
  @Mock private DistributedLockService distributedLockService;
  @Mock private TransactionTemplate transactionTemplate;

  @InjectMocks private AcademicLifecycleScheduler scheduler;

  @Test
  @DisplayName("Quét và hủy tự động các đơn PENDING quá hạn 30 ngày, cập nhật lịch học quá hạn thành COMPLETED")
  void processExpiredRequestsAndSchedules_shouldUpdateStatuses_andSave() {
    doAnswer(
            invocation -> {
              Runnable task = invocation.getArgument(2);
              task.run();
              return null;
            })
        .when(distributedLockService)
        .executeWithLock(any(), any(), any());

    doAnswer(
            invocation -> {
              Consumer<TransactionStatus> action = invocation.getArgument(0);
              action.accept(mock(TransactionStatus.class));
              return null;
            })
        .when(transactionTemplate)
        .executeWithoutResult(any());

    RequestEntity request = new RequestEntity();
    request.setRequestId(1L);
    request.setStatus("PENDING");
    request.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).minusDays(35));

    Schedule schedule = new Schedule();
    schedule.setScheduleId(10L);
    schedule.setStatus("UPCOMING");
    schedule.setDate(LocalDate.now(ZoneOffset.UTC).minusDays(1));

    when(requestRepository.findByStatusAndCreatedAtBefore(eq("PENDING"), any(OffsetDateTime.class)))
        .thenReturn(List.of(request));
    when(scheduleRepository.findByDateBeforeAndStatus(any(LocalDate.class), eq("UPCOMING")))
        .thenReturn(List.of(schedule));

    scheduler.processExpiredRequestsAndSchedules();

    assertEquals("CANCELLED", request.getStatus());
    assertEquals("Đơn bị hủy tự động do quá hạn xử lý 30 ngày.", request.getResponseNote());

    assertEquals("COMPLETED", schedule.getStatus());

    verify(distributedLockService, times(1)).executeWithLock(any(), any(), any());
    verify(transactionTemplate, times(1)).executeWithoutResult(any());
    verify(requestRepository, times(1)).saveAll(List.of(request));
    verify(scheduleRepository, times(1)).saveAll(List.of(schedule));
  }

  @Test
  @DisplayName("Không gọi saveAll khi không có đơn quá hạn hoặc lịch học quá khứ")
  void processExpiredRequestsAndSchedules_shouldDoNothing_whenNoData() {
    doAnswer(
            invocation -> {
              Runnable task = invocation.getArgument(2);
              task.run();
              return null;
            })
        .when(distributedLockService)
        .executeWithLock(any(), any(), any());

    doAnswer(
            invocation -> {
              Consumer<TransactionStatus> action = invocation.getArgument(0);
              action.accept(mock(TransactionStatus.class));
              return null;
            })
        .when(transactionTemplate)
        .executeWithoutResult(any());

    when(requestRepository.findByStatusAndCreatedAtBefore(eq("PENDING"), any(OffsetDateTime.class)))
        .thenReturn(Collections.emptyList());
    when(scheduleRepository.findByDateBeforeAndStatus(any(LocalDate.class), eq("UPCOMING")))
        .thenReturn(Collections.emptyList());

    scheduler.processExpiredRequestsAndSchedules();

    verify(distributedLockService, times(1)).executeWithLock(any(), any(), any());
    verify(transactionTemplate, times(1)).executeWithoutResult(any());
    verify(requestRepository, never()).saveAll(any());
    verify(scheduleRepository, never()).saveAll(any());
  }
}
