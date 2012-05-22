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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.carddav.tests;

import java.util.List;
import java.util.Map;

import com.openexchange.carddav.CardDAVTest;
import com.openexchange.carddav.StatusCodes;
import com.openexchange.carddav.SyncToken;
import com.openexchange.carddav.ThrowableHolder;
import com.openexchange.carddav.VCardResource;
import com.openexchange.carddav.reports.SyncCollectionResponse;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link DeleteTest} - Tests various delete operations via the CardDAV interface 
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DeleteTest extends CardDAVTest {

	public DeleteTest(String name) {
		super(name);
	}	

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
        assertEquals("N wrong", firstName, contactCard.getVCard().getName().getGivenName());
        assertEquals("N wrong", lastName, contactCard.getVCard().getName().getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, contactCard.getVCard().getFormattedName().getFormattedName());
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
        assertEquals("N wrong", firstName, contactCard.getVCard().getName().getGivenName());
        assertEquals("N wrong", lastName, contactCard.getVCard().getName().getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, contactCard.getVCard().getFormattedName().getFormattedName());
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
        assertEquals("N wrong", firstName, card.getVCard().getName().getGivenName());
        assertEquals("N wrong", lastName, card.getVCard().getName().getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, card.getVCard().getFormattedName().getFormattedName());
        /*
         * delete contact on client
         */
        assertEquals("response code wrong", StatusCodes.SC_OK, delete(uid));
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
	
	public void testDeleteContactInGroupOnClient() throws Throwable {
		/*
		 * fetch sync token for later synchronization
		 */
		SyncToken syncToken = new SyncToken(super.fetchSyncToken());		
		/*
		 * create contacts server
		 */
    	String contactUid = randomUID();
    	String firstName = "test";
    	String lastName = "klaus";    	
    	String email = "test.klaus@example.org";    	
		Contact contact = new Contact();
		contact.setSurName(lastName);
		contact.setGivenName(firstName);
		contact.setDisplayName(firstName + " " + lastName);
		contact.setEmail1(email);
		contact.setUid(contactUid);
		super.rememberForCleanUp(super.create(contact));
		Contact otherContact = new Contact();
		otherContact.setSurName("Anders");
		otherContact.setGivenName("Otto");
		super.rememberForCleanUp(super.create(otherContact));
		Contact furtherContact = new Contact();
		furtherContact.setSurName("Anders");
		furtherContact.setGivenName("Horst");
		super.rememberForCleanUp(super.create(furtherContact));
		/*
		 * create distribution list on server
		 */
    	String listUid = randomUID();
    	String listName = "test distribution list";
		Contact distributionList = new Contact();
		distributionList.setDisplayName(listName);
		distributionList.setUid(listUid);
		distributionList.setMarkAsDistributionlist(true);
		DistributionListEntryObject[] members = new DistributionListEntryObject[3];
		members[0] = asDistListMember(otherContact);
		members[1] = asDistListMember(contact);
		members[2] = asDistListMember(furtherContact);
		distributionList.setDistributionList(members);
		super.rememberForCleanUp(super.create(distributionList));
        /*
         * verify contact and group on client
         */
		Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = super.addressbookMultiget(eTags.keySet());
        VCardResource card = assertContains(contactUid, addressData);
        assertEquals("N wrong", firstName, card.getVCard().getName().getGivenName());
        assertEquals("N wrong", lastName, card.getVCard().getName().getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, card.getVCard().getFormattedName().getFormattedName());
        final VCardResource groupVCard = assertContains(listUid, addressData);
        assertEquals("FN wrong", listName, groupVCard.getFN());
        assertContainsMemberUID(contactUid, groupVCard);
        /*
         * delete contact on client, performing contact deletion and group update on different threads to simulate concurrent requests 
         * of the client 
         */
        groupVCard.getVCard().removeExtendedType(groupVCard.getMemberXFeature(contactUid));
        final ThrowableHolder throwableHolder = new ThrowableHolder(); 
        Thread updateThread = new Thread() {
        	@Override
            public void run() {
                try {
					assertEquals("response code wrong", StatusCodes.SC_CREATED, putVCardUpdate(groupVCard.getUID(), groupVCard.toString(), 
							groupVCard.getETag()));
				} catch (Throwable t) {
					throwableHolder.setThrowable(t);
				}
    		}
        };
        updateThread.start();
        assertEquals("response code wrong", StatusCodes.SC_OK, delete(contactUid));
        updateThread.join();
        throwableHolder.reThrowIfSet();
        /*
         * verify deletion on server
         */
        assertNull("contact not deleted on server", super.getContact(contactUid));
        distributionList = super.getContact(listUid);
        assertNotNull("distribution list not found on server", distributionList);
        assertEquals("uid wrong", listUid, distributionList.getUid());
        assertEquals("displayname wrong", listName, distributionList.getDisplayName());
        assertNotNull("no members in distribution list", distributionList.getDistributionList());
        assertTrue("invalid member count in distribution list", 2 == distributionList.getNumberOfDistributionLists());
        assertTrue("invalid member count in distribution list", 2 == distributionList.getDistributionList().length);
        for (DistributionListEntryObject member : distributionList.getDistributionList()) {
        	assertFalse("deleted member still in list", email.equals(member.getEmailaddress()));
        }
        /*
         * verify deletion on client        
         */
        SyncCollectionResponse syncCollectionResponse = super.syncCollection(syncToken);
        assertTrue("no resource deletions reported on sync collection", 0 < syncCollectionResponse.getHrefsStatusNotFound().size());
        eTags = syncCollectionResponse.getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        addressData = super.addressbookMultiget(eTags.keySet());
        VCardResource groupCard = assertContains(listUid, addressData);
        assertNotContainsMemberUID(contactUid, groupCard);
    }

	public void testRemoveContactFromGroupOnClient() throws Throwable {
		/*
		 * fetch sync token for later synchronization
		 */
		SyncToken syncToken = new SyncToken(super.fetchSyncToken());		
		/*
		 * create contacts server
		 */
    	String contactUid = randomUID();
    	String firstName = "test";
    	String lastName = "heinz";    	
    	String email = "test.heinz@example.org";    	
		Contact contact = new Contact();
		contact.setSurName(lastName);
		contact.setGivenName(firstName);
		contact.setDisplayName(firstName + " " + lastName);
		contact.setEmail1(email);
		contact.setUid(contactUid);
		super.rememberForCleanUp(super.create(contact));
		Contact otherContact = new Contact();
		otherContact.setSurName("Harry");
		otherContact.setGivenName("Lustig");
		super.rememberForCleanUp(super.create(otherContact));
		Contact furtherContact = new Contact();
		furtherContact.setSurName("Heiner");
		furtherContact.setGivenName("Heiter");
		super.rememberForCleanUp(super.create(furtherContact));
		/*
		 * create distribution list on server
		 */
    	String listUid = randomUID();
    	String listName = "test list";
		Contact distributionList = new Contact();
		distributionList.setDisplayName(listName);
		distributionList.setUid(listUid);
		distributionList.setMarkAsDistributionlist(true);
		DistributionListEntryObject[] members = new DistributionListEntryObject[3];
		members[0] = asDistListMember(otherContact);
		members[1] = asDistListMember(contact);
		members[2] = asDistListMember(furtherContact);
		distributionList.setDistributionList(members);
		super.rememberForCleanUp(super.create(distributionList));
        /*
         * verify contact and group on client
         */
		Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = super.addressbookMultiget(eTags.keySet());
        VCardResource card = assertContains(contactUid, addressData);
        assertEquals("N wrong", firstName, card.getVCard().getName().getGivenName());
        assertEquals("N wrong", lastName, card.getVCard().getName().getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, card.getVCard().getFormattedName().getFormattedName());
        final VCardResource groupVCard = assertContains(listUid, addressData);
        assertEquals("FN wrong", listName, groupVCard.getFN());
        assertContainsMemberUID(contactUid, groupVCard);
        /*
         * remove contact from group on client
         */
        groupVCard.getVCard().removeExtendedType(groupVCard.getMemberXFeature(contactUid));
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putVCardUpdate(groupVCard.getUID(), groupVCard.toString(), 
							groupVCard.getETag()));
        /*
         * verify removal on server
         */
        assertNotNull("contact deleted on server", super.getContact(contactUid));
        distributionList = super.getContact(listUid);
        assertNotNull("distribution list not found on server", distributionList);
        assertEquals("uid wrong", listUid, distributionList.getUid());
        assertEquals("displayname wrong", listName, distributionList.getDisplayName());
        assertNotNull("no members in distribution list", distributionList.getDistributionList());
        assertTrue("invalid member count in distribution list", 2 == distributionList.getNumberOfDistributionLists());
        assertTrue("invalid member count in distribution list", 2 == distributionList.getDistributionList().length);
        for (DistributionListEntryObject member : distributionList.getDistributionList()) {
        	assertFalse("removed member still in list", email.equals(member.getEmailaddress()));
        }
        /*
         * verify removal on client        
         */
        SyncCollectionResponse syncCollectionResponse = super.syncCollection(syncToken);
        assertTrue("deletions were reported on sync collection", 0 == syncCollectionResponse.getHrefsStatusNotFound().size());
        eTags = syncCollectionResponse.getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        addressData = super.addressbookMultiget(eTags.keySet());
        VCardResource groupCard = assertContains(listUid, addressData);
        assertNotContainsMemberUID(contactUid, groupCard);
    }

}
