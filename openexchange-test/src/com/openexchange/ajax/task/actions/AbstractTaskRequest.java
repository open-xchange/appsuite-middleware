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
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.ajax.fields.TaskFields;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.Header;
import com.openexchange.ajax.writer.TaskWriter;
import com.openexchange.groupware.tasks.Task;

/**
 * Shared information for all task requests.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class AbstractTaskRequest<T extends AbstractAJAXResponse> implements AJAXRequest<T> {

    /**
     * URL of the tasks AJAX interface.
     */
    public static final String TASKS_URL = "/ajax/tasks";

    public static final int[] GUI_COLUMNS = new int[] { Task.OBJECT_ID, Task.FOLDER_ID };

    protected AbstractTaskRequest() {
        super();
    }

    @Override
    public String getServletPath() {
        return TASKS_URL;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

    protected JSONObject convert(Task task, TimeZone timeZone) throws JSONException {
        JSONObject json = convertCommon(task, timeZone);
        json.remove(TaskFields.START_TIME);
        json.remove(TaskFields.END_TIME);
        json.remove(CalendarFields.FULL_TIME);
        return json;
    }

    protected JSONObject convertNew(Task task, TimeZone timeZone) throws JSONException {
        JSONObject json = convertCommon(task, timeZone);
        json.remove(TaskFields.START_DATE);
        json.remove(TaskFields.END_DATE);
        return json;
    }

    private JSONObject convertCommon(Task task, TimeZone timeZone) throws JSONException {
        JSONObject retval = new JSONObject();
        new TaskWriter(timeZone).writeTask(task, retval);
        // Add explicit values for start and end date if they are set and null
        // this may have to be put somewhere else.
        if (task.getStartDate() == null && task.containsStartDate()) {
            retval.put(TaskFields.START_DATE, JSONObject.NULL);
        }
        if (task.getEndDate() == null && task.containsEndDate()) {
            retval.put(TaskFields.END_DATE, JSONObject.NULL);
        }
        return retval;
    }

    public static int[] addGUIColumns(final int[] columns) {
        final List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < columns.length; i++) {
            list.add(Integer.valueOf(columns[i]));
        }
        // Move GUI_COLUMNS to end.
        for (int i = 0; i < GUI_COLUMNS.length; i++) {
            final Integer column = Integer.valueOf(GUI_COLUMNS[i]);
            if (!list.contains(column)) {
                list.add(column);
            }
        }
        final int[] retval = new int[list.size()];
        for (int i = 0; i < retval.length; i++) {
            retval[i] = list.get(i).intValue();
        }
        return retval;
    }
}
