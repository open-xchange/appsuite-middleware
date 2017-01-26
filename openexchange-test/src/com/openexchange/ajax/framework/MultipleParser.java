/**
 *
 */

package com.openexchange.ajax.framework;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class MultipleParser<T extends AbstractAJAXResponse> extends AbstractAJAXParser<MultipleResponse<T>> {

    private final AJAXRequest<T>[] requests;

    /**
     * @param requests
     */
    public MultipleParser(final AJAXRequest<T>[] requests) {
        super(true);
        this.requests = requests.clone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MultipleResponse<T> parse(final String body) throws JSONException {
        final Response response = new Response();
        response.setData(new JSONArray(body));
        return createResponse(response);
    }

    @Override
    protected MultipleResponse<T> createResponse(final Response response) throws JSONException {
        final JSONArray array = (JSONArray) response.getData();
        assertEquals("Multiple response array has different size.", requests.length, array.length());
        final List<T> responses2 = new ArrayList<T>();
        for (int i = 0; i < requests.length; i++) {
            responses2.add(requests[i].getParser().parse(array.getString(i)));
        }
        return new MultipleResponse<T>(createArray(responses2));
    }

    private T[] createArray(final List<T> list) {
        final T test = list.get(0);
        return list.toArray((T[]) Array.newInstance(test.getClass(), list.size()));
    }
}
