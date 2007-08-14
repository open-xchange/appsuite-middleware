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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

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
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.AbstractAJAXTest;
import com.openexchange.ajax.FolderTest;
import com.openexchange.ajax.config.ConfigTools;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.TaskFields;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.parser.TaskParser;
import com.openexchange.ajax.task.actions.AllRequest;
import com.openexchange.ajax.task.actions.AllResponse;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.DeleteResponse;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.ListRequest;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.ajax.writer.TaskWriter;
import com.openexchange.api2.OXException;
import com.openexchange.configuration.ConfigurationException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TaskException;
import com.openexchange.tools.URLParameter;
import com.openexchange.tools.servlet.AjaxException;

/**
 * Utility class that contains all methods for making task requests to the
 * server.
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class TaskTools extends Assert {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(TaskTools.class);

    /**
     * To use character encoding.
     */
    private static final String ENCODING = "UTF-8";

    /**
     * URL of the tasks AJAX interface.
     */
    private static final String TASKS_URL = "/ajax/tasks";
    
    /**
     * Prevent instanciation.
     */
    private TaskTools() {
        super();
    }

    /**
     * @return the identifier of the private task folder of the primary user.
     * @throws IOException if the communication with the server fails.
     * @throws SAXException if a SAX error occurs.
     * @throws JSONException if parsing of serialized json fails.
     * @throws OXException if reading the folders fails.
     * @deprecated Use {@link AJAXClient#getPrivateTaskFolder()}.
     */
    public static int getPrivateTaskFolder(final WebConversation conversation,
        final String hostName, final String sessionId)
        throws IOException, SAXException, JSONException,
        OXException {
        final FolderObject myTasks = FolderTest.getStandardTaskFolder(
            conversation, hostName, sessionId);
        return myTasks.getObjectID();
    }

    /**
     * This method implements storing of a task through the AJAX interface.
     * @param conversation WebConversation.
     * @param hostName Host name of the server.
     * @param sessionId Session identifier of the user.
     * @param task Task to store.
     * @return the reponse object of inserting the task.
     * @throws JSONException if parsing of serialized json fails.
     * @throws SAXException if a SAX error occurs.
     * @throws IOException if the communication with the server fails.
     * @throws ConfigurationException 
     * @deprecated use {@link #insert(AJAXSession, TaskInsertRequest)}
     */
    public static Response insertTask(final WebConversation conversation,
        final String hostName, final String sessionId, final Task task)
        throws JSONException, IOException, SAXException, AjaxException, ConfigurationException {
        LOG.trace("Inserting task.");
        final TimeZone timezone = ConfigTools.getTimeZone(conversation,
            hostName, sessionId);
		final JSONObject jsonObj = new JSONObject();
        new TaskWriter( timezone).writeTask(task, jsonObj);
        final String object = jsonObj.toString();
        LOG.trace(object);
        final ByteArrayInputStream bais = new ByteArrayInputStream(object
            .getBytes(ENCODING));
        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION,
            AJAXServlet.ACTION_NEW);
        parameter.setParameter(AJAXServlet.PARAMETER_FOLDERID,
            String.valueOf(task.getParentFolderID()));
        final WebRequest req = new PutMethodWebRequest(AbstractAJAXTest.PROTOCOL
            + hostName + TASKS_URL + parameter.getURLParameters(), bais,
            AJAXServlet.CONTENTTYPE_JAVASCRIPT);
        final WebResponse resp = conversation.getResponse(req);
        assertEquals("Response code is not okay.", HttpServletResponse.SC_OK,
            resp.getResponseCode());
        final String body = resp.getText();
        LOG.trace("Response body: " + body);
        return Response.parse(body);
    }

    public static InsertResponse insert(final AJAXSession session,
        final InsertRequest request) throws AjaxException, IOException,
        SAXException, JSONException {
        return (InsertResponse) Executor.execute(session, request);
    }

    public static InsertResponse insert(final AJAXClient client,
        final InsertRequest request) throws AjaxException, IOException,
        SAXException, JSONException {
        return insert(client.getSession(), request);
    }

    /**
     * @deprecated use {@link #update(AJAXSession, UpdateRequest)}
     */
    public static Response updateTask(final WebConversation conversation,
        final String hostName, final String sessionId, final int folderId,
        final int taskId,
        final JSONObject json, final Date lastModified) throws JSONException,
        IOException, SAXException {
        LOG.trace("Updating task.");
        final String object = json.toString();
        LOG.trace(object);
        final ByteArrayInputStream bais = new ByteArrayInputStream(object
            .getBytes(ENCODING));
        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION,
            AJAXServlet.ACTION_UPDATE);
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        parameter.setParameter(AJAXServlet.PARAMETER_INFOLDER,
            String.valueOf(folderId));
        parameter.setParameter(AJAXServlet.PARAMETER_ID,
            String.valueOf(taskId));
        parameter.setParameter(AJAXServlet.PARAMETER_TIMESTAMP,
            String.valueOf(lastModified.getTime()));
        final WebRequest req = new PutMethodWebRequest(AbstractAJAXTest.PROTOCOL
            + hostName + TASKS_URL + parameter.getURLParameters(), bais,
            AJAXServlet.CONTENTTYPE_JAVASCRIPT);
        final WebResponse resp = conversation.getResponse(req);
        assertEquals("Response code is not okay.", HttpServletResponse.SC_OK,
            resp.getResponseCode());
        final String body = resp.getText();
        LOG.trace("Response body: " + body);
        return Response.parse(body);
    }

    /**
     * @deprecated use {@link #update(AJAXSession, UpdateRequest)}
     */
    public static Response updateTask(final WebConversation conversation,
        final String hostName, final String sessionId, final int folderId,
        final Task task, final Date lastModified) throws JSONException,
        IOException, SAXException, AjaxException, ConfigurationException {
        final TimeZone timeZone = getUserTimeZone(conversation, hostName,
            sessionId);
		final JSONObject jsonObj = new JSONObject();
        new TaskWriter( timeZone).writeTask(task, jsonObj);
        return updateTask(conversation, hostName, sessionId, folderId, task
            .getObjectID(), jsonObj, lastModified);
    }

    public static UpdateResponse update(final AJAXSession session,
        final UpdateRequest request) throws AjaxException, IOException,
        SAXException, JSONException {
        return (UpdateResponse) Executor.execute(session, request);
    }

    public static UpdateResponse update(final AJAXClient client,
        final UpdateRequest request) throws AjaxException, IOException,
        SAXException, JSONException {
        return update(client.getSession(), request);
    }

    /**
     * @deprecated Use {@link AJAXClient#getTimeZone()}.
     */
    public static TimeZone getUserTimeZone(final WebConversation conversation,
        final String hostName, final String sessionId) throws IOException,
        SAXException, JSONException, AjaxException, ConfigurationException {
        return ConfigTools.getTimeZone(conversation, hostName, sessionId);
    }

    /**
     * @deprecated use {@link #get(AJAXSession, GetRequest)}
     */
    public static Response getTask(final WebConversation conversation,
        final String hostName, final String sessionId, final int folderId,
        final int taskId) throws IOException, SAXException, JSONException,
        OXException, AjaxException, ConfigurationException {
        LOG.trace("Getting task.");
        final WebRequest req = new GetMethodWebRequest(AbstractAJAXTest.PROTOCOL
            + hostName + TASKS_URL);
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
        final TimeZone timeZone = getUserTimeZone(conversation, hostName,
            sessionId);
        new TaskParser(timeZone).parse(task, (JSONObject) response.getData());
        response.setData(task);
        return response;
    }

    public static GetResponse get(final AJAXSession session,
        final GetRequest request) throws AjaxException, IOException,
        SAXException, JSONException {
        return (GetResponse) Executor.execute(session, request);
    }

    public static GetResponse get(final AJAXClient client,
        final GetRequest request) throws AjaxException, IOException,
        SAXException, JSONException {
        return get(client.getSession(), request);
    }
    /**
     * @param folderAndTaskId Contains the folder identifier with the index
     * <code>0</code> and the task identifier with the index <code>1</code>.
     * @throws JSONException if parsing of serialized json fails.
     * @throws SAXException if a SAX error occurs.
     * @throws IOException if the communication with the server fails.
     * @deprecated use {@link #delete(AJAXSession, DeleteRequest)}
     */
    public static void deleteTask(final WebConversation conversation,
        final String hostName, final String sessionId, final Date lastUpdate,
        final int folder, final int task) throws IOException, SAXException,
        JSONException {
        LOG.trace("Deleting tasks.");
        final JSONObject json = new JSONObject();
        json.put(AJAXServlet.PARAMETER_ID, task);
        json.put(AJAXServlet.PARAMETER_INFOLDER, folder);
        final ByteArrayInputStream bais = new ByteArrayInputStream(json
            .toString().getBytes(ENCODING));
        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION,
            AJAXServlet.ACTION_DELETE);
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        parameter.setParameter(AJAXServlet.PARAMETER_TIMESTAMP,
            String.valueOf(lastUpdate.getTime()));
        final WebRequest req = new PutMethodWebRequest(AbstractAJAXTest.PROTOCOL
            + hostName + TASKS_URL + parameter.getURLParameters(), bais,
            AJAXServlet.CONTENTTYPE_JAVASCRIPT);
        final WebResponse resp = conversation.getResponse(req);
        assertEquals("Response code is not okay.", HttpServletResponse.SC_OK,
            resp.getResponseCode());
        final Response response = Response.parse(resp.getText());
        assertFalse(response.getErrorMessage(), response.hasError());
    }

    public static DeleteResponse delete(final AJAXSession session,
        final DeleteRequest request) throws AjaxException, IOException,
        SAXException, JSONException {
        return (DeleteResponse) Executor.execute(session, request);
    }

    public static DeleteResponse delete(final AJAXClient client,
        final DeleteRequest request) throws AjaxException, IOException,
        SAXException, JSONException {
        return delete(client.getSession(), request);
    }

    /**
     * @deprecated use {@link #all(AJAXSession, AllRequest)}.
     */
    public static Response getAllTasksInFolder(
        final WebConversation conversation, final String hostName,
        final String sessionId, final int folderId, final int[] columns,
        final int sort, final String order) throws IOException, SAXException,
        JSONException {
        LOG.trace("Getting all task in a folder.");
        final WebRequest req = new GetMethodWebRequest(AbstractAJAXTest.PROTOCOL
            + hostName + TASKS_URL);
        req.setParameter(AJAXServlet.PARAMETER_ACTION,
            AJAXServlet.ACTION_ALL);
        req.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        req.setParameter(AJAXServlet.PARAMETER_FOLDERID,
            String.valueOf(folderId));
        final StringBuilder columnSB = new StringBuilder();
        for (int i : columns) {
            columnSB.append(i);
            columnSB.append(',');
        }
        columnSB.delete(columnSB.length() - 1, columnSB.length());
        req.setParameter(AJAXServlet.PARAMETER_COLUMNS, columnSB.toString());
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

    public static AllResponse all(final AJAXSession session,
        final AllRequest request) throws AjaxException, IOException,
        SAXException, JSONException {
        return (AllResponse) Executor.execute(session, request);
    }

    public static Response getUpdatedTasks(final WebConversation conversation,
        final String hostName, final String sessionId, final int folderId,
        final int[] columns, final int sort, final String order,
        final Date lastModified) throws IOException, SAXException,
        JSONException {
        LOG.trace("Getting updated tasks in a folder.");
        final WebRequest req = new GetMethodWebRequest(AbstractAJAXTest.PROTOCOL
            + hostName + TASKS_URL);
        req.setParameter(AJAXServlet.PARAMETER_ACTION,
            AJAXServlet.ACTION_UPDATES);
        req.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        req.setParameter(AJAXServlet.PARAMETER_FOLDERID,
            String.valueOf(folderId));
        final StringBuilder columnSB = new StringBuilder();
        for (int i : columns) {
            columnSB.append(i);
            columnSB.append(',');
        }
        columnSB.delete(columnSB.length() - 1, columnSB.length());
        req.setParameter(AJAXServlet.PARAMETER_COLUMNS, columnSB.toString());
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

    /**
     * @deprecated Use {@link #list(AJAXClient, ListRequest)}.
     */
    public static Response getTaskList(final WebConversation conversation,
        final String hostName, final String sessionId,
        final int[][] folderAndTaskIds, final int[] columns)
        throws IOException, SAXException, JSONException {
        LOG.trace("Get a list of tasks.");
        final JSONArray json = new JSONArray();
        for (int[] folderAndTask : folderAndTaskIds) {
            final JSONObject json2 = new JSONObject();
            json2.put(TaskFields.ID, folderAndTask[1]);
            json2.put(AJAXServlet.PARAMETER_INFOLDER, folderAndTask[0]);
            json.put(json2);
        }
        final ByteArrayInputStream bais = new ByteArrayInputStream(json
            .toString().getBytes(ENCODING));
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
        final WebRequest req = new PutMethodWebRequest(AbstractAJAXTest.PROTOCOL
            + hostName + TASKS_URL + parameter.getURLParameters(), bais,
            AJAXServlet.CONTENTTYPE_JAVASCRIPT);
        final WebResponse resp = conversation.getResponse(req);
        assertEquals("Response code is not okay.", HttpServletResponse.SC_OK,
            resp.getResponseCode());
        final String body = resp.getText();
        LOG.trace("Response body: " + body);
        final Response response = Response.parse(body);
        assertFalse(response.getErrorMessage(), response.hasError());
        return response;
    }

    public static CommonListResponse list(final AJAXClient client,
        final ListRequest request) throws AjaxException, IOException,
        SAXException, JSONException {
        return (CommonListResponse) Executor.execute(client.getSession(), request);
    }

    public static void confirmTask(final WebConversation conversation,
        final String hostName, final String sessionId, final int folderId,
        final int taskId, final int confirm, final String confirmMessage)
        throws IOException, SAXException, JSONException {
        final JSONObject json = new JSONObject();
        json.put(TaskFields.CONFIRMATION, confirm);
        json.put(TaskFields.FOLDER_ID, folderId);
        json.put(TaskFields.ID, taskId);
        json.put(TaskFields.CONFIRM_MESSAGE, confirmMessage);
        final ByteArrayInputStream bais = new ByteArrayInputStream(json
            .toString().getBytes(ENCODING));
        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION,
            AJAXServlet.ACTION_CONFIRM);
        final WebRequest req = new PutMethodWebRequest(AbstractAJAXTest.PROTOCOL
            + hostName + TASKS_URL + parameter.getURLParameters(), bais,
            AJAXServlet.CONTENTTYPE_JAVASCRIPT);
        final WebResponse resp = conversation.getResponse(req);
        assertEquals("Response code is not okay.", HttpServletResponse.SC_OK,
            resp.getResponseCode());
        final String body = resp.getText();
        LOG.trace("Response body: " + body);
        final Response response = Response.parse(body);
        assertFalse(response.getErrorMessage(), response.hasError());
    }

    /**
     * Executes a searach.
     * @param conversation WebConversation.
     * @param hostName Host name of the server.
     * @param sessionId Session identifier of the user.
     * @param search object with the search patterns.
     * @param columns attributes of the found task that should be returned.
     * @param sort Sort the result by this attribute.
     * @param order Sort direction. (ASC, DESC).
     * @return a response object that data contains the task list.
     * @throws TaskException if the search object contains invalid values.
     * @throws JSONException if parsing of serialized json fails.
     * @throws SAXException if a SAX error occurs.
     * @throws IOException if the communication with the server fails.
     */
    public static Response searchTask(final WebConversation conversation,
        final String hostName, final String sessionId,
        final TaskSearchObject search, final int[] columns, final int sort,
        final String order) throws JSONException, TaskException, IOException,
        SAXException {
        final JSONObject json = TaskSearchJSONWriter.write(search);
        final ByteArrayInputStream bais = new ByteArrayInputStream(json
            .toString().getBytes(ENCODING));
        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION,
            AJAXServlet.ACTION_SEARCH);
        final StringBuilder columnString = new StringBuilder();
        for (int i : columns) {
            columnString.append(i);
            columnString.append(',');
        }
        columnString.delete(columnString.length() - 1, columnString.length());
        parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS,
            columnString.toString());
        if (null != order) {
            parameter.setParameter(AJAXServlet.PARAMETER_SORT,
                String.valueOf(sort));
            parameter.setParameter(AJAXServlet.PARAMETER_ORDER, order);
        }
        final WebRequest req = new PutMethodWebRequest(AbstractAJAXTest.PROTOCOL
            + hostName + TASKS_URL + parameter.getURLParameters(), bais,
            AJAXServlet.CONTENTTYPE_JAVASCRIPT);
        final WebResponse resp = conversation.getResponse(req);
        assertEquals("Response code is not okay.", HttpServletResponse.SC_OK,
            resp.getResponseCode());
        final String body = resp.getText();
        LOG.trace("Response body: " + body);
        final Response response = Response.parse(body);
        assertFalse(response.getErrorMessage(), response.hasError());
        return response;
    }

    /**
     * Extracts the identifier of an inserted task. This method can only be used
     * with the reponse of the method
     * {@link #insertTask(WebConversation, String, String, Task)}.
     * @param response Response object after inserting a task.
     * @return the identifier of the new inserted task.
     * @throws JSONException if the respone object doesn't contain the task
     * identifier.
     */
    public static int extractInsertId(final Response response)
        throws JSONException {
        assertFalse(response.getErrorMessage(), response.hasError());
        final JSONObject data = (JSONObject) response.getData();
        if (!data.has(TaskFields.ID)) {
            fail(response.getErrorMessage());
        }
        final int taskId = data.getInt(TaskFields.ID);
        assertTrue("Problem while inserting task", taskId > 0);
        return taskId;
    }

    public static void compareAttributes(final Task task, final Task reload) {
        assertEquals("Title differs", task.containsTitle(),
            reload.containsTitle());
        assertEquals("Title differs", task.getTitle(), reload.getTitle());
        assertEquals("Private Flag differs", task.containsPrivateFlag(),
            reload.containsPrivateFlag());
        assertEquals("Private Flag differs", task.getPrivateFlag(),
            reload.getPrivateFlag());
        /* Not implemented in parser
        assertEquals("Creation date differs", task.containsCreationDate(),
            reload.containsCreationDate());
        assertEquals("Creation date differs", task.getCreationDate(),
            reload.getCreationDate());
        assertEquals("Last modified differs", task.containsLastModified(),
            reload.containsLastModified());
        assertEquals("Last modified differs", task.getLastModified(),
            reload.getLastModified());
        */
        assertEquals("Start date differs", task.containsStartDate(),
            reload.containsStartDate());
        assertEquals("Start date differs", task.getStartDate(),
            reload.getStartDate());
        assertEquals("End date differs", task.containsEndDate(),
            reload.containsEndDate());
        assertEquals("End date differs", task.getEndDate(),
            reload.getEndDate());
//        assertEquals("After complete differs", task.containsAfterComplete(),
//            reload.containsAfterComplete());
//        assertEquals("After complete differs", task.getAfterComplete(),
//            reload.getAfterComplete());
//        task.setNote("Description");
        assertEquals("Status differs", task.containsStatus(),
            reload.containsStatus());
        assertEquals("Status differs", task.getStatus(), reload.getStatus());
        assertEquals("Priority differs", task.containsPriority(),
            reload.containsPriority());
        assertEquals("Priority differs", task.getPriority(),
            reload.getPriority());
        assertEquals("PercentComplete differs", task.containsPercentComplete(),
            reload.containsPercentComplete());
        assertEquals("PercentComplete differs", task.getPercentComplete(),
            reload.getPercentComplete());
//        task.setCategories("Categories");
        assertEquals("TargetDuration differs", task.containsTargetDuration(),
            reload.containsTargetDuration());
        assertEquals("TargetDuration differs", task.getTargetDuration(),
            reload.getTargetDuration());
        assertEquals("ActualDuration differs", task.containsActualDuration(),
            reload.containsActualDuration());
        assertEquals("ActualDuration differs", task.getActualDuration(),
            reload.getActualDuration());
//        task.setTargetCosts(1.0f);
//        task.setActualCosts(1.0f);
//        task.setCurrency("\u20ac");
//        task.setTripMeter("trip meter");
//        task.setBillingInformation("billing information");
//        task.setCompanies("companies");
    }
}
