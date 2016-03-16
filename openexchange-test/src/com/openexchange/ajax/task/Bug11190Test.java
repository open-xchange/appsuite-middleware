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

package com.openexchange.ajax.task;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.xml.sax.SAXException;
import com.openexchange.ajax.fields.TaskFields;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tasks.Task;

public class Bug11190Test extends AbstractAJAXSession {

    public Bug11190Test(String name) {
        super(name);
    }

    /*
     * Changing a monthly recurring appointment in the GUI from one
     * style (every Xth day) to another (every Monday/Tuesday...)
     * results in a broken recurrence.
     */
    @Test
    public void testSwitchingBetweenMonthlyRecurrencePatternsShouldNotBreakRecurrence() throws OXException, IOException, SAXException, JSONException, OXException{
        AJAXClient ajaxClient = getClient();
        final TimeZone timezone = ajaxClient.getValues().getTimeZone();
        final int folderId = ajaxClient.getValues().getPrivateTaskFolder();

        //new task
        Task taskWithRecurrence = new Task();
        taskWithRecurrence.setTitle("Reproducing bug 11190");
        taskWithRecurrence.setParentFolderID(folderId);
        taskWithRecurrence.setStartDate(new Date());
        taskWithRecurrence.setEndDate(new Date());

        //...every three months:
        taskWithRecurrence.setRecurrenceType(Task.MONTHLY);
        taskWithRecurrence.setInterval(3);
        //...every second Monday
        taskWithRecurrence.setDays(Task.MONDAY);
        taskWithRecurrence.setDayInMonth(2);
        //send
        InsertRequest insertRequest = new InsertRequest(taskWithRecurrence, timezone);
        InsertResponse insertResponse = ajaxClient.execute(insertRequest);
        taskWithRecurrence.setLastModified(insertResponse.getTimestamp());

        try {
            //refresh task
            insertResponse.fillTask(taskWithRecurrence);
            //update task with new pattern for recurrence
            //...every two months
            taskWithRecurrence.setRecurrenceType(Task.MONTHLY);
            taskWithRecurrence.setInterval(2);
            //...every twelfth day
            taskWithRecurrence.setDayInMonth(12);
            // TODO The remove method clears the value in the object. If the value should be cleared over the AJAX interface it must be set to null. This is currently not possible with the int primitive type.
            taskWithRecurrence.removeDays(); //otherwise, the old value (Monday) will be kept, which then means "the twelfth Monday every two months" (which then is reduced to every 5th Monday)
            //send
            UpdateRequest updateRequest = new IntToNullSettingUpdateRequest(taskWithRecurrence,timezone);
            UpdateResponse updateResponse = ajaxClient.execute(updateRequest);
            taskWithRecurrence.setLastModified(updateResponse.getTimestamp());
            //get data
            GetRequest getRequest = new GetRequest(folderId, taskWithRecurrence.getObjectID());
            GetResponse getResponse = ajaxClient.execute(getRequest);
            Task resultingTask = getResponse.getTask(timezone);
            //compare
            assertEquals("Recurrence type does not match",Task.MONTHLY, resultingTask.getRecurrenceType());
            assertEquals("Recurrence interval does not match", 2, resultingTask.getInterval());
            assertEquals("Recurring day in month does not match", 12, resultingTask.getDayInMonth());
            assertEquals("Recurring days should not be set anymore", false, resultingTask.containsDays());
        } finally {
            DeleteRequest cleanUp = new DeleteRequest(taskWithRecurrence);
            ajaxClient.execute(cleanUp);
        }
    }

    private class IntToNullSettingUpdateRequest extends UpdateRequest {
        public IntToNullSettingUpdateRequest(Task task, TimeZone timeZone) {
            super(task, timeZone);
        }

        @Override
        public JSONObject getBody() throws JSONException {
            JSONObject obj = super.getBody();
            if (!getTask().containsDays() && getTask().containsDayInMonth()) {
                obj.put(TaskFields.DAYS, JSONObject.NULL);
            }
            return obj;
        }
    }
}
