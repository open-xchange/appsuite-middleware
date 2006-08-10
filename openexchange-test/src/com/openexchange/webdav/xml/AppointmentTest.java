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
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.webdav.xml.parser.ResponseParser;
import com.openexchange.webdav.xml.request.PropFindMethod;
import com.openexchange.webdav.xml.types.Response;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

public class AppointmentTest extends AbstractWebdavTest {
	
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
	
	private static final String APPOINTMENT_URL = "/servlet/webdav.calendar";
	
	protected void setUp() throws Exception {
		super.setUp();
		try {
				
			Calendar c = Calendar.getInstance();
			c.set(Calendar.HOUR_OF_DAY, 12);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			
			startTime = new Date(c.getTimeInMillis());
			endTime = new Date(startTime.getTime() + 3600000);
			
			userParticipant2 = AbstractConfigWrapper.parseProperty(webdavProps, "user_participant2", "");
			userParticipant3 = AbstractConfigWrapper.parseProperty(webdavProps, "user_participant3", "");
			
			groupParticipant = AbstractConfigWrapper.parseProperty(webdavProps, "group_participant", "");
			
			resourceParticipant = AbstractConfigWrapper.parseProperty(webdavProps, "resource_participant", "");

			final FolderObject folderObj = FolderTest.getAppointmentDefaultFolder(webCon, PROTOCOL + hostName, login, password);
			appointmentFolderId = folderObj.getObjectID();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new Exception("AppointmentTest.setUp", ex);
		}
	}
	
	public void testNewAppointment() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testNewAppointment");
		insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
	}
	
	public void testNewAppointmentWithParticipants() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testNewAppointmentWithParticipants");

		int userParticipantId = GroupUserTest.searchUser(webCon, userParticipant2, new Date(0), PROTOCOL + hostName, login, password)[0].getInternalUserId();
		int groupParticipantId = GroupUserTest.searchGroup(webCon, "*", new Date(0), PROTOCOL + hostName, login, password)[0].getIdentifier();
		
		com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[3];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userId);
		participants[1] = new UserParticipant();
		participants[1].setIdentifier(userParticipantId);
		participants[2] = new GroupParticipant();
		participants[2].setIdentifier(groupParticipantId);
		
		appointmentObj.setParticipants(participants);
		
		insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
	}
	
	public void testNewAppointmentWithUsers() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testNewAppointmentWithUsers");

		UserParticipant[] users = new UserParticipant[1];
		users[0] = new UserParticipant();
		users[0].setIdentifier(userId);
		users[0].setConfirm(CalendarObject.ACCEPT);
		
		appointmentObj.setUsers(users);
		
		insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
	}
	
	public void testUpdateAppointment() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testUpdateAppointment");
		int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		
		appointmentObj = createAppointmentObject("testUpdateAppointment2");
		appointmentObj.setLocation(null);
		appointmentObj.setShownAs(AppointmentObject.FREE);
		
		updateAppointment(webCon, appointmentObj, objectId, appointmentFolderId, PROTOCOL + hostName, login, password);
	}
	
	public void testUpdateAppointmentWithParticipants() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testUpdateAppointment");
		int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		
		appointmentObj = createAppointmentObject("testUpdateAppointment");
		
		int userParticipantId = GroupUserTest.searchUser(webCon, userParticipant3, new Date(0), PROTOCOL + hostName, login, password)[0].getInternalUserId();
		int groupParticipantId = GroupUserTest.searchGroup(webCon, "*", new Date(0), PROTOCOL + hostName, login, password)[0].getIdentifier();
		int resourceParticipantId = GroupUserTest.searchResource(webCon, "*", new Date(0), PROTOCOL + hostName, login, password)[0].getIdentifier();

		com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[4];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userId);
		participants[1] = new UserParticipant();
		participants[1].setIdentifier(userParticipantId);
		participants[2] = new GroupParticipant();
		participants[2].setIdentifier(groupParticipantId);
		participants[3] = new ResourceParticipant();
		participants[3].setIdentifier(resourceParticipantId);
		
		appointmentObj.setParticipants(participants);
		
		updateAppointment(webCon, appointmentObj, objectId, appointmentFolderId, PROTOCOL + hostName, login, password);
	}
	
	public void testDelete() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testDelete");
		int objectId1 = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		int objectId2 = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		
		int[][] objectIdAndFolderId = { { objectId1, appointmentFolderId }, { objectId2, appointmentFolderId } };
		
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password);
	}

	public void testPropFindWithModified() throws Exception {
		Date modified = new Date();
		
		AppointmentObject appointmentObj = createAppointmentObject("testPropFindWithModified");
		insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		
		AppointmentObject[] appointmentArray = listAppointment(webCon, appointmentFolderId, modified, "NEW_AND_MODIFIED", PROTOCOL + hostName, login, password);
		
		assertTrue("check response", appointmentArray.length >= 2);
	}

	public void testPropFindWithDelete() throws Exception {
		Date modified = new Date();
		
		AppointmentObject appointmentObj = createAppointmentObject("testPropFindWithModified");
		int objectId1 = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		int objectId2 = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		
		int[][] objectIdAndFolderId = { { objectId1, appointmentFolderId }, { objectId2, appointmentFolderId } };
		
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password);
		
		AppointmentObject[] appointmentArray = listAppointment(webCon, appointmentFolderId, modified, "DELETED", PROTOCOL + hostName, login, password);
		
		assertTrue("check response", appointmentArray.length >= 2);
	}
	 
	public void testPropFindWithObjectId() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testPropFindWithObjectId");
		int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
	 
		AppointmentObject loadAppointment = loadAppointment(webCon, objectId, appointmentFolderId, PROTOCOL + hostName, login, password);
	}
	
	public void testConfirm() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testConfirm");
		int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		confirmAppointment(webCon, objectId, CalendarObject.DECLINE, null, PROTOCOL + hostName, login, password);
	}
		
	private AppointmentObject createAppointmentObject(String title) throws Exception {
		AppointmentObject appointmentobject = new AppointmentObject();
		appointmentobject.setTitle(title);
		appointmentobject.setStartDate(startTime);
		appointmentobject.setEndDate(endTime);
		appointmentobject.setLocation("Location");
		appointmentobject.setShownAs(AppointmentObject.ABSEND);
		appointmentobject.setParentFolderID(appointmentFolderId);
		
		return appointmentobject;
	}
	
	public static int insertAppointment(WebConversation webCon, AppointmentObject appointmentObj, String host, String login, String password) throws Exception {
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
		
		appointmentObj = (AppointmentObject)response[0].getDataObject();
		objectId = appointmentObj.getObjectID();
		
		assertTrue("check objectId", objectId > 0);
		
		return objectId;
	}
	
	public static void updateAppointment(WebConversation webCon, AppointmentObject appointmentObj, int objectId, int inFolder, String host, String login, String password) throws Exception {
		appointmentObj.setObjectID(objectId);
		
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
		}
		
		assertEquals("check response status", 200, response[0].getStatus());
	}
	
	public static int[] deleteAppointment(WebConversation webCon, int[][] objectIdAndFolderId, String host, String login, String password) throws Exception {
		Element rootElement = new Element("multistatus", webdav);
		rootElement.addNamespaceDeclaration(XmlServlet.NS);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		for (int a = 0; a < objectIdAndFolderId.length; a++) {
			int[] i = objectIdAndFolderId[a];
			
			AppointmentObject appointmentObj = new AppointmentObject();
			appointmentObj.setObjectID(i[0]);
			appointmentObj.setParentFolderID(i[1]);
			
			Element eProp = new Element("prop", webdav);
			
			AppointmentWriter appointmentWriter = new AppointmentWriter();
			appointmentWriter.addContent2PropElement(eProp, appointmentObj, false);
			
			rootElement.addContent(addProp2PropertyUpdate(eProp));
		}
		
		Document doc = new Document(rootElement);
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
		
		assertEquals("check response", objectIdAndFolderId.length, response.length);
		
		ArrayList idList = new ArrayList();
		
		for (int a = 0; a < response.length; a++) {
			if (response[a].hasError()) {
				AppointmentObject appointmentObj = (AppointmentObject)response[a].getDataObject();
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
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		Element eProp = new Element("prop", webdav);
		
		Element eObjectId = new Element(OXObject.OBJECT_ID, XmlServlet.NS);
		eObjectId.addContent(String.valueOf(objectId));
		eProp.addContent(eObjectId);
		
		Element eMethod = new Element("method", XmlServlet.NS);
		eMethod.addContent("CONFIRM");
		eProp.addContent(eMethod);
		
		Element eConfirm = new Element("confirm", XmlServlet.NS);
		eConfirm.addContent("decline");
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
	
	public static AppointmentObject[] listAppointment(WebConversation webCon, int inFolder, Date modified, String objectMode, String host, String login, String password) throws Exception {
		Element ePropfind = new Element("propfind", webdav);
		Element eProp = new Element("prop", webdav);
		
		Element eFolderId = new Element("folder_id", XmlServlet.NS);
		Element eLastSync = new Element("lastsync", XmlServlet.NS);
		Element eObjectmode = new Element("objectmode", XmlServlet.NS);
		
		eFolderId.addContent(String.valueOf(inFolder));
		eLastSync.addContent(String.valueOf(modified.getTime()));
		
		if (objectMode != null) {
			eObjectmode.addContent(objectMode);
			eProp.addContent(eObjectmode);
		} 
		
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
}

