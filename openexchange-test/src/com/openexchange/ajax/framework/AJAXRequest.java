/**
 * 
 */
package com.openexchange.ajax.framework;

import org.json.JSONException;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public interface AJAXRequest {

    enum Method {
        GET,
        POST,
        PUT
    }

    Method getMethod();

    String getServletPath();

    class Parameter {
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

    Parameter[] getParameters();

    AJAXResponseParser getParser();

    Object getBody() throws JSONException;
}
