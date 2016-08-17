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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.dav.carddav.VCardResource;
import com.openexchange.dav.reports.SyncCollectionResponse;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link DeleteTest} - Tests various delete operations via the CardDAV interface
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DeleteTest extends CardDAVTest {

	public DeleteTest() {
		super();
	}

	@Test
	public void testDeleteContactOnServer() throws Exception {
		/*
		 * fetch sync token for later synchronization
		 */
		SyncToken syncToken = new SyncToken(super.fetchSyncToken());
		/*
		 * create contact on server
		 */
    	String uid = randomUID();
    	String firstName = "test";
    	String lastName = "banane";
		Contact contact = new Contact();
		contact.setSurName(lastName);
		contact.setGivenName(firstName);
		contact.setDisplayName(firstName + " " + lastName);
		contact.setUid(uid);
		super.rememberForCleanUp(super.create(contact));
        /*
         * verify contact on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = super.addressbookMultiget(eTags.keySet());
        VCardResource contactCard = assertContains(uid, addressData);
        assertEquals("N wrong", firstName, contactCard.getGivenName());
        assertEquals("N wrong", lastName, contactCard.getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, contactCard.getFN());
        /*
         * delete contact on server
         */
        super.delete(contact);
        /*
         * verify deletion on client
         */
        SyncCollectionResponse syncCollectionResponse = super.syncCollection(syncToken);
        assertTrue("no resource deletions reported on sync collection", 0 < syncCollectionResponse.getHrefsStatusNotFound().size());
        eTags = syncCollectionResponse.getETagsStatusOK();
    }

	@Test
	public void testDeleteContactInSubfolderOnServer() throws Exception {
		/*
		 * fetch sync token for later synchronization
		 */
		SyncToken syncToken = new SyncToken(super.fetchSyncToken());
		/*
		 * create folder and contact on server
		 */
    	String folderName = "testfolder_" + randomUID();
    	FolderObject folder = super.createFolder(folderName);
		super.rememberForCleanUp(folder);
    	String uid = randomUID();
    	String firstName = "test";
    	String lastName = "otto";
		Contact contact = new Contact();
		contact.setSurName(lastName);
		contact.setGivenName(firstName);
		contact.setDisplayName(firstName + " " + lastName);
		contact.setUid(uid);
		super.rememberForCleanUp(super.create(contact, folder.getObjectID()));
        /*
         * verify contact and folder group on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = super.addressbookMultiget(eTags.keySet());
        VCardResource contactCard = assertContains(uid, addressData);
        assertEquals("N wrong", firstName, contactCard.getGivenName());
        assertEquals("N wrong", lastName, contactCard.getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, contactCard.getFN());
        /*
         * delete contact on server
         */
        super.delete(contact);
        /*
         * verify deletion on client
         */
        SyncCollectionResponse syncCollectionResponse = super.syncCollection(syncToken);
        assertTrue("no resource deletions reported on sync collection", 0 < syncCollectionResponse.getHrefsStatusNotFound().size());
    }

	@Test
	public void testDeleteContactOnClient() throws Throwable {
		/*
		 * fetch sync token for later synchronization
		 */
		SyncToken syncToken = new SyncToken(super.fetchSyncToken());
		/*
		 * create contact on server
		 */
    	String uid = randomUID();
    	String firstName = "test";
    	String lastName = "manfred";
		Contact contact = new Contact();
		contact.setSurName(lastName);
		contact.setGivenName(firstName);
		contact.setDisplayName(firstName + " " + lastName);
		contact.setUid(uid);
		super.rememberForCleanUp(super.create(contact));
        /*
         * verify contact and folder group on client
         */
		Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = super.addressbookMultiget(eTags.keySet());
        VCardResource card = assertContains(uid, addressData);
        assertEquals("N wrong", firstName, card.getGivenName());
        assertEquals("N wrong", lastName, card.getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, card.getFN());
        /*
         * delete contact on client
         */
        assertEquals("response code wrong", StatusCodes.SC_NO_CONTENT, delete(uid));
        /*
         * verify deletion on server
         */
        assertNull("contact not deleted on server", super.getContact(uid));
        /*
         * verify deletion on client
         */
        SyncCollectionResponse syncCollectionResponse = super.syncCollection(syncToken);
        assertTrue("no resource deletions reported on sync collection", 0 < syncCollectionResponse.getHrefsStatusNotFound().size());
        eTags = syncCollectionResponse.getETagsStatusOK();
    }

}
