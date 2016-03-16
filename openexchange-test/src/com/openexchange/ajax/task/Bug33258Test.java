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
import java.io.IOException;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TaskExceptionCode;

/**
 * Verifies that creating tasks with priority having other values than 1,2 or 3 is not possible.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Bug33258Test extends AbstractAJAXSession {

    private AJAXClient client1;
    private TimeZone timeZone;
    private Task task;

    public Bug33258Test(String name) {
        super(name);
    }

    @Before
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client1 = getClient();
        timeZone = client1.getValues().getTimeZone();
        task = new Task();
        task.setParentFolderID(client1.getValues().getPrivateTaskFolder());
        task.setTitle("Test for bug 33258");
        client1.execute(new InsertRequest(task, timeZone)).fillTask(task);
    }

    @After
    @Override
    protected void tearDown() throws Exception {
        client1.execute(new DeleteRequest(task));
        super.tearDown();
    }

    @Test
    public void testForVerifiedPriority() throws OXException, IOException, JSONException {
        Task test = TaskTools.valuesForUpdate(task);
        for (int priority : new int[] { Task.LOW, Task.NORMAL, Task.HIGH } ) {
            test.setPriority(I(priority));
            UpdateResponse response = client1.execute(new UpdateRequest(test, timeZone, false));
            if (!response.hasError()) {
                test.setLastModified(response.getTimestamp());
                task.setLastModified(response.getTimestamp());
            }
            assertFalse("Priority value " + priority + " should work.", response.hasError());
            GetResponse getResponse = client1.execute(new GetRequest(test));
            test = getResponse.getTask(timeZone);
            assertTrue("Task should contain a priority.", test.containsPriority());
            assertEquals("Written priority should be equal to read one.", I(priority), test.getPriority());
        }
        for (int priority : new int[] { Task.LOW-1, Task.HIGH+1 }) {
            test.setPriority(I(priority));
            UpdateResponse response = client1.execute(new UpdateRequest(test, timeZone, false));
            if (!response.hasError()) {
                test.setLastModified(response.getTimestamp());
                task.setLastModified(response.getTimestamp());
            }
            assertTrue("Priority value " + priority + " should not work.", response.hasError());
            assertTrue("Did not get an exception about an invalid priority value.", response.getException().similarTo(TaskExceptionCode.INVALID_PRIORITY.create(I(priority))));
        }
        {
            test.removePriority();
            UpdateResponse response = client1.execute(new UpdateRequest(test, timeZone) {
                @Override
                public JSONObject getBody() throws JSONException {
                    JSONObject json = super.getBody();
                    json.put("priority", JSONObject.NULL);
                    return json;
                }
            });
            test.setLastModified(response.getTimestamp());
            task.setLastModified(response.getTimestamp());
            GetResponse getResponse = client1.execute(new GetRequest(test));
            test = getResponse.getTask(timeZone);
            assertFalse("Task should not contain a priority.", test.containsPriority());
        }
    }
}
