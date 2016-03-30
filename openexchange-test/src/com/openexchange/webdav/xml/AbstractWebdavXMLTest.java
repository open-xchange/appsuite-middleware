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

package com.openexchange.webdav.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import com.meterware.httpunit.PutMethodWebRequest;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.webdav.AbstractWebdavTest;
import com.openexchange.webdav.xml.fields.DataFields;
import com.openexchange.webdav.xml.request.PropFindMethod;

/**
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public abstract class AbstractWebdavXMLTest extends AbstractWebdavTest {

    protected static final int APPEND_MODIFIED = 1000000;

    public AbstractWebdavXMLTest(final String name) {
        super(name);
    }

    protected static int parseResponse(final Document response, final boolean delete) throws Exception {
        return parseRootElement(response.getRootElement(), delete);
    }

    protected static int parseRootElement(final Element e, final boolean delete) throws Exception {
        assertNotNull("root element (null)", e);
        assertEquals("root element", "multistatus", e.getName());

        return parseResponseElement(e.getChild("response", webdav), delete);
    }

    protected static int parseResponseElement(final Element e, final boolean delete) throws Exception {
        assertNotNull("response element (null)", e);
        assertEquals("response element", "response", e.getName());

        parseHrefElement(e.getChild("href", webdav), delete);
        return parsePropstatElement(e.getChild("propstat", webdav));
    }

    protected static void parseHrefElement(final Element e, final boolean delete) throws Exception {
        assertNotNull("response element (null)", e);
        assertEquals("response element", "href", e.getName());
        if (!delete) {
            assertTrue("href value > 0", (Integer.parseInt(e.getValue()) > 0));
        }
    }

    protected static int parsePropstatElement(final Element e) throws Exception {
        assertNotNull("propstat element (null)", e);
        assertEquals("propstat element", "propstat", e.getName());

        parseStatusElement(e.getChild("status", webdav));
        parseResponsedescriptionElement(e.getChild("responsedescription", webdav));

        return parsePropElement(e.getChild("prop", webdav));
    }

    protected static int parsePropElement(final Element e) throws Exception {
        assertNotNull("prop element (null)", e);
        assertEquals("prop element", "prop", e.getName());

        return parseObjectIdElement(e.getChild(DataFields.OBJECT_ID, XmlServlet.NS));
    }

    protected static void parseStatusElement(final Element e) throws Exception {
        assertNotNull("status element (null)", e);
        assertEquals("status element", "status", e.getName());
        assertNotNull("status not null", e.getValue());
        assertEquals("status 200", 200, Integer.parseInt(e.getValue()));
    }

    protected static void parseResponsedescriptionElement(final Element e) throws Exception {
        assertNotNull("status element (null)", e);
        assertEquals("status element", "responsedescription", e.getName());
        assertNotNull("response description not null", e.getValue());
    }

    protected static int parseObjectIdElement(final Element e) throws Exception {
        assertNotNull("object_id element (null)", e);
        assertEquals("object_id element", "object_id", e.getName());
        assertNotNull("object_id null", e.getValue());

        final int objectId = Integer.parseInt(e.getValue());

        assertTrue("object id > 0", (objectId > 0));

        return objectId;
    }

    protected static Element addProp2PropertyUpdate(final Element eProp) throws Exception {
        final Element rootElement = new Element("propertyupdate", webdav);
        rootElement.addNamespaceDeclaration(XmlServlet.NS);

        final Element eSet = new Element("set", webdav);
        eSet.addContent(eProp);

        rootElement.addContent(eSet);

        return rootElement;
    }

    protected static Document addProp2Document(final Element eProp) throws Exception {
        final Element rootElement = new Element("propertyupdate", webdav);
        rootElement.addNamespaceDeclaration(XmlServlet.NS);

        final Element eSet = new Element("set", webdav);
        eSet.addContent(eProp);

        rootElement.addContent(eSet);

        return new Document(rootElement);
    }

    protected int sendPut(final byte b[]) throws Exception {
        return sendPut(b, false);
    }

    protected int sendPut(final byte b[], final boolean delete) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        req = new PutMethodWebRequest(PROTOCOL + hostName + getURL(), bais, "text/javascript");
        req.setHeaderField("Authorization", "Basic " + authData);
        resp = webCon.getResponse(req);

        bais = new ByteArrayInputStream(resp.getText().getBytes(com.openexchange.java.Charsets.UTF_8));

        final Document doc = new SAXBuilder().build(bais);
        return parseResponse(doc, delete);
    }

    protected void sendPropFind(final byte requestByte[]) throws Exception {
        final HttpClient httpclient = new HttpClient();

        httpclient.getState().setCredentials(null, new UsernamePasswordCredentials(login, password));
        final PropFindMethod propFindMethod = new PropFindMethod(PROTOCOL + hostName + getURL());
        propFindMethod.setDoAuthentication( true );

        InputStream is = new ByteArrayInputStream(requestByte);
        propFindMethod.setRequestBody(is);

        final int status = httpclient.executeMethod(propFindMethod);

        assertEquals("check propfind response", 207, status);

        is = propFindMethod.getResponseBodyAsStream();

        final Document doc = new SAXBuilder().build(is);

        parseResponse(doc, false);
    }

    protected void deleteObject(final FolderChildObject folderChildObj, final int inFolder) throws Exception {
        final Element e_prop = new Element("prop", webdav);

        final Element e_objectId = new Element("object_id", XmlServlet.NS);
        e_objectId.addContent(String.valueOf(folderChildObj.getObjectID()));
        e_prop.addContent(e_objectId);

        if (inFolder != -1) {
            final Element eFolderId = new Element("folder_id", XmlServlet.NS);
            eFolderId.addContent(String.valueOf(inFolder));
            e_prop.addContent(eFolderId);
        }

        final Element e_method = new Element("method", XmlServlet.NS);
        e_method.addContent("DELETE");
        e_prop.addContent(e_method);

        final byte[] b = null; // writeRequest(e_prop);
        sendPut(b, true);
    }

    protected void listObjects(final int folderId, final Date lastSync, final boolean delete) throws Exception {
        final Element e_propfind = new Element("propfind", webdav);
        final Element e_prop = new Element("prop", webdav);

        final Element e_folderId = new Element("folder_id", XmlServlet.NS);
        final Element e_lastSync = new Element("lastsync", XmlServlet.NS);
        final Element e_objectmode = new Element("objectmode", XmlServlet.NS);

        e_folderId.addContent(String.valueOf(folderId));
        e_lastSync.addContent(String.valueOf(lastSync.getTime()));

        e_propfind.addContent(e_prop);
        e_prop.addContent(e_folderId);
        e_prop.addContent(e_lastSync);

        if (delete) {
            e_objectmode.addContent("NEW_AND_MODIFIED,DELETED");
            e_prop.addContent(e_objectmode);
        }

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final Document doc = new Document(e_propfind);

        final XMLOutputter xo = new XMLOutputter();
        xo.output(doc, baos);

        baos.flush();

        sendPropFind(baos.toByteArray());
    }

    protected void loadObject(final int objectId) throws Exception {
        final Element e_propfind = new Element("propfind", webdav);
        final Element e_prop = new Element("prop", webdav);

        final Element eObjectId = new Element("object_id", XmlServlet.NS);

        eObjectId.addContent(String.valueOf(objectId));

        e_propfind.addContent(e_prop);
        e_prop.addContent(eObjectId);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final Document doc = new Document(e_propfind);

        final XMLOutputter xo = new XMLOutputter();
        xo.output(doc, baos);

        baos.flush();

        sendPropFind(baos.toByteArray());
    }

    protected String getURL() {
        return "no/url";
    }

    public static final String appendPrefix(final String host) {
        if (host.startsWith("http://")) {
            return host;
        }
        return "http://" + host;
    }

    public static void assertEqualsAndNotNull(final String message, final Date expect, final Date value) throws Exception {
        if (expect != null) {
            assertNotNull(message + " is null", value);
            assertEquals(message, expect.getTime(), value.getTime());
        }
    }

    public static void assertEqualsAndNotNull(final String message, final Date[] expect, final Date[] value) throws Exception {
        if (expect != null) {
            assertNotNull(message + " is null", value);
            assertEquals(message + " date array size is not equals", expect.length, value.length);
            for (int a = 0; a < expect.length; a++) {
                assertEquals(message + " date in pos (" + a + ") is not equals",  expect[a].getTime(), value[a].getTime());
            }
        }
    }

    public static void assertEqualsAndNotNull(final String message, final byte[] expect, final byte[] value) throws Exception {
        if (expect != null) {
            assertNotNull(message + " is null", value);
            assertEquals(message + " byte array size is not equals", expect.length, value.length);
            for (int a = 0; a < expect.length; a++) {
                assertEquals(message + " byte in pos (" + a + ") is not equals",  expect[a], value[a]);
            }
        }
    }

    public static void assertEqualsAndNotNull(final String message, final Object expect, final Object value) throws Exception {
        if (expect != null) {
            assertNotNull(message + " is null", value);
            assertEquals(message, expect, value);
        }
    }

    /**
     * @deprecated XML now uses AJAX server error codes.
     */
    @Deprecated
    protected static void assertExceptionMessage(final String message, final int expectedStatus) throws Exception {
        assertExceptionMessage(message, String.valueOf(expectedStatus));
    }

    protected static void assertExceptionMessage(final String message, final String expectedStatus) throws Exception {
        final String statusCode = message.substring(1, message.indexOf("]"));
        assertEquals("Status code is not correct", expectedStatus, statusCode);
    }
}
