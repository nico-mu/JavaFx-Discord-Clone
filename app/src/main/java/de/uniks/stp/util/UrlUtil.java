package de.uniks.stp.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;

public class UrlUtil {
    
    public static boolean isImageURL(String URL) {
        URL u = createURL(URL);
        if (Objects.nonNull(u)) {
            URLConnection connection;
            try {
                connection = u.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            System.out.println(connection.getHeaderField("Content-Type"));
            return connection.getHeaderField("Content-Type").startsWith("image/");
        }
        return false;
    }

    public static URL createURL(String URL) {
        try {
            URL u = new URL(URL); // this would check for the protocol
            u.toURI(); // does the extra checking required for validation of URI
            return u;
        } catch (MalformedURLException | URISyntaxException e) {
            return null;
        }
    }
}
