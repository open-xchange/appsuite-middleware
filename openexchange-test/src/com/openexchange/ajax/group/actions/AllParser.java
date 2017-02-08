
package com.openexchange.ajax.group.actions;

import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;

public class AllParser extends AbstractAJAXParser<AllResponse> {

    protected AllParser(boolean failOnError) {
        super(failOnError);
    }

    @Override
    protected AllResponse createResponse(Response response) throws JSONException {
        assertNotNull("Timestamp is missing.", response.getTimestamp());
        return new AllResponse(response);
    }
}
