package pk.wc.pasir_wiktor_czerniak.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pk.wc.pasir_wiktor_czerniak.model.Membership;

import java.util.List;

public interface MembershipRepository
        extends JpaRepository<Membership, Long> {

    List<Membership> findByGroup_Id(
            Long groupId
    );

    boolean existsByGroup_IdAndUser_Id(
            Long groupId,
            Long userId
    );

    void deleteByGroup_Id(
            Long groupId
    );

    boolean existsByGroup_IdAndUser_Email(
            Long groupId,
            String email
    );
}