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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.dav.carddav.VCardResource;
import com.openexchange.groupware.container.Contact;

/**
 * {@link Bug47921Test}
 *
 * Repeated errors for invalid chars in user content
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.2
 */
public class Bug47921Test extends CardDAVTest {

    /**
     * Initializes a new {@link Bug47921Test}.
     */
    public Bug47921Test() {
        super();
    }

    @Test
    public void testBulkImportMalformedVCard() throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(fetchSyncToken());
        /*
         * try to create contact using bulk-import
         */
        String uid = randomUID();
        String vCard = "BEGIN:VCARD\r\n" + "VERSION:3.0\r\n" + "PRODID:-//Apple Inc.//Mac OS X 10.11.6//EN\r\n" + "N:Herbert;Tester;;;\r\n" + "FN:Tester Herbert\r\n" + "ORG:Acme Testdorf;\r\n" + "TITLE:Verkaufsberater Gro\u00dfabnehmer\r\n" + "item1.EMAIL;type=INTERNET;type=pref:Tester.Herbert@ncvspcvswfw-wqfvewew.io\r\n" + "item1.X-ABLabel:_$!<Other>!$_\r\n" + "TEL;type=WORK;type=FAX;type=pref:+49 (0) 511 86056-595\r\n" + "TEL;type=CELL;type=VOICE:+49 (0) 172 18 984 04\r\n" + "ADR;type=WORK;type=pref:;;Acme Testdorf GmbH;;;;\r\n" + "NOTE:Tester Herbert\nVerkaufsberater Gro\u00dfabnehmer\n\nTelefon: +49 (0) 511 86056-577\n\r\n" + "URL;type=WORK;type=pref:www.ncvspcvswfw-wqfvewew.io\r\n" + "Horstaheimer Stra\u221a\u00fce 303\nD 72942 Testdorf\nDeutschland\r\n" + "tel;charset=utf-8;type=work:+49 (0) 511 86056-577\r\n" + "Horstaheimer Stra\u221a\u00fce 303\n\r\n" + "D 72942 Testdorf\nwww.ncvspcvswfw-wqfvewew.io <http://www.ncvspcvswfw-wqfvewew.io>\n\n \n\r\n" + "Horstaheimer Stra\u221a\u00fce 303;Testdorf;;D 72942;Deutschland\r\n" + "label;charset=utf-8;type=work:Acme Testdorf GmbH\r\n" + "Horstaheimer Stra\u221a\u00fce 303\nD 72942 Testdorf\nDeutschland\r\n" + "tel;charset=utf-8;type=work:+49 (0) 511 86056-577\r\n" + "Horstaheimer Stra\u221a\u00fce 303\n\r\n" + "D 72942 Testdorf\nwww.ncvspcvswfw-wqfvewew.io <http://www.ncvspcvswfw-wqfvewew.io>\n\n \n\r\n" + "Horstaheimer Stra\u221a\u00fce 303;Testdorf;;D 72942;Deutschland\r\n" + "label;charset=utf-8;type=work:Acme Testdorf GmbH\r\n" + "REV:" + formatAsUTC(new Date()) + "\r\n" + "UID:" + uid + "\r\n" + "END:VCARD\r\n";
        postVCard(vCard, 0);
        /*
         * check the contact was created on server
         */
        Contact contact = getContact(uid);
        assertNotNull(contact);
        assertEquals("Tester", contact.getGivenName());
        assertEquals("Herbert", contact.getSurName());
        /*
         * verify contact on client
         */
        Map<String, String> eTags = syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = addressbookMultiget(eTags.keySet());
        VCardResource contactCard = assertContains(uid, addressData);
        assertEquals("Tester", contactCard.getGivenName());
        assertEquals("Herbert", contactCard.getFamilyName());
    }

}
