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

import static com.openexchange.ajax.task.TaskTools.valuesForUpdate;
import static org.junit.Assert.assertFalse;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.common.groupware.tasks.Create;

/**
 * This test ensures that bug 22305 does not occur again.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug22305Test extends AbstractTaskTest {

    private AJAXClient anton, berta;
    private int antonId, bertaId;
    private TimeZone bertaTZ;
    private Task task, bertaTask;

    public Bug22305Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        anton = getClient();
        antonId = anton.getValues().getUserId();
        berta = testUser2.getAjaxClient();
        bertaId = berta.getValues().getUserId();
        bertaTZ = berta.getValues().getTimeZone();
        task = Create.createWithDefaults(getPrivateFolder(), "Task to test for bug 22305");
        task.addParticipant(new UserParticipant(antonId));
        task.addParticipant(new UserParticipant(bertaId));
        InsertRequest request = new InsertRequest(task, getTimeZone());
        InsertResponse response = anton.execute(request);
        response.fillTask(task);
        bertaTask = valuesForUpdate(task, berta.getValues().getPrivateTaskFolder());
        bertaTask.addParticipant(new UserParticipant(bertaId));
        UpdateRequest uReq = new UpdateRequest(bertaTask, bertaTZ);
        UpdateResponse uResp = berta.execute(uReq);
        task.setLastModified(uResp.getTimestamp());
        bertaTask.setLastModified(uResp.getTimestamp());
    }

    @Test
    public void testConfirmWithIdOnlyInBody() throws Throwable {
        bertaTask = valuesForUpdate(bertaTask);
        bertaTask.setNote("Update to test for NullPointerException");
        UpdateRequest request = new UpdateRequest(bertaTask, bertaTZ, false);
        UpdateResponse response = berta.execute(request);
        assertFalse(response.hasError());
        bertaTask.setLastModified(response.getTimestamp());
    }
}
