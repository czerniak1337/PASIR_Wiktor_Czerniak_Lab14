package pk.wc.pasir_wiktor_czerniak.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;
import pk.wc.pasir_wiktor_czerniak.websocket.GroupNotificationWebSocketHandler;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig
        implements WebSocketConfigurer {

    private static final Logger log =
            LoggerFactory.getLogger(WebSocketConfig.class);

    private final GroupNotificationWebSocketHandler handler;

    @Override
    public void registerWebSocketHandlers(
            WebSocketHandlerRegistry registry
    ) {

        log.info("REGISTERING WS HANDLER");

        registry.addHandler(
                        handler,
                        "/ws/group-notifications"
                )
                .setAllowedOriginPatterns("*");
    }
}