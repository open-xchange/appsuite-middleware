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

package com.openexchange.ajax.onboarding.tests;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import xmlwise.Plist;
import xmlwise.XmlParseException;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpNotFoundException;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.webdav.xml.AbstractWebdavXMLTest;

/**
 * {@link PListDownloadTestHelper}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class PListDownloadTestHelper extends AbstractWebdavXMLTest {

    /**
     * Initializes a new {@link PListDownloadTestHelper}.
     * 
     * @param name
     */
    public PListDownloadTestHelper(String name) {
        super(name);
    }

    private static final String[] PLIST_BASIC_KEYS = new String[] { "PayloadIdentifier", "PayloadType", "PayloadUUID", "PayloadVersion", "PayloadDisplayName", "PayloadContent" };
    private static final String[] PLIST_MAIL_KEYS = new String[] { "PayloadType", "PayloadUUID", "PayloadIdentifier", "PayloadVersion", "EmailAccountDescription", "EmailAccountName", "EmailAccountType", "EmailAddress", "IncomingMailServerAuthentication", "IncomingMailServerHostName", "IncomingMailServerPortNumber", "IncomingMailServerUseSSL", "IncomingMailServerUsername", "OutgoingMailServerAuthentication", "OutgoingMailServerHostName", "OutgoingMailServerPortNumber", "OutgoingMailServerUseSSL", "OutgoingMailServerUsername" };
    private static final String[] PLIST_EAS_KEYS = new String[] { "PayloadType", "PayloadUUID", "PayloadIdentifier", "PayloadVersion", "UserName", "EmailAddress", "Host", "SSL" };
    private static final String[] PLIST_DAV_KEYS = new String[] { "PayloadType", "PayloadUUID", "PayloadIdentifier", "PayloadVersion", "PayloadOrganization", "CardDAVUsername", "CardDAVHostName", "CardDAVUseSSL", "CardDAVAccountDescription" };

    protected void testMailDownload(String url, String host) throws IOException, SAXException, ParserConfigurationException, TransformerException, XmlParseException {

        Map<String, Object> properties = testDownload(host, url);
        if (properties == null) {
            //Scenario is probably deactivated
            System.err.println("Unable to test mail Download. Mail Scenario is probably deactivated.");
            return;
        }
        for (String key : PLIST_MAIL_KEYS) {
            assertTrue("Plist does not contain the following property: " + key, properties.keySet().contains(key));
            assertNotNull("The property " + key + " is null", properties.get(key));
        }
    }

    protected void testEASDownload(String url, String host) throws IOException, SAXException, ParserConfigurationException, TransformerException, XmlParseException {

        Map<String, Object> properties = testDownload(host, url);
        if (properties == null) {
            //Scenario is probably deactivated
            System.err.println("Unable to test EAS Download. EAS Scenario is probably deactivated.");
            return;
        }

        for (String key : PLIST_EAS_KEYS) {
            assertTrue("Plist does not contain the following property: " + key, properties.keySet().contains(key));
            assertNotNull("The property " + key + " is null", properties.get(key));
        }
    }

    protected void testDavDownload(String url, String host) throws IOException, SAXException, ParserConfigurationException, TransformerException, XmlParseException {
        Map<String, Object> properties = testDownload(host, url);

        if (properties == null) {
            //Scenario is probably deactivated
            System.err.println("Unable to test Dav Download. Dav Scenario is probably deactivated.");
            return;
        }
        for (String key : PLIST_DAV_KEYS) {
            assertTrue("Plist does not contain the following property: " + key, properties.keySet().contains(key));
            assertNotNull("The property " + key + " is null", properties.get(key));
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> testDownload(String host, String url) throws TransformerException, XmlParseException, ParserConfigurationException, SAXException, IOException {
        try {
        host = AbstractWebdavXMLTest.appendPrefix(host);
        final WebRequest webRequest = new GetMethodWebRequest(host + url);

        final WebResponse webResponse = getNewWebConversation().getResponse(webRequest);

        assertEquals(200, webResponse.getResponseCode());
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(webResponse.getInputStream());
        assertNotNull(doc);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        String plist = writer.getBuffer().toString().replaceAll("\n|\r", "");
        Map<String, Object> properties = Plist.fromXml(plist);
        assertNotNull(properties);

        //Test basic values
        for (String key : PLIST_BASIC_KEYS) {
            assertTrue(properties.keySet().contains(key));
        }

        for (Object o : properties.values()) {
            assertNotNull(o);
        }

        properties = (Map<String, Object>) ((ArrayList<Object>) properties.get("PayloadContent")).get(0);
        assertNotNull(properties);

        return properties;
        } catch (HttpNotFoundException e) {
            return null;
        }

    }
}
