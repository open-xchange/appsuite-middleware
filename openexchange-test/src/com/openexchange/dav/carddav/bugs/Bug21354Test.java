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

package com.openexchange.dav.carddav.bugs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.dav.carddav.UserAgents;
import com.openexchange.dav.carddav.VCardResource;
import com.openexchange.groupware.container.Contact;

/**
 * {@link Bug21354Test}
 *
 * CardDAV client stuck after trying to delete user from global address book
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug21354Test extends CardDAVTest {

	public Bug21354Test() {
		super();
	}

	@Test
	public void testDeleteFromGAB_10_6() throws Exception {
		super.getWebDAVClient().setUserAgent(UserAgents.MACOS_10_6_8);
		/*
		 * store current sync state via all ETags and CTag properties
		 */
		Map<String, String> eTags = super.getAllETags();
		String cTag = super.getCTag();
		/*
		 * pick random contact from global address book
		 */
		List<Contact> contacts = super.getContacts(super.getGABFolderID());
		String uid = contacts.get(new Random().nextInt(contacts.size())).getUid();
		/*
		 * try to delete contact, asserting positive response
		 */
		super.removeFromETags(eTags, uid);
        assertEquals("response code wrong", StatusCodes.SC_NO_CONTENT, super.delete(uid));
		/*
		 * verify that contact was not deleted on server
		 */
		assertNotNull("Contact deleted on server", super.getContact(uid));
		/*
		 * check for updates via ctag
		 */
		String cTag2 = super.getCTag();
		assertFalse("No changes indicated by CTag", cTag.equals(cTag2));
		/*
		 * check Etag collection
		 */
		Map<String, String> eTags2 = super.getAllETags();
		List<String> changedHrefs = super.getChangedHrefs(eTags, eTags2);
		assertTrue("less than 1 change reported in Etags", 0 < changedHrefs.size());
		/*
		 * check updated vCard for deleted member
		 */
        final List<VCardResource> addressData = super.addressbookMultiget(changedHrefs);
        assertContains(uid, addressData);
	}

	@Test
	public void testDeleteFromGAB_10_7() throws Exception {
		super.getWebDAVClient().setUserAgent(UserAgents.MACOS_10_7_2);
		/*
		 * store current sync state via all ETags and sync-token properties
		 */
		Map<String, String> eTags = super.getAllETags();
		SyncToken syncToken = new SyncToken(super.fetchSyncToken());
		/*
		 * pick random contact from global address book
		 */
		List<Contact> contacts = super.getContacts(super.getGABFolderID());
		String uid =  contacts.get(new Random().nextInt(contacts.size())).getUid();
		/*
		 * try to delete contact, asserting positive response
		 */
		super.removeFromETags(eTags, uid);
		assertEquals("response code wrong", StatusCodes.SC_NO_CONTENT, super.delete(uid));
		/*
		 * verify that contact was not deleted on server
		 */
		assertNotNull("Contact deleted on server", super.getContact(uid));
		/*
		 * check for updates via Etags with sync-token
		 */
        Map<String, String> eTags2 = super.syncCollection(syncToken).getETagsStatusOK();
		List<String> changedHrefs = super.getChangedHrefs(eTags, eTags2);
		assertTrue("less than 1 change reported in Etags", 0 < changedHrefs.size());
		/*
		 * check updated vCards for deleted member and global address book
		 */
        final List<VCardResource> addressData = super.addressbookMultiget(changedHrefs);
        assertContains(uid, addressData);
	}

}
