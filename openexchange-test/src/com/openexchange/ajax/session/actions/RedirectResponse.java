package com.openexchange.ajax.session.actions;

import com.openexchange.ajax.framework.AbstractAJAXResponse;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class RedirectResponse extends AbstractAJAXResponse {

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
