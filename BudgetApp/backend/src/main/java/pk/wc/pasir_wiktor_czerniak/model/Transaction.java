package pk.wc.pasir_wiktor_czerniak.model;

import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private String tags;

    private String notes;

    private LocalDateTime timestamp;

    public Transaction(Double amount,
                       TransactionType type,
                       String tags,
                       String notes) {

        this.amount = amount;
        this.type = type;
        this.tags = tags;
        this.notes = notes;
        this.timestamp = LocalDateTime.now();
    }
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}