package com.openexchange.webdav.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.TestException;
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
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		final Calendar c = Calendar.getInstance();
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
		
		final FolderObject folderObj = FolderTest.getAppointmentDefaultFolder(webCon, PROTOCOL + hostName, login, password);
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
		
		assertEqualsAndNotNull("participants are not equals" , participants2String(appointmentObj1.getParticipants()), participants2String(appointmentObj2.getParticipants()));
		assertEqualsAndNotNull("users are not equals" , users2String(appointmentObj1.getUsers()), users2String(appointmentObj2.getUsers()));
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
	
	public static int insertAppointment(final WebConversation webCon, Appointment appointmentObj, String host, final String login, final String password) throws OXException, Exception {
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
		
		final byte b[] = baos.toByteArray();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		final WebRequest req = new PutMethodWebRequest(host + AbstractAppointmentRequest.URL, bais, "text/xml");
		req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password));
		final WebResponse resp = webCon.getResponse(req);
		
		assertEquals(207, resp.getResponseCode());
		
		bais = new ByteArrayInputStream(resp.getText().getBytes());
		final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.APPOINTMENT);
		
		assertEquals("check response", 1, response.length);
		
		if (response[0].hasError()) {
			throw new TestException(response[0].getErrorMessage());
		}
		
		assertEquals("check response status", 200, response[0].getStatus());
		
		appointmentObj = (Appointment)response[0].getDataObject();
		objectId = appointmentObj.getObjectID();
		
		assertNotNull("last modified is null", appointmentObj.getLastModified());
		assertTrue("last modified is not > 0", appointmentObj.getLastModified().getTime() > 0);
		
		assertTrue("check objectId", objectId > 0);
		
		return objectId;
	}
	
	public static int updateAppointment(final WebConversation webCon, final Appointment appointmentObj, final int objectId, final int inFolder, final String host, final String login, final String password) throws OXException, Exception {
		return updateAppointment(webCon, appointmentObj, objectId, inFolder, new Date(System.currentTimeMillis() + APPEND_MODIFIED), host, login, password);
	}

	public static int updateAppointment(final WebConversation webCon, Appointment appointmentObj, int objectId, final int inFolder, final Date lastModified, String host, final String login, final String password) throws OXException, Exception {
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
		
		final byte b[] = baos.toByteArray();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		final WebRequest req = new PutMethodWebRequest(host + AbstractAppointmentRequest.URL, bais, "text/javascript");
		req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password));
		final WebResponse resp = webCon.getResponse(req);
		
		assertEquals(207, resp.getResponseCode());
		
		bais = new ByteArrayInputStream(resp.getText().getBytes());
		final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.APPOINTMENT);
		
		assertEquals("check response", 1, response.length);
		
		if (response[0].hasError()) {
			throw new TestException(response[0].getErrorMessage());
		} else {
			appointmentObj = (Appointment)response[0].getDataObject();
			objectId = appointmentObj.getObjectID();
			
			assertNotNull("last modified is null", appointmentObj.getLastModified());
			assertTrue("last modified is not > 0", appointmentObj.getLastModified().getTime() > 0);
		}
		
		assertEquals("check response status", 200, response[0].getStatus());
		
		return objectId;
	}
	
	public static void deleteAppointment(final WebConversation webCon, final int objectId, final int inFolder, final Date lastModified, final Date recurrenceDatePosition, String host, final String login, final String password) throws OXException, Exception {
		host = AbstractWebdavXMLTest.appendPrefix(host);
		
		host = AbstractWebdavXMLTest.appendPrefix(host);
		
		final Element rootElement = new Element("multistatus", webdav);
		rootElement.addNamespaceDeclaration(XmlServlet.NS);
		
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
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
		
		final byte b[] = baos.toByteArray();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		final WebRequest req = new PutMethodWebRequest(host + AbstractAppointmentRequest.URL, bais, "text/xml");
		req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password));
		final WebResponse resp = webCon.getResponse(req);
		
		assertEquals(207, resp.getResponseCode());
		
		bais = new ByteArrayInputStream(resp.getText().getBytes());
		final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.APPOINTMENT);
		
		if (response[0].hasError()) {
			throw new TestException(response[0].getErrorMessage());
		}
	}
	
	public static int[] deleteAppointment(final WebConversation webCon, final int[][] objectIdAndFolderId, final String host, final String login, final String password) throws Exception {
		final ArrayList failed = new ArrayList();
		
		for (int a = 0; a < objectIdAndFolderId.length; a++) {
			deleteAppointment(webCon, objectIdAndFolderId[a][0], objectIdAndFolderId[a][1], host, login, password);
		}
		
		return new int[] { };
	}

	public static void deleteAppointment(final WebConversation webCon, final int objectId, final int inFolder, final String host, final String login, final String password) throws OXException, Exception {
		deleteAppointment(webCon, objectId, inFolder, new Date(System.currentTimeMillis() + APPEND_MODIFIED), host, login, password);
	}
	
	public static void deleteAppointment(final WebConversation webCon, final int objectId, final int inFolder, final Date lastModified, String host, final String login, final String password) throws OXException, Exception {
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
		
		final byte b[] = baos.toByteArray();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		final WebRequest req = new PutMethodWebRequest(host + AbstractAppointmentRequest.URL, bais, "text/xml");
		req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password));
		final WebResponse resp = webCon.getResource(req);
		
		assertEquals(207, resp.getResponseCode());
		
		bais = new ByteArrayInputStream(resp.getText().getBytes());
		final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.APPOINTMENT);
		
		if (response[0].hasError()) {
			throw new TestException(response[0].getErrorMessage());
		}
	}
	
	public static void confirmAppointment(final WebConversation webCon, final int objectId, final int confirm, final String confirmMessage, String host, final String login, final String password) throws Exception {
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
		
		final byte b[] = baos.toByteArray();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		final WebRequest req = new PutMethodWebRequest(host + AbstractAppointmentRequest.URL, bais, "text/javascript");
		req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password));
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
	
	public static int[] listAppointment(final WebConversation webCon, final int inFolder, String host, final String login, final String password) throws Exception {
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
		
		httpclient.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(login, password));
		final PropFindMethod propFindMethod = new PropFindMethod(host + AbstractAppointmentRequest.URL);
		propFindMethod.setDoAuthentication( true );
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		propFindMethod.setRequestBody(bais);
		
		final int status = httpclient.executeMethod(propFindMethod);
		
		assertEquals("check propfind response", 207, status);
		
        InputStream body = propFindMethod.getResponseBodyAsStream();
		final Response[] response = ResponseParser.parse(new SAXBuilder().build(body), Types.APPOINTMENT, true);
		
		assertEquals("response length not is 1", 1, response.length);
		
		return (int[])response[0].getDataObject();
	}
	
	public static Appointment[] listAppointment(final WebConversation webCon, final int inFolder, final Date modified, final boolean changed, final boolean deleted, String host, final String login, final String password) throws Exception {
		host = AbstractWebdavXMLTest.appendPrefix(host);
		
		if (!changed && !deleted) {
			return new Appointment[] { };
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
		
		httpclient.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(login, password));
		final PropFindMethod propFindMethod = new PropFindMethod(host + AbstractAppointmentRequest.URL);
		propFindMethod.setDoAuthentication( true );
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		propFindMethod.setRequestBody(bais);
		
		final int status = httpclient.executeMethod(propFindMethod);
		
		assertEquals("check propfind response", 207, status);
		
        InputStream body = propFindMethod.getResponseBodyAsStream();
		final Response[] response = ResponseParser.parse(new SAXBuilder().build(body), Types.APPOINTMENT);
		
		final Appointment[] appointmentArray = new Appointment[response.length];
		for (int a = 0; a < appointmentArray.length; a++) {
			if (response[a].hasError()) {
				fail("xml error: " + response[a].getErrorMessage());
			}
			
			appointmentArray[a] = (Appointment)response[a].getDataObject();
			assertNotNull("last modified is null", appointmentArray[a].getLastModified());
		}
		
		return appointmentArray;
	}
	
	public static Appointment loadAppointment(final WebConversation webCon, final int objectId, final int inFolder, final Date modified, final String host, final String login, final String password) throws TestException, Exception {
		final Appointment[] appointmentArray = listAppointment(webCon, inFolder, modified, true, false, host, login, password);
		
		for (int a = 0; a < appointmentArray.length; a++) {
			if (appointmentArray[a].getObjectID() == objectId) {
				return appointmentArray[a];
			}
		}
		
		throw new TestException("object not found");
	}
	
	public static Appointment loadAppointment(final WebConversation webCon, final int objectId, final int inFolder, String host, final String login, final String password) throws OXException, Exception {
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
		
		httpclient.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(login, password));
		final PropFindMethod propFindMethod = new PropFindMethod(host + AbstractAppointmentRequest.URL);
		propFindMethod.setDoAuthentication( true );
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		propFindMethod.setRequestBody(bais);
		
		final int status = httpclient.executeMethod(propFindMethod);
		
		assertEquals("check propfind response", 207, status);
		
        InputStream body = propFindMethod.getResponseBodyAsStream();
		final Response[] response = ResponseParser.parse(new SAXBuilder().build(body), Types.APPOINTMENT);
		
		assertEquals("check response" , 1, response.length);
		
		if (response[0].hasError()) {
			throw new TestException(response[0].getErrorMessage());
		}
		
		assertEquals("check response status", 200, response[0].getStatus());
		
		return (Appointment)response[0].getDataObject();
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
			final ExternalUserParticipant externalUserParticipant = (ExternalUserParticipant)p;
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

