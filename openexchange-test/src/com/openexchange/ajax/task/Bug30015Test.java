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

package com.openexchange.ajax.task;

import static org.junit.Assert.assertNull;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.java.util.TimeZones;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * Verifies that the next created occurrence does not contain the task_completed attribute.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Bug30015Test extends AbstractAJAXSession {

    private AJAXClient client1;
    private TimeZone timeZone;
    private Calendar cal;
    private Task task;
    private Task first;
    private Task second;

    public Bug30015Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client1 = getClient();
        timeZone = client1.getValues().getTimeZone();
        cal = TimeTools.createCalendar(TimeZones.UTC);
        task = new Task();
        task.setParentFolderID(client1.getValues().getPrivateTaskFolder());
        task.setTitle("Test for bug 30015");
        cal.set(Calendar.HOUR_OF_DAY, 0);
        task.setStartDate(cal.getTime());
        task.setEndDate(cal.getTime());
        task.setRecurrenceType(Task.DAILY);
        task.setInterval(1);
        task.setOccurrence(2);
        client1.execute(new InsertRequest(task, timeZone)).fillTask(task);
    }

    @Test
    public void testForTaskCompletedAttributeInNextOccurrence() throws OXException, IOException, JSONException {
        first = new Task();
        first.setObjectID(task.getObjectID());
        first.setParentFolderID(task.getParentFolderID());
        first.setLastModified(task.getLastModified());
        first.setStatus(Task.DONE);
        first.setPercentComplete(100);
        first.setDateCompleted(cal.getTime());
        first.setLastModified(client1.execute(new UpdateRequest(first, timeZone)).getTimestamp());
        second = Bug21026Test.findNextOccurrence(client1, client1.execute(new GetRequest(first)).getTask(timeZone));
        second = getClient().execute(new GetRequest(second)).getTask(timeZone);
        Assert.assertFalse("Next occurrence of task must not contain the attribute 'date completed'.", second.containsDateCompleted());
        assertNull("Next occurrence of task must not contain the attribute 'date completed'.", second.getDateCompleted());
    }
}
