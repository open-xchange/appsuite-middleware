
package com.openexchange.ajax.infostore.actions;

import java.io.IOException;
import java.util.Date;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Params;

public class DetachInfostoreRequest extends AbstractInfostoreRequest<DetachInfostoreResponse> {

    private final String id;

    private final int folderId;

    private final Set<String> versions;

    private final Date timestamp;

    public DetachInfostoreRequest(String id, int folderId, Set<String> versions, Date timestamp) {
        super();
        this.id = id;
        this.folderId = folderId;
        this.versions = versions;
        this.timestamp = timestamp;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.PUT;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        return new Params(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DETACH, AJAXServlet.PARAMETER_ID, String.valueOf(id), AJAXServlet.PARAMETER_FOLDERID, String.valueOf(folderId), AJAXServlet.PARAMETER_TIMESTAMP, String.valueOf(timestamp.getTime())).toArray();
    }

    @Override
    public AbstractAJAXParser<? extends DetachInfostoreResponse> getParser() {
        return new AbstractAJAXParser<DetachInfostoreResponse>(getFailOnError()) {

            @Override
            protected DetachInfostoreResponse createResponse(Response response) throws JSONException {
                return new DetachInfostoreResponse(response);
            }
        };
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return new JSONArray(versions);
    }

}
