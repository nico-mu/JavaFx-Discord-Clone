package de.uniks.stp.util;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.StringReader;

public class JsonUtil {

    public static JsonObject parse(String json) {
        return Json.createReader(new StringReader(json)).readObject();
    }

}
