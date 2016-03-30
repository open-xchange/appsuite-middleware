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
import com.openexchange.importexport.formats.Format;

public class Bug7732Test extends AbstractICalImportTest {
	//workaround for JUnit 3 runner
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(Bug7732Test.class);
	}


	@Test public void test7732() throws OXException, SQLException, UnsupportedEncodingException, OXException, NumberFormatException, OXException, OXException {
		final int count = 10;
		final String ical =
			"BEGIN:VCALENDAR\n" +
			"PRODID:-//Microsoft Corporation//Outlook 12.0 MIMEDIR//EN\n" +
			"VERSION:2.0\n" +
			"METHOD:PUBLISH\n" +
				"BEGIN:VEVENT\n" +
				"CLASS:PUBLIC\n" +
				"CREATED:20070531T130514Z\n" +
				"DESCRIPTION:\\n\n" +
				"DTEND:20070912T083000Z\n" +
				"DTSTAMP:20070531T130514Z\n" +
				"DTSTART:20070912T080000Z\n" +
				"LAST-MODIFIED:20070531T130514Z\n" +
				"LOCATION:loc\n" +
				"PRIORITY:5\n" +
				"RRULE:FREQ=DAILY;COUNT="+count+"\n" +
				"SEQUENCE:0\n" +
				"SUMMARY;LANGUAGE=de:Daily iCal\n" +
				"TRANSP:OPAQUE\n" +
				"UID:040000008200E00074C5B7101A82E008000000005059CADA94A3C701000000000000000010000000A1B56CAC71BB0948833B0C11C333ADB0\n" +
				"END:VEVENT\n" +
			"END:VCALENDAR";

		final ImportResult res = performOneEntryCheck(ical, Format.ICAL, FolderObject.CALENDAR, "7732", ctx, false);
		final AppointmentSQLInterface appointmentSql = new CalendarSql(sessObj);
		final Appointment appointmentObj = appointmentSql.getObjectById(Integer.parseInt( res.getObjectId() ), folderId);
		assertEquals(count + " occurences found?" , count , appointmentObj.getOccurrence());
	}

	@Test public void testMeaningfulParserMessage() throws Exception {
		final String ical =
			"BEGIN:VCALENDAR\n"+
			"PRODID:-//Microsoft Corporation//Outlook 12.0 MIMEDIR//EN\n"+
			"VERSION:2.0\n"+
			"METHOD:PUBLISH\n"+
			"BEGIN:VEVENT\n"+
			"DESCRIPTION:Daily for 10 occurrences:\n"+
			"DTSTAMP:20070102T053656Z\n"+
			"RRULE:FREQ=DAILY;COUNT=10\n"+
			"SUMMARY:RExample01\n"+
			"UID:RExample01\n"+
			"END:VEVENT\n"+
			"END:VCALENDAR";
		final ImportResult res = performOneEntryCheck(ical, Format.ICAL, FolderObject.CALENDAR, "7732-b", ctx, true);
		assertTrue(res.hasError());
		final OXException x = res.getException();
		x.printStackTrace();

		assertTrue(x.getMessage(), x.getMessage().contains("Missing DTSTART"));

	}

}
