/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
     * @param task Task object with updated attributes. This task must contain
     * the attributes parent folder identifier, object identifier and last
     * modification timestamp.
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
     *                       <code>false</code> to write start- and end-time properties along with the full-time flag
     */
    public UpdateRequest(final Task task, final TimeZone timeZone, boolean failOnError, boolean useLegacyDates) {
        this(task.getParentFolderID(), true, task, timeZone, failOnError, useLegacyDates);
    }

    /**
     * Constructor if the task should be moved into another folder.
     * @param folderId source folder of the task.
     * @param task Task object with updated attributes. This task must contain
     * the attributes destination folder identifier, object identifier and last
     * modification timestamp.
     * @param timeZone timeZone for converting time stamps.
     */
    public UpdateRequest(final int folderId, final Task task,
        final TimeZone timeZone) {
        this(folderId, false, task, timeZone);
    }

    public UpdateRequest(final int folderId, final Task task,
        final TimeZone timeZone, boolean failOnError) {
        this(folderId, false, task, timeZone, failOnError);
    }

    private UpdateRequest(final int folderId, final boolean removeFolderId,
        final Task task, final TimeZone timeZone) {
        this(folderId, removeFolderId, task, timeZone, true);
    }

    private UpdateRequest(final int folderId, final boolean removeFolderId,
        final Task task, final TimeZone timeZone, boolean failOnError) {
        this(folderId, removeFolderId, task, timeZone, failOnError, true);
    }

    private UpdateRequest(final int folderId, final boolean removeFolderId,
        final Task task, final TimeZone timeZone, boolean failOnError, boolean useLegacyDates) {
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
        return new Parameter[] {
            new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet
                .ACTION_UPDATE),
            new Parameter(AJAXServlet.PARAMETER_INFOLDER, folderId),
            new Parameter(AJAXServlet.PARAMETER_ID, task.getObjectID()),
            new Parameter(AJAXServlet.PARAMETER_TIMESTAMP,
                task.getLastModified().getTime())
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
