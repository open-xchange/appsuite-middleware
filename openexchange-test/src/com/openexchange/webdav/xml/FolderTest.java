package com.openexchange.webdav.xml;

import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.api.OXConflictException;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.OCLPermission;
import com.openexchange.test.TestException;
import com.openexchange.webdav.xml.fields.FolderFields;
import com.openexchange.webdav.xml.parser.ResponseParser;
import com.openexchange.webdav.xml.request.PropFindMethod;
import com.openexchange.webdav.xml.types.Response;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

public class FolderTest extends AbstractWebdavXMLTest {
	
	public static final String FOLDER_URL = "/servlet/webdav.folders";
	
	protected int userParticipantId2 = -1;
	
	protected int userParticipantId3 = -1;
	
	protected int groupParticipantId1 = -1;
	
	protected String userParticipant2 = null;
	
	protected String userParticipant3 = null;
	
	protected String groupParticipant = null;
	
	public FolderTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		
		userParticipant2 = AbstractConfigWrapper.parseProperty(webdavProps, "user_participant2", "");
		userParticipant3 = AbstractConfigWrapper.parseProperty(webdavProps, "user_participant3", "");
		
		groupParticipant = AbstractConfigWrapper.parseProperty(webdavProps, "group_participant", "");
	}
	
	protected void compareFolder(FolderObject folderObj1, FolderObject folderObj2) throws Exception {
		assertEqualsAndNotNull("id is not equals", folderObj1.getObjectID(), folderObj2.getObjectID());
		assertEqualsAndNotNull("folder name is not equals", folderObj1.getFolderName(), folderObj2.getFolderName());
		
		if (folderObj1.containsType()) {
			assertEqualsAndNotNull("type is not equals", folderObj1.getType(), folderObj2.getType());
		}
		
		if (folderObj1.containsModule()) {
			assertEqualsAndNotNull("module name is not equals", folderObj1.getModule(), folderObj2.getModule());
		}
		
		assertEqualsAndNotNull("parent folder id is not equals", folderObj1.getParentFolderID(), folderObj2.getParentFolderID());
		
		if (folderObj1.containsPermissions()) {
			assertEqualsAndNotNull("permissions are not equals" , permissions2String(folderObj1.getPermissionsAsArray()), permissions2String(folderObj2.getPermissionsAsArray()));
		}
	}
	
	public static FolderObject createFolderObject(int entity, String title, int module, boolean isPublic) throws Exception {
		FolderObject folderObj = new FolderObject();
		folderObj.setFolderName(title + System.currentTimeMillis());
		folderObj.setModule(module);
		
		if (isPublic) {
			folderObj.setType(FolderObject.PUBLIC);
			folderObj.setParentFolderID(2);
		} else {
			folderObj.setType(FolderObject.PRIVATE);
			folderObj.setParentFolderID(1);
		}
		
		folderObj.setPermissionsAsArray(new OCLPermission[] { createPermission( entity, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION) } );
		
		return folderObj;
	}
	
	public static OCLPermission createPermission(int entity, boolean isGroup, int fp, int orp, int owp, int odp) throws Exception {
		return createPermission(entity, isGroup, fp, orp, owp, odp, true);
	}
	
	public static OCLPermission createPermission(int entity, boolean isGroup, int fp, int orp, int owp, int odp, boolean isAdmin) throws Exception {
		OCLPermission oclp = new OCLPermission();
		oclp.setEntity(entity);
		oclp.setGroupPermission(isGroup);
		oclp.setFolderAdmin(isAdmin);
		oclp.setFolderPermission(fp);
		oclp.setReadObjectPermission(orp);
		oclp.setWriteObjectPermission(owp);
		oclp.setDeleteObjectPermission(odp);
		
		return oclp;
	}
	
	public static int insertFolder(WebConversation webCon, FolderObject folderObj, String host, String login, String password) throws Exception, OXException {
		host = appendPrefix(host);
		
		int objectId = 0;
		
		folderObj.removeObjectID();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		Element eProp = new Element("prop", webdav);
		
		DataWriter.addElement(FolderFields.TITLE, folderObj.getFolderName(), eProp);
		DataWriter.addElement(FolderFields.FOLDER_ID, folderObj.getParentFolderID(), eProp);
		addElementType(folderObj.getType(), eProp);
		addElementModule(folderObj.getModule(), eProp);
		FolderWriter.addElementPermission(folderObj.getPermissions(), eProp);
		
		Document doc = addProp2Document(eProp);
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		byte b[] = baos.toByteArray();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		WebRequest req = new PutMethodWebRequest(host + FOLDER_URL, bais, "text/javascript");
		req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password));
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(207, resp.getResponseCode());
		
		bais = new ByteArrayInputStream(resp.getText().getBytes());
		final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.FOLDER);
		
		assertEquals("check response", 1, response.length);
		
		if (response[0].hasError()) {
			throw new TestException(response[0].getErrorMessage());
		}
		
		if (response[0].getStatus() != 200) {
			throw new TestException(response[0].getErrorMessage());
		}
		
		folderObj = (FolderObject)response[0].getDataObject();
		objectId = folderObj.getObjectID();
		
		assertTrue("check objectId", objectId > 0);
		
		return objectId;
	}
	
	public static void updateFolder(WebConversation webCon, FolderObject folderObj, String host, String login, String password) throws Exception, OXException {
		host = appendPrefix(host);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		Element eProp = new Element("prop", webdav);
		
		DataWriter.addElement(FolderFields.TITLE, folderObj.getFolderName(), eProp);
		DataWriter.addElement(FolderFields.OBJECT_ID, folderObj.getObjectID(), eProp);
		if (folderObj.containsParentFolderID()) {
			DataWriter.addElement("folder", folderObj.getParentFolderID(), eProp);
		}
		
		if (folderObj.containsPermissions()) {
			FolderWriter.addElementPermission(folderObj.getPermissions(), eProp);
		}
		
		Document doc = addProp2Document(eProp);
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		byte b[] = baos.toByteArray();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		WebRequest req = new PutMethodWebRequest(host + FOLDER_URL, bais, "text/javascript");
		req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password));
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(207, resp.getResponseCode());
		
		bais = new ByteArrayInputStream(resp.getText().getBytes());
		final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.FOLDER);
		
		assertEquals("check response", 1, response.length);
		
		if (response[0].hasError()) {
			fail("xml error: " + response[0].getErrorMessage());
		}
		
		if (response[0].getStatus() != 200) {
			throw new TestException(response[0].getErrorMessage());
		}
	}
	
	public static int[] deleteFolder(WebConversation webCon, int[] id, String host, String login, String password) throws Exception, OXException {
		return deleteFolder(webCon, id, new Date(), host, login, password);
	}
	
	public static int[] deleteFolder(WebConversation webCon, int[] id, Date lastModified, String host, String login, String password) throws Exception, OXException {
		host = appendPrefix(host);
		
		Element rootElement = new Element("multistatus", webdav);
		rootElement.addNamespaceDeclaration(XmlServlet.NS);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		for (int a = 0; a < id.length; a++) {
			Element eProp = new Element("prop", webdav);
			DataWriter.addElement(FolderFields.OBJECT_ID, id[a], eProp);
			DataWriter.addElement(FolderFields.LAST_MODIFIED, lastModified, eProp);
			DataWriter.addElement("method", "DELETE", eProp);
			
			rootElement.addContent(addProp2PropertyUpdate(eProp));
		}
		
		Document doc = new Document(rootElement);
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		byte b[] = baos.toByteArray();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		WebRequest req = new PutMethodWebRequest(host + FOLDER_URL, bais, "text/javascript");
		req.setHeaderField(AUTHORIZATION, "Basic " + getAuthData(login, password));
		WebResponse resp = webCon.getResponse(req);
		
		assertEquals(207, resp.getResponseCode());
		
		bais = new ByteArrayInputStream(resp.getText().getBytes());
		final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.FOLDER);
		
		assertEquals("check response", id.length, response.length);
		
		ArrayList idList = new ArrayList();
		
		for (int a = 0; a < response.length; a++) {
			if (response[a].hasError()) {
				FolderObject folderObj = (FolderObject)response[a].getDataObject();
				idList.add(new Integer(folderObj.getObjectID()));
			}
			
			if (response[0].getStatus() != 200) {
				throw new TestException(response[0].getErrorMessage());
			}
		}
		
		int[] failed = new int[idList.size()];
		
		for (int a = 0; a < failed.length; a++) {
			failed[a] = ((Integer)idList.get(a)).intValue();
		}
		
		return failed;
	}
	
	public static int[] listFolder(WebConversation webCon, String host, String login, String password) throws Exception {
		host = appendPrefix(host);
		
		Element ePropfind = new Element("propfind", webdav);
		Element eProp = new Element("prop", webdav);
		
		Element eObjectmode = new Element("objectmode", XmlServlet.NS);
		
		eObjectmode.addContent("LIST");
		
		eProp.addContent(eObjectmode);
		
		ePropfind.addContent(eProp);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		Document doc = new Document(ePropfind);
		
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		baos.flush();
		
		HttpClient httpclient = new HttpClient();
		
		httpclient.getState().setCredentials(null, new UsernamePasswordCredentials(login, password));
		PropFindMethod propFindMethod = new PropFindMethod(host + FOLDER_URL);
		propFindMethod.setDoAuthentication( true );
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		propFindMethod.setRequestBody(bais);
		
		int status = httpclient.executeMethod(propFindMethod);
		
		assertEquals("check propfind response", 207, status);
		
		byte responseByte[] = propFindMethod.getResponseBody();
		
		bais = new ByteArrayInputStream(responseByte);
		final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.FOLDER, true);
		
		assertEquals("response length not is 1", 1, response.length);
		
		return (int[])response[0].getDataObject();
	}
	
	public static FolderObject[] listFolder(WebConversation webCon, Date modified, boolean changed, boolean deleted, String host, String login, String password) throws Exception {
		host = appendPrefix(host);
		
		if (!changed && !deleted) {
			return new FolderObject[] { };
		}
		
		Element ePropfind = new Element("propfind", webdav);
		Element eProp = new Element("prop", webdav);
		
		Element eLastSync = new Element("lastsync", XmlServlet.NS);
		Element eObjectmode = new Element("objectmode", XmlServlet.NS);
		
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
		eProp.addContent(eLastSync);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		Document doc = new Document(ePropfind);
		
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		baos.flush();
		
		HttpClient httpclient = new HttpClient();
		
		httpclient.getState().setCredentials(null, new UsernamePasswordCredentials(login, password));
		PropFindMethod propFindMethod = new PropFindMethod(host + FOLDER_URL);
		propFindMethod.setDoAuthentication( true );
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		propFindMethod.setRequestBody(bais);
		
		int status = httpclient.executeMethod(propFindMethod);
		
		assertEquals("check propfind response", 207, status);
		
		byte responseByte[] = propFindMethod.getResponseBody();
		
		bais = new ByteArrayInputStream(responseByte);
		final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.FOLDER);
		
		FolderObject[] folderArray = new FolderObject[response.length];
		for (int a = 0; a < folderArray.length; a++) {
			if (response[a].hasError()) {
				fail("xml error: " + response[a].getErrorMessage());
			}
			
			folderArray[a] = (FolderObject)response[a].getDataObject();
		}
		
		return folderArray;
	}
	
	public static FolderObject loadFolder(WebConversation webCon, int objectId, String host, String login, String password) throws Exception, OXException {
		host = appendPrefix(host);
		
		Element ePropfind = new Element("propfind", webdav);
		Element eProp = new Element("prop", webdav);
		
		Element eObjectId = new Element(FolderFields.OBJECT_ID, XmlServlet.NS);
		eObjectId.addContent(String.valueOf(objectId));
		
		ePropfind.addContent(eProp);
		eProp.addContent(eObjectId);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		Document doc = new Document(ePropfind);
		
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		baos.flush();
		
		HttpClient httpclient = new HttpClient();
		
		httpclient.getState().setCredentials(null, new UsernamePasswordCredentials(login, password));
		PropFindMethod propFindMethod = new PropFindMethod(host + FOLDER_URL);
		propFindMethod.setDoAuthentication( true );
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		propFindMethod.setRequestBody(bais);
		
		int status = httpclient.executeMethod(propFindMethod);
		
		assertEquals("check propfind response", 207, status);
		
		byte responseByte[] = propFindMethod.getResponseBody();
		
		bais = new ByteArrayInputStream(responseByte);
		final Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.FOLDER);
		
		assertTrue("no response object found", response.length > 0);
		
		FolderObject[] folderArray = new FolderObject[response.length];
		for (int a = 0; a < folderArray.length; a++) {
			if (response[a].hasError()) {
				throw new TestException(response[a].getErrorMessage());
			}
			
			folderArray[a] = (FolderObject)response[a].getDataObject();
		}
		
		assertEquals("id is not equals", objectId, folderArray[0].getObjectID());
		
		return folderArray[0];
	}
	
	
	public static FolderObject getAppointmentDefaultFolder(WebConversation webCon, String host, String login, String password) throws Exception {
		host = appendPrefix(host);
		
		FolderObject[] folderArray = listFolder(webCon, new Date(0), true, false, host, login, password);
		
		final int userId = GroupUserTest.getUserId(webCon, host, login, password);
		assertTrue("user not found", userId != -1);
		
		for (int a = 0; a < folderArray.length; a++) {
			FolderObject folderObj = folderArray[a];
			if (folderObj.isDefaultFolder() && folderObj.getModule() == FolderObject.CALENDAR && folderObj.getCreatedBy() == userId) {
				return folderObj;
			}
		}
		
		throw new OXConflictException("no appointment default folder found!");
	}
	
	public static FolderObject getContactDefaultFolder(WebConversation webCon, String host, String login, String password) throws Exception {
		host = appendPrefix(host);
		
		FolderObject[] folderArray = listFolder(webCon, new Date(0), true, false, host, login, password);
		
		final int userId = GroupUserTest.getUserId(webCon, host, login, password);
		assertTrue("user not found", userId != -1);
		
		for (int a = 0; a < folderArray.length; a++) {
			FolderObject folderObj = folderArray[a];
			if (folderObj.isDefaultFolder() && folderObj.getModule() == FolderObject.CONTACT && folderObj.getCreatedBy() == userId) {
				return folderObj;
			}
		}
		
		throw new OXConflictException("no contact default folder found!");
	}
	
	public static FolderObject getTaskDefaultFolder(WebConversation webCon, String host, String login, String password) throws Exception {
		host = appendPrefix(host);
		
		FolderObject[] folderArray = listFolder(webCon, new Date(0), true, false, host, login, password);
		
		final int userId = GroupUserTest.getUserId(webCon, host, login, password);
		assertTrue("user not found", userId != -1);
		
		for (int a = 0; a < folderArray.length; a++) {
			FolderObject folderObj = folderArray[a];
			if (folderObj.isDefaultFolder() && folderObj.getModule() == FolderObject.TASK && folderObj.getCreatedBy() == userId) {
				return folderObj;
			}
		}
		
		throw new OXConflictException("no task default folder found!");
	}
	
	protected static void addElementType(int type, Element parent) throws Exception {
		if (type == FolderObject.PRIVATE) {
			DataWriter.addElement(FolderFields.TYPE, FolderWriter.PRIVATE_STRING, parent);
		} else {
			DataWriter.addElement(FolderFields.TYPE, FolderWriter.PUBLIC_STRING, parent);
		}
	}
	
	protected static void addElementModule(int module, Element parent) throws Exception {
		switch (module) {
			case FolderObject.CALENDAR:
				DataWriter.addElement(FolderFields.MODULE, "calendar", parent);
				break;
			case FolderObject.CONTACT:
				DataWriter.addElement(FolderFields.MODULE, "contact", parent);
				break;
			case FolderObject.TASK:
				DataWriter.addElement(FolderFields.MODULE, "task", parent);
				break;
			default:
				throw new OXConflictException("invalid module: " + module);
		}
	}
	
	private HashSet permissions2String(OCLPermission[] oclp) throws Exception {
		if (oclp == null) {
			return null;
		}
		
		HashSet hs = new HashSet();
		
		for (int a = 0; a < oclp.length; a++) {
			hs.add(permission2String(oclp[a]));
		}
		
		return hs;
	}
	
	private String permission2String(OCLPermission oclp) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("ENTITY" + oclp.getEntity());
		sb.append("GROUP" + oclp.isGroupPermission());
		sb.append("ADMIN" + oclp.isFolderAdmin());
		sb.append("FP" + oclp.getFolderPermission());
		sb.append("ORP" + oclp.getReadPermission());
		sb.append("OWP" + oclp.getWritePermission());
		sb.append("ODP" + oclp.getDeletePermission());
		
		return sb.toString();
	}
}

