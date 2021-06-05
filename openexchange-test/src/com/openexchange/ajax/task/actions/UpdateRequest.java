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

import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.TaskFields;
import com.openexchange.groupware.tasks.Task;

/**
 * Implements creating the necessary values for a task update request. All
 * necessary values are read from the task. The task must contain the folder and
 * object identifier and the last modification timestamp.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class UpdateRequest extends AbstractTaskRequest<UpdateResponse> {

    private final int folderId;

    private final boolean removeFolderId;

    private final Task task;

    private final TimeZone timeZone;

    private final boolean failOnError;

    private final boolean useLegacyDates;

    /**
     * Constructor if the task should not be moved.
     * 
     * @param task Task object with updated attributes. This task must contain
     *            the attributes parent folder identifier, object identifier and last
     *            modification timestamp.
     */
    public UpdateRequest(final Task task, final TimeZone timeZone) {
        this(task.getParentFolderID(), true, task, timeZone);
    }

    public UpdateRequest(final Task task, final TimeZone timeZone, boolean failOnError) {
        this(task.getParentFolderID(), true, task, timeZone, failOnError);
    }

    /**
     * Initializes a new {@link UpdateRequest} to update a task without changing the folder.
     *
     * @param task The task to update
     * @param timeZone The timezone to use
     * @param failOnError <code>true</code> to fail on errors, <code>false</code>, otherwise
     * @param useLegacyDates <code>true</code> to convert the start- and end-date in legacy mode with <code>Date</code>-types,
     *            <code>false</code> to write start- and end-time properties along with the full-time flag
     */
    public UpdateRequest(final Task task, final TimeZone timeZone, boolean failOnError, boolean useLegacyDates) {
        this(task.getParentFolderID(), true, task, timeZone, failOnError, useLegacyDates);
    }

    /**
     * Constructor if the task should be moved into another folder.
     * 
     * @param folderId source folder of the task.
     * @param task Task object with updated attributes. This task must contain
     *            the attributes destination folder identifier, object identifier and last
     *            modification timestamp.
     * @param timeZone timeZone for converting time stamps.
     */
    public UpdateRequest(final int folderId, final Task task, final TimeZone timeZone) {
        this(folderId, false, task, timeZone);
    }

    public UpdateRequest(final int folderId, final Task task, final TimeZone timeZone, boolean failOnError) {
        this(folderId, false, task, timeZone, failOnError);
    }

    private UpdateRequest(final int folderId, final boolean removeFolderId, final Task task, final TimeZone timeZone) {
        this(folderId, removeFolderId, task, timeZone, true);
    }

    private UpdateRequest(final int folderId, final boolean removeFolderId, final Task task, final TimeZone timeZone, boolean failOnError) {
        this(folderId, removeFolderId, task, timeZone, failOnError, true);
    }

    private UpdateRequest(final int folderId, final boolean removeFolderId, final Task task, final TimeZone timeZone, boolean failOnError, boolean useLegacyDates) {
        super();
        this.folderId = folderId;
        this.removeFolderId = removeFolderId;
        this.task = task;
        this.timeZone = timeZone;
        this.failOnError = failOnError;
        this.useLegacyDates = useLegacyDates;
    }

    @Override
    public JSONObject getBody() throws JSONException {
        final JSONObject json = useLegacyDates ? convert(task, timeZone) : convertNew(task, timeZone);
        if (removeFolderId) {
            json.remove(TaskFields.FOLDER_ID);
        }
        return json;
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
        return new Parameter[] { new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATE), new Parameter(AJAXServlet.PARAMETER_INFOLDER, folderId), new Parameter(AJAXServlet.PARAMETER_ID, task.getObjectID()), new Parameter(AJAXServlet.PARAMETER_TIMESTAMP, task.getLastModified().getTime())
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UpdateParser getParser() {
        return new UpdateParser(failOnError);
    }

    /**
     * @return the task
     */
    protected Task getTask() {
        return task;
    }
}
