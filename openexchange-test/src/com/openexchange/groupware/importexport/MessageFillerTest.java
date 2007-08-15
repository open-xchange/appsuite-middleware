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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.groupware.importexport;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.contact.Contacts;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.ldap.User;
import com.openexchange.imap.IMAPProperties;
import com.openexchange.imap.OXMailException;
import com.openexchange.imap.OXMailException.MailCode;
import com.openexchange.server.DBPool;
import com.openexchange.server.DBPoolingException;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.OXContainerConverter;

/**
 * Note: This is not a good test. This is supposed to test MessageFiller.getUserVCard,
 * but it doesn't. The code is cloned from there, because I coulö not extract it
 * from there.
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public class MessageFillerTest extends AbstractVCardTest {
	
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(MessageFillerTest.class);
	}
	
	public String getSimulatedUserName(){
		return "Friendly guy, though with a dreaded comma, like these, in his name";
	}

	public String simulateGetVCard(String version, String mime) throws DBPoolingException, OXException, Exception{
		SessionObject session = sessObj;
		final User userObj = session.getUserObject();
		final OXContainerConverter converter = new OXContainerConverter(session);
		Connection readCon = null;
		try {
			readCon = DBPool.pickup(session.getContext());
			ContactObject contactObj = null;
			try {
				contactObj = Contacts.getContactById(userObj.getContactId(), userObj.getId(), userObj.getGroups(),
						session.getContext(), session.getUserConfiguration(), readCon);
				contactObj.setDisplayName(getSimulatedUserName());
			} catch (final OXException oxExc) {
				throw oxExc;
			} catch (final Exception e) {
				throw new OXMailException(MailCode.INTERNAL_ERROR, e, e.getMessage());
			}
			final VersitObject versitObj = converter.convertContact(contactObj, version);
			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			final VersitDefinition def = Versit.getDefinition(mime);
			final VersitDefinition.Writer w = def.getWriter(os, IMAPProperties.getDefaultMimeCharset());
			def.write(w, versitObj);
			w.flush();
			os.flush();
			return new String(os.toByteArray(), IMAPProperties.getDefaultMimeCharset());
		} finally {
			if (readCon != null) {
				DBPool.closeReaderSilent(session.getContext(), readCon);
				readCon = null;
			}
			converter.close();
		}
	}
	
	
	/*
	 * Should escape commas
	 */
	@Test public void testVersion30() throws DBPoolingException, OXException, Exception{
		final String vcard = simulateGetVCard("3.0", "text/vcard");
		final String escapedName = getSimulatedUserName().replace(",", "\\,");
		assertTrue( vcard.contains(escapedName) );
	}
	
	/*
	 * Should not escape commas
	 */
	@Test public void testVersion21() throws DBPoolingException, OXException, Exception{
		final String vcard = simulateGetVCard("2.1" , "text/x-vcard");
		assertTrue( vcard.contains( getSimulatedUserName() ) );

	}
}
