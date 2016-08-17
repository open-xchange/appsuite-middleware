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

package com.openexchange.dav.caldav.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link NewTest} - Tests appointment creation via the CalDAV interface
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class NewTest extends CalDAVTest {

	@Test
	public void testCreateSimpleOnClient() throws Exception {
		/*
		 * create appointment on client
		 */
    	String uid = randomUID();
    	String summary = "test";
    	String location = "testcity";
    	Date start = TimeTools.D("tomorrow at 3pm");
    	Date end = TimeTools.D("tomorrow at 4pm");
    	String iCal = generateICal(start, end, uid, summary, location);
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putICal(uid, iCal));
        /*
         * verify appointment on server
         */
        Appointment appointment = super.getAppointment(uid);
        super.rememberForCleanUp(appointment);
        assertAppointmentEquals(appointment, start, end, uid, summary, location);
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertEquals("SUMMARY wrong", summary, iCalResource.getVEvent().getSummary());
        assertEquals("LOCATION wrong", location, iCalResource.getVEvent().getLocation());
	}

	@Test
	public void testCreateSimpleOnServer() throws Exception {
		/*
		 * fetch sync token for later synchronization
		 */
		SyncToken syncToken = new SyncToken(super.fetchSyncToken());
		/*
		 * create appointment on server
		 */
    	String uid = randomUID();
    	String summary = "hallo";
    	String location = "achtung";
    	Date start = TimeTools.D("next friday at 11:30");
    	Date end = TimeTools.D("next friday at 12:45");
		Appointment appointment = generateAppointment(start, end, uid, summary, location);
		super.rememberForCleanUp(super.create(appointment));
        /*
         * verify appointment on client
         */
		Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<ICalResource> calendarData = super.calendarMultiget(eTags.keySet());
        ICalResource iCalResource = assertContains(uid, calendarData);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("SUMMARY wrong", summary, iCalResource.getVEvent().getSummary());
        assertEquals("LOCATION wrong", location, iCalResource.getVEvent().getLocation());
	}

	@Test
	public void testCreateAllDayOnClient() throws Exception {
		/*
		 * create appointment on client
		 */
    	String uid = randomUID();
    	String summary = "test all day";
    	Date start = TimeTools.D("midnight");
    	Date end = TimeTools.D("tomorrow at midnight");
    	String iCal =
    			"BEGIN:VCALENDAR" + "\r\n" +
    			"VERSION:2.0" + "\r\n" +
    			"PRODID:-//Apple Inc.//iCal 5.0.2//EN" + "\r\n" +
    			"CALSCALE:GREGORIAN" + "\r\n" +
    			"BEGIN:VEVENT" + "\r\n" +
    			"CREATED:" + formatAsUTC(new Date()) + "\r\n" +
    			"UID:" + uid + "\r\n" +
    			"DTEND;VALUE=DATE:" + formatAsDate(end) + "\r\n" +
    			"TRANSP:OPAQUE" + "\r\n" +
    			"CLASS:PUBLIC" + "\r\n" +
    			"SUMMARY:" + summary + "\r\n" +
    			"LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
    			"DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
    			"DTSTART;VALUE=DATE:" + formatAsDate(start) + "\r\n" +
    			"SEQUENCE:0" + "\r\n" +
    			"END:VEVENT" + "\r\n" +
    			"END:VCALENDAR"
		;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putICal(uid, iCal));
        /*
         * verify appointment on server
         */
        Appointment appointment = super.getAppointment(uid);
        assertNotNull("appointmnet not found on server", appointment);
        super.rememberForCleanUp(appointment);
        assertEquals("title wrong", summary, appointment.getTitle());
        assertTrue("full time wrong", appointment.getFullTime());
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertEquals("SUMMARY wrong", summary, iCalResource.getVEvent().getSummary());
        assertEquals("START wrong", start, iCalResource.getVEvent().getDTStart());
        assertEquals("END wrong", end, iCalResource.getVEvent().getDTEnd());
	}

	@Test
	public void testAllDayOnServer() throws Exception {
		/*
		 * fetch sync token for later synchronization
		 */
		SyncToken syncToken = new SyncToken(super.fetchSyncToken());
		/*
		 * create appointment on server
		 */
    	String uid = randomUID();
    	String summary = "all day";
    	String location = "testing";
    	Date start = TimeTools.D("next monday at midnight");
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(start);
    	calendar.add(Calendar.DAY_OF_YEAR, 1);
    	Date end = calendar.getTime();
		Appointment appointment = generateAppointment(start, end, uid, summary, location);
		appointment.setFullTime(true);
		super.rememberForCleanUp(super.create(appointment));
        /*
         * verify appointment on client
         */
		Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<ICalResource> calendarData = super.calendarMultiget(eTags.keySet());
        ICalResource iCalResource = assertContains(uid, calendarData);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("SUMMARY wrong", summary, iCalResource.getVEvent().getSummary());
        assertEquals("LOCATION wrong", location, iCalResource.getVEvent().getLocation());
        assertEquals("START wrong", start, iCalResource.getVEvent().getDTStart());
        assertEquals("END wrong", end, iCalResource.getVEvent().getDTEnd());
	}

	@Test
	public void testCreateWithDifferentName() throws Exception {
		/*
		 * create appointment on client
		 */
    	String resourceName = randomUID();
    	String uid = randomUID();
    	String summary = "test with filename";
    	String location = "loco";
    	Date start = TimeTools.D("last sunday at 2am");
    	Date end = TimeTools.D("last sunday at 7am");
    	String iCal = generateICal(start, end, uid, summary, location);
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putICal(resourceName, iCal));
        /*
         * verify appointment on server
         */
        Appointment appointment = super.getAppointment(uid);
        super.rememberForCleanUp(appointment);
        assertAppointmentEquals(appointment, start, end, uid, summary, location);
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertEquals("SUMMARY wrong", summary, iCalResource.getVEvent().getSummary());
        assertEquals("LOCATION wrong", location, iCalResource.getVEvent().getLocation());
	}

}
