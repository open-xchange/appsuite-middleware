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

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.groupware.tasks.Task;

/**
 * Retrieves a task from the server.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class GetRequest extends AbstractTaskRequest<GetResponse> {

    private final int folderId;
    private final int taskId;
    private final TimeZone timeZone;
    private final boolean failOnError;

    public GetRequest(int folderId, int taskId, boolean failOnError) {
        super();
        this.folderId = folderId;
        this.taskId = taskId;
        this.timeZone = null;
        this.failOnError = failOnError;
    }

    public GetRequest(int folderId, int taskId, TimeZone timeZone, boolean failOnError) {
        super();
        this.folderId = folderId;
        this.taskId = taskId;
        this.timeZone = timeZone;
        this.failOnError = failOnError;
    }

    public GetRequest(final InsertResponse insert) {
        this(insert.getFolderId(), insert.getId());
    }

    public GetRequest(final int folderId, final int taskId) {
        this(folderId, taskId, true);
    }

    public GetRequest(Task task) {
        this(task.getParentFolderID(), task.getObjectID());
    }

    public GetRequest(Task task, TimeZone timeZone) {
        this(task.getParentFolderID(), task.getObjectID(), timeZone, true);
    }

    @Override
    public Object getBody() {
        return null;
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public Parameter[] getParameters() {
        List<Parameter> retval = new ArrayList<Parameter>(4);
        retval.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_GET));
        retval.add(new Parameter(AJAXServlet.PARAMETER_INFOLDER, folderId));
        retval.add(new Parameter(AJAXServlet.PARAMETER_ID, taskId));
        if (null != timeZone) {
            retval.add(new Parameter(AJAXServlet.PARAMETER_TIMEZONE, timeZone.getID()));
        }
        return retval.toArray(new Parameter[retval.size()]);
    }

    @Override
    public GetParser getParser() {
        return new GetParser(failOnError);
    }
}
