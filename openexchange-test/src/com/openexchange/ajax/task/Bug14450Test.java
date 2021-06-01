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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import java.io.IOException;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.fields.TaskFields;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.ListRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.common.groupware.tasks.Create;

/**
 * Target duration set to null.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug14450Test extends AbstractTaskTest {

    private AJAXClient client;

    private int folderId;

    private TimeZone tz;

    private Task task;

    public Bug14450Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        folderId = client.getValues().getPrivateTaskFolder();
        tz = client.getValues().getTimeZone();
        task = Create.createWithDefaults(folderId, "Bug 14450 test task");
        final InsertResponse insertR = client.execute(new NullSendingInsertRequest(task, tz));
        insertR.fillTask(task);
    }

    @Test
    public void testGetRequest() throws OXException, IOException, JSONException {
        GetRequest request = new GetRequest(task.getParentFolderID(), task.getObjectID());
        GetResponse response = client.execute(request);
        Task toTest = response.getTask(tz);
        assertFalse("Task contains target duration but should not.", toTest.containsTargetDuration());
        assertEquals("Target duration has wrong value.", task.getTargetDuration(), toTest.getTargetDuration());
        assertFalse("Task contains actual duration but should not.", toTest.containsActualDuration());
        assertEquals("Actual duration has wrong value.", task.getActualDuration(), toTest.getActualDuration());
        assertFalse("Task contains target costs but should not.", toTest.containsTargetCosts());
        assertEquals("Target costs has wrong value.", task.getTargetCosts(), toTest.getTargetCosts());
        assertFalse("Task contains actual costs but should not.", toTest.containsActualCosts());
        assertEquals("Actual costs has wrong value.", task.getActualCosts(), toTest.getActualCosts());
    }

    @Test
    public void testListRequest() throws OXException, IOException, JSONException {
        ListIDs ids = ListIDs.l(new int[] { task.getParentFolderID(), task.getObjectID() });
        ListRequest request = new ListRequest(ids, new int[] { Task.TARGET_DURATION, Task.ACTUAL_DURATION, Task.TARGET_COSTS, Task.ACTUAL_COSTS });
        CommonListResponse response = client.execute(request);
        Object targetDuration = response.getValue(0, Task.TARGET_DURATION);
        assertNull("Target duration should not be set.", targetDuration);
        Object actualDuration = response.getValue(0, Task.ACTUAL_DURATION);
        assertNull("Actual duration should not be set.", actualDuration);
        Object targetCosts = response.getValue(0, Task.TARGET_COSTS);
        assertNull("Target costs should not be set.", targetCosts);
        Object actualCosts = response.getValue(0, Task.ACTUAL_COSTS);
        assertNull("Actual costs should not be set.", actualCosts);
    }

    private static final class NullSendingInsertRequest extends InsertRequest {

        public NullSendingInsertRequest(Task task, TimeZone timeZone) {
            super(task, timeZone);
        }

        @Override
        public JSONObject getBody() throws JSONException {
            JSONObject json = super.getBody();
            json.put(TaskFields.TARGET_DURATION, JSONObject.NULL);
            json.put(TaskFields.ACTUAL_DURATION, JSONObject.NULL);
            json.put(TaskFields.TARGET_COSTS, JSONObject.NULL);
            json.put(TaskFields.ACTUAL_COSTS, JSONObject.NULL);
            return json;
        }
    }
}
