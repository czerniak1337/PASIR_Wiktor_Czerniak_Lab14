package pk.wc.pasir_wiktor_czerniak.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import pk.wc.pasir_wiktor_czerniak.dto.GroupNotificationDto;
import pk.wc.pasir_wiktor_czerniak.websocket.WebSocketSessionManager;

@Service
@RequiredArgsConstructor
public class GroupNotificationService {

    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;


    public void sendToUser(
            String email,
            GroupNotificationDto dto
    ) {

        System.out.println("SENDING TO " + email);

        WebSocketSession session =
                sessionManager.getSession(email);

        System.out.println(
                "SESSION FOUND = " + (session != null)
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
            System.out.println("MESSAGE SENT");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}