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
