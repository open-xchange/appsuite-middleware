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
 *  new contacts added via iOS will be added to the collected addresses folder.
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
        String vCard =
    		"BEGIN:VCARD" + "\r\n" +
			"VERSION:3.0" + "\r\n" +
			"N:" + lastName + ";" + firstName + ";;;" + "\r\n" +
			"FN:" + firstName + " " + lastName + "\r\n" +
			"URL:" + url + "\r\n" +
			"UID:" + uid + "\r\n" +
			"REV:" + super.formatAsUTC(new Date()) + "\r\n" +
			"PRODID:-//Apple Inc.//AddressBook 6.1//EN" + "\r\n" +
			"END:VCARD" + "\r\n"
		;
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
