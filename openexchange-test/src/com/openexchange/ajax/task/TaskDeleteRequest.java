/**
 * 
 */
package com.openexchange.ajax.task;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AJAXResponseParser;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class TaskDeleteRequest extends AbstractTaskRequest {

    private final int folderId;

    private final int taskId;

    private final Date lastModified;

    /**
     * Default constructor.
     */
    public TaskDeleteRequest(final int folderId, final int taskId,
        final Date lastModified) {
        super();
        this.folderId = folderId;
        this.taskId = taskId;
        this.lastModified = lastModified;
    }

    /**
     * {@inheritDoc}
     */
    public Object getBody() throws JSONException {
        final JSONObject json = new JSONObject();
        json.put(AJAXServlet.PARAMETER_ID, taskId);
        json.put(AJAXServlet.PARAMETER_INFOLDER, folderId);
        return json;
    }

    /**
     * {@inheritDoc}
     */
    public Method getMethod() {
        return Method.PUT;
    }

    /**
     * {@inheritDoc}
     */
    public Parameter[] getParameters() {
        return new Parameter[] {
            new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet
                .ACTION_DELETE),
            new Parameter(AJAXServlet.PARAMETER_TIMESTAMP,
                String.valueOf(lastModified.getTime()))
        };
    }

    /**
     * {@inheritDoc}
     */
    public AJAXResponseParser getParser() {
        return new TaskDeleteDeleteResponseParser();
    }
}
