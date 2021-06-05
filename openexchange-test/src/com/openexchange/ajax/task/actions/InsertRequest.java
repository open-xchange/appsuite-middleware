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
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.groupware.tasks.Task;

/**
 * Stores the parameters for inserting the task.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class InsertRequest extends AbstractTaskRequest<InsertResponse> {

    private final Task task;
    private final TimeZone timeZone;
    private final boolean timeZoneParam;
    private final boolean failOnError;
    private final boolean useLegacyDates;

    /**
     * Initializes a new {@link InsertRequest}.
     *
     * @param task The task to insert
     * @param timeZone The timezone to use
     * @param timeZoneParam <code>true</code> to add the timezone as request parameter, <code>false</code>, otherwise
     * @param failOnError <code>true</code> to fail on errors, <code>false</code>, otherwise
     * @param useLegacyDates <code>true</code> to convert the start- and end-date in legacy mode with <code>Date</code>-types,
     *            <code>false</code> to write start- and end-time properties along with the full-time flag
     */
    public InsertRequest(Task task, TimeZone timeZone, boolean timeZoneParam, boolean failOnError, boolean useLegacyDates) {
        super();
        this.task = task;
        this.timeZone = timeZone;
        this.timeZoneParam = timeZoneParam;
        this.failOnError = failOnError;
        this.useLegacyDates = useLegacyDates;
    }

    public InsertRequest(Task task, TimeZone timeZone, boolean timeZoneParam, boolean failOnError) {
        this(task, timeZone, timeZoneParam, failOnError, true);
    }

    public InsertRequest(Task task, TimeZone timeZone, boolean failOnError) {
        this(task, timeZone, false, failOnError);
    }

    public InsertRequest(Task task, TimeZone timeZone) {
        this(task, timeZone, false, true);
    }

    @Override
    public JSONObject getBody() throws JSONException {
        return useLegacyDates ? convert(task, timeZone) : convertNew(task, timeZone);
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() {
        List<Parameter> retval = new ArrayList<Parameter>(3);
        retval.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW));
        retval.add(new Parameter(AJAXServlet.PARAMETER_FOLDERID, String.valueOf(task.getParentFolderID())));
        if (timeZoneParam) {
            retval.add(new Parameter(AJAXServlet.PARAMETER_TIMEZONE, timeZone.getID()));
        }
        return retval.toArray(new Parameter[retval.size()]);
    }

    @Override
    public InsertParser getParser() {
        return new InsertParser(failOnError, task.getParentFolderID());
    }
}
