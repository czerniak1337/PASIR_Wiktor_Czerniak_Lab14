package pk.wc.pasir_wiktor_czerniak.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
public class Debt {

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
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
}