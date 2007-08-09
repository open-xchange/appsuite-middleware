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
        public Parameter(final String name, final int[] values) {
            this.name = name;
            final StringBuilder columnSB = new StringBuilder();
            for (int i : values) {
                columnSB.append(i);
                columnSB.append(',');
            }
            columnSB.delete(columnSB.length() - 1, columnSB.length());
            this.value = columnSB.toString();
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

    /**
     * This method has to return all parameters that are necessary for the
     * request. The session request parameter is added by the {@link Executor}.
     * @return all request parameters except the session identifier.
     */
    Parameter[] getParameters();

    AbstractAJAXParser getParser();

    Object getBody() throws JSONException;
}
