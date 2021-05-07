package de.uniks.stp.network;

import de.uniks.stp.Constants;
import kong.unirest.Config;
import kong.unirest.HttpMethod;
import kong.unirest.HttpRequest;
import kong.unirest.Interceptor;

public class HttpRequestInterceptor implements Interceptor {


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
        String loginPath = Constants.USERS_PATH + Constants.LOGIN_PATH;

        // if requested path is not login or register, add userKey header with value from model
        if(!(requestMethod == HttpMethod.POST &&
            (requestPath.equals(loginPath) || requestPath.equals(Constants.USERS_PATH))))
        {
            request.header(Constants.USER_KEY_HEADER_NAME, UserKeyProvider.getUserKey());
        }

        Interceptor.super.onRequest(request, config);
    }
}
