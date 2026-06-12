package pk.wc.pasir_wiktor_czerniak.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pk.wc.pasir_wiktor_czerniak.model.Group;
import pk.wc.pasir_wiktor_czerniak.model.User;

import java.util.List;

public interface GroupRepository
        extends JpaRepository<Group, Long> {

    List<Group> findByMemberships_User(
            User user
    );
}