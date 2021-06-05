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
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.participant.ParticipantTools;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TaskExceptionCode;
import com.openexchange.test.common.groupware.tasks.Create;
import com.openexchange.test.common.test.TestClassConfig;

/**
 * Tests problem described in bug #7276.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Bug7276Test extends AbstractTaskTest {

    private AJAXClient client2;

    /**
     * @param name
     */
    public Bug7276Test() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client2 = testUser2.getAjaxClient();
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().withUserPerContext(2).createAjaxClient().build();
    }

    /**
     * Tests if bug #7276 appears again.
     *
     * @throws Throwable if this test fails.
     */
    @Test
    public void testBug() throws Throwable {
        final int folder2 = client2.getValues().getPrivateTaskFolder();
        // User 1 inserts task.
        Task task = Create.createWithDefaults();
        task.setTitle("Test bug #7276");
        task.setParentFolderID(getClient().getValues().getPrivateTaskFolder());
        task.setParticipants(ParticipantTools.createParticipants(getClient().getValues().getUserId(), client2.getValues().getUserId()));
        {
            final InsertResponse response = getClient().execute(new InsertRequest(task, getClient().getValues().getTimeZone()));
            response.fillTask(task);
        }
        // User 2 checks if he can see it.
        client2.execute(new GetRequest(folder2, task.getObjectID()));
        // User 1 modifies the task and removes participant User 2
        {
            final GetResponse response = getClient().execute(new GetRequest(task.getParentFolderID(), task.getObjectID()));
            task = response.getTask(getClient().getValues().getTimeZone());
        }
        task.setParticipants(ParticipantTools.createParticipants(getClient().getValues().getUserId()));
        {
            final UpdateResponse response = getClient().execute(new UpdateRequest(task, getClient().getValues().getTimeZone()));
            task.setLastModified(response.getTimestamp());
        }
        // Now User 2 tries to load the task again.
        {
            final GetResponse response = client2.execute(new GetRequest(folder2, task.getObjectID(), false));
            assertTrue("Server does not give exception although it has to.", response.hasError());
            OXException expectedErr = TaskExceptionCode.NO_PERMISSION.create(I(0), I(0));
            OXException actual = response.getException();
            assertTrue("Wrong exception", actual.similarTo(expectedErr));
        }
        // Clean up
        getClient().execute(new DeleteRequest(task));
    }
}
