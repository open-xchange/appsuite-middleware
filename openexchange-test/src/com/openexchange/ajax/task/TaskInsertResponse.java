/**
 * 
 */
package com.openexchange.ajax.task;

import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AJAXResponse;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class TaskInsertResponse extends AJAXResponse {

    private int id;
    
    /**
     * @param response
     */
    public TaskInsertResponse(final Response response) {
        super(response);
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    void setId(final int id) {
        this.id = id;
    }

}
