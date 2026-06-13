package pk.wc.pasir_wiktor_czerniak.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
public class Membership {

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Group group;

    @Transient
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    @Transient
    public String getEmail() {
        return user != null ? user.getEmail() : null;
    }

    @Transient
    public Long getGroupId() {
        return group != null ? group.getId() : null;
    }

    @Transient
    public String getUserEmail() {
        return user != null ? user.getEmail() : null;
    }
}