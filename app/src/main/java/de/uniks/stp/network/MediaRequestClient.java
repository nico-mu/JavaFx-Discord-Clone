package de.uniks.stp.network;

import de.uniks.stp.component.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MediaRequestClient {
    private static final int POOL_SIZE = 4;
    private static final Logger log = LoggerFactory.getLogger(WebSocketClient.class);
    private final ExecutorService executor;

    public MediaRequestClient() {
        executor = Executors.newFixedThreadPool(POOL_SIZE);
    }

    public void addImage(String url, ChatMessage messageNode) {
        log.debug("Get Image from: " + url);
        executor.submit(() -> messageNode.addImage(url));
    }

    public void stop() {
        executor.shutdown();
    }
}

