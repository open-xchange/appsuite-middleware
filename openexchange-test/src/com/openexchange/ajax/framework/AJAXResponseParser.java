/**
 * 
 */
package com.openexchange.ajax.framework;

import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.json.JSONException;

import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.container.Response;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class AJAXResponseParser extends Assert {

    protected AJAXResponseParser() {
        super();
    }

    protected Response getResponse(final String body) throws JSONException {
        final Response response = Response.parse(body);
        assertFalse(response.getErrorMessage(), response.hasError());
        return response;
    }

    public void checkResponse(final WebResponse resp) {
        assertEquals("Response code is not okay.", HttpServletResponse.SC_OK,
            resp.getResponseCode());
    }

    public AJAXResponse parse(final String body) throws JSONException {
        return createResponse(getResponse(body));
    }

    protected abstract AJAXResponse createResponse(final Response response)
        throws JSONException;
}
