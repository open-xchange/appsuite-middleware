
package com.openexchange.ajax.mail.actions;

import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;

public class CountResponse extends AbstractAJAXResponse {

    protected CountResponse(Response response) {
        super(response);
    }

    public int getCount() throws JSONException {
        return ((Integer) getData()).intValue();
    }

}
