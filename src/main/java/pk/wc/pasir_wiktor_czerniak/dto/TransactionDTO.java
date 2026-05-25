package pk.wc.pasir_wiktor_czerniak.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import pk.wc.pasir_wiktor_czerniak.model.TransactionType;

@Getter
@Setter
public class TransactionDTO {

    @NotNull(message = "Kwota nie może być pusta")
    @Min(value = 1,
            message = "Kwota musi być większa od 0")
    private Double amount;

    @NotNull(message = "Typ transakcji jest wymagany")
    private TransactionType type;

    @Size(max = 50,
            message = "Tagi mogą mieć maksymalnie 50 znaków")
    private String tags;

    @Size(max = 255,
            message = "Notatka może mieć maksymalnie 255 znaków")
    private String notes;
}