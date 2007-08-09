/**
 * 
 */
package com.openexchange.ajax.session;

import org.json.JSONException;
import org.json.JSONObject;

import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.Login;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class LoginResponseParser extends AbstractAJAXParser {

    private String jvmRoute;
    
    /**
     * Default constructor.
     */
    LoginResponseParser() {
        super(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkResponse(final WebResponse resp) {
        super.checkResponse(resp);
        final String[] newCookies = resp.getNewCookieNames();
        boolean oxCookieFound = false;
        for (String newCookie : newCookies) {
            if (newCookie.startsWith(Login.cookiePrefix)) {
                oxCookieFound = true;
                break;
            }
        }
        assertTrue("Session cookie is missing.", oxCookieFound);
        final String jsessionId = resp.getNewCookieValue("JSESSIONID");
        assertNotNull("JSESSIONID cookie is missing.", jsessionId);
        final int dotPos = jsessionId.lastIndexOf('.');
        assertTrue("jvmRoute is missing.", dotPos > 0);
        jvmRoute = jsessionId.substring(dotPos + 1);
    }

    /**
     * {@inheritDoc}
     * @throws IOException 
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
            retval.setJvmRoute(jvmRoute);
            retval.setSessionId(json.getString(Login.PARAMETER_SESSION));
            retval.setRandom(json.getString(Login._random));
        }
        assertFalse(response.getErrorMessage(), response.hasError());
        assertTrue("Session ID is missing.", json.has(Login.PARAMETER_SESSION));
        assertTrue("Random is missing.", json.has(Login._random));
        return retval;
    }
}
