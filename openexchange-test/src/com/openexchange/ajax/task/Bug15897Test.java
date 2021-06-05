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
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.task.actions.ConfirmResponse;
import com.openexchange.ajax.task.actions.ConfirmWith2IdsRequest;
import com.openexchange.ajax.task.actions.ConfirmWithTaskInBodyRequest;
import com.openexchange.ajax.task.actions.ConfirmWithTaskInParametersRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.common.groupware.tasks.Create;

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

    public Bug15897Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        client2 = testUser2.getAjaxClient();
        task = Create.createWithDefaults(getPrivateFolder(), "Task to test bug 15897");
        participantId = client2.getValues().getUserId();
        task.addParticipant(new UserParticipant(participantId));
        InsertRequest request = new InsertRequest(task, getTimeZone());
        InsertResponse response = client.execute(request);
        response.fillTask(task);
    }

    @Test
    public void testConfirmWithIdOnlyInBody() throws Throwable {
        String message = "Task identifier in body of confirm request.";
        ConfirmWithTaskInBodyRequest request = new ConfirmWithTaskInBodyRequest(task, Task.TENTATIVE, message);
        ConfirmResponse response = client2.execute(request);
        task.setLastModified(response.getTimestamp());
        checkConfirmation(message, Task.TENTATIVE);
    }

    @Test
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
    @Test
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
