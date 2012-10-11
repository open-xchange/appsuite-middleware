package com.openexchange.webdav.xml;

import static com.openexchange.webdav.xml.framework.RequestTools.addElement2PropFind;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import com.meterware.httpunit.WebConversation;
import com.openexchange.group.Group;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.Contact;
import com.openexchange.resource.Resource;
import com.openexchange.resource.ResourceGroup;
import com.openexchange.webdav.xml.parser.ResponseParser;
import com.openexchange.webdav.xml.request.PropFindMethod;
import com.openexchange.webdav.xml.types.Response;

public class GroupUserTest extends AbstractWebdavXMLTest {

    private static final Log LOG = LogFactory.getLog(GroupUserTest.class);

	public static final String GROUPUSER_URL = "/servlet/webdav.groupuser";

	public GroupUserTest(final String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testSearchUser() throws Exception {
		final Contact[] contactObj = searchUser(webCon, "*", new Date(0), PROTOCOL + hostName, login, password, context);
		for (int a = 0; a < contactObj.length; a++) {
			assertTrue("id > 0 expected", contactObj[a].getInternalUserId() > 0);
			assertNotNull("last modified is null", contactObj[a].getLastModified());
		}
	}

	public void testSearchGroup() throws Exception {
		final Group group[] = searchGroup(webCon, "*", new Date(0), PROTOCOL + hostName, login, password, context);
		for (int a = 0; a < group.length; a++) {
			assertNotNull("last modified is null", group[a].getLastModified());
		}
	}

	public void testSearchResource() throws Exception {
		final Resource[] resource = searchResource(getWebConversation(), "*", new Date(0), PROTOCOL + hostName, login, password, context);
		for (int a = 0; a < resource.length; a++) {
			assertTrue("id > 0 expected", resource[a].getIdentifier() > 0);
			assertNotNull("last modified is null", resource[a].getLastModified());
		}
	}

	public void testSearchGroupWithLastModifed() throws Exception {
		Group group[] = searchGroup(getWebConversation(), "*", new Date(0), PROTOCOL + hostName, login, password, context);
		assertTrue("no group found in response (group array length == 0)", group.length > 0);

		int posInArray = -1;
		for (int a = 0; a < group.length; a++) {
			if (group[a].getIdentifier() > 0) {
			    posInArray = a;
				break;
			}
		}
		assertFalse("No according group found.", -1 == posInArray);
		final int id = group[posInArray].getIdentifier();
		final String displayName = group[posInArray].getDisplayName();
		final Date lastModifed = group[posInArray].getLastModified();

		group = searchGroup(getWebConversation(), displayName, new Date(lastModifed.getTime()+5000), PROTOCOL + getHostName(), getLogin(), getPassword(), context);

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
		Resource resource[] = searchResource(getWebConversation(), "*", new Date(0), PROTOCOL + getHostName(), getLogin(), getPassword(), context);
		assertTrue("no group found in response (group array length == 0)", resource.length > 0);

		int posInArray = 0;

		for (int a = 0; a < resource.length; a++) {
			if (resource[a].getIdentifier() != 1) {
				posInArray = a;
				break;
			}
		}

		final int id = resource[posInArray].getIdentifier();
		final String displayName = resource[posInArray].getDisplayName();
		final Date lastModifed = resource[posInArray].getLastModified();


		resource = searchResource(getWebConversation(), displayName, new Date(lastModifed.getTime()+5000), PROTOCOL + getHostName(), getLogin(), getPassword(), context);

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
		final int userId = getUserId(getWebConversation(), PROTOCOL + getHostName(), getLogin(), getPassword(), context);
		assertTrue("user id for login user not found", userId != -1);
	}

	public void testGetContextId() throws Exception {
		final int contextId = getContextId(getWebConversation(), PROTOCOL + getHostName(), getLogin(), getPassword(), context);
		assertTrue("context id for login user not found", contextId != -1);
	}

	public static Contact[] searchUser(final WebConversation webCon, final String searchpattern, final Date modified, String host, final String login, final String password, String context) throws Exception {
		host = AbstractWebdavXMLTest.appendPrefix(host);

		final Element eUsers = new Element("user", XmlServlet.NS);
		eUsers.addContent(searchpattern);

		final Document doc = addElement2PropFind(eUsers, modified);

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();

		final XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);

		baos.flush();

		final HttpClient httpclient = new HttpClient();

		httpclient.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(login+"@"+context, password));
		final PropFindMethod propFindMethod = new PropFindMethod(host + GROUPUSER_URL);
		propFindMethod.setDoAuthentication( true );

        LOG.debug("Request Body: " + new String(baos.toByteArray(), com.openexchange.java.Charsets.UTF_8));
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		propFindMethod.setRequestBody(bais);

		final int status = httpclient.executeMethod(propFindMethod);

        InputStream body = propFindMethod.getResponseBodyAsStream();

		if (status != 207) {
			fail("Invalid response code '" + status + "'. Response code is not 207 as expected.");
		}

		final Response[] response = ResponseParser.parse(new SAXBuilder().build(body), Types.GROUPUSER);

		final Contact[] contactArray = new Contact[response.length];
		for (int a = 0; a < contactArray.length; a++) {
			if (response[a].hasError()) {
				fail("xml error: " + response[a].getErrorMessage());
			}

			contactArray[a] = (Contact)response[a].getDataObject();
		}

		return contactArray;
	}

	public static Group[] searchGroup(final WebConversation webCon, final String searchpattern, final Date modified, String host, final String login, final String password, String context) throws Exception {
		host = AbstractWebdavXMLTest.appendPrefix(host);

		final Element eGroups = new Element("group", XmlServlet.NS);
		eGroups.addContent(searchpattern);

		final Document doc = addElement2PropFind(eGroups, modified);

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();

		final XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);

		baos.flush();

		final HttpClient httpclient = new HttpClient();

		httpclient.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(login+"@"+context, password));
		final PropFindMethod propFindMethod = new PropFindMethod(host + GROUPUSER_URL);
		propFindMethod.setDoAuthentication( true );

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		propFindMethod.setRequestBody(bais);

		final int status = httpclient.executeMethod(propFindMethod);

		assertEquals("check propfind response", 207, status);

		InputStream body = propFindMethod.getResponseBodyAsStream();
		final Response[] response = ResponseParser.parse(new SAXBuilder().build(body), Types.GROUPUSER);

		final Group[] groupArray = new Group[response.length];
		for (int a = 0; a < groupArray.length; a++) {
			if (response[a].hasError()) {
				fail("xml error: " + response[a].getErrorMessage());
			}

			groupArray[a] = (Group)response[a].getDataObject();
		}

		return groupArray;
	}

	public static Resource[] searchResource(final WebConversation webCon, final String searchpattern, final Date modified, String host, final String login, final String password, String context) throws Exception {
		host = AbstractWebdavXMLTest.appendPrefix(host);

		final Element eResources = new Element("resource", XmlServlet.NS);
		eResources.addContent(searchpattern);

		final Document doc = addElement2PropFind(eResources, modified);

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();

		final XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);

		baos.flush();

		final HttpClient httpclient = new HttpClient();

		httpclient.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(login+"@"+context, password));
		final PropFindMethod propFindMethod = new PropFindMethod(host + GROUPUSER_URL);
		propFindMethod.setDoAuthentication( true );

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		propFindMethod.setRequestBody(bais);

		final int status = httpclient.executeMethod(propFindMethod);

		assertEquals("check propfind response", 207, status);

		InputStream body = propFindMethod.getResponseBodyAsStream();
		final Response[] response = ResponseParser.parse(new SAXBuilder().build(body), Types.GROUPUSER);

		final Resource[] resourceArray = new Resource[response.length];
		for (int a = 0; a < resourceArray.length; a++) {
			if (response[a].hasError()) {
				fail("xml error: " + response[a].getErrorMessage());
			}

			resourceArray[a] = (Resource)response[a].getDataObject();
		}

		return resourceArray;
	}

	public static ResourceGroup[] searchResourcegroup(final WebConversation webCon, final String searchpattern, final Date modified, String host, final String login, final String password) throws Exception {
		host = AbstractWebdavXMLTest.appendPrefix(host);

		final Element eResourceGroups = new Element("resourcegroup", XmlServlet.NS);
		eResourceGroups.addContent(searchpattern);

		final Document doc = addElement2PropFind(eResourceGroups, modified);

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();

		final XMLOutputter xo = new XMLOutputter();
		xo.output(doc, baos);

		baos.flush();

		final HttpClient httpclient = new HttpClient();

		httpclient.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(login, password));
		final PropFindMethod propFindMethod = new PropFindMethod(host + GROUPUSER_URL);
		propFindMethod.setDoAuthentication( true );

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		propFindMethod.setRequestBody(bais);

		final int status = httpclient.executeMethod(propFindMethod);

		assertEquals("check propfind response", 207, status);

        InputStream body = propFindMethod.getResponseBodyAsStream();
		final Response[] response = ResponseParser.parse(new SAXBuilder().build(body), Types.GROUPUSER);

		final ResourceGroup[] resourcegroupArray = new ResourceGroup[response.length];
		for (int a = 0; a < resourcegroupArray.length; a++) {
			if (response[a].hasError()) {
				fail("xml error: " + response[a].getErrorMessage());
			}

			resourcegroupArray[a] = (ResourceGroup)response[a].getDataObject();
		}

		return resourcegroupArray;
	}

	public static int getUserId(final WebConversation webCon, String host, final String login, final String password, String context) throws Exception {
		host = AbstractWebdavXMLTest.appendPrefix(host);

		final Contact[] contactArray = searchUser(webCon, "*", new Date(0), host, login, password, context);
		for (int a = 0; a < contactArray.length; a++) {
			final Contact contactObj = contactArray[a];
			final Map m = contactObj.getMap();
			if (m != null && m.containsKey("myidentity")) {
				return contactObj.getInternalUserId();
			}
		}
		return -1;
	}

	public static int getContextId(final WebConversation webCon, String host, final String login, final String password, String context) throws Exception {
		host = AbstractWebdavXMLTest.appendPrefix(host);

		final Contact[] contactArray = searchUser(webCon, "*", new Date(0), host, login, password, context);
		for (int a = 0; a < contactArray.length; a++) {
			final Contact contactObj = contactArray[a];
			final Map m = contactObj.getMap();
			if (m != null && m.containsKey("context_id")) {
				return Integer.parseInt(m.get("context_id").toString());
			}
		}
		return -1;
	}
}

