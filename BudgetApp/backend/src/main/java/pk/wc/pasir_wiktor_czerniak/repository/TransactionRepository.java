package pk.wc.pasir_wiktor_czerniak.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pk.wc.pasir_wiktor_czerniak.model.Transaction;
import pk.wc.pasir_wiktor_czerniak.model.User;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUser(User user);

    List<Transaction> findByUserAndTimestampAfter(
            User user,
            LocalDateTime timestamp
    );
}