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
import java.util.Date;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.groupware.tasks.Task;

/**
 * {@link DeleteMultipleTaskTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class DeleteMultipleTaskTest extends AbstractAJAXSession {

    private Task task1, task2;
    private TimeZone timeZone;

    /**
     * Initializes a new {@link DeleteMultipleTaskTest}.
     *
     * @param name
     */
    public DeleteMultipleTaskTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        timeZone = getClient().getValues().getTimeZone();

        task1 = new Task();
        task1.setTitle("Test 1");
        task1.setStartDate(new Date());
        task1.setEndDate(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 2));
        task1.setParentFolderID(getClient().getValues().getPrivateTaskFolder());
        final InsertRequest insReq1 = new InsertRequest(task1, timeZone);
        final InsertResponse insRes1 = getClient().execute(insReq1);
        insRes1.fillTask(task1);

        task2 = new Task();
        task2.setTitle("Test 2");
        task2.setStartDate(new Date());
        task2.setEndDate(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 2));
        task2.setParentFolderID(getClient().getValues().getPrivateTaskFolder());
        final InsertRequest insReq2 = new InsertRequest(task2, timeZone);
        final InsertResponse insRes2 = getClient().execute(insReq2);
        insRes2.fillTask(task2);
    }

    @Test
    public void testDeleteMultiple() throws Exception {
        final int[] ids = new int[] { task1.getObjectID(), task2.getObjectID() };
        final DeleteRequest delReq = new DeleteRequest(getClient().getValues().getPrivateTaskFolder(), ids, new Date(System.currentTimeMillis() + 300000), true);
        final CommonDeleteResponse delRes = getClient().execute(delReq);
        assertFalse("Multiple delete failed: " + delRes.getErrorMessage(), delRes.hasError());
    }

}
