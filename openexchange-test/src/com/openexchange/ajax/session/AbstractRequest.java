/**
 * 
 */
package com.openexchange.ajax.session;

import java.io.InputStream;

import org.json.JSONObject;

import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AJAXRequest.Method;
import com.openexchange.ajax.framework.AJAXRequest.Parameter;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class AbstractRequest extends AJAXRequest {

    /**
     * URL of the login AJAX servlet.
     */
    private static final String LOGIN_URL = "/ajax/login";
    private final Parameter[] parameters;

    /**
     * Default constructor.
     */
    protected AbstractRequest(final Parameter[] parameters) {
        super();
        this.parameters = parameters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getBody() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getServletPath() {
        return LOGIN_URL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Method getMethod() {
        return Method.POST;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Parameter[] getParameters() {
        return parameters.clone();
    }
}
