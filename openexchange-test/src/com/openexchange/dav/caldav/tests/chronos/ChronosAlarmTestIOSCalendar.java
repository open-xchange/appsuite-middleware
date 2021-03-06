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

package com.openexchange.dav.caldav.tests.chronos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.UserAgents;
import com.openexchange.dav.caldav.ical.SimpleICal.Component;
import com.openexchange.test.common.groupware.calendar.TimeTools;
import com.openexchange.testing.httpclient.models.EventData;

/**
 * {@link ChronosAlarmTestIOSCalendar}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class ChronosAlarmTestIOSCalendar extends ChronosCaldavTest {

    @Override
    protected String getDefaultUserAgent() {
        return UserAgents.IOS_9_1;
    }

    @Test
    public void testAcknowledgeReminder() throws Exception {
        /*
         * create appointment
         */
        String uid = randomUID();
        Date start = TimeTools.D("next sunday at 16:00");
        Date end = TimeTools.D("next sunday at 17:00");
        String iCal =
            "BEGIN:VCALENDAR\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "PRODID:-//Apple Inc.//iOS 9.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:19810329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
            "TZNAME:MESZ\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:19961027T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
            "TZNAME:MEZ\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "SEQUENCE:0\r\n" +
            "SUMMARY:Dem\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Erinnerung\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "UID:71926843-FB96-440E-B84F-0185F967096D\r\n" +
            "X-WR-ALARMUID:71926843-FB96-440E-B84F-0185F967096D\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(uid, iCal));
        /*
         * verify appointment on server
         */
        verifyEvent(uid, true, "-PT15M");
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
        assertEquals("ALARM wrong", "-PT15M", iCalResource.getVEvent().getVAlarm().getPropertyValue("TRIGGER"));
        /*
         * acknowledge reminder in client
         */
        Date acknowledgedDate = TimeTools.D("next sunday at 15:47:32");
        iCal =
            "BEGIN:VCALENDAR\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "PRODID:-//Apple Inc.//iOS 9.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:19810329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
            "TZNAME:MESZ\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:19961027T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
            "TZNAME:MEZ\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CLASS:PUBLIC\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "SEQUENCE:0\r\n" +
            "SUMMARY:Dem\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(acknowledgedDate) + "\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "UID:3C9EBB4D-8AA0-4B37-B3BB-9EEF8C70F2B0\r\n" +
            "X-WR-ALARMUID:3C9EBB4D-8AA0-4B37-B3BB-9EEF8C70F2B0\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(uid, iCal, iCalResource.getETag()));
        /*
         * verify appointment on server
         */
        verifyEvent(uid, false, "-PT15M");
        /*
         * verify appointment on client
         */
        iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertAcknowledgedOrDummyAlarm(iCalResource.getVEvent(), formatAsUTC(acknowledgedDate));
    }

    @Test
    public void testSnoozeReminder() throws Exception {
        /*
         * create appointment
         */
        String uid = randomUID();
        Date start = TimeTools.D("next sunday at 16:00");
        Date end = TimeTools.D("next sunday at 17:00");
        String iCal =
            "BEGIN:VCALENDAR\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "PRODID:-//Apple Inc.//iOS 9.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:19810329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
            "TZNAME:MESZ\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:19961027T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
            "TZNAME:MEZ\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "SEQUENCE:0\r\n" +
            "SUMMARY:Dem\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Erinnerung\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "UID:71926843-FB96-440E-B84F-0185F967096D\r\n" +
            "X-WR-ALARMUID:71926843-FB96-440E-B84F-0185F967096D\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(uid, iCal));
        /*
         * verify appointment on server
         */
        verifyEvent(uid, true, "-PT15M");
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
        assertEquals("ALARM wrong", "-PT15M", iCalResource.getVEvent().getVAlarm().getPropertyValue("TRIGGER"));
        /*
         * snooze reminder in client
         */
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(TimeTools.D("next sunday at 15:47"));
        calendar.add(Calendar.SECOND, 32);
        Date acknowledgedDate = calendar.getTime();
        iCal =
            "BEGIN:VCALENDAR\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "PRODID:-//Apple Inc.//iOS 9.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:19810329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
            "TZNAME:MESZ\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:19961027T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
            "TZNAME:MEZ\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CLASS:PUBLIC\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "SEQUENCE:0\r\n" +
            "SUMMARY:Dem\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Erinnerung\r\n" +
            "RELATED-TO:429D8792-FA4F-4D60-837F-FF673662ADF3\r\n" +
            "TRIGGER:-PT7M28S\r\n" +
            "UID:48DA570F-2E00-46BD-9CCA-A92D44E07AD8\r\n" +
            "X-WR-ALARMUID:48DA570F-2E00-46BD-9CCA-A92D44E07AD8\r\n" +
            "END:VALARM\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(acknowledgedDate) + "\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "UID:429D8792-FA4F-4D60-837F-FF673662ADF3\r\n" +
            "X-WR-ALARMUID:429D8792-FA4F-4D60-837F-FF673662ADF3\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(uid, iCal, iCalResource.getETag()));
        /*
         * verify appointment on server
         */
        verifyEvent(uid, false, 2);
        /*
         * verify appointment on client
         */
        iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
        List<Component> vAlarms = iCalResource.getVEvent().getVAlarms();
        assertEquals("Unexpected number of VALARMs found", 2, vAlarms.size());
        for (Component vAlarm : vAlarms) {
            if (null != vAlarm.getProperty("RELATED-TO")) {
                assertEquals("ALARM wrong", "-PT7M28S", vAlarm.getPropertyValue("TRIGGER"));
            } else {
                assertEquals("ALARM wrong", "-PT15M", vAlarm.getPropertyValue("TRIGGER"));
            }
        }
    }

    @Test
    public void testEditReminder() throws Exception {
        /*
         * create appointment
         */
        String uid = randomUID();
        Date start = TimeTools.D("next sunday at 16:00");
        Date end = TimeTools.D("next sunday at 17:00");
        String iCal =
            "BEGIN:VCALENDAR\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "PRODID:-//Apple Inc.//iOS 9.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:19810329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
            "TZNAME:MESZ\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:19961027T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
            "TZNAME:MEZ\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "SEQUENCE:0\r\n" +
            "SUMMARY:Dem\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Erinnerung\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "UID:71926843-FB96-440E-B84F-0185F967096D\r\n" +
            "X-WR-ALARMUID:71926843-FB96-440E-B84F-0185F967096D\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(uid, iCal));
        /*
         * verify appointment on server
         */
        verifyEvent(uid, true, "-PT15M");
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
        assertEquals("ALARM wrong", "-PT15M", iCalResource.getVEvent().getVAlarm().getPropertyValue("TRIGGER"));
        /*
         * edit reminder in client
         */
        Date initialAcknowledged = TimeTools.D("next sunday at 15:44");
        iCal =
            "BEGIN:VCALENDAR\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "PRODID:-//Apple Inc.//iOS 9.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:19810329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
            "TZNAME:MESZ\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:19961027T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
            "TZNAME:MEZ\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "SEQUENCE:0\r\n" +
            "SUMMARY:Dem\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(initialAcknowledged) + "\r\n" +
            "DESCRIPTION:Erinnerung\r\n" +
            "TRIGGER:-PT20M\r\n" +
            "UID:71926843-FB96-440E-B84F-0185F967096D\r\n" +
            "X-WR-ALARMUID:71926843-FB96-440E-B84F-0185F967096D\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(uid, iCal, iCalResource.getETag()));
        /*
         * verify appointment on server
         */
        verifyEvent(uid, false, "-PT20M");
        /*
         * verify appointment on client
         */
        iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
        assertEquals("ALARM wrong", "-PT20M", iCalResource.getVEvent().getVAlarm().getPropertyValue("TRIGGER"));
    }

    @Test
    public void testAcknowledgeRecurringReminder() throws Exception {
        /*
         * create appointment
         */
        String uid = randomUID();
        Date start = TimeTools.D("next saturday at 15:30");
        Date end = TimeTools.D("next saturday at 17:15");
        Date initialAcknowledged = TimeTools.D("next saturday at 15:14");
        String iCal =
            "BEGIN:VCALENDAR\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "PRODID:-//Apple Inc.//iOS 9.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:19810329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
            "TZNAME:MESZ\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:19961027T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
            "TZNAME:MEZ\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "RRULE:FREQ=DAILY\r\n" +
            "SEQUENCE:0\r\n" +
            "SUMMARY:Serie\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Erinnerung\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "UID:016232FB-1CA9-4657-99F7-51052C884495\r\n" +
            "X-WR-ALARMUID:016232FB-1CA9-4657-99F7-51052C884495\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(uid, iCal));
        /*
         * verify appointment on server
         */
        verifyEvent(uid, true, "-PT15M");
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
        assertEquals("ALARM wrong", "-PT15M", iCalResource.getVEvent().getVAlarm().getPropertyValue("TRIGGER"));
        /*
         * acknowledge reminder in client
         */
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
        calendar.setTime(initialAcknowledged);
        calendar.add(Calendar.MINUTE, 3);
        calendar.add(Calendar.SECOND, 17);
        Date acknowledgedDate = calendar.getTime();
        iCal =
            "BEGIN:VCALENDAR\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "PRODID:-//Apple Inc.//iOS 9.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:19810329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
            "TZNAME:MESZ\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:19961027T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
            "TZNAME:MEZ\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CLASS:PUBLIC\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "RRULE:FREQ=DAILY\r\n" +
            "SEQUENCE:0\r\n" +
            "SUMMARY:Serie\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(initialAcknowledged) + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(acknowledgedDate) + "\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "UID:F7FCDC9A-BA2A-4548-BC5A-815008F0FC6E\r\n" +
            "X-WR-ALARMUID:F7FCDC9A-BA2A-4548-BC5A-815008F0FC6E\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(uid, iCal, iCalResource.getETag()));
        /*
         * verify appointment on server
         */
        verifyEvent(uid, false, "-PT15M");
        /*
         * verify appointment on client
         */
        iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
        assertEquals("ALARM wrong", "-PT15M", iCalResource.getVEvent().getVAlarm().getPropertyValue("TRIGGER"));
        assertEquals("ACKNOWLEDGED wrong", formatAsUTC(acknowledgedDate), iCalResource.getVEvent().getVAlarm().getPropertyValue("ACKNOWLEDGED"));
    }

    @Test
    public void testSnoozeRecurringReminder() throws Exception {
        /*
         * create appointment
         */
        String uid = randomUID();

        Date start = TimeTools.D("next friday at 10:00");
        Date end = TimeTools.D("next friday at 11:00");
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
        calendar.setTime(start);
        calendar.add(Calendar.DATE, 2);
        calendar.add(Calendar.MINUTE, -16);
        Date seriesAcknowledged = calendar.getTime();
        String iCal =
            "BEGIN:VCALENDAR\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "PRODID:-//Apple Inc.//iOS 9.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:19810329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
            "TZNAME:MESZ\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:19961027T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
            "TZNAME:MEZ\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "RRULE:FREQ=DAILY\r\n" +
            "SEQUENCE:0\r\n" +
            "SUMMARY:Serie\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Erinnerung\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "UID:016232FB-1CA9-4657-99F7-51052C884495\r\n" +
            "X-WR-ALARMUID:016232FB-1CA9-4657-99F7-51052C884495\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(uid, iCal));
        /*
         * verify appointment on server
         */
        verifyEvent(uid, true, "-PT15M");
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
        assertEquals("ALARM wrong", "-PT15M", iCalResource.getVEvent().getVAlarm().getPropertyValue("TRIGGER"));
        /*
         * snooze reminder in client
         */
        calendar.setTime(start);
        calendar.add(Calendar.DATE, 2);
        calendar.add(Calendar.MINUTE, -14);
        calendar.add(Calendar.SECOND, 17);
        Date acknowledgedDate = calendar.getTime(); // 09:46:17
        iCal =
            "BEGIN:VCALENDAR\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "PRODID:-//Apple Inc.//iOS 9.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:19810329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
            "TZNAME:MESZ\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:19961027T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
            "TZNAME:MEZ\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CLASS:PUBLIC\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "RRULE:FREQ=DAILY\r\n" +
            "SEQUENCE:0\r\n" +
            "SUMMARY:Serie\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(seriesAcknowledged) + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(acknowledgedDate) + "\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "UID:F7FCDC9A-BA2A-4548-BC5A-815008F0FC6E\r\n" +
            "X-WR-ALARMUID:F7FCDC9A-BA2A-4548-BC5A-815008F0FC6E\r\n" +
            "END:VALARM\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Erinnerung\r\n" +
            "RELATED-TO:F7FCDC9A-BA2A-4548-BC5A-815008F0FC6E\r\n" +
            "TRIGGER:-PT8M43S\r\n" +
            "UID:48DA570F-2E00-46BD-9CCA-A92D44E07AD8\r\n" +
            "X-WR-ALARMUID:48DA570F-2E00-46BD-9CCA-A92D44E07AD8\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(uid, iCal, iCalResource.getETag()));
        /*
         * verify appointment on server
         */
        verifyEvent(uid, false, 2);
        /*
         * verify appointment on client
         */
        iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
        List<Component> vAlarms = iCalResource.getVEvent().getVAlarms();
        assertEquals("Unexpected number of VALARMs found", 2, vAlarms.size());
        assertEquals("ALARM wrong", "-PT15M", vAlarms.get(0).getPropertyValue("TRIGGER"));
        assertEquals("ACKNOWLEDGED wrong", formatAsUTC(acknowledgedDate), vAlarms.get(0).getPropertyValue("ACKNOWLEDGED"));
        assertNotNull("No RELATED-TO found", vAlarms.get(1).getProperty("RELATED-TO"));
        assertEquals("ALARM wrong", "-PT8M43S", vAlarms.get(1).getPropertyValue("TRIGGER"));
    }

    @Test
    public void testAcknowledgeExceptionReminder() throws Exception {
        /*
         * create appointment
         */
        String uid = randomUID();
        Date start = TimeTools.D("next friday at 10:00");
        Date end = TimeTools.D("next friday at 11:00");
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
        calendar.setTime(start);
        calendar.add(Calendar.DATE, 2);
        Date exceptionStart = calendar.getTime();
        calendar.add(Calendar.HOUR, 1);
        Date exceptionEnd = calendar.getTime();
        calendar.setTime(exceptionStart);
        calendar.add(Calendar.MINUTE, -16);
        calendar.add(Calendar.DATE, 1);
        Date seriesAcknowledged = calendar.getTime();
        String iCal =
            "BEGIN:VCALENDAR\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "PRODID:-//Apple Inc.//iOS 9.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:19810329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
            "TZNAME:MESZ\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:19961027T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
            "TZNAME:MEZ\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CLASS:PUBLIC\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "RRULE:FREQ=DAILY\r\n" +
            "SEQUENCE:0\r\n" +
            "SUMMARY:Serie\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(seriesAcknowledged) + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(seriesAcknowledged) + "\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "UID:AD79E8D2-9D87-4281-BADB-D14C069C463F\r\n" +
            "X-WR-ALARMUID:AD79E8D2-9D87-4281-BADB-D14C069C463F\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(exceptionEnd, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(exceptionStart, "Europe/Berlin") + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "RECURRENCE-ID;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "SEQUENCE:0\r\n" +
            "SUMMARY:SerieEdit\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "UID:AFB070AC-B007-488C-8AAA-0A5F9EE48CBC\r\n" +
            "X-WR-ALARMUID:AFB070AC-B007-488C-8AAA-0A5F9EE48CBC\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(uid, iCal));
        /*
         * verify appointment & exception on server
         */
        EventData event = verifyEvent(uid, true, "-PT15M");

        /*
         * verify appointment & exception on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
        assertEquals("ALARM wrong", "-PT15M", iCalResource.getVEvent().getVAlarm().getPropertyValue("TRIGGER"));
        assertEquals("ACKNOWLEDGED wrong", formatAsUTC(seriesAcknowledged), iCalResource.getVEvent().getVAlarm().getPropertyValue("ACKNOWLEDGED"));
        assertEquals("X-MOZ-LASTACK wrong", formatAsUTC(seriesAcknowledged), iCalResource.getVEvent().getVAlarm().getPropertyValue("X-MOZ-LASTACK"));
        assertEquals("Not all VEVENTs in iCal found", 2, iCalResource.getVEvents().size());
        assertEquals("UID wrong", uid, iCalResource.getVEvents().get(1).getUID());
        assertEquals("SUMMARY wrong", "SerieEdit", iCalResource.getVEvents().get(1).getSummary());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvents().get(1).getVAlarm());
        assertEquals("ALARM wrong", "-PT15M", iCalResource.getVEvents().get(1).getVAlarm().getPropertyValue("TRIGGER"));

        verifyEventException(event.getSeriesId(), 1, getPair(iCalResource.getVEvents().get(1).getVAlarm().getUID(), "-PT15M"));

        /*
         * acknowledge exception reminder in client
         */
        calendar.setTime(exceptionStart);
        calendar.add(Calendar.MINUTE, -14);
        calendar.add(Calendar.SECOND, 52);
        Date exceptionAcknowledged = calendar.getTime();
        iCal =
            "BEGIN:VCALENDAR\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "PRODID:-//Apple Inc.//iOS 9.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:19810329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
            "TZNAME:MESZ\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:19961027T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
            "TZNAME:MEZ\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CLASS:PUBLIC\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "RRULE:FREQ=DAILY\r\n" +
            "SEQUENCE:0\r\n" +
            "SUMMARY:Serie\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(seriesAcknowledged) + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(seriesAcknowledged) + "\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "UID:AD79E8D2-9D87-4281-BADB-D14C069C463F\r\n" +
            "X-WR-ALARMUID:AD79E8D2-9D87-4281-BADB-D14C069C463F\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(exceptionEnd, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(exceptionStart, "Europe/Berlin") + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "RECURRENCE-ID;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "SEQUENCE:0\r\n" +
            "SUMMARY:SerieEdit\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(exceptionAcknowledged) + "\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "UID:AFB070AC-B007-488C-8AAA-0A5F9EE48CBC\r\n" +
            "X-WR-ALARMUID:AFB070AC-B007-488C-8AAA-0A5F9EE48CBC\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(uid, iCal, iCalResource.getETag()));
        /*
         * verify appointment & exception on server
         */
        event = verifyEvent(uid, false, "-PT15M");
        /*
         * verify appointment & exception on client
         */
        iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
        assertEquals("ALARM wrong", "-PT15M", iCalResource.getVEvent().getVAlarm().getPropertyValue("TRIGGER"));
        assertEquals("ACKNOWLEDGED wrong", formatAsUTC(seriesAcknowledged), iCalResource.getVEvent().getVAlarm().getPropertyValue("ACKNOWLEDGED"));
        assertEquals("X-MOZ-LASTACK wrong", formatAsUTC(seriesAcknowledged), iCalResource.getVEvent().getVAlarm().getPropertyValue("X-MOZ-LASTACK"));
        assertEquals("Not all VEVENTs in iCal found", 2, iCalResource.getVEvents().size());
        assertEquals("UID wrong", uid, iCalResource.getVEvents().get(1).getUID());
        assertEquals("SUMMARY wrong", "SerieEdit", iCalResource.getVEvents().get(1).getSummary());
        assertAcknowledgedOrDummyAlarm(iCalResource.getVEvents().get(1), formatAsUTC(exceptionAcknowledged));

        verifyEventException(event.getSeriesId(), 1, getPair(iCalResource.getVEvents().get(1).getVAlarm().getUID(), "-PT15M"));
    }

    @Test
    public void testSnoozeExceptionReminder() throws Exception {
        /*
         * create appointment
         */
        String uid = randomUID();
        Date start = TimeTools.D("next friday at 10:00");
        Date end = TimeTools.D("next friday at 11:00");
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
        calendar.setTime(start);
        calendar.add(Calendar.DATE, 2);
        Date exceptionStart = calendar.getTime();
        calendar.add(Calendar.HOUR, 1);
        Date exceptionEnd = calendar.getTime();
        calendar.setTime(exceptionStart);
        calendar.add(Calendar.MINUTE, -16);
        calendar.add(Calendar.DATE, 1);
        Date seriesAcknowledged = calendar.getTime();
        String iCal =
            "BEGIN:VCALENDAR\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "PRODID:-//Apple Inc.//iOS 9.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:19810329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
            "TZNAME:MESZ\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:19961027T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
            "TZNAME:MEZ\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CLASS:PUBLIC\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "RRULE:FREQ=DAILY\r\n" +
            "SEQUENCE:0\r\n" +
            "SUMMARY:Serie\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(seriesAcknowledged) + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(seriesAcknowledged) + "\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "UID:AD79E8D2-9D87-4281-BADB-D14C069C463F\r\n" +
            "X-WR-ALARMUID:AD79E8D2-9D87-4281-BADB-D14C069C463F\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(exceptionEnd, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(exceptionStart, "Europe/Berlin") + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "RECURRENCE-ID;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "SEQUENCE:0\r\n" +
            "SUMMARY:SerieEdit\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "UID:AFB070AC-B007-488C-8AAA-0A5F9EE48CBC\r\n" +
            "X-WR-ALARMUID:AFB070AC-B007-488C-8AAA-0A5F9EE48CBC\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(uid, iCal));
        /*
         * verify appointment & exception on server
         */
        EventData event = verifyEvent(uid, true, "-PT15M");
        /*
         * verify appointment & exception on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
        assertEquals("ALARM wrong", "-PT15M", iCalResource.getVEvent().getVAlarm().getPropertyValue("TRIGGER"));
        assertEquals("ACKNOWLEDGED wrong", formatAsUTC(seriesAcknowledged), iCalResource.getVEvent().getVAlarm().getPropertyValue("ACKNOWLEDGED"));
        assertEquals("X-MOZ-LASTACK wrong", formatAsUTC(seriesAcknowledged), iCalResource.getVEvent().getVAlarm().getPropertyValue("X-MOZ-LASTACK"));
        assertEquals("Not all VEVENTs in iCal found", 2, iCalResource.getVEvents().size());
        assertEquals("UID wrong", uid, iCalResource.getVEvents().get(1).getUID());
        assertEquals("SUMMARY wrong", "SerieEdit", iCalResource.getVEvents().get(1).getSummary());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvents().get(1).getVAlarm());
        assertEquals("ALARM wrong", "-PT15M", iCalResource.getVEvents().get(1).getVAlarm().getPropertyValue("TRIGGER"));

        verifyEventException(event.getSeriesId(), 1, getPair(iCalResource.getVEvents().get(1).getVAlarm().getUID(), "-PT15M"));
        /*
         * snooze exception reminder in client
         */
        calendar.setTime(exceptionStart);
        calendar.add(Calendar.MINUTE, -14);
        calendar.add(Calendar.SECOND, 52);
        Date exceptionAcknowledged = calendar.getTime();
        iCal =
            "BEGIN:VCALENDAR\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "PRODID:-//Apple Inc.//iOS 9.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:19810329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
            "TZNAME:MESZ\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:19961027T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
            "TZNAME:MEZ\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CLASS:PUBLIC\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "RRULE:FREQ=DAILY\r\n" +
            "SEQUENCE:0\r\n" +
            "SUMMARY:Serie\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(seriesAcknowledged) + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(seriesAcknowledged) + "\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "UID:AD79E8D2-9D87-4281-BADB-D14C069C463F\r\n" +
            "X-WR-ALARMUID:AD79E8D2-9D87-4281-BADB-D14C069C463F\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(exceptionEnd, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(exceptionStart, "Europe/Berlin") + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "RECURRENCE-ID;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "SEQUENCE:0\r\n" +
            "SUMMARY:SerieEdit\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(exceptionAcknowledged) + "\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "UID:AFB070AC-B007-488C-8AAA-0A5F9EE48CBC\r\n" +
            "X-WR-ALARMUID:AFB070AC-B007-488C-8AAA-0A5F9EE48CBC\r\n" +
            "END:VALARM\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Erinnerung\r\n" +
            "RELATED-TO:AFB070AC-B007-488C-8AAA-0A5F9EE48CBC\r\n" +
            "TRIGGER:-PT4M08S\r\n" +
            "UID:48DA570F-2E00-46BD-9CCA-A92D44E07AD8\r\n" +
            "X-WR-ALARMUID:48DA570F-2E00-46BD-9CCA-A92D44E07AD8\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(uid, iCal, iCalResource.getETag()));
        /*
         * verify appointment & exception on server
         */
        event = verifyEvent(uid, false, "-PT15M");
        /*
         * verify appointment & exception on client
         */
        iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
        assertEquals("ALARM wrong", "-PT15M", iCalResource.getVEvent().getVAlarm().getPropertyValue("TRIGGER"));
        assertEquals("ACKNOWLEDGED wrong", formatAsUTC(seriesAcknowledged), iCalResource.getVEvent().getVAlarm().getPropertyValue("ACKNOWLEDGED"));
        assertEquals("X-MOZ-LASTACK wrong", formatAsUTC(seriesAcknowledged), iCalResource.getVEvent().getVAlarm().getPropertyValue("X-MOZ-LASTACK"));
        assertEquals("Not all VEVENTs in iCal found", 2, iCalResource.getVEvents().size());
        assertEquals("UID wrong", uid, iCalResource.getVEvents().get(1).getUID());
        assertEquals("SUMMARY wrong", "SerieEdit", iCalResource.getVEvents().get(1).getSummary());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvents().get(1).getVAlarm());
        List<Component> vAlarms = iCalResource.getVEvents().get(1).getVAlarms();
        assertEquals("Unexpected number of VALARMs found", 2, vAlarms.size());
        String uid1 = null;
        String uid2 = null;
        for (Component vAlarm : vAlarms) {
            if (null != vAlarm.getProperty("RELATED-TO")) {
                assertEquals("ALARM wrong", "-PT4M8S", vAlarm.getPropertyValue("TRIGGER"));
                uid1 = vAlarm.getUID();
            } else {
                assertEquals("ALARM wrong", "-PT15M", vAlarm.getPropertyValue("TRIGGER"));
                uid2 = vAlarm.getUID();
            }
        }

        verifyEventException(event.getSeriesId(), 2, getPair(uid1, "-PT4M8S"), getPair(uid2, "-PT15M"));
    }

}
