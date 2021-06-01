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

import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.framework.MultipleRequest;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.common.groupware.tasks.Create;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class InsertTest extends AbstractTaskTest {

    /**
     * @param name
     */
    public InsertTest() {
        super();
    }

    /**
     * Tests inserting a private task.
     *
     * @throws Throwable if an error occurs.
     */
    @Test
    public void testInsertPrivateTask() throws Throwable {
        final AJAXClient client = getClient();
        final int folderId = client.getValues().getPrivateTaskFolder();
        final TimeZone timeZone = client.getValues().getTimeZone();
        final Task task = Create.createTask();
        task.setParentFolderID(folderId);
        final InsertResponse insertR = client.execute(new InsertRequest(task, timeZone));
        final GetResponse getR = client.execute(new GetRequest(insertR));
        final Task reload = getR.getTask(timeZone);
        TaskTools.compareAttributes(task, reload);
        client.execute(new DeleteRequest(reload));
    }

    /**
     * Tests inserting a private task.
     *
     * @throws Throwable if an error occurs.
     */
    public void _testInsertTonnenTasks() throws Throwable {
        final AJAXClient client = getClient();
        final int folderId = client.getValues().getPrivateTaskFolder();
        final TimeZone timeZone = client.getValues().getTimeZone();
        final InsertRequest[] inserts = new InsertRequest[1000];
        for (int i = 0; i < inserts.length; i++) {
            final Task task = Create.createTask();
            task.setParentFolderID(folderId);
            inserts[i] = new InsertRequest(task, timeZone);
        }
        Executor.execute(client, MultipleRequest.create(inserts));
    }
}
