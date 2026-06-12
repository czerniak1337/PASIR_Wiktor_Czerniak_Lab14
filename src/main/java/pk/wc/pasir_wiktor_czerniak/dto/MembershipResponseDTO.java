package pk.wc.pasir_wiktor_czerniak.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MembershipResponseDTO {

    private Long id;

    private Long userId;

    private String email;
}