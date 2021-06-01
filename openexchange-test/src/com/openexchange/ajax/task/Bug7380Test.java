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

import java.util.List;
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
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.tasks.Task;

/**
 * Tests problem described in bug #7380.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Bug7380Test extends AbstractTaskTest {

    /**
     * @param name
     */
    public Bug7380Test() {
        super();
    }

    /**
     * Tests if bug #7380 appears again.
     *
     * @throws Throwable if this test fails.
     */
    @Test
    public void testBug() throws Throwable {
        final AJAXClient client = getClient();
        final Task task = new Task();
        task.setTitle("Test bug #7380");
        task.setParentFolderID(getPrivateFolder());
        final List<Participant> participants = ParticipantTools.getParticipants(getClient(), 1, true, client.getValues().getUserId());
        task.setParticipants(participants);
        final InsertResponse iResponse = client.execute(new InsertRequest(task, client.getValues().getTimeZone()));
        task.setObjectID(iResponse.getId());
        final GetResponse gResponse = client.execute(new GetRequest(getPrivateFolder(), task.getObjectID()));
        task.setLastModified(gResponse.getTimestamp());
        task.setParticipants((Participant[]) null);
        final UpdateResponse uResponse = client.execute(new UpdateRequest(task, client.getValues().getTimeZone()));
        task.setLastModified(uResponse.getTimestamp());
        client.execute(new DeleteRequest(task));
    }
}
