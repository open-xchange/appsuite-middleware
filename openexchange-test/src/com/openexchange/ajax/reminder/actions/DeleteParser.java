
package com.openexchange.ajax.reminder.actions;

import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.CommonDeleteResponse;

public class DeleteParser extends AbstractAJAXParser<CommonDeleteResponse> {

    public DeleteParser() {
        super(true);
    }

    protected DeleteParser(boolean failOnError) {
        super(failOnError);
    }

    @Override
    protected CommonDeleteResponse createResponse(final Response response) throws JSONException {
        return new CommonDeleteResponse(response);
    }

}
