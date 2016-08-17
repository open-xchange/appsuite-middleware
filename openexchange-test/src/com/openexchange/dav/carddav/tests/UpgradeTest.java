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

import static org.junit.Assert.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.ThrowableHolder;
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.dav.carddav.UserAgents;
import com.openexchange.dav.carddav.VCardResource;
import com.openexchange.dav.reports.SyncCollectionResponse;
import com.openexchange.groupware.container.Contact;

/**
 * {@link UpgradeTest} - Tests upgrades of the server with changed handling
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class UpgradeTest extends CardDAVTest {

	public UpgradeTest() {
		super();
	}

	@Test
	public void testUpgradeWithGAB_10_6() throws Throwable {
		super.getWebDAVClient().setUserAgent(UserAgents.MACOS_10_6_8);
		/*
		 * assume the following uids from previous syncs
		 */
    	final String contactsGroupUid = "f" + super.getClient().getValues().getContextId() + "_" + super.getDefaultFolderID();
    	final String gabGroupUid = "f" + super.getClient().getValues().getContextId() + "_" + super.getGABFolderID();
		/*
		 * get a ctag
		 */
		String cTag = super.getCTag();
		/*
		 * perform a client operation (create contact in default "Contacts" group)
		 */
    	final String contactUidPath = randomUID() + "-ABSPlugin";
    	String contactUid = randomUID() + "-ABSPlugin";
    	String firstName = "upgrade";
    	String lastName = "test";
    	String email = "upgrader@example.com";
        final String contactVCard =
        		"BEGIN:VCARD" + "\r\n" +
   				"VERSION:3.0" + "\r\n" +
				"N:" + lastName + ";" + firstName + ";;;" + "\r\n" +
				"FN:" + firstName + " " + lastName + "\r\n" +
				"ORG:test88;" + "\r\n" +
				"EMAIL;type=INTERNET;type=WORK;type=pref:" + email + "\r\n" +
				"TEL;type=WORK;type=pref:24235423" + "\r\n" +
				"TEL;type=CELL:352-3534" + "\r\n" +
				"TEL;type=HOME:547547" + "\r\n" +
				"UID:" + contactUid + "\r\n" +
				"REV:" + super.formatAsUTC(new Date()) + "\r\n" +
				"PRODID:-//Apple Inc.//AddressBook 6.0//EN" + "\r\n" +
				"END:VCARD" + "\r\n"
		;
        final String groupVCard =
        		"BEGIN:VCARD" + "\r\n" +
   				"VERSION:3.0" + "\r\n" +
				"PRODID:-//Apple Inc.//AddressBook 6.0//EN" + "\r\n" +
   				"N: Contacts" + "\r\n" +
				"FN: Contacts" + "\r\n" +
				"X-ADDRESSBOOKSERVER-MEMBER:urn:uuid:B1A4E8BE-F6AA-4193-932B-14BF7500D76A-ABSPlugin" + "\r\n" +
				"X-ADDRESSBOOKSERVER-MEMBER:urn:uuid:4d90f3ef-b438-4878-b977-16550809312d" + "\r\n" +
				"X-ADDRESSBOOKSERVER-MEMBER:urn:uuid:333e8382-2641-4ee4-9c2a-cd8a7fd8bb2d" + "\r\n" +
				"X-ADDRESSBOOKSERVER-MEMBER:urn:uuid:fe0c922a-aaa0-43d2-9412-edaba49ba683" + "\r\n" +
				"X-ADDRESSBOOKSERVER-MEMBER:urn:uuid:f0597cf8-e59a-4e7c-a925-9244b141d6e3" + "\r\n" +
				"X-ADDRESSBOOKSERVER-MEMBER:urn:uuid:e4e1c694-da0f-4dc2-b220-5d8b7801beae" + "\r\n" +
				"X-ADDRESSBOOKSERVER-MEMBER:urn:uuid:78D1A52B-8069-4008-A780-815EDA87424C-ABSPlugin" + "\r\n" +
				"X-ADDRESSBOOKSERVER-MEMBER:urn:uuid:0b3163b5-9de3-4664-a549-557ec7b0fcc7" + "\r\n" +
				"X-ADDRESSBOOKSERVER-MEMBER:urn:uuid:450abb55-bc9c-4106-8a2d-1f6dc6afbae9" + "\r\n" +
				"X-ADDRESSBOOKSERVER-MEMBER:urn:uuid:93da65d4-ec6c-4211-9bca-8648deebe3e4" + "\r\n" +
				"X-ADDRESSBOOKSERVER-MEMBER:urn:uuid:" + contactUid + "\r\n" +
				"X-ADDRESSBOOKSERVER-MEMBER:urn:uuid:4c9ed4a2-bb4b-4947-919b-5078ebddccb4" + "\r\n" +
				"X-ADDRESSBOOKSERVER-MEMBER:urn:uuid:0067e70e-cbc0-4040-a1cb-8928e7cdf9f9" + "\r\n" +
				"X-ADDRESSBOOKSERVER-MEMBER:urn:uuid:e5c4f3d5-d668-4797-a0c3-0441fbb50cf9" + "\r\n" +
				"X-ADDRESSBOOKSERVER-MEMBER:urn:uuid:d817a3e0-6c6f-4888-85c2-6442bfc05412" + "\r\n" +
        		"X-ADDRESSBOOKSERVER-KIND:group" + "\r\n" +
				"REV:" + super.formatAsUTC(new Date()) + "\r\n" +
				"UID:" + contactsGroupUid + "\r\n" +
				"X-ABUID:05358928-0808-4FAD-A86F-1D9B517C9F78\\:ABGroup" + "\r\n" +
				"END:VCARD" + "\r\n"
		;
        /*
         * perform contact creation and group update on different threads to simulate concurrent requests of the client
         */
        final ThrowableHolder throwableHolder = new ThrowableHolder();
        Thread createThread = new Thread() {
        	@Override
            public void run() {
                try {
	                assertEquals("response code wrong", StatusCodes.SC_CREATED, putVCard(contactUidPath, contactVCard));
				} catch (Throwable t) {
					throwableHolder.setThrowable(t);
				}
    		}
        };
        createThread.start();
		super.putVCardUpdate(contactsGroupUid, groupVCard);
        createThread.join();
        throwableHolder.reThrowIfSet();
        /*
         * verify contact on server
         */
        Contact contact = super.getContact(contactUid);
        super.rememberForCleanUp(contact);
        assertEquals("uid wrong", contactUid, contact.getUid());
        assertEquals("firstname wrong", firstName, contact.getGivenName());
        assertEquals("lastname wrong", lastName, contact.getSurName());
		/*
		 * check for updates via ctag
		 */
		String cTag2 = super.getCTag();
		assertFalse("No changes indicated by CTag", cTag.equals(cTag2));
		/*
		 * get all eTags
		 */
		Map<String, String> eTags = super.getAllETags();
		/*
		 * verify corresponding vCards
		 */
        List<VCardResource> vCards = super.addressbookMultiget(eTags.keySet());
        assertNotContains(contactsGroupUid, vCards);
        assertNotContains(gabGroupUid, vCards);
        assertContains(contactUid, vCards);
	}

	@Test
	public void testUpgradeWithGAB_10_7() throws Throwable {
		super.getWebDAVClient().setUserAgent(UserAgents.MACOS_10_7_2);
		/*
		 * fetch a sync token
		 */
		SyncToken syncToken = new SyncToken(super.fetchSyncToken());
		/*
		 * assume the following uids, hrefs and eTags from previous syncs
		 */
    	final String contactsGroupUid = "f" + super.getClient().getValues().getContextId() + "_" + super.getDefaultFolderID();
    	final String contactsGroupHRef = "http://www.open-xchange.com/carddav/" + contactsGroupUid + ".vcf";
    	final String contactsGroupETag = "http://www.open-xchange.com/carddav/" + contactsGroupUid + "_" + new Date().getTime();
    	final String gabGroupUid = "f" + super.getClient().getValues().getContextId() + "_" + super.getGABFolderID();
    	final String gabGroupHRef = "http://www.open-xchange.com/carddav/" + gabGroupUid + ".vcf";
//    	final String gabGroupETag = "http://www.open-xchange.com/carddav/" + gabGroupUid + "_" + new Date().getTime();
		/*
		 * perform a client operation (create contact in default "Contacts" group)
		 */
    	final String contactUid = randomUID();
    	String firstName = "upgrade";
    	String lastName = "test2";
    	String email = "upgrader2@example.com";
        final String contactVCard =
        		"BEGIN:VCARD" + "\r\n" +
   				"VERSION:3.0" + "\r\n" +
				"N:" + lastName + ";" + firstName + ";;;" + "\r\n" +
				"FN:" + firstName + " " + lastName + "\r\n" +
				"ORG:test88;" + "\r\n" +
				"EMAIL;type=INTERNET;type=WORK;type=pref:" + email + "\r\n" +
				"TEL;type=WORK;type=pref:24235423" + "\r\n" +
				"TEL;type=CELL:352-3534" + "\r\n" +
				"TEL;type=HOME:547547" + "\r\n" +
				"UID:" + contactUid + "\r\n" +
				"REV:" + super.formatAsUTC(new Date()) + "\r\n" +
				"PRODID:-//Apple Inc.//AddressBook 6.0//EN" + "\r\n" +
				"END:VCARD" + "\r\n"
		;
        final String groupVCard =
        		"BEGIN:VCARD" + "\r\n" +
   				"VERSION:3.0" + "\r\n" +
				"PRODID:-//Apple Inc.//AddressBook 6.0//EN" + "\r\n" +
   				"N: Contacts" + "\r\n" +
				"FN: Contacts" + "\r\n" +
				"X-ADDRESSBOOKSERVER-MEMBER:urn:uuid:4d90f3ef-b438-4878-b977-16550809312d" + "\r\n" +
				"X-ADDRESSBOOKSERVER-MEMBER:urn:uuid:333e8382-2641-4ee4-9c2a-cd8a7fd8bb2d" + "\r\n" +
				"X-ADDRESSBOOKSERVER-MEMBER:urn:uuid:fe0c922a-aaa0-43d2-9412-edaba49ba683" + "\r\n" +
				"X-ADDRESSBOOKSERVER-MEMBER:urn:uuid:f0597cf8-e59a-4e7c-a925-9244b141d6e3" + "\r\n" +
				"X-ADDRESSBOOKSERVER-MEMBER:urn:uuid:e4e1c694-da0f-4dc2-b220-5d8b7801beae" + "\r\n" +
				"X-ADDRESSBOOKSERVER-MEMBER:urn:uuid:0b3163b5-9de3-4664-a549-557ec7b0fcc7" + "\r\n" +
				"X-ADDRESSBOOKSERVER-MEMBER:urn:uuid:450abb55-bc9c-4106-8a2d-1f6dc6afbae9" + "\r\n" +
				"X-ADDRESSBOOKSERVER-MEMBER:urn:uuid:93da65d4-ec6c-4211-9bca-8648deebe3e4" + "\r\n" +
				"X-ADDRESSBOOKSERVER-MEMBER:urn:uuid:" + contactUid + "\r\n" +
				"X-ADDRESSBOOKSERVER-MEMBER:urn:uuid:4c9ed4a2-bb4b-4947-919b-5078ebddccb4" + "\r\n" +
				"X-ADDRESSBOOKSERVER-MEMBER:urn:uuid:0067e70e-cbc0-4040-a1cb-8928e7cdf9f9" + "\r\n" +
				"X-ADDRESSBOOKSERVER-MEMBER:urn:uuid:e5c4f3d5-d668-4797-a0c3-0441fbb50cf9" + "\r\n" +
				"X-ADDRESSBOOKSERVER-MEMBER:urn:uuid:d817a3e0-6c6f-4888-85c2-6442bfc05412" + "\r\n" +
        		"X-ADDRESSBOOKSERVER-KIND:group" + "\r\n" +
				"REV:" + super.formatAsUTC(new Date()) + "\r\n" +
				"UID:" + contactsGroupUid + "\r\n" +
				"X-ABUID:05358928-0808-4FAD-A86F-1D9B517C9F78\\:ABGroup" + "\r\n" +
				"END:VCARD" + "\r\n"
		;
        /*
         * perform contact creation and group update on different threads to simulate concurrent requests of the client
         */
        final ThrowableHolder throwableHolder = new ThrowableHolder();
        Thread createThread = new Thread() {
        	@Override
            public void run() {
                try {
	                assertEquals("response code wrong", StatusCodes.SC_CREATED, putVCard(contactUid, contactVCard));
				} catch (Throwable t) {
					throwableHolder.setThrowable(t);
				}
    		}
        };
        createThread.start();
		super.putVCardUpdate(contactsGroupUid, groupVCard);
        createThread.join();
        throwableHolder.reThrowIfSet();
        /*
         * verify contact on server
         */
        Contact contact = super.getContact(contactUid);
        super.rememberForCleanUp(contact);
        assertEquals("uid wrong", contactUid, contact.getUid());
        assertEquals("firstname wrong", firstName, contact.getGivenName());
        assertEquals("lastname wrong", lastName, contact.getSurName());
		/*
		 * check for updates via sync-collection
		 */
        SyncCollectionResponse syncCollection = super.syncCollection(syncToken);
        Map<String, String> eTagsOK = syncCollection.getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTagsOK.size());
        /*
         * verify contact on client
         */
        List<VCardResource> addressData = super.addressbookMultiget(eTagsOK.keySet());
        VCardResource card = assertContains(contactUid, addressData);
        assertEquals("N wrong", firstName, card.getGivenName());
        assertEquals("N wrong", lastName, card.getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, card.getFN());
        /*
         * verify group deletions from sync-collection
         */
        List<String> hrefsNotFound = syncCollection.getHrefsStatusNotFound();
        assertTrue("no resource deletions reported on sync collection", 0 < hrefsNotFound.size());
        assertFalse("contacts group not reported as deleted", hrefsNotFound.contains(gabGroupHRef));
        assertFalse("contacts group not reported as deleted", hrefsNotFound.contains(contactsGroupHRef));
	}

}
