/**
 * 
 */
package com.openexchange.ajax.session;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;

import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.Login;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class RedirectResponseParser extends AbstractAJAXParser {

    private String location;
    
    /**
     * Default constructor.
     */
    RedirectResponseParser() {
        super(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkResponse(final WebResponse resp) {
        assertEquals("Response code is not okay.", HttpServletResponse
            .SC_MOVED_TEMPORARILY, resp.getResponseCode());
        location = resp.getHeaderField("Location");
        assertNotNull("Location for redirect is missing.", location);
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractAJAXResponse parse(final String body) throws JSONException {
        return createResponse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RedirectResponse createResponse(final Response response)
        throws JSONException {
        return new RedirectResponse(location);
    }

}
