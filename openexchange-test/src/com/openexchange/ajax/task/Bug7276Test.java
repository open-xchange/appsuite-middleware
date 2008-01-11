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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.participant.ParticipantTools;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TaskException;

/**
 * Tests problem described in bug #7276.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Bug7276Test extends AbstractTaskTest {

    private AJAXClient client1;

    private AJAXClient client2;

    /**
     * @param name
     */
    public Bug7276Test(final String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client1 = getClient();
        client2 = new AJAXClient(AJAXClient.User.User2);
    }

    /**
     * Tests if bug #7276 appears again.
     * @throws Throwable if this test fails.
     */
    public void testBug() throws Throwable {
        // User 1 inserts task.
        Task task = Create.createWithDefaults();
        task.setTitle("Test bug #7276");
        task.setParentFolderID(client1.getValues().getPrivateTaskFolder());
        task.setParticipants(ParticipantTools.createParticipants(client1
            .getValues().getUserId(), client2.getValues().getUserId()));
        final InsertResponse iResponse = TaskTools.insert(client1,
            new InsertRequest(task, client1.getValues().getTimeZone()));
        final int taskId = iResponse.getId();
        // User 2 checks if he can see it.
        TaskTools.get(client2, new GetRequest(client2.getValues()
            .getPrivateTaskFolder(), taskId));
        // User 1 modifies the task and removes participant User 2
        final GetResponse gResponse1 = TaskTools.get(client1, new GetRequest(
            iResponse));
        task = gResponse1.getTask(client1.getValues().getTimeZone());
        task.setParticipants(ParticipantTools.createParticipants(client1
            .getValues().getUserId()));
        final UpdateResponse uResponse1 = TaskTools.update(client1,
            new UpdateRequest(task, client1.getValues().getTimeZone()));
        task.setLastModified(uResponse1.getTimestamp());
        // Now User 2 tries to load the task again.
        final GetResponse gResponse2 = TaskTools.get(client2, new GetRequest(
            client2.getValues().getPrivateTaskFolder(), taskId, false));
        assertTrue("Server does not give exception although it has to.",
            gResponse2.hasError());
        final TaskException.Code code = TaskException.Code.NO_PERMISSION;
        final AbstractOXException exc = gResponse2.getException();
        assertEquals("Wrong exception message.", Component.TASK, exc.getComponent());
        assertEquals("Wrong exception message.", code.getCategory(), exc.getCategory());
        assertEquals("Wrong exception message.", code.getNumber(), exc.getDetailNumber());
        // Clean up
        TaskTools.delete(client1, new DeleteRequest(task));
    }
}
