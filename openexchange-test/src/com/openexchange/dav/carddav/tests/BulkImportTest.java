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

package com.openexchange.dav.carddav.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.StringReader;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.dav.carddav.VCardResource;
import com.openexchange.groupware.container.Contact;

/**
 * {@link BulkImportTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class BulkImportTest extends CardDAVTest {

    public BulkImportTest() {
        super();
    }
    
    @Test
    public void testBulkImportWithSimilarityCheck() throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        final String syncToken = super.fetchSyncToken();
        /*
         * create contact
         */

        final String firstName = "test";
        final String lastName = "horst";
        final String uid = UUID.nameUUIDFromBytes((firstName + lastName + "_bulk_contact").getBytes()).toString();
        final String firstName2 = "test2";
        final String lastName2 = "horst2";
        final String uid2 = UUID.nameUUIDFromBytes((firstName2 + lastName2 + "_bulk_contact").getBytes()).toString();
        final String firstName3 = "test3";
        final String lastName3 = "horst3";
        final String uid3 = UUID.nameUUIDFromBytes((firstName3 + lastName3 + "_bulk_contact").getBytes()).toString();

        final String email1 = uid + "@domain.com";
        final String email2 = uid2 + "@domain.com";
        final String vCard1 =
                "BEGIN:VCARD" + "\r\n" +
                "VERSION:3.0" + "\r\n" +
                "N:" + lastName + ";" + firstName + ";;;" + "\r\n" +
                "FN:" + firstName + " " + lastName + "\r\n" +
                "ORG:test3;" + "\r\n" +
                "EMAIL;type=INTERNET;type=WORK;type=pref:" + email1 + "\r\n" +
                "TEL;type=WORK;type=pref:24235423" + "\r\n" +
                "TEL;type=CELL:352-3534" + "\r\n" +
                "TEL;type=HOME:346346" + "\r\n" +
                "UID:" + uid + "\r\n" +
                "REV:" + super.formatAsUTC(new Date()) + "\r\n" +
                "PRODID:-//Apple Inc.//AddressBook 6.0//EN" + "\r\n" +
                "END:VCARD" + "\r\n"   
        ;
        final String vCard2 = 
            "BEGIN:VCARD" + "\r\n" +
            "VERSION:3.0" + "\r\n" +
            "N:" + lastName2 + ";" + firstName2 + ";;;" + "\r\n" +
            "FN:" + firstName2 + " " + lastName2 + "\r\n" +
            "ORG:test3;" + "\r\n" +
                "EMAIL;type=INTERNET;type=WORK;type=pref:" + email2 + "\r\n" +
            "TEL;type=WORK;type=pref:24235423" + "\r\n" +
            "TEL;type=CELL:352-3534" + "\r\n" +
            "TEL;type=HOME:346346" + "\r\n" +
            "UID:" + uid2 + "\r\n" +
            "REV:" + super.formatAsUTC(new Date()) + "\r\n" +
            "PRODID:-//Apple Inc.//AddressBook 6.0//EN" + "\r\n" +
                "END:VCARD" + "\r\n";
        final String vCard3 = 
            "BEGIN:VCARD" + "\r\n" + 
            "VERSION:3.0" + "\r\n" + 
            "N:" + lastName3 + ";" + firstName3 + ";;;" + "\r\n" + 
            "FN:" + firstName3 + " " + lastName3 + "\r\n" + 
            "ORG:test3;" + "\r\n" + 
                "EMAIL;type=INTERNET;type=WORK;type=pref:" + email1 + "\r\n" + 
            "TEL;type=WORK;type=pref:24235423" + "\r\n" + 
            "TEL;type=CELL:352-3534" + "\r\n" + "TEL;type=HOME:346346" + "\r\n" + 
            "UID:" + uid3 + "\r\n" + 
            "REV:" + super.formatAsUTC(new Date()) + "\r\n" + 
            "PRODID:-//Apple Inc.//AddressBook 6.0//EN" + "\r\n" + 
            "END:VCARD" + "\r\n";

        final String vCard = vCard2 + vCard3;

        // Delete existing contacts
        super.delete(uid);
        super.delete(uid2);

        // Import first contacts (ignore similarity)
        String xmlResponse = super.postVCard(uid, vCard1, 0);
        Document xmlDoc = loadXMLFromString(xmlResponse);
        NodeList list = xmlDoc.getElementsByTagName("D:href");
        assertEquals("Unexpected href count", 1, list.getLength());
        Node node = list.item(0);
        String hrefContent = node.getTextContent();
        assertNotNull("Response does not contain a href", hrefContent);
        assertTrue("Response does not contain a href", !hrefContent.isEmpty());

        // Import contacts 2 and 3
        xmlResponse = super.postVCard(uid, vCard, 1);
        xmlDoc = loadXMLFromString(xmlResponse);
        list = xmlDoc.getElementsByTagName("D:href");
        assertEquals("Unexpected href count", 3, list.getLength());
        node = list.item(0);
        hrefContent = node.getTextContent();
        assertNotNull("Response does not contain a href", hrefContent);
        assertTrue("Response does not contain a href", !hrefContent.isEmpty());

        node = list.item(1);
        hrefContent = node.getTextContent();
        assertTrue("Response does contain a href, but it shouldn't", hrefContent == null || hrefContent.isEmpty());
        NodeList noSimilarContacts = xmlDoc.getElementsByTagName("OX:no-similar-contact");
        assertEquals("Unexpected no-similar-contact count", 1, noSimilarContacts.getLength());

        /*
         * verify contacts on server
         */
        final Contact contact = super.getContact(uid);
        super.rememberForCleanUp(contact);
        assertEquals("uid wrong", uid, contact.getUid());
        assertEquals("firstname wrong", firstName, contact.getGivenName());
        assertEquals("lastname wrong", lastName, contact.getSurName());

        final Contact contact2 = super.getContact(uid2);
        super.rememberForCleanUp(contact2);
        assertEquals("uid wrong", uid2, contact2.getUid());
        assertEquals("firstname wrong", firstName2, contact2.getGivenName());
        assertEquals("lastname wrong", lastName2, contact2.getSurName());

        // verify contact 3 is not on server
        final Contact contact3 = super.getContact(uid3);
        assertNull("Contact3 is not null", contact3);

        /*
         * verify contact on client
         */
        final Map<String, String> eTags = super.syncCollection(syncToken);
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        final List<VCardResource> addressData = super.addressbookMultiget(eTags.keySet());
        final VCardResource card = assertContains(uid, addressData);
        assertEquals("N wrong", firstName, card.getGivenName());
        assertEquals("N wrong", lastName, card.getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, card.getFN());

        final VCardResource card2 = assertContains(uid2, addressData);
        assertEquals("N wrong", firstName2, card2.getGivenName());
        assertEquals("N wrong", lastName2, card2.getFamilyName());
        assertEquals("FN wrong", firstName2 + " " + lastName2, card2.getFN());

        assertNotContains(uid3, addressData);
    }

    private static Document loadXMLFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }

}
