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
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.parser.TaskParser;
import com.openexchange.ajax.writer.TaskWriter;
import com.openexchange.api.OXException;
import com.openexchange.api.OXObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
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
        // TODO read folder id from folder interface
        final int folderId = 62;

        final int number = countTasks(getWebConversation(), hostName,
            getSessionId(), folderId);
        LOG.info(number);
    }

    public void testCountPublicFolder() throws Throwable {
        // TODO read folder id from folder interface
        final int folderId = 853;

        final int number = countTasks(getWebConversation(), hostName,
            getSessionId(), folderId);
        LOG.info(number);
    }

    /**
     * Test method for 'com.openexchange.ajax.Tasks.doPut(HttpServletRequest,
     * HttpServletResponse)'
     */
    public void testInsertPrivateTask() throws Throwable {
        final Task task = new Task();
        task.setTitle("Private delegated task");
        task.setPrivateFlag(CommonObject.PRIVATE_FLAG_FALSE);
        task.setCreationDate(new Date());
        task.setLastModified(new Date());
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
        final UserParticipant user = new UserParticipant();
        user.setIdentifier(139);
        // TODO read folder id from folder interface
        final int folderId = 62;

        task.setParentFolderID(folderId);
        task.setCreatedBy(139);
        final int taskId = insertTask(getWebConversation(), hostName,
            getSessionId(), task);
        assertTrue("Problem while inserting private task.", taskId > 0);
        final Task reload = getTask(getWebConversation(), hostName,
            getSessionId(), folderId, taskId);
        LOG.info(reload);
    }

    /**
     * Test method for 'com.openexchange.ajax.Tasks.doPut(HttpServletRequest,
     * HttpServletResponse)'
     */
    public void testInsertDelegatedPrivateTask() throws Throwable {
        final Task task = new Task();
        task.setTitle("Private delegated task");
        task.setPrivateFlag(CommonObject.PRIVATE_FLAG_FALSE);
        task.setCreationDate(new Date());
        task.setLastModified(new Date());
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
        user1.setIdentifier(140);
        final UserParticipant user2 = new UserParticipant();
        user2.setIdentifier(141);
        // TODO read folder id from folder interface
        final int folderId = 62;

        final List<Participant> participants = new ArrayList<Participant>();
        participants.add(user1);
        participants.add(user2);
        task.setParticipants(participants);
        task.setParentFolderID(folderId);
        task.setCreatedBy(139);

        assertTrue("No unique identifier of the new task was returned.",
            insertTask(getWebConversation(), hostName, getSessionId(),
            task) > 0);
    }
    
    /**
     * This method implements storing of a task through the AJAX interface.
     * @param conversation WebConversation.
     * @param hostname Host name of the server.
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
        final String hostname, final String sessionId, final Task task)
        throws JSONException, MalformedURLException, IOException, SAXException {
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
        final WebRequest req = new PutMethodWebRequest(PROTOCOL + hostname
            + TASKS_URL + parameter.getURLParameters(), bais, AJAXServlet
            .CONTENTTYPE_JAVASCRIPT);
        final WebResponse resp = conversation.getResponse(req);
        assertEquals("Response code is not okay.", 200, resp.getResponseCode());
        final String text = resp.getText();
        LOG.trace("!" + text + "!");
        final JSONObject jsonId = new JSONObject(text);
        if (jsonId.has("error")) {
            fail(jsonId.getString("error"));
        }
        final int identifier = jsonId.getInt(OXObject.OBJECT_ID);
        assertTrue("Unique identifier of task is zero.", identifier > 0);
        return identifier;
    }
    
    public static Task getTask(final WebConversation conversation,
        final String hostname, final String sessionId, final int folderId,
        final int taskId) throws MalformedURLException, IOException,
        SAXException, JSONException, OXException {
        final WebRequest req = new GetMethodWebRequest(PROTOCOL + hostname
            + TASKS_URL);
        req.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        req.setParameter(AJAXServlet.PARAMETER_FOLDERID,
            String.valueOf(folderId));
        req.setParameter(AJAXServlet.PARAMETER_ID, String.valueOf(taskId));
        final WebResponse resp = conversation.getResponse(req);
        final JSONObject json = new JSONObject(resp.getText());
        final Task task = new Task();
        new TaskParser().parse(task, json);
        return task;
    }
    
    public static int countTasks(final WebConversation conversation,
        final String hostname, final String sessionId, final int folderId)
        throws MalformedURLException, IOException, SAXException {
        final WebRequest req = new GetMethodWebRequest(PROTOCOL + hostname
            + TASKS_URL);
        req.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        req.setParameter(AJAXServlet.PARAMETER_ACTION,
            AJAXServlet.ACTION_COUNT);
        req.setParameter(AJAXServlet.PARAMETER_FOLDERID,
            String.valueOf(folderId));
        final WebResponse resp = conversation.getResponse(req);
        assertEquals("Response code is not okay.", 200, resp.getResponseCode());
        return Integer.parseInt(resp.getText());
    }
}
