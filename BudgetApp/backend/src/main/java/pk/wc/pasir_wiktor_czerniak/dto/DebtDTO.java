package pk.wc.pasir_wiktor_czerniak.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class DebtDTO {

    @NotNull
    private Long debtorId;

    @NotNull
    private Long creditorId;

    @NotNull
    private Long groupId;

    @NotNull
    @Positive
    private Double amount;

    @NotBlank
    private String title;
}