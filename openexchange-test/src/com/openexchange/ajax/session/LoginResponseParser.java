/**
 * 
 */
package com.openexchange.ajax.session;

import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.Login;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AJAXResponse;
import com.openexchange.ajax.framework.AJAXResponseParser;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class LoginResponseParser extends AJAXResponseParser {

    /**
     * Default constructor.
     */
    public LoginResponseParser() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LoginResponse parse(final String body) throws JSONException {
        final Response response = new Response();
        response.setData(new JSONObject(body));
        return createResponse(response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected LoginResponse createResponse(final Response response)
        throws JSONException {
        LoginResponse retval = new LoginResponse(response);
        final JSONObject json = (JSONObject) response.getData();
        if (json.has("error")) {
            retval = new LoginResponse(Response.parse(json.toString()));
        } else {
            retval.setSessionId(json.getString(Login.PARAMETER_SESSION));
            retval.setRandom(json.getString(Login._random));
        }
        assertFalse(response.getErrorMessage(), response.hasError());
        assertTrue("Session ID is missing.", json.has(Login.PARAMETER_SESSION));
        assertTrue("Random is missing.", json.has(Login._random));
        return retval;
    }
}
