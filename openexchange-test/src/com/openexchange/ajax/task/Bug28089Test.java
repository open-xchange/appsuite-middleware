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
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.tasks.Create;
import com.openexchange.groupware.tasks.Task;

/**
 * Verifies that the problem described in bug 28089 does not appear again. The issue is a SQL problem due to different connections used to
 * delete reminder.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Bug28089Test extends AbstractTaskTest {

    private AJAXClient client1;
    private TimeZone tz;
    private Task task;
    private FolderObject folder;

    public Bug28089Test(String name) {
        super(name);
    }

    @Before
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client1 = getClient();
        tz = getTimeZone();
        // Create public folder
        folder = com.openexchange.ajax.folder.Create.setupPublicFolder("Folder for test for bug 28089", FolderObject.TASK, client1.getValues().getUserId());
        folder.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
        CommonInsertResponse response = client1.execute(new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_OLD, folder));
        folder.setObjectID(response.getId());
        folder.setLastModified(response.getTimestamp());

        task = Create.createWithDefaults(folder.getObjectID(), "Task to test for bug 28089");
        task.setAlarm(new Date());
        client1.execute(new InsertRequest(task, tz)).fillTask(task);
    }

    @After
    @Override
    protected void tearDown() throws Exception {
        GetResponse response = client1.execute(new com.openexchange.ajax.folder.actions.GetRequest(EnumAPI.OX_OLD, folder.getObjectID(), false));
        if (!response.hasError()) {
            client1.execute(new DeleteRequest(task));
            folder.setLastModified(response.getTimestamp());
            client1.execute(new com.openexchange.ajax.folder.actions.DeleteRequest(EnumAPI.OX_OLD, folder));
        }
        super.tearDown();
    }

    @Test
    public void testForBug() throws OXException, IOException, JSONException {
        CommonDeleteResponse response = client1.execute(new com.openexchange.ajax.folder.actions.DeleteRequest(EnumAPI.OX_OLD, folder));
        List<OXException> warnings = response.getResponse().getWarnings();
        String message;
        try {
            message = warnings.get(0).getMessage();
        } catch (IndexOutOfBoundsException e) {
            message = "Response contains warnings.";
        }
        Assert.assertTrue(message, warnings.isEmpty());
    }
}
