package de.uniks.stp.network.rest;

import de.uniks.stp.Editor;
import kong.unirest.Config;
import kong.unirest.HttpMethod;
import kong.unirest.HttpRequest;
import kong.unirest.Interceptor;

import javax.inject.Inject;
import javax.inject.Named;

import static de.uniks.stp.Constants.*;

public class HttpRequestInterceptor implements Interceptor {

    private final String userKey;

    @Inject
    public HttpRequestInterceptor(@Named("userKey") String userKey) {
        this.userKey = userKey;
    }
    /**
     * Is called before a http request is sent to the server, adds
     * the userKey to the header if we need to. (For all routes except the login and register route)
     *
     * @param request object representing the http request
     * @param config object representing the unirest config
     *
     */
    @Override
    public void onRequest(HttpRequest<?> request, Config config) {
        HttpMethod requestMethod = request.getHttpMethod();
        String requestUrl = request.getUrl();

        //get requested server path by stripping the base url at the beginning
        String baseUrl = config.getDefaultBaseUrl();
        String requestPath = requestUrl.substring(baseUrl.length());
        String loginPath = REST_USERS_PATH + REST_LOGIN_PATH;

        // if requested path is not login or register, add userKey header with value from model
        if(!(requestMethod == HttpMethod.POST &&
            (requestPath.equals(loginPath) || requestPath.equals(REST_USERS_PATH))))
        {
            request.header(USER_KEY_HEADER_NAME, userKey);
        }

        Interceptor.super.onRequest(request, config);
    }
}
