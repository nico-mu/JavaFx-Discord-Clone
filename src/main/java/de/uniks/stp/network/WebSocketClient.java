//FIXME in CustomWebSocketConfigurator, might cause problems

package de.uniks.stp.network;

import de.uniks.stp.Editor;
import de.uniks.stp.util.JsonUtil;

import javax.json.JsonObject;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;

public class WebSocketClient extends Endpoint {

    private Editor editor;

    private Session session;
    private Timer noopTimer;

    private WSCallback callback;

    public WebSocketClient(Editor editor, URI endpoint, WSCallback callback) {
        this.editor = editor;
        this.noopTimer = new Timer();

        try {
            ClientEndpointConfig clientConfig = ClientEndpointConfig.Builder.create()
                    .configurator(new CustomWebSocketConfigurator(this.editor.getUserkey()))
                    .build();

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(
                    this,
                    clientConfig,
                    endpoint
            );
            this.callback = callback;
        } catch (Exception e) {
            System.err.println("Error during establishing websocket connection:");
            e.printStackTrace();
        }
    }

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
                    e.printStackTrace();
                }
            }
        }, 0, 1000 * 30);  // 1000*30 is just a guess
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        super.onClose(session, closeReason);
        // cancel timer
        this.noopTimer.cancel();
        // set session null
        this.session = null;
    }

    private void onMessage(String message) {
        // Process Message
        JsonObject jsonMessage = JsonUtil.parse(message);
        // Use callback to handle it
        this.callback.handleMessage(jsonMessage);
    }

    public void sendMessage(String message) throws IOException {
        // check if session is still open
        if(this.session != null && this.session.isOpen()) {
            // send message
            this.session.getBasicRemote().sendText(message);
            this.session.getBasicRemote().flushBatch();
        }
    }

    public void stop() throws IOException {
        // cancel timer
        this.noopTimer.cancel();
        // close session
        if(this.session != null) {
            this.session.close();
            this.session = null;
        }
    }
}
