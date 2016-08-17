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
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;

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
        iCalResource.getVEvent().setProperty("DTSTART", format(start, dtStart.getAttribute("TZID")), dtStart.getAttributes());
        end = TimeTools.D("next tuesday at 12:45");
        Property dtEnd = iCalResource.getVEvent().getProperty("DTEND");
        iCalResource.getVEvent().setProperty("DTEND", format(end, dtEnd.getAttribute("TZID")), dtEnd.getAttributes());
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
