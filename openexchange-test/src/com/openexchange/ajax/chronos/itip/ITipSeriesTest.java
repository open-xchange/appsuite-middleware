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

import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertAttendeePartStat;
import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertChanges;
import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertEvents;
import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertSingleChange;
import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertSingleDescription;
import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertSingleEvent;
import static com.openexchange.ajax.chronos.itip.ITipUtil.acceptSummary;
import static com.openexchange.ajax.chronos.itip.ITipUtil.constructBody;
import static com.openexchange.ajax.chronos.itip.ITipUtil.receiveIMip;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.java.Strings;
import com.openexchange.testing.httpclient.models.AnalysisChange;
import com.openexchange.testing.httpclient.models.AnalysisChangeNewEvent;
import com.openexchange.testing.httpclient.models.AnalyzeResponse;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.CalendarResult;
import com.openexchange.testing.httpclient.models.ChronosCalendarResultResponse;
import com.openexchange.testing.httpclient.models.ChronosMultipleCalendarResultResponse;
import com.openexchange.testing.httpclient.models.DeleteEventBody;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventId;
import com.openexchange.testing.httpclient.models.EventResponse;
import com.openexchange.testing.httpclient.models.MailData;
import com.openexchange.testing.httpclient.models.UpdateEventBody;

/**
 * {@link ITipSeriesTest}
 *
 * User A from context 1 will create a series with 10 occurrences with user B from context 2 as attendee.
 * User B will accept the event in the setup and the change will be accepted by the organizer.
 *
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class ITipSeriesTest extends AbstractITipAnalyzeTest {

    private String summary;

    /** User B from context 2 */
    private Attendee replyingAttendee;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        summary = this.getClass().getName() + " " + UUID.randomUUID().toString();
        EventData event = EventFactory.createSeriesEvent(getUserId(), summary, 10, defaultFolderId);
        replyingAttendee = prepareCommonAttendees(event);

        createdEvent = eventManager.createEvent(event);

        /*
         * Receive mail as attendee
         */
        MailData inviteMail = receiveIMip(apiClientC2, testUser.getLogin(), summary, 0, SchedulingMethod.REQUEST);
        AnalysisChangeNewEvent newEvent = assertSingleChange(analyze(apiClientC2, inviteMail)).getNewEvent();
        assertNotNull(newEvent);
        assertEquals(createdEvent.getUid(), newEvent.getUid());
        assertAttendeePartStat(newEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.NEEDS_ACTION.status);

        /*
         * reply with "accepted"
         */
        EventData attendeeEvent = assertSingleEvent(accept(apiClientC2, constructBody(inviteMail), null), createdEvent.getUid());
        assertAttendeePartStat(attendeeEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.ACCEPTED.status);

        MailData reply = receiveIMip(apiClient, replyingAttendee.getEmail(), summary, 0, SchedulingMethod.REPLY);
        analyze(reply.getId());

        /*
         * Take over accept and check in calendar
         */
        assertSingleEvent(update(constructBody(reply)), createdEvent.getUid());
        EventResponse eventResponse = chronosApi.getEvent(createdEvent.getId(), createdEvent.getFolder(), createdEvent.getRecurrenceId(), null, null);
        assertNull(eventResponse.getError(), eventResponse.getError());
        createdEvent = eventResponse.getData();
        for (Attendee attendee : createdEvent.getAttendees()) {
            assertThat("Participant status is not correct.", PartStat.ACCEPTED.status, is(attendee.getPartStat()));
        }
    }

    @Test
    public void testDeleteSingleOccurrence() throws Exception {
        /*
         * Delete a single occurrence as organizer
         */
        List<EventData> allEvents = getAllEventsOfCreatedEvent();
        String recurrenceId = allEvents.get(2).getRecurrenceId();

        DeleteEventBody body = new DeleteEventBody();
        EventId id = new EventId();
        id.setFolder(defaultFolderId);
        id.setId(createdEvent.getId());
        id.setRecurrenceId(recurrenceId);
        body.setEvents(Collections.singletonList(id));
        ChronosMultipleCalendarResultResponse result = chronosApi.deleteEvent(now(), body, null, null, null, null, null, null, null);

        /*
         * Check result
         */
        assertNotNull(result);
        assertNull(result.getError());
        assertNotNull(result.getData());
        assertTrue("Only one element should be deleted", result.getData().size() == 1);
        assertNotNull(result.getData().get(0).getUpdated());
        assertTrue("Only one element should be deleted", result.getData().get(0).getUpdated().size() == 1);
        assertTrue("Only one element should be deleted", result.getData().get(0).getUpdated().get(0).getDeleteExceptionDates().size() == 1);
        assertThat(result.getData().get(0).getUpdated().get(0).getDeleteExceptionDates().get(0), is(recurrenceId));

        /*
         * Receive deletion as attendee and delete
         */
        MailData iMip = receiveIMip(apiClientC2, testUser.getLogin(), "Appointment canceled: " + summary, 1, SchedulingMethod.CANCEL);
        AnalyzeResponse analyzeResponse = analyze(apiClientC2, iMip);
        analyze(analyzeResponse, CustomConsumers.CANCEL);
        cancel(apiClientC2, constructBody(iMip), null, true);
    }

    @Test
    public void testChangeSingleOccurrenceAndSplit() throws Exception {
        /*
         * Update a single occurrence as organizer
         */
        List<EventData> allEvents = getAllEventsOfCreatedEvent();
        String recurrenceId = allEvents.get(2).getRecurrenceId();

        EventData deltaEvent = prepareDeltaEvent(createdEvent);
        deltaEvent.setDescription("Totally new description for " + this.getClass().getName());

        UpdateEventBody body = getUpdateBody(deltaEvent);
        ChronosCalendarResultResponse result = chronosApi.updateEvent(defaultFolderId, createdEvent.getId(), now(), body, recurrenceId, null, null, null, null, null, null, null, null, null, null);
        /*
         * Check result
         */
        assertNotNull(result);
        CalendarResult calendarResult = checkResponse(result.getError(), result.getErrorDesc(), result.getData());
        assertNotNull(calendarResult.getCreated());
        assertTrue(calendarResult.getCreated().size() == 1);
        assertTrue(calendarResult.getUpdated().size() == 1);
        assertFalse(createdEvent.getId().equals(calendarResult.getCreated().get(0).getId()));
        assertTrue(createdEvent.getId().equals(calendarResult.getCreated().get(0).getSeriesId()));
        String exceptionId = calendarResult.getCreated().get(0).getId();
        createdEvent = calendarResult.getUpdated().get(0);
        /*
         * Receive update as attendee, no rescheduling
         */
        MailData iMip = receiveIMip(apiClientC2, testUser.getLogin(), summary, 1, SchedulingMethod.REQUEST);
        AnalyzeResponse analyzeResponse = analyze(apiClientC2, iMip);
        AnalysisChange change = assertSingleChange(analyzeResponse);
        AnalysisChangeNewEvent newEvent = change.getNewEvent();
        assertNotNull(newEvent);
        assertAttendeePartStat(newEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.ACCEPTED.getStatus());
        analyze(analyzeResponse, CustomConsumers.ALL);
        assertSingleDescription(change, "The appointment description has changed");
        /*
         * Tentative accept for attendee and check in calendar
         */
        tentative(apiClientC2, constructBody(iMip), null);
        EventData attendeeMaster = eventManagerC2.getEvent(null, change.getCurrentEvent().getSeriesId());
        assertTrue("No exception", attendeeMaster.getChangeExceptionDates().size() == 1);
        assertAttendeePartStat(attendeeMaster.getAttendees(), replyingAttendee.getEmail(), PartStat.ACCEPTED.getStatus());
        EventData attendeeException = eventManagerC2.getRecurringEvent(null, attendeeMaster.getId(), attendeeMaster.getChangeExceptionDates().get(0), false);
        assertAttendeePartStat(attendeeException.getAttendees(), replyingAttendee.getEmail(), PartStat.TENTATIVE.getStatus());
        /*
         * Update calendar object with the tentative accepted event as organizer
         */
        MailData reply = receiveIMip(apiClient, replyingAttendee.getEmail(), summary, 1, SchedulingMethod.REPLY);
        assertEvents(update(constructBody(reply)), createdEvent.getUid(), 2);
        EventResponse eventResponse = chronosApi.getEvent(exceptionId, createdEvent.getFolder(), null, null, null);
        assertNull(eventResponse.getError(), eventResponse.getError());
        assertAttendeePartStat(eventResponse.getData().getAttendees(), replyingAttendee.getEmail(), PartStat.TENTATIVE.getStatus());
        /*
         * Split series by changing location and summary at fifth occurrence
         */
        recurrenceId = allEvents.get(5).getRecurrenceId();
        String location = "Olpe";
        String updatedSummary = this.getClass().getName() + " " + UUID.randomUUID().toString();
        deltaEvent = prepareDeltaEvent(createdEvent);
        deltaEvent.setLocation(location);
        deltaEvent.setSummary(updatedSummary);
        body = getUpdateBody(deltaEvent);
        result = chronosApi.updateEvent(defaultFolderId, createdEvent.getId(), now(), body, recurrenceId, THIS_AND_FUTURE, null, null, null, null, null, null, null, null, null);
        /*
         * Check result
         */
        assertNotNull(result);
        calendarResult = checkResponse(result.getError(), result.getErrorDesc(), result.getData());
        assertNotNull(calendarResult.getCreated());
        assertTrue(calendarResult.getCreated().size() == 1);
        assertFalse(createdEvent.getUid().equals(calendarResult.getCreated().get(0).getUid()));
        assertTrue(calendarResult.getUpdated().size() == 2);
        assertTrue(createdEvent.getUid().equals(calendarResult.getUpdated().get(0).getUid()) || createdEvent.getUid().equals(calendarResult.getUpdated().get(1).getUid()));
        createdEvent = eventManager.getEvent(createdEvent.getFolder(), createdEvent.getSeriesId());
        assertThat("Should have no exception", createdEvent.getChangeExceptionDates(), is(empty()));
        /*
         * Get mails as attendee
         * a) Update with existing UID of event, updated summary
         * b) Invitation to detached series with the existing change exception, old summary
         */
        /*--------- a) --------*/
        /*
         * Get update to existing event as attendee and decline
         */
        MailData updatedSeriersIMip = receiveIMip(apiClientC2, testUser.getLogin(), updatedSummary, 1, SchedulingMethod.REQUEST);
        analyzeResponse = analyze(apiClientC2, updatedSeriersIMip);
        change = assertSingleChange(analyzeResponse);
        AnalysisChangeNewEvent updatedEvent = change.getNewEvent();
        assertNotNull(updatedEvent);
        assertAttendeePartStat(updatedEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.ACCEPTED.getStatus());
        assertEquals("Updated series must have the same UID as before", createdEvent.getUid(), updatedEvent.getUid());
        analyze(analyzeResponse, CustomConsumers.ACTIONS);
        ITipAssertion.assertMultipleDescription(change, "recurrence rule has changed", "appointment was rescheduled", "new subject", "takes place in a new location");
        String updatedEventSeriesId = assertSingleEvent(decline(apiClientC2, constructBody(updatedSeriersIMip), null)).getSeriesId();
        assertTrue(Strings.isNotEmpty(updatedEventSeriesId));
        assertTrue(attendeeMaster.getSeriesId().equals(updatedEventSeriesId));
        /*
         * Check event in attendees calendar
         */
        EventData attendeeSeriesMaster = eventManagerC2.getEvent(null, updatedEventSeriesId);
        assertAttendeePartStat(attendeeSeriesMaster.getAttendees(), replyingAttendee.getEmail(), PartStat.DECLINED.getStatus());
        assertAttendeePartStat(attendeeSeriesMaster.getAttendees(), testUser.getLogin(), PartStat.ACCEPTED.getStatus());
        assertThat(attendeeSeriesMaster.getChangeExceptionDates(), is(empty())); // TODO BUG?!
        /*
         * Check reply in organizers inbox
         */
        reply = receiveIMip(apiClient, replyingAttendee.getEmail(), updatedSummary, 1, SchedulingMethod.REPLY);
        analyze(reply.getId());

        /*--------- b) --------*/
        /*
         * Get invitation to new series, wit new UID and a existing change exception. Expect unchanged part stat
         */
        MailData newSeriersIMip = receiveIMip(apiClientC2, testUser.getLogin(), summary, 1, SchedulingMethod.REQUEST);
        newEvent = assertChanges(analyze(apiClientC2, newSeriersIMip), 2, 0).getNewEvent();
        assertNotNull(newEvent);
        assertNotEquals("New series must NOT have the same UID as before", createdEvent.getUid(), newEvent.getUid());
        assertAttendeePartStat(newEvent.getAttendees(), replyingAttendee.getEmail(), null == newEvent.getRrule() ? PartStat.TENTATIVE.getStatus() : PartStat.ACCEPTED.getStatus());

        /*
         * Accept and check that master and exception are accepted
         */
        for (EventData attendeeEvent : ITipAssertion.assertEvents(accept(apiClientC2, constructBody(newSeriersIMip), null), newEvent.getUid(), 2)) {
            assertAttendeePartStat(attendeeEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.ACCEPTED.status);
        }

        /*
         * Check reply in organizers inbox
         */
        reply = receiveIMip(apiClient, replyingAttendee.getEmail(), acceptSummary(replyingAttendee.getCn(), summary), 1, SchedulingMethod.REPLY);
        analyze(reply.getId());
    }
}
