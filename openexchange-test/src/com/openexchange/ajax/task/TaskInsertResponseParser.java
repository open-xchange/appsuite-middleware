/**
 * 
 */
package com.openexchange.ajax.task;

import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.TaskFields;
import com.openexchange.ajax.framework.AJAXResponseParser;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class TaskInsertResponseParser extends AJAXResponseParser {

    /**
     * Default constructor.
     */
    public TaskInsertResponseParser() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TaskInsertResponse createResponse(final Response response)
        throws JSONException {
        final TaskInsertResponse retval = new TaskInsertResponse(response);
        final JSONObject data = (JSONObject) response.getData();
        if (!data.has(TaskFields.ID)) {
            fail(response.getErrorMessage());
        }
        final int taskId = data.getInt(TaskFields.ID);
        assertTrue("Problem while inserting task", taskId > 0);
        retval.setId(taskId);
        return retval;
    }
}
