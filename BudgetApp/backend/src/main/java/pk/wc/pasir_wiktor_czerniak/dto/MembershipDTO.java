package pk.wc.pasir_wiktor_czerniak.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class MembershipDTO {

    @NotNull
    private Long groupId;

    @NotBlank
    @Email
    private String userEmail;
}