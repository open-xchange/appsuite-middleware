
package com.openexchange.ajax.mail.actions;

import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;

public class CountResponse extends AbstractAJAXResponse {

    protected CountResponse(Response response) {
        super(response);
    }

    public int getCount() {
        return ((Integer) getData()).intValue();
    }

}
