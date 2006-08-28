/*
 * OPEN-XCHANGE - "the communication and information enviroment"
 *
 * All intellectual property rights in the Software are protected by
 * international copyright laws.
 *
 * OPEN-XCHANGE is a trademark of Netline Internet Service GmbH and all other
 * brand and product names are or may be trademarks of, and are used to identify
 * products or services of, their respective owners.
 *
 * Please make sure that third-party modules and libraries are used according to
 * their respective licenses.
 *
 * Any modifications to this package must retain all copyright notices of the
 * original copyright holder(s) for the original code used.
 *
 * After any such modifications, the original code will still remain copyrighted
 * by the copyright holder(s) or original author(s).
 *
 * Copyright (C) 1998 - 2005 Netline Internet Service GmbH
 * mail:                    info@netline-is.de
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */

package com.openexchange.ajax;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.TaskFields;
import com.openexchange.ajax.parser.TaskParser;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.writer.TaskWriter;
import com.openexchange.api.OXException;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.tools.URLParameter;

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
     * URL of the tasks AJAX interface.
     */
    private static final String TASKS_URL = "/ajax/tasks";

    /**
     * Tests counting of tasks in the private folder.
     * @throws Throwable if an error occurs.
     */
    public void testCountPrivateFolder() throws Throwable {
        final int folderId = getPrivateTaskFolder(getWebConversation(),
            getHostName(), getSessionId());

        final int number = countTasks(getWebConversation(), getHostName(),
            getSessionId(), folderId);
        LOG.trace(number);
        assertTrue("Number of tasks is not okay.", number >= 0);
    }

    /**
     * Tests counting of tasks in a public folder.
     * @throws Throwable if an error occurs.
     */
    public void notestCountPublicFolder() throws Throwable {
        final List<FolderObject> folders = FolderTest.getSubfolders(
            getWebConversation(), getHostName(), getSessionId(), "2", false);
        int folderId = -1;
        for (FolderObject folder : folders) {
            if (folder.getModule() == FolderObject.TASK
                && folder.getObjectID() >= FolderObject.MIN_FOLDER_ID) {
                folderId = folder.getObjectID();
            }
        }
        assertTrue("Can't find public task folder.", folderId > 0);

        final int number = countTasks(getWebConversation(), getHostName(),
            getSessionId(), folderId);
        LOG.trace(number);
        assertTrue("Number of tasks is not okay.", number >= 0);
    }

    /**
     * Tests inserting a private task.
     * @throws Throwable if an error occurs.
     */
    public void testInsertPrivateTask() throws Throwable {
        final Task task = new Task();
        task.setTitle("Private task");
        task.setPrivateFlag(false);
        final Date lastModified = new Date();
        task.setCreationDate(new Date());
        task.setLastModified(lastModified);
        task.setStartDate(new Date(1133964000000l));
        task.setEndDate(new Date(1133967600000l));
        task.setAfterComplete(new Date(1133971200000l));
        task.setNote("Description");
        task.setStatus(Task.NOT_STARTED); //FIXME!
        task.setPriority(Task.NORMAL);
        task.setCategories("Categories");
        task.setTargetDuration(1.0f);
        task.setActualDuration(1.0f);
        task.setDurationType(CalendarObject.DAYS);
        task.setTargetCosts(1.0f);
        task.setActualCosts(1.0f);
        task.setCurrency("\u20ac");
        task.setTripMeter("trip meter");
        task.setBillingInformation("billing information");
        task.setCompanies("companies");

        final int folderId = getPrivateTaskFolder(getWebConversation(),
            getHostName(), getSessionId());

        task.setParentFolderID(folderId);
        final int taskId = insertTask(getWebConversation(), getHostName(),
            getSessionId(), task);

        final Response response = getTask(getWebConversation(), getHostName(),
            getSessionId(), folderId, taskId);
        assertFalse(response.getErrorMessage(), response.hasError());

        deleteTask(getWebConversation(), getHostName(), getSessionId(),
            lastModified, new int[] { folderId, taskId });
    }

    /**
     * Tests if the charset handling is correct.
     * @throws Throwable if an error occurs.
     */
    public void testCharset() throws Throwable {
        final Task task = new Task();
        task.setTitle("\u00E4\u00F6\u00FC\u00DF\u00C4\u00D6\u00DC");
        task.setNote("\uC11C\uC601\uC9C4");

        final int folderId = getPrivateTaskFolder(getWebConversation(),
            getHostName(), getSessionId());

        task.setParentFolderID(folderId);
        final int taskId = insertTask(getWebConversation(), getHostName(),
            getSessionId(), task);

        final Response response = getTask(getWebConversation(), getHostName(),
            getSessionId(), folderId, taskId);
        final Task reload = (Task) response.getData();
        assertEquals("Title differs.", task.getTitle(), reload.getTitle());
        assertEquals("Description differs.", task.getNote(), reload.getNote());
        final Date lastModified = response.getTimestamp();

        deleteTask(getWebConversation(), getHostName(), getSessionId(),
            lastModified, new int[] { folderId, taskId });
    }

    /**
     * Tests if floats can be stored correctly.
     * @throws Throwable if an error occurs.
     */
    public void testFloats() throws Throwable {
        final Task task = new Task();
        task.setActualDuration(1f);
        task.setTargetDuration(1f);
        task.setActualCosts(1f);
        task.setTargetCosts(1f);

        final int folderId = getPrivateTaskFolder(getWebConversation(),
            getHostName(), getSessionId());

        task.setParentFolderID(folderId);
        final int taskId = insertTask(getWebConversation(), getHostName(),
            getSessionId(), task);

        final Response response = getTask(getWebConversation(), getHostName(),
            getSessionId(), folderId, taskId);
        final Task reload = (Task) response.getData();
        assertEquals("Actual duration differs.", task.getActualDuration(),
            reload.getActualDuration());
        assertEquals("Target duration differs.", task.getTargetDuration(),
            reload.getTargetDuration());
        assertEquals("Actual costs differs.", task.getActualCosts(),
            reload.getActualCosts());
        assertEquals("Target costs differs.", task.getTargetCosts(),
            reload.getTargetCosts());
        final Date lastModified = response.getTimestamp();

        deleteTask(getWebConversation(), getHostName(), getSessionId(),
            lastModified, new int[] { folderId, taskId });
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
        final Date lastModified = new Date();
        task.setLastModified(lastModified);
        task.setStartDate(new Date(1133964000000l));
        task.setEndDate(new Date(1133967600000l));
        task.setAfterComplete(new Date(1133971200000l));
        task.setNote("Description");
        task.setStatus(Task.NOT_STARTED); //FIXME!
        task.setPriority(Task.NORMAL);
        task.setCategories("Categories");
        task.setTargetDuration(1.0f);
        task.setActualDuration(1.0f);
        task.setDurationType(CalendarObject.DAYS);
        task.setTargetCosts(1.0f);
        task.setActualCosts(1.0f);
        task.setCurrency("\u20ac");
        task.setTripMeter("trip meter");
        task.setBillingInformation("billing information");
        task.setCompanies("companies");

        final int folderId = getPrivateTaskFolder(getWebConversation(),
            getHostName(), getSessionId());

        final List<Participant> participants = getParticipants(
            getWebConversation(), getHostName(), getSessionId(), 2, true,
            getMyId(getWebConversation(), getHostName(), getSessionId()));
        task.setParticipants(participants);
        task.setParentFolderID(folderId);

        final int taskId = insertTask(getWebConversation(), getHostName(),
            getSessionId(), task);
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

        deleteTask(getWebConversation(), getHostName(), getSessionId(),
            lastModified, new int[] { folderId, taskId });
    }

    public void testUpdateDelegatedTask() throws Throwable {
        final List<Participant> participants = getParticipants(
            getWebConversation(), getHostName(), getSessionId(), 4, true,
            getMyId(getWebConversation(), getHostName(), getSessionId()));
        final List<Participant> firstParticipants =
            new ArrayList<Participant>();
        firstParticipants.addAll(participants.subList(0, 2));
        final List<Participant> secondParticipants =
            new ArrayList<Participant>();
        secondParticipants.addAll(participants.subList(2, 4));
        secondParticipants.add(participants.get(0));

        final Task task = new Task();
        task.setTitle("Private delegated task");
        final int folderId = getPrivateTaskFolder(getWebConversation(),
            getHostName(), getSessionId());
        task.setParentFolderID(folderId);
        task.setParticipants(firstParticipants);

        LOG.trace("Creating delegated task with participants: "
            + firstParticipants);
        final int taskId = insertTask(getWebConversation(), getHostName(),
            getSessionId(), task);
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
        updateTask(getWebConversation(), getHostName(), getSessionId(),
            folderId, updatedTask, lastModified);
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
            lastModified, new int[] { folderId, taskId });
    }

    public void testUpdate() throws Throwable {
        final String title = "Title";
        final String updatedTitle = "Complete other title.";
        final Task task = new Task();
        task.setTitle(title);

        final int folderId = getPrivateTaskFolder(getWebConversation(),
            getHostName(), getSessionId());

        task.setParentFolderID(folderId);
        final int taskId = insertTask(getWebConversation(), getHostName(),
            getSessionId(), task);

        Response response = getTask(getWebConversation(), getHostName(),
            getSessionId(), folderId, taskId);
        assertTrue("Response can't be parsed to a task.",
            response.getData() instanceof Task);
        Date lastModified = response.getTimestamp();

        task.setObjectID(taskId);
        task.setTitle(updatedTitle);
        updateTask(getWebConversation(), getHostName(), getSessionId(),
            folderId, task, lastModified);

        response = getTask(getWebConversation(), getHostName(), getSessionId(),
            folderId, taskId);
        assertEquals("Title of task is not updated.", updatedTitle,
            ((Task) response.getData()).getTitle());
        lastModified = response.getTimestamp();

        deleteTask(getWebConversation(), getHostName(), getSessionId(),
            lastModified, new int[] { folderId, taskId });
    }

    public void testAll() throws Throwable {
        final int folderId = getPrivateTaskFolder(getWebConversation(),
            getHostName(), getSessionId());
        final Task task = new Task();
        task.setParentFolderID(folderId);
        int[][] tasks = new int[10][2];
        for (int i = 0; i < tasks.length; i++) {
            task.setTitle("Task " + (i + 1));
            tasks[i][1] = insertTask(getWebConversation(), getHostName(),
                getSessionId(), task);
            tasks[i][0] = folderId;
        }
        final int[] columns = new int[] { Task.TITLE, Task.OBJECT_ID,
            Task.LAST_MODIFIED, Task.FOLDER_ID };
        final Response response = getAllTasksInFolder(getWebConversation(),
            getHostName(), getSessionId(), folderId, columns, 0, null);
        final JSONArray array = (JSONArray) response.getData();
        // TODO parse JSON array
        final Date lastModified = response.getTimestamp();
        for (int[] folderAndTask : tasks) {
            deleteTask(getWebConversation(), getHostName(), getSessionId(),
                lastModified, folderAndTask);
        }
    }

    /**
     * Tests a full list of tasks in a folder with ordering.
     * @throws Throwable if an error occurs.
     */
    public void testAllWithOrder() throws Throwable {
        final int folderId = getPrivateTaskFolder(getWebConversation(),
            getHostName(), getSessionId());
        final Task task = new Task();
        task.setParentFolderID(folderId);
        int[][] tasks = new int[10][2];
        for (int i = 0; i < tasks.length; i++) {
            task.setTitle("Task " + (i + 1));
            tasks[i][1] = insertTask(getWebConversation(), getHostName(),
                getSessionId(), task);
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
                lastModified, folderAndTask);
        }
    }

    public void testUpdates() throws Throwable {
        final int folderId = getPrivateTaskFolder(getWebConversation(),
            getHostName(), getSessionId());
        final Task task = new Task();
        task.setParentFolderID(folderId);
        int[][] tasks = new int[10][2];
        for (int i = 0; i < tasks.length; i++) {
            task.setTitle("Task " + (i + 1));
            tasks[i][1] = insertTask(getWebConversation(), getHostName(),
                getSessionId(), task);
            tasks[i][0] = folderId;
        }
        int[] columns = new int[] { Task.TITLE, Task.OBJECT_ID,
            Task.FOLDER_ID };
        Response response = getAllTasksInFolder(getWebConversation(),
            getHostName(), getSessionId(), folderId, columns, Task.TITLE,
            "asc");
        JSONArray array = (JSONArray) response.getData();
        assertTrue("Can't find " + tasks.length + " inserted tasks.",
            array.length() >= tasks.length);
        // TODO parse JSON array
        Date timestamp = response.getTimestamp();
        if (null == timestamp) {
            // TODO This has to be fixed.
            timestamp = new Date();
        }
        // Now update 5
        for (int i = 0; i < tasks.length / 2; i++) {
            task.setTitle("UpdatedTask " + (i + 1));
            task.setObjectID(tasks[i][1]);
            updateTask(getWebConversation(), getHostName(), getSessionId(),
                folderId, task, timestamp);
        }
        // And delete 2
        final int[][] deltasks = new int[2][2];
        System.arraycopy(tasks, 8, deltasks, 0, 2);
        for (int[] folderAndTask : deltasks) {
            deleteTask(getWebConversation(), getHostName(), getSessionId(),
                timestamp, folderAndTask);
        }
        // Now request updates for the list
        columns = new int[] { Task.OBJECT_ID, Task.FOLDER_ID, Task.TITLE,
            Task.START_DATE, Task.END_DATE, Task.PERCENT_COMPLETED,
            Task.PRIORITY };
        response = getUpdatedTasks(getWebConversation(), getHostName(),
            getSessionId(), folderId, columns, 0, null, timestamp);
        array = (JSONArray) response.getData();
        assertTrue("Can't find " + (tasks.length / 2 + 2) + " updated tasks.",
            array.length() >= tasks.length / 2 + 2);
        // Clean up
        timestamp = response.getTimestamp();
        for (int[] folderAndTask : tasks) {
            deleteTask(getWebConversation(), getHostName(), getSessionId(),
                timestamp, folderAndTask);
        }
    }

    public void testTaskList() throws Throwable {
        final int folderId = getPrivateTaskFolder(getWebConversation(),
            getHostName(), getSessionId());
        final Task task = new Task();
        task.setParentFolderID(folderId);
        int[][] tasks = new int[10][2];
        for (int i = 0; i < tasks.length; i++) {
            task.setTitle("Task " + (i + 1));
            tasks[i][1] = insertTask(getWebConversation(), getHostName(),
                getSessionId(), task);
            tasks[i][0] = folderId;
        }
        final int[] columns = new int[] { Task.TITLE, Task.OBJECT_ID,
            Task.LAST_MODIFIED };
        final Response response = getTaskList(getWebConversation(),
            getHostName(), getSessionId(), tasks, columns);
        final JSONArray array = (JSONArray) response.getData();
        // TODO parse JSON array
        final Date lastModified = response.getTimestamp();
        for (int[] folderAndTask : tasks) {
            deleteTask(getWebConversation(), getHostName(), getSessionId(),
                lastModified, folderAndTask);
        }
    }

    public void notestConfirmation() throws Throwable {
        final int folderId = getPrivateTaskFolder();

        final Task task = new Task();
        task.setTitle("Task to test confirmation");

        final String[] tmp = LoginTest.getSessionIdWithUserId(
            getWebConversation(), getHostName(), getSeconduser(),
            getPassword());
        final String sessionId2 = tmp[0];
        final int userId2 = Integer.parseInt(tmp[1]);

        final List<UserParticipant> participants =
            new ArrayList<UserParticipant>();
        final UserParticipant participant = new UserParticipant();
        participant.setIdentifier(userId2);
        participants.add(participant);
        task.setParticipants(participants);
        task.setParentFolderID(folderId);

        final int taskId = insertTask(getWebConversation(), getHostName(),
            getSessionId(), task);
        LOG.trace("Created delegated task for confirmation: " + taskId);

        confirmTask(getWebConversation(), getHostName(), sessionId2, taskId,
            CalendarObject.ACCEPT, "Testconfirmation.");
        LOG.trace("Confirmed task.");

        final Response response = getTask(getWebConversation(), getHostName(),
            getSessionId(), folderId, taskId);
        final Date lastModified = response.getTimestamp();
        final Task reload = (Task) response.getData();
        for (UserParticipant p1 : reload.getUsers()) {
            boolean found = false;
            for (UserParticipant p2 : participants) {
                if (p1.getIdentifier() == p2.getIdentifier()) {
                    found = true;
                    assertTrue("Can't read accept of participant.",
                        p1.getConfirm() == CalendarObject.ACCEPT);
                }
            }
            if (!found) {
                fail("Storing participant in delegated task failed.");
            }
        }

        deleteTask(getWebConversation(), getHostName(), getSessionId(),
            lastModified, new int[] { folderId, taskId });
    }

    /**
     * This method implements storing of a task through the AJAX interface.
     * @param conversation WebConversation.
     * @param hostName Host name of the server.
     * @param sessionId Session identifier of the user.
     * @param task Task to store.
     * @return the unique identifer of the task.
     * @throws JSONException if parsing of serialized json fails.
     * @throws SAXException if a SAX error occurs.
     * @throws IOException if the communication with the server fails.
     */
    public static int insertTask(final WebConversation conversation,
        final String hostName, final String sessionId, final Task task)
        throws JSONException, IOException, SAXException {
    	task.setStatus(Task.NOT_STARTED); // FIXME! 
        LOG.trace("Inserting task.");
        final StringWriter stringW = new StringWriter();
        final PrintWriter printW = new PrintWriter(stringW);
        final TaskWriter taskW = new TaskWriter(printW);
        taskW.writeTask(task);
        printW.flush();
        final String object = stringW.toString();
        LOG.trace(object);
        final ByteArrayInputStream bais = new ByteArrayInputStream(object
            .getBytes("UTF-8"));
        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION,
            AJAXServlet.ACTION_NEW);
        parameter.setParameter(AJAXServlet.PARAMETER_FOLDERID,
            String.valueOf(task.getParentFolderID()));
        final WebRequest req = new PutMethodWebRequest(PROTOCOL + hostName
            + TASKS_URL + parameter.getURLParameters(), bais, AJAXServlet
            .CONTENTTYPE_JAVASCRIPT);
        final WebResponse resp = conversation.getResponse(req);
        assertEquals("Response code is not okay.", HttpServletResponse.SC_OK,
            resp.getResponseCode());
        final String body = resp.getText();
        LOG.trace("Response body: " + body);
        final Response response = Response.parse(body);
        assertFalse(response.getErrorMessage(), response.hasError());
        final JSONObject data = (JSONObject) response.getData();
        if (!data.has(TaskFields.ID)) {
            fail(response.getErrorMessage());
        }
        final int taskId = data.getInt(TaskFields.ID);
        assertTrue("Problem while inserting task", taskId > 0);
        return taskId;
    }

    public static void updateTask(final WebConversation conversation,
        final String hostName, final String sessionId, final int folderId,
        final Task task, final Date lastModified) throws JSONException,
        IOException, SAXException {
    	task.setStatus(Task.NOT_STARTED); //FIXME
        LOG.trace("Updating task.");
        final StringWriter stringW = new StringWriter();
        final PrintWriter printW = new PrintWriter(stringW);
        final TaskWriter taskW = new TaskWriter(printW);
        taskW.writeTask(task);
        printW.flush();
        final String object = stringW.toString();
        LOG.trace(object);
        final ByteArrayInputStream bais = new ByteArrayInputStream(object
            .getBytes("UTF-8"));
        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION,
            AJAXServlet.ACTION_UPDATE);
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER,
            String.valueOf(folderId));
        parameter.setParameter(AJAXServlet.PARAMETER_ID,
            String.valueOf(task.getObjectID()));
        parameter.setParameter(AJAXServlet.PARAMETER_TIMESTAMP,
            String.valueOf(lastModified.getTime()));
        final WebRequest req = new PutMethodWebRequest(PROTOCOL + hostName
            + TASKS_URL + parameter.getURLParameters(), bais, AJAXServlet
            .CONTENTTYPE_JAVASCRIPT);
        final WebResponse resp = conversation.getResponse(req);
        assertEquals("Response code is not okay.", HttpServletResponse.SC_OK,
            resp.getResponseCode());
        final String body = resp.getText();
        LOG.trace("Response body: " + body);
        final Response response = Response.parse(body);
        assertFalse(response.getErrorMessage(), response.hasError());
    }

    public static Response getTask(final WebConversation conversation,
        final String hostName, final String sessionId, final int folderId,
        final int taskId) throws IOException, SAXException, JSONException,
        OXException {
        LOG.trace("Getting task.");
        final WebRequest req = new GetMethodWebRequest(PROTOCOL + hostName
            + TASKS_URL);
        req.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_GET);
        req.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        req.setParameter(AJAXServlet.PARAMETER_INFOLDER,
            String.valueOf(folderId));
        req.setParameter(AJAXServlet.PARAMETER_ID, String.valueOf(taskId));
        final WebResponse resp = conversation.getResponse(req);
        assertEquals("Response code is not okay.", HttpServletResponse.SC_OK,
            resp.getResponseCode());
        final String body = resp.getText();
        LOG.trace("Body: \"" + body + "\"");
        final Response response = Response.parse(body);
        assertFalse(response.getErrorMessage(), response.hasError());
        final Task task = new Task();
        new TaskParser().parse(task, (JSONObject) response.getData());
        response.setData(task);
        return response;
    }

    /**
     * @param folderAndTaskIds the first dimension contains several folder and
     * task identifier that should be deleted. The second dimension contains the
     * folder identifier with the index <code>0</code> and the task identifier
     * with the index <code>1</code>.
     * @throws JSONException if parsing of serialized json fails.
     * @throws SAXException if a SAX error occurs.
     * @throws IOException if the communication with the server fails.
     */
    public static void deleteTask(final WebConversation conversation,
        final String hostName, final String sessionId, final Date lastUpdate,
        final int[] folderAndTask) throws IOException, SAXException,
        JSONException {
        LOG.trace("Deleting tasks.");
        final JSONObject json = new JSONObject();
        json.put(AJAXServlet.PARAMETER_ID, folderAndTask[1]);
        json.put(AJAXServlet.PARAMETER_INFOLDER, folderAndTask[0]);
        json.put(AJAXServlet.PARAMETER_TIMESTAMP, lastUpdate.getTime());
        final ByteArrayInputStream bais = new ByteArrayInputStream(json
            .toString().getBytes("UTF-8"));
        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION,
            AJAXServlet.ACTION_DELETE);
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        parameter.setParameter(AJAXServlet.PARAMETER_TIMESTAMP,
            String.valueOf(lastUpdate.getTime()));
        final WebRequest req = new PutMethodWebRequest(PROTOCOL + hostName
            +TASKS_URL + parameter.getURLParameters(), bais, AJAXServlet
            .CONTENTTYPE_JAVASCRIPT);
        final WebResponse resp = conversation.getResponse(req);
        assertEquals("Response code is not okay.", HttpServletResponse.SC_OK,
            resp.getResponseCode());
        final Response response = Response.parse(resp.getText());
        assertFalse(response.getErrorMessage(), response.hasError());
    }

    public static int countTasks(final WebConversation conversation,
        final String hostName, final String sessionId, final int folderId)
        throws IOException, SAXException, JSONException {
        LOG.trace("Counting tasks.");
        final WebRequest req = new GetMethodWebRequest(PROTOCOL + hostName
            + TASKS_URL);
        req.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        req.setParameter(AJAXServlet.PARAMETER_ACTION,
            AJAXServlet.ACTION_COUNT);
        req.setParameter(AJAXServlet.PARAMETER_FOLDERID,
            String.valueOf(folderId));
        final WebResponse resp = conversation.getResponse(req);
        assertEquals("Response code is not okay.", HttpServletResponse.SC_OK,
            resp.getResponseCode());
        final String body = resp.getText();
        LOG.trace("Response body: " + body);
        return (Integer) Response.parse(body).getData();
    }

    public static Response getAllTasksInFolder(
        final WebConversation conversation, final String hostName,
        final String sessionId, final int folderId, final int[] columns,
        final int sort, final String order) throws IOException, SAXException,
        JSONException {
        LOG.trace("Getting all task in a folder.");
        final WebRequest req = new GetMethodWebRequest(PROTOCOL + hostName
            + TASKS_URL);
        req.setParameter(AJAXServlet.PARAMETER_ACTION,
            AJAXServlet.ACTION_ALL);
        req.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        req.setParameter(AJAXServlet.PARAMETER_FOLDERID,
            String.valueOf(folderId));
        final StringBuilder sb = new StringBuilder();
        for (int i : columns) {
            sb.append(i);
            sb.append(',');
        }
        sb.delete(sb.length() - 1, sb.length());
        req.setParameter(AJAXServlet.PARAMETER_COLUMNS, sb.toString());
        if (null != order) {
            req.setParameter(AJAXServlet.PARAMETER_SORT, String.valueOf(sort));
            req.setParameter(AJAXServlet.PARAMETER_ORDER, order);
        }
        final WebResponse resp = conversation.getResponse(req);
        assertEquals("Response code is not okay.", HttpServletResponse.SC_OK,
            resp.getResponseCode());
        final String body = resp.getText();
        LOG.trace("Response body: " + body);
        final Response response = Response.parse(body);
        assertFalse(response.getErrorMessage(), response.hasError());
        return response;
    }

    public static Response getUpdatedTasks(final WebConversation conversation,
        final String hostName, final String sessionId, final int folderId,
        final int[] columns, final int sort, final String order,
        final Date lastModified) throws IOException, SAXException,
        JSONException {
        LOG.trace("Getting updated tasks in a folder.");
        final WebRequest req = new GetMethodWebRequest(PROTOCOL + hostName
            + TASKS_URL);
        req.setParameter(AJAXServlet.PARAMETER_ACTION,
            AJAXServlet.ACTION_UPDATES);
        req.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        req.setParameter(AJAXServlet.PARAMETER_FOLDERID,
            String.valueOf(folderId));
        final StringBuilder sb = new StringBuilder();
        for (int i : columns) {
            sb.append(i);
            sb.append(',');
        }
        sb.delete(sb.length() - 1, sb.length());
        req.setParameter(AJAXServlet.PARAMETER_COLUMNS, sb.toString());
        if (null != order) {
            req.setParameter(AJAXServlet.PARAMETER_SORT, String.valueOf(sort));
            req.setParameter(AJAXServlet.PARAMETER_ORDER, order);
        }
        req.setParameter(AJAXServlet.PARAMETER_TIMESTAMP,
            String.valueOf(lastModified.getTime()));
        final WebResponse resp = conversation.getResponse(req);
        assertEquals("Response code is not okay.", HttpServletResponse.SC_OK,
            resp.getResponseCode());
        final String body = resp.getText();
        LOG.trace("Response body: " + body);
        final Response response = Response.parse(body);
        assertFalse(response.getErrorMessage(), response.hasError());
        return response;
    }

    public static Response getTaskList(final WebConversation conversation,
        final String hostName, final String sessionId,
        final int[][] folderAndTaskIds, final int[] columns)
        throws IOException, SAXException, JSONException {
        LOG.trace("Get a list of tasks.");
        final JSONArray json = new JSONArray();
        for (int[] folderAndTask : folderAndTaskIds) {
            final JSONObject json2 = new JSONObject();
            json2.put(DataFields.ID, folderAndTask[1]);
            json2.put(AJAXServlet.PARAMETER_INFOLDER, folderAndTask[0]);
            json.put(json2);
        }
        final ByteArrayInputStream bais = new ByteArrayInputStream(json
            .toString().getBytes("UTF-8"));
        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION,
            AJAXServlet.ACTION_LIST);
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        final StringBuilder columnString = new StringBuilder();
        for (int i : columns) {
            columnString.append(i);
            columnString.append(',');
        }
        columnString.delete(columnString.length() - 1, columnString.length());
        parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS,
            columnString.toString());
        final WebRequest req = new PutMethodWebRequest(PROTOCOL + hostName
            + TASKS_URL + parameter.getURLParameters(), bais, AJAXServlet
            .CONTENTTYPE_JAVASCRIPT);
        final WebResponse resp = conversation.getResponse(req);
        assertEquals("Response code is not okay.", HttpServletResponse.SC_OK,
            resp.getResponseCode());
        final String body = resp.getText();
        LOG.trace("Response body: " + body);
        final Response response = Response.parse(body);
        assertFalse(response.getErrorMessage(), response.hasError());
        return response;
    }

    public static void confirmTask(final WebConversation conversation,
        final String hostName, final String sessionId, final int taskId,
        final int confirm, final String confirmMessage) throws IOException,
        SAXException, JSONException {
        final WebRequest req = new PostMethodWebRequest(PROTOCOL + hostName
            + TASKS_URL);
        req.setParameter(AJAXServlet.PARAMETER_ACTION,
            AJAXServlet.ACTION_CONFIRM);
        req.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        req.setParameter(AJAXServlet.PARAMETER_ID, String.valueOf(taskId));
        req.setParameter(AJAXServlet.PARAMETER_CONFIRM,
            String.valueOf(confirm));
        req.setParameter(AJAXServlet.PARAMETER_CONFIRM_MESSAGE, confirmMessage);
        final WebResponse resp = conversation.getResponse(req);
        assertEquals("Response code is not okay.", HttpServletResponse.SC_OK,
            resp.getResponseCode());
        final String body = resp.getText();
        LOG.trace("Response body: " + body);
        final Response response = Response.parse(body);
        assertFalse(response.getErrorMessage(), response.hasError());
    }

    private int privateTaskFolder = 0;

    public int getPrivateTaskFolder() throws IOException, SAXException,
        JSONException, OXException, Exception {
        if (0 == privateTaskFolder) {
            privateTaskFolder = getPrivateTaskFolder(getWebConversation(),
                getHostName(), getSessionId());
        }
        return privateTaskFolder;
    }

    public static int getPrivateTaskFolder(final WebConversation conversation,
        final String hostName, final String sessionId)
        throws IOException, SAXException, JSONException,
        OXException {
        final FolderObject myTasks = FolderTest.getStandardTaskFolder(
            conversation, hostName, sessionId);
        return myTasks.getObjectID();
    }

    public static List<Participant> getParticipants(
        final WebConversation conversation, final String hostName,
        final String sessionId) throws Exception {
        final ContactObject[] userContacts = ContactTest.searchContact(
            conversation, "*", FolderObject.SYSTEM_LDAP_FOLDER_ID,
            new int[] { ContactObject.INTERNAL_USERID }, PROTOCOL + hostName,
            sessionId);
        final List<Participant> participants = new ArrayList<Participant>();
        for (ContactObject userContact : userContacts) {
            final UserParticipant user = new UserParticipant();
            user.setIdentifier(userContact.getInternalUserId());
            participants.add(user);
        }
        return participants;
    }

    public static void removeParticipant(List<Participant> participants,
        final int creatorId) {
        final Iterator<Participant> iter = participants.iterator();
        while (iter.hasNext()) {
            if (iter.next().getIdentifier() == creatorId) {
                iter.remove();
            }
        }
    }

    public static List<Participant> extractByRandom(
        final List<Participant> participants, final int count) {
        final Random rand = new Random(System.currentTimeMillis());
        final List<Participant> retval = new ArrayList<Participant>();
        do {
            final Participant participant = participants.get(rand.nextInt(
                participants.size()));
            if (!retval.contains(participant)) {
                retval.add(participant);
            }
        } while (retval.size() < count && retval.size() < participants.size());
        return retval;
    }

    public static List<Participant> getParticipants(
        final WebConversation conversation, final String hostName,
        final String sessionId, final int count, final boolean noCreator,
        final int creatorId) throws Exception {
        List<Participant> participants = getParticipants(conversation, hostName,
            sessionId);
        if (noCreator) {
            removeParticipant(participants, creatorId);
        }
        participants = extractByRandom(participants, count);
        return participants;
    }

    public static int getMyId(final WebConversation conversation,
        final String hostName, final String sessionId)
        throws MalformedURLException, IOException, SAXException, JSONException,
        OXException {
        LOG.trace("Getting my user identifier through my standard task "
            + "folder.");
        final FolderObject myTasks = FolderTest.getStandardTaskFolder(
            conversation, hostName, sessionId);
        final int retval = myTasks.getCreatedBy();
        LOG.trace("My identifier is " + retval);
        return retval;
    }
}
