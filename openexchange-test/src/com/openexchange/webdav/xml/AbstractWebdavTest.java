package com.openexchange.webdav.xml;

import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.api.OXObject;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.sessiond.SessiondConnector;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import junit.framework.TestCase;
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
	
	protected String sessionId = null;
	
	protected String hostName = null;
	
	protected String login = null;
	
	protected String password = null;
	
	protected int userId = -1;
	
	protected static Properties webdavProps = null;
	
	protected WebRequest req = null;
	
	protected WebResponse resp = null;
	
	protected WebConversation webCon = null;
	
	protected SessionObject sessionObj = null;
	
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
		
		isInit = true;
	}
	
	protected int parseResponse(Document response) throws Exception {
		return parseRootElement(response.getRootElement());
	}
	
	protected int parseRootElement(Element e) throws Exception {
		assertNotNull("root element (null)", e);
		assertEquals("root element", "multistatus", e.getName());
		
		return parseResponseElement(e.getChild("response", webdav));
	}
	
	protected int parseResponseElement(Element e) throws Exception {
		assertNotNull("response element (null)", e);
		assertEquals("response element", "response", e.getName());
		
		parseHrefElement(e.getChild("href", webdav));
		return parsePropstatElement(e.getChild("propstat", webdav));
	}
	
	protected void parseHrefElement(Element e) throws Exception {
		assertNotNull("response element (null)", e);
		assertEquals("response element", "href", e.getName());
		assertTrue("href value > 0", (Integer.parseInt(e.getValue()) > 0));
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
		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		req = new PutMethodWebRequest(PROTOCOL + hostName + getURL(), bais, "text/javascript");
		req.setHeaderField("Authorization", "Basic b2Zmc3ByaW5nOm5ldGxpbmU=");
		resp = webCon.getResponse(req);
		
		bais = new ByteArrayInputStream(resp.getText().getBytes("UTF-8"));
		
		Document doc = new SAXBuilder().build(bais);
		return parseResponse(doc);
	}
	
	protected int sendPropFind(byte b[]) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		req = new PropFindMethodWebResponse(PROTOCOL + hostName + getURL(), bais, "text/javascript");
		resp = webCon.getResponse(req);
		
		bais = new ByteArrayInputStream(resp.getText().getBytes("UTF-8"));
		
		Document doc = new SAXBuilder().build(bais);
		return parseResponse(doc);
	}
	
	protected abstract String getURL();
	
	private class PropFindMethodWebResponse extends PutMethodWebRequest {
		
		public PropFindMethodWebResponse(String url, InputStream is, String contentType) {
			super(url, is, contentType);
		}
		
		public String getMethod() {
			return "PROPFIND";
		}
	}
	
}
