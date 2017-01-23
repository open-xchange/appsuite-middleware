
package com.openexchange.ajax.contact.action;

import java.io.IOException;
import java.util.TimeZone;
import org.json.JSONException;
import com.openexchange.ajax.framework.Params;

public class GetContactForUserRequest extends AbstractContactRequest<GetResponse> {

    private final String id;
    private final boolean failOnError;
    private final TimeZone timezone;

    public GetContactForUserRequest(int id, boolean failOnError, TimeZone tz) {
        this.id = String.valueOf(id);
        this.failOnError = failOnError;
        this.timezone = tz;
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public Parameter[] getParameters() throws IOException, JSONException {
        return new Params("action", "getuser", "id", this.id).toArray();
    }

    @Override
    public GetParser getParser() {
        return new GetParser(failOnError, timezone);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return null;
    }

}
