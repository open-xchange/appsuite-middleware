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
import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertEvents;
import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertSingleChange;
import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertSingleEvent;
import static com.openexchange.ajax.chronos.itip.ITipUtil.constructBody;
import static com.openexchange.ajax.chronos.itip.ITipUtil.receiveIMip;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.testing.httpclient.models.AnalysisChangeNewEvent;
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
        replyingAttendee.setPartStat(PartStat.NEEDS_ACTION.getStatus());
        deltaEvent.getAttendees().add(replyingAttendee);

        UpdateEventBody body = getUpdateBody(deltaEvent);
        ChronosCalendarResultResponse result = chronosApi.updateEvent(defaultFolderId, createdEvent.getId(), now(), body, recurrenceId, null, null, null, null, null, null, null, null, null, null);
        /*
         * Check result
         */
        assertNotNull(result);
        assertNull(result.getError());
        assertNotNull(result.getData());
        assertNotNull(result.getData().getCreated());
        assertTrue(result.getData().getCreated().size() == 1);
        EventData changeException = result.getData().getCreated().get(0);
        assertFalse(createdEvent.getId().equals(changeException.getId()));
        assertTrue(createdEvent.getId().equals(changeException.getSeriesId()));
        assertTrue(changeException.getAttendees().size() == 2);

        /*
         * Receive mail as attendee
         */
        MailData iMip = receiveIMip(apiClientC2, userResponseC1.getData().getEmail1(), summary, 1, SchedulingMethod.REQUEST);
        AnalyzeResponse analyzeResponse = analyze(apiClientC2, iMip);
        assertNull("error during analysis: " + analyzeResponse.getError(), analyzeResponse.getCode());
        assertEquals("unexpected analysis number in response", 1, analyzeResponse.getData().size());
        analyze(analyzeResponse, CustomConsumers.ACTIONS);

        AnalysisChangeNewEvent newEvent = assertSingleChange(analyzeResponse).getNewEvent();
        assertNotNull(newEvent);
        assertEquals(createdEvent.getUid(), newEvent.getUid());
        assertAttendeePartStat(newEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.NEEDS_ACTION);

        /*
         * reply with "accept"
         */
        EventData attendeeEvent = assertSingleEvent(accept(apiClientC2, constructBody(iMip), null), createdEvent.getUid());
        assertAttendeePartStat(attendeeEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.ACCEPTED);

        /*
         * Receive mail as organizer and check actions
         */
        MailData reply = receiveIMip(apiClient, replyingAttendee.getEmail(), summary, 1, SchedulingMethod.REPLY);
        analyze(reply.getId());

        /*
         * Take over accept and check in calendar, expect master and one exception do be returned
         */
        for (EventData event : assertEvents(update(constructBody(reply)), createdEvent.getUid(), 2)) {
            for (Attendee attendee : event.getAttendees()) {
                assertThat("Participant status is not correct.", PartStat.ACCEPTED.status, is(attendee.getPartStat()));
            }
        }

        /*
         * TODO Add to another exception
         */
        /*
         * TODO Add to series
         */
    }
}
