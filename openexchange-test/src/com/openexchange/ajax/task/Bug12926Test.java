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
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.participant.ParticipantTools;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.common.groupware.tasks.Create;

/**
 * {@link Bug12926Test}
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug12926Test extends AbstractTaskTest {

    private AJAXClient client;

    private int userId;

    private int folderId;

    private TimeZone tz;

    private Task task;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        userId = client.getValues().getUserId();
        folderId = client.getValues().getPrivateTaskFolder();
        tz = client.getValues().getTimeZone();
        task = Create.createWithDefaults(folderId, "Bug 12926 test task");
        final List<Participant> participants = ParticipantTools.getParticipants(client, 1, userId);
        participants.add(new UserParticipant(userId));
        task.setParticipants(participants);
        final InsertResponse insertR = client.execute(new InsertRequest(task, tz));
        insertR.fillTask(task);
    }

    @Test
    public void testTaskStaysInDelegatorFolder() throws OXException, IOException, JSONException {
        final Task task2 = new Task();
        task2.setObjectID(task.getObjectID());
        task2.setParentFolderID(task.getParentFolderID());
        task2.setLastModified(task.getLastModified());
        final List<Participant> participants = new ArrayList<Participant>();
        for (final Participant participant : task.getParticipants()) {
            if (participant.getIdentifier() != userId) {
                participants.add(participant);
            }
        }
        task2.setParticipants(participants);
        final UpdateResponse uResponse = client.execute(new UpdateRequest(task, tz));
        task.setLastModified(uResponse.getTimestamp());
        final GetResponse getR = client.execute(new GetRequest(task.getParentFolderID(), task.getObjectID(), false));
        if (getR.hasError()) {
            fail(getR.getException().getMessage());
        }
    }
}
