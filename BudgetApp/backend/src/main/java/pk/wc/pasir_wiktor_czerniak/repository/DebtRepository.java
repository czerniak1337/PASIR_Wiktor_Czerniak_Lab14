package pk.wc.pasir_wiktor_czerniak.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pk.wc.pasir_wiktor_czerniak.model.Debt;

import java.util.List;

public interface DebtRepository
        extends JpaRepository<Debt, Long> {

    List<Debt> findByGroupId(
            Long groupId
    );

    void deleteByGroupId(
            Long groupId
    );
}