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
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.dav.carddav.VCardResource;
import com.openexchange.groupware.container.Contact;

/**
 * {@link UpdateTest} - Tests contact updates via the CardDAV interface
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class UpdateTest extends CardDAVTest {

	public UpdateTest() {
		super();
	}

	@Test
	public void testUpdateSimpleOnClient() throws Exception {
		/*
		 * fetch sync token for later synchronization
		 */
		SyncToken syncToken = new SyncToken(super.fetchSyncToken());
		/*
		 * create contact
		 */
    	String uid = randomUID();
    	String firstName = "test";
    	String lastName = "horst";
        String vCard =
        		"BEGIN:VCARD" + "\r\n" +
   				"VERSION:3.0" + "\r\n" +
				"N:" + lastName + ";" + firstName + ";;;" + "\r\n" +
				"FN:" + firstName + " " + lastName + "\r\n" +
				"ORG:test3;" + "\r\n" +
				"EMAIL;type=INTERNET;type=WORK;type=pref:test@example.com" + "\r\n" +
				"TEL;type=WORK;type=pref:24235423" + "\r\n" +
				"TEL;type=CELL:352-3534" + "\r\n" +
				"TEL;type=HOME:346346" + "\r\n" +
				"UID:" + uid + "\r\n" +
				"REV:" + super.formatAsUTC(new Date()) + "\r\n" +
				"PRODID:-//Apple Inc.//AddressBook 6.0//EN" + "\r\n" +
				"END:VCARD" + "\r\n"
		;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putVCard(uid, vCard));
        /*
         * verify contact on server
         */
        Contact contact = super.getContact(uid);
        super.rememberForCleanUp(contact);
        assertEquals("uid wrong", uid, contact.getUid());
        assertEquals("firstname wrong", firstName, contact.getGivenName());
        assertEquals("lastname wrong", lastName, contact.getSurName());
        /*
         * verify contact on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = super.addressbookMultiget(eTags.keySet());
        VCardResource card = assertContains(uid, addressData);
        assertEquals("N wrong", firstName, card.getGivenName());
        assertEquals("N wrong", lastName, card.getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, card.getFN());
        /*
         * update contact on client
         */
    	String updatedFirstName = "test2";
    	String udpatedLastName = "horst2";
    	card.getVCard().getN().setFamilyName(udpatedLastName);
    	card.getVCard().getN().setGivenName(updatedFirstName);
    	card.getVCard().getFN().setFormattedName(updatedFirstName + " " + udpatedLastName);
		assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putVCardUpdate(card.getUID(), card.toString(), card.getETag()));
        /*
         * verify updated contact on server
         */
        final Contact updatedContact = super.getContact(uid);
        super.rememberForCleanUp(updatedContact);
        assertEquals("uid wrong", uid, updatedContact.getUid());
        assertEquals("firstname wrong", updatedFirstName, updatedContact.getGivenName());
        assertEquals("lastname wrong", udpatedLastName, updatedContact.getSurName());
        /*
         * verify updated contact on client
         */
        eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        addressData = super.addressbookMultiget(eTags.keySet());
        card = assertContains(uid, addressData);
        assertEquals("N wrong", updatedFirstName, card.getGivenName());
        assertEquals("N wrong", udpatedLastName, card.getFamilyName());
        assertEquals("FN wrong", updatedFirstName + " " + udpatedLastName, card.getFN());
	}

	@Test
	public void testUpdateSimpleOnServer() throws Exception {
		/*
		 * fetch sync token for later synchronization
		 */
		SyncToken syncToken = new SyncToken(super.fetchSyncToken());
		/*
		 * create contact
		 */
    	String uid = randomUID();
    	String firstName = "test";
    	String lastName = "waldemar";
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
        VCardResource card = assertContains(uid, addressData);
        assertEquals("N wrong", firstName, card.getGivenName());
        assertEquals("N wrong", lastName, card.getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, card.getFN());
        /*
         * update contact on server
         */
    	String updatedFirstName = "test2";
    	String udpatedLastName = "waldemar2";
		contact.setSurName(udpatedLastName);
		contact.setGivenName(updatedFirstName);
		contact.setDisplayName(updatedFirstName + " " + udpatedLastName);
		contact = super.update(contact);
        /*
         * verify contact on client
         */
        eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        addressData = super.addressbookMultiget(eTags.keySet());
        card = assertContains(uid, addressData);
        assertEquals("N wrong", updatedFirstName, card.getGivenName());
        assertEquals("N wrong", udpatedLastName, card.getFamilyName());
        assertEquals("FN wrong", updatedFirstName + " " + udpatedLastName, card.getFN());
	}

	@Test
	public void testUpdateWithQuotedETag() throws Exception {
		/*
		 * fetch sync token for later synchronization
		 */
		SyncToken syncToken = new SyncToken(super.fetchSyncToken());
		/*
		 * create contact
		 */
    	String uid = randomUID();
    	String firstName = "otto";
    	String lastName = "horst";
        String vCard =
        		"BEGIN:VCARD" + "\r\n" +
   				"VERSION:3.0" + "\r\n" +
				"N:" + lastName + ";" + firstName + ";;;" + "\r\n" +
				"FN:" + firstName + " " + lastName + "\r\n" +
				"ORG:test3;" + "\r\n" +
				"EMAIL;type=INTERNET;type=WORK;type=pref:test@example.com" + "\r\n" +
				"TEL;type=WORK;type=pref:24235423" + "\r\n" +
				"TEL;type=CELL:352-3534" + "\r\n" +
				"TEL;type=HOME:346346" + "\r\n" +
				"UID:" + uid + "\r\n" +
				"REV:" + super.formatAsUTC(new Date()) + "\r\n" +
				"PRODID:-//Apple Inc.//AddressBook 6.0//EN" + "\r\n" +
				"END:VCARD" + "\r\n"
		;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putVCard(uid, vCard));
        /*
         * verify contact on server
         */
        Contact contact = super.getContact(uid);
        super.rememberForCleanUp(contact);
        assertEquals("uid wrong", uid, contact.getUid());
        assertEquals("firstname wrong", firstName, contact.getGivenName());
        assertEquals("lastname wrong", lastName, contact.getSurName());
        /*
         * verify contact on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = super.addressbookMultiget(eTags.keySet());
        VCardResource card = assertContains(uid, addressData);
        assertEquals("N wrong", firstName, card.getGivenName());
        assertEquals("N wrong", lastName, card.getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, card.getFN());
        /*
         * update contact on client
         */
    	String updatedFirstName = "otto2";
    	String udpatedLastName = "horst2";
    	card.getVCard().getN().setFamilyName(udpatedLastName);
    	card.getVCard().getN().setGivenName(updatedFirstName);
    	card.getVCard().getFN().setFormattedName(updatedFirstName + " " + udpatedLastName);
		assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putVCardUpdate(card.getUID(), card.toString(), "\"" + card.getETag() + "\""));
        /*
         * verify updated contact on server
         */
        final Contact updatedContact = super.getContact(uid);
        super.rememberForCleanUp(updatedContact);
        assertEquals("uid wrong", uid, updatedContact.getUid());
        assertEquals("firstname wrong", updatedFirstName, updatedContact.getGivenName());
        assertEquals("lastname wrong", udpatedLastName, updatedContact.getSurName());
        /*
         * verify updated contact on client
         */
        eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        addressData = super.addressbookMultiget(eTags.keySet());
        card = assertContains(uid, addressData);
        assertEquals("N wrong", updatedFirstName, card.getGivenName());
        assertEquals("N wrong", udpatedLastName, card.getFamilyName());
        assertEquals("FN wrong", updatedFirstName + " " + udpatedLastName, card.getFN());
	}

	@Test
	public void testUpdateWithDifferentFilename() throws Exception {
		/*
		 * fetch sync token for later synchronization
		 */
		SyncToken syncToken = new SyncToken(super.fetchSyncToken());
		/*
		 * create contact
		 */
    	String uid = randomUID();
    	String filename = randomUID();
    	String firstName = "test";
    	String lastName = "horst";
        String vCard =
        		"BEGIN:VCARD" + "\r\n" +
   				"VERSION:3.0" + "\r\n" +
				"N:" + lastName + ";" + firstName + ";;;" + "\r\n" +
				"FN:" + firstName + " " + lastName + "\r\n" +
				"ORG:test3;" + "\r\n" +
				"EMAIL;type=INTERNET;type=WORK;type=pref:test@example.com" + "\r\n" +
				"TEL;type=WORK;type=pref:24235423" + "\r\n" +
				"TEL;type=CELL:352-3534" + "\r\n" +
				"TEL;type=HOME:346346" + "\r\n" +
				"UID:" + uid + "\r\n" +
				"REV:" + super.formatAsUTC(new Date()) + "\r\n" +
				"PRODID:-//Apple Inc.//AddressBook 6.0//EN" + "\r\n" +
				"END:VCARD" + "\r\n"
		;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putVCard(filename, vCard));
        /*
         * verify contact on server
         */
        Contact contact = super.getContact(uid);
        super.rememberForCleanUp(contact);
        assertEquals("uid wrong", uid, contact.getUid());
        assertEquals("firstname wrong", firstName, contact.getGivenName());
        assertEquals("lastname wrong", lastName, contact.getSurName());
        /*
         * verify contact on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = super.addressbookMultiget(eTags.keySet());
        VCardResource card = assertContains(uid, addressData);
        assertEquals("N wrong", firstName, card.getGivenName());
        assertEquals("N wrong", lastName, card.getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, card.getFN());
        /*
         * update contact on client
         */
    	String updatedFirstName = "test2";
    	String udpatedLastName = "horst2";
    	card.getVCard().getN().setFamilyName(udpatedLastName);
    	card.getVCard().getN().setGivenName(updatedFirstName);
    	card.getVCard().getFN().setFormattedName(updatedFirstName + " " + udpatedLastName);
		assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putVCardUpdate(filename, card.toString(), card.getETag()));
        /*
         * verify updated contact on server
         */
        final Contact updatedContact = super.getContact(uid);
        super.rememberForCleanUp(updatedContact);
        assertEquals("uid wrong", uid, updatedContact.getUid());
        assertEquals("firstname wrong", updatedFirstName, updatedContact.getGivenName());
        assertEquals("lastname wrong", udpatedLastName, updatedContact.getSurName());
        /*
         * verify updated contact on client
         */
        eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        addressData = super.addressbookMultiget(eTags.keySet());
        card = assertContains(uid, addressData);
        assertEquals("N wrong", updatedFirstName, card.getGivenName());
        assertEquals("N wrong", udpatedLastName, card.getFamilyName());
        assertEquals("FN wrong", updatedFirstName + " " + udpatedLastName, card.getFN());
	}

}
