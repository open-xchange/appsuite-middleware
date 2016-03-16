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

import static com.openexchange.java.Autoboxing.I;
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
import com.openexchange.exception.OXException.Generic;
import com.openexchange.groupware.tasks.Create;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TaskExceptionCode;

/**
 * Tests problem described in bug #7276.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Bug7276Test extends AbstractTaskTest {

    private AJAXClient client2;
	private Generic expected;

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
        client2 = new AJAXClient(AJAXClient.User.User2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void tearDown() throws Exception {
        client2.logout();
        super.tearDown();
    }

    /**
     * Tests if bug #7276 appears again.
     * @throws Throwable if this test fails.
     */
    public void testBug() throws Throwable {
        final AJAXClient client1 = getClient();
        final int folder2 = client2.getValues().getPrivateTaskFolder();
        // User 1 inserts task.
        Task task = Create.createWithDefaults();
        task.setTitle("Test bug #7276");
        task.setParentFolderID(client1.getValues().getPrivateTaskFolder());
        task.setParticipants(ParticipantTools.createParticipants(client1
            .getValues().getUserId(), client2.getValues().getUserId()));
        {
            final InsertResponse response = client1.execute(new InsertRequest(task, client1.getValues().getTimeZone()));
            response.fillTask(task);
        }
        // User 2 checks if he can see it.
        TaskTools.get(client2, new GetRequest(folder2, task.getObjectID()));
        // User 1 modifies the task and removes participant User 2
        {
            final GetResponse response = TaskTools.get(client1, new GetRequest(
                task.getParentFolderID(), task.getObjectID()));
            task = response.getTask(client1.getValues().getTimeZone());
        }
        task.setParticipants(ParticipantTools.createParticipants(client1
            .getValues().getUserId()));
        {
            final UpdateResponse response = TaskTools.update(client1,
                new UpdateRequest(task, client1.getValues().getTimeZone()));
            task.setLastModified(response.getTimestamp());
        }
        // Now User 2 tries to load the task again.
        {
            final GetResponse response = TaskTools.get(client2, new GetRequest(
                folder2, task.getObjectID(), false));
            assertTrue("Server does not give exception although it has to.",
                response.hasError());
            OXException expectedErr = TaskExceptionCode.NO_PERMISSION.create(I(0), "", I(0));
            OXException actual= response.getException();
            assertTrue("Wrong exception", actual.similarTo(expectedErr));
        }
        // Clean up
        client1.execute(new DeleteRequest(task));
    }
}
