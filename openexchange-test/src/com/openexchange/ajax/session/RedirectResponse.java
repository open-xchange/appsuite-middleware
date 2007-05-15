package com.openexchange.ajax.session;

import com.openexchange.ajax.framework.AJAXResponse;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class RedirectResponse extends AJAXResponse {

    private final String location;
    
    /**
     *
     */
    RedirectResponse(final String location) {
        super(null);
        this.location = location;
    }

    /**
     * @return the location
     */
    public String getLocation() {
        return location;
    }
}
