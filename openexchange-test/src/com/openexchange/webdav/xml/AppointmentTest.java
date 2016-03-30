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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.SAXException;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.TestException;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;
import com.openexchange.webdav.xml.appointment.actions.AbstractAppointmentRequest;
import com.openexchange.webdav.xml.fields.DataFields;
import com.openexchange.webdav.xml.parser.ResponseParser;
import com.openexchange.webdav.xml.request.PropFindMethod;
import com.openexchange.webdav.xml.types.Response;

public class AppointmentTest extends AbstractWebdavXMLTest {

    protected int userId = -1;

    protected int userParticipantId2 = -1;

    protected int userParticipantId3 = -1;

    protected int groupParticipantId1 = -1;

    protected int resourceParticipantId1 = -1;

    protected int appointmentFolderId = -1;

    protected String userParticipant2 = null;

    protected String userParticipant3 = null;

    protected String groupParticipant = null;

    protected String resourceParticipant = null;

    protected Date startTime = null;

    protected Date endTime = null;

    /**
     * @deprecated Use {@link AbstractAppointmentRequest#URL} instead
     */
    @Deprecated
    private static final String APPOINTMENT_URL = AbstractAppointmentRequest.URL;

    public AppointmentTest(final String name) {
        super(name);
    }

    /**
     * Gets a newly created {@link Date date} with its time set to {@link Date#getTime()} - <code>1</code> of specified {@link Date date}
     * instance.
     *
     * @param date The date to decrement
     * @return A newly created {@link Date date} with decremented time
     */
    protected static Date decrementDate(final Date date) {
        return new Date(date.getTime() - 1);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        final Calendar c = Calendar.getInstance();
        // Appointments must be in the future to have reminder successfully set.
        c.add(Calendar.DATE, 1);
        c.set(Calendar.HOUR_OF_DAY, 12);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        startTime = new Date(c.getTimeInMillis());
        endTime = new Date(startTime.getTime() + 3600000);

        userParticipant2 = AbstractConfigWrapper.parseProperty(webdavProps, "user_participant2", "");
        userParticipant3 = AbstractConfigWrapper.parseProperty(webdavProps, "user_participant3", "");

        groupParticipant = AbstractConfigWrapper.parseProperty(webdavProps, "group_participant", "");

        resourceParticipant = AbstractConfigWrapper.parseProperty(webdavProps, "resource_participant", "");

        final FolderObject folderObj = FolderTest.getAppointmentDefaultFolder(webCon, PROTOCOL + hostName, login, password, context);
        appointmentFolderId = folderObj.getObjectID();
        userId = folderObj.getCreatedBy();
    }

    public static void compareObject(final Appointment appointmentObj1, final Appointment appointmentObj2) throws Exception {
        assertEquals("id is not equals", appointmentObj1.getObjectID(), appointmentObj2.getObjectID());
        assertEqualsAndNotNull("title is not equals", appointmentObj1.getTitle(), appointmentObj2.getTitle());
        assertEqualsAndNotNull("start is not equals", appointmentObj1.getStartDate(), appointmentObj2.getStartDate());
        assertEqualsAndNotNull("end is not equals", appointmentObj1.getEndDate(), appointmentObj2.getEndDate());
        assertEqualsAndNotNull("location is not equals", appointmentObj1.getLocation(), appointmentObj2.getLocation());
        assertEquals("shown_as is not equals", appointmentObj1.getShownAs(), appointmentObj2.getShownAs());
        assertEquals("folder id is not equals", appointmentObj1.getParentFolderID(), appointmentObj2.getParentFolderID());
        assertEquals("private flag is not equals", appointmentObj1.getPrivateFlag(), appointmentObj2.getPrivateFlag());
        assertEquals("full time is not equals", appointmentObj1.getFullTime(), appointmentObj2.getFullTime());
        assertEquals("label is not equals", appointmentObj1.getLabel(), appointmentObj2.getLabel());
        assertEquals("alarm is not equals", appointmentObj1.getAlarm(), appointmentObj2.getAlarm());
        assertEquals("alarm flag is not equals", appointmentObj1.getAlarmFlag(), appointmentObj2.getAlarmFlag());
        assertEquals("recurrence_type", appointmentObj1.getRecurrenceType(), appointmentObj2.getRecurrenceType());
        assertEquals("interval", appointmentObj1.getInterval(), appointmentObj2.getInterval());
        assertEquals("days", appointmentObj1.getDays(), appointmentObj2.getDays());
        assertEquals("month", appointmentObj1.getMonth(), appointmentObj2.getMonth());
        assertEquals("day_in_month", appointmentObj1.getDayInMonth(), appointmentObj2.getDayInMonth());
        assertEquals("until", appointmentObj1.getUntil(), appointmentObj2.getUntil());
        assertEqualsAndNotNull("note is not equals", appointmentObj1.getNote(), appointmentObj2.getNote());
        assertEqualsAndNotNull("categories is not equals", appointmentObj1.getCategories(), appointmentObj2.getCategories());
        assertEqualsAndNotNull("delete exception is not equals", appointmentObj1.getDeleteException(), appointmentObj2.getDeleteException());

        assertEqualsAndNotNull(
            "participants are not equals",
            participants2String(appointmentObj1.getParticipants()),
            participants2String(appointmentObj2.getParticipants()));
        assertEqualsAndNotNull("users are not equals", users2String(appointmentObj1.getUsers()), users2String(appointmentObj2.getUsers()));
    }

    protected Appointment createAppointmentObject(final String title) throws Exception {
        final Appointment appointmentobject = new Appointment();
        appointmentobject.setTitle(title);
        appointmentobject.setStartDate(startTime);
        appointmentobject.setEndDate(endTime);
        appointmentobject.setLocation("Location");
        appointmentobject.setShownAs(Appointment.ABSENT);
        appointmentobject.setParentFolderID(appointmentFolderId);

        return appointmentobject;
    }

    public static int insertAppointment(final WebConversation webCon, Appointment appointmentObj, String host, final String login, final String password, String context) throws OXException, Exception {
        host = AbstractWebdavXMLTest.appendPrefix(host);

        int objectId = 0;

        appointmentObj.removeObjectID();

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final Element eProp = new Element("prop", webdav);

        final AppointmentWriter appointmentWriter = new AppointmentWriter();
        appointmentWriter.addContent2PropElement(eProp, appointmentObj, false, true);

        final Document doc = addProp2Document(eProp);
        final XMLOutputter xo = new XMLOutputter();
        xo.output(doc, baos);

        baos.toByteArray();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final WebRequest req = new PutMethodWebRequest(host + AbstractAppointmentRequest.URL, bais, "text/xml");
        req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password, context));
        final WebResponse resp = webCon.getResource(req);

        assertEquals(207, resp.getResponseCode());

        bais = new ByteArrayInputStream(resp.getText().getBytes());
        final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.APPOINTMENT);

        assertEquals("check response", 1, response.length);

        if (response[0].hasError()) {
            throw new TestException(response[0].getErrorMessage());
        }

        assertEquals("check response status", 200, response[0].getStatus());

        appointmentObj = (Appointment) response[0].getDataObject();
        objectId = appointmentObj.getObjectID();

        assertNotNull("last modified is null", appointmentObj.getLastModified());
        assertTrue("last modified is not > 0", appointmentObj.getLastModified().getTime() > 0);

        assertTrue("check objectId", objectId > 0);

        return objectId;
    }

    public static int updateAppointment(final WebConversation webCon, final Appointment appointmentObj, final int objectId, final int inFolder, final String host, final String login, final String password, String context) throws OXException, Exception {
        return updateAppointment(
            webCon,
            appointmentObj,
            objectId,
            inFolder,
            new Date(System.currentTimeMillis() + APPEND_MODIFIED),
            host,
            login,
            password,
            context);
    }

    public static int updateAppointment(final WebConversation webCon, Appointment appointmentObj, int objectId, final int inFolder, final Date lastModified, String host, final String login, final String password, String context) throws OXException, Exception {
        host = AbstractWebdavXMLTest.appendPrefix(host);

        appointmentObj.setObjectID(objectId);
        appointmentObj.setLastModified(lastModified);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final Element eProp = new Element("prop", webdav);

        final AppointmentWriter appointmentWriter = new AppointmentWriter();
        appointmentWriter.addContent2PropElement(eProp, appointmentObj, false, true);

        final Document doc = addProp2Document(eProp);
        final XMLOutputter xo = new XMLOutputter();
        xo.output(doc, baos);

        baos.toByteArray();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final WebRequest req = new PutMethodWebRequest(host + AbstractAppointmentRequest.URL, bais, "text/javascript");
        req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password, context));
        final WebResponse resp = webCon.getResource(req);

        assertEquals(207, resp.getResponseCode());

        bais = new ByteArrayInputStream(resp.getText().getBytes());
        final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.APPOINTMENT);

        assertEquals("check response", 1, response.length);

        if (response[0].hasError()) {
            throw new TestException(response[0].getErrorMessage());
        } else {
            appointmentObj = (Appointment) response[0].getDataObject();
            objectId = appointmentObj.getObjectID();

            assertNotNull("last modified is null", appointmentObj.getLastModified());
            assertTrue("last modified is not > 0", appointmentObj.getLastModified().getTime() > 0);
        }

        assertEquals("check response status", 200, response[0].getStatus());

        return objectId;
    }

    public static void deleteAppointment(final WebConversation webCon, final int objectId, final int inFolder, final Date lastModified, final Date recurrenceDatePosition, String host, final String login, final String password, String context) throws OXException, Exception {
        host = AbstractWebdavXMLTest.appendPrefix(host);

        host = AbstractWebdavXMLTest.appendPrefix(host);

        final Element rootElement = new Element("multistatus", webdav);
        rootElement.addNamespaceDeclaration(XmlServlet.NS);

        final ByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream();

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setObjectID(objectId);
        appointmentObj.setParentFolderID(inFolder);
        appointmentObj.setLastModified(lastModified);
        appointmentObj.setRecurrenceDatePosition(recurrenceDatePosition);

        final Element eProp = new Element("prop", webdav);

        final AppointmentWriter appointmentWriter = new AppointmentWriter();
        appointmentWriter.addContent2PropElement(eProp, appointmentObj, false);

        final Element eMethod = new Element("method", XmlServlet.NS);
        eMethod.addContent("DELETE");
        eProp.addContent(eMethod);

        rootElement.addContent(addProp2PropertyUpdate(eProp));

        final Document doc = new Document(rootElement);
        final XMLOutputter xo = new XMLOutputter();
        xo.output(doc, baos);

        ByteArrayInputStream bais = new UnsynchronizedByteArrayInputStream(baos.toByteArray());
        final WebRequest req = new PutMethodWebRequest(host + AbstractAppointmentRequest.URL, bais, "text/xml");
        req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password, context));
        final WebResponse resp = webCon.getResponse(req);

        assertEquals(207, resp.getResponseCode());

        bais = new ByteArrayInputStream(resp.getText().getBytes());
        final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.APPOINTMENT);

        if (response[0].hasError()) {
            throw new TestException(response[0].getErrorMessage());
        }
    }

    public static int[] deleteAppointment(final WebConversation webCon, final int[][] objectIdAndFolderId, final String host, final String login, final String password, String context) throws Exception {
        new ArrayList();

        for (int a = 0; a < objectIdAndFolderId.length; a++) {
            deleteAppointment(webCon, objectIdAndFolderId[a][0], objectIdAndFolderId[a][1], host, login, password, context);
        }

        return new int[] {};
    }

    public static void deleteAppointment(final WebConversation webCon, final int objectId, final int inFolder, final String host, final String login, final String password, String context) throws OXException, Exception {
        deleteAppointment(webCon, objectId, inFolder, new Date(System.currentTimeMillis() + APPEND_MODIFIED), host, login, password, context);
    }

    public static void deleteAppointment(final WebConversation webCon, final int objectId, final int inFolder, final Date lastModified, String host, final String login, final String password, String context) throws OXException, Exception {
        host = AbstractWebdavXMLTest.appendPrefix(host);

        final Element rootElement = new Element("multistatus", webdav);
        rootElement.addNamespaceDeclaration(XmlServlet.NS);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setObjectID(objectId);
        appointmentObj.setParentFolderID(inFolder);
        appointmentObj.setLastModified(lastModified);

        final Element eProp = new Element("prop", webdav);

        final AppointmentWriter appointmentWriter = new AppointmentWriter();
        appointmentWriter.addContent2PropElement(eProp, appointmentObj, false);

        final Element eMethod = new Element("method", XmlServlet.NS);
        eMethod.addContent("DELETE");
        eProp.addContent(eMethod);

        rootElement.addContent(addProp2PropertyUpdate(eProp));

        final Document doc = new Document(rootElement);
        final XMLOutputter xo = new XMLOutputter();
        xo.output(doc, baos);

        baos.toByteArray();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final WebRequest req = new PutMethodWebRequest(host + AbstractAppointmentRequest.URL, bais, "text/xml");
        req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password, context));
        final WebResponse resp = webCon.getResource(req);

        assertEquals(207, resp.getResponseCode());

        bais = new ByteArrayInputStream(resp.getText().getBytes());
        final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.APPOINTMENT);

        if (response[0].hasError()) {
            throw new TestException(response[0].getErrorMessage());
        }
    }

    public static void confirmAppointment(final WebConversation webCon, final int objectId, final int confirm, final String confirmMessage, String host, final String login, final String password, String context) throws Exception {
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
        final WebRequest req = new PutMethodWebRequest(host + AbstractAppointmentRequest.URL, bais, "text/javascript");
        req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password, context));
        final WebResponse resp = webCon.getResponse(req);

        assertEquals(207, resp.getResponseCode());

        bais = new ByteArrayInputStream(resp.getText().getBytes());
        final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.APPOINTMENT);

        assertEquals("check response", 1, response.length);

        if (response[0].hasError()) {
            fail("xml error: " + response[0].getErrorMessage());
        }

        assertEquals("check response status", 200, response[0].getStatus());
    }

    public static int[] listAppointment(final WebConversation webCon, final int inFolder, String host, final String login, final String password, String context) throws Exception {
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
        final PropFindMethod propFindMethod = new PropFindMethod(host + AbstractAppointmentRequest.URL);
        propFindMethod.setDoAuthentication(true);

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        propFindMethod.setRequestBody(bais);

        final int status = httpclient.executeMethod(propFindMethod);

        assertEquals("check propfind response", 207, status);

        final InputStream body = propFindMethod.getResponseBodyAsStream();
        final Response[] response = ResponseParser.parse(new SAXBuilder().build(body), Types.APPOINTMENT, true);

        assertEquals("response length not is 1", 1, response.length);

        return (int[]) response[0].getDataObject();
    }

    public static Appointment[] listAppointment(final WebConversation webCon, final int inFolder, final Date modified, final boolean changed, final boolean deleted, String host, final String login, final String password, String context) throws Exception {
        host = AbstractWebdavXMLTest.appendPrefix(host);

        if (!changed && !deleted) {
            return new Appointment[] {};
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

        objectMode.delete(objectMode.length() - 1, objectMode.length());

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
        final PropFindMethod propFindMethod = new PropFindMethod(host + AbstractAppointmentRequest.URL);
        propFindMethod.setDoAuthentication(true);

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        propFindMethod.setRequestBody(bais);

        final int status = httpclient.executeMethod(propFindMethod);

        assertEquals("check propfind response", 207, status);

        final InputStream body = propFindMethod.getResponseBodyAsStream();
        final Response[] response = ResponseParser.parse(new SAXBuilder().build(body), Types.APPOINTMENT);

        final Appointment[] appointmentArray = new Appointment[response.length];
        for (int a = 0; a < appointmentArray.length; a++) {
            if (response[a].hasError()) {
                fail("xml error: " + response[a].getErrorMessage());
            }

            appointmentArray[a] = (Appointment) response[a].getDataObject();
            assertNotNull("last modified is null", appointmentArray[a].getLastModified());
        }

        return appointmentArray;
    }
    public static Appointment loadAppointment(final WebConversation webCon, final int objectId, final int inFolder, final Date modified, final String host, final String login, final String password, String context) throws OXException, Exception {
        final Appointment[] appointmentArray = listAppointment(webCon, inFolder, modified, true, false, host, login, password, context);

        for (int a = 0; a < appointmentArray.length; a++) {
            if (appointmentArray[a].getObjectID() == objectId) {
                return appointmentArray[a];
            }
        }

        throw new TestException("object not found");
    }

    public static Appointment loadAppointment(final WebConversation webCon, final int objectId, final int inFolder, String host, final String login, final String password, String context) throws OXException, Exception {
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
        final PropFindMethod propFindMethod = new PropFindMethod(host + AbstractAppointmentRequest.URL);
        propFindMethod.setDoAuthentication(true);

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        propFindMethod.setRequestBody(bais);

        final int status = httpclient.executeMethod(propFindMethod);

        assertEquals("check propfind response", 207, status);

        final InputStream body = propFindMethod.getResponseBodyAsStream();
        final Response[] response = ResponseParser.parse(new SAXBuilder().build(body), Types.APPOINTMENT);

        assertEquals("check response", 1, response.length);

        if (response[0].hasError()) {
            throw new TestException(response[0].getErrorMessage());
        }

        assertEquals("check response status", 200, response[0].getStatus());

        return (Appointment) response[0].getDataObject();
    }


    private static Credentials getCredentials(String login, String password,
			String context) {
		return new UsernamePasswordCredentials((context == null || context.equals("")) ? login : login+"@"+context, password);
	}

	protected int getFreeBusyState(final WebConversation webCon, String contextid, String username, String context, Date start, Date end) throws IOException, SAXException {

        String url = "http://"+getHostName()+"/servlet/webdav.freebusy?contextid="+contextid+"&username="+username+"&server="+context+"&start="+start.getTime()+"&end="+end.getTime();
        WebRequest request = new GetMethodWebRequest(url);
        WebResponse response = webCon.getResponse(request);
        String text = response.getText();
        // System.out.println(text);
        return -1;
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
        if (p instanceof ExternalUserParticipant) {
            final ExternalUserParticipant externalUserParticipant = (ExternalUserParticipant) p;
            sb.append("MAIL" + externalUserParticipant.getEmailAddress());
        }

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
