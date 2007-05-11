package com.openexchange.ajax.framework;

import junit.framework.Assert;

import com.openexchange.ajax.container.Response;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class AJAXResponse extends Assert {

    private final Response response;

    protected AJAXResponse(final Response response) {
        super();
        this.response = response;
    }

    public Response getResponse() {
        return response;
    }
}
