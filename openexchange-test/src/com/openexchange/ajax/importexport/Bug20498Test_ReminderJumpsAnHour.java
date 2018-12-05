
package com.openexchange.ajax.importexport;

import static org.junit.Assert.assertEquals;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.DisableOnDebug;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.importexport.ImportResult;

public class Bug20498Test_ReminderJumpsAnHour extends ManagedAppointmentTest {

    private int appointmentId;

    @Rule
    public TestRule timeout = new DisableOnDebug(new Timeout(2000000000, TimeUnit.MILLISECONDS));

    // @formatter:off
    public String ical =
        "BEGIN:VCALENDAR\n" +
        "PRODID:Strato Communicator 3.5\n" +
        "VERSION:2.0\n" +
        "CALSCALE:GREGORIAN\n" +
        "BEGIN:VTIMEZONE\n" +
        "TZID:Europe/Berlin\n" +
        "X-LIC-LOCATION:Europe/Berlin\n" +
        "BEGIN:DAYLIGHT\n" +
        "TZOFFSETFROM:+0100\n" +
        "TZOFFSETTO:+0200\n" +
        "TZNAME:CEST\n" +
        "DTSTART:19700329T020000\n" +
        "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\n" +
        "END:DAYLIGHT\n" +
        "BEGIN:STANDARD\n" +
        "TZOFFSETFROM:+0200\n" +
        "TZOFFSETTO:+0100\n" +
        "TZNAME:CET\n" +
        "DTSTART:19701025T030000\n" +
        "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\n" +
        "END:STANDARD\n" +
        "END:VTIMEZONE\n" +
        "BEGIN:VEVENT\n" +
        "DTSTAMP:20110930T140717Z\n" +
        "SUMMARY:Geburtstag: Vorname Nachname (01.11.1971)\n" +
        "CLASS:PUBLIC\n" +
        "LAST-MODIFIED:20110930T135935Z\n" +
        "DTEND;VALUE=DATE;TZID=Europe/Berlin:20111102\n" +
        "CREATED:20110930T135935Z\n" +
        "DTSTART;VALUE=DATE;TZID=Europe/Berlin:20111101\n" +
        "RRULE:FREQ=YEARLY;INTERVAL=1\n" +
        "BEGIN:VALARM\n" +
        "ACTION:DISPLAY\n" +
        "TRIGGER:-P2W\n" +
        "DESCRIPTION:Vorname Nachname\n" +
        "END:VALARM\n" +
        "TRANSP:OPAQUE\n" +
        "END:VEVENT\n" +
        "END:VCALENDAR";
    // @formatter:on

    @Override
    @After
    public void tearDown() throws Exception {
        try {
            if (appointmentId > 0) {
                getClient().execute(new DeleteRequest(appointmentId, folder.getObjectID(), new Date(System.currentTimeMillis()), true));
            }
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testReminderTwoWeeksBefore() throws Exception {
        ICalImportRequest importRequest = new ICalImportRequest(folder.getObjectID(), ical);
        ICalImportResponse importResponse = getClient().execute(importRequest);

        ImportResult[] imports = importResponse.getImports();
        assertEquals(1, imports.length);
        appointmentId = Integer.parseInt(imports[0].getObjectId());
        TimeZone tz = getClient().getValues().getTimeZone();

        Appointment actual = getClient().execute(new GetRequest(folder.getObjectID(), appointmentId)).getAppointment(tz);
        int alarmMinutes = getAlarmMinutes(actual, 14);

        assertEquals("Wrong alarm value.", alarmMinutes, actual.getAlarm());
    }

    @Test
    public void testReminderFourDaysBefore() throws Exception {
        ICalImportRequest importRequest = new ICalImportRequest(folder.getObjectID(), ical.replace("-P2W", "-P4D"));
        ICalImportResponse importResponse = getClient().execute(importRequest);

        ImportResult[] imports = importResponse.getImports();
        assertEquals(1, imports.length);
        appointmentId = Integer.parseInt(imports[0].getObjectId());
        TimeZone tz = getClient().getValues().getTimeZone();

        Appointment actual = getClient().execute(new GetRequest(folder.getObjectID(), appointmentId)).getAppointment(tz);
        int alarmMinutes = getAlarmMinutes(actual, 4);

        assertEquals("Wrong alarm value.", alarmMinutes, actual.getAlarm());
    }

    @Test
    public void testReminderFourWeeksBefore() throws Exception {
        ICalImportRequest importRequest = new ICalImportRequest(folder.getObjectID(), ical.replace("-P2W", "-P4W"));
        ICalImportResponse importResponse = getClient().execute(importRequest);

        ImportResult[] imports = importResponse.getImports();
        assertEquals(1, imports.length);
        appointmentId = Integer.parseInt(imports[0].getObjectId());
        TimeZone tz = getClient().getValues().getTimeZone();

        Appointment actual = getClient().execute(new GetRequest(folder.getObjectID(), appointmentId)).getAppointment(tz);
        int alarmMinutes = getAlarmMinutes(actual, 28);

        assertEquals("Wrong alarm value.", alarmMinutes, actual.getAlarm());
    }

    @Test
    public void testReminderWithCombinedTimeBefore() throws Exception {
        ICalImportRequest importRequest = new ICalImportRequest(folder.getObjectID(), ical.replace("-P2W", "-PT1W2D3H4M5S"));
        ICalImportResponse importResponse = getClient().execute(importRequest);

        ImportResult[] imports = importResponse.getImports();
        assertEquals(1, imports.length);
        appointmentId = Integer.parseInt(imports[0].getObjectId());
        TimeZone tz = getClient().getValues().getTimeZone();

        Appointment actual = getClient().execute(new GetRequest(folder.getObjectID(), appointmentId)).getAppointment(tz);

        Date alarmDate = CalendarUtils.add(actual.getStartDate(), Calendar.DATE, -9);
        alarmDate = CalendarUtils.add(alarmDate, Calendar.HOUR, -3);
        alarmDate = CalendarUtils.add(alarmDate, Calendar.MINUTE, -4);
        alarmDate = CalendarUtils.add(alarmDate, Calendar.SECOND, -5);

        int alarmMinutes = (int) ((actual.getStartDate().getTime() - alarmDate.getTime()) / 60000L);

        assertEquals("Wrong alarm value.", alarmMinutes, actual.getAlarm());
    }

    @Test
    public void testBiggerThanOxSupposedlyAllows() throws Exception {
        ICalImportRequest importRequest = new ICalImportRequest(folder.getObjectID(), ical.replace("-P2W", "-PT6W"));
        ICalImportResponse importResponse = getClient().execute(importRequest);

        ImportResult[] imports = importResponse.getImports();
        assertEquals(1, imports.length);
        appointmentId = Integer.parseInt(imports[0].getObjectId());
        TimeZone tz = getClient().getValues().getTimeZone();

        Appointment actual = getClient().execute(new GetRequest(folder.getObjectID(), appointmentId)).getAppointment(tz);
        int alarmMinutes = getAlarmMinutes(actual, 42);

        assertEquals("Wrong alarm value.", alarmMinutes, actual.getAlarm());
    }

    // -----------------------------------------------------------------------------------

    private int getAlarmMinutes(Appointment actual, int days) {
        Date alarmDate = CalendarUtils.add(actual.getStartDate(), Calendar.DATE, -1 * days);
        int alarmMinutes = (int) ((actual.getStartDate().getTime() - alarmDate.getTime()) / 60000L);
        return alarmMinutes;
    }

}
