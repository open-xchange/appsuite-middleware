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

import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.fields.TaskFields;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.common.groupware.tasks.Create;

/**
 * Checks if the problem described in bug 8935 appears again.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Bug8935Test extends AbstractTaskTest {

    private AJAXClient client;

    /**
     * Default constructor.
     *
     * @param name name of the test.
     */
    public Bug8935Test() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
    }

    /**
     * Checks if a task can be created with a title of "null";
     *
     * @throws Throwable if an exception occurs.
     */
    @Test
    public void testNull() throws Throwable {
        final Task task = Create.createWithDefaults();
        task.setTitle("null");
        task.setParentFolderID(client.getValues().getPrivateTaskFolder());
        final InsertResponse iResponse = client.execute(new InsertRequest(task, client.getValues().getTimeZone()));
        final GetResponse gResponse = client.execute(new GetRequest(iResponse));
        final Task reload = gResponse.getTask(client.getValues().getTimeZone());
        TaskTools.compareAttributes(task, reload);
        client.execute(new DeleteRequest(reload));
    }

    @Test
    public void testRealNull() throws Throwable {
        final Task task = Create.createWithDefaults();
        task.removeTitle();
        task.setParentFolderID(client.getValues().getPrivateTaskFolder());
        final InsertResponse iResponse = client.execute(new SpecialInsertRequest(task, client.getValues().getTimeZone()));
        final GetResponse gResponse = client.execute(new GetRequest(iResponse));
        final Task reload = gResponse.getTask(client.getValues().getTimeZone());
        TaskTools.compareAttributes(task, reload);
        client.execute(new DeleteRequest(reload));
    }

    private class SpecialInsertRequest extends InsertRequest {

        public SpecialInsertRequest(final Task task, final TimeZone timeZone) {
            super(task, timeZone);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public JSONObject getBody() throws JSONException {
            final JSONObject json = super.getBody();
            json.put(TaskFields.TITLE, JSONObject.NULL);
            return json;
        }
    }

    @Test
    public void testEmptyString() throws Throwable {
        final Task task = Create.createWithDefaults();
        // Empty string must be interpreted as null.
        task.setTitle("");
        task.setParentFolderID(client.getValues().getPrivateTaskFolder());
        final InsertResponse iResponse = client.execute(new SpecialInsertRequest(task, client.getValues().getTimeZone()));
        // remove it because server won't sent empty fields.
        task.removeTitle();
        final GetResponse gResponse = client.execute(new GetRequest(iResponse));
        final Task reload = gResponse.getTask(client.getValues().getTimeZone());
        TaskTools.compareAttributes(task, reload);
        client.execute(new DeleteRequest(reload));
    }
}
