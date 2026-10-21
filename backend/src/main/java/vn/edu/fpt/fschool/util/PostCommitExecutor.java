package vn.edu.fpt.fschool.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@Slf4j
public class PostCommitExecutor {

  public void execute(Runnable task) {
    if (task == null) {
      return;
    }
    if (TransactionSynchronizationManager.isActualTransactionActive()) {
      TransactionSynchronizationManager.registerSynchronization(
          new TransactionSynchronization() {
            @Override
            public void afterCommit() {
              try {
                task.run();
              } catch (Exception ex) {
                log.error(
                    "Lỗi khi thực thi tác vụ sau khi commit transaction: {}",
                    ex.getMessage(),
                    ex);
              }
            }
          });
    } else {
      try {
        task.run();
      } catch (Exception ex) {
        log.error("Lỗi khi thực thi tác vụ ngoài transaction: {}", ex.getMessage(), ex);
      }
    }
  }
}
