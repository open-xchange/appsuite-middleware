package com.openexchange.webdav.xml;

import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.api.OXObject;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.ldap.Group;
import com.openexchange.groupware.ldap.Resource;
import com.openexchange.webdav.xml.parser.ResponseParser;
import com.openexchange.webdav.xml.request.PropFindMethod;
import com.openexchange.webdav.xml.types.Response;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

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
	
	protected static final int dayInMillis = 86400000;
	
	private static final String APPOINTMENT_URL = "/servlet/webdav.calendar";
	
	protected void setUp() throws Exception {
		super.setUp();
		
		Calendar c = Calendar.getInstance();
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
	
	protected void compareObject(AppointmentObject appointmentObj1, AppointmentObject appointmentObj2) throws Exception {
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
		assertEqualsAndNotNull("note is not equals", appointmentObj1.getNote(), appointmentObj2.getNote());
		assertEqualsAndNotNull("categories is not equals", appointmentObj1.getCategories(), appointmentObj2.getCategories());
		
		assertEqualsAndNotNull("participants are not equals" , participants2String(appointmentObj1.getParticipants()), participants2String(appointmentObj2.getParticipants()));
		assertEqualsAndNotNull("users are not equals" , users2String(appointmentObj1.getUsers()), users2String(appointmentObj2.getUsers()));
	}
	
	protected AppointmentObject createAppointmentObject(String title) throws Exception {
		AppointmentObject appointmentobject = new AppointmentObject();
		appointmentobject.setTitle(title);
		appointmentobject.setStartDate(startTime);
		appointmentobject.setEndDate(endTime);
		appointmentobject.setLocation("Location");
		appointmentobject.setShownAs(AppointmentObject.ABSENT);
		appointmentobject.setParentFolderID(appointmentFolderId);
		
		return appointmentobject;
	}
	
	public static int insertAppointment(WebConversation webCon, AppointmentObject appointmentObj, String host, String login, String password) throws Exception {
		host = appendPrefix(host);
		
		int objectId = 0;
		
		appointmentObj.removeObjectID();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		Element eProp = new Element("prop", webdav);
		
		AppointmentWriter appointmentWriter = new AppointmentWriter();
		appointmentWriter.addContent2PropElement(eProp, appointmentObj, false);
		
		Document doc = addProp2Document(eProp);
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		byte b[] = baos.toByteArray();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		WebRequest req = new PutMethodWebRequest(host + APPOINTMENT_URL, bais, "text/xml");
		req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password));
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(207, resp.getResponseCode());
		
		bais = new ByteArrayInputStream(resp.getText().getBytes());
		final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.APPOINTMENT);
		
		assertEquals("check response", 1, response.length);
		
		if (response[0].hasError()) {
			fail("xml error: " + response[0].getErrorMessage());
		}
		
		assertEquals("check response status", 200, response[0].getStatus());
		
		appointmentObj = (AppointmentObject)response[0].getDataObject();
		objectId = appointmentObj.getObjectID();
		
		assertNotNull("last modified is null", appointmentObj.getLastModified());
		assertTrue("last modified is not > 0", appointmentObj.getLastModified().getTime() > 0);
		
		assertTrue("check objectId", objectId > 0);
		
		return objectId;
	}
	
	public static int updateAppointment(WebConversation webCon, AppointmentObject appointmentObj, int objectId, int inFolder, String host, String login, String password) throws Exception {
		host = appendPrefix(host);
		
		appointmentObj.setObjectID(objectId);
		appointmentObj.setLastModified(new Date());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		Element eProp = new Element("prop", webdav);
		
		AppointmentWriter appointmentWriter = new AppointmentWriter();
		appointmentWriter.addContent2PropElement(eProp, appointmentObj, false);
		
		Document doc = addProp2Document(eProp);
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		byte b[] = baos.toByteArray();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		WebRequest req = new PutMethodWebRequest(host + APPOINTMENT_URL, bais, "text/javascript");
		req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password));
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(207, resp.getResponseCode());
		
		bais = new ByteArrayInputStream(resp.getText().getBytes());
		final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.APPOINTMENT);
		
		assertEquals("check response", 1, response.length);
		
		if (response[0].hasError()) {
			fail("xml error: " + response[0].getErrorMessage());
		} else {
			appointmentObj = (AppointmentObject)response[0].getDataObject();
			objectId = appointmentObj.getObjectID();
			
			assertNotNull("last modified is null", appointmentObj.getLastModified());
			assertTrue("last modified is not > 0", appointmentObj.getLastModified().getTime() > 0);
		}
		
		assertEquals("check response status", 200, response[0].getStatus());
		
		return objectId;
	}
	
	public static int[] deleteAppointment(WebConversation webCon, int[][] objectIdAndFolderId, Date[] deleteException, String host, String login, String password) throws Exception {
		host = appendPrefix(host);
		
		throw new Exception("not supported yet!");
	}
	
	public static int[] deleteAppointment(WebConversation webCon, int[][] objectIdAndFolderId, String host, String login, String password) throws Exception {
		host = appendPrefix(host);
		
		Element rootElement = new Element("multistatus", webdav);
		rootElement.addNamespaceDeclaration(XmlServlet.NS);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		for (int a = 0; a < objectIdAndFolderId.length; a++) {
			int[] i = objectIdAndFolderId[a];
			
			AppointmentObject appointmentObj = new AppointmentObject();
			appointmentObj.setObjectID(i[0]);
			appointmentObj.setParentFolderID(i[1]);
			appointmentObj.setLastModified(new Date());
			
			Element eProp = new Element("prop", webdav);
			
			AppointmentWriter appointmentWriter = new AppointmentWriter();
			appointmentWriter.addContent2PropElement(eProp, appointmentObj, false);
			
			Element eMethod = new Element("method", XmlServlet.NS);
			eMethod.addContent("DELETE");
			eProp.addContent(eMethod);
			
			rootElement.addContent(addProp2PropertyUpdate(eProp));
		}
		
		Document doc = new Document(rootElement);
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		byte b[] = baos.toByteArray();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		WebRequest req = new PutMethodWebRequest(host + APPOINTMENT_URL, bais, "text/xml");
		req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password));
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(207, resp.getResponseCode());
		
		bais = new ByteArrayInputStream(resp.getText().getBytes());
		final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.APPOINTMENT);
		
		assertEquals("check response", objectIdAndFolderId.length, response.length);
		
		ArrayList idList = new ArrayList();
		
		for (int a = 0; a < response.length; a++) {
			AppointmentObject appointmentObj = (AppointmentObject)response[a].getDataObject();
			
			if (response[a].hasError()) {
				idList.add(new Integer(appointmentObj.getObjectID()));
			}
			
			assertEquals("check response status", 200, response[a].getStatus());
		}
		
		int[] failed = new int[idList.size()];
		
		for (int a = 0; a < failed.length; a++) {
			failed[a] = ((Integer)idList.get(a)).intValue();
		}
		
		return failed;
	}
	
	public static void confirmAppointment(WebConversation webCon, int objectId, int confirm, String confirmMessage, String host, String login, String password) throws Exception {
		host = appendPrefix(host);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		Element eProp = new Element("prop", webdav);
		
		Element eObjectId = new Element(OXObject.OBJECT_ID, XmlServlet.NS);
		eObjectId.addContent(String.valueOf(objectId));
		eProp.addContent(eObjectId);
		
		Element eMethod = new Element("method", XmlServlet.NS);
		eMethod.addContent("CONFIRM");
		eProp.addContent(eMethod);
		
		Element eConfirm = new Element("confirm", XmlServlet.NS);
		switch (confirm) {
			case AppointmentObject.ACCEPT:
				eConfirm.addContent("accept");
				break;
			case AppointmentObject.DECLINE:
				eConfirm.addContent("decline");
				break;
			case AppointmentObject.NONE:
				eConfirm.addContent("none");
				break;
			default:
				throw new Exception("invalid confirm value: " + confirm);
		} 
			
			
		eProp.addContent(eConfirm);
		
		Document doc = addProp2Document(eProp);
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		byte b[] = baos.toByteArray();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		WebRequest req = new PutMethodWebRequest(host + APPOINTMENT_URL, bais, "text/javascript");
		req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password));
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(207, resp.getResponseCode());
		
		bais = new ByteArrayInputStream(resp.getText().getBytes());
		final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.APPOINTMENT);
		
		assertEquals("check response", 1, response.length);
		
		if (response[0].hasError()) {
			fail("xml error: " + response[0].getErrorMessage());
		}
		
		assertEquals("check response status", 200, response[0].getStatus());
	}
	
	public static AppointmentObject[] listAppointment(WebConversation webCon, int inFolder, Date modified, boolean changed, boolean deleted, String host, String login, String password) throws Exception {
		host = appendPrefix(host);
		
		if (!changed && !deleted) {
			return new AppointmentObject[] { };
		}
		
		Element ePropfind = new Element("propfind", webdav);
		Element eProp = new Element("prop", webdav);
		
		Element eFolderId = new Element("folder_id", XmlServlet.NS);
		Element eLastSync = new Element("lastsync", XmlServlet.NS);
		Element eObjectmode = new Element("objectmode", XmlServlet.NS);
		
		eFolderId.addContent(String.valueOf(inFolder));
		eLastSync.addContent(String.valueOf(modified.getTime()));
		
		StringBuffer objectMode = new StringBuffer();
		
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
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		Document doc = new Document(ePropfind);
		
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		baos.flush();
		
		HttpClient httpclient = new HttpClient();
		
		httpclient.getState().setCredentials(null, new UsernamePasswordCredentials(login, password));
		PropFindMethod propFindMethod = new PropFindMethod(host + APPOINTMENT_URL);
		propFindMethod.setDoAuthentication( true );
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		propFindMethod.setRequestBody(bais);
		
		int status = httpclient.executeMethod(propFindMethod);
		
		assertEquals("check propfind response", 207, status);
		
		byte responseByte[] = propFindMethod.getResponseBody();
		
		bais = new ByteArrayInputStream(responseByte);
		final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.APPOINTMENT);
		
		AppointmentObject[] appointmentArray = new AppointmentObject[response.length];
		for (int a = 0; a < appointmentArray.length; a++) {
			if (response[a].hasError()) {
				fail("xml error: " + response[a].getErrorMessage());
			}
			
			appointmentArray[a] = (AppointmentObject)response[a].getDataObject();
		}
		
		return appointmentArray;
	}
	
	public static AppointmentObject loadAppointment(WebConversation webCon, int objectId, int inFolder, String host, String login, String password) throws Exception {
		host = appendPrefix(host);
		
		Element ePropfind = new Element("propfind", webdav);
		Element eProp = new Element("prop", webdav);
		
		Element eFolderId = new Element("folder_id", XmlServlet.NS);
		Element eObjectId = new Element("object_id", XmlServlet.NS);
		
		eFolderId.addContent(String.valueOf(inFolder));
		eObjectId.addContent(String.valueOf(objectId));
		
		ePropfind.addContent(eProp);
		eProp.addContent(eFolderId);
		eProp.addContent(eObjectId);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		Document doc = new Document(ePropfind);
		
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		baos.flush();
		
		HttpClient httpclient = new HttpClient();
		
		httpclient.getState().setCredentials(null, new UsernamePasswordCredentials(login, password));
		PropFindMethod propFindMethod = new PropFindMethod(host + APPOINTMENT_URL);
		propFindMethod.setDoAuthentication( true );
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		propFindMethod.setRequestBody(bais);
		
		int status = httpclient.executeMethod(propFindMethod);
		
		assertEquals("check propfind response", 207, status);
		
		byte responseByte[] = propFindMethod.getResponseBody();
		
		bais = new ByteArrayInputStream(responseByte);
		final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.APPOINTMENT);
		
		assertEquals("check response" , 1, response.length);
		
		if (response[0].hasError()) {
			fail("xml error: " + response[0].getErrorMessage());
		}
		
		assertEquals("check response status", 200, response[0].getStatus());
		
		return (AppointmentObject)response[0].getDataObject();
	}
	
	private HashSet participants2String(Participant[] participant) throws Exception {
		if (participant == null) {
			return null;
		}
		
		HashSet hs = new HashSet();
		
		for (int a = 0; a < participant.length; a++) {
			hs.add(participant2String(participant[a]));
		}
		
		return hs;
	}
	
	private String participant2String(Participant p) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("T" + p.getType());
		sb.append("ID" + p.getIdentifier());
		
		return sb.toString();
	}
	
	private HashSet users2String(UserParticipant[] users) throws Exception {
		if (users == null) {
			return null;
		}
		
		HashSet hs = new HashSet();
		
		for (int a = 0; a < users.length; a++) {
			hs.add(user2String(users[a]));
		}
		
		return hs;
	}
	
	private String user2String(UserParticipant user) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("ID" + user.getIdentifier());
		sb.append("C" + user.getConfirm());
		
		return sb.toString();
	}
}

