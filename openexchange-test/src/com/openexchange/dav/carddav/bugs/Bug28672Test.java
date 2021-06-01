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
 * {@link Bug28672Test}
 *
 * card contacts imported in MAC addressbook do not sync with App Suite addressbook.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug28672Test extends CardDAVTest {

    public Bug28672Test() {
        super();
    }

    @Test
    public void testImportVCard() throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        String syncToken = super.fetchSyncToken();
        /*
         * create contact
         */
        String uid = randomUID() + "-ABSPlugin";
        String vCard = "BEGIN:VCARD" + "\r\n" + "VERSION:3.0" + "\r\n" + "N:sdfsd;sdfsdf;;kulesh;" + "\r\n" + "FN:kulesh sdfsdf sdfsd" + "\r\n" + "EMAIL;type=INTERNET;type=WORK;type=pref:sdf@gmail.com" + "\r\n" + "NOTE:X-OPEN-XCHANGE-CTYPE\\: contact" + "\r\n" + "X-ABUID:4D863654-A664-4196-AFAF-0C3F93F07F12\\:ABPerson" + "\r\n" + "UID:" + uid + "\r\n" + "REV:2013-09-03T06:01:39Z" + "\r\n" + "END:VCARD" + "\r\n";
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putVCard(uid, vCard));
        /*
         * verify contact on server
         */
        Contact contact = super.getContact(uid);
        super.rememberForCleanUp(contact);
        assertEquals("uid wrong", uid, contact.getUid());
        /*
         * verify contact on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken);
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = super.addressbookMultiget(eTags.keySet());
        assertContains(uid, addressData);
    }

}
