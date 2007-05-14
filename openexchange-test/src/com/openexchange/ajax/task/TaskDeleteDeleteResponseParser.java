/**
 * 
 */
package com.openexchange.ajax.task;

import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.TaskFields;
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
    public TaskDeleteDeleteResponseParser() {
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
