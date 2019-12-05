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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.file.storage.owncloud;

import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.openexchange.file.storage.owncloud.internal.SearchResult;

/**
 * {@link SearchResultTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class SearchResultTest {

    private static final String data = "<?xml version=\"1.0\"?>\n" +
        "<d:multistatus xmlns:d=\"DAV:\" xmlns:s=\"http://sabredav.org/ns\" xmlns:oc=\"http://owncloud.org/ns\">\n" +
        "    <d:response>\n" +
        "        <d:href>/remote.php/dav/files/admin/Photos</d:href>\n" +
        "        <d:propstat>\n" +
        "            <d:prop>\n" +
        "                <d:getetag>&quot;5e21619b21f6a&quot;</d:getetag>\n" +
        "                <d:getlastmodified>Fri, 17 Jan 2020 07:26:19 GMT</d:getlastmodified>\n" +
        "                <d:resourcetype>\n" +
        "                    <d:collection/>\n" +
        "                </d:resourcetype>\n" +
        "                <oc:comments-unread>0</oc:comments-unread>\n" +
        "                <oc:fileid>4</oc:fileid>\n" +
        "                <oc:owner-display-name>admin</oc:owner-display-name>\n" +
        "                <oc:permissions>RDNVCK</oc:permissions>\n" +
        "                <oc:share-types/>\n" +
        "                <oc:size>678556</oc:size>\n" +
        "                <oc:tags/>\n" +
        "            </d:prop>\n" +
        "            <d:status>HTTP/1.1 200 OK</d:status>\n" +
        "        </d:propstat>\n" +
        "        <d:propstat>\n" +
        "            <d:prop>\n" +
        "                <d:creationdate/>\n" +
        "                <d:getcontentlength/>\n" +
        "                <d:getcontenttype/>\n" +
        "                <d:lockdiscovery/>\n" +
        "                <oc:favorites/>\n" +
        "            </d:prop>\n" +
        "            <d:status>HTTP/1.1 404 Not Found</d:status>\n" +
        "        </d:propstat>\n" +
        "    </d:response>\n" +
        "    <d:response>\n" +
        "        <d:href>/remote.php/dav/files/admin/photo.txt</d:href>\n" +
        "        <d:propstat>\n" +
        "            <d:prop>\n" +
        "                <d:getcontentlength>0</d:getcontentlength>\n" +
        "                <d:getcontenttype>text/plain</d:getcontenttype>\n" +
        "                <d:getetag>&quot;6ec86075a02fe82d9d7cef2c282a850a&quot;</d:getetag>\n" +
        "                <d:getlastmodified>Fri, 17 Jan 2020 07:26:54 GMT</d:getlastmodified>\n" +
        "                <d:resourcetype/>\n" +
        "                <oc:comments-unread>0</oc:comments-unread>\n" +
        "                <oc:fileid>16</oc:fileid>\n" +
        "                <oc:owner-display-name>admin</oc:owner-display-name>\n" +
        "                <oc:permissions>RDNVW</oc:permissions>\n" +
        "                <oc:share-types/>\n" +
        "                <oc:size>0</oc:size>\n" +
        "                <oc:tags/>\n" +
        "            </d:prop>\n" +
        "            <d:status>HTTP/1.1 200 OK</d:status>\n" +
        "        </d:propstat>\n" +
        "        <d:propstat>\n" +
        "            <d:prop>\n" +
        "                <d:creationdate/>\n" +
        "                <d:lockdiscovery/>\n" +
        "                <oc:favorites/>\n" +
        "            </d:prop>\n" +
        "            <d:status>HTTP/1.1 404 Not Found</d:status>\n" +
        "        </d:propstat>\n" +
        "    </d:response>\n" + "</d:multistatus>";

    @Test
    public void testParsing() throws JAXBException, ParserConfigurationException, SAXException, IOException {
        Document doc = convertStringToXMLDocument(data);
        JAXBContext jaxbContext = JAXBContext.newInstance(SearchResult.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        SearchResult searchResult = (SearchResult) jaxbUnmarshaller.unmarshal(doc);
        assertTrue(searchResult.getFiles().stream().filter((file) -> file.getPropstat().get().getFile().get().isCollection()).findFirst().isPresent());
    }

    private static Document convertStringToXMLDocument(String xmlString) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        factory.setNamespaceAware(true);
        builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xmlString)));
    }

}
