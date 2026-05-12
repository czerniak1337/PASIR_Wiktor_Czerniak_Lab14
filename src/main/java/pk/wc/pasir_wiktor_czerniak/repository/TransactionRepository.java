package pk.wc.PASIR_Wiktor_Czerniak.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pk.wc.PASIR_Wiktor_Czerniak.model.Transaction;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {
}