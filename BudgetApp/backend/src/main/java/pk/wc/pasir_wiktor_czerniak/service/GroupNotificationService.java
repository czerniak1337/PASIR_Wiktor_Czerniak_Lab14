package pk.wc.pasir_wiktor_czerniak.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import pk.wc.pasir_wiktor_czerniak.dto.GroupNotificationDto;
import pk.wc.pasir_wiktor_czerniak.websocket.WebSocketSessionManager;

@Service
@RequiredArgsConstructor
public class GroupNotificationService {

    private static final Logger log =
            LoggerFactory.getLogger(GroupNotificationService.class);

    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    public void sendToUser(
            String email,
            GroupNotificationDto dto
    ) {

        log.info("SENDING TO {}", email);

        WebSocketSession session =
                sessionManager.getSession(email);

        log.info(
                "SESSION FOUND = {}",
                session != null
        );

        try {

            if (session == null || !session.isOpen()) {
                return;
            }

            session.sendMessage(
                    new TextMessage(
                            objectMapper.writeValueAsString(dto)
                    )
            );

            log.info("MESSAGE SENT");

        } catch (Exception e) {
            log.error("Failed to send WebSocket message", e);
        }
    }
}