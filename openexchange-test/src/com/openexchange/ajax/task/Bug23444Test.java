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

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.java.util.TimeZones;
import com.openexchange.test.common.groupware.calendar.TimeTools;
import com.openexchange.test.common.groupware.tasks.Create;

/**
 * This test ensures that bug 23444 does not occur again. The bug describes the problem that the recurrence information can not be removed
 * from a task. Settings recurrence_type to 0 is not working to remove the recurrence
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Bug23444Test extends AbstractTaskTest {

    private AJAXClient client;
    private Task task;
    private TimeZone tz;

    public Bug23444Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        tz = getTimeZone();
        task = Create.createWithDefaults(getPrivateFolder(), "Task to test for bug 23444");
        Calendar cal = TimeTools.createCalendar(TimeZones.UTC);
        cal.set(Calendar.HOUR, 0);
        task.setStartDate(cal.getTime());
        task.setEndDate(cal.getTime());
        task.setRecurrenceType(Task.DAILY);
        task.setInterval(1);
        InsertRequest request = new InsertRequest(task, tz);
        InsertResponse response = client.execute(request);
        response.fillTask(task);
    }

    @Test
    public void testForBug() throws OXException, IOException, JSONException {
        Task update = TaskTools.valuesForUpdate(task);
        update.setRecurrenceType(Task.NO_RECURRENCE);
        UpdateRequest request = new UpdateRequest(update, tz);
        client.execute(request);
        GetResponse response = client.execute(new GetRequest(task, tz));
        Task test = response.getTask(tz);
        task.setLastModified(test.getLastModified());
        assertEquals("Series information can not be removed from a task.", Task.NO_RECURRENCE, test.getRecurrenceType());
    }
}
