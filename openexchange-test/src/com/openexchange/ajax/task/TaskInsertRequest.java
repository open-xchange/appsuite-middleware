/**
 * 
 */
package com.openexchange.ajax.task;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AJAXResponseParser;
import com.openexchange.ajax.writer.TaskWriter;
import com.openexchange.groupware.tasks.Task;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class TaskInsertRequest extends AbstractTaskRequest {

    private final Task task;

    private final TimeZone timeZone;

    public TaskInsertRequest(final Task task, final TimeZone timeZone) {
        this.task = task;
        this.timeZone = timeZone;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getBody() throws JSONException {
        final StringWriter stringW = new StringWriter();
        final PrintWriter printW = new PrintWriter(stringW);
        final TaskWriter taskW = new TaskWriter(printW, timeZone);
        taskW.writeTask(task);
        printW.flush();
        return new JSONObject(stringW.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Parameter[] getParameters() {
        return new Parameter[] {
            new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW),
            new Parameter(AJAXServlet.PARAMETER_FOLDERID, String.valueOf(task
                .getParentFolderID()))
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AJAXResponseParser getParser() {
        return new TaskInsertResponseParser();
    }
}
