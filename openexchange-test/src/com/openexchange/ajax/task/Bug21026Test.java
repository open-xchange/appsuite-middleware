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

import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.java.Autoboxing.l;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.json.JSONException;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.task.actions.AllRequest;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.java.util.TimeZones;

/**
 * Checks if a series limited by occurrences does not just continue endlessly by unfinishing and finishing the last task.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug21026Test extends AbstractAJAXSession {

    @SuppressWarnings("hiding")
    private AJAXClient client;
    private Task first;
    private Task second;
    private Task third;
    private TimeZone timeZone;

    public Bug21026Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        timeZone = client.getValues().getTimeZone();
        final Task task = new Task();
        task.setParentFolderID(client.getValues().getPrivateTaskFolder());
        task.setTitle("Test for bug 21026");
        final Calendar cal = TimeTools.createCalendar(TimeZones.UTC);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        task.setStartDate(cal.getTime());
        task.setEndDate(cal.getTime());
        task.setRecurrenceType(Task.DAILY);
        task.setInterval(1);
        task.setOccurrence(2);
        client.execute(new InsertRequest(task, timeZone)).fillTask(task);
        first = new Task();
        first.setObjectID(task.getObjectID());
        first.setParentFolderID(task.getParentFolderID());
        first.setLastModified(task.getLastModified());
        first.setStatus(Task.DONE);
        first.setPercentComplete(100);
        first.setLastModified(client.execute(new UpdateRequest(first, timeZone)).getTimestamp());
        second = findNextOccurrence(client, task);
    }

    @Override
    protected void tearDown() throws Exception {
        if (null != third) {
            client.execute(new DeleteRequest(third));
        }
        client.execute(new DeleteRequest(second));
        client.execute(new DeleteRequest(first));
        super.tearDown();
    }

    public void testNoFurtherOccurrence() throws Exception {
        assertNotNull("Second occurrence not found.", second);
        assertEquals("Occurrences of first task is wrong.", 2, client.execute(new GetRequest(first.getParentFolderID(), first.getObjectID())).getTask(timeZone).getOccurrence());
        assertEquals("Occurrences of second task is wrong.", 1, client.execute(new GetRequest(second.getParentFolderID(), second.getObjectID())).getTask(timeZone).getOccurrence());

        // Set last occurrence to finished.
        final Task finish = new Task();
        finish.setObjectID(second.getObjectID());
        finish.setParentFolderID(second.getParentFolderID());
        finish.setLastModified(second.getLastModified());
        finish.setStatus(Task.DONE);
        finish.setPercentComplete(100);
        finish.setLastModified(client.execute(new UpdateRequest(finish, timeZone)).getTimestamp());
        second.setLastModified(finish.getLastModified());
        third = findNextOccurrence(client, second);
        assertNull("No next occurrence should be created.", third);

        // Set last occurrence to not finished.
        finish.setStatus(Task.NOT_STARTED);
        finish.setPercentComplete(0);
        finish.setLastModified(client.execute(new UpdateRequest(finish, timeZone)).getTimestamp());
        second.setLastModified(finish.getLastModified());

        finish.setStatus(Task.DONE);
        finish.setPercentComplete(100);
        finish.setLastModified(client.execute(new UpdateRequest(finish, timeZone)).getTimestamp());
        second.setLastModified(finish.getLastModified());

        third = findNextOccurrence(client, second);
        assertNull("No next occurrence should be created.", third);
    }

    public static Task findNextOccurrence(AJAXClient client, Task previous) throws IOException, JSONException, OXException {
        final CommonAllResponse response = client.execute(new AllRequest(previous.getParentFolderID(), new int[] { Task.OBJECT_ID, Task.TITLE, Task.RECURRENCE_COUNT, Task.START_DATE, Task.END_DATE, Task.LAST_MODIFIED_UTC }, 0, null));
        Task retval = null;
        Calendar cal = new GregorianCalendar(TimeZones.UTC);
        cal.setTime(previous.getEndDate());
        cal.add(Calendar.DATE, 1);
        for (Object[] data : response) {
            if (previous.getTitle().equals(data[response.getColumnPos(Task.TITLE)])
                && previous.getObjectID() != i((Integer) data[response.getColumnPos(Task.OBJECT_ID)])
                && cal.getTimeInMillis() == l((Long) data[response.getColumnPos(Task.END_DATE)])) {
                retval = new Task();
                retval.setObjectID(i((Integer) data[response.getColumnPos(Task.OBJECT_ID)]));
                retval.setParentFolderID(previous.getParentFolderID());
                retval.setTitle((String) data[response.getColumnPos(Task.TITLE)]);
                final Integer occurrences = (Integer) data[response.getColumnPos(Task.RECURRENCE_COUNT)];
                if (null != occurrences) {
                    retval.setOccurrence(i(occurrences));
                }
                retval.setStartDate(new Date(l((Long) data[response.getColumnPos(Task.START_DATE)])));
                retval.setEndDate(new Date(l((Long) data[response.getColumnPos(Task.END_DATE)])));
                retval.setLastModified(new Date(l((Long) data[response.getColumnPos(Task.LAST_MODIFIED_UTC)])));
                break;
            }
        }
        return retval;
    }
}
