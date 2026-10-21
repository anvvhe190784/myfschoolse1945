package vn.edu.fpt.fschool.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import vn.edu.fpt.fschool.model.RequestEntity;
import vn.edu.fpt.fschool.model.Schedule;
import vn.edu.fpt.fschool.repository.RequestRepository;
import vn.edu.fpt.fschool.repository.ScheduleRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class AcademicLifecycleScheduler {

  private final RequestRepository requestRepository;
  private final ScheduleRepository scheduleRepository;
  private final DistributedLockService distributedLockService;
  private final TransactionTemplate transactionTemplate;

  @Scheduled(cron = "0 0 2 * * ?")
  public void processExpiredRequestsAndSchedules() {
    if (distributedLockService != null) {
      distributedLockService.executeWithLock(
          "AcademicLifecycleScheduler",
          Duration.ofMinutes(10),
          this::executeLifecycleCleanup);
    } else {
      executeLifecycleCleanup();
    }
  }

  public void executeLifecycleCleanup() {
    log.info("[SCHEDULED] Bắt đầu quét đơn từ quá hạn và lịch học quá khứ...");

    Runnable cleanupAction =
        () -> {
          // 1. Quét đơn PENDING tạo hơn 30 ngày trước
          OffsetDateTime cutoff = OffsetDateTime.now(ZoneOffset.UTC).minusDays(30);
          List<RequestEntity> expiredRequests =
              requestRepository.findByStatusAndCreatedAtBefore("PENDING", cutoff);
          if (!expiredRequests.isEmpty()) {
            for (RequestEntity req : expiredRequests) {
              req.setStatus("CANCELLED");
              req.setResponseNote("Đơn bị hủy tự động do quá hạn xử lý 30 ngày.");
            }
            requestRepository.saveAll(expiredRequests);
            log.info("Đã tự động hủy {} đơn quá hạn xử lý 30 ngày.", expiredRequests.size());
          }

          // 2. Quét lịch học có ngày trước ngày hôm nay và đang ở trạng thái UPCOMING -> COMPLETED
          LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
          List<Schedule> pastSchedules =
              scheduleRepository.findByDateBeforeAndStatus(today, "UPCOMING");
          if (!pastSchedules.isEmpty()) {
            for (Schedule schedule : pastSchedules) {
              schedule.setStatus("COMPLETED");
            }
            scheduleRepository.saveAll(pastSchedules);
            log.info(
                "Đã cập nhật {} lịch học quá hạn sang trạng thái COMPLETED.", pastSchedules.size());
          }
        };

    if (transactionTemplate != null) {
      transactionTemplate.executeWithoutResult(status -> cleanupAction.run());
    } else {
      cleanupAction.run();
    }
  }
}
