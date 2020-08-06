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

package com.openexchange.ajax.chronos.itip;

import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertAttendeePartStat;
import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertSingleChange;
import static com.openexchange.ajax.chronos.itip.ITipAssertion.assertSingleEvent;
import static com.openexchange.ajax.chronos.itip.ITipUtil.constructBody;
import static com.openexchange.ajax.chronos.itip.ITipUtil.receiveIMip;
import static com.openexchange.java.Autoboxing.I;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.factory.EventFactory.RecurringFrequency;
import com.openexchange.ajax.chronos.factory.RRuleFactory;
import com.openexchange.ajax.chronos.manager.ChronosApiException;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.testing.httpclient.models.AnalysisChangeNewEvent;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.DateTimeData;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventId;
import com.openexchange.testing.httpclient.models.EventResponse;
import com.openexchange.testing.httpclient.models.MailData;

/**
 * {@link ITipCancelTest}
 * Starting with:
 * - Test User 1 from context C1
 * - Test User 2 from context C2
 * and their API clients as well as user information
 * 
 * <p>
 * Note: Also tests should only check one specific aspect, iTIP test require to do some steps. Therefore we can test
 * everything at once, to avoid duplicating code.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public class ITipCancelTest extends AbstractITipAnalyzeTest {

    private String summary;

    private EventData attendeeEvent;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.summary = this.getClass().getSimpleName() + UUID.randomUUID().toString();
    }

    @Override
    public void tearDown() throws Exception {
        try {
            createdEvent = null;
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testSingleEvent_CancleEvent() throws Exception {
        EventData eventToCreate = EventFactory.createSingleTwoHourEvent(0, summary);
        Attendee replyingAttendee = prepareCommonAttendees(eventToCreate);
        createdEvent = prepareSituation(eventToCreate, replyingAttendee);

        /*
         * Delete event as organizer
         */
        eventManager.deleteEvent(createdEvent, null);

        /*
         * Receive cancel as attendee
         */
        MailData iMip = receiveIMip(apiClientC2, userResponseC1.getData().getEmail1(), "Appointment canceled: " + summary, 1, SchedulingMethod.CANCEL);
        rememberMail(apiClientC2, iMip);
        analyze(analyze(apiClientC2, iMip), CustomConsumers.CANCEL);
        cancel(apiClientC2, constructBody(iMip), null, false);

        /*
         * Check that event has been deleted
         */
        Exception e = null;
        try {
            attendeeEvent = eventManagerC2.getEvent(folderIdC2, attendeeEvent.getId(), true);
        } catch (ChronosApiException cae) {
            e = cae;
        }
        Assert.assertNotNull("Excpected an error", e);

        /*
         * Check that there is no REPLY mail from the attendee
         */
        checkNoReply(replyingAttendee);
    }

    @Test
    public void testSeriesEvent_CancelOccurrence() throws Exception {
        EventData seriesToCreate = EventFactory.createSeriesEvent(0, summary, 10, defaultFolderId);
        Attendee replyingAttendee = prepareCommonAttendees(seriesToCreate);
        createdEvent = prepareSituation(seriesToCreate, replyingAttendee);

        /*
         * Delete the second occurrence as organizer
         */
        List<EventData> events = getAllEventsOfCreatedEvent();
        EventData secondOccurrence = events.get(2);
        EventId eventId = new EventId();
        eventId.setId(secondOccurrence.getId());
        eventId.setFolder(defaultFolderId);
        eventId.setRecurrenceId(secondOccurrence.getRecurrenceId());
        eventManager.deleteEvent(eventId);

        /*
         * Receive CANCEL as attendee
         */
        MailData iMip = receiveIMip(apiClientC2, userResponseC1.getData().getEmail1(), "Appointment canceled: " + summary, 1, SchedulingMethod.CANCEL);
        rememberMail(apiClientC2, iMip);
        analyze(analyze(apiClientC2, iMip), CustomConsumers.CANCEL);

        cancel(apiClientC2, constructBody(iMip), null, true);

        /*
         * Check that occurrence has been deleted
         */
        attendeeEvent = eventManagerC2.getEvent(folderIdC2, attendeeEvent.getId());
        assertThat("Event has not been deleted!", I(attendeeEvent.getDeleteExceptionDates().size()), is(I(1)));
        assertThat("Wron occurrence has been deleted!", attendeeEvent.getDeleteExceptionDates().get(0), is(secondOccurrence.getRecurrenceId().toString()));

        /*
         * Check that there is no REPLY mail from the attendee
         */
        checkNoReply(replyingAttendee);
    }

    @Test
    public void testSeriesEvent_CancleSeries() throws Exception {
        EventData seriesToCreate = EventFactory.createSeriesEvent(0, summary, 10, defaultFolderId);
        Attendee replyingAttendee = prepareCommonAttendees(seriesToCreate);
        createdEvent = prepareSituation(seriesToCreate, replyingAttendee);

        /*
         * Delete series as organizer
         */
        EventId eventId = new EventId();
        eventId.setId(createdEvent.getId());
        eventId.setFolder(defaultFolderId);
        eventManager.deleteEvent(eventId);

        /*
         * Receive CANCEL as attendee
         */
        MailData iMip = receiveIMip(apiClientC2, userResponseC1.getData().getEmail1(), "Appointment canceled: " + summary, 1, SchedulingMethod.CANCEL);
        rememberMail(apiClientC2, iMip);
        analyze(analyze(apiClientC2, iMip), CustomConsumers.CANCEL);

        cancel(apiClientC2, constructBody(iMip), null, false);

        /*
         * Check that series has been deleted
         */
        Exception e = null;
        try {
            attendeeEvent = eventManagerC2.getEvent(folderIdC2, attendeeEvent.getId(), true);
        } catch (ChronosApiException cae) {
            e = cae;
        }
        Assert.assertNotNull("Excpected an error", e);
        checkNoReply(replyingAttendee);
    }

    @Test
    public void testSeriesEvent_ShortenSeries() throws Exception {
        EventData seriesToCreate = EventFactory.createSeriesEvent(0, summary, 10, defaultFolderId);
        Attendee replyingAttendee = prepareCommonAttendees(seriesToCreate);
        createdEvent = prepareSituation(seriesToCreate, replyingAttendee);

        /*
         * Shorten series as organizer by two days
         */
        List<EventData> events = getAllEventsOfCreatedEvent();
        EventData fifthOccurrence = events.get(5);
        EventId eventId = new EventId();
        eventId.setId(fifthOccurrence.getId());
        eventId.setFolder(defaultFolderId);
        eventId.setRecurrenceId(fifthOccurrence.getRecurrenceId());
        eventId.setRecurrenceRange("THISANDFUTURE");
        eventManager.deleteEvent(eventId);

        /*
         * Receive REQUEST as attendee. Shorten the series behaves more like an update, most other
         * calendar providers do it the same way.
         */
        MailData iMip = receiveIMip(apiClientC2, userResponseC1.getData().getEmail1(), summary, 1, SchedulingMethod.REQUEST);
        rememberMail(apiClientC2, iMip);
        analyze(analyze(apiClientC2, iMip), CustomConsumers.ALL);

        update(apiClientC2, constructBody(iMip));

        /*
         * Check that series has been shortened
         */
        attendeeEvent = eventManagerC2.getEvent(folderIdC2, attendeeEvent.getId());
        assertThat("Missing rrule", attendeeEvent.getRrule(), is(not(nullValue())));
        DateTimeData expectedUntil = DateTimeUtil.getDateTimeWithoutTimeInformation(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(5));
        String expectedRRule = RRuleFactory.RRuleBuilder.create().addFrequency(RecurringFrequency.DAILY).addUntil(expectedUntil).build();
        assertThat("Wrong rrule", attendeeEvent.getRrule(), startsWith(expectedRRule));
        checkNoReply(replyingAttendee);
    }

    private EventData prepareSituation(EventData event, Attendee replyingAttendee) throws Exception {
        /*
         * Create event, user A is organizer
         */
        EventData created = eventManager.createEvent(event, true);

        /*
         * Receive mail as attendee
         */
        MailData iMip = receiveIMip(apiClientC2, userResponseC1.getData().getEmail1(), summary, 0, SchedulingMethod.REQUEST);
        rememberMail(apiClientC2, iMip);
        AnalysisChangeNewEvent newEvent = assertSingleChange(analyze(apiClientC2, iMip)).getNewEvent();
        assertNotNull(newEvent);
        assertEquals(created.getUid(), newEvent.getUid());
        assertAttendeePartStat(newEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.NEEDS_ACTION.status);

        /*
         * reply with "accepted"
         */
        attendeeEvent = assertSingleEvent(accept(apiClientC2, constructBody(iMip), null), created.getUid());
        assertAttendeePartStat(attendeeEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.ACCEPTED.status);
        rememberForCleanup(apiClientC2, attendeeEvent);

        /*
         * Receive mail as organizer and check actions
         */
        MailData reply = receiveIMip(apiClient, replyingAttendee.getEmail(), summary, 0, SchedulingMethod.REPLY);
        analyze(reply.getId());
        rememberMail(reply);

        /*
         * Apply change as organizer via iTIP API
         */
        assertSingleEvent(update(constructBody(reply)), created.getUid());
        EventResponse eventResponse = chronosApi.getEvent(apiClient.getSession(), created.getId(), created.getFolder(), created.getRecurrenceId(), null, null);
        assertNull(eventResponse.getError(), eventResponse.getError());
        createdEvent = eventResponse.getData();
        for (Attendee attendee : createdEvent.getAttendees()) {
            assertThat("Participant status is not correct.", PartStat.ACCEPTED.status, is(attendee.getPartStat()));
        }
        return created;
    }

    private void checkNoReply(Attendee replyingAttendee) throws Exception {
        Error error = null;
        try {
            MailData reply = receiveIMip(apiClient, replyingAttendee.getEmail(), summary, 1, SchedulingMethod.REPLY);
            rememberMail(apiClient, reply);
        } catch (AssertionError ae) {
            error = ae;
        }
        Assert.assertNotNull("Excpected an error", error);
    }

}
