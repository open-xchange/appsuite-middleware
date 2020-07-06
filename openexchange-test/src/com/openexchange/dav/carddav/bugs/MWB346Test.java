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
import static org.junit.Assert.assertTrue;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.dav.carddav.UserAgents;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link MWB346Test}
 *
 * CardDAV: deletion of a contact does not sync for contacts which were created on an iOS device
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.4
 */
public class MWB346Test extends CardDAVTest {

    /**
     * Initializes a new {@link MWB346Test}.
     */
    public MWB346Test() {
        super();
    }

    @Override
    protected String getDefaultUserAgent() {
        return UserAgents.IOS_8_4_0;
    }

    @Test
    public void testCreateInGAB() throws Exception {
        String gabFolderId = String.valueOf(FolderObject.SYSTEM_LDAP_FOLDER_ID);
        String defaultFolderId = String.valueOf(getDefaultFolderID());
        SyncToken syncTokenDefaultFolder = new SyncToken(fetchSyncToken(defaultFolderId));
        SyncToken syncTokenGAB = new SyncToken(fetchSyncToken(gabFolderId));
        /*
         * prepare vCard
         */
        String uid = randomUID();
        String vCard = // @formatter:off
            "BEGIN:VCARD" + "\r\n" +
            "VERSION:3.0" + "\r\n" +
            "PRODID:-//Apple Inc.//AddressBook 6.1//EN" + "\r\n" +
            "REV:" + formatAsUTC(new Date()) + "\r\n" +
            "UID:" + uid + "\r\n" +
            "FN:Test GAB" + "\r\n" +
            "N:;Test GAB;;;" + "\r\n" +
            "END:VCARD" + "\r\n";
        ; // @formatter:on
        /*
         * create vCard resource in folder 6 on server
         */
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putVCard(uid, vCard, gabFolderId));
        /*
         * get & verify created contact on server
         */
        Contact contact = getContact(uid);
        rememberForCleanUp(contact);
        assertEquals("uid wrong", uid, contact.getUid());
        /*
         * sync default folder on client, expecting the contact is present there
         */
        Map<String, String> eTags = syncCollection(defaultFolderId, syncTokenDefaultFolder).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        assertContains(uid, addressbookMultiget(defaultFolderId, eTags.keySet()));
        /*
         * sync GAB folder on client, expecting the contact to be removed there
         */
        List<String> hrefs = syncCollection(gabFolderId, syncTokenGAB).getHrefsStatusNotFound();
        assertTrue("no resource changes reported on sync collection", 0 < hrefs.size());
        boolean found = false;
        for (String href : hrefs) {
            if (null != href && href.contains(uid)) {
                found = true;
                break;
            }
        }
        assertTrue("contact not reported as deleted in GAB", found);
    }

}
