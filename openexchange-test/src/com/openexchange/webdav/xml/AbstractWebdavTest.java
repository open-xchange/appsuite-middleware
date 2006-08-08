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
import com.openexchange.sessiond.SessionObject;
import com.openexchange.webdav.xml.request.PropFindMethod;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.Properties;

import junit.framework.TestCase;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
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
	
	protected String hostName = "localhost";
	
	protected String login = null;
	
	protected String password = null;
	
	protected int userId = -1;
	
	protected Properties webdavProps = null;
	
	protected String authData = null;
	
	protected WebRequest req = null;
	
	protected WebResponse resp = null;
	
	protected WebConversation webCon = null;
	
	public static final String AUTHORIZATION = "authorization";
	
	/**
	 * {@inheritDoc}
	 */
	protected void setUp() throws Exception {
		super.setUp();
		webCon = new WebConversation();
		
		webdavProps = Init.getWebdavProperties();
		
		login = AbstractConfigWrapper.parseProperty(webdavProps, "login", "");
		password = AbstractConfigWrapper.parseProperty(webdavProps, "password", "");
		
		userId = GroupUserTest.searchUser(webCon, login, new Date(0), PROTOCOL + hostName, login, password)[0].getInternalUserId();
		
		authData = getAuthData(login, password);		
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
	
	protected static Element addProp2PropertyUpdate(Element eProp) throws Exception {
		Element rootElement = new Element("propertyupdate", webdav);
		rootElement.addNamespaceDeclaration(XmlServlet.NS);
		
		Element eSet = new Element("set", webdav);
		eSet.addContent(eProp);
		
		rootElement.addContent(eSet);
		
		return rootElement;
	}
	
	protected static Document addProp2Document(Element eProp) throws Exception {
		Element rootElement = new Element("propertyupdate", webdav);
		rootElement.addNamespaceDeclaration(XmlServlet.NS);
		
		Element eSet = new Element("set", webdav);
		eSet.addContent(eProp);
		
		rootElement.addContent(eSet);
		
		return new Document(rootElement);
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
		
		byte[] b = null; // writeRequest(e_prop);
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
	
	protected String getURL() {
		return "no/url";
	}
	
	protected static String getAuthData(String login, String password) throws Exception {
		if (password == null) {
			password = "";
		}
		
		return new String(Base64.encode(login + ":" + password));
	}
}
