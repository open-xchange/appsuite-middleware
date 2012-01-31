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

package com.openexchange.carddav.bugs;

import java.util.List;
import java.util.Map;
import java.util.Random;

import net.sourceforge.cardme.vcard.features.ExtendedFeature;

import com.openexchange.carddav.CardDAVClient;
import com.openexchange.carddav.CardDAVTest;
import com.openexchange.carddav.StatusCodes;
import com.openexchange.carddav.VCardResource;

/**
 * {@link Bug21354Test}
 * 
 * CardDAV client stuck after trying to delete user from global address book
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug21354Test extends CardDAVTest {

	public Bug21354Test(String name) {
		super(name);
	}
	
	public void testDeleteFromGAB_10_6() throws Exception {
		super.getCardDAVClient().setUserAgent(CardDAVClient.USER_AGENT_10_6_8);
		/*
		 * store current sync state via all ETags and CTag properties
		 */
		final Map<String, String> eTags = super.getAllETags();
		final String cTag = super.getCTag();		
		/*
		 * pick random contact from global address book
		 */
		final VCardResource globalAddressbookVCard = super.getGlobalAddressbookVCard();
		final List<ExtendedFeature> members = globalAddressbookVCard.getExtendedFeatures("X-ADDRESSBOOKSERVER-MEMBER");
		final int randomIndex = new Random().nextInt(members.size());
		final ExtendedFeature member = members.get(randomIndex);
		final String memberUID = member.getExtensionData().substring(9);
		/*
		 * try to delete contact and change group, asserting positive responses
		 */
		globalAddressbookVCard.getVCard().removeExtendedType(member);
		assertEquals("response code wrong", StatusCodes.SC_CREATED, 
				super.putVCardUpdate(globalAddressbookVCard.getUID(), globalAddressbookVCard.toString()));
		super.removeFromETags(eTags, memberUID);
		assertEquals("response code wrong", StatusCodes.SC_OK, super.delete(memberUID));
		super.removeFromETags(eTags, globalAddressbookVCard.getUID());
		/*
		 * verify that contact was not deleted on server
		 */
		assertNotNull("Contact deleted on server", super.getContact(memberUID, super.getGABFolderId()));
		/*
		 * check for updates via ctag
		 */
		final String cTag2 = super.getCTag();
		assertFalse("No changes indicated by CTag", cTag.equals(cTag2));
		/*
		 * check Etag collection 
		 */
		final Map<String, String> eTags2 = super.getAllETags();
		final List<String> changedHrefs = super.getChangedHrefs(eTags, eTags2);
		assertTrue("less than 2 changes reported in Etags", 1 < changedHrefs.size());
		/*
		 * check updated vCards for deleted member and global address book 
		 */
        final List<VCardResource> addressData = super.addressbookMultiget(changedHrefs);
        assertContains(memberUID, addressData);
        assertContains(globalAddressbookVCard.getUID(), addressData);
	}
	
	public void testDeleteFromGAB_10_7() throws Exception {
		super.getCardDAVClient().setUserAgent(CardDAVClient.USER_AGENT_10_7_2);
		/*
		 * store current sync state via all ETags and sync-token properties
		 */
		final Map<String, String> eTags = super.getAllETags();
		final String syncToken = super.fetchSyncToken();
		/*
		 * pick random contact from global address book
		 */
		final VCardResource globalAddressbookVCard = super.getGlobalAddressbookVCard();
		final List<ExtendedFeature> members = globalAddressbookVCard.getExtendedFeatures("X-ADDRESSBOOKSERVER-MEMBER");
		final int randomIndex = new Random().nextInt(members.size());
		final ExtendedFeature member = members.get(randomIndex);
		final String memberUID = member.getExtensionData().substring(9);
		/*
		 * try to delete contact and change group, asserting positive responses
		 */
		globalAddressbookVCard.getVCard().removeExtendedType(member);
		assertEquals("response code wrong", StatusCodes.SC_CREATED, 
				super.putVCardUpdate(globalAddressbookVCard.getUID(), globalAddressbookVCard.toString()));
		super.removeFromETags(eTags, memberUID);
		assertEquals("response code wrong", StatusCodes.SC_OK, super.delete(memberUID));
		super.removeFromETags(eTags, globalAddressbookVCard.getUID());
		/*
		 * verify that contact was not deleted on server
		 */
		assertNotNull("Contact deleted on server", super.getContact(memberUID, super.getGABFolderId()));
		/*
		 * check for updates via Etags with sync-token
		 */
        final Map<String, String> eTags2 = super.syncCollection(syncToken);
		final List<String> changedHrefs = super.getChangedHrefs(eTags, eTags2);
		assertTrue("less than 2 changes reported in Etags", 1 < changedHrefs.size());
		/*
		 * check updated vCards for deleted member and global address book 
		 */
        final List<VCardResource> addressData = super.addressbookMultiget(changedHrefs);
        assertContains(memberUID, addressData);
        assertContains(globalAddressbookVCard.getUID(), addressData);
	}
}
