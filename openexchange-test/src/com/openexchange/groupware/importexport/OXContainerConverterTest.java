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

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import junit.framework.TestCase;

import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.tools.versit.Property;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;
import com.openexchange.tools.versit.values.RecurrenceValue;

/**
 * Since the OXContainerConverter has been ported from OX5 to OX Hyperion,
 * it deserves some after-the-fact testing.
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public class OXContainerConverterTest extends TestCase {

	protected SessionObject session;
	protected OXContainerConverter converter;
	protected VersitParserTest parser;
	
	protected void setUp() throws Exception {
		super.setUp();
		session = SessionHelper.getSession();
		converter = new OXContainerConverter(session);
		parser = new VersitParserTest();
	}
	
	public Task convertTask(VersitObject obj) throws ConverterException{
		return converter.convertTask(obj);
	}

	public AppointmentObject convertAppointment(VersitObject obj) throws ConverterException{
		return converter.convertAppointment(obj);
	}

	public ContactObject convertContact(VersitObject obj) throws ConverterException{
		return converter.convertContact(obj);
	}
	
	public void test8411() throws ConverterException{
		AppointmentObject app = new AppointmentObject();
		app.setTitle("Tierlieb's birthday");
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		app.setTimezone("UTC");
		cal.set(1981, 3, 1, 0, 0);
		app.setStartDate(cal.getTime());

		cal.set(1981, 3, 2, 0, 0);
		app.setEndDate(cal.getTime()); //+24 std
		
		app.setShownAs(AppointmentObject.FREE);
		app.setFullTime(true);
		app.setRecurrenceType(AppointmentObject.YEARLY);
		app.setDayInMonth(1);
		app.setMonth(3);
		app.setInterval(1);
		app.setCreatedBy(session.getUserObject().getId());
		
		VersitObject  ical = converter.convertAppointment(app);
		
		Property prop = ical.getProperty("SUMMARY");
		assertEquals("Summary is correct" , "Tierlieb's birthday" , prop.getValue());
		
		prop = ical.getProperty("RRULE");
		RecurrenceValue rv = (RecurrenceValue) prop.getValue();
		assertEquals("Interval is correct" , rv.Freq, RecurrenceValue.YEARLY);
	
	}
	
	public void test7470() throws IOException, ConverterException{
		String ical2 = 
			"BEGIN:VCALENDAR\n" +
			"PRODID:-//Microsoft Corporation//Outlook 12.0 MIMEDIR//EN\n" +
			"VERSION:2.0\n" +
			"METHOD:REQUEST\n" +
			"X-MS-OLK-FORCEINSPECTOROPEN:TRUE\n" +
				"BEGIN:VEVENT\n" +
				"ATTENDEE;CN=\"Camil Bartkowiak (cbartkowiak@oxhemail.open-xchange.com)\";RSVP\n" +
				"	=TRUE:mailto:cbartkowiak@oxhemail.open-xchange.com\n" +
				"CLASS:PUBLIC\n" +
				"CREATED:20070521T150327Z\n" +
				"DESCRIPTION:Hallo Hallo\\n\\n\n" +
				"DTEND:20070523T090000Z\n" +
				"DTSTAMP:20070521T150327Z\n" +
				"DTSTART:20070523T083000Z\n" +
				"LAST-MODIFIED:20070521T150327Z\n" +
				"LOCATION:Location here\n" +
				"ORGANIZER;CN=Tobias:mailto:tfriedrich@oxhemail.open-xchange.com\n" +
				"PRIORITY:5\n" +
				"SEQUENCE:0\n" +
				"SUMMARY;LANGUAGE=de:Simple Appointment with participant\n" +
				"TRANSP:OPAQUE\n" +
				"UID:040000008200E00074C5B7101A82E0080000000060565ABBC99BC701000000000000000\n" +
				"	010000000E4B2BA931D32B84DAFB227C9E0CA348C\n" +
				"X-ALT-DESC;FMTTYPE=text/html:<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2//E\n	N\">\\n<HTML>\\n<HEAD>\\n<META NAME=\"Generator\" CONTENT=\"MS Exchange Server ve\\n	rsion 08.00.0681.000\">\\n<TITLE></TITLE>\\n</HEAD>\\n<BODY>\\n<!-- Converted f\n	rom text/rtf format -->\\n\\n<P DIR=LTR><SPAN LANG=\"de\"><FONT FACE=\"Calibri\"\n	>Hallo Hallo</FONT></SPAN></P>\\n\\n<P DIR=LTR><SPAN LANG=\"de\"></SPAN></P>\\n\\n	\\n</BODY>\\n</HTML>\n" +
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
			"END:VCALENDAR\n";
		
		
		List<VersitObject> list = parser.parse(ical2);
		AppointmentObject obj = convertAppointment( list.get(1) );
		Participant[] participants = obj.getParticipants();
		assertEquals("One participant?" , participants.length, 1);
		assertEquals("User is the right one?" , "cbartkowiak@oxhemail.open-xchange.com", participants[0].getEmailAddress());
	}
}
