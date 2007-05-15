/**
 * 
 */
package com.openexchange.ajax.session;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AJAXResponseParser;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class LogoutRequest extends AbstractRequest {

    /**
     * Default constructor.
     * @param sessionId session identifier.
     */
    public LogoutRequest(final String sessionId) {
        super(new Parameter[] {
            new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet
                .ACTION_LOGOUT),
            new Parameter(AJAXServlet.PARAMETER_SESSION, sessionId)
        });
    }

    /**
     * {@inheritDoc}
     */
    public AJAXResponseParser getParser() {
        return new LogoutParser();
    }
}
