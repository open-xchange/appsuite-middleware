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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.MultipleRequest;
import com.openexchange.ajax.framework.MultipleResponse;
import com.openexchange.ajax.task.actions.AllRequest;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.ListRequest;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.tasks.Task;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class ListTest extends AbstractTaskTest {

    private static final int NUMBER = 10;
    private static final int DELETES = 2;
    private AJAXClient client;

    public ListTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
    }

    public void testTaskList() throws Throwable {
        final InsertRequest[] inserts = new InsertRequest[NUMBER];
        for (int i = 0; i < inserts.length; i++) {
            final Task task = new Task();
            task.setTitle("Task " + (i + 1));
            task.setParentFolderID(getPrivateFolder());
            inserts[i] = new InsertRequest(task, getTimeZone());
        }
        final MultipleResponse<InsertResponse> mInsert = client.execute(MultipleRequest.create(inserts));

        final int[][] tasks = new int[NUMBER][2];
        for (int i = 0; i < tasks.length; i++) {
            final InsertResponse insertR = mInsert.getResponse(i);
            tasks[i] = new int[] { insertR.getFolderId(), insertR.getId() };
        }
        final int[] columns = new int[] { Task.TITLE, Task.OBJECT_ID, Task.LAST_MODIFIED };
        final CommonListResponse listR = client.execute(new ListRequest(tasks, columns));
        final DeleteRequest[] deletes = new DeleteRequest[inserts.length];
        for (int i = 0; i < inserts.length; i++) {
            deletes[i] = new DeleteRequest(tasks[i][0], tasks[i][1], listR.getTimestamp());
        }
        client.execute(MultipleRequest.create(deletes));
    }

    public void oldRemovedObjectHandling() throws Throwable {
        final int[] columns = new int[] { Task.TITLE, Task.OBJECT_ID, Task.LAST_MODIFIED };
        final CommonListResponse listR = client.execute(new ListRequest(new int[][] { { getPrivateFolder(), Integer.MAX_VALUE } }, columns, false));
        assertTrue("No error when listing not existing object.", listR.hasError());
    }

    public void testRemovedObjectHandling() throws Throwable {
        final int folderA = client.getValues().getPrivateTaskFolder();
        final AJAXClient clientB = new AJAXClient(User.User2);
        final int folderB = clientB.getValues().getPrivateTaskFolder();
        // Create some tasks.
        final InsertRequest[] inserts = new InsertRequest[NUMBER];
        for (int i = 0; i < inserts.length; i++) {
            final Task task = new Task();
            task.setTitle("Task " + (i + 1));
            task.setParentFolderID(folderA);
            task.addParticipant(new UserParticipant(client.getValues().getUserId()));
            task.addParticipant(new UserParticipant(clientB.getValues().getUserId()));
            inserts[i] = new InsertRequest(task, getTimeZone());
        }
        final MultipleResponse<InsertResponse> mInsert = client.execute(MultipleRequest.create(inserts));
        final List<InsertResponse> toDelete = new ArrayList<InsertResponse>(NUMBER);
        final Iterator<InsertResponse> iter = mInsert.iterator();
        while (iter.hasNext()) {
            toDelete.add(iter.next());
        }
        // A now gets all of the folder.
        final int[] columns = new int[] { Task.TITLE, Task.OBJECT_ID, Task.FOLDER_ID };
        final CommonAllResponse allR = client.execute(new AllRequest(folderA, columns, Task.TITLE, Order.ASCENDING));

        // Now B deletes some of them.
        final DeleteRequest[] deletes1 = new DeleteRequest[DELETES];
        for (int i = 0; i < deletes1.length; i++) {
            final InsertResponse insertR = toDelete.remove((NUMBER - DELETES)/2 + i);
            deletes1[i] = new DeleteRequest(folderB, insertR.getId(), insertR.getTimestamp());
        }
        clientB.execute(MultipleRequest.create(deletes1));

        // List request of A must now not contain the deleted objects and give
        // no error.
        final CommonListResponse listR = client.execute(new ListRequest(allR.getListIDs(), columns, true));

        final DeleteRequest[] deletes2 = new DeleteRequest[toDelete.size()];
        for (int i = 0; i < deletes2.length; i++) {
            final InsertResponse insertR = toDelete.get(i);
            deletes2[i] = new DeleteRequest(insertR.getFolderId(), insertR.getId(), listR.getTimestamp());
        }
        client.execute(MultipleRequest.create(deletes2));
    }
}
