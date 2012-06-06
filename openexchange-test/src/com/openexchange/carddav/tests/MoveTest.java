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

import net.sourceforge.cardme.vcard.types.ExtendedType;

import com.openexchange.carddav.CardDAVTest;
import com.openexchange.carddav.StatusCodes;
import com.openexchange.carddav.SyncToken;
import com.openexchange.carddav.VCardResource;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link MoveTest} - Tests various move operations via the CardDAV interface 
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class MoveTest extends CardDAVTest {

	public MoveTest(String name) {
		super(name);
	}	

	public void testMoveContactToSubfolderOnServer() throws Exception {
		/*
		 * fetch sync token for later synchronization
		 */
		SyncToken syncToken = new SyncToken(super.fetchSyncToken());		
		/*
		 * create subfolder and contact on server
		 */
    	String subFolderName = "testfolder_" + randomUID();
    	FolderObject subFolder = super.createFolder(subFolderName);
		super.rememberForCleanUp(subFolder);
    	String uid = randomUID();
    	String firstName = "test";
    	String lastName = "jaqueline";    	
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
         * move contact on server
         */
        contact.setParentFolderID(subFolder.getObjectID());
        super.update(super.getDefaultFolder().getObjectID(), contact);
        /*
         * verify contact on client        
         */
        eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        addressData = super.addressbookMultiget(eTags.keySet());
        contactCard = assertContains(uid, addressData);
        assertEquals("N wrong", firstName, contactCard.getVCard().getName().getGivenName());
        assertEquals("N wrong", lastName, contactCard.getVCard().getName().getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, contactCard.getVCard().getFormattedName().getFormattedName());
    }

	public void testMoveContactToDefaultFolderOnServer() throws Exception {
		/*
		 * fetch sync token for later synchronization
		 */
		SyncToken syncToken = new SyncToken(super.fetchSyncToken());		
		/*
		 * create subfolder and contact on server
		 */
    	String subFolderName = "testfolder_" + randomUID();
    	FolderObject subFolder = super.createFolder(subFolderName);
		super.rememberForCleanUp(subFolder);
    	String uid = randomUID();
    	String firstName = "test";
    	String lastName = "jaqueline";    	
		Contact contact = new Contact();
		contact.setSurName(lastName);
		contact.setGivenName(firstName);
		contact.setDisplayName(firstName + " " + lastName);
		contact.setUid(uid);
		super.rememberForCleanUp(super.create(contact, subFolder.getObjectID()));
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
         * move contact on server
         */
        contact.setParentFolderID(super.getDefaultFolder().getObjectID());
        super.update(subFolder.getObjectID(), contact);
        contact.setParentFolderID(subFolder.getObjectID());
        /*
         * verify contact on client        
         */
        eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        addressData = super.addressbookMultiget(eTags.keySet());
        contactCard = assertContains(uid, addressData);
        assertEquals("N wrong", firstName, contactCard.getVCard().getName().getGivenName());
        assertEquals("N wrong", lastName, contactCard.getVCard().getName().getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, contactCard.getVCard().getFormattedName().getFormattedName());
    }

	public void testMoveContactToGroupOnClient() throws Exception {
		/*
		 * fetch sync token for later synchronization
		 */
		SyncToken syncToken = new SyncToken(super.fetchSyncToken());		
		/*
		 * create distribution list on server
		 */
    	String listUid = randomUID();
    	String listName = "liste6546";
		Contact distributionList = new Contact();
		distributionList.setDisplayName(listName);
		distributionList.setUid(listUid);
		distributionList.setMarkAsDistributionlist(true);
		super.rememberForCleanUp(super.create(distributionList));
		/*
		 * create contact on server
		 */
    	String uid = randomUID();
    	String firstName = "Waldo";
    	String lastName = "Nacktnasenwombat";
    	String email = "nacktnasenwombat@example.com";
		Contact contact = new Contact();
		contact.setEmail1(email);
		contact.setSurName(lastName);
		contact.setGivenName(firstName);
		contact.setDisplayName(firstName + " " + lastName);
		contact.setUid(uid);
		super.rememberForCleanUp(super.create(contact));
        /*
         * verify contact and group on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = super.addressbookMultiget(eTags.keySet());
        VCardResource groupCard = assertContains(listUid, addressData);
        assertNotContainsMemberUID(uid, groupCard);
        assertEquals("FN wrong", listName, groupCard.getVCard().getFormattedName().getFormattedName());
        VCardResource contactCard = assertContains(uid, addressData);
        assertEquals("N wrong", firstName, contactCard.getVCard().getName().getGivenName());
        assertEquals("N wrong", lastName, contactCard.getVCard().getName().getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, contactCard.getVCard().getFormattedName().getFormattedName());
        /*
         * move contact to group on client
         */
        ExtendedType newMember = new ExtendedType("X-ADDRESSBOOKSERVER-MEMBER", "urn:uuid:" + uid);
        groupCard.getVCard().addExtendedType(newMember);
		assertEquals("response code wrong", StatusCodes.SC_CREATED, putVCardUpdate(groupCard.getUID(), 
				groupCard.toString(), groupCard.getETag()));
		/*
		 * verify list on server
		 */
        distributionList = super.getContact(listUid);
        assertNotNull("distribution list not found on server", distributionList);
        assertEquals("uid wrong", listUid, distributionList.getUid());
        assertEquals("displayname wrong", listName, distributionList.getDisplayName());
        assertTrue("list not marked as distribution list", distributionList.getMarkAsDistribtuionlist());
        assertNotNull("no members in distribution list", distributionList.getDistributionList());
        assertTrue("invalid member count in distribution list", 1 == distributionList.getNumberOfDistributionLists());
        assertTrue("invalid member count in distribution list", 1 == distributionList.getDistributionList().length);
        DistributionListEntryObject member = distributionList.getDistributionList()[0];
        assertNotNull("no member in distribution list", member);
        assertEquals("email wrong", email, member.getEmailaddress());
        /*
         * verify move on client        
         */
        eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        addressData = super.addressbookMultiget(eTags.keySet());
        groupCard = assertContains(listUid, addressData);
        assertContainsMemberUID(uid, groupCard);
    }

	public void testMoveContactToOtherGroupOnClient() throws Exception {
		/*
		 * fetch sync token for later synchronization
		 */
		SyncToken syncToken = new SyncToken(super.fetchSyncToken());
		/*
		 * create contacts server
		 */
    	String contactUid = randomUID();
    	String firstName = "test";
    	String lastName = "hund";    	
    	String email = "test.hund@example.org";    	
		Contact contact = new Contact();
		contact.setSurName(lastName);
		contact.setGivenName(firstName);
		contact.setDisplayName(firstName + " " + lastName);
		contact.setEmail1(email);
		contact.setUid(contactUid);
		super.rememberForCleanUp(super.create(contact));
		Contact otherContact = new Contact();
		otherContact.setSurName("otto");
		otherContact.setGivenName("kurz");
		super.rememberForCleanUp(super.create(otherContact));
		Contact furtherContact = new Contact();
		furtherContact.setSurName("anton");
		furtherContact.setGivenName("lang");
		super.rememberForCleanUp(super.create(furtherContact));
		/*
		 * create distribution list on server
		 */
    	String list1Uid = randomUID();
    	String list1Name = "liste1";
		Contact distributionList1 = new Contact();
		distributionList1.setDisplayName(list1Name);
		distributionList1.setUid(list1Uid);
		distributionList1.setMarkAsDistributionlist(true);
		DistributionListEntryObject[] members = new DistributionListEntryObject[3];
		members[0] = asDistListMember(otherContact);
		members[1] = asDistListMember(contact);
		members[2] = asDistListMember(furtherContact);
		distributionList1.setDistributionList(members);
		super.rememberForCleanUp(super.create(distributionList1));
    	String list2Uid = randomUID();
    	String list2Name = "liste2";
		Contact distributionList2 = new Contact();
		distributionList2.setDisplayName(list2Name);
		distributionList2.setUid(list2Uid);
		distributionList2.setMarkAsDistributionlist(true);
		super.rememberForCleanUp(super.create(distributionList2));
        /*
         * verify contact and groups on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = super.addressbookMultiget(eTags.keySet());
        VCardResource contactCard = assertContains(contactUid, addressData);
        assertEquals("N wrong", firstName, contactCard.getVCard().getName().getGivenName());
        assertEquals("N wrong", lastName, contactCard.getVCard().getName().getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, contactCard.getVCard().getFormattedName().getFormattedName());
        VCardResource groupCard1 = assertContains(list1Uid, addressData);
        assertEquals("FN wrong", list1Name, groupCard1.getVCard().getFormattedName().getFormattedName());
        assertContainsMemberUID(contactUid, groupCard1);
        VCardResource groupCard2 = assertContains(list2Uid, addressData);
        assertEquals("FN wrong", list2Name, groupCard2.getVCard().getFormattedName().getFormattedName());
        assertNotContainsMemberUID(contactUid, groupCard2);
        /*
         * move contact to group on client
         */
        ExtendedType newMember = new ExtendedType("X-ADDRESSBOOKSERVER-MEMBER", "urn:uuid:" + contactUid);
        groupCard2.getVCard().addExtendedType(newMember);
		assertEquals("response code wrong", StatusCodes.SC_CREATED, putVCardUpdate(groupCard2.getUID(), 
				groupCard2.toString(), groupCard2.getETag()));
		/*
		 * verify list on server
		 */
        distributionList2 = super.getContact(list2Uid);
        assertNotNull("distribution list not found on server", distributionList2);
        assertEquals("uid wrong", list2Uid, distributionList2.getUid());
        assertEquals("displayname wrong", list2Name, distributionList2.getDisplayName());
        assertTrue("list not marked as distribution list", distributionList2.getMarkAsDistribtuionlist());
        assertNotNull("no members in distribution list", distributionList2.getDistributionList());
        assertTrue("invalid member count in distribution list", 1 == distributionList2.getNumberOfDistributionLists());
        assertTrue("invalid member count in distribution list", 1 == distributionList2.getDistributionList().length);
        DistributionListEntryObject member = distributionList2.getDistributionList()[0];
        assertNotNull("no member in distribution list", member);
        assertEquals("email wrong", email, member.getEmailaddress());
        /*
         * verify move on client        
         */
        eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        addressData = super.addressbookMultiget(eTags.keySet());
        groupCard2 = assertContains(list2Uid, addressData);
        assertContainsMemberUID(contactUid, groupCard2);
    }

}
