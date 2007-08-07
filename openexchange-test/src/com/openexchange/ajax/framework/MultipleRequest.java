/**
 * 
 */
package com.openexchange.ajax.framework;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.AJAXServlet;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class MultipleRequest implements AJAXRequest {

    private final AJAXRequest[] requests;
    
    public MultipleRequest(final AJAXRequest[] requests) {
        this.requests = requests.clone();
    }

    /**
     * {@inheritDoc}
     */
    public Method getMethod() {
        return Method.PUT;
    }

    /**
     * {@inheritDoc}
     */
    public Parameter[] getParameters() {
        return new Parameter[0];
    }

    /**
     * {@inheritDoc}
     */
    public String getServletPath() {
        return "/ajax/multiple";
    }

    public Object getBody() throws JSONException {
        final JSONArray array = new JSONArray();
        for (AJAXRequest request : requests) {
            final JSONObject object = new JSONObject();
            final String path = request.getServletPath();
            object.put("module", path.substring(path.lastIndexOf('/') + 1));
            for (Parameter parameter : request.getParameters()) {
                object.put(parameter.getName(), parameter.getValue());
            }
            object.put(AJAXServlet.PARAMETER_DATA, request.getBody());
            array.put(object);
        }
        return array.toString();
    }

    /**
     * {@inheritDoc}
     */
    public MultipleParser getParser() {
        return new MultipleParser(requests);
    }
}
