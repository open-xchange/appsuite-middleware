/**
 * 
 */
package com.openexchange.ajax.session;

import com.openexchange.ajax.AJAXServlet;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class LoginRequest extends AbstractRequest {

    private static final String PARAM_PASSWORD = "password";

    private static final String PARAM_NAME = "name";

    public LoginRequest(final String login, final String password) {
        super(new Parameter[] {
            new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet
                .ACTION_LOGIN),
            new Parameter(PARAM_NAME, login),
            new Parameter(PARAM_PASSWORD, password)
        });
    }

    /**
     * {@inheritDoc}
     */
    public LoginResponseParser getParser() {
        return new LoginResponseParser();
    }
}
