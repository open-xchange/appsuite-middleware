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
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.group.actions.CreateRequest;
import com.openexchange.ajax.group.actions.CreateResponse;
import com.openexchange.ajax.participant.ParticipantTools;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.tasks.Create;
import com.openexchange.groupware.tasks.Task;

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

    public Bug15291Test(String name) {
        super(name);
    }

    @Before
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client1 = getClient();
        group.setSimpleName("GroupForTestingBug15291");
        group.setDisplayName("Group for testing bug 15291");
        Participant participant = ParticipantTools.getSomeParticipant(client1);
        group.setMember(new int[] { participant.getIdentifier() });
        CreateResponse response = client1.execute(new CreateRequest(group));
        response.fillGroup(group);

        timeZone = client.getValues().getTimeZone();
        task = Create.createWithDefaults(client1.getValues().getPrivateTaskFolder(), "Test for bug 15291");
        task.addParticipant(participant);
        client1.execute(new InsertRequest(task, timeZone, true)).fillTask(task);
    }

    @After
    @Override
    protected void tearDown() throws Exception {
        client1.execute(new DeleteRequest(task));
        client1.execute(new com.openexchange.ajax.group.actions.DeleteRequest(group));
        super.tearDown();
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
