package pk.wc.pasir_wiktor_czerniak.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {

    @NotBlank
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
}