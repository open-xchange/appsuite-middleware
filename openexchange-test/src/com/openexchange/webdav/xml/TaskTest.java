package com.openexchange.webdav.xml;

import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.api.OXObject;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.ldap.Group;
import com.openexchange.groupware.ldap.Resource;
import com.openexchange.groupware.tasks.Task;
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

public class TaskTest extends AbstractWebdavTest {
	
	protected int userParticipantId2 = -1;
	
	protected int userParticipantId3 = -1;
	
	protected int groupParticipantId1 = -1;
	
	protected int resourceParticipantId1 = -1;
	
	protected int taskFolderId = -1;
	
	protected String userParticipant2 = null;
	
	protected String userParticipant3 = null;
	
	protected String groupParticipant = null;
	
	protected Date startTime = null;
	
	protected Date endTime = null;
	
	protected Date dateCompleted = null;
	
	private static final long d7 = 604800000;
	
	private static final String TASK_URL = "/servlet/webdav.tasks";
	
	protected void setUp() throws Exception {
		super.setUp();
		
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 12);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		startTime = new Date(c.getTimeInMillis());
		endTime = new Date(startTime.getTime() + 3600000);
		
		dateCompleted = new Date(c.getTimeInMillis() + d7);
		
		userParticipant2 = AbstractConfigWrapper.parseProperty(webdavProps, "user_participant2", "");
		userParticipant3 = AbstractConfigWrapper.parseProperty(webdavProps, "user_participant3", "");
		
		groupParticipant = AbstractConfigWrapper.parseProperty(webdavProps, "group_participant", "");
		
		final FolderObject folderObj = FolderTest.getTaskDefaultFolder(webCon, PROTOCOL + hostName, login, password);
		taskFolderId = folderObj.getObjectID();
	}
	
	public void testNewTask() throws Exception {
		Task taskObj = createTask("testNewTask");
		insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
	}
	
	public void testNewTaskWithParticipants() throws Exception {
		Task taskObj = createTask("testNewTaskWithParticipants");
		
		ContactObject[] contactArray = GroupUserTest.searchUser(webCon, userParticipant2, new Date(0), PROTOCOL + hostName, login, password);
		assertTrue("contact array size is not > 0", contactArray.length > 0);
		int userParticipantId = contactArray[0].getInternalUserId();
		Group[] groupArray = GroupUserTest.searchGroup(webCon, "*", new Date(0), PROTOCOL + hostName, login, password);
		assertTrue("group array size is not > 0", groupArray.length > 0);
		int groupParticipantId = groupArray[0].getIdentifier();
		
		com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[3];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userId);
		participants[1] = new UserParticipant();
		participants[1].setIdentifier(userParticipantId);
		participants[2] = new GroupParticipant();
		participants[2].setIdentifier(groupParticipantId);
		
		taskObj.setParticipants(participants);
		
		insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
	}
	
	public void testNewTaskWithUsers() throws Exception {
		Task taskObj = createTask("testNewTaskWithUsers");
		
		UserParticipant[] users = new UserParticipant[1];
		users[0] = new UserParticipant();
		users[0].setIdentifier(userId);
		users[0].setConfirm(CalendarObject.ACCEPT);
		
		taskObj.setUsers(users);
		
		insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
	}
	
	public void testUpdateTask() throws Exception {
		Task taskObj = createTask("testUpdateTask");
		int objectId = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
		
		taskObj = createTask("testUpdateTask2");
		taskObj.setNote(null);
		
		updateTask(webCon, taskObj, objectId, taskFolderId, PROTOCOL + hostName, login, password);
	}
	
	public void testUpdateTaskWithParticipants() throws Exception {
		Task taskObj = createTask("testUpdateTask");
		int objectId = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
		
		taskObj = createTask("testUpdateTask");
		
		ContactObject[] contactArray = GroupUserTest.searchUser(webCon, userParticipant3, new Date(0), PROTOCOL + hostName, login, password);
		assertTrue("contact array size is not > 0", contactArray.length > 0);
		int userParticipantId = contactArray[0].getInternalUserId();
		Group[] groupArray = GroupUserTest.searchGroup(webCon, "*", new Date(0), PROTOCOL + hostName, login, password);
		assertTrue("group array size is not > 0", groupArray.length > 0);
		int groupParticipantId = groupArray[0].getIdentifier();
		Resource[] resourceArray = GroupUserTest.searchResource(webCon, "*", new Date(0), PROTOCOL + hostName, login, password);
		assertTrue("resource array size is not > 0", resourceArray.length > 0);
		int resourceParticipantId = resourceArray[0].getIdentifier();
		
		com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[4];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userId);
		participants[1] = new UserParticipant();
		participants[1].setIdentifier(userParticipantId);
		participants[2] = new GroupParticipant();
		participants[2].setIdentifier(groupParticipantId);
		participants[3] = new ResourceParticipant();
		participants[3].setIdentifier(resourceParticipantId);
		
		taskObj.setParticipants(participants);
		
		updateTask(webCon, taskObj, objectId, taskFolderId, PROTOCOL + hostName, login, password);
	}
	
	public void testDelete() throws Exception {
		Task taskObj = createTask("testDelete");
		int objectId1 = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
		int objectId2 = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
		
		int[][] objectIdAndFolderId = { { objectId1, taskFolderId }, { objectId2, taskFolderId } };
		
		deleteTask(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password);
	}
	
	public void testPropFindWithModified() throws Exception {
		Date modified = new Date();
		
		Task taskObj = createTask("testPropFindWithModified");
		insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
		insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
		
		Task[] taskArray = listTask(webCon, taskFolderId, modified, "NEW_AND_MODIFIED", PROTOCOL + hostName, login, password);
		
		assertTrue("check response", taskArray.length >= 2);
	}
	
	public void testPropFindWithDelete() throws Exception {
		Date modified = new Date();
		
		Task taskObj = createTask("testPropFindWithModified");
		int objectId1 = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
		int objectId2 = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
		
		int[][] objectIdAndFolderId = { { objectId1, taskFolderId }, { objectId2, taskFolderId } };
		
		deleteTask(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password);
		
		Task[] taskArray = listTask(webCon, taskFolderId, modified, "DELETED", PROTOCOL + hostName, login, password);
		
		assertTrue("wrong response array length", taskArray.length >= 2);
	}
	
	public void testPropFindWithObjectId() throws Exception {
		Task taskObj = createTask("testPropFindWithObjectId");
		int objectId = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
		
		Task loadTask = loadTask(webCon, objectId, taskFolderId, PROTOCOL + hostName, login, password);
	}
	
	public void testListWithAllFields() throws Exception {
		Date modified = new Date();
		
		Task taskObj = new Task();
		taskObj.setTitle("testGetWithAllFields");
		taskObj.setStartDate(startTime);
		taskObj.setEndDate(endTime);
		taskObj.setParentFolderID(taskFolderId);
		taskObj.setPrivateFlag(true);
		taskObj.setLabel(2);
		taskObj.setNote("note");
		taskObj.setCategories("testcat1,testcat2,testcat3");
		taskObj.setActualCosts(1.5F);
		taskObj.setActualDuration(3.5F);
		taskObj.setBillingInformation("billing information");
		taskObj.setCompanies("companies");
		taskObj.setCurrency("currency");
		taskObj.setDateCompleted(dateCompleted);
		taskObj.setDurationType(Task.DURATION_HOURS);
		taskObj.setPercentComplete(50);
		taskObj.setPriority(Task.HIGH);
		taskObj.setStatus(Task.IN_PROGRESS);
		taskObj.setTargetCosts(5.5F);
		taskObj.setTargetDuration(7.5F);
		taskObj.setTripMeter("trip meter");
		
		int objectId = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
		
		Task[] taskArray = listTask(webCon, taskFolderId, modified, "NEW_AND_MODIFIED", PROTOCOL + hostName, login, password);
		
		assertEquals("wrong response array length", 1, taskArray.length);
		
		Task loadTask = taskArray[0];
		
		taskObj.setObjectID(objectId);
		taskObj.setStartDate(startTime);
		taskObj.setEndDate(endTime);
		compareObject(taskObj, loadTask);
	}
	
	public void _notestConfirm() throws Exception {
		Task taskObj = createTask("testConfirm");
		int objectId = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
		confirmTask(webCon, objectId, CalendarObject.DECLINE, null, PROTOCOL + hostName, login, password);
	}
	
	private void compareObject(Task taskObj1, Task taskObj2) throws Exception {
		assertEquals("id is not equals", taskObj1.getObjectID(), taskObj2.getObjectID());
		assertEqualsAndNotNull("title is not equals", taskObj1.getTitle(), taskObj2.getTitle());
		assertEqualsAndNotNull("start is not equals", taskObj1.getStartDate(), taskObj2.getStartDate());
		assertEqualsAndNotNull("end is not equals", taskObj1.getEndDate(), taskObj2.getEndDate());
		assertEquals("folder id is not equals", taskObj1.getParentFolderID(), taskObj2.getParentFolderID());
		assertEquals("private flag is not equals", taskObj1.getPrivateFlag(), taskObj2.getPrivateFlag());
		assertEquals("label is not equals", taskObj1.getLabel(), taskObj2.getLabel());
		assertEqualsAndNotNull("note is not equals", taskObj1.getNote(), taskObj2.getNote());
		assertEqualsAndNotNull("categories is not equals", taskObj1.getCategories(), taskObj2.getCategories());
		assertEqualsAndNotNull("actual costs is not equals", taskObj1.getActualCosts(), taskObj2.getActualCosts());
		assertEqualsAndNotNull("actual duration", taskObj1.getActualDuration(), taskObj2.getActualDuration());
		assertEqualsAndNotNull("billing information", taskObj1.getBillingInformation(), taskObj2.getBillingInformation());
		assertEqualsAndNotNull("companies", taskObj1.getCompanies(), taskObj2.getCompanies());
		assertEqualsAndNotNull("currency", taskObj1.getCurrency(), taskObj2.getCurrency());
		assertEqualsAndNotNull("date completed", taskObj1.getDateCompleted(), taskObj2.getDateCompleted());
		assertEqualsAndNotNull("duration type", taskObj1.getDurationType(), taskObj2.getDurationType());
		assertEqualsAndNotNull("percent complete", taskObj1.getPercentComplete(), taskObj2.getPercentComplete());
		assertEqualsAndNotNull("priority", taskObj1.getPriority(), taskObj2.getPriority());
		assertEqualsAndNotNull("status", taskObj1.getStatus(), taskObj2.getStatus());
		assertEqualsAndNotNull("target costs", taskObj1.getTargetCosts(), taskObj2.getTargetCosts());
		assertEqualsAndNotNull("target duration", taskObj1.getTargetDuration(), taskObj2.getTargetDuration());
		assertEqualsAndNotNull("trip meter", taskObj1.getTripMeter(), taskObj2.getTripMeter());
		
		assertEqualsAndNotNull("participants are not equals" , participants2String(taskObj1.getParticipants()), participants2String(taskObj2.getParticipants()));
		assertEqualsAndNotNull("users are not equals" , users2String(taskObj1.getUsers()), users2String(taskObj2.getUsers()));
	}
	
	private Task createTask(String title) throws Exception {
		Task taskObj = new Task();
		taskObj.setTitle(title);
		taskObj.setStartDate(startTime);
		taskObj.setEndDate(endTime);
		taskObj.setParentFolderID(taskFolderId);
		taskObj.setStatus(Task.IN_PROGRESS);
		
		return taskObj;
	}
	
	public static int insertTask(WebConversation webCon, Task taskObj, String host, String login, String password) throws Exception {
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
			fail("xml error: " + response[0].getErrorMessage());
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
		taskObj.setObjectID(objectId);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		Element eProp = new Element("prop", webdav);
		
		TaskWriter appointmentWriter = new TaskWriter();
		appointmentWriter.addContent2PropElement(eProp, taskObj, false);
		Element eInFolder = new Element("infolder", XmlServlet.NS);
		eInFolder.addContent(String.valueOf(inFolder));
		eProp.addContent(eInFolder);

		
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
		} else {
			taskObj = (Task)response[0].getDataObject();
			objectId = taskObj.getObjectID();
			
			assertNotNull("last modified is null", taskObj.getLastModified());
			assertTrue("last modified is not > 0", taskObj.getLastModified().getTime() > 0);
		}
		
		assertEquals("check response status", 200, response[0].getStatus());
	}
	
	public static int[] deleteTask(WebConversation webCon, int[][] objectIdAndFolderId, String host, String login, String password) throws Exception {
		Element rootElement = new Element("multistatus", webdav);
		rootElement.addNamespaceDeclaration(XmlServlet.NS);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		for (int a = 0; a < objectIdAndFolderId.length; a++) {
			int[] i = objectIdAndFolderId[a];
			
			Task taskObj = new Task();
			taskObj.setObjectID(i[0]);
			
			Element eProp = new Element("prop", webdav);
			
			TaskWriter taskWriter = new TaskWriter();
			taskWriter.addContent2PropElement(eProp, taskObj, false);
			Element eInFolder = new Element("infolder", XmlServlet.NS);
			eInFolder.addContent(String.valueOf(i[1]));
			eProp.addContent(eInFolder);

			
			rootElement.addContent(addProp2PropertyUpdate(eProp));
		}
		
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
		
		assertEquals("check response", objectIdAndFolderId.length, response.length);
		
		ArrayList idList = new ArrayList();
		
		for (int a = 0; a < response.length; a++) {
			Task taskObj = (Task)response[a].getDataObject();
			
			if (response[a].hasError()) {
				idList.add(new Integer(taskObj.getObjectID()));
			} else {
				assertNotNull("last modified is null", taskObj.getLastModified());
				assertTrue("last modified is not > 0", taskObj.getLastModified().getTime() > 0);
			}
			
			assertEquals("check response status", 200, response[a].getStatus());
		}
		
		int[] failed = new int[idList.size()];
		
		for (int a = 0; a < failed.length; a++) {
			failed[a] = ((Integer)idList.get(a)).intValue();
		}
		
		return failed;
	}
	
	public static void confirmTask(WebConversation webCon, int objectId, int confirm, String confirmMessage, String host, String login, String password) throws Exception {
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
	
	public static Task[] listTask(WebConversation webCon, int inFolder, Date modified, String objectMode, String host, String login, String password) throws Exception {
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
		}
		
		return taskArray;
	}
	
	public static Task loadTask(WebConversation webCon, int objectId, int inFolder, String host, String login, String password) throws Exception {
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
			fail("xml error: " + response[0].getErrorMessage());
		}
		
		assertEquals("check response status", 200, response[0].getStatus());
		
		return (Task)response[0].getDataObject();
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

