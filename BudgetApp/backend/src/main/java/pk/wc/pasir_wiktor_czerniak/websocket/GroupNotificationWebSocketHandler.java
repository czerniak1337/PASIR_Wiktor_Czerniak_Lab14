package pk.wc.pasir_wiktor_czerniak.websocket;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import pk.wc.pasir_wiktor_czerniak.security.JwtUtil;

@Component
@RequiredArgsConstructor
public class GroupNotificationWebSocketHandler
        extends TextWebSocketHandler {

    private static final Logger log =
            LoggerFactory.getLogger(
                    GroupNotificationWebSocketHandler.class
            );

    private final JwtUtil jwtUtil;
    private final WebSocketSessionManager sessionManager;

    @Override
    public void afterConnectionEstablished(
            WebSocketSession session
    ) {

        try {

            log.info("WS CONNECT ATTEMPT");

            if (session.getUri() == null) {
                return;
            }

            log.info("URI = {}", session.getUri());

            String query =
                    session.getUri().getQuery();

            if (query == null ||
                    !query.startsWith("token=")) {
                return;
            }

            log.info("QUERY = {}", query);

            String token =
                    query.substring(6);

            boolean valid =
                    jwtUtil.validateToken(token);

            log.info("TOKEN VALID = {}", valid);

            if (!valid) {
                return;
            }

            String email =
                    jwtUtil.extractUsername(token);

            log.info("EMAIL = {}", email);

            sessionManager.register(
                    email,
                    session
            );

            log.info("SESSION REGISTERED");

        } catch (Exception e) {

            log.error(
                    "Error during WebSocket connection",
                    e
            );

        }
    }

    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus status
    ) {

        if (session.getUri() == null) {
            return;
        }

        String query =
                session.getUri().getQuery();

        if (query == null ||
                !query.startsWith("token=")) {
            return;
        }

        String token =
                query.substring(6);

        boolean valid =
                jwtUtil.validateToken(token);

        log.info("TOKEN VALID = {}", valid);

        if (!valid) {
            return;
        }

        String email =
                jwtUtil.extractUsername(token);

        log.info("EMAIL = {}", email);

        sessionManager.unregister(email);

        log.info("WS USER DISCONNECTED = {}", email);
    }
}