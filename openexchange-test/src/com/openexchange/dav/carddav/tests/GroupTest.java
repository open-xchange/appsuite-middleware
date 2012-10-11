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

package com.openexchange.dav.carddav.tests;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.dav.carddav.VCardResource;
import com.openexchange.groupware.container.Contact;

/**
 * {@link GroupTest} - Tests group handling via the CardDAV interface 
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class GroupTest extends CardDAVTest {

	public GroupTest(String name) {
		super(name);
	}	
	
	public void testCreateGroupOnClient() throws Exception {
		/*
		 * fetch sync token for later synchronization
		 */
		String syncToken = super.fetchSyncToken();
		/*
		 * create folder group
		 */
    	String uid = randomUID();
    	String listName = "testlist_" + uid;
        String vCard =
        		"BEGIN:VCARD" + "\r\n" +
   				"VERSION:3.0" + "\r\n" +
				"PRODID:-//Apple Inc.//AddressBook 6.0//EN" + "\r\n" +
   				"N:" + listName + "\r\n" +
				"FN:" + listName + "\r\n" +
        		"X-ADDRESSBOOKSERVER-KIND:group" + "\r\n" +
				"REV:" + super.formatAsUTC(new Date()) + "\r\n" +
				"UID:" + uid + "\r\n" +
				"END:VCARD" + "\r\n"
		;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putVCard(uid, vCard));
        /*
         * verify distribution list on server
         */
        Contact distributionList = super.getContact(uid);
        assertNotNull("distribution list not found on server", distributionList);
        super.rememberForCleanUp(distributionList);        
        assertEquals("listname wrong", listName, distributionList.getDisplayName());
        /*
         * verify group on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken);
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = super.addressbookMultiget(eTags.keySet());
        VCardResource card = assertContains(uid, addressData);
        assertEquals("FN wrong", listName, card.getFN());
	}
	
	public void testUpdateGroupOnClient() throws Exception {
		/*
		 * fetch sync token for later synchronization
		 */
		SyncToken syncToken = new SyncToken(super.fetchSyncToken());		
		/*
		 * create group on client
		 */
    	String uid = randomUID();
    	String listName = "testlist_" + uid;
        String vCard =
        		"BEGIN:VCARD" + "\r\n" +
   				"VERSION:3.0" + "\r\n" +
				"PRODID:-//Apple Inc.//AddressBook 6.0//EN" + "\r\n" +
   				"N:" + listName + "\r\n" +
				"FN:" + listName + "\r\n" +
        		"X-ADDRESSBOOKSERVER-KIND:group" + "\r\n" +
				"REV:" + super.formatAsUTC(new Date()) + "\r\n" +
				"UID:" + uid + "\r\n" +
				"END:VCARD" + "\r\n"
		;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putVCard(uid, vCard));
        /*
         * verify distribution list on server
         */
        Contact distributionList = super.getContact(uid);
        assertNotNull("distribution list not found on server", distributionList);
        super.rememberForCleanUp(distributionList);        
        assertEquals("listname wrong", listName, distributionList.getDisplayName());
        /*
         * verify group on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = super.addressbookMultiget(eTags.keySet());
        VCardResource card = assertContains(uid, addressData);
        assertEquals("FN wrong", listName, card.getFN());
        /*
         * edit group on client
         */
        String updatedListName = listName + "_changed";
    	card.getVCard().getFormattedName().setFormattedName(updatedListName);
		assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putVCardUpdate(card.getUID(), card.toString(), card.getETag()));
        /*
         * verify updated distribution list on server
         */
        distributionList = super.getContact(uid);
        assertNotNull("distribution list not found on server", distributionList);
        super.rememberForCleanUp(distributionList);        
        assertEquals("listname wrong", updatedListName, distributionList.getDisplayName());
        /*
         * verify updated group on client
         */
        eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        addressData = super.addressbookMultiget(eTags.keySet());
        assertNotContainsFN(listName, addressData);
        card = assertContains(uid, addressData);
        assertEquals("FN wrong", updatedListName, card.getFN());	
    }
	
	public void testCreateGroupOnServer() throws Exception {
		/*
		 * fetch sync token for later synchronization
		 */
		SyncToken syncToken = new SyncToken(super.fetchSyncToken());		
		/*
		 * create distribution list on server
		 */
    	String listUid = randomUID();
    	String listName = "test list";
		Contact distributionList = new Contact();
		distributionList.setDisplayName(listName);
		distributionList.setUid(listUid);
		distributionList.setMarkAsDistributionlist(true);
		super.rememberForCleanUp(super.create(distributionList));
        /*
         * verify group on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = super.addressbookMultiget(eTags.keySet());
        VCardResource card = assertContains(listUid, addressData);
        assertEquals("FN wrong", listName, card.getFN());
        assertTrue("card doesn't represent a group", card.isGroup());
	}
	
	public void testUpdateGroupOnServer() throws Exception {
		/*
		 * fetch sync token for later synchronization
		 */
		SyncToken syncToken = new SyncToken(super.fetchSyncToken());		
		/*
		 * create distribution list on server
		 */
    	String listUid = randomUID();
    	String listName = "test list 77";
		Contact distributionList = new Contact();
		distributionList.setDisplayName(listName);
		distributionList.setUid(listUid);
		distributionList.setMarkAsDistributionlist(true);
		super.rememberForCleanUp(super.create(distributionList));
        /*
         * verify group on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = super.addressbookMultiget(eTags.keySet());
        VCardResource card = assertContains(listUid, addressData);
        assertEquals("FN wrong", listName, card.getFN());
        assertTrue("card doesn't represent a group", card.isGroup());
        /*
         * update list on server
         */
        String updatedListName = listName + "_changed";
        distributionList.setDisplayName(updatedListName);
        distributionList = super.update(distributionList);
        /*
         * verify updated group on client
         */
        eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        addressData = super.addressbookMultiget(eTags.keySet());
        card = assertContains(listUid, addressData);
        assertEquals("FN wrong", updatedListName, card.getFN());	
        assertTrue("card doesn't represent a group", card.isGroup());
	}

}
