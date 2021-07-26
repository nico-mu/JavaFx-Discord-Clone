package de.uniks.stp.network.integration.authorization;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import de.uniks.stp.AccordApp;
import de.uniks.stp.network.integration.IntegrationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;
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

    public AbstractAuthorizationClient(AccordApp app) {
        this.app = app;
        this.serverPath = IntegrationConstants.TEMP_SERVER_INTEGRATION_PATH;
        executorService = Executors.newCachedThreadPool();
     }

    public void authorize(AuthorizationCallback authorizationCallback) {
        this.authorizationCallback = authorizationCallback;
        try {
            server = HttpServer.create(new InetSocketAddress(IntegrationConstants.TEMP_SERVER_HOST, 8001), 0);
            server.createContext(serverPath, this);
            server.setExecutor(executorService);
            server.start();
            log.debug("Server started on port: " + server.getAddress().getPort());
        } catch (IOException e) {
            e.printStackTrace();
            authorizationCallback.onFailure(e.getMessage());
        }
    }

    protected void noTimeout() {
        serverTimeout.cancel();
        serverTimeout.purge();
    }


    protected void stopServer() {
        server.stop(0);
        executorService.shutdown();
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
