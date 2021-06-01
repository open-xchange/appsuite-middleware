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

package com.openexchange.dav.carddav.bugs;

import static org.junit.Assert.assertEquals;
import java.io.StringReader;
import java.util.Date;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import com.openexchange.dav.carddav.CardDAVTest;

/**
 * {@link Bug61873Test}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class Bug61873Test extends CardDAVTest {

    /**
     * Test-case for bug 61873
     *
     * @throws Exception
     */
    @Test
    public void testBulkImportWithTooBigVCard() throws Exception {
        final String firstName = "test";
        final String lastName = "horst";
        final String uid = UUID.nameUUIDFromBytes((firstName + lastName + "_bulk_contact").getBytes()).toString();
        final String firstName2 = RandomStringUtils.randomAlphabetic(3000000);
        final String lastName2 = "horst2";
        final String uid2 = UUID.nameUUIDFromBytes((firstName2 + lastName2 + "_bulk_contact").getBytes()).toString();
        final String firstName3 = "test";
        final String lastName3 = "peter";
        final String uid3 = UUID.nameUUIDFromBytes((firstName + lastName + "_bulk_contact").getBytes()).toString();
        
        final String email1 = uid + "@domain.com";
        final String email2 = uid2 + "@domain.com";
        final String email3 = uid3 + "@domain.com";
        final String vCard1 = "BEGIN:VCARD" + "\r\n" + "VERSION:3.0" + "\r\n" + "N:" + lastName + ";" + firstName + ";;;" + "\r\n" + "FN:" + firstName + " " + lastName + "\r\n" + "ORG:test3;" + "\r\n" + "EMAIL;type=INTERNET;type=WORK;type=pref:" + email1 + "\r\n" + "TEL;type=WORK;type=pref:24235423" + "\r\n" + "TEL;type=CELL:352-3534" + "\r\n" + "TEL;type=HOME:346346" + "\r\n" + "UID:" + uid + "\r\n" + "REV:" + super.formatAsUTC(new Date()) + "\r\n" + "PRODID:-//Apple Inc.//AddressBook 6.0//EN" + "\r\n" + "END:VCARD" + "\r\n";
        final String vCard2 = "BEGIN:VCARD" + "\r\n" + "VERSION:3.0" + "\r\n" + "N:" + lastName2 + ";" + firstName2 + ";;;" + "\r\n" + "FN:" + firstName2 + " " + lastName2 + "\r\n" + "ORG:test3;" + "\r\n" + "EMAIL;type=INTERNET;type=WORK;type=pref:" + email2 + "\r\n" + "TEL;type=WORK;type=pref:24235423" + "\r\n" + "TEL;type=CELL:352-3534" + "\r\n" + "TEL;type=HOME:346346" + "\r\n" + "UID:" + uid2 + "\r\n" + "REV:" + super.formatAsUTC(new Date()) + "\r\n" + "PRODID:-//Apple Inc.//AddressBook 6.0//EN" + "\r\n" + "END:VCARD" + "\r\n";
        final String vCard3 = "BEGIN:VCARD" + "\r\n" + "VERSION:3.0" + "\r\n" + "N:" + lastName3 + ";" + firstName3 + ";;;" + "\r\n" + "FN:" + firstName3 + " " + lastName3 + "\r\n" + "ORG:test3;" + "\r\n" + "EMAIL;type=INTERNET;type=WORK;type=pref:" + email3 + "\r\n" + "TEL;type=WORK;type=pref:24235423" + "\r\n" + "TEL;type=CELL:352-3534" + "\r\n" + "TEL;type=HOME:346346" + "\r\n" + "UID:" + uid3 + "\r\n" + "REV:" + super.formatAsUTC(new Date()) + "\r\n" + "PRODID:-//Apple Inc.//AddressBook 6.0//EN" + "\r\n" + "END:VCARD" + "\r\n";

        final String vCard = vCard1 + vCard2 + vCard3;

        // Delete existing contacts
        super.delete(uid);
        super.delete(uid2);
        super.delete(uid3);

        // Import contacts 1 and 2 and 3
        String xmlResponse = super.postVCard(vCard, -1);
        Document xmlDoc = loadXMLFromString(xmlResponse);
        NodeList list = xmlDoc.getElementsByTagName("D:href");
        assertEquals("Unexpected href count", 3, list.getLength());
    }

    private static Document loadXMLFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }

}
