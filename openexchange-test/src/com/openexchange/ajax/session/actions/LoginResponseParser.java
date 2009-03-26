/**
 * 
 */
package com.openexchange.ajax.session.actions;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
public class LoginResponseParser extends AbstractAJAXParser<LoginResponse> {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(LoginResponseParser.class);

    private String jvmRoute;
    
    /**
     * Default constructor.
     */
    LoginResponseParser(final boolean failOnError) {
        super(failOnError);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkResponse(final WebResponse resp) {
        super.checkResponse(resp);
        // Check for error messages
        try {
            super.getResponse(resp.getText());
        } catch (final JSONException e) {
            try {
                LOG.error("Invalid login body: \"" + resp.getText() + "\"");
            } catch (final IOException e1) {
                fail(e.getMessage());
            }
            fail(e.getMessage());
        } catch (final IOException e) {
            fail(e.getMessage());
        }
        final String[] newCookies = resp.getNewCookieNames();
        if (isFailOnError()) {
            boolean oxCookieFound = false;
            for (final String newCookie : newCookies) {
                if (newCookie.startsWith(Login.COOKIE_PREFIX)) {
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
    }

    /**
     * {@inheritDoc}
     * @throws IOException 
     */
    @Override
    public LoginResponse parse(final String body) throws JSONException {
        final JSONObject json = new JSONObject(body);
        final Response response = getResponse(body);
        response.setData(json);
        return createResponse(response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected LoginResponse createResponse(final Response response)
        throws JSONException {
        final LoginResponse retval = new LoginResponse(response);
        final JSONObject json = (JSONObject) response.getData();
        if (response.hasError()) {
            response.setData(null);
        } else {
            retval.setJvmRoute(jvmRoute);
            retval.setSessionId(json.getString(Login.PARAMETER_SESSION));
            retval.setRandom(json.getString(Login.PARAM_RANDOM));
        }
        if (isFailOnError()) {
            assertFalse(response.getErrorMessage(), response.hasError());
            assertTrue("Session ID is missing.", json.has(Login.PARAMETER_SESSION));
            assertTrue("Random is missing.", json.has(Login.PARAM_RANDOM));
        }
        return retval;
    }
}
