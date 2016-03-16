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

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.calendar.CalendarSql;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.importexport.formats.Format;

public class Bug8653 extends AbstractICalImportTest {

	//workaround for JUnit 3 runner
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(Bug8653.class);
	}

	@Test public void testImportIntoCorrectFolder() throws OXException, UnsupportedEncodingException, SQLException, OXException, OXException, OXException, OXException {
		final String ical =
			"BEGIN:VCALENDAR\n" +
			"BEGIN:VEVENT\n" +
			"UID:irgendeinschrott\n" +
			"SUMMARY:testtermin-\u00dcberschrift\n" +
			"DESCRIPTION:Bla\n" +
			"LOCATION:Besprechungszimmer\n" +
			"CATEGORIES:ImportTerminGroupwise\n" +
			"STATUS:CONFIRMED\n" +
			"DTSTART;VALUE=DATE;TZID=/Mozilla.org/BasicTimezones/GMT:20070830\n" +
			"DTEND;VALUE=DATE;TZID=/Mozilla.org/BasicTimezones/GMT:20070831\n" +
			"DTSTAMP:20070731T110038Z\n" +
			"END:VEVENT\n" +
			"END:VCALENDAR";
		final Context ctx = ContextStorage.getInstance().getContext(ContextStorage.getInstance().getContextId("defaultcontext")) ;
		final ImportResult res = performOneEntryCheck( ical, Format.ICAL, FolderObject.CALENDAR, "8475", ctx, false);
		final AppointmentSQLInterface appointmentSql = new CalendarSql(sessObj);
		final int oid = Integer.valueOf( res.getObjectId() );
		final Appointment appointmentObj = appointmentSql.getObjectById(oid, folderId);
		assertEquals("Title is correct?","testtermin-\u00dcberschrift",appointmentObj.getTitle());
	}

	@Test public void testImportIntoWrongFolder() throws OXException, UnsupportedEncodingException, SQLException, OXException, OXException, OXException, OXException {
		final String ical =
			"BEGIN:VCALENDAR\n" +
			"BEGIN:VEVENT\n" +
			"UID:irgendeinschrott\n" +
			"SUMMARY:testtermin-\u00dcberschrift\n" +
			"DESCRIPTION:Bla\n" +
			"LOCATION:Besprechungszimmer\n" +
			"CATEGORIES:ImportTerminGroupwise\n" +
			"STATUS:CONFIRMED\n" +
			"DTSTART;VALUE=DATE;TZID=/Mozilla.org/BasicTimezones/GMT:20070830\n" +
			"DTEND;VALUE=DATE;TZID=/Mozilla.org/BasicTimezones/GMT:20070831\n" +
			"DTSTAMP:20070731T110038Z\n" +
			"END:VEVENT\n" +
			"END:VCALENDAR";
		final Context ctx = ContextStorage.getInstance().getContext(ContextStorage.getInstance().getContextId("defaultcontext")) ;
		performMultipleEntryImport( ical, Format.ICAL, FolderObject.TASK, "8475", ctx);
	}
}
