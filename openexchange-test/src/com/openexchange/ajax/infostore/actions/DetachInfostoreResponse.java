
package com.openexchange.ajax.infostore.actions;

import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;

public class DetachInfostoreResponse extends AbstractAJAXResponse {

    protected DetachInfostoreResponse(Response response) {
        super(response);
    }

    public int[] getNotDeleted() throws JSONException {
        JSONArray json = ((JSONArray) getData());
        int retval[] = new int[json.length()];
        for (int i = 0; i < json.length(); i++) {
            retval[i] = json.getInt(i);
        }
        return retval;
    }

}
