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
import static org.junit.Assert.assertTrue;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import com.google.api.client.util.Objects;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.dav.carddav.UserAgents;
import com.openexchange.dav.carddav.VCardResource;
import com.openexchange.dav.reports.SyncCollectionResponse;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;

/**
 * {@link DistListTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.4
 */
public class DistListTest extends CardDAVTest {

    @Override
    protected String getDefaultUserAgent() {
        return UserAgents.EM_CLIENT_FOR_APP_SUITE;
    }

    //@Test - not yet supported, as long as UID references are only resolved within the current collection
    public void noTestContactReferencesFromMultipleFolders() throws Exception {
        /*
         * fetch initial sync token
         */
        String collection = String.valueOf(getDefaultFolderID());
        SyncToken syncToken = new SyncToken(fetchSyncToken(collection));
        /*
         * create contact on server in default folder
         */
        String uid1 = randomUID();
        String firstName = "John";
        String lastName = "Doe";
        String email = firstName.toLowerCase() + '.' + lastName.toLowerCase() + "@example.org";
        Contact contact1 = new Contact();
        contact1.setSurName(lastName);
        contact1.setGivenName(firstName);
        contact1.setDisplayName(firstName + " " + lastName);
        contact1.setUid(uid1);
        contact1.setEmail1(email);
        rememberForCleanUp(create(contact1));
        /*
         * lookup "own" user contact in global addressbook
         */
        Contact userContact = com.openexchange.ajax.user.UserTools.getUserContact(getClient(), getClient().getValues().getUserId());
        /*
         * sync client
         */
        SyncCollectionResponse syncCollectionResponse = syncCollection(syncToken, "/carddav/" + collection + "/");
        Map<String, String> eTags = syncCollectionResponse.getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = addressbookMultiget(collection, eTags.keySet());
        assertContains(uid1, addressData);
        /*
         * prepare list vCard referencing both contacts on client
         */
        String uid = randomUID();
        String vCard = // @formatter:off
            "BEGIN:VCARD" + "\r\n" +
            "VERSION:3.0" + "\r\n" +
            "N:list;;;;" + "\r\n" +
            "FN:list" + "\r\n" +
            "UID:" + uid + "\r\n" +
            "X-ADDRESSBOOKSERVER-KIND:GROUP" + "\r\n" +
            "X-ADDRESSBOOKSERVER-MEMBER;X-EMAIL=\"" + contact1.getEmail1() + "\";X-CN=\"" + contact1.getDisplayName() + "\":urn:uuid:" + contact1.getUid() + "\r\n" +
            "X-ADDRESSBOOKSERVER-MEMBER;X-EMAIL=\"" + userContact.getEmail1() + "\";X-CN=\"" + userContact.getDisplayName() + "\":urn:uuid:" + userContact.getUid() + "\r\n" +
            "END:VCARD" + "\r\n"
        ; // @formatter:on
        /*
         * create vCard resource on server
         */
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putVCard(uid, vCard, collection));
        /*
         * get & verify created contact on server
         */
        Contact createdContact = getContact(uid);
        rememberForCleanUp(createdContact);
        assertEquals("uid wrong", uid, createdContact.getUid());
        assertTrue("no distribution list", createdContact.getMarkAsDistribtuionlist());
        assertNotNull("no distribution list", createdContact.getDistributionList());
        assertEquals("unexpected number of members", 2, createdContact.getDistributionList().length);
        DistributionListEntryObject entry1 = null;
        DistributionListEntryObject entryUser = null;
        for (DistributionListEntryObject entry : createdContact.getDistributionList()) {
            if (Objects.equal(entry.getEmailaddress(), contact1.getEmail1())) {
                entry1 = entry;
            }
            if (Objects.equal(entry.getEmailaddress(), userContact.getEmail1())) {
                entryUser = entry;
            }
        }
        assertNotNull("contact not found in list", entry1);
        assertEquals("entry id wrong", contact1.getObjectID(), entry1.getEntryID());
        assertNotNull("user contact not found in list", userContact);
        assertEquals("entry id wrong", userContact.getObjectID(), entryUser.getEntryID());
    }

    @Test
    public void testContactReferencesFromMultipleDuringUpdate() throws Exception {
        /*
         * fetch initial sync token
         */
        String collection = String.valueOf(getDefaultFolderID());
        SyncToken syncToken = new SyncToken(fetchSyncToken(collection));
        /*
         * create contact on server in default folder
         */
        String uid1 = randomUID();
        String firstName = "John";
        String lastName = "Doe";
        String email = firstName.toLowerCase() + '.' + lastName.toLowerCase() + "@example.org";
        Contact contact1 = new Contact();
        contact1.setSurName(lastName);
        contact1.setGivenName(firstName);
        contact1.setDisplayName(firstName + " " + lastName);
        contact1.setUid(uid1);
        contact1.setEmail1(email);
        rememberForCleanUp(create(contact1));
        /*
         * lookup "own" user contact in global addressbook
         */
        Contact userContact = com.openexchange.ajax.user.UserTools.getUserContact(getClient(), getClient().getValues().getUserId());
        /*
         * create distribution list on server containing of these contacts
         */
        Contact distList = new Contact();
        distList.setUid(randomUID());
        distList.setDisplayName("listnameeditme");
        distList.setSurName("listnameeditme");
        DistributionListEntryObject[] dleo = new DistributionListEntryObject[2];
        dleo[0] = new DistributionListEntryObject(contact1.getDisplayName(), contact1.getEmail1(), DistributionListEntryObject.EMAILFIELD1);
        dleo[0].setEntryID(contact1.getObjectID());
        dleo[0].setFolderID(contact1.getParentFolderID());
        dleo[1] = new DistributionListEntryObject(userContact.getDisplayName(), userContact.getEmail1(), DistributionListEntryObject.EMAILFIELD1);
        dleo[1].setEntryID(userContact.getObjectID());
        dleo[1].setFolderID(userContact.getParentFolderID());
        distList.setDistributionList(dleo);
        rememberForCleanUp(create(distList));
        /*
         * sync client
         */
        SyncCollectionResponse syncCollectionResponse = syncCollection(syncToken, "/carddav/" + collection + "/");
        Map<String, String> eTags = syncCollectionResponse.getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = addressbookMultiget(collection, eTags.keySet());
        assertContains(uid1, addressData);
        VCardResource distlistResource = assertContains(distList.getUid(), addressData);
        /*
         * update name of dist list on client
         */
        String updatedVCard = distlistResource.getVCardString().replaceAll("listnameeditme", "listnameedited");
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putVCardUpdate(distlistResource.getUID(), updatedVCard, collection, distlistResource.getETag()));
        /*
         * verify updated list on server
         */
        Contact updatedDistlist = getContact(distList.getUid());
        rememberForCleanUp(updatedDistlist);
        assertTrue("no distribution list", updatedDistlist.getMarkAsDistribtuionlist());
        assertNotNull("no distribution list", updatedDistlist.getDistributionList());
        assertEquals("unexpected number of members", 2, updatedDistlist.getDistributionList().length);
        DistributionListEntryObject entry1 = null;
        DistributionListEntryObject entryUser = null;
        for (DistributionListEntryObject entry : updatedDistlist.getDistributionList()) {
            if (Objects.equal(entry.getEmailaddress(), contact1.getEmail1())) {
                entry1 = entry;
            }
            if (Objects.equal(entry.getEmailaddress(), userContact.getEmail1())) {
                entryUser = entry;
            }
        }
        assertNotNull("contact not found in list", entry1);
        assertEquals("entry id wrong", contact1.getObjectID(), entry1.getEntryID());
        assertNotNull("user contact not found in list", userContact);
        assertEquals("entry id wrong", userContact.getObjectID(), entryUser.getEntryID());
    }

}
