package com.openexchange.webdav.xml;

import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

import com.openexchange.api2.OXException;
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.TimeZone;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

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
	
	public TaskTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		
		Calendar c = Calendar.getInstance();
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
		
		final FolderObject folderObj = FolderTest.getTaskDefaultFolder(webCon, PROTOCOL + hostName, login, password);
		taskFolderId = folderObj.getObjectID();
		userId = folderObj.getCreatedBy();
	}
	
	public static void compareObject(Task taskObj1, Task taskObj2) throws Exception {
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
	
	protected Task createTask(String title) throws Exception {
		Task taskObj = new Task();
		taskObj.setTitle(title);
		taskObj.setStartDate(startTime);
		taskObj.setEndDate(endTime);
		taskObj.setParentFolderID(taskFolderId);
		taskObj.setStatus(Task.IN_PROGRESS);
		taskObj.setPercentComplete(50);
		
		return taskObj;
	}
	
	public static int insertTask(WebConversation webCon, Task taskObj, String host, String login, String password) throws Exception {
		host = appendPrefix(host);
		
		int objectId = 0;
		
		taskObj.removeObjectID();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		Element eProp = new Element("prop", webdav);
		
		TaskWriter taskWriter = new TaskWriter();
		taskWriter.addContent2PropElement(eProp, taskObj, false);
		
		Document doc = addProp2Document(eProp);
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		byte b[] = baos.toByteArray();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		WebRequest req = new PutMethodWebRequest(host + TASK_URL, bais, "text/javascript");
		req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password));
		WebResponse resp = webCon.getResponse(req);
		
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
	
	public static void updateTask(WebConversation webCon, Task taskObj, int objectId, int inFolder, String host, String login, String password) throws Exception {
		updateTask(webCon, taskObj, objectId, inFolder, new Date(), host, login, password);
	}
	
	public static void updateTask(WebConversation webCon, Task taskObj, int objectId, int inFolder, Date lastModified, String host, String login, String password) throws Exception {
		host = appendPrefix(host);
		
		taskObj.setObjectID(objectId);
		taskObj.setLastModified(lastModified);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		Element eProp = new Element("prop", webdav);
		
		TaskWriter appointmentWriter = new TaskWriter();
		appointmentWriter.addContent2PropElement(eProp, taskObj, false);
		
		Document doc = addProp2Document(eProp);
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		byte b[] = baos.toByteArray();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		WebRequest req = new PutMethodWebRequest(host + TASK_URL, bais, "text/javascript");
		req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password));
		WebResponse resp = webCon.getResponse(req);
		
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
	
	public static int[] deleteTask(WebConversation webCon, int[][] objectIdAndFolderId, String host, String login, String password) throws Exception {
		ArrayList failed = new ArrayList();
		
		for (int a = 0; a < objectIdAndFolderId.length; a++) {
			deleteTask(webCon, objectIdAndFolderId[a][0], objectIdAndFolderId[a][1], host, login, password);
		}
		
		return new int[] { };
	}
	
	public static void deleteTask(WebConversation webCon, int objectId, int inFolder, String host, String login, String password) throws Exception {
		deleteTask(webCon, objectId, inFolder, new Date(), host, login, password);
	}
	
	public static void deleteTask(WebConversation webCon, int objectId, int inFolder, Date lastModified, String host, String login, String password) throws Exception {
		host = appendPrefix(host);
		
		Element rootElement = new Element("multistatus", webdav);
		rootElement.addNamespaceDeclaration(XmlServlet.NS);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		
		Task taskObj = new Task();
		taskObj.setObjectID(objectId);
		taskObj.setParentFolderID(inFolder);
		taskObj.setLastModified(lastModified);
		
		Element eProp = new Element("prop", webdav);
		
		TaskWriter taskWriter = new TaskWriter();
		taskWriter.addContent2PropElement(eProp, taskObj, false);
		
		Element eMethod = new Element("method", XmlServlet.NS);
		eMethod.addContent("DELETE");
		eProp.addContent(eMethod);
		
		rootElement.addContent(addProp2PropertyUpdate(eProp));
		
		Document doc = new Document(rootElement);
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		byte b[] = baos.toByteArray();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		WebRequest req = new PutMethodWebRequest(host + TASK_URL, bais, "text/javascript");
		req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password));
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(207, resp.getResponseCode());
		
		bais = new ByteArrayInputStream(resp.getText().getBytes());
		final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.TASK);

		if (response[0].hasError()) {
			throw new TestException(response[0].getErrorMessage());
		}
		
		assertEquals("check response status", 200, response[0].getStatus());
	}
	
	public static void confirmTask(WebConversation webCon, int objectId, int confirm, String confirmMessage, String host, String login, String password) throws Exception {
		host = appendPrefix(host);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		Element eProp = new Element("prop", webdav);
		
		Element eObjectId = new Element(DataFields.OBJECT_ID, XmlServlet.NS);
		eObjectId.addContent(String.valueOf(objectId));
		eProp.addContent(eObjectId);
		
		Element eMethod = new Element("method", XmlServlet.NS);
		eMethod.addContent("CONFIRM");
		eProp.addContent(eMethod);
		
		Element eConfirm = new Element("confirm", XmlServlet.NS);
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
		
		Document doc = addProp2Document(eProp);
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		byte b[] = baos.toByteArray();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		WebRequest req = new PutMethodWebRequest(host + TASK_URL, bais, "text/javascript");
		req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password));
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(207, resp.getResponseCode());
		
		bais = new ByteArrayInputStream(resp.getText().getBytes());
		final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.TASK);
		
		assertEquals("check response", 1, response.length);
		
		if (response[0].hasError()) {
			fail("xml error: " + response[0].getErrorMessage());
		}
		
		assertEquals("check response status", 200, response[0].getStatus());
	}
	
	public static int[] listTask(WebConversation webCon, int inFolder, String host, String login, String password) throws Exception {
		host = appendPrefix(host);
		
		Element ePropfind = new Element("propfind", webdav);
		Element eProp = new Element("prop", webdav);
		
		Element eFolderId = new Element("folder_id", XmlServlet.NS);
		Element eObjectmode = new Element("objectmode", XmlServlet.NS);
		
		eFolderId.addContent(String.valueOf(inFolder));
		eObjectmode.addContent("LIST");
		
		eProp.addContent(eFolderId);
		eProp.addContent(eObjectmode);
		
		ePropfind.addContent(eProp);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		Document doc = new Document(ePropfind);
		
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		baos.flush();
		
		HttpClient httpclient = new HttpClient();
		
		httpclient.getState().setCredentials(null, new UsernamePasswordCredentials(login, password));
		PropFindMethod propFindMethod = new PropFindMethod(host + TASK_URL);
		propFindMethod.setDoAuthentication( true );
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		propFindMethod.setRequestBody(bais);
		
		int status = httpclient.executeMethod(propFindMethod);
		
		assertEquals("check propfind response", 207, status);
		
		byte responseByte[] = propFindMethod.getResponseBody();
		
		bais = new ByteArrayInputStream(responseByte);
		final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.TASK, true);
		
		assertEquals("response length not is 1", 1, response.length);
		
		return (int[])response[0].getDataObject();
	}
	
	public static Task[] listTask(WebConversation webCon, int inFolder, Date modified, boolean changed, boolean deleted, String host, String login, String password) throws Exception {
		host = appendPrefix(host);
		
		if (!changed && !deleted) {
			return new Task[] { };
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
		PropFindMethod propFindMethod = new PropFindMethod(host + TASK_URL);
		propFindMethod.setDoAuthentication( true );
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		propFindMethod.setRequestBody(bais);
		
		int status = httpclient.executeMethod(propFindMethod);
		
		assertEquals("check propfind response", 207, status);
		
		byte responseByte[] = propFindMethod.getResponseBody();
		
		bais = new ByteArrayInputStream(responseByte);
		final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.TASK);
		
		Task[] taskArray = new Task[response.length];
		for (int a = 0; a < taskArray.length; a++) {
			if (response[a].hasError()) {
				fail("xml error: " + response[a].getErrorMessage());
			}
			
			taskArray[a] = (Task)response[a].getDataObject();
			assertNotNull("last modified is null", taskArray[a].getLastModified());
		}
		
		return taskArray;
	}
	
	public static Task loadTask(WebConversation webCon, int objectId, int inFolder, String host, String login, String password) throws OXException, Exception {
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
		PropFindMethod propFindMethod = new PropFindMethod(host + TASK_URL);
		propFindMethod.setDoAuthentication( true );
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		propFindMethod.setRequestBody(bais);
		
		int status = httpclient.executeMethod(propFindMethod);
		
		assertEquals("check propfind response", 207, status);
		
		byte responseByte[] = propFindMethod.getResponseBody();
		
		bais = new ByteArrayInputStream(responseByte);
		final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.TASK);
		
		assertEquals("check response" , 1, response.length);
		
		if (response[0].hasError()) {
			throw new TestException(response[0].getErrorMessage());
		}
		
		assertEquals("check response status", 200, response[0].getStatus());
		
		return (Task)response[0].getDataObject();
	}
	
	private static HashSet participants2String(Participant[] participant) throws Exception {
		if (participant == null) {
			return null;
		}
		
		HashSet hs = new HashSet();
		
		for (int a = 0; a < participant.length; a++) {
			hs.add(participant2String(participant[a]));
		}
		
		return hs;
	}
	
	private static String participant2String(Participant p) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("T" + p.getType());
		sb.append("ID" + p.getIdentifier());
		
		return sb.toString();
	}
	
	private static HashSet users2String(UserParticipant[] users) throws Exception {
		if (users == null) {
			return null;
		}
		
		HashSet hs = new HashSet();
		
		for (int a = 0; a < users.length; a++) {
			hs.add(user2String(users[a]));
		}
		
		return hs;
	}
	
	private static String user2String(UserParticipant user) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("ID" + user.getIdentifier());
		sb.append("C" + user.getConfirm());
		
		return sb.toString();
	}
}

