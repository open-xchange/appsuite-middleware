/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.dav.caldav.bugs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
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
import com.openexchange.dav.caldav.ical.SimpleICal.Component;
import com.openexchange.dav.caldav.ical.SimpleICal.Property;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link Bug22395Test} - Change exceptions created in iCal client appear one day off
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug22395Test extends CalDAVTest {

    @Test
    public void testDateOfChangeExceptions() throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(super.fetchSyncToken());
        /*
         * create appointment series on server
         */
        TimeZone timeZone = getClient().getValues().getTimeZone();
        List<Appointment> appointments = new ArrayList<Appointment>();
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTime(TimeTools.D("Tomorrow at midnight", timeZone));
        for (int i = 0; i < 24; i++) {
            Appointment appointment = new Appointment();
            appointment.setTimezone(timeZone.getID());
            appointment.setUid(randomUID());
            appointment.setTitle("Series " + i);
            appointment.setIgnoreConflicts(true);
            appointment.setStartDate(calendar.getTime());
            calendar.add(Calendar.HOUR_OF_DAY, 1);
            appointment.setEndDate(calendar.getTime());
            appointment.setRecurrenceType(Appointment.DAILY);
            appointment.setInterval(1);
            super.create(appointment);
            appointments.add(appointment);
        }
        Date clientLastModified = getManager().getLastModification();
        /*
         * verify appointment series on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<ICalResource> calendarData = super.calendarMultiget(eTags.keySet());
        for (Appointment appointment : appointments) {
            ICalResource iCalResource = assertContains(appointment.getUid(), calendarData);
            assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
            assertEquals("SUMMARY wrong", appointment.getTitle(), iCalResource.getVEvent().getSummary());
            assertEquals("DTSTART wrong", appointment.getStartDate(), iCalResource.getVEvent().getDTStart());
            assertEquals("DTEND wrong", appointment.getEndDate(), iCalResource.getVEvent().getDTEnd());
        }
        /*
         * create exceptions on client (3rd occurrence in the series)
         */
        for (ICalResource iCalResource : calendarData) {
            Component exception = new Component(ICalResource.VEVENT);
            exception.setProperty("CREATED", formatAsUTC(new Date()));
            exception.setProperty("UID", iCalResource.getVEvent().getUID());
            calendar.setTime(iCalResource.getVEvent().getDTStart());
            calendar.add(Calendar.DAY_OF_YEAR, 2);
            Date exceptionStart = calendar.getTime();
            Property dtStart = iCalResource.getVEvent().getProperty("DTSTART");
            exception.setProperty("DTSTART", null == dtStart.getAttribute("TZID") ? formatAsUTC(exceptionStart) : format(exceptionStart, dtStart.getAttribute("TZID")), dtStart.getAttributes());
            calendar.setTime(iCalResource.getVEvent().getDTEnd());
            calendar.add(Calendar.DAY_OF_YEAR, 2);
            Date exceptionEnd = calendar.getTime();
            Property dtEnd = iCalResource.getVEvent().getProperty("DTEND");
            exception.setProperty("DTEND", null == dtEnd.getAttribute("TZID") ? formatAsUTC(exceptionEnd) : format(exceptionEnd, dtEnd.getAttribute("TZID")), dtEnd.getAttributes());
            exception.setProperty("TRANSP", "OPAQUE");
            exception.setProperty("SUMMARY", iCalResource.getVEvent().getPropertyValue("SUMMARY") + "_edit");
            exception.setProperty("DTSTAMP", formatAsUTC(new Date()));
            exception.setProperty("SEQUENCE", "3");
            exception.setProperty("RECURRENCE-ID", null == dtStart.getAttribute("TZID") ? formatAsUTC(exceptionStart) : format(exceptionStart, dtStart.getAttribute("TZID")), dtStart.getAttributes());
            iCalResource.addComponent(exception);
            assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putICalUpdate(iCalResource));
        }
        /*
         * verify exceptions on server
         */
        List<Appointment> updates = super.getManager().updates(parse(getDefaultFolderID()), clientLastModified, true);
        assertNotNull("no updates found on server", updates);
        assertTrue("no updated appointments on server", 0 < updates.size());
        for (Appointment appointment : appointments) {
            calendar.setTime(appointment.getStartDate());
            calendar.add(Calendar.DAY_OF_YEAR, 2);
            Date expectedStart = calendar.getTime();
            calendar.setTime(appointment.getEndDate());
            calendar.add(Calendar.DAY_OF_YEAR, 2);
            Date expectedEnd = calendar.getTime();
            Appointment exception = null;
            for (Appointment update : updates) {
                if (appointment.getObjectID() != update.getObjectID() && appointment.getUid().equals(update.getUid())) {
                    exception = update;
                    break;
                }
            }
            assertNotNull("Exception not found", exception);
            assertEquals("Title wrong", appointment.getTitle() + "_edit", exception.getTitle());
            assertEquals("Start date wrong", expectedStart, exception.getStartDate());
            assertEquals("End date wrong", expectedEnd, exception.getEndDate());
            assertEquals("Recurrence date position wrong", 3, exception.getRecurrencePosition());
        }
        /*
         * verify appointment series on client
         */
        eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        calendarData = super.calendarMultiget(eTags.keySet());
        for (Appointment appointment : appointments) {
            calendar.setTime(appointment.getStartDate());
            calendar.add(Calendar.DAY_OF_YEAR, 2);
            Date expectedStart = calendar.getTime();
            calendar.setTime(appointment.getEndDate());
            calendar.add(Calendar.DAY_OF_YEAR, 2);
            Date expectedEnd = calendar.getTime();
            ICalResource iCalResource = assertContains(appointment.getUid(), calendarData);
            assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
            assertEquals("No exception found in iCal", 2, iCalResource.getVEvents().size());
            for (Component vEvent : iCalResource.getVEvents()) {
                Date recurrenceID = vEvent.getRecurrenceID();
                if (null != recurrenceID) {
                    // exception
                    assertEquals("SUMMARY wrong", appointment.getTitle() + "_edit", vEvent.getSummary());
                    assertEquals("DTSTART wrong", expectedStart, vEvent.getDTStart());
                    assertEquals("DTEND wrong", expectedEnd, vEvent.getDTEnd());
                    assertEquals("RECURRENCE-ID wrong", expectedStart, recurrenceID);
                } else {
                    // master
                    assertEquals("SUMMARY wrong", appointment.getTitle(), vEvent.getSummary());
                    assertEquals("DTSTART wrong", appointment.getStartDate(), vEvent.getDTStart());
                    assertEquals("DTEND wrong", appointment.getEndDate(), vEvent.getDTEnd());
                }
            }

        }
    }

}
