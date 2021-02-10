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
import static org.junit.Assert.fail;
import java.util.List;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.dav.carddav.VCardResource;
import com.openexchange.groupware.container.Contact;
import net.sourceforge.cardme.vcard.types.TelType;
import net.sourceforge.cardme.vcard.types.params.TelParamType;

/**
 * {@link MWB459Test}
 *
 * appsuite adds additional PREF field to vcard export
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public class MWB459Test extends CardDAVTest {

    @Test
    public void testCreateWithFourEmails() throws Exception {
        /*
         * prepare vCard
         */
        String uid = randomUID();
        String vCard = // @formatter:off
            "BEGIN:VCARD" + "\r\n" +
            "VERSION:3.0" + "\r\n" +
            "FN:MWB459Test" + "\r\n" +
            "N:;MWB459Test;;;" + "\r\n" +
            "TEL;CELL:+49176123456\r\n" +
            "TEL;HOME;PREF:+49123456789\r\n" +
            "PRODID:-//dmfs.org//mimedir.vcard//EN" + "\r\n" +
            "REV:20200111T150424Z" + "\r\n" +
            "UID:" + uid + "\r\n" +
            "END:VCARD" + "\r\n"
        ; // @formatter:on
        /*
         * create vCard resource on server
         */
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putVCard(uid, vCard));
        /*
         * get & verify created contact on server
         */
        Contact contact = getContact(uid);
        rememberForCleanUp(contact);
        assertEquals("uid wrong", uid, contact.getUid());
        assertEquals("cell phone 1 wrong", "+49176123456", contact.getCellularTelephone1());
        assertEquals("home phone 1 wrong", "+49123456789", contact.getTelephoneHome1());
        /*
         * get & verify created vCard from server
         */
        VCardResource vCardResource = getVCard(uid);
        TelType telCell = null;
        TelType telHome = null;
        List<TelType> tels = vCardResource.getVCard().getTels();
        for (TelType tel : tels) {
            if ("+49176123456".equals(tel.getTelephone())) {
                telCell = tel;
            } else if ("+49123456789".equals(tel.getTelephone())) {
                telHome = tel;
            } else {
                fail("unexepcted tel" + tel.getTelephone());
            }
        }
        assertNotNull(telCell);
        assertTrue(telCell.getParams().contains(TelParamType.CELL));
        assertFalse(telCell.getParams().contains(TelParamType.PREF));
        assertNotNull(telHome);
        assertTrue(telHome.getParams().contains(TelParamType.HOME));
        assertTrue(telHome.getParams().contains(TelParamType.PREF));
    }


}
