package de.uniks.stp.network;

import de.uniks.stp.Constants;
import de.uniks.stp.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonObject;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;

public class WebSocketClient extends Endpoint {
    private static final Logger log = LoggerFactory.getLogger(WebSocketClient.class);

    private final Timer noopTimer;
    private Session session;
    private WSCallback callback;

    /**
     * Intitilization chores
     *
     * @param endpoint URI with connection adress
     * @param callback method to call when message is received
     */
    public WebSocketClient(String endpoint, WSCallback callback) {
        this.noopTimer = new Timer();

        try {
            ClientEndpointConfig clientConfig = ClientEndpointConfig.Builder.create()
                .configurator(new CustomWebSocketConfigurator(UserKeyProvider.getUserKey()))
                .build();

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();

            final String path = endpoint.startsWith("/") ?
                Constants.WEBSOCKET_BASE_URL + endpoint : Constants.WEBSOCKET_BASE_URL + "/" + endpoint;

            container.connectToServer(
                this,
                clientConfig,
                URI.create(path)
            );
            this.callback = callback;
        } catch (Exception e) {
            log.error("Error during establishing websocket connection:", e);
        }
    }

    /**
     * Is called automatically, initializes and sets NOOP-timer
     *
     * @param session passed automatically
     * @param config  passed automatically
     */
    @Override
    public void onOpen(Session session, EndpointConfig config) {
        // Store session
        this.session = session;
        // add MessageHandler
        this.session.addMessageHandler(String.class, this::onMessage);

        this.noopTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Send NOOP Message
                try {
                    session.getBasicRemote().sendText("noop");
                } catch (IOException e) {
                    log.error("Failed to send noop", e);
                }
            }
        }, 0, 1000 * 30);  // 30s works, 1min is to much
    }

    /**
     * Is called automatically; cleanup and disables NOOP-timer
     *
     * @param session     passed automatically
     * @param closeReason passed automatically
     */
    @Override
    public void onClose(Session session, CloseReason closeReason) {
        log.debug("onClose");
        super.onClose(session, closeReason);
        // cancel timer
        this.noopTimer.cancel();
        // set session null
        this.session = null;
    }

    /**
     * Is called when message is received
     *
     * @param message Json String that is received
     */
    private void onMessage(String message) {
        // Process Message
        JsonObject jsonMessage = JsonUtil.parse(message);
        // Use callback to handle it
        this.callback.handleMessage(jsonMessage);
    }

    /**
     * Sends message if possible.
     *
     * @param message JsonObject as a string
     * @throws IOException session could cause problems
     */
    public void sendMessage(String message) throws IOException {
        // check if session is still open
        if (this.session != null && this.session.isOpen()) {
            // send message
            this.session.getBasicRemote().sendText(message);
            this.session.getBasicRemote().flushBatch();
        }
    }

    /**
     * Should be called when WebSocket is not used anymore; cancels Timer and closes session
     */
    public void stop() {
        log.debug("stop");
        // cancel timer
        this.noopTimer.cancel();
        // close session
        if (this.session != null) {
            try {
                this.session.close();
            } catch (IOException e) {
                log.error("Failed to close the session", e);
            }
            this.session = null;
        }
    }
}
