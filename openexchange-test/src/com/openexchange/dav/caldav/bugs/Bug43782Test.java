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
import java.util.Date;
import java.util.List;
import java.util.Map;
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
 * {@link Bug43782Test}
 *
 * Alarm lost after changing other appointment property via CalDAV
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug43782Test extends CalDAVTest {

    @Test
    public void testUpdateSummary() throws Exception {
        testUpdateSummary(true);
    }

    @Test
    public void testUpdateSummaryRemovingAcknowledged() throws Exception {
        testUpdateSummary(false);
    }

    @Test
    public void testUpdateStartAndEnd() throws Exception {
        testUpdateStartAndEnd(true);
    }

    @Test
    public void testUpdateStartAndEndRemovingAcknowledged() throws Exception {
        testUpdateStartAndEnd(false);
    }

    private void testUpdateSummary(boolean preserveAcknowledged) throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(fetchSyncToken());
        /*
         * create appointment on server
         */
        String uid = randomUID();
        String summary = "test";
        String location = "ort";
        Date start = TimeTools.D("next tuesday at 12:30");
        Date end = TimeTools.D("next tuesday at 13:45");
        Appointment appointment = generateAppointment(start, end, uid, summary, location);
        appointment.setAlarm(15);
        appointment.setAlarmFlag(true);
        rememberForCleanUp(create(appointment));
        /*
         * verify appointment on client
         */
        Map<String, String> eTags = syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<ICalResource> calendarData = calendarMultiget(eTags.keySet());
        ICalResource iCalResource = assertContains(uid, calendarData);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("SUMMARY wrong", summary, iCalResource.getVEvent().getSummary());
        assertEquals("LOCATION wrong", location, iCalResource.getVEvent().getLocation());
        List<Component> vAlarms = iCalResource.getVEvent().getVAlarms();
        assertEquals("Unexpected number of VALARMs found", 1, vAlarms.size());
        assertEquals("ALARM wrong", "-PT15M", vAlarms.get(0).getPropertyValue("TRIGGER"));
        /*
         * update appointment on client
         */
        summary += "_edit";
        iCalResource.getVEvent().setSummary(summary);
        if (false == preserveAcknowledged) {
            iCalResource.getVEvent().getVAlarm().removeProperties("ACKNOWLEDGED");
        }
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(iCalResource));
        /*
         * verify appointment on server
         */
        appointment = getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        assertEquals("Title wrong", summary, appointment.getTitle());
        assertEquals("Start wrong", start, appointment.getStartDate());
        assertEquals("End wrong", end, appointment.getEndDate());
        assertTrue("No reminder found", appointment.containsAlarm());
        assertEquals("Reminder wrong", 15, appointment.getAlarm());
        /*
         * verify appointment on client
         */
        iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertEquals("SUMMARY wrong", summary, iCalResource.getVEvent().getSummary());
        assertEquals("LOCATION wrong", location, iCalResource.getVEvent().getLocation());
        vAlarms = iCalResource.getVEvent().getVAlarms();
        assertEquals("Unexpected number of VALARMs found", 1, vAlarms.size());
        assertEquals("ALARM wrong", "-PT15M", vAlarms.get(0).getPropertyValue("TRIGGER"));
    }

    private void testUpdateStartAndEnd(boolean preserveAcknowledged) throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(fetchSyncToken());
        /*
         * create appointment on server
         */
        String uid = randomUID();
        String summary = "test";
        String location = "ort";
        Date start = TimeTools.D("next tuesday at 12:30");
        Date end = TimeTools.D("next tuesday at 13:45");
        Appointment appointment = generateAppointment(start, end, uid, summary, location);
        appointment.setAlarm(15);
        appointment.setAlarmFlag(true);
        rememberForCleanUp(create(appointment));
        /*
         * verify appointment on client
         */
        Map<String, String> eTags = syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<ICalResource> calendarData = calendarMultiget(eTags.keySet());
        ICalResource iCalResource = assertContains(uid, calendarData);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("SUMMARY wrong", summary, iCalResource.getVEvent().getSummary());
        assertEquals("LOCATION wrong", location, iCalResource.getVEvent().getLocation());
        List<Component> vAlarms = iCalResource.getVEvent().getVAlarms();
        assertEquals("Unexpected number of VALARMs found", 1, vAlarms.size());
        assertEquals("ALARM wrong", "-PT15M", vAlarms.get(0).getPropertyValue("TRIGGER"));
        /*
         * update appointment on client
         */
        start = TimeTools.D("next tuesday at 11:30");
        Property dtStart = iCalResource.getVEvent().getProperty("DTSTART");
        iCalResource.getVEvent().setProperty("DTSTART", null == dtStart.getAttribute("TZID") ? formatAsUTC(start) : format(start, dtStart.getAttribute("TZID")), dtStart.getAttributes());
        end = TimeTools.D("next tuesday at 12:45");
        Property dtEnd = iCalResource.getVEvent().getProperty("DTEND");
        iCalResource.getVEvent().setProperty("DTEND", null == dtEnd.getAttribute("TZID") ? formatAsUTC(end) : format(end, dtEnd.getAttribute("TZID")), dtEnd.getAttributes());
        if (false == preserveAcknowledged) {
            iCalResource.getVEvent().getVAlarm().removeProperties("ACKNOWLEDGED");
        }
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(iCalResource));
        /*
         * verify appointment on server
         */
        appointment = getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        assertEquals("Title wrong", summary, appointment.getTitle());
        assertEquals("Start wrong", start, appointment.getStartDate());
        assertEquals("End wrong", end, appointment.getEndDate());
        assertTrue("No reminder found", appointment.containsAlarm());
        assertEquals("Reminder wrong", 15, appointment.getAlarm());
        /*
         * verify appointment on client
         */
        iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertEquals("SUMMARY wrong", summary, iCalResource.getVEvent().getSummary());
        assertEquals("LOCATION wrong", location, iCalResource.getVEvent().getLocation());
        vAlarms = iCalResource.getVEvent().getVAlarms();
        assertEquals("Unexpected number of VALARMs found", 1, vAlarms.size());
        assertEquals("ALARM wrong", "-PT15M", vAlarms.get(0).getPropertyValue("TRIGGER"));
    }

}
