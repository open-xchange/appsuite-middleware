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
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.dav.carddav.UserAgents;
import com.openexchange.dav.carddav.VCardResource;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link Bug38550Test}
 *
 * new contacts added via iOS will be added to the collected addresses folder.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug38550Test extends CardDAVTest {

    public Bug38550Test() {
        super();
    }

    @Test
    public void testCreateInOtherFolder() throws Exception {
        for (String userAgent : UserAgents.IOS_ALL) {
            testCreateInOtherFolder(userAgent);
        }
    }

    private void testCreateInOtherFolder(String userAgent) throws Exception {
        /*
         * apply user agent
         */
        getWebDAVClient().setUserAgent(userAgent);
        String gabCollection = String.valueOf(FolderObject.SYSTEM_LDAP_FOLDER_ID);
        String defaultCollection = String.valueOf(getDefaultFolderID());
        /*
         * fetch sync tokens for later synchronization
         */
        String gabSyncToken = fetchSyncToken(gabCollection);
        String defaultSyncToken = fetchSyncToken(defaultCollection);
        /*
         * try & create contact in global address book
         */
        String uid = randomUID();
        String firstName = "test";
        String lastName = "test";
        String url = "http://";
        String vCard = "BEGIN:VCARD" + "\r\n" + "VERSION:3.0" + "\r\n" + "N:" + lastName + ";" + firstName + ";;;" + "\r\n" + "FN:" + firstName + " " + lastName + "\r\n" + "URL:" + url + "\r\n" + "UID:" + uid + "\r\n" + "REV:" + super.formatAsUTC(new Date()) + "\r\n" + "PRODID:-//Apple Inc.//AddressBook 6.1//EN" + "\r\n" + "END:VCARD" + "\r\n";
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putVCard(uid, vCard, gabCollection));
        /*
         * verify contact on server
         */
        Contact contact = getContact(uid);
        rememberForCleanUp(contact);
        assertEquals("uid wrong", uid, contact.getUid());
        assertEquals("url wrong", url, contact.getURL());
        assertEquals("folder wrong", getDefaultFolderID(), contact.getParentFolderID());
        /*
         * verify contact not created in global addressbook collection on client
         */
        Map<String, String> eTags = syncCollection(gabCollection, gabSyncToken);
        assertTrue("resource changes reported on sync collection", 0 == eTags.size());
        /*
         * verify contact appears in default folder instead
         */
        eTags = super.syncCollection(defaultCollection, defaultSyncToken);
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = addressbookMultiget(defaultCollection, eTags.keySet());
        VCardResource card = assertContains(uid, addressData);
        assertEquals("N wrong", firstName, card.getGivenName());
        assertEquals("N wrong", lastName, card.getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, card.getFN());
        assertNotNull("URL wrong", card.getVCard().getUrls().get(0));
    }

}
