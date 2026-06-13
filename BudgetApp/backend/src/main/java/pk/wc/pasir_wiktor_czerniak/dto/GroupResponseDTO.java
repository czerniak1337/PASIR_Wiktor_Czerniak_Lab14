package pk.wc.pasir_wiktor_czerniak.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GroupResponseDTO {

    private Long id;

    private String name;

    private Long ownerId;
}