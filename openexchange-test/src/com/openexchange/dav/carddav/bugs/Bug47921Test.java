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

import static org.junit.Assert.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.dav.carddav.VCardResource;
import com.openexchange.groupware.container.Contact;

/**
 * {@link Bug47921Test}
 *
 * Repeated errors for invalid chars in user content
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.2
 */
public class Bug47921Test extends CardDAVTest {

    /**
     * Initializes a new {@link Bug47921Test}.
     */
	public Bug47921Test() {
		super();
	}

	@Test
	public void testBulkImportMalformedVCard() throws Exception {
		/*
		 * fetch sync token for later synchronization
		 */
        SyncToken syncToken = new SyncToken(fetchSyncToken());
		/*
		 * try to create contact using bulk-import
		 */
    	String uid = randomUID();
    	String vCard =
            "BEGIN:VCARD\r\n" +
            "VERSION:3.0\r\n" +
            "PRODID:-//Apple Inc.//Mac OS X 10.11.6//EN\r\n" +
            "N:Herbert;Tester;;;\r\n" +
            "FN:Tester Herbert\r\n" +
            "ORG:Acme Testdorf;\r\n" +
            "TITLE:Verkaufsberater Gro\u00dfabnehmer\r\n" +
            "item1.EMAIL;type=INTERNET;type=pref:Tester.Herbert@ncvspcvswfw-wqfvewew.io\r\n" +
            "item1.X-ABLabel:_$!<Other>!$_\r\n" +
            "TEL;type=WORK;type=FAX;type=pref:+49 (0) 511 86056-595\r\n" +
            "TEL;type=CELL;type=VOICE:+49 (0) 172 18 984 04\r\n" +
            "ADR;type=WORK;type=pref:;;Acme Testdorf GmbH;;;;\r\n" +
            "NOTE:Tester Herbert\nVerkaufsberater Gro\u00dfabnehmer\n\nTelefon: +49 (0) 511 86056-577\n\r\n" +
            "URL;type=WORK;type=pref:www.ncvspcvswfw-wqfvewew.io\r\n" +
            "Horstaheimer Stra\u221a\u00fce 303\nD 72942 Testdorf\nDeutschland\r\n" +
            "tel;charset=utf-8;type=work:+49 (0) 511 86056-577\r\n" +
            "Horstaheimer Stra\u221a\u00fce 303\n\r\n" +
            "D 72942 Testdorf\nwww.ncvspcvswfw-wqfvewew.io <http://www.ncvspcvswfw-wqfvewew.io>\n\n \n\r\n" +
            "Horstaheimer Stra\u221a\u00fce 303;Testdorf;;D 72942;Deutschland\r\n" +
            "label;charset=utf-8;type=work:Acme Testdorf GmbH\r\n" +
            "Horstaheimer Stra\u221a\u00fce 303\nD 72942 Testdorf\nDeutschland\r\n" +
            "tel;charset=utf-8;type=work:+49 (0) 511 86056-577\r\n" +
            "Horstaheimer Stra\u221a\u00fce 303\n\r\n" +
            "D 72942 Testdorf\nwww.ncvspcvswfw-wqfvewew.io <http://www.ncvspcvswfw-wqfvewew.io>\n\n \n\r\n" +
            "Horstaheimer Stra\u221a\u00fce 303;Testdorf;;D 72942;Deutschland\r\n" +
            "label;charset=utf-8;type=work:Acme Testdorf GmbH\r\n" +
            "REV:" + formatAsUTC(new Date()) + "\r\n" +
            "UID:" + uid + "\r\n" +
            "END:VCARD\r\n"
		;
    	postVCard(uid, vCard, 0);
    	/*
    	 * check the contact was created on server
    	 */
    	Contact contact = getContact(uid);
        assertNotNull(contact);
        assertEquals("Tester", contact.getGivenName());
        assertEquals("Herbert", contact.getSurName());
        /*
         * verify contact on client
         */
        Map<String, String> eTags = syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = addressbookMultiget(eTags.keySet());
        VCardResource contactCard = assertContains(uid, addressData);
        assertEquals("Tester", contactCard.getGivenName());
        assertEquals("Herbert", contactCard.getFamilyName());
	}

}
