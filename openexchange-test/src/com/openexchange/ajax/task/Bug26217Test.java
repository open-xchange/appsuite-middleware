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

import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.java.util.TimeZones;
import com.openexchange.test.common.groupware.calendar.TimeTools;
import com.openexchange.test.common.groupware.tasks.Create;

/**
 * This test ensures that bug 26217 does not occur again. The bug mentions a problem that moving a task creating in the private default
 * folder can not be moved to some private sub folder and back.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Bug26217Test extends AbstractTaskTest {

    private AJAXClient client;
    private Task task;
    private TimeZone tz;
    private FolderObject moveTo;

    public Bug26217Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        tz = getTimeZone();
        task = Create.createWithDefaults(getPrivateFolder(), "Task to test for bug 26217");
        Calendar cal = TimeTools.createCalendar(TimeZones.UTC);
        cal.set(Calendar.HOUR, 0);
        task.setStartDate(cal.getTime());
        task.setEndDate(cal.getTime());
        InsertRequest request = new InsertRequest(task, tz);
        InsertResponse response = client.execute(request);
        response.fillTask(task);

        moveTo = com.openexchange.ajax.folder.Create.createPrivateFolder("Bug 26217 test", FolderObject.TASK, client.getValues().getUserId());
        moveTo.setParentFolderID(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
        com.openexchange.ajax.folder.actions.InsertResponse response2 = client.execute(new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_OLD, moveTo));
        moveTo.setObjectID(response2.getId());
        moveTo.setLastModified(response2.getTimestamp());
    }

    @Test
    public void testForBug() throws OXException, IOException, JSONException {
        Task update = TaskTools.valuesForUpdate(task);
        update.setParentFolderID(moveTo.getObjectID());
        UpdateResponse response = client.execute(new UpdateRequest(getPrivateFolder(), update, tz));
        if (response.hasError()) {
            fail("Moving task failed: " + response.getErrorMessage());
        }
        update.setLastModified(response.getTimestamp());
        update.setParentFolderID(getPrivateFolder());
        GetResponse testResponse = client.execute(new GetRequest(moveTo.getObjectID(), task.getObjectID()));
        if (testResponse.hasError()) {
            fail("Reading task after moving failed: " + testResponse.getErrorMessage());
        }
        response = client.execute(new UpdateRequest(moveTo.getObjectID(), update, tz));
        if (response.hasError()) {
            fail("Moving task failed: " + response.getErrorMessage());
        }
        update.setLastModified(response.getTimestamp());
        task.setLastModified(response.getTimestamp());
        testResponse = client.execute(new GetRequest(getPrivateFolder(), task.getObjectID()));
        if (testResponse.hasError()) {
            fail("Reading task after moving failed: " + testResponse.getErrorMessage());
        }
    }
}
