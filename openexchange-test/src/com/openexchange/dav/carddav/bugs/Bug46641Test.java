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
import org.junit.Test;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.dav.reports.SyncCollectionResponse;

/**
 * {@link Bug46641Test}
 *
 * immediate deletion of contacts group which was created via OS X contacts app fails with NPE
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.2
 */
public class Bug46641Test extends CardDAVTest {

    /**
     * Initializes a new {@link Bug46641Test}.
     */
	public Bug46641Test() {
		super();
	}

	@Test
	public void testBulkImportContactGroup() throws Exception {
		/*
		 * fetch sync token for later synchronization
		 */
        SyncToken syncToken = new SyncToken(fetchSyncToken());
		/*
		 * try to create contact group using bulk-import
		 */
    	String uid = randomUID();
    	String vCard =
			"BEGIN:VCARD" + "\r\n" +
			"VERSION:3.0" + "\r\n" +
            "PRODID:-//Apple Inc.//AddressBook 9.0//EN" + "\r\n" +
            "N:untitled group" + "\r\n" +
            "FN:untitled group" + "\r\n" +
            "X-ADDRESSBOOKSERVER-KIND:group" + "\r\n" +
			"REV:" + formatAsUTC(new Date()) + "\r\n" +
            "UID:" + uid + "\r\n" +
			"END:VCARD" + "\r\n"
		;
    	postVCard(uid, vCard, 0);
    	/*
    	 * check that no contact was created on server
    	 */
        assertNull(getContact(uid));
        /*
         * check that sync-collection reports the resource as deleted
         */
        SyncCollectionResponse syncCollectionResponse = syncCollection(syncToken);
        assertEquals("no resource deletions reported on sync collection", 1, syncCollectionResponse.getHrefsStatusNotFound().size());
        assertTrue(syncCollectionResponse.getHrefsStatusNotFound().get(0).contains(uid));
	}
}
