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

/**
 * {@link MWB1024Test}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.6
 */
public class MWB1024Test extends CardDAVTest {

    @Test
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
                    "NOTE;CHARSET=Windows-1252:Wollte mich am Montag zur\u00fcckrufen." + "\r\n" +
                    "TEL;WORK;VOICE:034523-34234234" + "\r\n" +
                    "TEL;HOME;VOICE:0444 / 2340349" + "\r\n" +
                    "TEL;CELL;VOICE:+49 4545345324523" + "\r\n" +
                    "TEL;WORK;FAX:034523-34234230" + "\r\n" +
                    "X-MS-OL-DEFAULT-POSTAL-ADDRESS:2" + "\r\n" +
                    "URL;WORK:http://www.metzgerei-fischer.co.uk" + "\r\n" +
                    "X-MS-IMADDRESS:ICQ: 143823280" + "\r\n" +
                    "PHOTO;TYPE=JPEG;ENCODING=BASE64:" + "\r\n" +
                    " /9j/4AAQSkZJRgABAgAAAQABAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRof" + "\r\n" +
                    " Hh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwh" + "\r\n" +
                    " MjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAAR" + "\r\n" +
                    " CABMAEADASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAA" + "\r\n" +
                    " AgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkK" + "\r\n" +
                    " FhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWG" + "\r\n" +
                    " h4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl" + "\r\n" +
                    " 5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREA" + "\r\n" +
                    " AgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYk" + "\r\n" +
                    " NOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOE" + "\r\n" +
                    " hYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk" + "\r\n" +
                    " 5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD3+iiigDG8VeIrTwr4du9YvMtFbrkIDgux" + "\r\n" +
                    " 4Cj6mvmrxD8dvFurXe/Tpl0qAZ2xQgOcf7TMOfyFes/He3u9Q8KaZptoMvd6ikWCcA/KSMn0" + "\r\n" +
                    " zXgWpeA7+xvtJtbe7sr7+03McE1rLujLggEE44xuH55oA77wr8dNf060M+uxw6pbNIE4bypU" + "\r\n" +
                    " 9SMAhvpx0r6H0nVLTWtKttRsZBJbXCB0b2Pr718eeI/A8/h7Tmuf7StL8QT/AGe5FqzMIZMc" + "\r\n" +
                    " KSQAc88j0r3L9nnUpbvwNd2kjEi0uyqZ7Kyg/wA80AevUUUUAFIelLSHpQB578XmMHh7TbsB" + "\r\n" +
                    " CYNTgbDnAIJwQT2yDXKeH/Bem29rA5kldYJWmt5JJMmLLA4BGB2X8q2/iVNL4r1fS/BWjGKe" + "\r\n" +
                    " 5E63Wo56W8SYI3Hpkkngc8VbufBN2t20VrkW7NnGMj8Mnj8a8vM6GJqwSoSsdOHnTjfnR5J4" + "\r\n" +
                    " 90a08P8Ah3U0067adNTvoppQ56Fd5G3A55c5z7V1/wCzfMy2HiKzeN0khmgkYMMfeVx/7JXX" + "\r\n" +
                    " +JfhrHrfgq701SBfld8DE8CRegJ9+n414d4B8fXnhDxyjauTHbbfsl6u3JUKTgkDqVOf1rqw" + "\r\n" +
                    " dOtCko1pXkZVXFyvFH1tRUFneW99Zw3dpMs1vMgeORDkMp6EVPXUZhXMfEDxSPCHg691RNpu" + "\r\n" +
                    " gPLtkbo0rcD8uv4V0xNeB/HrXI9S1TSvDdhN5lzbuXuEAOEd9ojz2zgsfxFFwJ/gHY6xc3Ou" + "\r\n" +
                    " eIb4l1vJET7RKdxlYbi+PoSK9zAbd975fpWX4a0WHw74bsdKt1CrbQqhx3bHJ/E5rTjbc7+x" + "\r\n" +
                    " xQBBeLeFf9GeP3DDn86+Wfi74Mn0TxYb0k7NVZ5kXb0cY3j/AMeBz7n0r6wrx/8AaHts+DLC" + "\r\n" +
                    " 9T5Zbe82hh1CspBH5hfyoAwP2e/FcguL7wvf3LEhRLZRt2xnzFH6HHsa+gK+KPCGrvonjnQ9" + "\r\n" +
                    " SDFWinj8z3UnaR+Kn9a+1x0oAqz31pasFuLqGIntJIF/nXhfivwXJc+LtQ8V6dcPcubqOf7K" + "\r\n" +
                    " V4kVNpwD65Xj1Fej68g/t+bODmJD/OqWK+UzDO69Ks6VNW5X956mHwcHDnl1On8O+KdK8T2h" + "\r\n" +
                    " lsLhfOjA8+2biWAns6dR0P1xWvECGkyOrfnXz34r0zxBo3jSw17whBcyXdzlLiKFSQ+MEbh0" + "\r\n" +
                    " wQT19M19CQPI8EbSrskKAuvoccivocHiY4mhGrHr+Z59Wm6cnFkhNeO/Hy/WbwetpGN3l3aN" + "\r\n" +
                    " J7jB4H516zqF/aadZyXN7dQ2tug+aWZwir9Sa8oM2n+L4bposXFm0mFZhw+O4rDMcW8LTjU8" + "\r\n" +
                    " 1c0w9JVZOJ4Z4M0aTxB4ls4ljYBJ43kcDKqgPJJ7cDivtRWDKGByCMivJdF8PWOiJL9lt44j" + "\r\n" +
                    " IQW2jriu/wBD1X7QgtZeJUGFP94VyYPOqeJxDp2suhpWwjpw5tzn9cstUg1W5uxaPcwSY2mE" + "\r\n" +
                    " bioA6Edax01m1MpilLRSDgpICpH4GvVD0qKeGKeFo5o0kQjlXUEGoxWQ0q03NSabKpY2UEot" + "\r\n" +
                    " aHkPiTVo7XTTJFc7JR80bo2GUjnIrmNN+JfxYmsV+zeHnvo8DZdHTpPnHY5BCnPXivbLXwho" + "\r\n" +
                    " Nlf/AG6302FJ+oYDgH2Fbo6kV2ZbgHg4OPNe5nia6qtOx8k+Lv8AhZ/i4xvrejaw8MWSkMdg" + "\r\n" +
                    " 6xpnqcBfbvmu7+HEqWnh5LWbMUqfeRxgqeeo7V74awtS8JaJq94Lm8sUaYDBdSVLfXHWqzPB" + "\r\n" +
                    " vF0uS9icNWVKV2jl9OSXV7lobLaQmPMlPRP8TXY6Xo8WmgvuaSZhgu38h6CrNlZ21hbLb2kK" + "\r\n" +
                    " QxJkBEGB1q2OlZ4DKaOESlvLuVXxU6unQ//Z" + "\r\n" +
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
