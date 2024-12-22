package net.asaken1021.vmmanager.util.webapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JSONObjectParser {
    public static Object parseObject(Map<?, ?> data, String key) throws JSONParseException {
        Object obj = getMapValue(data, key);

        return obj;
    }

    public static Map<?, ?> parseMap(Map<?, ?> data, String key) throws JSONParseException {
        Object obj = getMapValue(data, key);

        if (obj instanceof Map<?, ?>) {
            return (Map<?, ?>) obj;
        } else {
            throw new JSONParseException("parseMap", key);
        }
    }

    public static List<?> parseList(Map<?, ?> data, String key, boolean isAllowEmptyList) throws JSONParseException {
        Object obj = getMapValue(data, key);

        if (obj instanceof List<?>) {
            return (List<?>) obj;
        } else {
            if (isAllowEmptyList) {
                return (List<?>) new ArrayList<>();
            } else {
                throw new JSONParseException("parseList", key);
            }
        }
    }

    public static String parseString(Map<?, ?> data, String key) throws JSONParseException {
        Object obj = getMapValue(data, key);

        if (obj instanceof String) {
            return (String) obj;
        } else {
            throw new JSONParseException("parseString", key);
        }
    }

    public static Integer parseInteger(Map<?, ?> data, String key) throws JSONParseException {
        Object obj = getMapValue(data, key);

        if (obj instanceof Integer) {
            return (Integer) obj;
        } else {
            throw new JSONParseException("parseInterger", key);
        }
    }

    private static Object getMapValue(Map<?, ?> data, String key) throws JSONParseException {
        Object obj = data.get(key);
        
        return obj;
    }
}
