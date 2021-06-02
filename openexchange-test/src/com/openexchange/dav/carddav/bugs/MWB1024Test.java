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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Autoboxing;
import com.openexchange.java.Strings;
import com.openexchange.test.tryagain.TryAgain;

/**
 * {@link MWB1024Test}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.6
 */
public class MWB1024Test extends CardDAVTest {

    @Test
    @TryAgain
    public void testBulkImportOneCardTooLarge() {
        /*
         * prepare vCards, with the second one being too large
         */
        String email1 = "first_" + randomUID() + "@example.com";
        String email2 = "second_" + randomUID() + "@example.com";
        String email3 = "third_" + randomUID() + "@example.com";
        String vCards = // @formatter:off
            "BEGIN:VCARD" + "\r\n" +
            "VERSION:3.0" + "\r\n" +
            "FN:test1" + "\r\n" +
            "EMAIL;type=INTERNET;type=WORK;type=pref:" + email1 + "\r\n" +
            "REV:" + formatAsUTC(new Date()) + "\r\n" +
            "PRODID:-//Apple Inc.//AddressBook 6.0//EN" + "\r\n" +
            "END:VCARD" + "\r\n" +
            "BEGIN:VCARD" + "\r\n" +
            "VERSION:3.0" + "\r\n" +
            "FN:" + RandomStringUtils.randomAlphabetic(5000000) + "\r\n" +
            "EMAIL;type=INTERNET;type=WORK;type=pref:" + email2 + "\r\n" +
            "REV:" + formatAsUTC(new Date()) + "\r\n" +
            "PRODID:-//Apple Inc.//AddressBook 6.0//EN" + "\r\n" +
            "END:VCARD" + "\r\n" +
            "BEGIN:VCARD" + "\r\n" +
            "VERSION:3.0" + "\r\n" +
            "FN:test1" + "\r\n" +
            "EMAIL;type=INTERNET;type=WORK;type=pref:" + email3 + "\r\n" +
            "REV:" + formatAsUTC(new Date()) + "\r\n" +
            "PRODID:-//Apple Inc.//AddressBook 6.0//EN" + "\r\n" +
            "END:VCARD" + "\r\n"
        ; // @formatter:on
        /*
         * attempt to upload them multiple times
         */
        testUploadRepeatedly(vCards, Arrays.asList(email1, email3));
    }

    @Test
    @TryAgain
    public void testBulkImportManyCards() {
        /*
         * prepare many vCards for the bulk import
         */
        List<String> emails = new ArrayList<String>();
        String vCards;
        {
            int numCards = 1500;
            StringBuilder stringBuilder = new StringBuilder(4096 * numCards);
            for (int i = 0; i < numCards; i++) {
                String email = i + '_' + randomUID() + "@example.com";
                emails.add(email);
                String uid = randomUID();
                String vCard = // @formatter:off
                    "BEGIN:VCARD" + "\r\n" +
                    "VERSION:3.0" + "\r\n" +
                    "FN:test1 " + randomUID() + "\r\n" +
                    "EMAIL;type=INTERNET;type=WORK;type=pref:" + email + "\r\n" +
                    "UID:" + uid + "\r\n" +
                    "ORG:Metzgerei Fischer" + "\r\n" +
                    "TITLE:Angestellter" + "\r\n" +
                    "NICKNAME:" + RandomStringUtils.randomAlphabetic(3000) + "\r\n" +
                    "NOTE;CHARSET=Windows-1252:Wollte mich am Montag zur\u00fcckrufen." + "\r\n" +
                    "TEL;WORK;VOICE:034523-34234234" + "\r\n" +
                    "TEL;HOME;VOICE:0444 / 2340349" + "\r\n" +
                    "TEL;CELL;VOICE:+49 4545345324523" + "\r\n" +
                    "TEL;WORK;FAX:034523-34234230" + "\r\n" +
                    "X-MS-OL-DEFAULT-POSTAL-ADDRESS:2" + "\r\n" +
                    "URL;WORK:http://www.metzgerei-fischer.co.uk" + "\r\n" +
                    "X-MS-IMADDRESS:ICQ: 143823280" + "\r\n" +
                    "REV:" + formatAsUTC(new Date()) + "\r\n" +
                    "PRODID:-//Apple Inc.//AddressBook 6.0//EN" + "\r\n" +
                    "END:VCARD" + "\r\n"
                ; // @formatter:on
                stringBuilder.append(vCard);
            }
            vCards = stringBuilder.toString();
        }
        /*
         * attempt to upload them multiple times
         */
        testUploadRepeatedly(vCards, emails);
    }

    private void testUploadRepeatedly(String vCards, List<String> expectedEmails) {
        /*
         * attempt to upload the bulk import, repeating multiple times if an error occurs
         */
        int count = 3;
        for (int i = 0; i < count; i++) {
            try {
                postVCard(vCards, 0);
                break;
            } catch (Exception | AssertionError e) {
                // ignore, like a dumb client does
            }
        }
        /*
         * count created contacts on server, based on their mail address
         */
        Map<String, Integer> contactCounts = new HashMap<String, Integer>();
        Contact[] contacts = cotm.allAction(getDefaultFolderID(), new int[] { Contact.OBJECT_ID, Contact.FOLDER_ID, Contact.UID, Contact.EMAIL1 });
        for (Contact contact : contacts) {
            String email1 = contact.getEmail1();
            if (Strings.isEmpty(email1) || false == expectedEmails.contains(email1)) {
                continue;
            }
            contactCounts.merge(email1, Autoboxing.I(1), Integer::sum);
        }
        assertFalse("no contacts imported at all", contactCounts.isEmpty());
        /*
         * check that all counts are equal
         */
        Set<Integer> distinctValues = new HashSet<Integer>(contactCounts.values());
        assertEquals("unequal number of contacts created", 1, distinctValues.size());
        /*
         * check that all expected contacts were imported
         */
        for (String email : expectedEmails) {
            assertTrue("contact " + email + " not imported", contactCounts.containsKey(email));
        }
    }

}
