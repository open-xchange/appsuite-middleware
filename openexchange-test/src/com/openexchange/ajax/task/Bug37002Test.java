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
import com.openexchange.ajax.framework.Abstrac2UserAJAXSession;
import com.openexchange.ajax.task.actions.ConfirmWithTaskInParametersRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.common.groupware.tasks.Create;

/**
 * {@link Bug37002Test} verifies that changing some task state does not
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug37002Test extends Abstrac2UserAJAXSession {

    private Task task;
    private TimeZone timeZone;

    public Bug37002Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        Participant participant = new UserParticipant(client2.getValues().getUserId());

        timeZone = getClient().getValues().getTimeZone();
        task = Create.createWithDefaults(client1.getValues().getPrivateTaskFolder(), "Test for bug 37002");
        task.addParticipant(participant);
        client1.execute(new InsertRequest(task, timeZone, true)).fillTask(task);

        Task client2Task = Create.cloneForUpdate(task);
        client2Task.setParentFolderID(client2.getValues().getPrivateTaskFolder());
        client2.execute(new ConfirmWithTaskInParametersRequest(client2Task, Task.ACCEPT, "Will do the stuff.")).fillTask(task, client2Task);

        Task done = Create.cloneForUpdate(client2Task);
        done.addParticipant(participant); // OX6 frontend sends all participants again
        done.setStatus(Task.DONE);
        client2.execute(new UpdateRequest(done, timeZone)).fillTask(task, client2Task, done);
    }

    @Test
    public void testForLostConfirmStatus() throws OXException, IOException, JSONException {
        Task test = client1.execute(new GetRequest(task)).getTask(timeZone);
        UserParticipant[] participants = test.getUsers();
        assertEquals("Number of task participants should be one.", 1, participants.length);
        UserParticipant participant = participants[0];
        assertEquals("Confirmation status got lost.", Task.ACCEPT, participant.getConfirm());
        assertEquals("Confirm message got lost.", "Will do the stuff.", participant.getConfirmMessage());
    }
}
