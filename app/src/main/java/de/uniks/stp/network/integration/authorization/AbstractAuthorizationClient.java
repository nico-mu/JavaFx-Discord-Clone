package de.uniks.stp.network.integration.authorization;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import de.uniks.stp.AccordApp;
import de.uniks.stp.network.integration.IntegrationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractAuthorizationClient implements HttpHandler {

    protected static final Logger log = LoggerFactory.getLogger(AbstractAuthorizationClient.class);

    private Timer serverTimeout;
    protected final AccordApp app;
    protected ExecutorService executorService;
    protected HttpServer server;
    protected String serverPath;
    protected AuthorizationCallback authorizationCallback;

    protected boolean serverStopped;

    public AbstractAuthorizationClient(AccordApp app) {
        this.app = app;
        this.serverPath = IntegrationConstants.TEMP_SERVER_INTEGRATION_PATH;
        executorService = Executors.newCachedThreadPool();
     }

    public void authorize(AuthorizationCallback authorizationCallback) {
        this.authorizationCallback = authorizationCallback;
        try {
            //needed for testing purposes
            if(Objects.isNull(server)) {
                server = HttpServer.create(new InetSocketAddress(IntegrationConstants.TEMP_SERVER_HOST, IntegrationConstants.TEMP_SERVER_PORT), 0);
                server.createContext(serverPath, this);
                server.setExecutor(executorService);
                server.start();
                log.debug("Server started on port: " + server.getAddress().getPort());
            }
        } catch (IOException e) {
            e.printStackTrace();
            authorizationCallback.onFailure(e.getMessage());
        }
        stopServerAfterTimeout();
    }

    protected String getRedirectUri() {
        return "http://" +
            IntegrationConstants.TEMP_SERVER_HOST + ":" + IntegrationConstants.TEMP_SERVER_PORT + serverPath;
    }

    protected static Map<String, String> splitQuery(String queryString) {
        Map<String, String> queryPairs = new LinkedHashMap<>();
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            int index = pair.indexOf("=");
            queryPairs.put(URLDecoder.decode(pair.substring(0, index), StandardCharsets.UTF_8),
                URLDecoder.decode(pair.substring(index + 1), StandardCharsets.UTF_8));
        }
        return queryPairs;
    }

    public void noTimeout() {
        serverTimeout.cancel();
        serverTimeout.purge();
    }


    public void stopServer() {
        noTimeout();
        serverStopped = true;
        server.stop(0);
        executorService.shutdown();
    }

    public boolean isServerStopped() {
        return serverStopped;
    }

    protected void stopServerAfterTimeout() {
        serverTimeout = new Timer();
        serverTimeout.schedule(new TimerTask() {
            @Override
            public void run() {
               stopServer();
            }
        }, IntegrationConstants.AUTHORIZATION_TIMEOUT);
    }
}
