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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.ajax.chronos.itip;

import static com.openexchange.ajax.chronos.itip.ITipUtil.receiveIMip;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.testing.httpclient.models.AnalyzeResponse;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.CalendarUser;
import com.openexchange.testing.httpclient.models.ChronosCalendarResultResponse;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.MailData;
import com.openexchange.testing.httpclient.models.UpdateEventBody;

/**
 * {@link ITipSeriesExceptionTest}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class ITipSeriesExceptionTest extends AbstractITipAnalyzeTest {

    private String summary;
    private Attendee replyingAttendee;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        /*
         * Create event
         */
        summary = this.getClass().getName() + " " + UUID.randomUUID().toString();
        EventData event = EventFactory.createSeriesEvent(getUserId(), summary, 10, defaultFolderId);
        CalendarUser c = new CalendarUser();
        c.cn(userResponseC1.getData().getDisplayName());
        c.email(userResponseC1.getData().getEmail1());
        c.entity(Integer.valueOf(userResponseC1.getData().getId()));
        event.setOrganizer(c);
        event.setCalendarUser(c);
        createdEvent = eventManager.createEvent(event);

        /*
         * Prepare replying attendee
         */
        replyingAttendee = ITipUtil.convertToAttendee(testUserC2, apiClientC2.getUserId());
        replyingAttendee.setEntity(Integer.valueOf(0));

    }

    @Test
    public void testAddToSingleOccurrence() throws Exception {
        /*
         * Create a single change occurrence as organizer
         */
        List<EventData> allEvents = getAllEventsOfCreatedEvent();
        String recurrenceId = allEvents.get(2).getRecurrenceId();

        EventData deltaEvent = prepareDeltaEvent(createdEvent);
        deltaEvent.getAttendees().add(replyingAttendee);

        UpdateEventBody body = getUpdateBody(deltaEvent);
        ChronosCalendarResultResponse result = chronosApi.updateEvent(apiClient.getSession(), defaultFolderId, createdEvent.getId(), now(), body, recurrenceId, null, null, null, null, null, null, null, null, null, null);
        /*
         * Check result
         */
        assertNotNull(result);
        assertNull(result.getError());
        assertNotNull(result.getData());
        assertNotNull(result.getData().getCreated());
        assertTrue(result.getData().getCreated().size() == 1);
        EventData changeException = result.getData().getCreated().get(0);
        rememberForCleanup(changeException);
        assertFalse(createdEvent.getId().equals(changeException.getId()));
        assertTrue(createdEvent.getId().equals(changeException.getSeriesId()));
        assertTrue(changeException.getAttendees().size() == 2);

        /*
         * Receive mail as attendee
         */
        MailData iMip = receiveIMip(apiClientC2, userResponseC1.getData().getEmail1(), summary, 1, SchedulingMethod.REQUEST);
        rememberMail(apiClientC2, iMip);
        AnalyzeResponse analyzeResponse = analyze(apiClientC2, iMip);
        assertNull("error during analysis: " + analyzeResponse.getError(), analyzeResponse.getCode());
        assertEquals("unexpected analysis number in response", 1, analyzeResponse.getData().size());
        analyze(analyzeResponse, CustomConsumers.IGNORE);

        //        XXX Use code below once the server can accept to single event occurrences
//        AnalysisChangeNewEvent newEvent = assertSingleChange(analyzeResponse).getNewEvent();
//        assertNotNull(newEvent);
//        assertEquals(createdEvent.getUid(), newEvent.getUid());
//        assertAttendeePartStat(newEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.NEEDS_ACTION);
//
//        /*
//         * reply with "accepted"
//         */
//        EventData attendeeEvent = assertSingleEvent(accept(apiClientC2, constructBody(iMip)), createdEvent.getUid());
//        assertAttendeePartStat(attendeeEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.ACCEPTED);
//        rememberForCleanup(apiClientC2, attendeeEvent);
//
//        /*
//         * Receive mail as organizer and check actions
//         */
//        MailData reply = receiveIMip(apiClient, replyingAttendee.getEmail(), summary, 0, SchedulingMethod.REPLY);
//        rememberMail(reply);
//        analyze(reply.getId());
//
//        /*
//         * Take over accept and check in calendar
//         */
//        EventData event = assertSingleEvent(update(constructBody(reply)));
//        rememberForCleanup(event);
//        for (Attendee attendee : event.getAttendees()) {
//            assertThat("Participant status is not correct.", PartStat.ACCEPTED.status, is(attendee.getPartStat()));
//        }
    }
}
