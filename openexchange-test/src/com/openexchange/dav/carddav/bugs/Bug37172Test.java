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
import static org.junit.Assert.assertTrue;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.dav.carddav.VCardResource;
import com.openexchange.groupware.container.Contact;

/**
 * {@link Bug37172Test}
 *
 * Importing ownCloud vCards cause lost of phone numbers
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class Bug37172Test extends CardDAVTest {

    public Bug37172Test() {
        super();
    }

    @Test
    public void testNotLosingPhoneNumbers() throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        String syncToken = super.fetchSyncToken();
        /*
         * create contact
         */
        String uid = randomUID();
        String vCard = "BEGIN:VCARD\r\n" + "VERSION:3.0\r\n" + "N:;Test;;;\r\n" + "UID:" + uid + "\r\n" + "REV:2015-03-09T23:04:44+00:00\r\n" + "FN:Test\r\n" + "PRODID:-//ownCloud//NONSGML Contacts 0.3.0.18//EN\r\n" + "EMAIL;TYPE=WORK:test@abc123.de\r\n" + "TEL;TYPE=CELL:0151 123456789\r\n" + "TEL;TYPE=HOME:0911 9876543\r\n" + "TEL;TYPE=HOME:0160 123456\r\n" + "IMPP;X-SERVICE-TYPE=jabber:xmpp:87654321\r\n" + "TEL;TYPE=WORK:0912 12345678\r\n" + "END:VCARD\r\n";

        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putVCard(uid, vCard));
        /*
         * verify contact on server
         */
        Contact contact = super.getContact(uid);
        super.rememberForCleanUp(contact);
        assertEquals("uid wrong", uid, contact.getUid());
        assertEquals("firstname wrong", "Test", contact.getGivenName());
        assertEquals("lastname wrong", null, contact.getSurName());
        assertEquals("cellular phone wrong", "0151 123456789", contact.getCellularTelephone1());
        assertEquals("home phone wrong", "0911 9876543", contact.getTelephoneHome1());
        assertEquals("home phone alternative wrong", "0160 123456", contact.getTelephoneHome2());
        assertEquals("company phone wrong", "0912 12345678", contact.getTelephoneBusiness1());
        assertEquals("xmpp jabber wrong", "xmpp:87654321", contact.getInstantMessenger2());
        assertEquals("email wrong", "test@abc123.de", contact.getEmail1());

        /*
         * verify contact on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken);
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = super.addressbookMultiget(eTags.keySet());
        VCardResource card = assertContains(uid, addressData);
        assertEquals("N wrong", "Test", card.getGivenName());
        assertEquals("FN wrong", "Test", card.getFN());
    }

    @Test
    public void testNotLosingPhoneNumbersAlt() throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        String syncToken = super.fetchSyncToken();
        /*
         * create contact
         */
        String uid = randomUID();
        String vCard = "BEGIN:VCARD\r\n" + "VERSION:3.0\r\n" + "N:;Test;;;\r\n" + "UID:" + uid + "\r\n" + "REV:2015-03-09T23:04:44+00:00\r\n" + "FN:Test\r\n" + "PRODID:-//ownCloud//NONSGML Contacts 0.3.0.18//EN\r\n" + "EMAIL;TYPE=WORK:test@abc123.de\r\n" + "TEL;TYPE=CELL:0151 123456789\r\n" + "TEL;TYPE=home,voice:0911 9876543\r\n" + "TEL;TYPE=home,voice:0160 123456\r\n" + "IMPP;X-SERVICE-TYPE=jabber:xmpp:87654321\r\n" + "TEL;TYPE=WORK,voice:0912 12345678\r\n" + "END:VCARD\r\n";

        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putVCard(uid, vCard));
        /*
         * verify contact on server
         */
        Contact contact = super.getContact(uid);
        super.rememberForCleanUp(contact);
        assertEquals("uid wrong", uid, contact.getUid());
        assertEquals("firstname wrong", "Test", contact.getGivenName());
        assertEquals("lastname wrong", null, contact.getSurName());
        assertEquals("cellular phone wrong", "0151 123456789", contact.getCellularTelephone1());
        assertEquals("home phone wrong", "0911 9876543", contact.getTelephoneHome1());
        assertEquals("home phone alternative wrong", "0160 123456", contact.getTelephoneHome2());
        assertEquals("company phone wrong", "0912 12345678", contact.getTelephoneBusiness1());
        assertEquals("xmpp jabber wrong", "xmpp:87654321", contact.getInstantMessenger2());
        assertEquals("email wrong", "test@abc123.de", contact.getEmail1());

        /*
         * verify contact on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken);
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = super.addressbookMultiget(eTags.keySet());
        VCardResource card = assertContains(uid, addressData);
        assertEquals("N wrong", "Test", card.getGivenName());
        assertEquals("FN wrong", "Test", card.getFN());
    }
}
