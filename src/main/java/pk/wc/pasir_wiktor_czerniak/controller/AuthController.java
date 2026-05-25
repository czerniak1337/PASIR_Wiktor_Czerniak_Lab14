package pk.wc.pasir_wiktor_czerniak.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pk.wc.pasir_wiktor_czerniak.dto.LoginDto;
import pk.wc.pasir_wiktor_czerniak.dto.UserDto;
import pk.wc.pasir_wiktor_czerniak.model.User;
import pk.wc.pasir_wiktor_czerniak.service.UserService;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(
            @Valid @RequestBody UserDto dto) {

        return ResponseEntity.ok(
                userService.register(dto)
        );
    }

    @PostMapping("/login")
    public Map<String, String> login(
            @RequestBody LoginDto dto
    ) {

        String token = userService.login(dto);

        return Map.of(
                "token",
                token
        );
    }
}