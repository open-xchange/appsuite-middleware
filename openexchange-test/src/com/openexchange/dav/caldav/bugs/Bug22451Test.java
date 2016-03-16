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

package com.openexchange.dav.caldav.bugs;

import static org.junit.Assert.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.ical.SimpleICal.Property;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link Bug22451Test} - Wrong until date after exporting and importing recurring appointment
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug22451Test extends CalDAVTest {

	@Test
	public void testUntilDate() throws Exception {
		/*
		 * fetch sync token for later synchronization
		 */
		SyncToken syncToken = new SyncToken(super.fetchSyncToken());
		/*
		 * create appointment series on server
		 */
		List<Appointment> appointments = new ArrayList<Appointment>();
		Calendar calendar = Calendar.getInstance();
        calendar.setTime(TimeTools.D("3 days after midnight", TimeZone.getTimeZone("UTC")));
        Date serverUntil = calendar.getTime();
		calendar.setTime(TimeTools.D("Tomorrow at midnight", TimeZone.getTimeZone("Europe/Berlin")));
		for (int i = 0; i < 24; i++) {
		    Appointment appointment = new Appointment();
		    appointment.setUid(randomUID());
		    appointment.setTitle("Series " + i);
		    appointment.setIgnoreConflicts(true);
		    appointment.setStartDate(calendar.getTime());
		    calendar.add(Calendar.HOUR_OF_DAY, 1);
            appointment.setEndDate(calendar.getTime());
		    appointment.setRecurrenceType(Appointment.DAILY);
		    appointment.setInterval(1);
		    appointment.setUntil(serverUntil);
	        super.create(appointment);
            appointments.add(appointment);
		}
		Date clientLastModified = getManager().getLastModification();
        /*
         * verify appointment series on client
         */
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Calendar userCalendar = Calendar.getInstance(getClient().getValues().getTimeZone());
		Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<ICalResource> calendarData = super.calendarMultiget(eTags.keySet());
        for (Appointment appointment : appointments) {
            ICalResource iCalResource = assertContains(appointment.getUid(), calendarData);
            assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
            assertEquals("SUMMARY wrong", appointment.getTitle(), iCalResource.getVEvent().getSummary());
            assertEquals("DTSTART wrong", appointment.getStartDate(), iCalResource.getVEvent().getDTStart());
            assertEquals("DTEND wrong", appointment.getEndDate(), iCalResource.getVEvent().getDTEnd());
            Property rruleProperty = iCalResource.getVEvent().getProperty("RRULE");
            assertNotNull("RRULE not found", rruleProperty);
            int startIndex = rruleProperty.getValue().indexOf("UNTIL=") + 6;
            int endIndex = rruleProperty.getValue().indexOf(";", startIndex);
            String iCalUntil = 0 < endIndex ? rruleProperty.getValue().substring(startIndex, endIndex) :
                rruleProperty.getValue().substring(startIndex);
            userCalendar.setTime(dateFormat.parse(iCalUntil));
            calendar.setTime(appointment.getUntil());
            assertEquals("UNTIL date wrong", calendar.get(Calendar.DATE), userCalendar.get(Calendar.DATE));
        }
        /*
         * update appointments on client
         */
        for (ICalResource iCalResource : calendarData) {
            iCalResource.getVEvent().setSummary(iCalResource.getVEvent().getSummary() + "_edit");
            assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putICalUpdate(iCalResource));
        }
        /*
         * verify appointments on server
         */
        List<Appointment> updates = super.getManager().updates(parse(getDefaultFolderID()), clientLastModified, false);
        assertNotNull("no updates found on server", updates);
        assertTrue("no updated appointments on server", 0 < updates.size());
        for (Appointment appointment : appointments) {
            Appointment updatedAppointment = null;
            for (Appointment update : updates) {
                if (appointment.getObjectID() == update.getObjectID()) {
                    updatedAppointment = update;
                    break;
                }
            }
            assertNotNull("Exception not found", updatedAppointment);
            assertEquals("Title wrong", appointment.getTitle() + "_edit", updatedAppointment.getTitle());
            assertEquals("Until date wrong", appointment.getUntil(), updatedAppointment.getUntil());
        }
        /*
         * verify appointment series on client
         */
        eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        calendarData = super.calendarMultiget(eTags.keySet());
        for (Appointment appointment : appointments) {
            ICalResource iCalResource = assertContains(appointment.getUid(), calendarData);
            assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
            assertEquals("SUMMARY wrong", appointment.getTitle() + "_edit", iCalResource.getVEvent().getSummary());
            Property rruleProperty = iCalResource.getVEvent().getProperty("RRULE");
            assertNotNull("RRULE not found", rruleProperty);
            int startIndex = rruleProperty.getValue().indexOf("UNTIL=") + 6;
            int endIndex = rruleProperty.getValue().indexOf(";", startIndex);
            String iCalUntil = 0 < endIndex ? rruleProperty.getValue().substring(startIndex, endIndex) :
                rruleProperty.getValue().substring(startIndex);
            userCalendar.setTime(dateFormat.parse(iCalUntil));
            calendar.setTime(appointment.getUntil());
            assertEquals("UNTIL date wrong", calendar.get(Calendar.DATE), userCalendar.get(Calendar.DATE));
        }
	}

}
