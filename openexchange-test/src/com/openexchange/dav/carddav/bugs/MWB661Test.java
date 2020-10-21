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
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.dav.carddav.UserAgents;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;

/**
 * {@link MWB661Test}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public class MWB661Test extends CardDAVTest {

    @Override
    protected String getDefaultUserAgent() {
        return UserAgents.CALDAV_SYNCHRONIZER;
    }

    @Test
    public void testUidOnlyReferences() throws Exception {
        String collection = String.valueOf(getDefaultFolderID());
        /*
         * prepare contact vCard
         */
        String contactUid = randomUID();
        String contactVCard = // @formatter:off
            "BEGIN:VCARD" + "\r\n" +
            "VERSION:3.0" + "\r\n" +
            "FN:Test" + "\r\n" +
            "EMAIL:test@example.com" + "\r\n" +
            "PRODID:-//Microsoft Corporation//Outlook 16.0 MIMEDIR//EN" + "\r\n" +
            "REV:" + formatAsUTC(new Date()) + "\r\n" +
            "UID:" + contactUid + "\r\n" +
            "END:VCARD" + "\r\n"
        ; // @formatter:on
        /*
         * create contact vCard resource on server
         */
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putVCard(contactUid, contactVCard, collection));
        /*
         * prepare distribution list vCard referencing this contact
         */
        String listUid = randomUID();
        String listVCard = // @formatter:off
            "BEGIN:VCARD" + "\r\n" +
            "VERSION:3.0" + "\r\n" +
            "FN:List" + "\r\n" +
            "X-ADDRESSBOOKSERVER-KIND:group" + "\r\n" +
            "X-ADDRESSBOOKSERVER-MEMBER:urn:uuid:" + contactUid + "\r\n" +
            "PRODID:-//Microsoft Corporation//Outlook 16.0 MIMEDIR//EN" + "\r\n" +
            "REV:" + formatAsUTC(new Date()) + "\r\n" +
            "UID:" + listUid + "\r\n" +
            "END:VCARD" + "\r\n"
        ; // @formatter:on
        /*
         * create list vCard resource on server
         */
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putVCard(listUid, listVCard, collection));
        /*
         * get & verify created contact & list on server
         */
        Contact contact = getContact(contactUid, getDefaultFolderID());
        assertNotNull(contact);
        rememberForCleanUp(contact);
        Contact distList = getContact(listUid, getDefaultFolderID());
        assertNotNull(distList);
        rememberForCleanUp(distList);
        assertTrue("no distribution list", distList.getMarkAsDistribtuionlist());
        assertNotNull("no distribution list", distList.getDistributionList());
        assertEquals("unexpected number of members", 1, distList.getDistributionList().length);
        DistributionListEntryObject entry = distList.getDistributionList()[0];
        assertEquals("entry id wrong", contact.getObjectID(), entry.getEntryID());
    }

}
