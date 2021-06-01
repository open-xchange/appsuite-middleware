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

import static org.junit.Assert.assertFalse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.fields.ParticipantsFields;
import com.openexchange.ajax.fields.TaskFields;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.common.groupware.tasks.Create;

/**
 * Checks if external participants contain identifier 0 in JSON.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug11397Test extends AbstractTaskTest {

    /**
     * Default constructor.
     *
     * @param name test name
     */
    public Bug11397Test() {
        super();
    }

    /**
     * Checks if external participant does not contain identifier 0.
     *
     * @throws Throwable if some problem occurs.
     */
    @Test
    public void testExternalParticipant() throws Throwable {
        final AJAXClient client = getClient();
        Task task = Create.createWithDefaults(getPrivateFolder(), "Bug 11397 test");
        task.setParticipants(new Participant[] { new ExternalUserParticipant("test@example.org")
        });
        final InsertResponse insertR = client.execute(new InsertRequest(task, getTimeZone()));
        try {
            final GetResponse getR = client.execute(new GetRequest(insertR));
            task = getR.getTask(getTimeZone());
            final JSONObject json = (JSONObject) getR.getData();
            final JSONArray partArray = json.getJSONArray(TaskFields.PARTICIPANTS);
            final JSONObject partJSON = partArray.getJSONObject(0);
            assertFalse("External participant contains identifier.", partJSON.has(ParticipantsFields.ID));
        } finally {
            client.execute(new DeleteRequest(insertR));
        }
    }
}
