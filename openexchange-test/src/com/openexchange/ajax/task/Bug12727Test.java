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
import static org.junit.Assert.fail;
import java.io.IOException;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.ajax.task.actions.ListRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.TaskTestManager;
import com.openexchange.test.common.groupware.tasks.TestTask;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug12727Test extends AbstractTaskTestForAJAXClient {

    private AJAXClient client;

    private TaskTestManager manager;

    private TestTask task;

    /**
     * Default constructor.
     *
     * @param name test name.
     */
    public Bug12727Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        manager = new TaskTestManager(client);
        createTask();
    }

    @Test
    public void testOccurrences() throws OXException, IOException, JSONException {
        final ListRequest request = new ListRequest(ListIDs.l(new int[] { task.getParentFolderID(), task.getObjectID()
        }), new int[]
        { Task.FOLDER_ID, Task.OBJECT_ID, Task.RECURRENCE_COUNT }, false);
        final CommonListResponse response = client.execute(request);
        if (response.hasError()) {
            fail(response.getException().toString());
        }
        final int columnPos = response.getColumnPos(Task.RECURRENCE_COUNT);
        for (final Object[] data : response) {
            assertEquals("Column with recurrence count is missing.", columnPos + 1, data.length);
            assertEquals("Occurrences does not match.", Integer.valueOf(5), data[columnPos]);
        }
    }

    private void createTask() throws OXException, IOException, JSONException {
        task = getNewTask("Test for bug 12727");
        task.startsToday();
        task.endsTheFollowingDay();
        task.setParentFolderID(client.getValues().getPrivateTaskFolder());
        task.everyDay();
        task.occurs(5);
        manager.insertTaskOnServer(task);
    }
}
