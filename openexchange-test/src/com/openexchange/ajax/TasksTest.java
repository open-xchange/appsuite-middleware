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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.ajax.fields.TaskFields;
import com.openexchange.ajax.parser.ResponseParser;
import com.openexchange.ajax.parser.TaskParser;
import com.openexchange.ajax.types.Response;
import com.openexchange.ajax.writer.TaskWriter;
import com.openexchange.api.OXException;
import com.openexchange.groupware.container.CalendarObject;
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
    
    private static final String TASKS_URL = "/ajax/tasks";

    public void testCountPrivateFolder() throws Throwable {
        final int folderId = getPrivateTaskFolder(getWebConversation(),
            getHostName(), getSessionId());

        final int number = countTasks(getWebConversation(), getHostName(),
            getSessionId(), folderId);
        LOG.trace(number);
    }

    public void notestCountPublicFolder() throws Throwable {
        List<FolderObject> folders = FolderTest.getSubfolders(
            getWebConversation(), getHostName(), getSessionId(), "2", false);
        int folderId = -1;
        for (FolderObject folder : folders) {
            if (folder.getModule() == FolderObject.TASK
                && folder.getObjectID() >= 20) {
                folderId = folder.getObjectID();
            }
        }
        assertTrue("Can't find public task folder.", folderId > 0);

        final int number = countTasks(getWebConversation(), getHostName(),
            getSessionId(), folderId);
        LOG.trace(number);
    }

    /**
     * Test method for 'com.openexchange.ajax.Tasks.doPut(HttpServletRequest,
     * HttpServletResponse)'
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
        task.setStatus(Task.IN_PROGRESS);
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
        assertTrue("Problem while inserting private task.", taskId > 0);

        final Response response = getTask(getWebConversation(), getHostName(),
            getSessionId(), folderId, taskId);
        assertFalse(response.getErrorMessage(), response.hasError());

        final int[] notDeleted = deleteTask(getWebConversation(), getHostName(),
            getSessionId(), lastModified, new int[][] {{ folderId, taskId }});
        assertEquals("Task can't be deleted.", 0, notDeleted.length);
    }

    public void testCharset() throws Throwable {
        final Task task = new Task();
        task.setTitle("\u00E4\u00F6\u00FC\u00DF\u00C4\u00D6\u00DC");
        task.setNote("\uC11C\uC601\uC9C4");

        final int folderId = getPrivateTaskFolder(getWebConversation(),
            getHostName(), getSessionId());

        task.setParentFolderID(folderId);
        final int taskId = insertTask(getWebConversation(), getHostName(),
            getSessionId(), task);
        assertTrue("Problem while inserting private task.", taskId > 0);

        final Response response = getTask(getWebConversation(), getHostName(),
            getSessionId(), folderId, taskId);
        final Task reload = (Task) response.getData();
        assertEquals("Title differs.", task.getTitle(), reload.getTitle());
        assertEquals("Description differs.", task.getNote(), reload.getNote());
        final Date lastModified = response.getTimestamp();

        final int[] notDeleted = deleteTask(getWebConversation(), getHostName(),
            getSessionId(), lastModified, new int[][] {{ folderId, taskId }});
        assertEquals("Task can't be deleted.", 0, notDeleted.length);
    }
    
    /**
     * Test method for 'com.openexchange.ajax.Tasks.doPut(HttpServletRequest,
     * HttpServletResponse)'
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
        task.setStatus(Task.IN_PROGRESS);
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

        // TODO read user from user interface
        final UserParticipant user1 = new UserParticipant();
        user1.setIdentifier(232); // offspring
        final UserParticipant user2 = new UserParticipant();
        user2.setIdentifier(227); // viktor

        final int folderId = getPrivateTaskFolder(getWebConversation(),
            getHostName(), getSessionId());

        final List<Participant> participants = new ArrayList<Participant>();
        participants.add(user1);
        participants.add(user2);
        task.setParticipants(participants);
        task.setParentFolderID(folderId);

        final int taskId = insertTask(getWebConversation(), getHostName(),
            getSessionId(), task);
        LOG.trace("Created delegated task: " + taskId);
        assertTrue("Problem while inserting task", taskId > 0);

        final Response response = getTask(getWebConversation(), getHostName(),
            getSessionId(), folderId, taskId);
        final Task reload = (Task) response.getData();

        final int[] notDeleted = deleteTask(getWebConversation(), getHostName(),
            getSessionId(), lastModified, new int[][] {{ folderId, taskId }});
        assertEquals("Task can't be deleted.", 0, notDeleted.length);
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
        assertTrue("Problem while inserting private task.", taskId > 0);

        Response response = getTask(getWebConversation(), getHostName(),
            getSessionId(), folderId, taskId);
        assertTrue(response.getData() instanceof Task);
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

        final int[] notDeleted = deleteTask(getWebConversation(), getHostName(),
            getSessionId(), lastModified, new int[][] {{ folderId, taskId }});
        assertEquals("Task can't be deleted.", 0, notDeleted.length);
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
            Task.LAST_MODIFIED };
        final Response response = getAllTasksInFolder(getWebConversation(),
            getHostName(), getSessionId(), folderId, columns, 0, null);
        final JSONArray array = (JSONArray) response.getData();
        // TODO parse JSON array
        Date timestamp = response.getTimestamp();
        if (null == timestamp) {
            // TODO This has to be fixed.
            timestamp = new Date();
        }
        deleteTask(getWebConversation(), getHostName(), getSessionId(),
            timestamp, tasks);
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
        Date timestamp = response.getTimestamp();
        if (null == timestamp) {
            // TODO This has to be fixed.
            timestamp = new Date();
        }
        deleteTask(getWebConversation(), getHostName(), getSessionId(),
            timestamp, tasks);
    }
    
    /**
     * This method implements storing of a task through the AJAX interface.
     * @param conversation WebConversation.
     * @param getHostName() Host name of the server.
     * @param sessionId Session identifier of the user.
     * @param task Task to store.
     * @return the unique identifer of the task.
     * @throws JSONException 
     * @throws SAXException 
     * @throws IOException 
     * @throws MalformedURLException 
     * @throws Exception if an error occurs while storing the task.
     */
    public static int insertTask(final WebConversation conversation,
        final String hostName, final String sessionId, final Task task)
        throws JSONException, MalformedURLException, IOException, SAXException {
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
        assertEquals("Response code is not okay.", 200, resp.getResponseCode());
        final Response response = ResponseParser.parse(resp.getText());
        assertFalse(response.getErrorMessage(), response.hasError());
        final JSONObject data = (JSONObject) response.getData();
        if (!data.has(TaskFields.ID)) {
            fail(response.getErrorMessage());
        }
        return data.getInt(TaskFields.ID);
    }
    
    public static void updateTask(final WebConversation conversation,
        final String hostName, final String sessionId, final int folderId,
        final Task task, final Date lastModified) throws JSONException,
        MalformedURLException, IOException, SAXException {
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
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION,
            AJAXServlet.ACTION_UPDATE);
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
        assertEquals("Response code is not okay.", 200, resp.getResponseCode());
        final String body = resp.getText();
        LOG.trace("Response body: " + body);
        final Response response = ResponseParser.parse(body);
        assertFalse(response.getErrorMessage(), response.hasError());
    }

    public static Response getTask(final WebConversation conversation,
        final String hostName, final String sessionId, final int folderId,
        final int taskId) throws MalformedURLException, IOException,
        SAXException, JSONException, OXException {
        LOG.trace("Getting task.");
        final WebRequest req = new GetMethodWebRequest(PROTOCOL + hostName
            + TASKS_URL);
        req.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_GET);
        req.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        req.setParameter(AJAXServlet.PARAMETER_INFOLDER,
            String.valueOf(folderId));
        req.setParameter(AJAXServlet.PARAMETER_ID, String.valueOf(taskId));
        final WebResponse resp = conversation.getResponse(req);
        assertEquals("Response code is not okay.", 200, resp.getResponseCode());
        final String body = resp.getText();
        LOG.trace("Body: \"" + body + "\"");
        final Response response = ResponseParser.parse(body);
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
     */
    public static int[] deleteTask(final WebConversation conversation,
        final String hostName, final String sessionId, final Date lastUpdate,
        final int[][] folderAndTaskIds) throws MalformedURLException,
        IOException, SAXException, JSONException {
        LOG.trace("Deleting task.");
        final JSONArray json = new JSONArray();
        for (int[] folderAndTask : folderAndTaskIds) {
            final JSONObject json2 = new JSONObject();
            json2.put(DataFields.ID, folderAndTask[1]);
            json2.put(FolderChildFields.FOLDER_ID, folderAndTask[0]);
            json.put(json2);
        }
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
        assertEquals("Response code is not okay.", 200, resp.getResponseCode());
        final Response response = ResponseParser.parse(resp.getText());
        assertFalse(response.getErrorMessage(), response.hasError());
        final JSONArray array = (JSONArray) response.getData();
        final int[] retval = new int[array.length()];
        for (int i = 0; i < array.length(); i++) {
            retval[i] = array.getInt(i);
        }
        return retval;
    }

    public static int countTasks(final WebConversation conversation,
        final String hostName, final String sessionId, final int folderId)
        throws MalformedURLException, IOException, SAXException, JSONException {
        LOG.trace("Counting tasks.");
        final WebRequest req = new GetMethodWebRequest(PROTOCOL + hostName
            + TASKS_URL);
        req.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        req.setParameter(AJAXServlet.PARAMETER_ACTION,
            AJAXServlet.ACTION_COUNT);
        req.setParameter(AJAXServlet.PARAMETER_FOLDERID,
            String.valueOf(folderId));
        final WebResponse resp = conversation.getResponse(req);
        assertEquals("Response code is not okay.", 200, resp.getResponseCode());
        final String body = resp.getText();
        LOG.trace("Response body: " + body);
        final Response response = ResponseParser.parse(body);
        return (Integer) response.getData();
    }

    public static Response getAllTasksInFolder(
        final WebConversation conversation, final String hostName,
        final String sessionId, final int folderId, final int[] columns,
        final int sort, final String order) throws MalformedURLException,
        IOException, SAXException, JSONException {
        LOG.trace("Getting all task in a folder.");
        final WebRequest req = new GetMethodWebRequest(PROTOCOL + hostName
            + TASKS_URL);
        req.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        req.setParameter(AJAXServlet.PARAMETER_ACTION,
            AJAXServlet.ACTION_ALL);
        req.setParameter(AJAXServlet.PARAMETER_FOLDERID,
            String.valueOf(folderId));
        StringBuilder sb = new StringBuilder();
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
        assertEquals("Response code is not okay.", 200, resp.getResponseCode());
        final String body = resp.getText();
        LOG.trace("Response body: " + body);
        final Response response = ResponseParser.parse(body);
        assertFalse(response.getErrorMessage(), response.hasError());
        return response;
    }

    public static Response getTaskList(final WebConversation conversation,
        final String hostName, final String sessionId,
        final int[][] folderAndTaskIds, final int[] columns)
        throws MalformedURLException, IOException, SAXException, JSONException {
        LOG.trace("Get a list of tasks.");
        final JSONArray json = new JSONArray();
        for (int[] folderAndTask : folderAndTaskIds) {
            final JSONObject json2 = new JSONObject();
            json2.put(DataFields.ID, folderAndTask[1]);
            json2.put(FolderChildFields.FOLDER_ID, folderAndTask[0]);
            json.put(json2);
        }
        final ByteArrayInputStream bais = new ByteArrayInputStream(json
            .toString().getBytes("UTF-8"));
        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION,
            AJAXServlet.ACTION_LIST);
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        final StringBuilder sb = new StringBuilder();
        for (int i : columns) {
            sb.append(i);
            sb.append(',');
        }
        sb.delete(sb.length() - 1, sb.length());
        parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, sb.toString());
        final WebRequest req = new PutMethodWebRequest(PROTOCOL + hostName
            +TASKS_URL + parameter.getURLParameters(), bais, AJAXServlet
            .CONTENTTYPE_JAVASCRIPT);
        final WebResponse resp = conversation.getResponse(req);
        assertEquals("Response code is not okay.", 200, resp.getResponseCode());
        final String body = resp.getText();
        LOG.trace("Response body: " + body);
        final Response response = ResponseParser.parse(body);
        assertFalse(response.getErrorMessage(), response.hasError());
        return response;
    }

    public static int getPrivateTaskFolder(final WebConversation conversation,
        final String hostName, final String sessionId)
        throws MalformedURLException, IOException, SAXException, JSONException,
        OXException {
        final FolderObject myTasks = FolderTest.getStandardTaskFolder(
            conversation, hostName, sessionId);
        return myTasks.getObjectID();
    }
}
