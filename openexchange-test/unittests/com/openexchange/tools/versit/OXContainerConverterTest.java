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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.tools.versit;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tasks.Task;

/**
 * This test was only written to test additions that I made to
 * OXContainerConverter, but I was too lazy to implement tests
 * for all cases.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public class OXContainerConverterTest extends AbstractOXContainerConverterTest {

    /**
	 * Test of the private flag.
	 * @throws Exception
	 */
	public void test7472_forPrivate() throws Exception{
		final String versitData = "BEGIN:VCALENDAR\nVERSION:2.0\nPRODID:-//Apple Computer\\, Inc//iCal 2.0//EN\nBEGIN:VEVENT\nCLASS:PRIVATE\nDTSTART:20070514T150000Z\nDTEND:20070514T163000Z\nLOCATION:Olpe\nSUMMARY:Simple iCal Appointment\nDESCRIPTION:Notes here...\nEND:VEVENT\nEND:VCALENDAR\n";
		isFlaggedAsPrivate(versitData);
	}

	/**
	 * Test of the confidential flag.
	 * @throws Exception
	 */

	public void test7472_forConfidential() {
		final String versitData = "BEGIN:VCALENDAR\nVERSION:2.0\nPRODID:-//Apple Computer\\, Inc//iCal 2.0//EN\nBEGIN:VEVENT\nCLASS:CONFIDENTIAL\nDTSTART:20070514T150000Z\nDTEND:20070514T163000Z\nLOCATION:Olpe\nSUMMARY:Simple iCal Appointment\nDESCRIPTION:Notes here...\nEND:VEVENT\nEND:VCALENDAR\n";
		try {
			isFlaggedAsPrivate(versitData);
			fail("Should throw privacy exception");
		} catch (final OXException e){
			assertTrue("Should throw privacy exception" , true);
			return;
		} catch (final Exception e) {
			fail("Wanted ConverterPrivacyException");
		}
	}

	/**
	 * Test of the public flag.
	 * @throws Exception
	 */
	public void test7472_forPublic() throws Exception{
		final String versitData = "BEGIN:VCALENDAR\nVERSION:2.0\nPRODID:-//Apple Computer\\, Inc//iCal 2.0//EN\nBEGIN:VEVENT\nCLASS:PUBLIC\nDTSTART:20070514T150000Z\nDTEND:20070514T163000Z\nLOCATION:Olpe\nSUMMARY:Simple iCal Appointment\nDESCRIPTION:Notes here...\nEND:VEVENT\nEND:VCALENDAR\n";
		assertFalse(isFlaggedAsPrivate(versitData));
	}

	public void test8475() throws Exception{
		final User testUser = getUserParticipant();
		final String participantEmail = testUser.getMail();
		final String versitData =
			"BEGIN:VCALENDAR\n" +
			"VERSION:2.0\n" +
			"PRODID:-//Apple Computer\\, Inc//iCal 1.5//EN\n" +
			"BEGIN:VTODO\n" +
			"ORGANIZER:MAILTO:tobias.friedrich@open-xchange.com\n" +
			"ATTENDEE:MAILTO:"+ participantEmail+"\n" +
			"DTSTART:20070608T080000Z\n" +
			"STATUS:COMPLETED\n" +
			"SUMMARY:Test todo\n" +
			"UID:8D4FFA7A-ABC0-11D7-8200-00306571349C-RID\n" +
			"DUE:20070618T080000Z\n" +
			"END:VTODO\n" +
			"END:VCALENDAR";
		final Task task = convertTask(versitData);
		final Participant[] participants = task.getParticipants();
		assertEquals(1,participants.length);

		try {
			final UserParticipant p = (UserParticipant) participants[0];
			assertEquals(testUser.getId() ,p.getIdentifier());
		} catch (final ClassCastException e){
			fail("User with e-mail " + participantEmail + " should be internal user");
		}
	}
}
