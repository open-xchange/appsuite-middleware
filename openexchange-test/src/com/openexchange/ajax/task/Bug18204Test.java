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

import static org.junit.Assert.assertFalse;
import java.util.Calendar;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 *
 */
public class Bug18204Test extends AbstractAJAXSession {

    AJAXClient client;
    TimeZone tz;
    Calendar start;
    Calendar due;
    Task task;

    public Bug18204Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        tz = client.getValues().getTimeZone();
        start = TimeTools.createCalendar(tz);
        due = (Calendar) start.clone();
        due.add(Calendar.HOUR_OF_DAY, 2);
        task = createTask();
    }

    @Test
    public void testBug18204() throws Exception {
        // Insert new recurring task with end of series set to _after_
        InsertResponse insertResponse = client.execute(new InsertRequest(task, tz, true));
        insertResponse.fillTask(task);

        // Modify task to end of series set to _on_
        task.removeOccurrence();
        due.add(Calendar.DAY_OF_MONTH, 4);
        task.setUntil(due.getTime());
        UpdateResponse updateResponse = client.execute(new UpdateRequest(task, tz, true));
        task.setLastModified(updateResponse.getTimestamp());

        // Get Task to compare
        GetResponse getResponse = client.execute(new GetRequest(task));
        Task toCompare = getResponse.getTask(tz);

        assertFalse("Task contains Occurrences although it should not.", toCompare.containsOccurrence());
    }

    private Task createTask() throws Exception {
        Task task = new Task();
        task.setTitle("Bug18204 Task");
        task.setParentFolderID(client.getValues().getPrivateTaskFolder());
        task.setRecurrenceType(Task.DAILY);
        task.setInterval(1);
        task.setOccurrence(2);
        task.setStartDate(start.getTime());
        task.setEndDate(due.getTime());

        return task;
    }

}
