
package com.openexchange.unitedinternet.smartdrive.client.internal;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;

/**
 * {@link JSONCoercion} - Turns JSON data to its Java representation and vice versa.
 * <p>
 * A {@link JSONObject} is coerced to a {@link Map}, a {@link JSONArray} is coerced to a {@link Collection}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JSONCoercion {

    private JSONCoercion() {
        super();
    }

    /**
     * Coerces given JSON data to its Java representation.
     * 
     * @param object The JSON data to coerce
     * @return The resulting Java representation.
     * @throws JSONException If coercion fails
     */
    public static Object coerceToNative(final Object object) throws JSONException {
        if (object instanceof JSONArray) {
            final JSONArray jsonArray = (JSONArray) object;
            final int length = jsonArray.length();
            final List<Object> list = new ArrayList<Object>(length);
            for (int i = 0; i < length; i++) {
                list.add(coerceToNative(jsonArray.get(i)));
            }
            return list;
        }
        if (object instanceof JSONObject) {
            final JSONObject jsonObject = (JSONObject) object;
            final Map<String, Object> map = new HashMap<String, Object>(jsonObject.length());
            for (final String key : jsonObject.keySet()) {
                map.put(key, coerceToNative(jsonObject.get(key)));
            }
            return map;
        }
        if (JSONObject.NULL == object) {
            return null;
        }
        return object;
    }

    /**
     * Checks if specified object needs to be coerced to JSON.
     * 
     * @param value The object to check
     * @return <code>true</code> if specified object needs to be coerced to JSON; otherwise <code>false</code>
     */
    public static boolean needsJSONCoercion(final Object value) {
        return (value instanceof Map) || (value instanceof Collection) || isArray(value);
    }

    /**
     * Checks if specified object needs to be coerced to a native Java object.
     * 
     * @param value The object to check
     * @return <code>true</code> if specified object needs to be coerced to a native Java object; otherwise <code>false</code>
     */
    public static boolean needsNativeCoercion(final Object value) {
        return (value instanceof JSONValue);
    }

    /**
     * Coerces given Java object to its JSON representation.
     * 
     * @param value The Java object to coerce
     * @return The resulting JSON representation
     * @throws JSONException If coercion fails
     */
    public static Object coerceToJSON(final Object value) throws JSONException {
        if (value instanceof JSONValue) {
            return value;
        }
        if (value instanceof Map) {
            @SuppressWarnings("unchecked") final Map<String, ?> map = (Map<String, ?>) value;
            final JSONObject jsonObject = new JSONObject();
            for (final Map.Entry<String, ?> entry : map.entrySet()) {
                jsonObject.put(entry.getKey(), coerceToJSON(entry.getValue()));
            }
            return jsonObject;
        }
        if (value instanceof Collection) {
            final Collection<?> collection = (Collection<?>) value;
            final JSONArray jsonArray = new JSONArray();
            for (final Object object : collection) {
                jsonArray.put(coerceToJSON(object));
            }
            return jsonArray;
        }
        if (isArray(value)) {
            final int length = Array.getLength(value);
            final JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < length; i++) {
                final Object object = Array.get(value, i);
                jsonArray.put(coerceToJSON(object));
            }
            return jsonArray;
        }
        /*
         * Put directly
         */
        // new JSONArray().put(value); // Is valid in JSON? But why? JSONArray.put(Object) validates nothing
        return value;
    }

    /**
     * Checks if specified object is an array.
     * 
     * @param object The object to check
     * @return <code>true</code> if specified object is an array; otherwise <code>false</code>
     */
    public static boolean isArray(final Object object) {
        /*-
         * getClass().isArray() is significantly slower on Sun Java 5 or 6 JRE than on IBM.
         * So much that using clazz.getName().charAt(0) == '[' is faster on Sun JVM.
         */
        // return (null != object && object.getClass().isArray());
        return (null != object && '[' == object.getClass().getName().charAt(0));
    }

}
