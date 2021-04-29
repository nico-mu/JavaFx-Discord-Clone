package de.uniks.stp.network;

public class HttpResponseHelper {

    public static boolean isSuccess(int code) {
        return code == 200;
    }

}
