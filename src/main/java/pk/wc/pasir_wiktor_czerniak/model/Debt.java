package pk.wc.pasir_wiktor_czerniak.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Debt {

    private LocalDateTime createdAt;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;

    private String title;

    private boolean paidByDebtor = false;

    private boolean confirmedByCreditor = false;

    @ManyToOne
    private User debtor;

    @ManyToOne
    private User creditor;

    @ManyToOne
    private Group group;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}