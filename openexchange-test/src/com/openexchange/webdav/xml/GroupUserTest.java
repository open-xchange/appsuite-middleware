package com.openexchange.webdav.xml;

import com.meterware.httpunit.WebConversation;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.ldap.Group;
import com.openexchange.groupware.ldap.Resource;
import com.openexchange.groupware.ldap.ResourceGroup;
import com.openexchange.webdav.xml.parser.ResponseParser;
import com.openexchange.webdav.xml.request.PropFindMethod;
import com.openexchange.webdav.xml.types.Response;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.Map;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

public class GroupUserTest extends AbstractWebdavXMLTest {

    private static final Log LOG = LogFactory.getLog(GroupUserTest.class);

	public static final String GROUPUSER_URL = "/servlet/webdav.groupuser";
	
	public GroupUserTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testSearchUser() throws Exception {
		ContactObject[] contactObj = searchUser(webCon, "*", new Date(0), PROTOCOL + hostName, login, password);
		for (int a = 0; a < contactObj.length; a++) {
			assertTrue("id > 0 expected", contactObj[a].getInternalUserId() > 0);
			assertNotNull("last modified is null", contactObj[a].getLastModified());
		} 
	}
	
	public void testSearchGroup() throws Exception {
		Group group[] = searchGroup(webCon, "*", new Date(0), PROTOCOL + hostName, login, password);
		for (int a = 0; a < group.length; a++) {
			assertTrue("id > 0 expected", group[a].getIdentifier() > 0);
			assertNotNull("last modified is null", group[a].getLastModified());
		} 
	}
	
	public void testSearchResource() throws Exception {
		Resource[] resource = searchResource(getWebConversation(), "*", new Date(0), PROTOCOL + hostName, login, password);
		for (int a = 0; a < resource.length; a++) {
			assertTrue("id > 0 expected", resource[a].getIdentifier() > 0);
			assertNotNull("last modified is null", resource[a].getLastModified());
		} 
	}
	
	public void testSearchGroupWithLastModifed() throws Exception {
		Group group[] = searchGroup(getWebConversation(), "*", new Date(0), PROTOCOL + hostName, login, password);
		assertTrue("no group found in response (group array length == 0)", group.length > 0);
		
		int posInArray = 0;
		
		for (int a = 0; a < group.length; a++) {
			if (group[a].getIdentifier() != 1) {
				posInArray = a;
				break;
			}
		}
		
		int id = group[posInArray].getIdentifier();
		String displayName = group[posInArray].getDisplayName();
		Date lastModifed = group[posInArray].getLastModified();
		
		
		group = searchGroup(getWebConversation(), displayName, new Date(lastModifed.getTime()+5000), PROTOCOL + getHostName(), getLogin(), getPassword());
		
		boolean found = false;
		
		for (int a = 0; a < group.length; a++) {
			if (group[a].getIdentifier() == id) {
				found = true;
				break;
			}
		}

		assertFalse("unexpected id found in response", found);
	}
	
	public void testSearchResourceWithLastModifed() throws Exception {
		Resource resource[] = searchResource(getWebConversation(), "*", new Date(0), PROTOCOL + getHostName(), getLogin(), getPassword());
		assertTrue("no group found in response (group array length == 0)", resource.length > 0);
		
		int posInArray = 0;
		
		for (int a = 0; a < resource.length; a++) {
			if (resource[a].getIdentifier() != 1) {
				posInArray = a;
				break;
			}
		}
		
		int id = resource[posInArray].getIdentifier();
		String displayName = resource[posInArray].getDisplayName();
		Date lastModifed = resource[posInArray].getLastModified();
		
		
		resource = searchResource(getWebConversation(), displayName, new Date(lastModifed.getTime()+5000), PROTOCOL + getHostName(), getLogin(), getPassword());
		
		boolean found = false;
		
		for (int a = 0; a < resource.length; a++) {
			if (resource[a].getIdentifier() == id) {
				found = true;
				break;
			}
		}

		assertFalse("unexpected id found in response", found);
	}
	
	public void testSearchResourceGroup() throws Exception {
		searchResourcegroup(webCon, "*", new Date(0), PROTOCOL + hostName, login, password);
	}
	
	public void testGetUserId() throws Exception {
		final int userId = getUserId(getWebConversation(), PROTOCOL + getHostName(), getLogin(), getPassword());
		assertTrue("user id for login user not found", userId != -1);
	}
	
	public void testGetContextId() throws Exception {
		final int contextId = getContextId(getWebConversation(), PROTOCOL + getHostName(), getLogin(), getPassword());
		assertTrue("context id for login user not found", contextId != -1);
	}
	
	public static ContactObject[] searchUser(WebConversation webCon, String searchpattern, Date modified, String host, String login, String password) throws Exception {
		host = appendPrefix(host);
		
		Element eUsers = new Element("user", XmlServlet.NS);
		eUsers.addContent(searchpattern);
		
		Document doc = addElement2PropFind(eUsers, modified);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		baos.flush();
		
		HttpClient httpclient = new HttpClient();
		
		httpclient.getState().setCredentials(null, new UsernamePasswordCredentials(login, password));
		PropFindMethod propFindMethod = new PropFindMethod(host + GROUPUSER_URL);
		propFindMethod.setDoAuthentication( true );
		
        LOG.debug("Request Body: " + new String(baos.toByteArray(), "UTF-8"));
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		propFindMethod.setRequestBody(bais);
		
		int status = httpclient.executeMethod(propFindMethod);
		
		byte responseByte[] = propFindMethod.getResponseBody();
		bais = new ByteArrayInputStream(responseByte);
		
		if (status != 207) {
			fail("Invalid response code '" + status + "'. Response code is not 207 as expected. Response data: " + new String(responseByte));
		}

		Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.GROUPUSER);
		
		ContactObject[] contactArray = new ContactObject[response.length];
		for (int a = 0; a < contactArray.length; a++) {
			if (response[a].hasError()) {
				fail("xml error: " + response[a].getErrorMessage());
			}
			
			contactArray[a] = (ContactObject)response[a].getDataObject();
		}
		
		return contactArray;
	}
	
	public static Group[] searchGroup(WebConversation webCon, String searchpattern, Date modified, String host, String login, String password) throws Exception {
		host = appendPrefix(host);
		
		Element eGroups = new Element("group", XmlServlet.NS);
		eGroups.addContent(searchpattern);
		
		Document doc = addElement2PropFind(eGroups, modified);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		baos.flush();
		
		HttpClient httpclient = new HttpClient();
		
		httpclient.getState().setCredentials(null, new UsernamePasswordCredentials(login, password));
		PropFindMethod propFindMethod = new PropFindMethod(host + GROUPUSER_URL);
		propFindMethod.setDoAuthentication( true );
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		propFindMethod.setRequestBody(bais);
		
		int status = httpclient.executeMethod(propFindMethod);
		
		assertEquals("check propfind response", 207, status);
		
		byte responseByte[] = propFindMethod.getResponseBody();
		bais = new ByteArrayInputStream(responseByte);
		Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.GROUPUSER);
		
		Group[] groupArray = new Group[response.length];
		for (int a = 0; a < groupArray.length; a++) {
			if (response[a].hasError()) {
				fail("xml error: " + response[a].getErrorMessage());
			}
			
			groupArray[a] = (Group)response[a].getDataObject();
		}
		
		return groupArray;
	}
	
	public static Resource[] searchResource(WebConversation webCon, String searchpattern, Date modified, String host, String login, String password) throws Exception {
		host = appendPrefix(host);
		
		Element eResources = new Element("resource", XmlServlet.NS);
		eResources.addContent(searchpattern);
		
		Document doc = addElement2PropFind(eResources, modified);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		baos.flush();
		
		HttpClient httpclient = new HttpClient();
		
		httpclient.getState().setCredentials(null, new UsernamePasswordCredentials(login, password));
		PropFindMethod propFindMethod = new PropFindMethod(host + GROUPUSER_URL);
		propFindMethod.setDoAuthentication( true );
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		propFindMethod.setRequestBody(bais);
		
		int status = httpclient.executeMethod(propFindMethod);
		
		assertEquals("check propfind response", 207, status);
		
		byte responseByte[] = propFindMethod.getResponseBody();
		bais = new ByteArrayInputStream(responseByte);
		Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.GROUPUSER);
		
		Resource[] resourceArray = new Resource[response.length];
		for (int a = 0; a < resourceArray.length; a++) {
			if (response[a].hasError()) {
				fail("xml error: " + response[a].getErrorMessage());
			}
			
			resourceArray[a] = (Resource)response[a].getDataObject();
		}
		
		return resourceArray;
	}
	
	public static ResourceGroup[] searchResourcegroup(WebConversation webCon, String searchpattern, Date modified, String host, String login, String password) throws Exception {
		host = appendPrefix(host);
		
		Element eResourceGroups = new Element("resourcegroup", XmlServlet.NS);
		eResourceGroups.addContent(searchpattern);
		
		Document doc = addElement2PropFind(eResourceGroups, modified);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);
		
		baos.flush();
		
		HttpClient httpclient = new HttpClient();
		
		httpclient.getState().setCredentials(null, new UsernamePasswordCredentials(login, password));
		PropFindMethod propFindMethod = new PropFindMethod(host + GROUPUSER_URL);
		propFindMethod.setDoAuthentication( true );
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		propFindMethod.setRequestBody(bais);
		
		int status = httpclient.executeMethod(propFindMethod);
		
		assertEquals("check propfind response", 207, status);
		
		byte responseByte[] = propFindMethod.getResponseBody();
		bais = new ByteArrayInputStream(responseByte);
		Response[] response = ResponseParser.parse(new SAXBuilder().build(bais), Types.GROUPUSER);
		
		ResourceGroup[] resourcegroupArray = new ResourceGroup[response.length];
		for (int a = 0; a < resourcegroupArray.length; a++) {
			if (response[a].hasError()) {
				fail("xml error: " + response[a].getErrorMessage());
			}
			
			resourcegroupArray[a] = (ResourceGroup)response[a].getDataObject();
		}
		
		return resourcegroupArray;
	}
	
	public static int getUserId(WebConversation webCon, String host, String login, String password) throws Exception {
		host = appendPrefix(host);
		
		ContactObject[] contactArray = searchUser(webCon, "*", new Date(0), host, login, password);
		for (int a = 0; a < contactArray.length; a++) {
			ContactObject contactObj = contactArray[a];
			Map m = contactObj.getMap();
			if (m != null && m.containsKey("myidentity")) {
				return contactObj.getInternalUserId();
			}
		}
		return -1;
	}
	
	public static int getContextId(WebConversation webCon, String host, String login, String password) throws Exception {
		host = appendPrefix(host);
		
		ContactObject[] contactArray = searchUser(webCon, "*", new Date(0), host, login, password);
		for (int a = 0; a < contactArray.length; a++) {
			ContactObject contactObj = contactArray[a];
			Map m = contactObj.getMap();
			if (m != null && m.containsKey("context_id")) {
				return Integer.parseInt(m.get("context_id").toString());
			}
		}
		return -1;
	}
	
	public static Document addElement2PropFind(Element e, Date modified) throws Exception {
		Element ePropfind = new Element("propfind", webdav);
		Element eProp = new Element("prop", webdav);
		
		Element eLastSync = new Element("lastsync", XmlServlet.NS);
		eLastSync.addContent(String.valueOf(modified.getTime()));
		
		ePropfind.addContent(eProp);
		eProp.addContent(eLastSync);
		eProp.addContent(e);
		
		return new Document(ePropfind);
	}
}

