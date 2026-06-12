package pk.wc.pasir_wiktor_czerniak.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_groups")
@Data
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    private User owner;

    private LocalDateTime createdAt;

    @Transient
    public Long getOwnerId() {
        return owner != null ? owner.getId() : null;
    }

    @OneToMany(mappedBy = "group")
    private java.util.List<Membership> memberships;
}