/**
 * 
 */
package com.openexchange.ajax.task;

import com.openexchange.ajax.framework.AJAXRequest;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class AbstractTaskRequest implements AJAXRequest {

    /**
     * URL of the tasks AJAX interface.
     */
    private static final String TASKS_URL = "/ajax/tasks";

    /**
     * 
     */
    protected AbstractTaskRequest() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public String getServletPath() {
        return TASKS_URL;
    }
}
