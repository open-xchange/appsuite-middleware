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
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.ajax.task.actions.ListRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TestTask;
import com.openexchange.test.TaskTestManager;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug12727Test extends AbstractTaskTestForAJAXClient {

    private AJAXClient client;

    private TaskTestManager manager;

    private TestTask task;

    /**
     * Default constructor.
     * @param name test name.
     */
    public Bug12727Test(final String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        manager = new TaskTestManager(client);
        createTask();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void tearDown() throws Exception {
        manager.cleanUp();
        super.tearDown();
    }

    public void testOccurrences() throws OXException, IOException,
        SAXException, JSONException {
        final ListRequest request = new ListRequest(ListIDs.l(
            new int[] {
                task.getParentFolderID(), task.getObjectID()
            }),
            new int[] { Task.FOLDER_ID, Task.OBJECT_ID, Task.RECURRENCE_COUNT },
            false);
        final CommonListResponse response = client.execute(request);
        if (response.hasError()) {
            fail(response.getException().toString());
        }
        final int columnPos = response.getColumnPos(Task.RECURRENCE_COUNT);
        for(final Object[] data : response) {
            assertEquals("Column with recurrence count is missing.", columnPos + 1,
                data.length);
            assertEquals("Occurrences does not match.", Integer.valueOf(5), data[columnPos]);
        }
    }

    private void createTask() throws OXException, IOException, SAXException,
        JSONException {
        task = getNewTask("Test for bug 12727");
        task.startsToday();
        task.endsTheFollowingDay();
        task.setParentFolderID(client.getValues().getPrivateTaskFolder());
        task.everyDay();
        task.occurs(5);
        manager.insertTaskOnServer(task);
    }
}
