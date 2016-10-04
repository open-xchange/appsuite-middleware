/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.dav;

import static org.junit.Assert.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.json.JSONException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runners.Parameterized.Parameter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.VisibleFoldersRequest;
import com.openexchange.ajax.folder.actions.VisibleFoldersResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.oauth.provider.AbstractOAuthTest;
import com.openexchange.ajax.oauth.provider.OAuthSession;
import com.openexchange.ajax.oauth.provider.protocol.Grant;
import com.openexchange.ajax.oauth.provider.protocol.OAuthParams;
import com.openexchange.ajax.oauth.provider.protocol.Protocol;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.dav.reports.SyncCollectionReportInfo;
import com.openexchange.dav.reports.SyncCollectionResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.util.UUIDs;
import com.openexchange.oauth.provider.rmi.client.ClientDto;

/**
 * {@link WebDAVTest} - Common base class for WebDAV tests
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public abstract class WebDAVTest {

    private static final boolean AUTODISCOVER_AUTH = false;

    protected static final int TIMEOUT = 10000;

    private List<FolderObject> foldersToCleanUp;

    private Map<Long, WebDAVClient> webDAVClients;

    protected AJAXClient client;

    @BeforeClass
    public static void prepareFramework() throws OXException {
        AJAXConfig.init();
    }


    // --- BEGIN: Optional OAuth Configuration ------------------------------------------------------------------------------

    protected static final String AUTH_METHOD_BASIC = "Basic Auth";

    protected static final String AUTH_METHOD_OAUTH = "OAuth";

    protected static ClientDto oAuthClientApp;

    protected static Grant oAuthGrant;

    @Parameter(value = 0)
    public String authMethod;

    protected static Iterable<Object[]> availableAuthMethods() {
        if (false == AUTODISCOVER_AUTH) {
            List<Object[]> authMethods = new ArrayList<>(2);
            authMethods.add(new Object[] { AUTH_METHOD_BASIC });
            authMethods.add(new Object[] { AUTH_METHOD_OAUTH });
            return authMethods;
        }
        List<Object[]> authMethods = new ArrayList<Object[]>(2);
        PropFindMethod propFind = null;
        try {
            AJAXConfig.init();
            DavPropertyNameSet props = new DavPropertyNameSet();
            props.add(PropertyNames.CURRENT_USER_PRINCIPAL);
            propFind = new PropFindMethod(Config.getBaseUri() + '/', DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_0);
            if (HttpServletResponse.SC_UNAUTHORIZED == new HttpClient().executeMethod(propFind)) {
                for (Header header : propFind.getResponseHeaders("WWW-Authenticate")) {
                    if (header.getValue().startsWith("Bearer")) {
                        authMethods.add(new Object[] { AUTH_METHOD_OAUTH });
                    } else if (header.getValue().startsWith("Basic")) {
                        authMethods.add(new Object[] { AUTH_METHOD_BASIC });
                    }
                }
            }
        } catch (OXException | IOException e) {
            fail(e.getMessage());
        } finally {
            release(propFind);
        }
        return authMethods;
    }

    @Before
    public void prepareOAuthClient() throws Exception {
        /*
         * Lazy initialization - static (BeforeClass) is not possible because the testOAuth()
         * depends on the configuration of the concrete subclass (via parameterized testing).
         *
         */
        if (testOAuth() && oAuthClientApp == null && oAuthGrant == null) {
            oAuthClientApp = AbstractOAuthTest.registerTestClient();
            DefaultHttpClient client = OAuthSession.newOAuthHttpClient();
            String state = UUIDs.getUnformattedStringFromRandom();
            OAuthParams params = new OAuthParams()
                .setHostname(Config.getHostname())
                .setClientId(oAuthClientApp.getId())
                .setClientSecret(oAuthClientApp.getSecret())
                .setRedirectURI(oAuthClientApp.getRedirectURIs().get(0))
                .setScope("carddav caldav")
                .setState(state);
            oAuthGrant = Protocol.obtainAccess(client, params, Config.getLogin(), Config.getPassword());
        }
    }

    protected boolean testOAuth() {
        return AUTH_METHOD_OAUTH.equals(authMethod);
    }

    @AfterClass
    public static void unregisterOAuthClient() throws Exception {
        if (oAuthClientApp != null) {
            try {
                AbstractOAuthTest.unregisterTestClient(oAuthClientApp);
            } catch (Exception e) {
                e.printStackTrace();
            }
            oAuthClientApp = null;
            oAuthGrant = null;
        }
    }

    // --- END: Optional OAuth Configuration --------------------------------------------------------------------------------

    @Before
    public void before() throws Exception {
        client = new AJAXClient(User.User1);

        this.webDAVClients = new HashMap<Long, WebDAVClient>();
        getAJAXClient().setHostname(getHostname());
        getAJAXClient().setProtocol(getProtocol());
        this.foldersToCleanUp = new ArrayList<FolderObject>();
    }

    @After
    public void after() throws Exception {
        if (null != client) {
            cleanupFolders();
            client.logout();
            client = null;
        }
    }

    public AJAXSession getSession() {
        return client.getSession();
    }

    protected AJAXClient getClient() {
        return client;
    }

    protected abstract String getDefaultUserAgent();

    protected WebDAVClient getWebDAVClient() throws Exception {
        Long threadID = Long.valueOf(Thread.currentThread().getId());
        if (false == this.webDAVClients.containsKey(threadID)) {
            WebDAVClient webDAVClient = new WebDAVClient(getDefaultUserAgent(), oAuthGrant);
            this.webDAVClients.put(threadID, webDAVClient);
            return webDAVClient;
        } else {
            return this.webDAVClients.get(threadID);
        }
    }

    private void cleanupFolders() {
        if (null != this.foldersToCleanUp) {
            for (FolderObject folder : foldersToCleanUp) {
                try {
                    getClient().execute(new com.openexchange.ajax.folder.actions.DeleteRequest(EnumAPI.OX_NEW, folder.getObjectID(), new Date()));
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
    }

    /**
     * Remembers the supplied folder for deletion after the test is finished
     * in the <code>tearDown()</code> method.
     *
     * @param folder
     */
    protected void rememberForCleanUp(FolderObject folder) {
        this.foldersToCleanUp.add(folder);
    }

    /**
     * Gets a folder by its name.
     *
     * @param folderName
     * @return
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    protected FolderObject getFolder(String folderName) throws OXException, IOException, JSONException {
        VisibleFoldersResponse response = client.execute(
            new VisibleFoldersRequest(EnumAPI.OX_NEW, "contacts", new int[] { FolderObject.OBJECT_ID, FolderObject.FOLDER_NAME }));
        FolderObject folder = findByName(response.getPrivateFolders(), folderName);
        if (null == folder) {
            folder = findByName(response.getPublicFolders(), folderName);
            if (null == folder) {
                folder = findByName(response.getSharedFolders(), folderName);
            }
        }
        if (null != folder) {
            folder = client.execute(new com.openexchange.ajax.folder.actions.GetRequest(EnumAPI.OX_NEW, folder.getObjectID())).getFolder();
        }
        return folder;
    }

    /**
     * Gets a folder by its name.
     *
     * @param folderName
     * @return
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    protected FolderObject getCalendarFolder(String folderName) throws OXException, IOException, JSONException {
        VisibleFoldersResponse response =
            client.execute(new VisibleFoldersRequest(EnumAPI.OX_NEW, "calendar", new int[] { FolderObject.OBJECT_ID, FolderObject.FOLDER_NAME }));
        FolderObject folder = findByName(response.getPrivateFolders(), folderName);
        if (null == folder) {
            folder = findByName(response.getPublicFolders(), folderName);
            if (null == folder) {
                folder = findByName(response.getSharedFolders(), folderName);
            }
        }
        if (null != folder) {
            folder = client.execute(new com.openexchange.ajax.folder.actions.GetRequest(EnumAPI.OX_NEW, folder.getObjectID())).getFolder();
        }
        return folder;
    }

    private static FolderObject findByName(Iterator<FolderObject> iter, String folderName) {
        while (iter.hasNext()) {
            FolderObject folder = iter.next();
            if (folderName.equals(folder.getFolderName())) {
                return folder;
            }
        }
        return null;
    }

    protected boolean removeFromETags(Map<String, String> eTags, String uid) {
        String href = this.getHrefFromETags(eTags, uid);
        if (null != href) {
            eTags.remove(href);
            return true;
        } else {
            return false;
        }
    }

    protected String getHrefFromETags(Map<String, String> eTags, String uid) {
        for (String href : eTags.keySet()) {
            if (href.contains(uid)) {
                return href;
            }
        }
        return null;
    }

    protected List<String> getChangedHrefs(Map<String, String> previousETags, Map<String, String> newETags) {
        List<String> hrefs = new ArrayList<String>();
        for (String href : newETags.keySet()) {
            if (false == previousETags.containsKey(href) || false == newETags.get(href).equals(newETags.get(href))) {
                hrefs.add(href);
            }
        }
        return hrefs;
    }

    protected FolderObject createFolder(FolderObject folder) throws OXException, IOException, JSONException {
        InsertResponse response = getClient().execute(new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_NEW, folder));
        folder.setObjectID(response.getId());
        folder.setLastModified(response.getTimestamp());
        this.rememberForCleanUp(folder);
        return folder;
    }

    protected FolderObject updateFolder(FolderObject folder) throws OXException, IOException, JSONException {
        InsertResponse response = getClient().execute(new com.openexchange.ajax.folder.actions.UpdateRequest(EnumAPI.OX_NEW, folder));
        folder.setLastModified(response.getTimestamp());
        return folder;
    }

    protected FolderObject getFolder(int folderID) throws OXException, IOException, JSONException {
        return getClient().execute(new com.openexchange.ajax.folder.actions.GetRequest(EnumAPI.OX_NEW, folderID)).getFolder();
    }

    protected void deleteFolder(FolderObject folder) throws OXException, IOException, JSONException {
        getClient().execute(new com.openexchange.ajax.folder.actions.DeleteRequest(EnumAPI.OX_NEW, true, folder));
    }

    protected FolderObject createFolder(FolderObject parent, String folderName) throws OXException, IOException, JSONException {
        FolderObject folder = new FolderObject();
        folder.setFolderName(folderName);
        folder.setParentFolderID(parent.getObjectID());
        folder.setModule(parent.getModule());
        folder.setType(parent.getType());
        folder.setPermissions(parent.getPermissions());
        return this.createFolder(folder);
    }

    protected static String getBaseUri() throws OXException {
        return getProtocol() + "://" + getHostname();
    }

    protected static User getUser() {
        return User.User1;
    }

    protected static String getLogin() throws OXException {
        return getLogin(getUser());
    }

    protected static String getUsername() throws OXException {
        return getUsername(getUser());
    }

    protected static String getPassword() throws OXException {
        return getPassword(getUser());
    }

    protected static String getHostname() throws OXException {
        final String hostname = AJAXConfig.getProperty(Property.HOSTNAME);
        if (null == hostname) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(Property.HOSTNAME.getPropertyName());
        }
        return hostname;
    }

    protected static String getProtocol() throws OXException {
        final String hostname = AJAXConfig.getProperty(Property.PROTOCOL);
        if (null == hostname) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(Property.PROTOCOL.getPropertyName());
        }
        return hostname;
    }

    protected static String getLogin(final User user) throws OXException {
        final String login = AJAXConfig.getProperty(user.getLogin());
        if (null == login) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(user.getLogin().getPropertyName());
        } else if (login.contains("@")) {
            return login;
        } else {
            final String context = AJAXConfig.getProperty(Property.CONTEXTNAME);
            if (null == context) {
                throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(Property.CONTEXTNAME.getPropertyName());
            }
            return login + "@" + context;
        }
    }

    protected static String getUsername(final User user) throws OXException {
        final String username = AJAXConfig.getProperty(user.getLogin());
        if (null == username) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(user.getLogin().getPropertyName());
        } else {
            return username.contains("@") ? username.substring(0, username.indexOf("@")) : username;
        }
    }

    protected static String getPassword(final User user) throws OXException {
        final String password = AJAXConfig.getProperty(user.getPassword());
        if (null == password) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(user.getPassword().getPropertyName());
        }
        return password;
    }

    protected static void release(HttpMethodBase method) {
        if (null != method) {
            method.releaseConnection();
        }
    }

    protected static String randomUID() {
        return UUID.randomUUID().toString();
    }

    protected AJAXClient getAJAXClient() {
        return getClient();
    }

    protected String fetchSyncToken(String relativeUrl) throws Exception {
        PropFindMethod propFind = null;
        try {
            DavPropertyNameSet props = new DavPropertyNameSet();
            props.add(PropertyNames.SYNC_TOKEN);
            propFind = new PropFindMethod(getBaseUri() + relativeUrl, DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_0);
            MultiStatusResponse response = assertSingleResponse(this.getWebDAVClient().doPropFind(propFind, StatusCodes.SC_MULTISTATUS));
            return this.extractTextContent(PropertyNames.SYNC_TOKEN, response);
        } finally {
            release(propFind);
        }
    }

    /**
     * Performs a REPORT method at the specified URL with a Depth of 1,
     * requesting the ETag property of all resources that were changed since
     * the supplied sync token.
     *
     * @param syncToken
     * @return
     * @throws IOException
     * @throws ConfigurationException
     * @throws DavException
     */
    protected Map<String, String> syncCollection(String syncToken, String relativeUrl) throws Exception {
        Map<String, String> eTags = new HashMap<String, String>();
        DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.GETETAG);
        ReportInfo reportInfo = new SyncCollectionReportInfo(syncToken, props);
        MultiStatusResponse[] responses = this.getWebDAVClient().doReport(reportInfo, getBaseUri() + relativeUrl);
        for (final MultiStatusResponse response : responses) {
            if (response.getProperties(StatusCodes.SC_OK).contains(PropertyNames.GETETAG)) {
                String href = response.getHref();
                assertNotNull("got no href from response", href);
                String eTag = this.extractTextContent(PropertyNames.GETETAG, response);
                assertNotNull("got no ETag from response", eTag);
                eTags.put(href, eTag);
            }
        }
        return eTags;
    }

    protected SyncCollectionResponse syncCollection(SyncToken syncToken, String relativeUrl) throws Exception {
        DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.GETETAG);
        SyncCollectionReportInfo reportInfo = new SyncCollectionReportInfo(syncToken.getToken(), props);
        SyncCollectionResponse syncCollectionResponse = this.getWebDAVClient().doReport(reportInfo, getBaseUri() + relativeUrl);
        syncToken.setToken(syncCollectionResponse.getSyncToken());
        return syncCollectionResponse;
    }

    protected String extractHref(DavPropertyName propertyName, MultiStatusResponse response) {
        Node node = this.extractNodeValue(propertyName, response);
        assertMatches(PropertyNames.HREF, node);
        String content = node.getTextContent();
        assertNotNull("no text content in " + PropertyNames.HREF + " child for " + propertyName, content);
        return content;
    }

    protected Node extractNodeValue(final DavPropertyName propertyName, final MultiStatusResponse response) {
        assertNotEmpty(propertyName, response);
        final Object value = response.getProperties(StatusCodes.SC_OK).get(propertyName).getValue();
        assertTrue("value is not a node in " + propertyName, value instanceof Node);
        return (Node) value;
    }

    protected List<Node> extractNodeListValue(DavPropertyName propertyName, MultiStatusResponse response) {
        assertNotEmpty(propertyName, response);
        final Object value = response.getProperties(StatusCodes.SC_OK).get(propertyName).getValue();
        assertTrue("value is not a node list in " + propertyName, value instanceof List<?>);
        return (List<Node>) value;
    }

    protected DavProperty<?> extractProperty(DavPropertyName propertyName, MultiStatusResponse response) {
        assertNotEmpty(propertyName, response);
        DavProperty<?> property = response.getProperties(StatusCodes.SC_OK).get(propertyName);
        assertNotNull("property " + propertyName + " not found", property);
        return property;
    }

    protected String extractTextContent(final DavPropertyName propertyName, final MultiStatusResponse response) {
        assertNotEmpty(propertyName, response);
        final Object value = response.getProperties(StatusCodes.SC_OK).get(propertyName).getValue();
        assertTrue("value is not a string in " + propertyName, value instanceof String);
        return (String) value;
    }

    protected static String extractChildTextContent(DavPropertyName propertyName, Element element) {
        NodeList nodes = element.getElementsByTagNameNS(propertyName.getNamespace().getURI(), propertyName.getName());
        assertNotNull("no child elements found by property name", nodes);
        assertEquals("0 or more than one child nodes found for property", 1, nodes.getLength());
        Node node = nodes.item(0);
        assertNotNull("no child element found by property name", node);
        return node.getTextContent();
    }

    protected static String formatAsUTC(final Date date) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }

    protected static String formatAsDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }

    /*
     * Additional assertXXX methods
     */

    public static void assertMatches(final DavPropertyName propertyName, final Node node) {
        assertEquals("wrong element name", propertyName.getName(), node.getLocalName());
        assertEquals("wrong element namespace", propertyName.getNamespace().getURI(), node.getNamespaceURI());
    }

    public static void assertContains(DavPropertyName propertyName, List<Node> nodeList) {
        for (Node node : nodeList) {
            if (propertyName.getName().equals(node.getLocalName()) && propertyName.getNamespace().getURI().equals(node.getNamespaceURI())) {
                return;
            }
        }
        fail("property " + propertyName + " not found in list");
    }

    public static void assertIsPresent(DavPropertyName propertyName, MultiStatusResponse response) {
        final DavProperty<?> property = response.getProperties(StatusCodes.SC_OK).get(propertyName);
        assertNotNull("property " + propertyName + " not found", property);
    }

    public static void assertNotEmpty(DavPropertyName propertyName, MultiStatusResponse response) {
        assertIsPresent(propertyName, response);
        final Object value = response.getProperties(StatusCodes.SC_OK).get(propertyName).getValue();
        assertNotNull("no value for " + propertyName, value);
    }

    public static MultiStatusResponse assertSingleResponse(MultiStatusResponse[] responses) {
        assertNotNull("got no multistatus responses", responses);
        assertTrue("got zero multistatus responses", 0 < responses.length);
        assertTrue("got more than one multistatus responses", 1 == responses.length);
        final MultiStatusResponse response = responses[0];
        assertNotNull("no multistatus response", response);
        return response;
    }

    public static void assertResponseHeaders(String[] expected, String headerName, HttpMethod method) {
        for (String expectedHeader : expected) {
            boolean found = false;
            Header[] actualHeaders = method.getResponseHeaders(headerName);
            assertTrue("header '" + headerName + "' not found", null != actualHeaders && 0 < actualHeaders.length);
            for (Header actualHeader : actualHeaders) {
                HeaderElement[] actualHeaderElements = actualHeader.getElements();
                assertTrue("no elements found in header '" + headerName + "'", null != actualHeaderElements && 0 < actualHeaderElements.length);
                for (HeaderElement actualHeaderElement : actualHeaderElements) {
                    if (expectedHeader.equals(actualHeaderElement.getName())) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    break;
                }
            }
            assertTrue("header element '" + expectedHeader + "'not found in header '" + headerName + "'", found);
        }
    }

}
