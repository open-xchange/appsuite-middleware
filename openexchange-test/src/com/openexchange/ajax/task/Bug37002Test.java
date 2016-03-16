/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.ajax.task;

import java.io.IOException;
import java.util.TimeZone;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.task.actions.ConfirmWithTaskInParametersRequest;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.tasks.Create;
import com.openexchange.groupware.tasks.Task;

/**
 * {@link Bug37002Test} verifies that changing some task state does not
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug37002Test extends AbstractAJAXSession {

    private AJAXClient client1, client2;
    private Task task;
    private TimeZone timeZone;

    public Bug37002Test(String name) {
        super(name);
    }

    @Before
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client1 = getClient();
        client2 = new AJAXClient(User.User2);
        Participant participant = new UserParticipant(client2.getValues().getUserId());

        timeZone = client.getValues().getTimeZone();
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

    @After
    @Override
    protected void tearDown() throws Exception {
        client1.execute(new DeleteRequest(task));
        super.tearDown();
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
