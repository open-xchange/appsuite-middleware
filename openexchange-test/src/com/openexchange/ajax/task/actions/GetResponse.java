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

import java.util.Locale;
import java.util.TimeZone;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.ajax.fields.TaskFields;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.parser.TaskParser;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tasks.Task;

/**
 * Stores the response of getting a task from the server.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class GetResponse extends AbstractAJAXResponse {

    /**
     * @param response
     */
    GetResponse(final Response response) {
        super(response);
    }

    /**
     * @return the task
     * @throws OXException parsing the task out of the response fails.
     */
    public Task getTask(final TimeZone timeZone) throws OXException {
        return getTask(timeZone, true);
    }

    /**
     * Parses the task from the response.
     *
     * @param timeZone The client timezone
     * @param useLegacyDates <code>true</code> to convert the start- and end-date in legacy mode with <code>Date</code>-types,
     *            <code>false</code> to write start- and end-time properties along with the full-time flag
     * @return The task
     * @throws OXException
     */
    public Task getTask(final TimeZone timeZone, boolean useLegacyDates) throws OXException {
        JSONObject json = (JSONObject) getData();
        if (useLegacyDates) {
            json = new JSONObject(json);
            json.remove(CalendarFields.FULL_TIME);
            json.remove(TaskFields.START_TIME);
            json.remove(TaskFields.END_TIME);
        }
        return parseTask(json, timeZone);
    }

    private Task parseTask(JSONObject json, TimeZone timeZone) throws OXException {
        Task task = new Task();
        new TaskParser(true, timeZone).parse(task, json, Locale.ENGLISH);
        return task;
    }

}
