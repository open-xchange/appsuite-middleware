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

import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.task.actions.ConfirmResponse;
import com.openexchange.ajax.task.actions.ConfirmWith2IdsRequest;
import com.openexchange.ajax.task.actions.ConfirmWithTaskInBodyRequest;
import com.openexchange.ajax.task.actions.ConfirmWithTaskInParametersRequest;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.tasks.Create;
import com.openexchange.groupware.tasks.Task;

/**
 * This test verifies that all possible backwards compatible ways to confirm a task works.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug15897Test extends AbstractTaskTest {

    private AJAXClient client;
    private AJAXClient client2;
    private Task task;
    private int participantId;

    public Bug15897Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        client2 = new AJAXClient(User.User2);
        task = Create.createWithDefaults(getPrivateFolder(), "Task to test bug 15897");
        participantId = client2.getValues().getUserId();
        task.addParticipant(new UserParticipant(participantId));
        InsertRequest request = new InsertRequest(task, getTimeZone());
        InsertResponse response = client.execute(request);
        response.fillTask(task);
    }

    @Override
    protected void tearDown() throws Exception {
        DeleteRequest request = new DeleteRequest(task);
        client.execute(request);
        super.tearDown();
    }

    public void testConfirmWithIdOnlyInBody() throws Throwable {
        String message = "Task identifier in body of confirm request.";
        ConfirmWithTaskInBodyRequest request = new ConfirmWithTaskInBodyRequest(task, Task.TENTATIVE, message);
        ConfirmResponse response = client2.execute(request);
        task.setLastModified(response.getTimestamp());
        checkConfirmation(message, Task.TENTATIVE);
    }

    public void testConfirmWithIdOnlyInURL() throws Throwable {
        String message = "Task identifier in URL parameters of confirm request.";
        ConfirmWithTaskInParametersRequest request = new ConfirmWithTaskInParametersRequest(task, Task.DECLINE, message);
        ConfirmResponse response = client2.execute(request);
        task.setLastModified(response.getTimestamp());
        checkConfirmation(message, Task.DECLINE);
    }

    /**
     * Backend must prefer identifier in URL parameters.
     */
    public void testConfirmWithIdInBodyAndURL() throws Throwable {
        String message = "Task identifier in URL parameters and body contains nonsense identifier.";
        ConfirmWith2IdsRequest request = new ConfirmWith2IdsRequest(task, Integer.MIN_VALUE, Task.ACCEPT, message);
        ConfirmResponse response = client2.execute(request);
        task.setLastModified(response.getTimestamp());
        checkConfirmation(message, Task.ACCEPT);
    }

    private void checkConfirmation(String confirmMessage, int confirmStatus) throws Throwable {
        GetRequest request = new GetRequest(task);
        GetResponse response = client.execute(request);
        Task test = response.getTask(getTimeZone());
        UserParticipant[] participants = test.getUsers();
        assertEquals("Number of participants does not match.", 1, participants.length);
        UserParticipant participant = participants[0];
        assertEquals("Participant does not match.", participantId, participant.getIdentifier());
        assertEquals("Confirmation message does not match.", confirmMessage, participant.getConfirmMessage());
        assertEquals("Confirmation status does not match.", confirmStatus, participant.getConfirm());
    }
}
