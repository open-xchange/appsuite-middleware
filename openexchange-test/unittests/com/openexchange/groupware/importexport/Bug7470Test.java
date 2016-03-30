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

package com.openexchange.groupware.importexport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.util.List;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.calendar.CalendarSql;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;

public class Bug7470Test extends AbstractICalImportTest {
  //FIXME this one is still broken
    
    //workaround for JUnit 3 runner
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(Bug7470Test.class);
	}

	/*
	 * Imported appointment loses participants
	 */
	@Test public void test7470() throws SQLException, OXException, NumberFormatException, OXException, OXException {
	    folderId = createTestFolder(FolderObject.CALENDAR, sessObj, ctx, "ical7470Folder");
		final String email = "cbartkowiak@oxhemail.open-xchange.com";
		final String cn = "Camil Bartkowiak (cbartkowiak@oxhemail.open-xchange.com)";
		final String ical =
			"BEGIN:VCALENDAR\n" +
			"PRODID:-//Microsoft Corporation//Outlook 12.0 MIMEDIR//EN\n" +
			"VERSION:2.0\n" +
			"METHOD:REQUEST\n" +
			"X-MS-OLK-FORCEINSPECTOROPEN:TRUE\n" +
			"BEGIN:VEVENT\n" +
			"ATTENDEE;CN=\""+cn+"\";RSVP\n" +
			"\n" +
			"=TRUE:mailto:"+email+"\n" +
			"CLASS:PUBLIC\n" +
			"CREATED:20070521T150327Z\n" +
			"DESCRIPTION:Hallo Hallo\n" +
			"\n" +
			"\n" +
			"DTEND:20070523T090000Z\n" +
			"DTSTAMP:20070521T150327Z\n" +
			"DTSTART:20070523T083000Z\n" +
			"LAST-MODIFIED:20070521T150327Z\n" +
			"LOCATION:Location here\n" +
			"ORGANIZER;CN=Tobias:mailto:tfriedrich@oxhemail.open-xchange.com\n" +
			"PRIORITY:5\n" +
			"SEQUENCE:0\n" +
			"SUMMARY;LANGUAGE=de:Simple Appointment with participant\n" +
			"TRANSP:OPAQUE\nUID:040000008200E00074C5B7101A82E0080000000060565ABBC99BC701000000000000000\n" +
			"	010000000E4B2BA931D32B84DAFB227C9E0CA348C\n" +
			"X-MICROSOFT-CDO-BUSYSTATUS:BUSY\n" +
			"X-MICROSOFT-CDO-IMPORTANCE:1\n" +
			"X-MICROSOFT-DISALLOW-COUNTER:FALSE\n" +
			"X-MS-OLK-ALLOWEXTERNCHECK:TRUE\n" +
			"X-MS-OLK-AUTOFILLLOCATION:FALSE\n" +
			"X-MS-OLK-CONFTYPE:0\n" +
			"BEGIN:VALARM\n" +
			"TRIGGER:PT0M\n" +
			"ACTION:DISPLAY\n" +
			"DESCRIPTION:Reminder\n" +
			"END:VALARM\n" +
			"END:VEVENT\n" +
			"END:VCALENDAR";

		assertTrue("Can import?" ,  imp.canImport(sessObj, format, _folders(), null));
		final List<ImportResult> results = imp.importData(sessObj, format, new ByteArrayInputStream(ical.getBytes(com.openexchange.java.Charsets.UTF_8)), _folders(), null);
		assertEquals("One import?" , 1 , results.size());
		final ImportResult res = results.get(0);
		assertEquals("Shouldn't have error" , null, res.getException());

		final AppointmentSQLInterface appointmentSql = new CalendarSql(sessObj);
		final Appointment appointmentObj = appointmentSql.getObjectById(Integer.parseInt( res.getObjectId() ), folderId);
		assertTrue("Exists" , appointmentObj != null);
		final Participant[] participants = appointmentObj.getParticipants();
		assertEquals("Number of attendees?" , 2, participants.length);

		boolean containsAttendee = false;
		for(final Participant p : participants){
			if(cn.equals( p.getDisplayName() ) && email.equals( p.getEmailAddress())){
				containsAttendee = true;
			}
		}
		assertTrue("Found attendee?" , containsAttendee);

	}
}
