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
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.task.actions.ConfirmWithTaskInBodyRequest;
import com.openexchange.ajax.task.actions.ConfirmWithTaskInParametersRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.TaskTestManager;
import com.openexchange.test.common.groupware.tasks.TestTask;

/**
 * {@link ConfirmTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class ConfirmTest extends AbstractTaskTestForAJAXClient {

    private TaskTestManager manager;
    private int userId;
    private TestTask task;

    public ConfirmTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        manager = new TaskTestManager(getClient());
        task = getNewTask(this.getClass().getCanonicalName());

        userId = getClient().getValues().getUserId();
        task.addParticipant(new UserParticipant(userId));

        manager.insertTaskOnServer(task);

    }

    @Test
    public void testConfirmWithTaskInParameters() throws OXException, IOException, JSONException {
        ConfirmWithTaskInParametersRequest request = new ConfirmWithTaskInParametersRequest(task, Task.ACCEPT, "Confirmanize!");
        getClient().execute(request);

        checkTaskOnServer(Task.ACCEPT, "Confirmanize!");
    }

    @Test
    public void testConfirmWithTaskInBody() throws OXException, IOException, JSONException {
        ConfirmWithTaskInBodyRequest request = new ConfirmWithTaskInBodyRequest(task, Task.ACCEPT, "Confirmanize!");
        getClient().execute(request);

        checkTaskOnServer(Task.ACCEPT, "Confirmanize!");
    }

    private void checkTaskOnServer(int confirmmation, String message) {
        Task reloaded = manager.getTaskFromServer(task);

        boolean found = false;
        for (UserParticipant user : reloaded.getUsers()) {
            if (user.getIdentifier() == userId) {
                assertEquals(confirmmation, user.getConfirm());
                assertEquals(message, user.getConfirmMessage());
                found = true;
            }
        }

        assertTrue(found);

    }

}
