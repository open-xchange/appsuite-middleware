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
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.tasks.Task;

/**
 * Checks if group 0 works for tasks.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug11659Test extends AbstractTaskTest {

    /**
     * Default constructor.
     * 
     * @param name test name
     */
    public Bug11659Test() {
        super();
    }

    /**
     * Tries to create a task with group 0 as participant.
     */
    @Test
    public void testAllInternalUsersGroup() throws Throwable {
        final AJAXClient client = getClient();
        final int folderId = client.getValues().getPrivateTaskFolder();
        final TimeZone tz = client.getValues().getTimeZone();
        final Task task = new Task();
        task.setTitle("Bug 11659 test");
        task.setParentFolderID(folderId);
        task.setParticipants(new Participant[] { new GroupParticipant(0) });
        final InsertResponse iResponse = Executor.execute(client, new InsertRequest(task, tz));
        try {
            final GetResponse gResponse = Executor.execute(client, new GetRequest(iResponse));
            final Task reload = gResponse.getTask(tz);
            final Participant[] participants = reload.getParticipants();
            assertEquals("Participant number differs.", 1, participants.length);
            assertEquals("Participant is not group 0.", 0, participants[0].getIdentifier());
        } finally {
            Executor.execute(client, new DeleteRequest(iResponse));
        }
    }
}
