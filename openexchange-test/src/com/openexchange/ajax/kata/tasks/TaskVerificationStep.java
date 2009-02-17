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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.ajax.kata.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import org.json.JSONException;
import org.xml.sax.SAXException;

import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.ajax.kata.NeedExistingStep;
import com.openexchange.ajax.task.actions.AllRequest;
import com.openexchange.ajax.task.actions.ListRequest;
import com.openexchange.ajax.task.actions.UpdatesRequest;
import com.openexchange.api.OXConflictException;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.TaskTestManager;
import com.openexchange.tools.servlet.AjaxException;


/**
 * {@link TaskVerificationStep}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class TaskVerificationStep extends NeedExistingStep<Task> {

    private Task entry;
    private TaskTestManager manager;

    /**
     * Initializes a new {@link TaskVerificationStep}.
     * @param name
     * @param expectedError
     */
    public TaskVerificationStep(Task entry, String name, String expectedError) {
        super(name, expectedError);
        this.entry = entry;
    }

    public void perform(AJAXClient client) throws Exception {
        this.client = client;
        this.manager = new TaskTestManager(client);
        assumeIdentity(entry);
        checkWithReadMethods(entry);
    }

    private void checkWithReadMethods(Task task) throws OXException, JSONException, AjaxException, IOException, SAXException {
        checkViaGet(task);
        checkViaAll(task);
        checkViaList(task);
        checkViaUpdates(task);
    }

    private void checkViaGet(Task task) throws OXException, JSONException {
        Task loaded = manager.getTaskFromServer(task);
        compare(task, loaded);
    }

    private void checkViaAll(Task task) throws AjaxException, IOException, SAXException, JSONException {
        Object[][] rows = getViaAll(task);

        checkInList(task, rows, Task.ALL_COLUMNS);
    }

    private void checkViaList(Task task) throws AjaxException, IOException, SAXException, JSONException {
        ListRequest listRequest = new ListRequest(
            ListIDs.l(new int[] { task.getParentFolderID(), task.getObjectID() }),
            Task.ALL_COLUMNS);
        CommonListResponse response = client.execute(listRequest);

        Object[][] rows = response.getArray();

        checkInList(task, rows, Task.ALL_COLUMNS);
    }

    private void checkViaUpdates(Task task) throws AjaxException, IOException, SAXException, JSONException, OXConflictException {
        UpdatesRequest updates = new UpdatesRequest(task.getParentFolderID(), Task.ALL_COLUMNS, Task.OBJECT_ID, Order.ASCENDING, new Date(0));
        AbstractAJAXResponse response = client.execute(updates);

        //TODO: List<Task> tasks = response.getData()

       // checkInList(task, tasks);

    }

    private Object[][] getViaAll(Task task) throws AjaxException, IOException, SAXException, JSONException {
        long rangeStart = task.getStartDate().getTime() - 24*3600000;
        long rangeEnd = task.getEndDate().getTime() + 24*3600000;
        AllRequest all = new AllRequest(0, null, 0, null); 
        CommonAllResponse response = client.execute(all);
        return response.getArray();
    }

    private void compare(Task task, Task loaded) {
        int[] columns = Task.ALL_COLUMNS;
        for (int i = 0; i < columns.length; i++) {
            int col = columns[i];
            if (col == DataObject.LAST_MODIFIED_UTC || col == DataObject.LAST_MODIFIED) {
                continue;
            }
            if (task.contains(col)) {
                assertEquals(name+": Column "+ col + " differs!", task.get(col), loaded.get(col));
            }
        }
    }

    private void checkInList(Task task, Object[][] rows, int[] columns) throws AjaxException, IOException, SAXException, JSONException {
        int idPos = findIDIndex(columns);

        for (int i = 0; i < rows.length; i++) {
            Object[] row = rows[i];
            int id = (Integer) row[idPos];
            if (id == task.getObjectID()) {
                compare(task, row, columns);
                return;
            }
        }

        fail("Object not found in response. " + name);

    }

    private void compare(Task task, Object[] row, int[] columns) throws AjaxException, IOException, SAXException, JSONException {
        assertEquals(row.length, columns.length);
        for (int i = 0; i < columns.length; i++) {
            int column = columns[i];
            if (column == DataObject.LAST_MODIFIED_UTC || column == DataObject.LAST_MODIFIED) {
                continue;
            }
            if (task.contains(column)) {
                Object expected = task.get(column);
                Object actual = row[i];
                actual = transform(column, actual);
                assertEquals(name + " Column: " + column, expected, actual);
            }
        }
    }

    private void checkInList(Task task, List<Task> tasks) {
        for (Task taskFromList : tasks) {
            if (taskFromList.getObjectID() == task.getObjectID()) {
                compare(task, taskFromList);
                return;
            }
        }

        fail("Object not found in response. " + name);
    }

    private int findIDIndex(int[] columns) {
        for (int i = 0; i < columns.length; i++) {
            if (columns[i] == Task.OBJECT_ID) {
                return i;
            }
        }
        fail("No ID column requested. This won't work. " + name);
        return -1;
    }

    private Object transform(int column, Object actual) throws AjaxException, IOException, SAXException, JSONException {
        switch (column) {
        case Task.START_DATE:
        case Task.END_DATE:
            int offset = getTimeZone().getOffset((Long) actual);
            return new Date((Long) actual - offset);
        }

        return actual;
    }

    public void cleanUp() throws Exception {
    }


}
