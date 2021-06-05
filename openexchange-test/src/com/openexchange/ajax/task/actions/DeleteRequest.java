/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.task.actions;

import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.groupware.tasks.Task;

/**
 * Stores parameters for the task delete request.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class DeleteRequest extends AbstractTaskRequest<CommonDeleteResponse> {

    private final int folderId;

    private final int taskId;

    private final int[] taskIds;

    private final Date lastModified;

    private final boolean failOnError;

    /**
     * Default constructor.
     */
    public DeleteRequest(final int folderId, final int taskId, final Date lastModified) {
        this(folderId, taskId, lastModified, true);
    }

    /**
     * @param task Task object to delete. This object must contain the folder
     *            identifier, the object identifier and the last modification timestamp.
     */
    public DeleteRequest(final Task task) {
        this(task.getParentFolderID(), task.getObjectID(), task.getLastModified(), true);
    }

    /**
     * @param insert An insert response contains all necessary information for
     *            deleting the task.
     */
    public DeleteRequest(final InsertResponse insert) {
        this(insert.getFolderId(), insert.getId(), insert.getTimestamp(), true);
    }

    /**
     * Default constructor.
     */
    public DeleteRequest(final int folderId, final int taskId, final Date lastModified, boolean failOnError) {
        super();
        this.folderId = folderId;
        this.taskId = taskId;
        this.taskIds = null;
        this.lastModified = lastModified;
        this.failOnError = failOnError;
    }

    public DeleteRequest(final int folderId, final int[] taskIds, final Date lastModified, boolean failOnError) {
        super();
        this.folderId = folderId;
        this.taskId = 0;
        this.taskIds = taskIds;
        this.lastModified = lastModified;
        this.failOnError = failOnError;
    }

    /**
     * @param task Task object to delete. This object must contain the folder
     *            identifier, the object identifier and the last modification timestamp.
     */
    public DeleteRequest(final Task task, boolean failOnError) {
        this(task.getParentFolderID(), task.getObjectID(), task.getLastModified(), failOnError);
    }

    /**
     * @param insert An insert response contains all necessary information for
     *            deleting the task.
     */
    public DeleteRequest(final InsertResponse insert, boolean failOnError) {
        this(insert.getFolderId(), insert.getId(), insert.getTimestamp(), failOnError);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getBody() throws JSONException {
        if (taskIds == null) {
            final JSONObject json = new JSONObject();
            json.put(AJAXServlet.PARAMETER_ID, taskId);
            json.put(AJAXServlet.PARAMETER_INFOLDER, folderId);
            return json;
        }
        JSONArray jsonArray = new JSONArray();
        for (final int id : taskIds) {
            final JSONObject json = new JSONObject();
            json.put(AJAXServlet.PARAMETER_ID, id);
            json.put(AJAXServlet.PARAMETER_INFOLDER, folderId);
            jsonArray.put(json);
        }
        return jsonArray;
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
        return new Parameter[] { new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DELETE), new Parameter(AJAXServlet.PARAMETER_TIMESTAMP, String.valueOf(lastModified.getTime()))
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeleteParser getParser() {
        return new DeleteParser(failOnError);
    }
}
