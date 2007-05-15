/**
 * 
 */
package com.openexchange.ajax.task;

import org.json.JSONException;

import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AJAXResponse;
import com.openexchange.ajax.framework.AJAXResponseParser;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class TaskDeleteDeleteResponseParser extends AJAXResponseParser {

    /**
     *
     */
    TaskDeleteDeleteResponseParser() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AJAXResponse createResponse(final Response response)
        throws JSONException {
        final TaskDeleteResponse retval = new TaskDeleteResponse(response);
        return retval;
    }
}
