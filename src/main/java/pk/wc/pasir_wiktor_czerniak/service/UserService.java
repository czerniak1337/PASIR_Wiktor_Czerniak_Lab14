package pk.wc.pasir_wiktor_czerniak.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pk.wc.pasir_wiktor_czerniak.dto.LoginDto;
import pk.wc.pasir_wiktor_czerniak.dto.UserDto;
import pk.wc.pasir_wiktor_czerniak.exception.UserAlreadyExistsException;
import pk.wc.pasir_wiktor_czerniak.model.User;
import pk.wc.pasir_wiktor_czerniak.repository.UserRepository;
import pk.wc.pasir_wiktor_czerniak.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService
        implements UserDetailsService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    public User register(UserDto dto) {

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {

            throw new UserAlreadyExistsException(
                    "Uzytkownik już istnieje"
            );
        }

        User user = new User();

        user.setUsername(dto.getUsername());

        user.setEmail(dto.getEmail());

        user.setPassword(
                passwordEncoder.encode(dto.getPassword())
        );

        return userRepository.save(user);
    }

    public String login(LoginDto dto) {

        User user = userRepository
                .findByEmail(dto.getEmail())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Nieprawidlowy email lub haslo"
                        )
                );

        boolean matches =
                passwordEncoder.matches(
                        dto.getPassword(),
                        user.getPassword()
                );

        if (!matches) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Nieprawidlowy email lub haslo"
            );
        }

        return jwtUtil.generateToken(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Nie znaleziono uzytkownika"
                        )
                );

        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }
}