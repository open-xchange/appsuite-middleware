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
import java.util.Calendar;
import java.util.TimeZone;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.tasks.Create;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.java.util.TimeZones;

/**
 * This test ensures that bug 26217 does not occur again. The bug mentions a problem that moving a task creating in the private default
 * folder can not be moved to some private sub folder and back.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Bug26217Test extends AbstractTaskTest {

    private AJAXClient client;
    private Task task;
    private TimeZone tz;
    private FolderObject moveTo;

    public Bug26217Test(String name) {
        super(name);
    }

    @Before
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        tz = getTimeZone();
        task = Create.createWithDefaults(getPrivateFolder(), "Task to test for bug 26217");
        Calendar cal = TimeTools.createCalendar(TimeZones.UTC);
        cal.set(Calendar.HOUR, 0);
        task.setStartDate(cal.getTime());
        task.setEndDate(cal.getTime());
        InsertRequest request = new InsertRequest(task, tz);
        InsertResponse response = client.execute(request);
        response.fillTask(task);

        moveTo = com.openexchange.ajax.folder.Create.createPrivateFolder("Bug 26217 test", FolderObject.TASK, client.getValues().getUserId());
        moveTo.setParentFolderID(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
        com.openexchange.ajax.folder.actions.InsertResponse response2 = client.execute(
            new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_OLD, moveTo));
        moveTo.setObjectID(response2.getId());
        moveTo.setLastModified(response2.getTimestamp());
    }

    @After
    @Override
    protected void tearDown() throws Exception {
        client.execute(new DeleteRequest(task));
        client.execute(new com.openexchange.ajax.folder.actions.DeleteRequest(EnumAPI.OX_OLD, moveTo));
        super.tearDown();
    }

    @Test
    public void testForBug() throws OXException, IOException, JSONException {
        Task update = TaskTools.valuesForUpdate(task);
        update.setParentFolderID(moveTo.getObjectID());
        UpdateResponse response = client.execute(new UpdateRequest(getPrivateFolder(), update, tz));
        if (response.hasError()) {
            fail("Moving task failed: " + response.getErrorMessage());
        }
        update.setLastModified(response.getTimestamp());
        update.setParentFolderID(getPrivateFolder());
        GetResponse testResponse = client.execute(new GetRequest(moveTo.getObjectID(), task.getObjectID()));
        if (testResponse.hasError()) {
            fail("Reading task after moving failed: " + testResponse.getErrorMessage());
        }
        response = client.execute(new UpdateRequest(moveTo.getObjectID(), update, tz));
        if (response.hasError()) {
            fail("Moving task failed: " + response.getErrorMessage());
        }
        update.setLastModified(response.getTimestamp());
        task.setLastModified(response.getTimestamp());
        testResponse = client.execute(new GetRequest(getPrivateFolder(), task.getObjectID()));
        if (testResponse.hasError()) {
            fail("Reading task after moving failed: " + testResponse.getErrorMessage());
        }
    }
}
