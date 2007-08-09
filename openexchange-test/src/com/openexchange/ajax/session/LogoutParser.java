/**
 * 
 */
package com.openexchange.ajax.session;

import org.json.JSONException;

import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class LogoutParser extends AbstractAJAXParser {

    /**
     * Default constructor.
     */
    LogoutParser() {
        super(true);
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
    protected LogoutResponse createResponse(final Response response)
        throws JSONException {
        return new LogoutResponse();
    }
}
