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

package com.openexchange.ajax.importexport;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONException;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Appointment;

/**
 * @author tobiasp
 */
public class ICalSeriesTests extends ManagedAppointmentTest {

	public ICalSeriesTests(String name) {
		super(name);
	}

	public void testDeleteException() throws OXException, IOException, JSONException{
		String ical =
		"BEGIN:VCALENDAR\n"+
		"VERSION:2.0\n"+
		"BEGIN:VEVENT\n"+
		"DTSTART;TZID=Europe/Rome:20100202T103000\n"+
		"DTEND;TZID=Europe/Rome:20100202T120000\n"+
		"RRULE:FREQ=DAILY;UNTIL=20100204T215959Z\n"+
		"EXDATE:20100203T103000\n"+
		"DTSTAMP:20110105T174810Z\n"+
		"SUMMARY:Exceptional Meeting #1\n"+
		"END:VEVENT\n";

		AJAXClient client = getClient();
		int fid = folder.getObjectID();
		TimeZone tz = client.getValues().getTimeZone();

		ICalImportRequest request = new ICalImportRequest(fid, ical);
		client.execute(request);

		AllRequest allRequest = new AllRequest(fid, Appointment.ALL_COLUMNS, D("2010-02-03 00:00", tz), D("2010-02-04 00:00", tz), tz, false);
		CommonAllResponse response2 = client.execute(allRequest);

		Object[][] data = response2.getArray();
		assertEquals(0,data.length);
	}

	public void testChangeExceptionWithExceptionFirst() throws Exception{
		String uid = "change-exception-"+new Date().getTime();
		String title = "Change to exceptional meeting #3: One hour later";
		String ical =
		"BEGIN:VCALENDAR\n"+
		"VERSION:2.0\n"+

		"BEGIN:VEVENT\n"+
		"DTSTART;TZID=Europe/Rome:20100204T113000\n"+
		"DTEND;TZID=Europe/Rome:20100204T130000\n"+
		"DTSTAMP:20110105T174810Z\n"+
		"SUMMARY:"+title+"\n"+
		"UID:"+uid+"\n"+
		"END:VEVENT\n"+

		"BEGIN:VEVENT\n"+
		"DTSTART;TZID=Europe/Rome:20100202T103000\n"+
		"DTEND;TZID=Europe/Rome:20100202T120000\n"+
		"RRULE:FREQ=DAILY;UNTIL=20100228T215959Z\n"+
		"DTSTAMP:20110105T174810Z\n"+
		"SUMMARY:Exceptional meeting #3\n"+
		"UID:"+uid+"\n"+
		"END:VEVENT\n";

		TimeZone tz = TimeZone.getTimeZone("GMT");

		Date start = D("2010-02-04 00:00", tz);
		Date end = D("2010-02-05 00:00", tz);

		testChangeException(ical, title, start, end);
	}

	public void testChangeExceptionWithMasterFirst() throws Exception{
		String uid = "change-exception-"+new Date().getTime();

		String title = "Change to exceptional meeting #2: Five hours later";
		String ical =
		"BEGIN:VCALENDAR\n"+
		"VERSION:2.0\n"+

		"BEGIN:VEVENT\n"+
		"DTSTART;TZID=Europe/Rome:20100202T110000\n"+
		"DTEND;TZID=Europe/Rome:20100202T120000\n"+
		"RRULE:FREQ=DAILY;UNTIL=20100228T215959Z\n"+
		"DTSTAMP:20110105T174810Z\n"+
		"SUMMARY:Exceptional meeting #2\n"+
		"UID:"+uid+"\n"+
		"END:VEVENT\n" +

		"BEGIN:VEVENT\n"+
		"DTSTART;TZID=Europe/Rome:20100204T160000\n"+
		"DTEND;TZID=Europe/Rome:20100204T170000\n"+
		"DTSTAMP:20110105T174810Z\n"+
		"SUMMARY:"+title+"\n"+
		"UID:"+uid+"\n"+
		"END:VEVENT\n";

		TimeZone tz = TimeZone.getTimeZone("GMT");
		Date start = D("2010-02-04 00:00", tz);
		Date end = D("2010-02-05 00:00", tz);

		testChangeException(ical, title, start, end);
	}

	protected void testChangeException(String ical, String expectedTitle, Date start, Date end) throws Exception{
		AJAXClient client = getClient();
		int fid = folder.getObjectID();
		TimeZone tz = client.getValues().getTimeZone();

		ICalImportRequest request = new ICalImportRequest(fid, ical);
		client.execute(request);

		AllRequest allRequest = new AllRequest(fid, new int[]{Appointment.OBJECT_ID}, start, end, tz, false);
		CommonAllResponse response2 = client.execute(allRequest);

		Object[][] data = response2.getArray();
		assertEquals(1,data.length);

		int oid = (Integer) data[0][response2.getColumnPos(ContactField.OBJECT_ID.getNumber())];
		GetRequest getRequest = new GetRequest(fid, oid);
		GetResponse getResponse = client.execute(getRequest);

		Appointment actual = getResponse.getAppointment(tz);
		assertEquals(expectedTitle, actual.getTitle());
	}

}
