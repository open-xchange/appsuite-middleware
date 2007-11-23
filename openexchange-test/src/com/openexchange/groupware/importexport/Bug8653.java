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

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.List;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.calendar.CalendarSql;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.DBPoolingException;

public class Bug8653 extends AbstractICalImportTest {
	
	//workaround for JUnit 3 runner
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(Bug8653.class);
	}
	
	@Test public void testImportIntoCorrectFolder() throws DBPoolingException, UnsupportedEncodingException, SQLException, OXObjectNotFoundException, OXException{
		final String ical =
			"BEGIN:VCALENDAR\n" +
			"BEGIN:VEVENT\n" +
			"UID:irgendeinschrott\n" +
			"SUMMARY:testtermin-Überschrift\n" +
			"DESCRIPTION:Bla\n" +
			"LOCATION:Besprechungszimmer\n" +
			"CATEGORIES:ImportTerminGroupwise\n" +
			"STATUS:CONFIRMED\n" +
			"DTSTART;VALUE=DATE;TZID=/Mozilla.org/BasicTimezones/GMT:20070830\n" +
			"DTEND;VALUE=DATE;TZID=/Mozilla.org/BasicTimezones/GMT:20070831\n" +
			"DTSTAMP:20070731T110038Z\n" +
			"END:VEVENT\n" +
			"END:VCALENDAR";
		
		ImportResult res = performOneEntryCheck( ical, Format.ICAL, FolderObject.CALENDAR, "8475", false);
		final AppointmentSQLInterface appointmentSql = new CalendarSql(sessObj);
		final int oid = Integer.valueOf( res.getObjectId() );
		final AppointmentObject appointmentObj = appointmentSql.getObjectById(oid, folderId);
		assertEquals("Title is correct?","testtermin-Überschrift",appointmentObj.getTitle());
	}
	
	@Test public void testImportIntoWrongFolder() throws DBPoolingException, UnsupportedEncodingException, SQLException, OXObjectNotFoundException, OXException{
		final String ical =
			"BEGIN:VCALENDAR\n" +
			"BEGIN:VEVENT\n" +
			"UID:irgendeinschrott\n" +
			"SUMMARY:testtermin-Überschrift\n" +
			"DESCRIPTION:Bla\n" +
			"LOCATION:Besprechungszimmer\n" +
			"CATEGORIES:ImportTerminGroupwise\n" +
			"STATUS:CONFIRMED\n" +
			"DTSTART;VALUE=DATE;TZID=/Mozilla.org/BasicTimezones/GMT:20070830\n" +
			"DTEND;VALUE=DATE;TZID=/Mozilla.org/BasicTimezones/GMT:20070831\n" +
			"DTSTAMP:20070731T110038Z\n" +
			"END:VEVENT\n" +
			"END:VCALENDAR";
		
		List<ImportResult> res = performMultipleEntryImport( ical, Format.ICAL, FolderObject.TASK, "8475");
	}
}
