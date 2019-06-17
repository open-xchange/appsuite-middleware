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

package com.openexchange.ajax.chronos.itip.bugs;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.regex.Pattern;
import javax.ws.rs.core.GenericType;
import org.junit.Test;
import com.openexchange.ajax.chronos.itip.AbstractITipTest;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.ical.ImportedCalendar;
import com.openexchange.chronos.ical.ical4j.mapping.ICalMapper;
import com.openexchange.chronos.ical.impl.ICalUtils;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.invoker.Pair;
import com.openexchange.testing.httpclient.models.ActionResponse;
import com.openexchange.testing.httpclient.models.Analysis;
import com.openexchange.testing.httpclient.models.AnalysisChange;
import com.openexchange.testing.httpclient.models.AnalysisChangeNewEvent;
import com.openexchange.testing.httpclient.models.AnalyzeResponse;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.ChronosAttachment;
import com.openexchange.testing.httpclient.models.ConversionDataSource;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventId;
import com.openexchange.testing.httpclient.models.EventResponse;
import com.openexchange.testing.httpclient.models.MailAttachment;
import com.openexchange.testing.httpclient.models.MailData;
import com.openexchange.testing.httpclient.models.MailDestinationData;
import com.openexchange.testing.httpclient.models.MailDestinationResponse;
import com.openexchange.testing.httpclient.models.MailResponse;
import com.openexchange.testing.httpclient.models.MailsResponse;
import com.openexchange.testing.httpclient.modules.ChronosApi;
import com.openexchange.testing.httpclient.modules.MailApi;
import net.fortuna.ical4j.util.CompatibilityHints;

/**
 * {@link Bug65533Test}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.3
 */
public class Bug65533Test extends AbstractITipTest {

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
        File file = new File(AJAXConfig.getProperty(AJAXConfig.Property.TEST_DIR), "bug65533.eml");
        try (InputStream inputStream = new FileInputStream(file)) {
            iMip = Streams.stream2string(inputStream, Charsets.UTF_8_NAME);
        }
        iMip = iMip.replaceAll(Pattern.quote("{{NOW}}"), now);
        iMip = iMip.replaceAll(Pattern.quote("{{DTSTART}}"), dtStart);
        iMip = iMip.replaceAll(Pattern.quote("{{DTEND}}"), dtEnd);
        iMip = iMip.replaceAll(Pattern.quote("{{FROM_MAIL}}"), organizerMail);
        iMip = iMip.replaceAll(Pattern.quote("{{FROM_CN}}"), organizerCn);
        iMip = iMip.replaceAll(Pattern.quote("{{TO_MAIL}}"), recipientMail);
        iMip = iMip.replaceAll(Pattern.quote("{{TO_CN}}"), recipientCn);
        iMip = iMip.replaceAll(Pattern.quote("{{UID}}"), uid);
        iMip = iMip.replaceAll(Pattern.quote("{{SUMMARY}}"), summary);
        sendIMip(apiClientC2, iMip);
        /*
         * receive & analyze iMIP request as user a
         */
        MailData iMipRequestData = receiveIMip(apiClient, organizerMail, summary, 0, "REQUEST");
        AnalysisChangeNewEvent newEvent = assertSingleChange(analyze(apiClient, iMipRequestData)).getNewEvent();
        assertNotNull(newEvent);
        assertEquals(uid, newEvent.getUid());
        assertAttendeePartStat(newEvent.getAttendees(), recipientMail, "NEEDS-ACTION");
        /*
         * reply with "accepted"
         */
        EventData eventData = assertSingleEvent(accept(constructBody(iMipRequestData.getId())));
        assertEquals(uid, eventData.getUid());
        assertAttendeePartStat(eventData.getAttendees(), recipientMail, "ACCEPTED");
        /*
         * check event in calendar
         */
        EventResponse eventResponse = chronosApi.getEvent(
            apiClient.getSession(), eventData.getId(), eventData.getFolder(), eventData.getRecurrenceId(), null, null);
        assertNull(eventResponse.getError(), eventResponse.getError());
        eventData = eventResponse.getData();
        rememberForCleanup(eventData);
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
        assertEquals(L(177549), attachment.getSize());
        byte[] attachmentData = chronosApi.getEventAttachment(apiClient.getSession(), eventData.getId(), eventData.getFolder(), attachment.getManagedId());
        assertNotNull(attachmentData);
        /*
         * receive & analyze iMIP reply as user b, too
         */
        MailData iMipReplyData = receiveIMip(apiClientC2, recipientMail, summary, 0, "REPLY");
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

    protected static AnalysisChange assertSingleChange(AnalyzeResponse analyzeResponse) {
        assertNull("error during analysis: " + analyzeResponse.getError(), analyzeResponse.getCode());
        assertEquals("unexpected analysis number in response", 1, analyzeResponse.getData().size());
        Analysis analysis = analyzeResponse.getData().get(0);
        assertEquals("unexpected number of changes in analysis", 1, analysis.getChanges().size());
        return analysis.getChanges().get(0);
    }

    private void rememberForCleanup(EventData eventData) {
        if (null != eventData) {
            EventId eventId = new EventId();
            eventId.setId(eventData.getId());
            eventId.setFolder(eventData.getFolder());
            rememberEventId(eventId);
        }
    }

    private static String extractITipAttachmentId(MailData mailData, String expectedMethod) throws OXException {
        assertNotNull(mailData.getAttachments());
        for (MailAttachment attachment : mailData.getAttachments()) {
            if (ContentType.isMimeType(attachment.getContentType(), "text/calendar")) {
                if (null != expectedMethod && false == attachment.getContentType().contains(expectedMethod)) {
                    continue;
                }
                return attachment.getId();
            }
        }
        throw new AssertionError("no itip attachment found");
    }

    private static AnalyzeResponse analyze(ApiClient apiClient, MailData mailData) throws Exception {
        ConversionDataSource body = new ConversionDataSource();
        body.setComOpenexchangeMailConversionFullname(mailData.getFolderId());
        body.setComOpenexchangeMailConversionMailid(mailData.getId());
        body.setComOpenexchangeMailConversionSequenceid(extractITipAttachmentId(mailData, null));
        return new ChronosApi(apiClient).analyze(apiClient.getSession(), "com.openexchange.mail.ical", "html", body, null);
    }

    private static Attendee assertAttendeePartStat(List<Attendee> attendees, String email, String expectedPartStat) {
        Attendee attendee = extractAttendee(attendees, email);
        assertNotNull(attendee);
        assertEquals(expectedPartStat, attendee.getPartStat());
        return attendee;
    }

    private static Attendee extractAttendee(List<Attendee> attendees, String email) {
        if (null != attendees) {
            for (Attendee attendee : attendees) {
                String uri = attendee.getUri();
                if (null != uri && uri.toLowerCase().contains(email.toLowerCase())) {
                    return attendee;
                }
            }
        }
        return null;
    }

    private static com.openexchange.chronos.Attendee assertAttendeePartStat(List<com.openexchange.chronos.Attendee> attendees, String email, com.openexchange.chronos.ParticipationStatus expectedPartStat) {
        com.openexchange.chronos.Attendee matchingAttendee = null;
        if (null != attendees) {
            for (com.openexchange.chronos.Attendee attendee : attendees) {
                String uri = attendee.getUri();
                if (null != uri && uri.toLowerCase().contains(email.toLowerCase())) {
                    matchingAttendee = attendee;
                    break;
                }
            }
        }
        assertNotNull(matchingAttendee);
        assertEquals(expectedPartStat, matchingAttendee.getPartStat());
        return matchingAttendee;
    }

    private static EventData assertSingleEvent(ActionResponse actionResponse) {
        assertNotNull(actionResponse.getData());
        assertEquals(1, actionResponse.getData().size());
        return actionResponse.getData().get(0);
    }
    
    private static MailData receiveIMip(ApiClient apiClient, String fromToMatch, String subjectToMatch, int sequenceToMatch, String iTipMethodToMatch) throws Exception {
        for (int i = 0; i < 10; i++) {
            MailData mailData = lookupMail(apiClient, "default0%2FINBOX", fromToMatch, subjectToMatch, sequenceToMatch, iTipMethodToMatch);
            if (null != mailData) {
                return mailData;
            }
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
        }
        throw new AssertionError("No mail with " + subjectToMatch + " from " + fromToMatch + " received");
    }

    private static MailData lookupMail(ApiClient apiClient, String folder, String fromToMatch, String subjectToMatch, int sequenceToMatch, String iTipMethodToMatch) throws Exception {
        MailApi mailApi = new MailApi(apiClient);
        MailsResponse mailsResponse = mailApi.getAllMails(apiClient.getSession(), folder, "600,601,607,610", null, null, null, "610", "desc", null, null, I(10), null);
        assertNull(mailsResponse.getError(), mailsResponse.getError());
        assertNotNull(mailsResponse.getData());
        for (List<String> mail : mailsResponse.getData()) {
            String subject = mail.get(2);
            if (Strings.isEmpty(subject) || false == subject.contains(subjectToMatch)) {
                continue;
            }
            MailResponse mailResponse = mailApi.getMail(apiClient.getSession(), mail.get(1), mail.get(0), null, null, null, null, null, null, null, null, null, null, null, null);
            assertNull(mailResponse.getError(), mailsResponse.getError());
            assertNotNull(mailResponse.getData());
            MailData mailData = mailResponse.getData();
            if (null == extractMatchingAddress(mailData.getFrom(), fromToMatch)) {
                continue;
            }
            ImportedCalendar calendar = parseICalAttachment(apiClient, mailData, iTipMethodToMatch);
            if (null == calendar) {
                continue;
            }
            Event matchingEvent = extractMatchingEvent(calendar.getEvents(), sequenceToMatch);
            if (null == matchingEvent) {
                continue;
            }
            return mailData;
        }
        return null;
    }

    private static Event extractMatchingEvent(List<Event> events, int sequence) {
        if (null != events) {
            for (Event event : events) {
                if (event.getSequence() == sequence) {
                    return event;
                }
            }
        }
        return null;
    }

    private static List<String> extractMatchingAddress(List<List<String>> addresses, String email) {
        if (null != addresses) {
            for (List<String> address : addresses) {
                assertEquals(2, address.size());
                if (null != address.get(1) && address.get(1).contains(email)) {
                    return address;
                }
            }
        }
        return null;
    }

    private static ImportedCalendar parseICalAttachment(ApiClient apiClient, MailData mailData) throws Exception {
        return parseICalAttachment(apiClient, mailData.getFolderId(), mailData.getId(), extractITipAttachmentId(mailData, null));
    }

    private static ImportedCalendar parseICalAttachment(ApiClient apiClient, MailData mailData, String expectedMethod) throws Exception {
        return parseICalAttachment(apiClient, mailData.getFolderId(), mailData.getId(), extractITipAttachmentId(mailData, expectedMethod));
    }

    private static ImportedCalendar parseICalAttachment(ApiClient apiClient, String folder, String id, String attachmentId) throws Exception {
        MailApi mailApi = new MailApi(apiClient);
        byte[] attachment = mailApi.getMailAttachment(apiClient.getSession(), folder, id, attachmentId, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true);
        return ICalUtils.importCalendar(Streams.newByteArrayInputStream(attachment), new ICalMapper(), null);
    }

    private static MailDestinationData sendIMip(ApiClient apiClient, Object body) throws ApiException {
        Object localVarPostBody = body;

        // verify the required parameter 'session' is set
        if (null == apiClient.getSession()) {
            throw new ApiException(400, "Missing the required parameter 'session' when calling sendOrSaveMail");
        }

        // create path and map variables
        String localVarPath = "/mail?action=new".replaceAll("\\{format\\}", "json");

        // query params
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        localVarQueryParams.addAll(apiClient.parameterToPairs("", "session", apiClient.getSession()));

        final String[] localVarAccepts = {
            "application/json"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

        final String[] localVarContentTypes = {
            "text/plain"
        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {};

        GenericType<MailDestinationResponse> localVarReturnType = new GenericType<MailDestinationResponse>() {};
        MailDestinationResponse response = apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
        assertNull(response.getError(), response.getError());
        return response.getData();
    }

}
