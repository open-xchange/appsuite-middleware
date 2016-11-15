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

package com.openexchange.ajax.drive.updater;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import com.openexchange.ajax.config.actions.SetRequest;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.java.Strings;
import org.apache.commons.codec.binary.Hex;

/**
 * {@link UpdaterXMLTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class UpdaterXMLTest extends AbstractAJAXSession {

    private AJAXClient client;

    private String userName;

    private String password;

    private String hostname;

    private String context;

    private String login;

    private HttpHost targetHost;

    /**
     * Initializes a new {@link UpdaterXMLTest}.
     *
     * @param name
     */
    public UpdaterXMLTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        User user = User.User1;
        userName = AJAXConfig.getProperty(user.getLogin());
        context = AJAXConfig.getProperty(Property.CONTEXTNAME);
        login = userName + '@' + context;
        password = AJAXConfig.getProperty(user.getPassword());
        hostname = AJAXConfig.getProperty(Property.HOSTNAME);
        targetHost = new HttpHost(hostname, 80);
        this.client = new AJAXClient(user);
    }

    public void testBasicAuth() throws Exception {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        try {

            HttpGet getXML = new HttpGet("/ajax/drive/client/windows/v1/update.xml");
            HttpResponse response = httpClient.execute(targetHost, getXML);
            HttpEntity entity = response.getEntity();
            EntityUtils.consume(entity);
            assertEquals(401, response.getStatusLine().getStatusCode());

            httpClient.getCredentialsProvider().setCredentials(
                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                new UsernamePasswordCredentials(login, password));
            response = httpClient.execute(targetHost, getXML);
            entity = response.getEntity();
            EntityUtils.consume(entity);
            assertTrue("Expected 200.", response.getStatusLine().getStatusCode() == 200);

            List<Cookie> cookies = httpClient.getCookieStore().getCookies();
            boolean foundCookie = false;
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("sessionid")) {
                    foundCookie = true;
                    break;
                }
            }

            assertTrue("Did not find sessionid cookie.", foundCookie);
            // Clear credentials to force check of sessionid cookie (which must be invalid)
            httpClient.getCredentialsProvider().clear();
            response = httpClient.execute(targetHost, getXML);
            entity = response.getEntity();
            EntityUtils.consume(entity);
            assertTrue("Expected 401.", response.getStatusLine().getStatusCode() == 401);
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
    }

    public void testUpdateXML() throws Exception {
        DefaultHttpClient httpClient = client.getSession().getHttpClient();
        httpClient.getCredentialsProvider().setCredentials(
            new AuthScope(targetHost.getHostName(), targetHost.getPort()),
            new UsernamePasswordCredentials(login, password));
        UpdateXMLRequest request = new UpdateXMLRequest();
        UpdateXMLResponse response = client.execute(request);

        SAXBuilder builder = new SAXBuilder();
        String xml = response.getXML();
        assertFalse("The response no or only an empty xml!", Strings.isEmpty(xml));

        Document doc = null;
        try {
            doc = builder.build(new StringReader(xml));
            Element products = doc.getRootElement();
            List<Element> children = products.getChildren("Product");

            Map<String, String> filesToGet = new HashMap<String, String>();
            for (Element child : children) {
                filesToGet.put(child.getChildText("URL"), child.getChildText("MD5"));
            }

            for (String url : filesToGet.keySet()) {
                FileRequest fileRequest = new FileRequest(extractFileName(url));
                FileResponse fileResponse = client.execute(fileRequest);
                byte[] fileBytes = fileResponse.getFileBytes();
                String md5 = calculateMD5(new ByteArrayInputStream(fileBytes));

                assertEquals("MD5 Hash was not correct for Download from " + url, md5, filesToGet.get(url));
            }
        } catch (JDOMException e) {
            System.out.println(xml);
            throw e;
        }
    }

    public void testAgainWithChangedLocale() throws Exception {
        SetRequest setRequest = new SetRequest(Tree.Language, "wx_YZ");
        client.execute(setRequest);

        try {
            testUpdateXML();
        } finally {
            SetRequest setBackRequest = new SetRequest(Tree.Language, "en_US");
            client.execute(setBackRequest);
        }
    }

    private String extractFileName(String url) {
        String searchStr = "/ajax/drive/client/windows/download/";
        return url.substring(url.indexOf(searchStr) + searchStr.length());
    }

    private String calculateMD5(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return null;
        }
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            IOException e1 = new IOException(e.getMessage());
            e1.initCause(e);
            throw e1;
        }
        int length = -1;
        byte[] buf = new byte[512];
        while ((length = inputStream.read(buf)) != -1) {
            digest.update(buf, 0, length);
        }
        return new String(Hex.encodeHex(digest.digest()));
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
