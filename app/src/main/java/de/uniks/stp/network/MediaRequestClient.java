package de.uniks.stp.network;

import de.uniks.stp.component.ChatMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MediaRequestClient {
    private static final int POOL_SIZE = 4;
    private final ExecutorService executor;

    public MediaRequestClient() {
        executor = Executors.newFixedThreadPool(POOL_SIZE);
    }

    public void addImage(String url, ChatMessage messageNode) {
        executor.submit(() -> messageNode.addImage(url));
    }

    public void addVideo(String url, String type, ChatMessage messageNode) {executor.submit(() -> messageNode.addVideo(url, type));}

    public void stop() {
        executor.shutdown();
    }

    public void addYouTubeVideo(String url, ChatMessage messageNode) {
        executor.submit(() -> messageNode.addYouTubeVideo(url));
    }

    public void loadContent(String content, ChatMessage messageNode) {
        executor.submit(() -> messageNode.loadContent(content));
    }
}

