/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.drive.updater;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.binary.Hex;
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
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.config.actions.SetRequest;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.java.Strings;
import com.openexchange.test.common.configuration.AJAXConfig;
import com.openexchange.test.common.configuration.AJAXConfig.Property;

/**
 * {@link UpdaterXMLTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class UpdaterXMLTest extends AbstractAJAXSession {

    private String hostname;

    private HttpHost targetHost;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        hostname = AJAXConfig.getProperty(Property.HOSTNAME);
        targetHost = new HttpHost(hostname, 80);
    }

    @Test
    public void testBasicAuth() throws Exception {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        try {
            HttpGet getXML = new HttpGet("/ajax/drive/client/windows/v1/update.xml");
            HttpResponse response = httpClient.execute(targetHost, getXML);
            HttpEntity entity = response.getEntity();
            EntityUtils.consume(entity);
            assertEquals(401, response.getStatusLine().getStatusCode());

            httpClient.getCredentialsProvider().setCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()), new UsernamePasswordCredentials(testUser.getLogin(), testUser.getPassword()));
            response = httpClient.execute(targetHost, getXML);
            entity = response.getEntity();
            EntityUtils.consume(entity);
            assertEquals("Expected 200 but was: " + response.getStatusLine().getReasonPhrase(), 200, response.getStatusLine().getStatusCode());

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

    @Test
    public void testUpdateXML() throws Exception {
        DefaultHttpClient httpClient = getClient().getSession().getHttpClient();
        httpClient.getCredentialsProvider().setCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()), new UsernamePasswordCredentials(testUser.getLogin(), testUser.getPassword()));
        UpdateXMLRequest request = new UpdateXMLRequest();
        UpdateXMLResponse response = getClient().execute(request);

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
                FileResponse fileResponse = getClient().execute(fileRequest);
                byte[] fileBytes = fileResponse.getFileBytes();
                String md5 = calculateMD5(new ByteArrayInputStream(fileBytes));

                assertEquals("MD5 Hash was not correct for Download from " + url, md5, filesToGet.get(url));
            }
        } catch (JDOMException e) {
            System.out.println(xml);
            throw e;
        }
    }

    @Test
    public void testAgainWithChangedLocale() throws Exception {
        SetRequest setRequest = new SetRequest(Tree.Language, "wx_YZ");
        getClient().execute(setRequest);

        try {
            testUpdateXML();
        } finally {
            SetRequest setBackRequest = new SetRequest(Tree.Language, "en_US");
            getClient().execute(setBackRequest);
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
}
