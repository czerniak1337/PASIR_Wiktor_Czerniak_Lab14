package pk.wc.pasir_wiktor_czerniak.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import pk.wc.pasir_wiktor_czerniak.model.User;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {

    private static final SecretKey KEY =
            Keys.hmacShaKeyFor(
                    "wiktorczerniakpasirlab10supersecretkey123"
                            .getBytes()
            );

    private static final Duration TOKEN_VALIDITY =
            Duration.ofHours(1);

    private static Date toDate(Instant instant) {
        return Date.from(instant);
    }

    public String generateToken(User user) {

        Instant now = Instant.now();

        Date issuedAt = toDate(now);
        Date expiration = toDate(
                now.plus(TOKEN_VALIDITY)
        );

        return Jwts.builder()
                .claim("id", user.getId())
                .claim("email", user.getEmail())
                .subject(user.getEmail())
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(KEY)
                .compact();
    }

    public String extractUsername(String token) {

        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token) {

        try {

            Jwts.parser()
                    .verifyWith(KEY)
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (Exception e) {

            return false;
        }
    }
}