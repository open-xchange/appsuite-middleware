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

import com.openexchange.exception.OXException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.calendar.CalendarSql;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.importexport.formats.Format;

public class Bug8527 extends AbstractICalImportTest {

	//workaround for JUnit 3 runner
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(Bug8527.class);
	}

    @Test public void testDummy() {
        // do nothing
    }
    // FIXME!
    public void bugritMilleniumHandAndShrimp() throws OXException, UnsupportedEncodingException, SQLException, OXException, NumberFormatException, OXException, OXException, OXException {
		final String ical =
				"BEGIN:VCALENDAR\n" +
				"METHOD:REQUEST\n" +
				"PRODID:Microsoft CDO for Microsoft Exchange\n" +
				"VERSION:2.0\n" +
				"BEGIN:VTIMEZONE\n" +
					"TZID:(GMT) Greenwich Mean Time/Dublin/Edinburgh/London\n" +
					"X-MICROSOFT-CDO-TZID:1\n" +
					"BEGIN:STANDARD\n" +
						"DTSTART:16010101T020000\n" +
						"TZOFFSETFROM:+0100\n" +
						"TZOFFSETTO:+0000\n" +
						"RRULE:FREQ=YEARLY;WKST=MO;INTERVAL=1;BYMONTH=10;BYDAY=-1SU\n" +
					"END:STANDARD\n" +
					"BEGIN:DAYLIGHT\n" +
						"DTSTART:16010101T010000\n" +
						"TZOFFSETFROM:+0000\n" +
						"TZOFFSETTO:+0100\n" +
						"RRULE:FREQ=YEARLY;WKST=MO;INTERVAL=1;BYMONTH=3;BYDAY=-1SU\n" +
					"END:DAYLIGHT\n" +
				"END:VTIMEZONE\n" +
				"BEGIN:VEVENT\n" +
					"DTSTAMP:20070719T155206Z\n" +
					"DTSTART;TZID=\"(GMT) Greenwich Mean Time/Dublin/Edinburgh/London\":20070724T1\n" +
					" 10000\n" +
					"SUMMARY:Open-Xchange discussion \n" +
					"UID:{BC079982-AB9F-41DF-94B6-51F883933F14}\n" +
					"ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=TRUE;CN=\"Dumbleton\n" +
					" , Steve\":MAILTO:Steve.Dumbleton@colt.net\n" +
					"ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=TRUE;CN=\"stephan.m\n" +
					" artin@open-xchange.com\":MAILTO:stephan.martin@open-xchange.com\n" +
					"ORGANIZER;CN=\"Ijaz, Khurram\":MAILTO:Khurram.Ijaz@colt.net\n" +
					"LOCATION:Conf call (details provided in email)\n" +
					"DTEND;TZID=\"(GMT) Greenwich Mean Time/Dublin/Edinburgh/London\":20070724T113\n" +
					" 000\n" +
					"DESCRIPTION:Stephan\\,\\n\\nAs discussed\\, I am setting up a conf call to foll\n" +
					" ow up on Geoff's discussion with Frank. \\n\\nI have provided the conf call \n" +
					" numbers below. \\n\\nGermany Berlin +49 030726167225 \\nGermany Dusseldorf +4\n" +
					" 9 021154073800 \\nGermany Frankfurt +49 069589990825 \\nGermany Hamburg +49 \n" +
					" 040809020615 \\nGermany Munich +49 089244432763 \\nGermany Stuttgart +49 071\n" +
					" 1490813208 \\n\\nPlease dial any of the above numbers and then dial *8441-19\n" +
					" 93* (please do enter the *). \\n\\nRegards\\,\\nKhurram \\n\nSEQUENCE:2\n" +
					"PRIORITY:5\n" +
					"CLASS:\n" +
					"CREATED:20070719T154738Z\n" +
					"LAST-MODIFIED:20070719T155206Z\n" +
					"STATUS:CONFIRMED\n" +
					"TRANSP:OPAQUE\n" +
					"X-MICROSOFT-CDO-BUSYSTATUS:BUSY\n" +
					"X-MICROSOFT-CDO-INSTTYPE:0\n" +
					"X-MICROSOFT-CDO-INTENDEDSTATUS:BUSY\n" +
					"X-MICROSOFT-CDO-ALLDAYEVENT:FALSE\n" +
					"X-MICROSOFT-CDO-IMPORTANCE:1\n" +
					"X-MICROSOFT-CDO-OWNERAPPTID:-1\n" +
					"X-MICROSOFT-CDO-ATTENDEE-CRITICAL-CHANGE:20070719T154911Z\n" +
					"X-MICROSOFT-CDO-OWNER-CRITICAL-CHANGE:20070719T155206Z\n" +
					"BEGIN:VALARM\n" +
						"ACTION:DISPLAY\n" +
						"DESCRIPTION:REMINDER\n" +
						"TRIGGER;RELATED=START:-PT00H15M00S\n" +
					"END:VALARM\n" +
				"END:VEVENT\n" +
				"END:VCALENDAR";
        final Context ctx = ContextStorage.getInstance().getContext(ContextStorage.getInstance().getContextId("defaultcontext")) ;
        final ImportResult res = performOneEntryCheck(ical, Format.ICAL, FolderObject.CALENDAR, "8475",ctx, false);

		final AppointmentSQLInterface appointmentSql = new CalendarSql(sessObj);
		final int oid = Integer.valueOf( res.getObjectId() );
		final Appointment appointmentObj = appointmentSql.getObjectById(oid, folderId);
		assertTrue("Has participants" , appointmentObj.containsParticipants());
		final Participant[] participants = appointmentObj.getParticipants();
		assertEquals("Has three participants", 3, participants.length);
		boolean foundSteve = false, foundStephan = false;
		for(final Participant p : participants){
			if( "Steve.Dumbleton@colt.net".equals( p.getEmailAddress()) && !foundSteve){
				foundSteve = true;
			}
			if( "stephan.martin@open-xchange.com".equals( p.getEmailAddress()) && !foundSteve){
				foundStephan = true;
			}
		}

		assertTrue("Found attendee #1" , foundSteve);
		assertTrue("Found attendee #2" , foundStephan);

	}
}
