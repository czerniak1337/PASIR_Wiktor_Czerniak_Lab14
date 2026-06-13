package pk.wc.pasir_wiktor_czerniak.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

@Data
public class GroupTransactionDTO {

    @NotNull
    private Long groupId;

    @NotNull
    @Positive
    private Double amount;

    @NotBlank
    private String title;

    @NotBlank
    private String type;

    private List<Long> selectedUserIds;
}