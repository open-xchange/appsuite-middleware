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
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.common.groupware.tasks.Create;

/**
 * Updates a task and adds an external participant.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug11619Test extends AbstractTaskTest {

    /**
     * Default constructor.
     *
     * @param name test name
     */
    public Bug11619Test() {
        super();
    }

    /**
     * Creates and updates a task and adds on updating an external participant.
     *
     * @throws Throwable if some problem occurs.
     */
    @Test
    public void testExternalParticipant() throws Throwable {
        final AJAXClient client = getClient();
        final TimeZone tz = getTimeZone();
        Task task = Create.createWithDefaults(getPrivateFolder(), "Bug 11619 test");
        final InsertResponse insertR = client.execute(new InsertRequest(task, tz));
        try {
            {
                final GetRequest request = new GetRequest(insertR);
                final GetResponse response = client.execute(request);
                task = response.getTask(tz);
            }
            {
                task.setParticipants(new Participant[] { new ExternalUserParticipant("test@example.org")
                });
                final UpdateRequest request = new UpdateRequest(task, tz);
                final UpdateResponse response = client.execute(request);
                task.setLastModified(response.getTimestamp());
            }
            {
                final GetRequest request = new GetRequest(insertR);
                final GetResponse response = client.execute(request);
                task = response.getTask(tz);
                assertTrue("External participant get lost.", task.getParticipants().length > 0);
            }
        } finally {
            final GetRequest request = new GetRequest(insertR);
            final GetResponse response = client.execute(request);
            task = response.getTask(tz);
            client.execute(new DeleteRequest(task));
        }
    }
}
