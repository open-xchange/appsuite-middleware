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

import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.TaskFields;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.groupware.tasks.Create;
import com.openexchange.groupware.tasks.Task;

/**
 * Checks if the problem described in bug 8935 appears again.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Bug8935Test extends AbstractTaskTest {

    private AJAXClient client;

    /**
     * Default constructor.
     * @param name name of the test.
     */
    public Bug8935Test(final String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
    }

    /**
     * Checks if a task can be created with a title of "null";
     * @throws Throwable if an exception occurs.
     */
    public void testNull() throws Throwable {
        final Task task = Create.createWithDefaults();
        task.setTitle("null");
        task.setParentFolderID(client.getValues().getPrivateTaskFolder());
        final InsertResponse iResponse = client.execute(new InsertRequest(task, client.getValues().getTimeZone()));
        final GetResponse gResponse = TaskTools.get(client,
            new GetRequest(iResponse));
        final Task reload = gResponse.getTask(client.getValues().getTimeZone());
        TaskTools.compareAttributes(task, reload);
        client.execute(new DeleteRequest(reload));
    }

    public void testRealNull() throws Throwable {
        final Task task = Create.createWithDefaults();
        task.removeTitle();
        task.setParentFolderID(client.getValues().getPrivateTaskFolder());
        final InsertResponse iResponse = client.execute(new SpecialInsertRequest(task, client.getValues().getTimeZone()));
        final GetResponse gResponse = TaskTools.get(client,
            new GetRequest(iResponse));
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

    public void testEmptyString() throws Throwable {
        final Task task = Create.createWithDefaults();
        // Empty string must be interpreted as null.
        task.setTitle("");
        task.setParentFolderID(client.getValues().getPrivateTaskFolder());
        final InsertResponse iResponse = client.execute(new SpecialInsertRequest(task, client.getValues().getTimeZone()));
        // remove it because server won't sent empty fields.
        task.removeTitle();
        final GetResponse gResponse = TaskTools.get(client,
            new GetRequest(iResponse));
        final Task reload = gResponse.getTask(client.getValues().getTimeZone());
        TaskTools.compareAttributes(task, reload);
        client.execute(new DeleteRequest(reload));
    }
}
