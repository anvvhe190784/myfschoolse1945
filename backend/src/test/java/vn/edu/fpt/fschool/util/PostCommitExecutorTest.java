package vn.edu.fpt.fschool.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class PostCommitExecutorTest {

  private final PostCommitExecutor executor = new PostCommitExecutor();

  @AfterEach
  void tearDown() {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.clearSynchronization();
    }
    TransactionSynchronizationManager.setActualTransactionActive(false);
  }

  @Test
  @DisplayName("Thực thi ngay lập tức khi không có transaction đang hoạt động")
  void execute_shouldRunImmediately_whenNoTransactionActive() {
    AtomicBoolean executed = new AtomicBoolean(false);

    executor.execute(() -> executed.set(true));

    assertTrue(executed.get(), "Task phải được chạy ngay khi không có transaction");
  }

  @Test
  @DisplayName("Bỏ qua an toàn khi task là null")
  void execute_shouldHandleNullTask_gracefully() {
    assertDoesNotThrow(() -> executor.execute(null));
  }

  @Test
  @DisplayName("Nuốt lỗi và ghi log khi task ngoài transaction ném Exception")
  void execute_shouldHandleExceptionOutsideTransaction_gracefully() {
    assertDoesNotThrow(
        () ->
            executor.execute(
                () -> {
                  throw new RuntimeException("Lỗi mô phỏng ngoài tx");
                }));
  }

  @Test
  @DisplayName("Đăng ký afterCommit khi có transaction đang hoạt động và chạy sau commit")
  void execute_shouldRegisterSynchronization_andRunAfterCommit_whenTransactionActive() {
    TransactionSynchronizationManager.initSynchronization();
    TransactionSynchronizationManager.setActualTransactionActive(true);

    AtomicBoolean executed = new AtomicBoolean(false);

    executor.execute(() -> executed.set(true));

    assertFalse(executed.get(), "Task chưa được chạy trước khi commit");

    // Kích hoạt afterCommit của các synchronizations đã đăng ký
    for (TransactionSynchronization sync :
        TransactionSynchronizationManager.getSynchronizations()) {
      sync.afterCommit();
    }

    assertTrue(executed.get(), "Task phải được chạy trong afterCommit");
  }

  @Test
  @DisplayName("Nuốt lỗi và ghi log khi task trong afterCommit ném Exception")
  void execute_shouldHandleExceptionInAfterCommit_gracefully() {
    TransactionSynchronizationManager.initSynchronization();
    TransactionSynchronizationManager.setActualTransactionActive(true);

    executor.execute(
        () -> {
          throw new RuntimeException("Lỗi mô phỏng trong afterCommit");
        });

    assertDoesNotThrow(
        () -> {
          for (TransactionSynchronization sync :
              TransactionSynchronizationManager.getSynchronizations()) {
            sync.afterCommit();
          }
        });
  }
}
