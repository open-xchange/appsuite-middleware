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
import org.json.JSONObject;
import com.openexchange.ajax.fields.TaskFields;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.ListRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tasks.Create;
import com.openexchange.groupware.tasks.Task;

/**
 * Target duration set to null.
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug14450Test extends AbstractTaskTest {

    private AJAXClient client;

    private int folderId;

    private TimeZone tz;

    private Task task;

    public Bug14450Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        folderId = client.getValues().getPrivateTaskFolder();
        tz = client.getValues().getTimeZone();
        task = Create.createWithDefaults(folderId, "Bug 14450 test task");
        final InsertResponse insertR = client.execute(new NullSendingInsertRequest(task, tz));
        insertR.fillTask(task);
    }

    @Override
    protected void tearDown() throws Exception {
        client.execute(new DeleteRequest(task));
        super.tearDown();
    }

    public void testGetRequest() throws OXException, IOException, JSONException, OXException {
        GetRequest request = new GetRequest(task.getParentFolderID(), task.getObjectID());
        GetResponse response = client.execute(request);
        Task toTest = response.getTask(tz);
        assertFalse("Task contains target duration but should not.", toTest.containsTargetDuration());
        assertEquals("Target duration has wrong value.", task.getTargetDuration(), toTest.getTargetDuration());
        assertFalse("Task contains actual duration but should not.", toTest.containsActualDuration());
        assertEquals("Actual duration has wrong value.", task.getActualDuration(), toTest.getActualDuration());
        assertFalse("Task contains target costs but should not.", toTest.containsTargetCosts());
        assertEquals("Target costs has wrong value.", task.getTargetCosts(), toTest.getTargetCosts());
        assertFalse("Task contains actual costs but should not.", toTest.containsActualCosts());
        assertEquals("Actual costs has wrong value.", task.getActualCosts(), toTest.getActualCosts());
    }

    public void testListRequest() throws OXException, IOException, JSONException {
        ListIDs ids = ListIDs.l(new int[] { task.getParentFolderID(), task.getObjectID() });
        ListRequest request = new ListRequest(ids, new int[] { Task.TARGET_DURATION, Task.ACTUAL_DURATION, Task.TARGET_COSTS, Task.ACTUAL_COSTS });
        CommonListResponse response = client.execute(request);
        Object targetDuration = response.getValue(0, Task.TARGET_DURATION);
        assertNull("Target duration should not be set.", targetDuration);
        Object actualDuration = response.getValue(0, Task.ACTUAL_DURATION);
        assertNull("Actual duration should not be set.", actualDuration);
        Object targetCosts = response.getValue(0, Task.TARGET_COSTS);
        assertNull("Target costs should not be set.", targetCosts);
        Object actualCosts = response.getValue(0, Task.ACTUAL_COSTS);
        assertNull("Actual costs should not be set.", actualCosts);
    }

    private static final class NullSendingInsertRequest extends InsertRequest {

        public NullSendingInsertRequest(Task task, TimeZone timeZone) {
            super(task, timeZone);
        }

        @Override
        public JSONObject getBody() throws JSONException {
            JSONObject json = super.getBody();
            json.put(TaskFields.TARGET_DURATION, JSONObject.NULL);
            json.put(TaskFields.ACTUAL_DURATION, JSONObject.NULL);
            json.put(TaskFields.TARGET_COSTS, JSONObject.NULL);
            json.put(TaskFields.ACTUAL_COSTS, JSONObject.NULL);
            return json;
        }
    }
}
