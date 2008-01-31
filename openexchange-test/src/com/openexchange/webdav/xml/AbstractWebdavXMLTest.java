package com.openexchange.webdav.xml;

import com.meterware.httpunit.PutMethodWebRequest;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.webdav.AbstractWebdavTest;
import com.openexchange.webdav.xml.fields.DataFields;
import com.openexchange.webdav.xml.request.PropFindMethod;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */

public abstract class AbstractWebdavXMLTest extends AbstractWebdavTest {
	
	public AbstractWebdavXMLTest(String name) {
		super(name);
	}
	
	protected static int parseResponse(Document response, boolean delete) throws Exception {
		return parseRootElement(response.getRootElement(), delete);
	}
	
	protected static int parseRootElement(Element e, boolean delete) throws Exception {
		assertNotNull("root element (null)", e);
		assertEquals("root element", "multistatus", e.getName());
		
		return parseResponseElement(e.getChild("response", webdav), delete);
	}
	
	protected static int parseResponseElement(Element e, boolean delete) throws Exception {
		assertNotNull("response element (null)", e);
		assertEquals("response element", "response", e.getName());
		
		parseHrefElement(e.getChild("href", webdav), delete);
		return parsePropstatElement(e.getChild("propstat", webdav));
	}
	
	protected static void parseHrefElement(Element e, boolean delete) throws Exception {
		assertNotNull("response element (null)", e);
		assertEquals("response element", "href", e.getName());
		if (!delete) {
			assertTrue("href value > 0", (Integer.parseInt(e.getValue()) > 0));
		}
	}
	
	protected static int parsePropstatElement(Element e) throws Exception {
		assertNotNull("propstat element (null)", e);
		assertEquals("propstat element", "propstat", e.getName());
		
		parseStatusElement(e.getChild("status", webdav));
		parseResponsedescriptionElement(e.getChild("responsedescription", webdav));
		
		return parsePropElement(e.getChild("prop", webdav));
	}
	
	protected static int parsePropElement(Element e) throws Exception {
		assertNotNull("prop element (null)", e);
		assertEquals("prop element", "prop", e.getName());
		
		return parseObjectIdElement(e.getChild(DataFields.OBJECT_ID, XmlServlet.NS));
	}
	
	protected static void parseStatusElement(Element e) throws Exception {
		assertNotNull("status element (null)", e);
		assertEquals("status element", "status", e.getName());
		assertNotNull("status not null", e.getValue());
		assertEquals("status 200", 200, Integer.parseInt(e.getValue()));
	}
	
	protected static void parseResponsedescriptionElement(Element e) throws Exception {
		assertNotNull("status element (null)", e);
		assertEquals("status element", "responsedescription", e.getName());
		assertNotNull("response description not null", e.getValue());
	}
	
	protected static int parseObjectIdElement(Element e) throws Exception {
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
		
		InputStream is = new ByteArrayInputStream(requestByte);
		propFindMethod.setRequestBody(is);
		
		int status = httpclient.executeMethod(propFindMethod);
		
		assertEquals("check propfind response", 207, status);
		
        is = propFindMethod.getResponseBodyAsStream();
        
		Document doc = new SAXBuilder().build(is);
		
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
	
	public static void assertEqualsAndNotNull(String message, Date expect, Date value) throws Exception {
		if (expect != null) {
			assertNotNull(message + " is null", value);
			assertEquals(message, expect.getTime(), value.getTime());
		}
	}
	
	public static void assertEqualsAndNotNull(String message, Date[] expect, Date[] value) throws Exception {
		if (expect != null) {
			assertNotNull(message + " is null", value);
			assertEquals(message + " date array size is not equals", expect.length, value.length);
			for (int a = 0; a < expect.length; a++) {
				assertEquals(message + " byte in pos (" + a + ") is not equals",  expect[a].getTime(), value[a].getTime());
			}
		}
	}
	
	public static void assertEqualsAndNotNull(String message, byte[] expect, byte[] value) throws Exception {
		if (expect != null) {
			assertNotNull(message + " is null", value);
			assertEquals(message + " byte array size is not equals", expect.length, value.length);
			for (int a = 0; a < expect.length; a++) {
				assertEquals(message + " byte in pos (" + a + ") is not equals",  expect[a], value[a]);
			}
		}
	}
	
	public static void assertEqualsAndNotNull(String message, Object expect, Object value) throws Exception {
		if (expect != null) {
			assertNotNull(message + " is null", value);
			assertEquals(message, expect, value);
		}
	}
	
	public static String appendPrefix(String host) {
		if (host.startsWith("http://")) {
			return host;
		}
		return "http://" + host;
	}
	
	protected static void assertExceptionMessage(String message, int expectedStatus) throws Exception {
		System.out.println("message: "+ message);
		int status = Integer.parseInt(message.substring(1, 5));
		assertEquals("message status is not correct", expectedStatus, status);
	}
}
