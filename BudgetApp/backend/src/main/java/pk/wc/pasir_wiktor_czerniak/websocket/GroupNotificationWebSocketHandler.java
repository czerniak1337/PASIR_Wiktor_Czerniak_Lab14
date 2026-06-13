package pk.wc.pasir_wiktor_czerniak.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import pk.wc.pasir_wiktor_czerniak.security.JwtUtil;

@Component
@RequiredArgsConstructor
public class GroupNotificationWebSocketHandler
        extends TextWebSocketHandler {

    private final JwtUtil jwtUtil;
    private final WebSocketSessionManager sessionManager;

    @Override
    public void afterConnectionEstablished(
            WebSocketSession session
    ) {

        try {

            System.out.println("WS CONNECT ATTEMPT");
            System.out.println(session.getUri());

            String query =
                    session.getUri().getQuery();

            System.out.println("QUERY = " + query);

            String token =
                    query.substring(6);

            System.out.println("TOKEN RAW = " + token);

            boolean valid =
                    jwtUtil.validateToken(token);

            System.out.println("TOKEN VALID = " + valid);

            if (!valid) {
                return;
            }

            String email =
                    jwtUtil.extractUsername(token);

            System.out.println("EMAIL = " + email);

            sessionManager.register(
                    email,
                    session
            );

            System.out.println("SESSION REGISTERED");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus status
    ) {

        String query =
                session.getUri().getQuery();

        if (query == null ||
                !query.startsWith("token=")) {
            return;
        }

        String token =
                query.substring(6);

        System.out.println("TOKEN VALID = " +
                jwtUtil.validateToken(token));

        String email =
                jwtUtil.extractUsername(token);

        System.out.println("EMAIL = " + email);

        boolean valid =
                jwtUtil.validateToken(token);

        System.out.println("TOKEN VALID = " + valid);

        if (!valid) {
            return;
        }

        sessionManager.unregister(
                jwtUtil.extractUsername(token)
        );

        System.out.println("WS USER = " + email);

        sessionManager.register(email, session);

        System.out.println("SESSION REGISTERED");
    }


}