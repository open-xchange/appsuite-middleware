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

package com.openexchange.ajax.chronos.itip.bugs;

import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertAttendeePartStat;
import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertSingleChange;
import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertSingleEvent;
import static com.openexchange.ajax.chronos.itip.ITipUtil.constructBody;
import static com.openexchange.ajax.chronos.itip.ITipUtil.parseICalAttachment;
import static com.openexchange.ajax.chronos.itip.ITipUtil.receiveIMip;
import static com.openexchange.java.Autoboxing.L;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Pattern;
import org.junit.Test;
import com.openexchange.ajax.chronos.itip.AbstractITipTest;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.ical.ImportedCalendar;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.test.common.configuration.AJAXConfig;
import com.openexchange.test.common.groupware.calendar.TimeTools;
import com.openexchange.testing.httpclient.models.AnalysisChangeNewEvent;
import com.openexchange.testing.httpclient.models.ChronosAttachment;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventResponse;
import com.openexchange.testing.httpclient.models.MailData;
import com.openexchange.testing.httpclient.models.MailDestinationResponse;
import com.openexchange.testing.httpclient.modules.MailApi;

/**
 * {@link Bug65533Test}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.3
 */
public class Bug65533Test extends AbstractITipTest {

    private File tmpFile = null;
    private FileWriter writer = null;

    @Override
    public void tearDown() throws Exception {
        try {
            if (null != writer) {
                writer.close();
            }
            if (null != tmpFile) {
                tmpFile.delete();
            }
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testImportIMipAttachment() throws Exception {
        /*
         * prepare variable parts of invitation
         */
        String now = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US).format(new Date());
        String organizerMail = userResponseC2.getData().getEmail1();
        String organizerCn = userResponseC2.getData().getDisplayName();
        String recipientMail = userResponseC1.getData().getEmail1();
        String recipientCn = userResponseC1.getData().getDisplayName();
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");
        Date startDate = TimeTools.D("next friday at noon", timeZone);
        Date endDate = CalendarUtils.add(startDate, Calendar.HOUR, 1);
        String dtStart = format(startDate, timeZone);
        String dtEnd = format(endDate, timeZone);
        String uid = randomUID();
        String summary = randomUID();
        /*
         * prepare & send message from user b to user a
         */
        String iMip;
        File file = new File(AJAXConfig.getProperty(AJAXConfig.Property.TEST_MAIL_DIR), "bug65533.eml");
        try (InputStream inputStream = new FileInputStream(file)) {
            iMip = Streams.stream2string(inputStream, Charsets.UTF_8_NAME);
        }
        iMip = iMip.replaceAll(Pattern.quote("{{NOW}}"), now);
        iMip = iMip.replaceAll(Pattern.quote("{{DTSTART}}"), dtStart);
        iMip = iMip.replaceAll(Pattern.quote("{{DTEND}}"), dtEnd);
        iMip = iMip.replaceAll(Pattern.quote("{{FROM_MAIL}}"), organizerMail);
        iMip = iMip.replaceAll(Pattern.quote("{{FROM_CN}}"), quoteCN(organizerCn));
        iMip = iMip.replaceAll(Pattern.quote("{{TO_MAIL}}"), recipientMail);
        iMip = iMip.replaceAll(Pattern.quote("{{TO_CN}}"), quoteCN(recipientCn));
        iMip = iMip.replaceAll(Pattern.quote("{{UID}}"), uid);
        iMip = iMip.replaceAll(Pattern.quote("{{SUMMARY}}"), summary);
        tmpFile = File.createTempFile("test", ".tmp");
        writer = new FileWriter(tmpFile);
        writer.write(iMip);
        MailDestinationResponse response = new MailApi(apiClientC2).sendOrSaveMail(tmpFile, null, null);
        assertNull(response.getError(), response.getError());

        /*
         * receive & analyze iMIP request as user a
         */
        MailData iMipRequestData = receiveIMip(apiClient, organizerMail, summary, 0, SchedulingMethod.REQUEST);
        AnalysisChangeNewEvent newEvent = assertSingleChange(analyze(apiClient, iMipRequestData)).getNewEvent();
        assertNotNull(newEvent);
        assertEquals(uid, newEvent.getUid());
        assertAttendeePartStat(newEvent.getAttendees(), recipientMail, "NEEDS-ACTION");
        /*
         * reply with "accepted"
         */
        EventData eventData = assertSingleEvent(accept(constructBody(iMipRequestData.getId()), null));
        assertEquals(uid, eventData.getUid());
        assertAttendeePartStat(eventData.getAttendees(), recipientMail, "ACCEPTED");
        /*
         * check event in calendar
         */
        EventResponse eventResponse = chronosApi.getEvent(eventData.getId(), eventData.getFolder(), eventData.getRecurrenceId(), null, null);
        assertNull(eventResponse.getError(), eventResponse.getError());
        eventData = eventResponse.getData();
        assertEquals(uid, eventData.getUid());
        assertAttendeePartStat(eventData.getAttendees(), recipientMail, "ACCEPTED");
        /*
         * check if attachment was imported correctly
         */
        List<ChronosAttachment> attachments = eventData.getAttachments();
        assertTrue(null != attachments && 1 == attachments.size());
        ChronosAttachment attachment = attachments.get(0);
        assertEquals("homer.jpg", attachment.getFilename());
        assertEquals("image/jpeg", attachment.getFmtType());
        assertNotNull(attachment.getSize());
        assertThat("Invalid attachment size", attachment.getSize(), is(both(greaterThanOrEqualTo(L(175338l))).and(lessThanOrEqualTo(L(178880l)))));
        byte[] attachmentData = chronosApi.getEventAttachment(eventData.getId(), eventData.getFolder(), attachment.getManagedId());
        assertNotNull(attachmentData);
        /*
         * receive & analyze iMIP reply as user b, too
         */
        MailData iMipReplyData = receiveIMip(apiClientC2, recipientMail, summary, 0, SchedulingMethod.REPLY);
        assertNotNull(iMipReplyData);
        ImportedCalendar iTipReply = parseICalAttachment(apiClientC2, iMipReplyData);
        assertEquals("REPLY", iTipReply.getMethod());
        assertTrue(null != iTipReply.getEvents() && 1 == iTipReply.getEvents().size());
        Event replyEvent = iTipReply.getEvents().get(0);
        assertAttendeePartStat(replyEvent.getAttendees(), recipientMail, ParticipationStatus.ACCEPTED);
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

    private String quoteCN(String cn) {
        return "\"" + cn + "\"";
    }

}
