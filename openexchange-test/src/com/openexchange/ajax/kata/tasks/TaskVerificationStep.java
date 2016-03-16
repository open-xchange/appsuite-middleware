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

package com.openexchange.ajax.kata.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.ajax.kata.NeedExistingStep;
import com.openexchange.ajax.kata.appointments.ParticipantComparisonFailure;
import com.openexchange.ajax.kata.appointments.UserParticipantComparisonFailure;
import com.openexchange.ajax.task.actions.AllRequest;
import com.openexchange.ajax.task.actions.ListRequest;
import com.openexchange.ajax.task.actions.SearchRequest;
import com.openexchange.ajax.task.actions.SearchResponse;
import com.openexchange.ajax.task.actions.TaskUpdatesResponse;
import com.openexchange.ajax.task.actions.UpdatesRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.TaskTestManager;


/**
 * {@link TaskVerificationStep}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 *
 */
public class TaskVerificationStep extends NeedExistingStep<Task> {

    private final Task entry;
    private TaskTestManager manager;
    private int expectedFolderId;

    /**
     * Initializes a new {@link TaskVerificationStep}.
     * @param name
     * @param expectedError
     */
    public TaskVerificationStep(Task entry, String name) {
        super(name, null);
        this.entry = entry;
    }

    @Override
    protected void assumeIdentity(Task thing) {
        expectedFolderId = entry.getParentFolderID();
        boolean containsFolderId = entry.containsParentFolderID();
        super.assumeIdentity(entry);
        if( ! containsFolderId ){
            expectedFolderId = entry.getParentFolderID();
        }
    }

    @Override
    public void perform(AJAXClient client) throws Exception {
        this.client = client;
        this.manager = new TaskTestManager(client);
        assumeIdentity(entry);
        checkWithReadMethods(entry);
    }

    private void checkWithReadMethods(Task task) throws OXException, JSONException, OXException, IOException, SAXException {
        checkViaGet(task);
        checkViaAll(task);
        checkViaList(task);
        checkViaUpdates(task);
        checkViaSearch(task);
    }

    private void checkViaGet(Task task) throws OXException, JSONException {
        Task loaded = manager.getTaskFromServer(expectedFolderId, task.getObjectID());
        compare(task, loaded);
    }

    private void checkViaAll(Task task) throws OXException, IOException, SAXException, JSONException {
        Object[][] rows = getViaAll(task);

        checkInList(task, rows, Task.ALL_COLUMNS);
    }

    private void checkViaList(Task task) throws OXException, IOException, SAXException, JSONException {
        ListRequest listRequest = new ListRequest(
            ListIDs.l(new int[] { expectedFolderId, task.getObjectID() }),
            Task.ALL_COLUMNS);
        CommonListResponse response = client.execute(listRequest);

        Object[][] rows = response.getArray();

        checkInList(task, rows, Task.ALL_COLUMNS);
    }

    private void checkViaUpdates(Task task) throws OXException, IOException, SAXException, JSONException, OXException {
        UpdatesRequest updates = new UpdatesRequest(expectedFolderId, Task.ALL_COLUMNS, Task.OBJECT_ID, Order.ASCENDING, new Date(0), getTimeZone());
        TaskUpdatesResponse response = client.execute(updates);

        List<Task> tasks = response.getTasks();
        checkInList(task, tasks);

    }

    private void checkViaSearch(Task task) throws OXException, IOException, SAXException, JSONException{
        Object[][] rows = getViaSearch(task);
        checkInList(task, rows, Task.ALL_COLUMNS);
    }

    private Object[][] getViaAll(Task task) throws OXException, IOException, SAXException, JSONException {
        AllRequest all = new AllRequest(expectedFolderId, Task.ALL_COLUMNS, Task.OBJECT_ID, Order.ASCENDING);
        CommonAllResponse response = client.execute(all);
        return response.getArray();
    }

    private Object[][] getViaSearch(Task task) throws OXException, IOException, SAXException, JSONException{
        TaskSearchObject searchObject = new TaskSearchObject();
        searchObject.addFolder(expectedFolderId);
        searchObject.setPattern("*");
        SearchRequest searchRequest = new SearchRequest(searchObject, Task.ALL_COLUMNS);
        SearchResponse searchResponse = client.execute(searchRequest);
        return searchResponse.getArray();
    }

    private void compare(Task task, Task loaded) {
        int[] columns = Task.ALL_COLUMNS;
        for (int i = 0; i < columns.length; i++) {
            int col = columns[i];
            if (col == DataObject.LAST_MODIFIED_UTC || col == DataObject.LAST_MODIFIED) {
                continue;
            }
            if (col == CalendarObject.PARTICIPANTS && task.containsParticipants()) {
                Participant[] expected = task.getParticipants();
                Participant[] actual = loaded.getParticipants();
                if (!compareArrays(expected, actual)) {

                    throw new ParticipantComparisonFailure("", expected, actual);
                }
                continue;
            }
            if (col == CalendarObject.USERS && task.containsUserParticipants()) {
                UserParticipant[] expected = task.getUsers();
                UserParticipant[] actual = loaded.getUsers();
                if (!compareArrays(expected, actual)) {
                    throw new UserParticipantComparisonFailure("", expected, actual);
                }
                continue;
            }
            if (task.contains(col)) {
                assertEquals(name+": Column "+ col + " differs!", task.get(col), loaded.get(col));
            }
        }
    }

    private void checkInList(Task task, Object[][] rows, int[] columns) throws OXException, IOException, SAXException, JSONException {
        int idPos = findIDIndex(columns);

        for (int i = 0; i < rows.length; i++) {
            Object[] row = rows[i];
            int id = ( (Integer) row[idPos] ).intValue();
            if (id == task.getObjectID()) {
                compare(task, row, columns);
                return;
            }
        }

        fail("Object not found in response. " + name);

    }

    private void compare(Task task, Object[] row, int[] columns) throws OXException, IOException, SAXException, JSONException {
        for (int i = 0; i < columns.length; i++) {
            int column = columns[i];
            if (column == DataObject.LAST_MODIFIED_UTC || column == DataObject.LAST_MODIFIED) {
                continue;
            }

            if (column == CalendarObject.PARTICIPANTS && task.containsParticipants()) {
                Participant[] expected = task.getParticipants();
                Participant[] actual = (Participant[]) transform(column, row[i]);
                if (!compareArrays(expected, actual)) {
                    throw new ParticipantComparisonFailure("", expected, actual);
                }
                continue;
            }
            if (column == CalendarObject.USERS && task.containsUserParticipants()) {
                UserParticipant[] expected = task.getUsers();
                UserParticipant[] actual = (UserParticipant[]) transform(column, row[i]);
                if (!compareArrays(expected, actual)) {
                    throw new UserParticipantComparisonFailure("", expected, actual);
                }
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

    protected <T> boolean compareArrays(T[] expected, T[] actual) {
        if (expected == null && actual == null){
            return true;
        }
        if (expected == null && actual != null){
            return false;
        }
        if (expected != null && actual == null){
            return false;
        }
        Set<T> expectedParticipants = new HashSet<T>(Arrays.asList(expected));
        Set<T> actualParticipants = new HashSet<T>(Arrays.asList(actual));
        if (expectedParticipants.size() != actualParticipants.size()){
            return false;
        }
        if (!expectedParticipants.containsAll(actualParticipants)){
            return false;
        }
        return true;
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

    private Object transform(int column, Object actual) throws OXException, IOException, SAXException, JSONException {
        switch (column) {
        case Task.START_DATE:
        case Task.END_DATE:
            return new Date( ( (Long) actual).longValue() );
        case Task.PARTICIPANTS:
            JSONArray participantArr = (JSONArray) actual;
            List<Participant> participants = new LinkedList<Participant>();
            for (int i = 0, size = participantArr.length(); i < size; i++) {
                JSONObject participantObj = participantArr.getJSONObject(i);
                int type = participantObj.getInt("type");
                switch (type) {
                case Participant.USER:
                    participants.add(new UserParticipant(participantObj.getInt("id")));
                    break;
                case Participant.GROUP:
                    participants.add(new GroupParticipant(participantObj.getInt("id")));
                    break;
                case Participant.EXTERNAL_USER:
                    participants.add(new ExternalUserParticipant(participantObj.getString("mail")));
                    break;
                }
            }
            return participants.toArray(new Participant[participants.size()]);

        case Appointment.USERS:
            JSONArray userParticipantArr = (JSONArray) actual;
            List<UserParticipant> userParticipants = new LinkedList<UserParticipant>();
            for (int i = 0, size = userParticipantArr.length(); i < size; i++) {
                JSONObject participantObj = userParticipantArr.getJSONObject(i);
                userParticipants.add(new UserParticipant(participantObj.getInt("id")));
            }
            return userParticipants.toArray(new UserParticipant[userParticipants.size()]);
        }

        return actual;
    }


    @Override
    public void cleanUp() throws Exception {

    }


}
