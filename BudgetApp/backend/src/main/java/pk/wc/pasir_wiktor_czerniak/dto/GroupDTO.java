package pk.wc.pasir_wiktor_czerniak.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;


@Data
public class GroupDTO {

    @NotBlank
    private String name;
}