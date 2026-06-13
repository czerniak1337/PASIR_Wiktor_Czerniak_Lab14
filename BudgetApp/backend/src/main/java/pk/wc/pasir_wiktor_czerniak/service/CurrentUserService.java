package pk.wc.pasir_wiktor_czerniak.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pk.wc.pasir_wiktor_czerniak.model.User;
import pk.wc.pasir_wiktor_czerniak.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getCurrentUser() {

        Authentication auth =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        return userRepository
                .findByEmail(auth.getName())
                .orElseThrow();
    }
}