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
import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertEvents;
import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertSingleChange;
import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertSingleEvent;
import static com.openexchange.ajax.chronos.itip.ITipUtil.constructBody;
import static com.openexchange.ajax.chronos.itip.ITipUtil.receiveIMip;
import static com.openexchange.ajax.chronos.util.DateTimeUtil.incrementDateTimeData;
import static com.openexchange.ajax.chronos.util.DateTimeUtil.parseDateTime;
import static com.openexchange.java.Autoboxing.I;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.Test;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.itip.AbstractITipAnalyzeTest;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.java.Strings;
import com.openexchange.testing.httpclient.models.AnalysisChange;
import com.openexchange.testing.httpclient.models.AnalysisChangeNewEvent;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.MailData;

/**
 * {@link MWB263Test}
 * 
 * 1. Create a series event as user A in context 1.
 * 2. Create a change exception as organizer by moving a specific event
 * 3. Receive mails as attendee B in context 2.
 * 4. Accept series
 * 5. Decline exception
 * 
 * 6. Exception got duplicated
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public class MWB263Test extends AbstractITipAnalyzeTest {

    private String summary;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.summary = this.getClass().getSimpleName() + UUID.randomUUID().toString();
    }

    @Test
    public void testBug() throws Exception {
        EventData eventToCreate = EventFactory.createSeriesEvent(getUserId(), summary, 10, defaultFolderId);
        Attendee replyingAttendee = prepareCommonAttendees(eventToCreate);

        createdEvent = eventManager.createEvent(eventToCreate, true);

        /*
         * Immediately create exception by updating end date
         */
        List<EventData> events = getAllEventsOfCreatedEvent();
        EventData secondOccurrence = events.get(1);
        EventData deltaEvent = prepareDeltaEvent(secondOccurrence);
        deltaEvent.setEndDate(incrementDateTimeData(secondOccurrence.getEndDate(), TimeUnit.HOURS.toMillis(1)));
        createdEvent = eventManager.updateEvent(deltaEvent);
        assertThat(createdEvent.getSequence(), is(I(0)));

        /*
         * Receive series mail as attendee
         */
        MailData iMip = receiveIMip(apiClientC2, userResponseC1.getData().getEmail1(), summary, 0, SchedulingMethod.REQUEST);
        AnalysisChangeNewEvent newEvent = assertSingleChange(analyze(apiClientC2, iMip)).getNewEvent();
        assertNotNull(newEvent);
        assertEquals(createdEvent.getUid(), newEvent.getUid());
        assertAttendeePartStat(newEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.NEEDS_ACTION.getStatus());

        /*
         * Reply with "accepted"
         */
        EventData attendeeEvent = assertSingleEvent(accept(apiClientC2, constructBody(iMip), null), createdEvent.getUid());
        assertAttendeePartStat(attendeeEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.ACCEPTED.getStatus());

        /*
         * Receive accept as organizer and update event
         */
        MailData reply = receiveIMip(apiClient, testUserC2.getLogin(), summary, 0, SchedulingMethod.REPLY);
        AnalysisChangeNewEvent series = assertSingleChange(analyze(apiClient, reply)).getNewEvent();
        assertNotNull(series);
        assertAttendeePartStat(series.getAttendees(), replyingAttendee.getEmail(), PartStat.ACCEPTED.getStatus());
        update(constructBody(reply));

        /*
         * Check updated event from organizer view
         */
        EventData masterEvent = eventManager.getEvent(null, createdEvent.getId());
        assertAttendeePartStat(masterEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.ACCEPTED.getStatus());
        assertAttendeePartStat(masterEvent.getAttendees(), testUser.getLogin(), PartStat.ACCEPTED.getStatus());
        assertThat(masterEvent.getSequence(), is(I(0)));

        /*
         * Receive exception mail as attendee, status for change exception is still NEEDS_ACTION as mail was sent before organizer updated
         */
        MailData iMipException = receiveIMip(apiClientC2, userResponseC1.getData().getEmail1(), summary, 1, SchedulingMethod.REQUEST);
        AnalysisChange change = assertSingleChange(analyze(apiClientC2, iMipException));
        assertThat(change.getCurrentEvent().getId(), is(attendeeEvent.getId()));
        AnalysisChangeNewEvent newException = change.getNewEvent();
        assertNotNull(newException);
        assertEquals(secondOccurrence.getRecurrenceId(), newException.getRecurrenceId());
        assertAttendeePartStat(newException.getAttendees(), replyingAttendee.getEmail(), PartStat.NEEDS_ACTION.getStatus());

        /*
         * Reply with "decline", look up if the exception is created for the series, check series afterwards
         */
        List<EventData> updates = assertEvents(decline(apiClientC2, constructBody(iMipException), null), createdEvent.getUid(), 2);
        for (EventData event : updates) {
            assertAttendeePartStat(event.getAttendees(), replyingAttendee.getEmail(), //
                Strings.isNotEmpty(event.getSeriesId()) && event.getSeriesId().equals(event.getId()) ? //
                    PartStat.ACCEPTED.getStatus() : PartStat.DECLINED.getStatus());
            assertThat(event.getSeriesId(), is(attendeeEvent.getId()));
            assertThat(event.getUid(), is(attendeeEvent.getUid()));
        }
        attendeeEvent = eventManagerC2.getEvent(attendeeEvent.getFolder(), attendeeEvent.getSeriesId());
        assertAttendeePartStat(attendeeEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.ACCEPTED.getStatus());
        assertThat("no excpetion", attendeeEvent.getChangeExceptionDates(), is(not(empty())));
        assertThat("no excpetion", I(attendeeEvent.getChangeExceptionDates().size()), is(I(1)));
        /*
         * Ensure that there are no duplicates
         */
        List<EventData> allEvents = eventManagerC2.getAllEvents(parseDateTime(eventToCreate.getStartDate()), parseDateTime(deltaEvent.getEndDate()));
        allEvents = allEvents.stream().filter(e -> summary.equals(e.getSummary())).collect(Collectors.toList());
        assertThat(I(allEvents.size()), is(I(2)));
        /*
         * Ensure that the master event wasn't updated (besides change exception field) 
         * Reason: Only an exception was transmitted in the mail, no update for the master indicated
         */
        allEvents.forEach(e -> assertThat(e.getSequence(), is(I(e.getId().equals(e.getSeriesId()) ? 0 : 1))));

        /*
         * Receive decline as organizer and update event
         */
        reply = receiveIMip(apiClient, testUserC2.getLogin(), summary, 1, SchedulingMethod.REPLY);
        AnalysisChangeNewEvent exception = assertSingleChange(analyze(apiClient, reply)).getNewEvent();
        assertNotNull(exception);
        assertEquals(secondOccurrence.getRecurrenceId(), exception.getRecurrenceId());
        assertAttendeePartStat(exception.getAttendees(), replyingAttendee.getEmail(), PartStat.DECLINED.getStatus());
        update(constructBody(reply));

        /*
         * Check updated event from organizer view
         */
        masterEvent = eventManager.getEvent(null, createdEvent.getId());
        assertAttendeePartStat(masterEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.ACCEPTED.getStatus());
        assertAttendeePartStat(masterEvent.getAttendees(), testUser.getLogin(), PartStat.ACCEPTED.getStatus());
        assertTrue(null != masterEvent.getChangeExceptionDates() && masterEvent.getChangeExceptionDates().size() == 1);
        assertThat(masterEvent.getSequence(), is(I(0)));
        EventData exceptionEvent = eventManager.getRecurringEvent(null, masterEvent.getId(), masterEvent.getChangeExceptionDates().get(0), false);
        assertAttendeePartStat(exceptionEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.DECLINED.getStatus());
        assertAttendeePartStat(exceptionEvent.getAttendees(), testUser.getLogin(), PartStat.ACCEPTED.getStatus());
        assertThat(exceptionEvent.getSequence(), is(I(1)));
    }

}
