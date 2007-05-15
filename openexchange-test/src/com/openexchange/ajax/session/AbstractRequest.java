/**
 * 
 */
package com.openexchange.ajax.session;

import com.openexchange.ajax.framework.AJAXRequest;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class AbstractRequest implements AJAXRequest {

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
    public Object getBody() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getServletPath() {
        return LOGIN_URL;
    }

    /**
     * {@inheritDoc}
     */
    public Method getMethod() {
        return Method.POST;
    }

    /**
     * {@inheritDoc}
     */
    public Parameter[] getParameters() {
        return parameters.clone();
    }
}
