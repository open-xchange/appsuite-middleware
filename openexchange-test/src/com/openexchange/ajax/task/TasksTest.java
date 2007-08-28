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

package com.openexchange.ajax.task;

import static com.openexchange.ajax.task.TaskTools.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.openexchange.ajax.AbstractAJAXTest;
import com.openexchange.ajax.config.ConfigTools;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.TaskFields;
import com.openexchange.ajax.participant.ParticipantTools;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.tools.servlet.AjaxException;

/**
 * This class tests the AJAX interface of the tasks.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class TasksTest extends AbstractAJAXTest {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(TasksTest.class);
    /**
     * Proxy attribute for the private task folder of the user.
     */
    private int privateTaskFolder;

    /**
     * Default constructor.
     * @param name Name of this test.
     */
    public TasksTest(final String name) {
        super(name);
    }

    /**
     * Tests inserting a delegated task.
     * @throws Throwable if an error occurs.
     */
    public void testInsertDelegatedPrivateTask() throws Throwable {
        final Task task = new Task();
        task.setTitle("Private delegated task");
        task.setPrivateFlag(false);
        task.setCreationDate(new Date());
        Date lastModified = new Date();
        task.setLastModified(lastModified);
        task.setStartDate(new Date(1133964000000l));
        task.setEndDate(new Date(1133967600000l));
        task.setAfterComplete(new Date(1133971200000l));
        task.setNote("Description");
        task.setStatus(Task.NOT_STARTED); //FIXME!
        task.setPriority(Task.NORMAL);
        task.setCategories("Categories");
        task.setTargetDuration(1440);
        task.setActualDuration(1440);
        task.setTargetCosts(1.0f);
        task.setActualCosts(1.0f);
        task.setCurrency("\u20ac");
        task.setTripMeter("trip meter");
        task.setBillingInformation("billing information");
        task.setCompanies("companies");

        final int folderId = TaskTools.getPrivateTaskFolder(getWebConversation(),
            getHostName(), getSessionId());

        final List<Participant> participants = ParticipantTools.getParticipants(
            getWebConversation(), getHostName(), getSessionId(), 2, true,
            ConfigTools.getUserId(getWebConversation(), getHostName(),
                getSessionId()));
        ExternalUserParticipant external = new ExternalUserParticipant();
        external.setEmailAddress("external@external.no");
        external.setDisplayName("External, External");
        participants.add(external);
        task.setParticipants(participants);
        task.setParentFolderID(folderId);

        final int taskId = extractInsertId(insertTask(getWebConversation(),
            getHostName(), getSessionId(), task));
        LOG.trace("Created delegated task: " + taskId);

        final Response response = getTask(getWebConversation(), getHostName(),
            getSessionId(), folderId, taskId);
        final Task reload = (Task) response.getData();
        for (Participant p1 : reload.getParticipants()) {
            boolean found = false;
            for (Participant p2 : participants) {
                if (p1.getIdentifier() == p2.getIdentifier()) {
                    found = true;
                }
            }
            if (!found) {
                fail("Storing participant in delegated task failed.");
            }
        }
        lastModified = response.getTimestamp();

        deleteTask(getWebConversation(), getHostName(), getSessionId(),
            lastModified, folderId, taskId);
    }

    public void testUpdateDelegatedTask() throws Throwable {
        final List<Participant> participants = ParticipantTools.getParticipants(
            getWebConversation(), getHostName(), getSessionId(), 4, true,
            ConfigTools.getUserId(getWebConversation(), getHostName(),
                getSessionId()));
        final List<Participant> firstParticipants =
            new ArrayList<Participant>();
        firstParticipants.addAll(participants.subList(0, 2));
        final List<Participant> secondParticipants =
            new ArrayList<Participant>();
        secondParticipants.addAll(participants.subList(2, 4));
        secondParticipants.add(participants.get(0));

        final Task task = new Task();
        task.setTitle("Private delegated task");
        final int folderId = TaskTools.getPrivateTaskFolder(getWebConversation(),
            getHostName(), getSessionId());
        task.setParentFolderID(folderId);
        task.setParticipants(firstParticipants);

        LOG.trace("Creating delegated task with participants: "
            + firstParticipants);
        final int taskId = extractInsertId(insertTask(getWebConversation(),
            getHostName(), getSessionId(), task));
        LOG.trace("Created delegated task: " + taskId);
        Response response = getTask(getWebConversation(), getHostName(),
            getSessionId(), folderId, taskId);
        Date lastModified = response.getTimestamp();
        Task reload = (Task) response.getData();
        assertEquals("Number of participants differ", firstParticipants.size(),
            reload.getParticipants().length);
        for (Participant p1 : firstParticipants) {
            boolean found = false;
            for (Participant p2 : reload.getParticipants()) {
                if (p1.getIdentifier() == p2.getIdentifier()) {
                    found = true;
                }
            }
            if (!found) {
                fail("Delegated task misses participant: "
                    + p1.getIdentifier());
            }
        }

        final Task updatedTask = new Task();
        updatedTask.setTitle("Updated delegated task");
        updatedTask.setObjectID(taskId);
        updatedTask.setParticipants(secondParticipants);
        LOG.trace("Updating delegated task with participants: "
            + secondParticipants);
        failOnError(updateTask(getWebConversation(), getHostName(),
            getSessionId(), folderId, updatedTask, lastModified));
        response = getTask(getWebConversation(), getHostName(),
            getSessionId(), folderId, taskId);
        lastModified = response.getTimestamp();
        reload = (Task) response.getData();
        assertEquals("Number of participants differ", secondParticipants.size(),
            reload.getParticipants().length);
        for (Participant p1 : secondParticipants) {
            boolean found = false;
            for (Participant p2 : reload.getParticipants()) {
                if (p1.getIdentifier() == p2.getIdentifier()) {
                    found = true;
                }
            }
            if (!found) {
                fail("Delegated task misses participant: "
                    + p1.getIdentifier());
            }
        }

        deleteTask(getWebConversation(), getHostName(), getSessionId(),
            lastModified, folderId, taskId);
    }

    public void testUpdate() throws Throwable {
        final String title = "Title";
        final String updatedTitle = "Complete other title.";
        final Task task = new Task();
        task.setTitle(title);

        final int folderId = TaskTools.getPrivateTaskFolder(getWebConversation(),
            getHostName(), getSessionId());

        task.setParentFolderID(folderId);
        final int taskId = extractInsertId(insertTask(getWebConversation(),
            getHostName(), getSessionId(), task));

        Response response = getTask(getWebConversation(), getHostName(),
            getSessionId(), folderId, taskId);
        assertTrue("Response can't be parsed to a task.",
            response.getData() instanceof Task);
        Date lastModified = response.getTimestamp();

        task.setObjectID(taskId);
        task.setTitle(updatedTitle);
        failOnError(updateTask(getWebConversation(), getHostName(),
            getSessionId(), folderId, task, lastModified));

        response = getTask(getWebConversation(), getHostName(), getSessionId(),
            folderId, taskId);
        assertEquals("Title of task is not updated.", updatedTitle,
            ((Task) response.getData()).getTitle());
        lastModified = response.getTimestamp();

        deleteTask(getWebConversation(), getHostName(), getSessionId(),
            lastModified, folderId, taskId);
    }

    /**
     * Tests a full list of tasks in a folder with ordering.
     * @throws Throwable if an error occurs.
     */
    public void testAllWithOrder() throws Throwable {
        final int folderId = TaskTools.getPrivateTaskFolder(getWebConversation(),
            getHostName(), getSessionId());
        final Task task = new Task();
        task.setParentFolderID(folderId);
        int[][] tasks = new int[10][2];
        for (int i = 0; i < tasks.length; i++) {
            task.setTitle("Task " + (i + 1));
            tasks[i][1] = extractInsertId(insertTask(getWebConversation(),
                getHostName(), getSessionId(), task));
            tasks[i][0] = folderId;
        }
        final int[] columns = new int[] { Task.TITLE, Task.OBJECT_ID,
            Task.LAST_MODIFIED, Task.FOLDER_ID };
        final Response response = getAllTasksInFolder(getWebConversation(),
            getHostName(), getSessionId(), folderId, columns,
            Task.TITLE, "asc");
        final JSONArray array = (JSONArray) response.getData();
        // TODO parse JSON array
        final Date lastModified = response.getTimestamp();
        for (int[] folderAndTask : tasks) {
            deleteTask(getWebConversation(), getHostName(), getSessionId(),
                lastModified, folderAndTask[0], folderAndTask[1]);
        }
    }

    public void testConfirmation() throws Throwable {
        final int folderId = getPrivateTaskFolder();

        final Task task = new Task();
        task.setTitle("Task to test confirmation");

        final int folderId2 = TaskTools.getPrivateTaskFolder(getSecondWebConversation(),
            getHostName(), getSecondSessionId());
        final int userId2 = ConfigTools.getUserId(getSecondWebConversation(),
            getHostName(), getSecondSessionId());

        final List<UserParticipant> participants =
            new ArrayList<UserParticipant>();
        final UserParticipant participant = new UserParticipant();
        participant.setIdentifier(userId2);
        participants.add(participant);
        task.setParticipants(participants);
        task.setParentFolderID(folderId);

        final int taskId = extractInsertId(insertTask(getWebConversation(),
            getHostName(), getSessionId(), task));
        LOG.trace("Created delegated task for confirmation: " + taskId);

        confirmTask(getSecondWebConversation(), getHostName(),
            getSecondSessionId(), folderId2, taskId, Task.ACCEPT,
            "Testconfirmation.");
        LOG.trace("Confirmed task.");

        final Response response = getTask(getWebConversation(), getHostName(),
            getSessionId(), folderId, taskId);
        final Date lastModified = response.getTimestamp();
        final JSONObject reload = response.getResponseData();
        final JSONArray users = reload.getJSONArray("users");
        boolean confirmed = false;
        for (int i = 0; i < users.length(); i++) {
            final JSONObject user = users.getJSONObject(i);
            final int confirm = user.getInt("confirmation");
            final int userId = user.getInt("id");
            if (userId2 == userId && Task.ACCEPT == confirm) {
                confirmed = true;
            }
        }
        assertTrue("Can't find confirmation.", confirmed);

        deleteTask(getWebConversation(), getHostName(), getSessionId(),
            lastModified, folderId, taskId);
    }

    /**
     * Tests the search for tasks.
     * @throws Throwable if an error occurs.
     */
    public void testSearch() throws Throwable {
        final TaskSearchObject search = new TaskSearchObject();
        final Response response = searchTask(getWebConversation(),
            getHostName(), getSessionId(), search, new int[] { Task.OBJECT_ID },
            -1, null);
        assertNotNull("Response contains no data.", response.getData());
        // TODO parse response
    }

    /**
     * Creates a task with a reminder and checks if the reminder is stored
     * correctly.
     * @throws Throwable if an error occurs.
     */
    public void testReminder() throws Throwable {
        final int folderId = getPrivateTaskFolder();

        final Task task = new Task();
        task.setTitle("Task to test reminder");
        task.setParentFolderID(folderId);
        final long remindTime = System.currentTimeMillis() / 1000 * 1000;
        final Date remind = new Date(remindTime);
        task.setAlarm(remind);

        final int taskId = extractInsertId(insertTask(getWebConversation(),
            getHostName(), getSessionId(), task));

        final Response response = getTask(getWebConversation(), getHostName(),
            getSessionId(), folderId, taskId);
        final Task reload = (Task) response.getData();
        assertEquals("Missing reminder.", remind, reload.getAlarm());
        deleteTask(getWebConversation(), getHostName(), getSessionId(),
            response.getTimestamp(), folderId, taskId);
    }

    /**
     * Creates a task with a field set. Then it updates the task with
     * <code>null</code> in that field. Server should not get an error on this.
     * @throws Throwable if an error occurs.
     */
    public void testInternalEqualError() throws Throwable {
        final int folderId = getPrivateTaskFolder();
        final Task task = new Task();
        task.setTitle("Title to remove on update");
        task.setNote("Not to remove on update");
        task.setParentFolderID(folderId);
        Response response = insertTask(getWebConversation(),
            getHostName(), getSessionId(), task);
        task.setObjectID(extractInsertId(response));
        response = getTask(getWebConversation(), getHostName(), getSessionId(),
            folderId, task.getObjectID());
        Date lastModified = extractTimestamp(response);
        final JSONObject json = new JSONObject();
        json.put(TaskFields.FOLDER_ID, folderId);
        json.put(TaskFields.ID, task.getObjectID());
        json.put(TaskFields.TITLE, JSONObject.NULL);
        json.put(TaskFields.NOTE, JSONObject.NULL);
        response = updateTask(getWebConversation(), getHostName(),
            getSessionId(), folderId, task.getObjectID(), json, lastModified);
        failOnError(response);
        lastModified = extractTimestamp(response);
        deleteTask(getWebConversation(), getHostName(), getSessionId(),
            lastModified, folderId, task.getObjectID());
    }

    /**
     * @param response response object of an update or some other request that
     * returns a timestamp.
     * @return the timestamp of the response.
     */
    public static Date extractTimestamp(final Response response) {
        final Date retval = response.getTimestamp();
        assertNotNull("Timestamp is missing.", retval);
        return retval;
    }

    public static void failOnError(final Response response) {
        assertFalse(response.getErrorMessage(), response.hasError());
    }

    /**
     * @return the identifier of the private task folder of the primary user.
     * @throws IOException if the communication with the server fails.
     * @throws SAXException if a SAX error occurs.
     * @throws JSONException if parsing of serialized json fails.
     * @throws OXException if reading the folders fails.
     */
    protected int getPrivateTaskFolder() throws IOException, SAXException,
        JSONException, OXException, AjaxException {
        if (0 == privateTaskFolder) {
            privateTaskFolder = TaskTools.getPrivateTaskFolder(getWebConversation(),
                getHostName(), getSessionId());
        }
        return privateTaskFolder;
    }
}
