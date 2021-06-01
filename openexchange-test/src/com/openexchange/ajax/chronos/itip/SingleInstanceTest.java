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

package com.openexchange.ajax.chronos.itip;

import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertAttendeePartStat;
import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertSingleChange;
import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertSingleEvent;
import static com.openexchange.ajax.chronos.itip.ITipUtil.constructBody;
import static com.openexchange.ajax.chronos.itip.ITipUtil.parseICalAttachment;
import static com.openexchange.ajax.chronos.itip.ITipUtil.receiveIMip;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Pattern;
import org.junit.Test;
import com.google.common.io.BaseEncoding;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.ical.ImportedCalendar;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.java.Charsets;
import com.openexchange.testing.httpclient.models.AnalysisChangeDeletedEvent;
import com.openexchange.testing.httpclient.models.AnalysisChangeNewEvent;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventResponse;
import com.openexchange.testing.httpclient.models.MailData;
import com.openexchange.testing.httpclient.models.MailDestinationResponse;
import com.openexchange.testing.httpclient.models.UserData;
import com.openexchange.testing.httpclient.modules.MailApi;

/**
 * {@link SingleInstanceTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.6
 */
public class SingleInstanceTest extends AbstractITipTest {

    protected static final String IMIP_TEMPLATE = // @formatter:off 
        "Return-Path: {{FROM}}" + "\r\n" +
        "Delivered-To: {{TO}}" + "\r\n" +
        "From: {{FROM}}" + "\r\n" +
        "To: {{TO}}" + "\r\n" +
        "Subject: {{SUBJECT}}" + "\r\n" +
        "Date: {{DATE}}" + "\r\n" +
        "Message-ID: <{{MESSAGE_ID}}@WINHEXBEEU109.win.mail>" + "\r\n" +
        "Accept-Language: de-DE, en-US" + "\r\n" +
        "Content-Language: de-DE" + "\r\n" +
        "Content-Type: multipart/alternative;" + "\r\n" +
        "   boundary=\"_000_7852067692f94262ac04e48a013322e2WINHEXBEEU109winmail_\"" + "\r\n" +
        "MIME-Version: 1.0" + "\r\n" +
        "X-Spam-Flag: NO" + "\r\n" +
        "" + "\r\n" +
        "--_000_7852067692f94262ac04e48a013322e2WINHEXBEEU109winmail_" + "\r\n" +
        "Content-Type: text/plain; charset=\"iso-8859-1\"" + "\r\n" +
        "Content-Transfer-Encoding: quoted-printable" + "\r\n" +
        "" + "\r\n" +
        "" + "\r\n" +
        "" + "\r\n" +
        "--_000_7852067692f94262ac04e48a013322e2WINHEXBEEU109winmail_" + "\r\n" +
        "Content-Type: text/html; charset=\"iso-8859-1\"" + "\r\n" +
        "Content-Transfer-Encoding: quoted-printable" + "\r\n" +
        "" + "\r\n" +
        "<html>" + "\r\n" +
        "<head>" + "\r\n" +
        "<meta http-equiv=3D\"Content-Type\" content=3D\"text/html; charset=3Diso-8859-=" + "\r\n" +
        "1\">" + "\r\n" +
        "<style type=3D\"text/css\" style=3D\"display:none\"><!-- p { margin-top: 0px; m=" + "\r\n" +
        "argin-bottom: 0px; }--></style>" + "\r\n" +
        "</head>" + "\r\n" +
        "<body dir=3D\"ltr\" style=3D\"font-size:12pt;color:#000000;background-color:#F=" + "\r\n" +
        "FFFFF;font-family:Calibri,Arial,Helvetica,sans-serif;\">" + "\r\n" +
        "<p><br>" + "\r\n" +
        "</p>" + "\r\n" +
        "</body>" + "\r\n" +
        "</html>" + "\r\n" +
        "" + "\r\n" +
        "--_000_7852067692f94262ac04e48a013322e2WINHEXBEEU109winmail_" + "\r\n" +
        "Content-Type: text/calendar; charset=\"utf-8\"; method={{METHOD}}" + "\r\n" +
        "Content-Transfer-Encoding: base64" + "\r\n" +
        "" + "\r\n" +
        "{{ITIP}}" + "\r\n" +
        "" + "\r\n" +
        "--_000_7852067692f94262ac04e48a013322e2WINHEXBEEU109winmail_--" + "\r\n"
    ; // @formatter:on

    protected static String generateImip(Map<String, String> replacements) {
        String template = IMIP_TEMPLATE;
        for (Entry<String, String> entry : replacements.entrySet()) {
            template = template.replaceAll(Pattern.quote(entry.getKey()), entry.getValue());
        }
        return template;
    }

    protected static String generateImip(UserData from, UserData to, String messageId, String subject, Date date, String method, String iTip) {
        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put("{{FROM}}", from.getEmail1());
        replacements.put("{{TO}}", to.getEmail1());
        replacements.put("{{MESSAGE_ID}}", messageId);
        replacements.put("{{SUBJECT}}", subject);
        replacements.put("{{DATE}}", new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US).format(date));
        replacements.put("{{METHOD}}", method);
        replacements.put("{{ITIP}}", BaseEncoding.base64().withSeparator("\r\n", 77).encode(iTip.getBytes(Charsets.UTF_8)));
        return generateImip(replacements);
    }

    @Test
    public void testOrganizedFromExchange() throws Exception {
        /*
         * prepare iTIP REQUEST with orphaned instance
         */
        UserData userB = userResponseC2.getData();
        UserData userA = userResponseC1.getData();
        String uid = randomUID();
        String summary = randomUID();
        Date start = com.openexchange.time.TimeTools.D("next monday afternoon", TimeZone.getTimeZone("Europe/Berlin"));
        Date end = CalendarUtils.add(start, Calendar.HOUR, 1);
        int sequence = 3;
        String iTip = // @formatter:off 
            "BEGIN:VCALENDAR" + "\r\n" +
            "METHOD:REQUEST" + "\r\n" +
            "PRODID:Microsoft Exchange Server 2010" + "\r\n" +
            "VERSION:2.0" + "\r\n" +
            "BEGIN:VTIMEZONE" + "\r\n" +
            "TZID:W. Europe Standard Time" + "\r\n" +
            "BEGIN:STANDARD" + "\r\n" +
            "DTSTART:16010101T030000" + "\r\n" +
            "TZOFFSETFROM:+0200" + "\r\n" +
            "TZOFFSETTO:+0100" + "\r\n" +
            "RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=-1SU;BYMONTH=10" + "\r\n" +
            "END:STANDARD" + "\r\n" +
            "BEGIN:DAYLIGHT" + "\r\n" +
            "DTSTART:16010101T020000" + "\r\n" +
            "TZOFFSETFROM:+0100" + "\r\n" +
            "TZOFFSETTO:+0200" + "\r\n" +
            "RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=-1SU;BYMONTH=3" + "\r\n" +
            "END:DAYLIGHT" + "\r\n" +
            "END:VTIMEZONE" + "\r\n" +
            "BEGIN:VEVENT" + "\r\n" +
            "ORGANIZER;CN=" + userB.getDisplayName() + ":MAILTO:" + userB.getEmail1() + "\r\n" +
            "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=TRUE;CN=" + userA.getEmail1() + "\r\n" +
            " :MAILTO:" + userA.getEmail1() + "\r\n" +
            "DESCRIPTION;LANGUAGE=de-DE:\n" + "\r\n" +
            "SUMMARY;LANGUAGE=de-DE:" + summary + "\r\n" +
            "DTSTART;TZID=W. Europe Standard Time:" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=W. Europe Standard Time:"+ format(end, "Europe/Berlin")  + "\r\n" +
            "UID:" + uid + "\r\n" +
            "RECURRENCE-ID;TZID=W. Europe Standard Time:"+ format(start, "Europe/Berlin") + "\r\n" +
            "CLASS:PUBLIC" + "\r\n" +
            "PRIORITY:5" + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "TRANSP:OPAQUE" + "\r\n" +
            "STATUS:CONFIRMED" + "\r\n" +
            "SEQUENCE:" + sequence  + "\r\n" +
            "LOCATION;LANGUAGE=de-DE:" + "\r\n" +
            "X-MICROSOFT-CDO-APPT-SEQUENCE:" + sequence + "\r\n" +
            "X-MICROSOFT-CDO-OWNERAPPTID:2119509619" + "\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:TENTATIVE" + "\r\n" +
            "X-MICROSOFT-CDO-INTENDEDSTATUS:BUSY" + "\r\n" +
            "X-MICROSOFT-CDO-ALLDAYEVENT:FALSE" + "\r\n" +
            "X-MICROSOFT-CDO-IMPORTANCE:1" + "\r\n" +
            "X-MICROSOFT-CDO-INSTTYPE:3" + "\r\n" +
            "X-MICROSOFT-DISALLOW-COUNTER:FALSE" + "\r\n" +
            "BEGIN:VALARM" + "\r\n" +
            "DESCRIPTION:REMINDER" + "\r\n" +
            "TRIGGER;RELATED=START:-PT15M" + "\r\n" +
            "ACTION:DISPLAY" + "\r\n" +
            "END:VALARM" + "\r\n" +
            "END:VEVENT" + "\r\n" +
            "END:VCALENDAR" + "\r\n"
        ; // @formatter:on
        /*
         * wrap in iMIP message & send it from user b to user a
         */
        sendImip(apiClientC2, generateImip(userB, userA, randomUID(), summary, new Date(), "REQUEST", iTip));
        /*
         * receive & analyze iMIP request as user a
         */
        MailData iMipRequestData = receiveIMip(apiClient, userB.getEmail1(), summary, sequence, uid, SchedulingMethod.REQUEST);
        AnalysisChangeNewEvent newEvent = assertSingleChange(analyze(apiClient, iMipRequestData)).getNewEvent();
        assertNotNull(newEvent);
        assertEquals(uid, newEvent.getUid());
        assertAttendeePartStat(newEvent.getAttendees(), userA.getEmail1(), "NEEDS-ACTION");
        /*
         * reply with "accepted"
         */
        EventData eventData = assertSingleEvent(accept(constructBody(iMipRequestData), null));
        assertEquals(uid, eventData.getUid());
        assertAttendeePartStat(eventData.getAttendees(), userA.getEmail1(), "ACCEPTED");
        /*
         * check event in calendar
         */
        EventResponse eventResponse = chronosApi.getEvent(eventData.getId(), eventData.getFolder(), eventData.getRecurrenceId(), null, null);
        assertNull(eventResponse.getError(), eventResponse.getError());
        eventData = eventResponse.getData();
        assertEquals(uid, eventData.getUid());
        assertAttendeePartStat(eventData.getAttendees(), userA.getEmail1(), "ACCEPTED");
        /*
         * receive & analyze iMIP reply as user b, too
         */
        MailData iMipReplyData = receiveIMip(apiClientC2, userA.getEmail1(), summary, sequence, uid, SchedulingMethod.REPLY);
        assertNotNull(iMipReplyData);
        ImportedCalendar iTipReply = parseICalAttachment(apiClientC2, iMipReplyData);
        assertEquals("REPLY", iTipReply.getMethod());
        assertTrue(null != iTipReply.getEvents() && 1 == iTipReply.getEvents().size());
        Event replyEvent = iTipReply.getEvents().get(0);
        assertAttendeePartStat(replyEvent.getAttendees(), userA.getEmail1(), ParticipationStatus.ACCEPTED);
        /*
         * prepare iTIP REQUEST with update of orphaned instance
         */
        Date start2 = CalendarUtils.add(start, Calendar.MINUTE, 30);
        Date end2 = CalendarUtils.add(end, Calendar.HOUR, 30);
        int sequence2 = sequence + 1;
        iTip = // @formatter:off 
            "BEGIN:VCALENDAR" + "\r\n" +
            "METHOD:REQUEST" + "\r\n" +
            "PRODID:Microsoft Exchange Server 2010" + "\r\n" +
            "VERSION:2.0" + "\r\n" +
            "BEGIN:VTIMEZONE" + "\r\n" +
            "TZID:W. Europe Standard Time" + "\r\n" +
            "BEGIN:STANDARD" + "\r\n" +
            "DTSTART:16010101T030000" + "\r\n" +
            "TZOFFSETFROM:+0200" + "\r\n" +
            "TZOFFSETTO:+0100" + "\r\n" +
            "RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=-1SU;BYMONTH=10" + "\r\n" +
            "END:STANDARD" + "\r\n" +
            "BEGIN:DAYLIGHT" + "\r\n" +
            "DTSTART:16010101T020000" + "\r\n" +
            "TZOFFSETFROM:+0100" + "\r\n" +
            "TZOFFSETTO:+0200" + "\r\n" +
            "RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=-1SU;BYMONTH=3" + "\r\n" +
            "END:DAYLIGHT" + "\r\n" +
            "END:VTIMEZONE" + "\r\n" +
            "BEGIN:VEVENT" + "\r\n" +
            "ORGANIZER;CN=" + userB.getDisplayName() + ":MAILTO:" + userB.getEmail1() + "\r\n" +
            "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=TRUE;CN=" + userA.getEmail1() + "\r\n" +
            " :MAILTO:" + userA.getEmail1() + "\r\n" +
            "DESCRIPTION;LANGUAGE=de-DE:\n" + "\r\n" +
            "SUMMARY;LANGUAGE=de-DE:" + summary + "\r\n" +
            "DTSTART;TZID=W. Europe Standard Time:" + format(start2, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=W. Europe Standard Time:"+ format(end2, "Europe/Berlin")  + "\r\n" +
            "UID:" + uid + "\r\n" +
            "RECURRENCE-ID;TZID=W. Europe Standard Time:"+ format(start, "Europe/Berlin") + "\r\n" +
            "CLASS:PUBLIC" + "\r\n" +
            "PRIORITY:5" + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "TRANSP:OPAQUE" + "\r\n" +
            "STATUS:CONFIRMED" + "\r\n" +
            "SEQUENCE:" + sequence2 + "\r\n" +
            "LOCATION;LANGUAGE=de-DE:" + "\r\n" +
            "X-MICROSOFT-CDO-APPT-SEQUENCE:" + sequence2 + "\r\n" +
            "X-MICROSOFT-CDO-OWNERAPPTID:2119509619" + "\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:TENTATIVE" + "\r\n" +
            "X-MICROSOFT-CDO-INTENDEDSTATUS:BUSY" + "\r\n" +
            "X-MICROSOFT-CDO-ALLDAYEVENT:FALSE" + "\r\n" +
            "X-MICROSOFT-CDO-IMPORTANCE:1" + "\r\n" +
            "X-MICROSOFT-CDO-INSTTYPE:3" + "\r\n" +
            "X-MICROSOFT-DISALLOW-COUNTER:FALSE" + "\r\n" +
            "BEGIN:VALARM" + "\r\n" +
            "DESCRIPTION:REMINDER" + "\r\n" +
            "TRIGGER;RELATED=START:-PT15M" + "\r\n" +
            "ACTION:DISPLAY" + "\r\n" +
            "END:VALARM" + "\r\n" +
            "END:VEVENT" + "\r\n" +
            "END:VCALENDAR" + "\r\n"
        ; // @formatter:on
        /*
         * wrap in iMIP message & send it from user b to user a
         */
        sendImip(apiClientC2, generateImip(userB, userA, randomUID(), summary, new Date(), "REQUEST", iTip));
        /*
         * receive & analyze iMIP request as user a
         */
        iMipRequestData = receiveIMip(apiClient, userB.getEmail1(), summary, sequence2, uid, SchedulingMethod.REQUEST);
        newEvent = assertSingleChange(analyze(apiClient, iMipRequestData)).getNewEvent();
        assertNotNull(newEvent);
        assertEquals(uid, newEvent.getUid());
        assertAttendeePartStat(newEvent.getAttendees(), userA.getEmail1(), "NEEDS-ACTION");
        /*
         * reply with "tentative"
         */
        eventData = assertSingleEvent(tentative(constructBody(iMipRequestData), null));
        assertEquals(uid, eventData.getUid());
        assertAttendeePartStat(eventData.getAttendees(), userA.getEmail1(), "TENTATIVE");
        /*
         * check event in calendar
         */
        eventResponse = chronosApi.getEvent(eventData.getId(), eventData.getFolder(), eventData.getRecurrenceId(), null, null);
        assertNull(eventResponse.getError(), eventResponse.getError());
        eventData = eventResponse.getData();
        assertEquals(uid, eventData.getUid());
        assertAttendeePartStat(eventData.getAttendees(), userA.getEmail1(), "TENTATIVE");
        /*
         * receive & analyze iMIP reply as user b, too
         */
        iMipReplyData = receiveIMip(apiClientC2, userA.getEmail1(), summary, sequence2, uid, SchedulingMethod.REPLY);
        assertNotNull(iMipReplyData);
        iTipReply = parseICalAttachment(apiClientC2, iMipReplyData);
        assertEquals("REPLY", iTipReply.getMethod());
        assertTrue(null != iTipReply.getEvents() && 1 == iTipReply.getEvents().size());
        replyEvent = iTipReply.getEvents().get(0);
        assertAttendeePartStat(replyEvent.getAttendees(), userA.getEmail1(), ParticipationStatus.TENTATIVE);
        /*
         * prepare iTIP CANCEL for the orphaned instance
         */
        int sequence3 = sequence2 + 1;
        iTip = // @formatter:off 
            "BEGIN:VCALENDAR" + "\r\n" +
            "METHOD:CANCEL" + "\r\n" +
            "PRODID:Microsoft Exchange Server 2010" + "\r\n" +
            "VERSION:2.0" + "\r\n" +
            "BEGIN:VTIMEZONE" + "\r\n" +
            "TZID:W. Europe Standard Time" + "\r\n" +
            "BEGIN:STANDARD" + "\r\n" +
            "DTSTART:16010101T030000" + "\r\n" +
            "TZOFFSETFROM:+0200" + "\r\n" +
            "TZOFFSETTO:+0100" + "\r\n" +
            "RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=-1SU;BYMONTH=10" + "\r\n" +
            "END:STANDARD" + "\r\n" +
            "BEGIN:DAYLIGHT" + "\r\n" +
            "DTSTART:16010101T020000" + "\r\n" +
            "TZOFFSETFROM:+0100" + "\r\n" +
            "TZOFFSETTO:+0200" + "\r\n" +
            "RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=-1SU;BYMONTH=3" + "\r\n" +
            "END:DAYLIGHT" + "\r\n" +
            "END:VTIMEZONE" + "\r\n" +
            "BEGIN:VEVENT" + "\r\n" +
            "ORGANIZER;CN=" + userB.getDisplayName() + ":MAILTO:" + userB.getEmail1() + "\r\n" +
            "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=TRUE;CN=" + userA.getEmail1() + "\r\n" +
            " :MAILTO:" + userA.getEmail1() + "\r\n" +
            "DESCRIPTION;LANGUAGE=de-DE:\n" + "\r\n" +
            "SUMMARY;LANGUAGE=de-DE:Abgesagt: " + summary + "\r\n" +
            "DTSTART;TZID=W. Europe Standard Time:" + format(start2, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=W. Europe Standard Time:"+ format(end2, "Europe/Berlin")  + "\r\n" +
            "UID:" + uid + "\r\n" +
            "RECURRENCE-ID;TZID=W. Europe Standard Time:"+ format(start, "Europe/Berlin") + "\r\n" +
            "CLASS:PUBLIC" + "\r\n" +
            "PRIORITY:5" + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "TRANSP:OPAQUE" + "\r\n" +
            "STATUS:CANCELLED" + "\r\n" +
            "SEQUENCE:" + sequence3 + "\r\n" +
            "LOCATION;LANGUAGE=de-DE:" + "\r\n" +
            "X-MICROSOFT-CDO-APPT-SEQUENCE:" + sequence3 + "\r\n" +
            "X-MICROSOFT-CDO-OWNERAPPTID:2119509619" + "\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:FREE" + "\r\n" +
            "X-MICROSOFT-CDO-INTENDEDSTATUS:FREE" + "\r\n" +
            "X-MICROSOFT-CDO-ALLDAYEVENT:FALSE" + "\r\n" +
            "X-MICROSOFT-CDO-IMPORTANCE:1" + "\r\n" +
            "X-MICROSOFT-CDO-INSTTYPE:3" + "\r\n" +
            "X-MICROSOFT-DISALLOW-COUNTER:FALSE" + "\r\n" +
            "END:VEVENT" + "\r\n" +
            "END:VCALENDAR" + "\r\n"
        ; // @formatter:on
        /*
         * wrap in iMIP message & send it from user b to user a
         */
        sendImip(apiClientC2, generateImip(userB, userA, randomUID(), summary, new Date(), "CANCEL", iTip));
        /*
         * receive & analyze iMIP request as user a
         */
        MailData iMipCancelData = receiveIMip(apiClient, userB.getEmail1(), summary, sequence3, uid, SchedulingMethod.CANCEL);
        AnalysisChangeDeletedEvent deletedEvent = assertSingleChange(analyze(apiClient, iMipCancelData)).getDeletedEvent();
        assertNotNull(deletedEvent);
        assertEquals(uid, deletedEvent.getUid());
        /*
         * take over cancellation
         */
        cancel(constructBody(iMipCancelData), null, false);
        /*
         * check that event was removed from calendar
         */
        eventResponse = chronosApi.getEvent(eventData.getId(), eventData.getFolder(), eventData.getRecurrenceId(), null, null);
        assertNotNull(eventResponse.getError());
        assertEquals("CAL-4040", eventResponse.getCode());
    }

    @Test
    public void testOrganizedFromGoogle() throws Exception {
        /*
         * prepare iTIP REQUEST with orphaned instance
         */
        UserData userB = userResponseC2.getData();
        UserData userA = userResponseC1.getData();
        String uid = randomUID();
        String summary = randomUID();
        Date start = com.openexchange.time.TimeTools.D("next monday afternoon", TimeZone.getTimeZone("Europe/Berlin"));
        Date end = CalendarUtils.add(start, Calendar.HOUR, 1);
        int sequence = 0;
        String iTip = // @formatter:off 
            "BEGIN:VCALENDAR" + "\r\n" +
            "PRODID:-//Google Inc//Google Calendar 70.9054//EN" + "\r\n" +
            "VERSION:2.0" + "\r\n" +
            "CALSCALE:GREGORIAN" + "\r\n" +
            "METHOD:REQUEST" + "\r\n" +
            "BEGIN:VTIMEZONE" + "\r\n" +
            "TZID:Europe/Berlin" + "\r\n" +
            "X-LIC-LOCATION:Europe/Berlin" + "\r\n" +
            "BEGIN:DAYLIGHT" + "\r\n" +
            "TZOFFSETFROM:+0100" + "\r\n" +
            "TZOFFSETTO:+0200" + "\r\n" +
            "TZNAME:CEST" + "\r\n" +
            "DTSTART:19700329T020000" + "\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU" + "\r\n" +
            "END:DAYLIGHT" + "\r\n" +
            "BEGIN:STANDARD" + "\r\n" +
            "TZOFFSETFROM:+0200" + "\r\n" +
            "TZOFFSETTO:+0100" + "\r\n" +
            "TZNAME:CET" + "\r\n" +
            "DTSTART:19701025T030000" + "\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU" + "\r\n" +
            "END:STANDARD" + "\r\n" +
            "END:VTIMEZONE" + "\r\n" +
            "BEGIN:VEVENT" + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "ORGANIZER;CN=" + userB.getEmail1() + ":mailto:" + userB.getEmail1() + "\r\n" +
            "UID:" + uid + "\r\n" +
            "ATTENDEE;CUTYPE=INDIVIDUAL;ROLE=REQ-PARTICIPANT;PARTSTAT=ACCEPTED;RSVP=TRUE" + "\r\n" +
            " ;CN=" + userB.getEmail1() + ";X-NUM-GUESTS=0:mailto:" + userB.getEmail1() + "\r\n" +
            "ATTENDEE;CUTYPE=INDIVIDUAL;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=" + "\r\n" +
            " TRUE;CN=" + userA.getEmail1() + ";X-NUM-GUESTS=0:mailto:" + userA.getEmail1() + "\r\n" +
            "X-MICROSOFT-CDO-OWNERAPPTID:-2072440628" + "\r\n" +
            "RECURRENCE-ID;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DESCRIPTION:-::~:~::~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~" + "\r\n" +
            " :~:~:~:~:~:~:~:~::~:~::-\nDiesen Abschnitt der Beschreibung nicht bearbeite" + "\r\n" +
            " n.\n\nZeigen Sie Ihren Termin unter https://calendar.google.com/calendar/ev" + "\r\n" +
            " ent?action=VIEW&eid=123452343243432432453454354353454&ctz=Europe%2FBerlin&h" + "\r\n" +
            " l=de&es=1 an.\n-::~:~::~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:" + "\r\n" +
            " ~:~:~:~:~:~:~:~:~:~::~:~::-" + "\r\n" +
            "LAST-MODIFIED:"  + formatAsUTC(new Date()) + "\r\n" +
            "LOCATION:" + "\r\n" +
            "SEQUENCE:" + sequence  + "\r\n" +
            "STATUS:CONFIRMED" + "\r\n" +
            "SUMMARY:" + summary + "\r\n" +
            "TRANSP:OPAQUE" + "\r\n" +
            "END:VEVENT" + "\r\n" +
            "END:VCALENDAR" + "\r\n"
        ; // @formatter:on
        /*
         * wrap in iMIP message & send it from user b to user a
         */
        sendImip(apiClientC2, generateImip(userB, userA, randomUID(), summary, new Date(), "REQUEST", iTip));
        /*
         * receive & analyze iMIP request as user a
         */
        MailData iMipRequestData = receiveIMip(apiClient, userB.getEmail1(), summary, sequence, uid, SchedulingMethod.REQUEST);
        AnalysisChangeNewEvent newEvent = assertSingleChange(analyze(apiClient, iMipRequestData)).getNewEvent();
        assertNotNull(newEvent);
        assertEquals(uid, newEvent.getUid());
        assertAttendeePartStat(newEvent.getAttendees(), userA.getEmail1(), "NEEDS-ACTION");
        /*
         * reply with "accepted"
         */
        EventData eventData = assertSingleEvent(accept(constructBody(iMipRequestData), null));
        assertEquals(uid, eventData.getUid());
        assertAttendeePartStat(eventData.getAttendees(), userA.getEmail1(), "ACCEPTED");
        /*
         * check event in calendar
         */
        EventResponse eventResponse = chronosApi.getEvent(eventData.getId(), eventData.getFolder(), eventData.getRecurrenceId(), null, null);
        assertNull(eventResponse.getError(), eventResponse.getError());
        eventData = eventResponse.getData();
        assertEquals(uid, eventData.getUid());
        assertAttendeePartStat(eventData.getAttendees(), userA.getEmail1(), "ACCEPTED");
        /*
         * receive & analyze iMIP reply as user b, too
         */
        MailData iMipReplyData = receiveIMip(apiClientC2, userA.getEmail1(), summary, sequence, uid, SchedulingMethod.REPLY);
        assertNotNull(iMipReplyData);
        ImportedCalendar iTipReply = parseICalAttachment(apiClientC2, iMipReplyData);
        assertEquals("REPLY", iTipReply.getMethod());
        assertTrue(null != iTipReply.getEvents() && 1 == iTipReply.getEvents().size());
        Event replyEvent = iTipReply.getEvents().get(0);
        assertAttendeePartStat(replyEvent.getAttendees(), userA.getEmail1(), ParticipationStatus.ACCEPTED);
        /*
         * prepare iTIP REQUEST with update of orphaned instance
         */
        Date start2 = CalendarUtils.add(start, Calendar.MINUTE, 30);
        Date end2 = CalendarUtils.add(end, Calendar.HOUR, 30);
        int sequence2 = sequence + 1;
        iTip = // @formatter:off 
            "BEGIN:VCALENDAR" + "\r\n" +
            "PRODID:-//Google Inc//Google Calendar 70.9054//EN" + "\r\n" +
            "VERSION:2.0" + "\r\n" +
            "CALSCALE:GREGORIAN" + "\r\n" +
            "METHOD:REQUEST" + "\r\n" +
            "BEGIN:VTIMEZONE" + "\r\n" +
            "TZID:Europe/Berlin" + "\r\n" +
            "X-LIC-LOCATION:Europe/Berlin" + "\r\n" +
            "BEGIN:DAYLIGHT" + "\r\n" +
            "TZOFFSETFROM:+0100" + "\r\n" +
            "TZOFFSETTO:+0200" + "\r\n" +
            "TZNAME:CEST" + "\r\n" +
            "DTSTART:19700329T020000" + "\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU" + "\r\n" +
            "END:DAYLIGHT" + "\r\n" +
            "BEGIN:STANDARD" + "\r\n" +
            "TZOFFSETFROM:+0200" + "\r\n" +
            "TZOFFSETTO:+0100" + "\r\n" +
            "TZNAME:CET" + "\r\n" +
            "DTSTART:19701025T030000" + "\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU" + "\r\n" +
            "END:STANDARD" + "\r\n" +
            "END:VTIMEZONE" + "\r\n" +
            "BEGIN:VEVENT" + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start2, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end2, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "ORGANIZER;CN=" + userB.getEmail1() + ":mailto:" + userB.getEmail1() + "\r\n" +
            "UID:" + uid + "\r\n" +
            "ATTENDEE;CUTYPE=INDIVIDUAL;ROLE=REQ-PARTICIPANT;PARTSTAT=ACCEPTED;RSVP=TRUE" + "\r\n" +
            " ;CN=" + userB.getEmail1() + ";X-NUM-GUESTS=0:mailto:" + userB.getEmail1() + "\r\n" +
            "ATTENDEE;CUTYPE=INDIVIDUAL;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=" + "\r\n" +
            " TRUE;CN=" + userA.getEmail1() + ";X-NUM-GUESTS=0:mailto:" + userA.getEmail1() + "\r\n" +
            "X-MICROSOFT-CDO-OWNERAPPTID:-2072440628" + "\r\n" +
            "RECURRENCE-ID;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DESCRIPTION:-::~:~::~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~" + "\r\n" +
            " :~:~:~:~:~:~:~:~::~:~::-\nDiesen Abschnitt der Beschreibung nicht bearbeite" + "\r\n" +
            " n.\n\nZeigen Sie Ihren Termin unter https://calendar.google.com/calendar/ev" + "\r\n" +
            " ent?action=VIEW&eid=123452343243432432453454354353454&ctz=Europe%2FBerlin&h" + "\r\n" +
            " l=de&es=1 an.\n-::~:~::~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:" + "\r\n" +
            " ~:~:~:~:~:~:~:~:~:~::~:~::-" + "\r\n" +
            "LAST-MODIFIED:"  + formatAsUTC(new Date()) + "\r\n" +
            "LOCATION:" + "\r\n" +
            "SEQUENCE:" + sequence2 + "\r\n" +
            "STATUS:CONFIRMED" + "\r\n" +
            "SUMMARY:" + summary + "\r\n" +
            "TRANSP:OPAQUE" + "\r\n" +
            "END:VEVENT" + "\r\n" +
            "END:VCALENDAR" + "\r\n"
        ; // @formatter:on
        /*
         * wrap in iMIP message & send it from user b to user a
         */
        sendImip(apiClientC2, generateImip(userB, userA, randomUID(), summary, new Date(), "REQUEST", iTip));
        /*
         * receive & analyze iMIP request as user a
         */
        iMipRequestData = receiveIMip(apiClient, userB.getEmail1(), summary, sequence2, uid, SchedulingMethod.REQUEST);
        newEvent = assertSingleChange(analyze(apiClient, iMipRequestData)).getNewEvent();
        assertNotNull(newEvent);
        assertEquals(uid, newEvent.getUid());
        assertAttendeePartStat(newEvent.getAttendees(), userA.getEmail1(), "NEEDS-ACTION");
        /*
         * reply with "tentative"
         */
        eventData = assertSingleEvent(tentative(constructBody(iMipRequestData), null));
        assertEquals(uid, eventData.getUid());
        assertAttendeePartStat(eventData.getAttendees(), userA.getEmail1(), "TENTATIVE");
        /*
         * check event in calendar
         */
        eventResponse = chronosApi.getEvent(eventData.getId(), eventData.getFolder(), eventData.getRecurrenceId(), null, null);
        assertNull(eventResponse.getError(), eventResponse.getError());
        eventData = eventResponse.getData();
        assertEquals(uid, eventData.getUid());
        assertAttendeePartStat(eventData.getAttendees(), userA.getEmail1(), "TENTATIVE");
        /*
         * receive & analyze iMIP reply as user b, too
         */
        iMipReplyData = receiveIMip(apiClientC2, userA.getEmail1(), summary, sequence2, uid, SchedulingMethod.REPLY);
        assertNotNull(iMipReplyData);
        iTipReply = parseICalAttachment(apiClientC2, iMipReplyData);
        assertEquals("REPLY", iTipReply.getMethod());
        assertTrue(null != iTipReply.getEvents() && 1 == iTipReply.getEvents().size());
        replyEvent = iTipReply.getEvents().get(0);
        assertAttendeePartStat(replyEvent.getAttendees(), userA.getEmail1(), ParticipationStatus.TENTATIVE);
        /*
         * prepare iTIP CANCEL for the orphaned instance
         */
        int sequence3 = sequence2 + 1;
        iTip = // @formatter:off 
            "BEGIN:VCALENDAR" + "\r\n" +
            "PRODID:-//Google Inc//Google Calendar 70.9054//EN" + "\r\n" +
            "VERSION:2.0" + "\r\n" +
            "CALSCALE:GREGORIAN" + "\r\n" +
            "METHOD:CANCEL" + "\r\n" +
            "BEGIN:VTIMEZONE" + "\r\n" +
            "TZID:Europe/Berlin" + "\r\n" +
            "X-LIC-LOCATION:Europe/Berlin" + "\r\n" +
            "BEGIN:DAYLIGHT" + "\r\n" +
            "TZOFFSETFROM:+0100" + "\r\n" +
            "TZOFFSETTO:+0200" + "\r\n" +
            "TZNAME:CEST" + "\r\n" +
            "DTSTART:19700329T020000" + "\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU" + "\r\n" +
            "END:DAYLIGHT" + "\r\n" +
            "BEGIN:STANDARD" + "\r\n" +
            "TZOFFSETFROM:+0200" + "\r\n" +
            "TZOFFSETTO:+0100" + "\r\n" +
            "TZNAME:CET" + "\r\n" +
            "DTSTART:19701025T030000" + "\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU" + "\r\n" +
            "END:STANDARD" + "\r\n" +
            "END:VTIMEZONE" + "\r\n" +
            "BEGIN:VEVENT" + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start2, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end2, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "ORGANIZER;CN=" + userB.getEmail1() + ":mailto:" + userB.getEmail1() + "\r\n" +
            "UID:" + uid + "\r\n" +
            "ATTENDEE;CUTYPE=INDIVIDUAL;ROLE=REQ-PARTICIPANT;PARTSTAT=ACCEPTED;RSVP=TRUE" + "\r\n" +
            " ;CN=" + userB.getEmail1() + ";X-NUM-GUESTS=0:mailto:" + userB.getEmail1() + "\r\n" +
            "ATTENDEE;CUTYPE=INDIVIDUAL;ROLE=REQ-PARTICIPANT;PARTSTAT=TENTATIVE;RSVP=" + "\r\n" +
            " TRUE;CN=" + userA.getEmail1() + ";X-NUM-GUESTS=0:mailto:" + userA.getEmail1() + "\r\n" +
            "RECURRENCE-ID;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DESCRIPTION:" + "\r\n" +
            "LAST-MODIFIED:"  + formatAsUTC(new Date()) + "\r\n" +
            "LOCATION:" + "\r\n" +
            "SEQUENCE:" + sequence3 + "\r\n" +
            "STATUS:CANCELLED" + "\r\n" +
            "SUMMARY:" + summary + "\r\n" +
            "TRANSP:OPAQUE" + "\r\n" +
            "END:VEVENT" + "\r\n" +
            "END:VCALENDAR" + "\r\n"
        ; // @formatter:on
        /*
         * wrap in iMIP message & send it from user b to user a
         */
        sendImip(apiClientC2, generateImip(userB, userA, randomUID(), summary, new Date(), "CANCEL", iTip));
        /*
         * receive & analyze iMIP request as user a
         */
        MailData iMipCancelData = receiveIMip(apiClient, userB.getEmail1(), summary, sequence3, uid, SchedulingMethod.CANCEL);
        AnalysisChangeDeletedEvent deletedEvent = assertSingleChange(analyze(apiClient, iMipCancelData)).getDeletedEvent();
        assertNotNull(deletedEvent);
        assertEquals(uid, deletedEvent.getUid());
        /*
         * take over cancellation
         */
        cancel(constructBody(iMipCancelData), null, false);
        /*
         * check that event was removed from calendar
         */
        eventResponse = chronosApi.getEvent(eventData.getId(), eventData.getFolder(), eventData.getRecurrenceId(), null, null);
        assertNotNull(eventResponse.getError());
        assertEquals("CAL-4040", eventResponse.getCode());
    }

    protected static MailDestinationResponse sendImip(com.openexchange.testing.httpclient.invoker.ApiClient client, String iMip) throws Exception {
        File tmpFile = null;
        MailDestinationResponse response;
        try {
            tmpFile = File.createTempFile("test", ".tmp");
            try (FileWriter writer = new FileWriter(tmpFile)) {
                writer.write(iMip);
            }
            response = new MailApi(client).sendOrSaveMail(tmpFile, null, null);
        } finally {
            if (null != tmpFile) {
                tmpFile.delete();
            }
        }
        assertNull(response.getError(), response.getError());
        return response;
    }

    protected static String randomUID() {
        return UUID.randomUUID().toString();
    }

    protected static String format(Date date, TimeZone timeZone) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmm'00'");
        dateFormat.setTimeZone(timeZone);
        return dateFormat.format(date);
    }

    protected static String format(Date date, String timeZoneID) {
        return format(date, TimeZone.getTimeZone(timeZoneID));
    }

    protected static String formatAsUTC(final Date date) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }

    protected static String formatAsDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }

}
