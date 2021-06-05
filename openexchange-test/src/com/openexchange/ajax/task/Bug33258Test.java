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

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TaskExceptionCode;

/**
 * Verifies that creating tasks with priority having other values than 1,2 or 3 is not possible.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Bug33258Test extends AbstractAJAXSession {

    private AJAXClient client1;
    private TimeZone timeZone;
    private Task task;

    public Bug33258Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client1 = getClient();
        timeZone = client1.getValues().getTimeZone();
        task = new Task();
        task.setParentFolderID(client1.getValues().getPrivateTaskFolder());
        task.setTitle("Test for bug 33258");
        client1.execute(new InsertRequest(task, timeZone)).fillTask(task);
    }

    @Test
    public void testForVerifiedPriority() throws OXException, IOException, JSONException {
        Task test = TaskTools.valuesForUpdate(task);
        for (int priority : new int[] { Task.LOW, Task.NORMAL, Task.HIGH }) {
            test.setPriority(I(priority));
            UpdateResponse response = client1.execute(new UpdateRequest(test, timeZone, false));
            if (!response.hasError()) {
                test.setLastModified(response.getTimestamp());
                task.setLastModified(response.getTimestamp());
            }
            assertFalse("Priority value " + priority + " should work.", response.hasError());
            GetResponse getResponse = client1.execute(new GetRequest(test));
            test = getResponse.getTask(timeZone);
            assertTrue("Task should contain a priority.", test.containsPriority());
            assertEquals("Written priority should be equal to read one.", I(priority), test.getPriority());
        }
        for (int priority : new int[] { Task.LOW - 1, Task.HIGH + 1 }) {
            test.setPriority(I(priority));
            UpdateResponse response = client1.execute(new UpdateRequest(test, timeZone, false));
            if (!response.hasError()) {
                test.setLastModified(response.getTimestamp());
                task.setLastModified(response.getTimestamp());
            }
            assertTrue("Priority value " + priority + " should not work.", response.hasError());
            assertTrue("Did not get an exception about an invalid priority value.", response.getException().similarTo(TaskExceptionCode.INVALID_PRIORITY.create(I(priority))));
        }
        {
            test.removePriority();
            UpdateResponse response = client1.execute(new UpdateRequest(test, timeZone) {

                @Override
                public JSONObject getBody() throws JSONException {
                    JSONObject json = super.getBody();
                    json.put("priority", JSONObject.NULL);
                    return json;
                }
            });
            test.setLastModified(response.getTimestamp());
            task.setLastModified(response.getTimestamp());
            GetResponse getResponse = client1.execute(new GetRequest(test));
            test = getResponse.getTask(timeZone);
            assertFalse("Task should not contain a priority.", test.containsPriority());
        }
    }
}
