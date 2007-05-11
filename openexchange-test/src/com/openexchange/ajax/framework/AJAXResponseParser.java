/**
 * 
 */
package com.openexchange.ajax.framework;

import junit.framework.Assert;

import org.json.JSONException;

import com.openexchange.ajax.container.Response;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class AJAXResponseParser extends Assert {

    public AJAXResponseParser() {
        super();
    }

    protected Response getResponse(final String body) throws JSONException {
        final Response response = Response.parse(body);
        assertFalse(response.getErrorMessage(), response.hasError());
        return response;
    }

    public AJAXResponse parse(final String body) throws JSONException {
        return createResponse(getResponse(body));
    }

    protected abstract AJAXResponse createResponse(final Response response)
        throws JSONException;
}
