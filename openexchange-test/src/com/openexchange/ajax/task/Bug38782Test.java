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

import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TaskExceptionCode;
import com.openexchange.test.common.groupware.calendar.TimeTools;
import com.openexchange.test.common.groupware.tasks.Create;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link Bug38782Test}
 *
 * Verify that recurrence calculation works even if start and end date are missing in the object.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug38782Test extends AbstractAJAXSession {

    private AJAXClient client1;
    private TimeZone timeZone;
    private Task task;

    public Bug38782Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client1 = getClient();
        timeZone = getClient().getValues().getTimeZone();
        task = new Task();
        task.setParentFolderID(client1.getValues().getPrivateTaskFolder());
        task.setTitle("Test for bug 38782");
        task.setRecurrenceType(Task.DAILY);
        task.setInterval(1);
        task.setOccurrence(2);
        Calendar start = TimeTools.createCalendar(timeZone);
        task.setStartDate(start.getTime());
        start.add(Calendar.HOUR_OF_DAY, 2);
        task.setEndDate(start.getTime());
        client1.execute(new InsertRequest(task, timeZone, true)).fillTask(task);
    }

    @Test
    public void testGetAllTasks() throws Exception {
        Task test = Create.cloneForUpdate(task);
        test.setStartDate(null);
        test.setEndDate(null);
        UpdateResponse response = client1.execute(new UpdateRequest(test, timeZone, false));
        if (!response.hasError()) {
            task.setLastModified(response.getTimestamp());
            test = Create.cloneForUpdate(task);
            test.setStatus(Task.DONE);
            response = client1.execute(new UpdateRequest(test, timeZone, false));
            if (response.hasError()) {
                assertTrue("This is the expected NullPointerException from the bug report.", AjaxExceptionCodes.UNEXPECTED_ERROR.create().similarTo(response.getException()));
            }
            task.setLastModified(response.getTimestamp());
        } else {
            assertTrue("Expected exception for recurring tasks getting start and end date removed.", TaskExceptionCode.MISSING_RECURRENCE_VALUE.create().similarTo(response.getException()));
        }
    }
}
