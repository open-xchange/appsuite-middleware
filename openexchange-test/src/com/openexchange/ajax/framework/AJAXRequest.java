/**
 * 
 */
package com.openexchange.ajax.framework;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class AJAXRequest {

    public abstract Method getMethod();

    public enum Method {
        GET,
        POST,
        PUT
    }

    public abstract String getServletPath();

    public abstract Parameter[] getParameters();

    public static class Parameter {
        private final String name;
        private final String value;
        public Parameter(final String name, final String value) {
            this.name = name;
            this.value = value;
        }
        /**
         * @return the name
         */
        public String getName() {
            return name;
        }
        /**
         * @return the value
         */
        public String getValue() {
            return value;
        }
    }

    public abstract AJAXResponseParser getParser();

    public abstract Object getBody() throws JSONException;
}
