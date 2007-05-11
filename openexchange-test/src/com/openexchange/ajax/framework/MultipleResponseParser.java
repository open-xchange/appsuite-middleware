/**
 * 
 */
package com.openexchange.ajax.framework;

import org.json.JSONArray;
import org.json.JSONException;

import com.openexchange.ajax.container.Response;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class MultipleResponseParser extends AJAXResponseParser {

    private final AJAXRequest[] requests;

    /**
     * @param requests 
     * 
     */
    public MultipleResponseParser(AJAXRequest[] requests) {
        super();
        this.requests = requests;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AJAXResponse parse(final String body) throws JSONException {
        final Response response = new Response();
        response.setData(new JSONArray(body));
        return createResponse(response);
    }

    @Override
    protected MultipleResponse createResponse(final Response response)
        throws JSONException {
        final JSONArray array = (JSONArray) response.getData();
        assertEquals("Multiple response array has different size.",
            requests.length, array.length());
        final AJAXResponse[] responses = new AJAXResponse[requests.length];
        for (int i = 0; i < requests.length; i++) {
            responses[i] = requests[i].getParser().parse(array.getString(i));
        }
        return new MultipleResponse(responses);
    }
}
