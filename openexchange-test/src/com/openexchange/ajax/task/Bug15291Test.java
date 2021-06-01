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
import java.io.IOException;
import java.util.TimeZone;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.group.actions.CreateRequest;
import com.openexchange.ajax.group.actions.CreateResponse;
import com.openexchange.ajax.participant.ParticipantTools;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.common.groupware.tasks.Create;

/**
 * {@link Bug15291Test}
 *
 * Verify that adding a group participant containing only a single member that is already participant of a task can be added to the task.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug15291Test extends AbstractAJAXSession {

    private AJAXClient client1;
    private final Group group = new Group();
    private Task task;
    private TimeZone timeZone;

    public Bug15291Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client1 = getClient();
        group.setSimpleName("GroupForTestingBug15291");
        group.setDisplayName("Group for testing bug 15291");
        Participant participant = ParticipantTools.getSomeParticipant(client1);
        group.setMember(new int[] { participant.getIdentifier() });
        CreateResponse response = client1.execute(new CreateRequest(group));
        response.fillGroup(group);

        timeZone = getClient().getValues().getTimeZone();
        task = Create.createWithDefaults(client1.getValues().getPrivateTaskFolder(), "Test for bug 15291");
        task.addParticipant(participant);
        client1.execute(new InsertRequest(task, timeZone, true)).fillTask(task);
    }

    @Test
    public void testAddGroupParticipant() throws OXException, IOException, JSONException {
        Task test = Create.cloneForUpdate(task);
        test.addParticipant(new GroupParticipant(group.getIdentifier()));
        UpdateResponse response = client1.execute(new UpdateRequest(test, timeZone));
        response.fillTask(task, test);
        Task test2 = client1.execute(new GetRequest(test)).getTask(timeZone);
        Participant[] participants = test2.getParticipants();
        assertEquals("Number of task participants should be one.", 1, participants.length);
        Participant participant = participants[0];
        assertEquals("Only participant should be the group participant.", group.getIdentifier(), participant.getIdentifier());
    }
}
