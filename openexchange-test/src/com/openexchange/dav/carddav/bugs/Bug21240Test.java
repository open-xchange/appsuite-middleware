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
import static org.junit.Assert.assertNull;
import java.util.Date;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.dav.carddav.UserAgents;
import com.openexchange.groupware.container.Contact;

/**
 * {@link Bug21240Test}
 *
 * Contact can't be deleted with Addressbook client on Mac OS 10.6.8
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug21240Test extends CardDAVTest {

	public Bug21240Test() {
		super();
	}

    @Before
    public void setUserAgent() throws Exception {
		super.getWebDAVClient().setUserAgent(UserAgents.MACOS_10_6_8);
    }

    @Test
	public void testDeleteContact() throws Exception {
		/*
		 * create contact
		 */
    	final String uid = randomUID() + "-ABSPlugin";
    	final String pathUid = randomUID() + "-ABSPlugin";
    	final String firstName = "test";
    	final String lastName = "hannes";
    	final String vCard =
    			"BEGIN:VCARD" + "\r\n" +
				"VERSION:3.0" + "\r\n" +
				"N:" + lastName + ";" + firstName + ";;;" + "\r\n" +
				"FN:" + firstName + " " + lastName + "\r\n" +
				"CATEGORIES:Kontakte" + "\r\n" +
				"X-ABUID:A33920F3-656F-47B7-A335-2C603DA3F324\\:ABPerson" + "\r\n" +
				"UID:" + uid + "\r\n" +
				"REV:" + super.formatAsUTC(new Date()) + "\r\n" +
				"END:VCARD" + "\r\n"
		;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putVCard(pathUid, vCard));
        /*
         * verify contact on server
         */
        final Contact contact = super.getContact(uid);
        super.rememberForCleanUp(contact);
        assertEquals("uid wrong", uid, contact.getUid());
        assertEquals("firstname wrong", firstName, contact.getGivenName());
        assertEquals("lastname wrong", lastName, contact.getSurName());
		/*
		 * delete contact
		 */
        assertEquals("response code wrong", StatusCodes.SC_NO_CONTENT, super.delete(pathUid));
        /*
         * verify deletion on server
         */
        assertNull("contact not deleted", super.getContact(uid));
        /*
         * verify deletion on client
         */
        Map<String, String> allETags = super.getAllETags();
        for (String href : allETags.values()) {
        	assertFalse("resource still present", href.contains(pathUid));
        }
	}
}
