
package com.openexchange.webdav.xml;

import static com.openexchange.webdav.xml.framework.RequestTools.addElement2PropFind;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.junit.Test;
import com.meterware.httpunit.WebConversation;
import com.openexchange.group.Group;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.Contact;
import com.openexchange.resource.Resource;
import com.openexchange.resource.ResourceGroup;
import com.openexchange.webdav.xml.parser.ResponseParser;
import com.openexchange.webdav.xml.request.PropFindMethod;
import com.openexchange.webdav.xml.types.Response;

public class GroupUserTest extends AbstractWebdavXMLTest {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GroupUserTest.class);

    public static final String GROUPUSER_URL = "/servlet/webdav.groupuser";

    @Test
    public void testSearchUser() throws Exception {
        final Contact[] contactObj = searchUser(webCon, "*", new Date(0), getHostURI(), login, password);
        for (int a = 0; a < contactObj.length; a++) {
            assertTrue("id > 0 expected", contactObj[a].getInternalUserId() > 0);
            assertNotNull("last modified is null", contactObj[a].getLastModified());
        }
    }

    @Test
    public void testSearchGroup() throws Exception {
        final Group group[] = searchGroup(webCon, "*", new Date(0), getHostURI(), login, password);
        for (int a = 0; a < group.length; a++) {
            assertNotNull("last modified is null", group[a].getLastModified());
        }
    }

    @Test
    public void testSearchResource() throws Exception {
        final Resource[] resource = searchResource(getWebConversation(), "*", new Date(0), getHostURI(), login, password);
        for (int a = 0; a < resource.length; a++) {
            assertTrue("id > 0 expected", resource[a].getIdentifier() > 0);
            assertNotNull("last modified is null", resource[a].getLastModified());
        }
    }

    @Test
    public void testSearchGroupWithLastModifed() throws Exception {
        Group group[] = searchGroup(getWebConversation(), "*", new Date(0), getHostURI(), login, password);
        assertTrue("no group found in response (group array length == 0)", group.length > 0);

        int posInArray = -1;
        for (int a = 0; a < group.length; a++) {
            if (GroupStorage.GROUP_ZERO_IDENTIFIER != group[a].getIdentifier() && GroupStorage.GUEST_GROUP_IDENTIFIER != group[a].getIdentifier()) {
                posInArray = a;
                break;
            }
        }
        assertFalse("No according group found.", -1 == posInArray);
        final int id = group[posInArray].getIdentifier();
        final String displayName = group[posInArray].getDisplayName();
        final Date lastModifed = group[posInArray].getLastModified();

        group = searchGroup(getWebConversation(), displayName, new Date(lastModifed.getTime() + 5000), getHostURI(), getLogin(), getPassword());

        boolean found = false;

        for (int a = 0; a < group.length; a++) {
            if (group[a].getIdentifier() == id) {
                found = true;
                break;
            }
        }

        assertFalse("unexpected id found in response", found);
    }

    @Test
    public void testSearchResourceWithLastModifed() throws Exception {
        Resource resource[] = searchResource(getWebConversation(), "*", new Date(0), getHostURI(), getLogin(), getPassword());
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

        resource = searchResource(getWebConversation(), displayName, new Date(lastModifed.getTime() + 5000), getHostURI(), getLogin(), getPassword());

        boolean found = false;

        for (int a = 0; a < resource.length; a++) {
            if (resource[a].getIdentifier() == id) {
                found = true;
                break;
            }
        }

        assertFalse("unexpected id found in response", found);
    }

    @Test
    public void testSearchResourceGroup() throws Exception {
        searchResourcegroup(webCon, "*", new Date(0), getHostURI(), getLogin(), getPassword());
    }

    @Test
    public void testGetUserId() throws Exception {
        final int userId = getUserId(getWebConversation(), getHostURI(), getLogin(), getPassword());
        assertTrue("user id for login user not found", userId != -1);
    }

    @Test
    public void testGetContextId() throws Exception {
        final int contextId = getContextId(getWebConversation(), getHostURI(), getLogin(), getPassword());
        assertTrue("context id for login user not found", contextId != -1);
    }

    public static Contact[] searchUser(final WebConversation webCon, final String searchpattern, final Date modified, String host, final String login, final String password) throws Exception {
        final Element eUsers = new Element("user", XmlServlet.NS);
        eUsers.addContent(searchpattern);

        final Document doc = addElement2PropFind(eUsers, modified);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final XMLOutputter xo = new XMLOutputter();
        xo.output(doc, baos);

        baos.flush();

        final HttpClient httpclient = new HttpClient();

        httpclient.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(login, password));
        final PropFindMethod propFindMethod = new PropFindMethod(host + GROUPUSER_URL);
        propFindMethod.setDoAuthentication(true);

        LOG.debug("Request Body: {}", new String(baos.toByteArray(), com.openexchange.java.Charsets.UTF_8));
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

            contactArray[a] = (Contact) response[a].getDataObject();
        }

        return contactArray;
    }

    public static Group[] searchGroup(final WebConversation webCon, final String searchpattern, final Date modified, String host, final String login, final String password) throws Exception {
        final Element eGroups = new Element("group", XmlServlet.NS);
        eGroups.addContent(searchpattern);

        final Document doc = addElement2PropFind(eGroups, modified);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final XMLOutputter xo = new XMLOutputter();
        xo.output(doc, baos);

        baos.flush();

        final HttpClient httpclient = new HttpClient();

        httpclient.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(login, password));
        final PropFindMethod propFindMethod = new PropFindMethod(host + GROUPUSER_URL);
        propFindMethod.setDoAuthentication(true);

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

            groupArray[a] = (Group) response[a].getDataObject();
        }

        return groupArray;
    }

    public static Resource[] searchResource(final WebConversation webCon, final String searchpattern, final Date modified, String host, final String login, final String password) throws Exception {
        final Element eResources = new Element("resource", XmlServlet.NS);
        eResources.addContent(searchpattern);

        final Document doc = addElement2PropFind(eResources, modified);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final XMLOutputter xo = new XMLOutputter();
        xo.output(doc, baos);

        baos.flush();

        final HttpClient httpclient = new HttpClient();

        httpclient.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(login, password));
        final PropFindMethod propFindMethod = new PropFindMethod(host + GROUPUSER_URL);
        propFindMethod.setDoAuthentication(true);

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

            resourceArray[a] = (Resource) response[a].getDataObject();
        }

        return resourceArray;
    }

    public static ResourceGroup[] searchResourcegroup(WebConversation webCon, String searchpattern, Date modified, String host, String login, String password) throws Exception {
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
        propFindMethod.setDoAuthentication(true);

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

            resourcegroupArray[a] = (ResourceGroup) response[a].getDataObject();
        }

        return resourcegroupArray;
    }

    public static int getUserId(final WebConversation webCon, String host, final String login, final String password) throws Exception {
        final Contact[] contactArray = searchUser(webCon, "*", new Date(0), host, login, password);
        for (int a = 0; a < contactArray.length; a++) {
            final Contact contactObj = contactArray[a];
            final Map m = contactObj.getMap();
            if (m != null && m.containsKey("myidentity")) {
                return contactObj.getInternalUserId();
            }
        }
        return -1;
    }

    public static int getContextId(final WebConversation webCon, String host, final String login, final String password) throws Exception {
        final Contact[] contactArray = searchUser(webCon, "*", new Date(0), host, login, password);
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
