/**
 * 
 */
package com.openexchange.ajax.session;

import org.json.JSONException;

import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AJAXResponse;
import com.openexchange.ajax.framework.AJAXResponseParser;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class LogoutParser extends AJAXResponseParser {

    /**
     * Default constructor.
     */
    public LogoutParser() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    
    @Override
    public AJAXResponse parse(final String body) throws JSONException {
        return createResponse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected LogoutResponse createResponse(final Response response)
        throws JSONException {
        return new LogoutResponse();
    }
}
