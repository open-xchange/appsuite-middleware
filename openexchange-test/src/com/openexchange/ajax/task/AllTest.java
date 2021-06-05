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

import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.MultipleRequest;
import com.openexchange.ajax.framework.MultipleResponse;
import com.openexchange.ajax.task.actions.AllRequest;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.groupware.tasks.Task;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class AllTest extends AbstractTaskTest {

    private static final int NUMBER = 10;
    private AJAXClient client;

    public AllTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
    }

    @Test
    public void testAll() throws Throwable {
        final InsertRequest[] inserts = new InsertRequest[NUMBER];
        for (int i = 0; i < inserts.length; i++) {
            final Task task = new Task();
            task.setTitle("Task " + (i + 1));
            task.setParentFolderID(getPrivateFolder());
            task.setAlarm(new Date());
            // TODO add participants
            inserts[i] = new InsertRequest(task, getTimeZone());
        }
        final MultipleResponse<InsertResponse> mInsert = client.execute(MultipleRequest.create(inserts));
        final GetRequest[] gets = new GetRequest[NUMBER];
        for (int i = 0; i < gets.length; i++) {
            final InsertResponse ins = mInsert.getResponse(i);
            gets[i] = new GetRequest(ins);
        }
        final MultipleResponse<GetResponse> mGet = client.execute(MultipleRequest.create(gets));
        // TODO Read Task.ALARM
        final int[] columns = new int[] { Task.TITLE, Task.OBJECT_ID, Task.LAST_MODIFIED, Task.FOLDER_ID, Task.PARTICIPANTS };
        client.execute(new AllRequest(getPrivateFolder(), columns, 0, null));
        final DeleteRequest[] deletes = new DeleteRequest[inserts.length];
        for (int i = 0; i < inserts.length; i++) {
            final GetResponse get = mGet.getResponse(i);
            deletes[i] = new DeleteRequest(get.getTask(getTimeZone()));
        }
        client.execute(MultipleRequest.create(deletes));
    }
}
