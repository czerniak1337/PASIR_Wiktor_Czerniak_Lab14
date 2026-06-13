package pk.wc.pasir_wiktor_czerniak.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionManager {

    private final Map<String, WebSocketSession> sessions =
            new ConcurrentHashMap<>();

    public void register(
            String email,
            WebSocketSession session
    ) {
        sessions.put(email, session);
    }

    public void unregister(
            String email
    ) {
        sessions.remove(email);
    }

    public WebSocketSession getSession(
            String email
    ) {
        return sessions.get(email);
    }
}