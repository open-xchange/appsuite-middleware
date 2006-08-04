package com.openexchange.webdav.xml;

import com.meterware.httpunit.Base64;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.api.OXObject;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.ldap.Group;
import com.openexchange.groupware.ldap.GroupStorage;
import com.openexchange.groupware.ldap.Resource;
import com.openexchange.groupware.ldap.ResourceStorage;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.sessiond.SessiondConnector;
import com.openexchange.tools.OXFolderTools;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.Date;
import java.util.Properties;

import junit.framework.TestCase;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */

public abstract class AbstractWebdavTest extends TestCase {
	
	protected static final String PROTOCOL = "http://";
	
	protected static final String webdavPropertiesFile = "webdavPropertiesFile";
	
	protected static final String propertyHost = "hostname";
	
	protected static final String propertyLogin = "login";
	
	protected static final String propertyPassword = "password";
	
	protected static final Namespace webdav = Namespace.getNamespace("D", "DAV:");
	
	protected static String sessionId = null;
	
	protected static String hostName = "localhost";
	
	protected static String login = null;
	
	protected static String password = null;
	
	protected static int userId = -1;
	
	protected static Properties webdavProps = null;
	
	protected static SessionObject sessionObj = null;
	
	protected static String authData = null;
	
	protected WebRequest req = null;
	
	protected WebResponse resp = null;
	
	protected WebConversation webCon = null;
	
	protected static int appointmentFolderId = -1;

	protected static int taskFolderId = -1;
	
	protected static int contactFolderId = -1;
	
	protected static long startTime = 0;
	
	protected static long endTime = 0;
	
	protected static int userParticipantId2 = -1;
	
	protected static int userParticipantId3 = -1;
	
	protected static int groupParticipantId1 = -1;
	
	protected static int resourceParticipantId1 = -1;
	
	protected static String appointmentUrl = "/servlet/webdav.calendar";

	protected static String taskUrl = "/servlet/webdav.tasks";
	
	protected static String contactUrl = "/servlet/webdav.contacts";
	
	protected static String folderUrl = "/servlet/webdav.folders";

	protected static String attachmentUrl = "/servlet/webdav.attachments";
	
	private static boolean isInit = false;
	
	/**
	 * {@inheritDoc}
	 */
	protected void setUp() throws Exception {
		super.setUp();
		init();
		
		webCon = new WebConversation();
	}
	
	public void init() throws Exception {
		if (isInit) {
			return ;
		}
		
		Init.loadSystemProperties();
		Init.loadServerConf();
		Init.initDB();
		Init.initSessiond();
		
		String propfile = Init.getTestProperties().getProperty(webdavPropertiesFile);
		
		if (propfile == null) {
			throw new Exception("no webdav propfile given!");
		}
		
		webdavProps = new Properties();
		webdavProps.load(new FileInputStream(propfile));
		
		hostName = AbstractConfigWrapper.parseProperty(webdavProps, propertyHost, hostName);
		login = AbstractConfigWrapper.parseProperty(webdavProps, propertyLogin, login);
		password = AbstractConfigWrapper.parseProperty(webdavProps, propertyPassword, password);
		
		SessiondConnector sc = SessiondConnector.getInstance();
		sessionObj = sc.addSession(login, password, "localhost");
		
		GroupStorage groupStorage = GroupStorage.getInstance(sessionObj.getContext());
		ResourceStorage resourceStorage = ResourceStorage.getInstance(sessionObj.getContext());
		
		userId = sessionObj.getUserObject().getId();
		
		String userParticipant2 = AbstractConfigWrapper.parseProperty(webdavProps, "user_participant2", "");
		String userParticipant3 = AbstractConfigWrapper.parseProperty(webdavProps, "user_participant3", "");
		
		userParticipantId2 = sc.addSession(userParticipant2, password, "localhost").getUserObject().getId();
		userParticipantId3 = sc.addSession(userParticipant3, password, "localhost").getUserObject().getId();
		
		String groupParticipant = AbstractConfigWrapper.parseProperty(webdavProps, "group_participant", "");
		
		Group group = groupStorage.searchGroups(groupParticipant, new String[] { GroupStorage.DISPLAYNAME } )[0];
		
		groupParticipantId1 = group.getIdentifier();
		
		String resourceParticipant = AbstractConfigWrapper.parseProperty(webdavProps, "resource_participant", "");
		
		Resource resource = resourceStorage.searchResources(resourceParticipant)[0];
		
		resourceParticipantId1 = resource.getIdentifier();
		
		appointmentUrl = AbstractConfigWrapper.parseProperty(webdavProps, "appointment_url", appointmentUrl);
		taskUrl = AbstractConfigWrapper.parseProperty(webdavProps, "task_url", taskUrl);
		contactUrl = AbstractConfigWrapper.parseProperty(webdavProps, "contact_url", contactUrl);
		folderUrl = AbstractConfigWrapper.parseProperty(webdavProps, "folder_url", contactUrl);
		attachmentUrl = AbstractConfigWrapper.parseProperty(webdavProps, "attachment_url", attachmentUrl);
		
		appointmentFolderId = OXFolderTools.getCalendarStandardFolder(userId, sessionObj.getContext());
		contactFolderId = OXFolderTools.getContactStandardFolder(userId, sessionObj.getContext());
		taskFolderId = OXFolderTools.getTaskStandardFolder(userId, sessionObj.getContext());
		
		if (password == null) {
			password = "";
		}
		
		authData = new String(Base64.encode(login + ":" + password));
		
		isInit = true;
	}
	
	protected int parseResponse(Document response, boolean delete) throws Exception {
		return parseRootElement(response.getRootElement(), delete);
	}
	
	protected int parseRootElement(Element e, boolean delete) throws Exception {
		assertNotNull("root element (null)", e);
		assertEquals("root element", "multistatus", e.getName());
		
		return parseResponseElement(e.getChild("response", webdav), delete);
	}
	
	protected int parseResponseElement(Element e, boolean delete) throws Exception {
		assertNotNull("response element (null)", e);
		assertEquals("response element", "response", e.getName());
		
		parseHrefElement(e.getChild("href", webdav), delete);
		return parsePropstatElement(e.getChild("propstat", webdav));
	}
	
	protected void parseHrefElement(Element e, boolean delete) throws Exception {
		assertNotNull("response element (null)", e);
		assertEquals("response element", "href", e.getName());
		if (!delete) {
			assertTrue("href value > 0", (Integer.parseInt(e.getValue()) > 0));
		}
	}
	
	protected int parsePropstatElement(Element e) throws Exception {
		assertNotNull("propstat element (null)", e);
		assertEquals("propstat element", "propstat", e.getName());
		
		parseStatusElement(e.getChild("status", webdav));
		parseResponsedescriptionElement(e.getChild("responsedescription", webdav));
		
		return parsePropElement(e.getChild("prop", webdav));
	}
	
	protected int parsePropElement(Element e) throws Exception {
		assertNotNull("prop element (null)", e);
		assertEquals("prop element", "prop", e.getName());
		
		return parseObjectIdElement(e.getChild(OXObject.OBJECT_ID, XmlServlet.NS));
	}
	
	protected void parseStatusElement(Element e) throws Exception {
		assertNotNull("status element (null)", e);
		assertEquals("status element", "status", e.getName());
		assertNotNull("status not null", e.getValue());
		assertEquals("status 200", 200, Integer.parseInt(e.getValue()));
	}
	
	protected void parseResponsedescriptionElement(Element e) throws Exception {
		assertNotNull("status element (null)", e);
		assertEquals("status element", "responsedescription", e.getName());
		assertNotNull("response description not null", e.getValue());
	}
	
	protected int parseObjectIdElement(Element e) throws Exception {
		assertNotNull("object_id element (null)", e);
		assertEquals("object_id element", "object_id", e.getName());
		assertNotNull("object_id null", e.getValue());
		
		int objectId = Integer.parseInt(e.getValue());
		
		assertTrue("object id > 0", (objectId > 0));
		
		return objectId;
	}
	
	protected byte[] writeRequest(Element e_prop) throws Exception {
		Element e_root = new Element("propertyupdate", webdav);
		e_root.addNamespaceDeclaration(XmlServlet.NS);
		
		Element e_set = new Element("set", webdav);
		e_set.addContent(e_prop);
		
		e_root.addContent(e_set);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		Document doc = new Document(e_root);
		
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		baos.flush();
		
		return baos.toByteArray();
	}
	
	protected int sendPut(byte b[]) throws Exception {
		return sendPut(b, false);
	}
	
	protected int sendPut(byte b[], boolean delete) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		req = new PutMethodWebRequest(PROTOCOL + hostName + getURL(), bais, "text/javascript");
		req.setHeaderField("Authorization", "Basic " + authData);
		resp = webCon.getResponse(req);
		
		bais = new ByteArrayInputStream(resp.getText().getBytes("UTF-8"));
		
		Document doc = new SAXBuilder().build(bais);
		return parseResponse(doc, delete);
	}
	
	protected void sendPropFind(byte requestByte[]) throws Exception {
		HttpClient httpclient = new HttpClient();
		
		httpclient.getState().setCredentials(null, new UsernamePasswordCredentials(login, password));
		PropFindMethod propFindMethod = new PropFindMethod(PROTOCOL + hostName + getURL());
		propFindMethod.setDoAuthentication( true );
		
		ByteArrayInputStream bais = new ByteArrayInputStream(requestByte);
		propFindMethod.setRequestBody(bais);
		
		int status = httpclient.executeMethod(propFindMethod);
		
		assertEquals("check propfind response", 207, status);
		
		byte responseByte[] = propFindMethod.getResponseBody();
		
		assertTrue("check body size", responseByte.length > 0);
		
		bais = new ByteArrayInputStream(responseByte);
		
		Document doc = new SAXBuilder().build(bais);

		parseResponse(doc, false);
	}
	
	protected void deleteObject(FolderChildObject folderChildObj, int inFolder) throws Exception {
		Element e_prop = new Element("prop", webdav);
		
		Element e_objectId = new Element("object_id", XmlServlet.NS);
		e_objectId.addContent(String.valueOf(folderChildObj.getObjectID()));
		e_prop.addContent(e_objectId);

		if (inFolder != -1) {
			Element eFolderId = new Element("folder_id", XmlServlet.NS);
			eFolderId.addContent(String.valueOf(inFolder));
			e_prop.addContent(eFolderId);
		} 
		
		Element e_method = new Element("method", XmlServlet.NS);
		e_method.addContent("DELETE");
		e_prop.addContent(e_method);
		
		byte[] b = writeRequest(e_prop);
		sendPut(b, true);
	}
		
	protected void listObjects(int folderId, Date lastSync, boolean delete) throws Exception {
		Element e_propfind = new Element("propfind", webdav);
		Element e_prop = new Element("prop", webdav);
		
		Element e_folderId = new Element("folder_id", XmlServlet.NS);
		Element e_lastSync = new Element("lastsync", XmlServlet.NS);
		Element e_objectmode = new Element("objectmode", XmlServlet.NS);
		
		e_folderId.addContent(String.valueOf(folderId));
		e_lastSync.addContent(String.valueOf(lastSync.getTime()));
		
		e_propfind.addContent(e_prop);
		e_prop.addContent(e_folderId);
		e_prop.addContent(e_lastSync);
		
		if (delete) {
			e_objectmode.addContent("NEW_AND_MODIFIED,DELETED");
			e_prop.addContent(e_objectmode);
		} 
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		Document doc = new Document(e_propfind);
		
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		baos.flush();
		
		sendPropFind(baos.toByteArray());
	} 
	
	protected void loadObject(int objectId) throws Exception {
		Element e_propfind = new Element("propfind", webdav);
		Element e_prop = new Element("prop", webdav);
		
		Element eObjectId = new Element("object_id", XmlServlet.NS);
		
		eObjectId.addContent(String.valueOf(objectId));
		
		e_propfind.addContent(e_prop);
		e_prop.addContent(eObjectId);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		Document doc = new Document(e_propfind);
		
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		baos.flush();
		
		sendPropFind(baos.toByteArray());
	}
	
	protected abstract String getURL();
	
	protected class PropFindMethod extends EntityEnclosingMethod {
		
		public PropFindMethod() {
			super();
		}
		
		public PropFindMethod(String url) {
			super(url);
		}
		
		public String getName() {
			return "PROPFIND";
		}
		
	}
	
}
