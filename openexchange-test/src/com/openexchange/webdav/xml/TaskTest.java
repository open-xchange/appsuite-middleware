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

package com.openexchange.webdav.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.TimeZone;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.TestException;
import com.openexchange.webdav.xml.fields.DataFields;
import com.openexchange.webdav.xml.parser.ResponseParser;
import com.openexchange.webdav.xml.request.PropFindMethod;
import com.openexchange.webdav.xml.types.Response;

public class TaskTest extends AbstractWebdavXMLTest {

    protected int taskFolderId = -1;

    protected String userParticipant2 = null;

    protected String userParticipant3 = null;

    protected String groupParticipant = null;

    protected Date startTime = null;

    protected Date endTime = null;

    protected Date dateCompleted = null;

    protected static final long d7 = 604800000;

    private static final String TASK_URL = "/servlet/webdav.tasks";

    public TaskTest(final String name) {
        super(name);
    }

    protected static Date decrementDate(final Date date) {
        return new Date(date.getTime() - 1);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        startTime = new Date(c.getTimeInMillis());
        endTime = new Date(startTime.getTime() + dayInMillis);

        dateCompleted = new Date(c.getTimeInMillis() + d7);

        userParticipant2 = AbstractConfigWrapper.parseProperty(webdavProps, "user_participant2", "");
        userParticipant3 = AbstractConfigWrapper.parseProperty(webdavProps, "user_participant3", "");

        groupParticipant = AbstractConfigWrapper.parseProperty(webdavProps, "group_participant", "");

        final FolderObject folderObj = FolderTest.getTaskDefaultFolder(webCon, PROTOCOL + hostName, login, password, context);
        taskFolderId = folderObj.getObjectID();
        userId = folderObj.getCreatedBy();
    }

    public static void compareObject(final Task taskObj1, final Task taskObj2) throws Exception {
        assertEquals("id is not equals", taskObj1.getObjectID(), taskObj2.getObjectID());
        assertEqualsAndNotNull("title is not equals", taskObj1.getTitle(), taskObj2.getTitle());
        assertEqualsAndNotNull("start is not equals", taskObj1.getStartDate(), taskObj2.getStartDate());
        assertEqualsAndNotNull("end is not equals", taskObj1.getEndDate(), taskObj2.getEndDate());
        assertEquals("folder id is not equals", taskObj1.getParentFolderID(), taskObj2.getParentFolderID());
        assertEquals("private flag is not equals", taskObj1.getPrivateFlag(), taskObj2.getPrivateFlag());
        assertEquals("alarm is not equals", taskObj1.getAlarm(), taskObj2.getAlarm());
        assertEqualsAndNotNull("note is not equals", taskObj1.getNote(), taskObj2.getNote());
        assertEqualsAndNotNull("categories is not equals", taskObj1.getCategories(), taskObj2.getCategories());
        assertEqualsAndNotNull("actual costs is not equals", taskObj1.getActualCosts(), taskObj2.getActualCosts());
        assertEqualsAndNotNull("actual duration", taskObj1.getActualDuration(), taskObj2.getActualDuration());
        assertEqualsAndNotNull("billing information", taskObj1.getBillingInformation(), taskObj2.getBillingInformation());
        assertEqualsAndNotNull("companies", taskObj1.getCompanies(), taskObj2.getCompanies());
        assertEqualsAndNotNull("currency", taskObj1.getCurrency(), taskObj2.getCurrency());
        assertEqualsAndNotNull("date completed", taskObj1.getDateCompleted(), taskObj2.getDateCompleted());
        assertEqualsAndNotNull("percent complete", taskObj1.getPercentComplete(), taskObj2.getPercentComplete());
        assertEqualsAndNotNull("priority", taskObj1.getPriority(), taskObj2.getPriority());
        assertEqualsAndNotNull("status", taskObj1.getStatus(), taskObj2.getStatus());
        assertEqualsAndNotNull("target costs", taskObj1.getTargetCosts(), taskObj2.getTargetCosts());
        assertEqualsAndNotNull("target duration", taskObj1.getTargetDuration(), taskObj2.getTargetDuration());
        assertEqualsAndNotNull("trip meter", taskObj1.getTripMeter(), taskObj2.getTripMeter());

        assertEqualsAndNotNull("participants are not equals" , participants2String(taskObj1.getParticipants()), participants2String(taskObj2.getParticipants()));
        assertEqualsAndNotNull("users are not equals" , users2String(taskObj1.getUsers()), users2String(taskObj2.getUsers()));
    }

    protected Task createTask(final String title) throws Exception {
        final Task taskObj = new Task();
        taskObj.setTitle(title);
        taskObj.setStartDate(startTime);
        taskObj.setEndDate(endTime);
        taskObj.setParentFolderID(taskFolderId);
        taskObj.setStatus(Task.IN_PROGRESS);
        taskObj.setPercentComplete(50);

        return taskObj;
    }

    public static int insertTask(final WebConversation webCon, Task taskObj, String host, final String login, final String password, String context) throws Exception {
        host = AbstractWebdavXMLTest.appendPrefix(host);

        int objectId = 0;

        taskObj.removeObjectID();

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final Element eProp = new Element("prop", webdav);

        final TaskWriter taskWriter = new TaskWriter();
        taskWriter.addContent2PropElement(eProp, taskObj, false);

        final Document doc = addProp2Document(eProp);
        final XMLOutputter xo = new XMLOutputter();
        xo.output(doc, baos);

        baos.toByteArray();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final WebRequest req = new PutMethodWebRequest(host + TASK_URL, bais, "text/javascript");
        req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password, context));
        final WebResponse resp = webCon.getResponse(req);

        assertEquals(207, resp.getResponseCode());

        bais = new ByteArrayInputStream(resp.getText().getBytes());
        final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.TASK);

        assertEquals("check response", 1, response.length);

        if (response[0].hasError()) {
            throw new TestException(response[0].getErrorMessage());
        } else {
            taskObj = (Task)response[0].getDataObject();
            objectId = taskObj.getObjectID();

            assertNotNull("last modified is null", taskObj.getLastModified());
            assertTrue("last modified is not > 0", taskObj.getLastModified().getTime() > 0);
        }

        assertEquals("check response status", 200, response[0].getStatus());

        assertTrue("check objectId", objectId > 0);

        return objectId;
    }

    public static int[] insertTasks(final WebConversation webCon, String host, final String login, final String password, String context, final Task... tasks) throws Exception{
        host = AbstractWebdavXMLTest.appendPrefix(host);
        final int[] objectIds = new int[tasks.length];

        final TaskWriter taskWriter = new TaskWriter();

        final Element rootElement = new Element("propertyupdate", webdav);
        rootElement.addNamespaceDeclaration(XmlServlet.NS);

        final Document doc =  new Document(rootElement);

        for(final Task taskObj : tasks) {
            final Element eProp = new Element("prop", webdav);
            taskWriter.addContent2PropElement(eProp, taskObj, false);
            final Element eSet = new Element("set", webdav);
            eSet.addContent(eProp);
            rootElement.addContent(eSet);
        }

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final XMLOutputter xo = new XMLOutputter();
        xo.output(doc, baos);

        baos.toByteArray();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final WebRequest req = new PutMethodWebRequest(host + TASK_URL, bais, "text/javascript");
        req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password, context));
        final WebResponse resp = webCon.getResponse(req);

        assertEquals(207, resp.getResponseCode());

        bais = new ByteArrayInputStream(resp.getText().getBytes());
        final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.TASK);

        assertEquals("check response", tasks.length, response.length);

        for(int i = 0; i < tasks.length; i++) {
            if (response[i].hasError()) {
                throw new TestException(response[i].getErrorMessage());
            } else {
                final Task taskObj = (Task)response[i].getDataObject();
                objectIds[i] = taskObj.getObjectID();

                assertNotNull("last modified is null", taskObj.getLastModified());
                assertTrue("last modified is not > 0", taskObj.getLastModified().getTime() > 0);
            }
        }

        return objectIds;
    }

    public static void updateTask(final WebConversation webCon, final Task taskObj, final int objectId, final int inFolder, final String host, final String login, final String password, String context) throws Exception {
        updateTask(webCon, taskObj, objectId, inFolder, new Date(System.currentTimeMillis() + APPEND_MODIFIED), host, login, password, context);
    }

    public static void updateTask(final WebConversation webCon, Task taskObj, int objectId, final int inFolder, final Date lastModified, String host, final String login, final String password, String context) throws Exception {
        host = AbstractWebdavXMLTest.appendPrefix(host);

        taskObj.setObjectID(objectId);
        taskObj.setLastModified(lastModified);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final Element eProp = new Element("prop", webdav);

        final TaskWriter appointmentWriter = new TaskWriter();
        appointmentWriter.addContent2PropElement(eProp, taskObj, false);

        final Document doc = addProp2Document(eProp);
        final XMLOutputter xo = new XMLOutputter();
        xo.output(doc, baos);

        baos.toByteArray();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final WebRequest req = new PutMethodWebRequest(host + TASK_URL, bais, "text/javascript");
        req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password, context));
        final WebResponse resp = webCon.getResponse(req);

        assertEquals(207, resp.getResponseCode());

        bais = new ByteArrayInputStream(resp.getText().getBytes());
        final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.TASK);

        assertEquals("check response", 1, response.length);

        if (response[0].hasError()) {
            throw new TestException(response[0].getErrorMessage());
        } else {
            taskObj = (Task)response[0].getDataObject();
            objectId = taskObj.getObjectID();

            assertNotNull("last modified is null", taskObj.getLastModified());
            assertTrue("last modified is not > 0", taskObj.getLastModified().getTime() > 0);
        }

        assertEquals("check response status", 200, response[0].getStatus());
    }

    public static int[] deleteTask(final WebConversation webCon, final int[][] objectIdAndFolderId, final String host, final String login, final String password, String context) throws Exception {
        new ArrayList();

        for (int a = 0; a < objectIdAndFolderId.length; a++) {
            deleteTask(webCon, objectIdAndFolderId[a][0], objectIdAndFolderId[a][1], host, login, password, context);
        }

        return new int[] { };
    }

    public static void deleteTask(final WebConversation webCon, final int objectId, final int inFolder, final String host, final String login, final String password, String context) throws Exception {
        deleteTask(webCon, objectId, inFolder, new Date(System.currentTimeMillis() + APPEND_MODIFIED), host, login, password, context);
    }

    public static void deleteTask(final WebConversation webCon, final int objectId, final int inFolder, final Date lastModified, String host, final String login, final String password, String context) throws Exception {
        host = AbstractWebdavXMLTest.appendPrefix(host);

        final Element rootElement = new Element("multistatus", webdav);
        rootElement.addNamespaceDeclaration(XmlServlet.NS);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();


        final Task taskObj = new Task();
        taskObj.setObjectID(objectId);
        taskObj.setParentFolderID(inFolder);
        taskObj.setLastModified(lastModified);

        final Element eProp = new Element("prop", webdav);

        final TaskWriter taskWriter = new TaskWriter();
        taskWriter.addContent2PropElement(eProp, taskObj, false);

        final Element eMethod = new Element("method", XmlServlet.NS);
        eMethod.addContent("DELETE");
        eProp.addContent(eMethod);

        rootElement.addContent(addProp2PropertyUpdate(eProp));

        final Document doc = new Document(rootElement);
        final XMLOutputter xo = new XMLOutputter();
        xo.output(doc, baos);

        baos.toByteArray();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final WebRequest req = new PutMethodWebRequest(host + TASK_URL, bais, "text/javascript");
        req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password, context));
        final WebResponse resp = webCon.getResource(req);

        assertEquals(207, resp.getResponseCode());

        bais = new ByteArrayInputStream(resp.getText().getBytes());
        final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.TASK);

        if (response[0].hasError()) {
            throw new TestException(response[0].getErrorMessage());
        }

        assertEquals("check response status", 200, response[0].getStatus());
    }

    public static void confirmTask(final WebConversation webCon, final int objectId, final int confirm, final String confirmMessage, String host, final String login, final String password, String context) throws Exception {
        host = AbstractWebdavXMLTest.appendPrefix(host);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final Element eProp = new Element("prop", webdav);

        final Element eObjectId = new Element(DataFields.OBJECT_ID, XmlServlet.NS);
        eObjectId.addContent(String.valueOf(objectId));
        eProp.addContent(eObjectId);

        final Element eMethod = new Element("method", XmlServlet.NS);
        eMethod.addContent("CONFIRM");
        eProp.addContent(eMethod);

        final Element eConfirm = new Element("confirm", XmlServlet.NS);
        switch (confirm) {
            case CalendarObject.NONE:
                eConfirm.addContent("none");
                break;
            case CalendarObject.ACCEPT:
                eConfirm.addContent("accept");
                break;
            case CalendarObject.DECLINE:
                eConfirm.addContent("decline");
                break;
            default:
                eConfirm.addContent("invalid");
                break;
        }

        eProp.addContent(eConfirm);

        final Document doc = addProp2Document(eProp);
        final XMLOutputter xo = new XMLOutputter();
        xo.output(doc, baos);

        baos.toByteArray();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final WebRequest req = new PutMethodWebRequest(host + TASK_URL, bais, "text/javascript");
        req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password, context));
        final WebResponse resp = webCon.getResponse(req);

        assertEquals(207, resp.getResponseCode());

        bais = new ByteArrayInputStream(resp.getText().getBytes());
        final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.TASK);

        assertEquals("check response", 1, response.length);

        if (response[0].hasError()) {
            fail("xml error: " + response[0].getErrorMessage());
        }

        assertEquals("check response status", 200, response[0].getStatus());
    }

    public static int[] listTask(final WebConversation webCon, final int inFolder, String host, final String login, final String password, String context) throws Exception {
        host = AbstractWebdavXMLTest.appendPrefix(host);

        final Element ePropfind = new Element("propfind", webdav);
        final Element eProp = new Element("prop", webdav);

        final Element eFolderId = new Element("folder_id", XmlServlet.NS);
        final Element eObjectmode = new Element("objectmode", XmlServlet.NS);

        eFolderId.addContent(String.valueOf(inFolder));
        eObjectmode.addContent("LIST");

        eProp.addContent(eFolderId);
        eProp.addContent(eObjectmode);

        ePropfind.addContent(eProp);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final Document doc = new Document(ePropfind);

        final XMLOutputter xo = new XMLOutputter();
        xo.output(doc, baos);

        baos.flush();

        final HttpClient httpclient = new HttpClient();

        httpclient.getState().setCredentials(AuthScope.ANY, getCredentials(login, password, context));
        final PropFindMethod propFindMethod = new PropFindMethod(host + TASK_URL);
        propFindMethod.setDoAuthentication( true );

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        propFindMethod.setRequestBody(bais);

        final int status = httpclient.executeMethod(propFindMethod);

        assertEquals("check propfind response", 207, status);

        final InputStream body = propFindMethod.getResponseBodyAsStream();
        final Response[] response = ResponseParser.parse(new SAXBuilder().build(body), Types.TASK, true);

        assertEquals("response length not is 1", 1, response.length);

        return (int[])response[0].getDataObject();
    }

    private static Credentials getCredentials(String login, String password,
			String context) {
		
    	return new UsernamePasswordCredentials((context == null || context.equals("")) ? login : login+"@"+context, password);
	}

	public static Task[] listTask(final WebConversation webCon, final int inFolder, final Date modified, final boolean changed, final boolean deleted, String host, final String login, final String password, String context) throws Exception {
        host = AbstractWebdavXMLTest.appendPrefix(host);

        if (!changed && !deleted) {
            return new Task[] { };
        }

        final Element ePropfind = new Element("propfind", webdav);
        final Element eProp = new Element("prop", webdav);

        final Element eFolderId = new Element("folder_id", XmlServlet.NS);
        final Element eLastSync = new Element("lastsync", XmlServlet.NS);
        final Element eObjectmode = new Element("objectmode", XmlServlet.NS);

        eFolderId.addContent(String.valueOf(inFolder));
        eLastSync.addContent(String.valueOf(modified.getTime()));

        final StringBuffer objectMode = new StringBuffer();

        if (changed) {
            objectMode.append("NEW_AND_MODIFIED,");
        }

        if (deleted) {
            objectMode.append("DELETED,");
        }

        objectMode.delete(objectMode.length()-1, objectMode.length());

        eObjectmode.addContent(objectMode.toString());
        eProp.addContent(eObjectmode);

        ePropfind.addContent(eProp);
        eProp.addContent(eFolderId);
        eProp.addContent(eLastSync);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final Document doc = new Document(ePropfind);

        final XMLOutputter xo = new XMLOutputter();
        xo.output(doc, baos);

        baos.flush();

        final HttpClient httpclient = new HttpClient();

        httpclient.getState().setCredentials(AuthScope.ANY, getCredentials(login, password, context));
        final PropFindMethod propFindMethod = new PropFindMethod(host + TASK_URL);
        propFindMethod.setDoAuthentication( true );

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        propFindMethod.setRequestBody(bais);

        final int status = httpclient.executeMethod(propFindMethod);

        assertEquals("check propfind response", 207, status);

        final InputStream body = propFindMethod.getResponseBodyAsStream();
        final Response[] response = ResponseParser.parse(new SAXBuilder().build(body), Types.TASK);

        final Task[] taskArray = new Task[response.length];
        for (int a = 0; a < taskArray.length; a++) {
            if (response[a].hasError()) {
                fail("xml error: " + response[a].getErrorMessage());
            }

            taskArray[a] = (Task)response[a].getDataObject();
            assertNotNull("last modified is null", taskArray[a].getLastModified());
        }

        return taskArray;
    }

    public static Task loadTask(final WebConversation webCon, final int objectId, final int inFolder, String host, final String login, final String password, String context) throws OXException, Exception {
        host = AbstractWebdavXMLTest.appendPrefix(host);

        final Element ePropfind = new Element("propfind", webdav);
        final Element eProp = new Element("prop", webdav);

        final Element eFolderId = new Element("folder_id", XmlServlet.NS);
        final Element eObjectId = new Element("object_id", XmlServlet.NS);

        eFolderId.addContent(String.valueOf(inFolder));
        eObjectId.addContent(String.valueOf(objectId));

        ePropfind.addContent(eProp);
        eProp.addContent(eFolderId);
        eProp.addContent(eObjectId);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final Document doc = new Document(ePropfind);

        final XMLOutputter xo = new XMLOutputter();
        xo.output(doc, baos);

        baos.flush();

        final HttpClient httpclient = new HttpClient();

        httpclient.getState().setCredentials(AuthScope.ANY, getCredentials(login, password, context));
        final PropFindMethod propFindMethod = new PropFindMethod(host + TASK_URL);
        propFindMethod.setDoAuthentication( true );

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        propFindMethod.setRequestBody(bais);

        final int status = httpclient.executeMethod(propFindMethod);

        assertEquals("check propfind response", 207, status);

        final InputStream body = propFindMethod.getResponseBodyAsStream();
        final Response[] response = ResponseParser.parse(new SAXBuilder().build(body), Types.TASK);

        assertEquals("check response" , 1, response.length);

        if (response[0].hasError()) {
            throw new TestException(response[0].getErrorMessage());
        }

        assertEquals("check response status", 200, response[0].getStatus());

        return (Task)response[0].getDataObject();
    }

    private static HashSet participants2String(final Participant[] participant) throws Exception {
        if (participant == null) {
            return null;
        }

        final HashSet hs = new HashSet();

        for (int a = 0; a < participant.length; a++) {
            hs.add(participant2String(participant[a]));
        }

        return hs;
    }

    private static String participant2String(final Participant p) throws Exception {
        final StringBuffer sb = new StringBuffer();
        sb.append("T" + p.getType());
        sb.append("ID" + p.getIdentifier());

        return sb.toString();
    }

    private static HashSet users2String(final UserParticipant[] users) throws Exception {
        if (users == null) {
            return null;
        }

        final HashSet hs = new HashSet();

        for (int a = 0; a < users.length; a++) {
            hs.add(user2String(users[a]));
        }

        return hs;
    }

    private static String user2String(final UserParticipant user) throws Exception {
        final StringBuffer sb = new StringBuffer();
        sb.append("ID" + user.getIdentifier());
        sb.append("C" + user.getConfirm());

        return sb.toString();
    }
}
