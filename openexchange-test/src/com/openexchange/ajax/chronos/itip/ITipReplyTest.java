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
import static com.openexchange.ajax.chronos.itip.ITipUtil.prepareForAttendeeUpdate;
import static com.openexchange.ajax.chronos.itip.ITipUtil.receiveIMip;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.jms.IllegalStateException;
import org.junit.Test;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.java.Strings;
import com.openexchange.test.common.test.TestClassConfig;
import com.openexchange.test.common.test.pool.TestUser;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.AnalysisChangeNewEvent;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.AttendeeAndAlarm;
import com.openexchange.testing.httpclient.models.ComposeBody;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventResponse;
import com.openexchange.testing.httpclient.models.MailComposeResponse;
import com.openexchange.testing.httpclient.models.MailComposeResponseMessageModel;
import com.openexchange.testing.httpclient.models.MailComposeSendResponse;
import com.openexchange.testing.httpclient.models.MailData;
import com.openexchange.testing.httpclient.modules.MailComposeApi;

/**
 * {@link ITipReplyTest}
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
 * @since v7.10.0
 */
public class ITipReplyTest extends AbstractITipAnalyzeTest {

    private String summary;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.summary = this.getClass().getSimpleName() + UUID.randomUUID().toString();
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createApiClient().withContexts(2).withUserPerContext(3).useEnhancedApiClients().build();
    }

    @Test
    public void testSingleEvent_AcceptAsAttendee() throws Exception {
        EventData eventToCreate = EventFactory.createSingleTwoHourEvent(0, summary);
        Attendee replyingAttendee = prepareCommonAttendees(eventToCreate);
        String comment = "Just a comment";
        prepareSituation(eventToCreate, replyingAttendee, comment);
        checkComment(createdEvent, replyingAttendee, comment);
    }

    @Test
    public void testSeriesEvent_AcceptAsAttendee_CreateDeclineException() throws Exception {
        EventData seriesToCreate = EventFactory.createSeriesEvent(0, summary, 10, defaultFolderId);
        Attendee replyingAttendee = prepareCommonAttendees(seriesToCreate);
        EventData attendeeEvent = prepareSituation(seriesToCreate, replyingAttendee, null);

        /*
         * Decline the second occurrence as attendee
         */
        List<EventData> events = getAllEventsOfEvent(eventManagerC2, attendeeEvent);
        EventData secondOccurrence = events.get(2);
        EventData deltaEvent = prepareDeltaEvent(secondOccurrence);
        String comment = "No time";
        AttendeeAndAlarm attendeeAndAlarm = prepareForAttendeeUpdate(deltaEvent, replyingAttendee.getEmail(), PartStat.DECLINED.status, comment);
        eventManagerC2.updateAttendee(attendeeEvent.getId(), secondOccurrence.getRecurrenceId(), null, attendeeAndAlarm, false);

        /*
         * Receive mail to declined occurrence as organizer and check actions
         */
        MailData reply = receiveIMip(apiClient, replyingAttendee.getEmail(), "declined the invitation: " + summary, 0, SchedulingMethod.REPLY);
        analyze(reply.getId());

        /*
         * Apply change as organizer via iTIP API, expect the master with the new change exception and exception itself to be returned
         */
        List<EventData> changedEvents = assertEvents(update(constructBody(reply)), createdEvent.getUid(), 2);
        EventData createdSecoundOccurrence = changedEvents.stream().filter(e -> !e.getId().equals(createdEvent.getId())).findAny().orElseThrow(() -> new IllegalStateException("Exception not found"));
        createdSecoundOccurrence = eventManager.getEvent(createdSecoundOccurrence.getFolder(), createdSecoundOccurrence.getId());
        Optional<Attendee> declinedAttendee = createdSecoundOccurrence.getAttendees().stream().filter(a -> a.getEmail().equals(replyingAttendee.getEmail())).findFirst();
        assertTrue(declinedAttendee.isPresent());
        assertThat("Participant status is not correct.", PartStat.DECLINED.status, is(declinedAttendee.get().getPartStat()));
        assertTrue(comment.equalsIgnoreCase(declinedAttendee.get().getComment()));
        checkComment(changedEvents.stream().filter(e -> e.getId().equals(createdEvent.getId())).findAny().orElseThrow(() -> new IllegalStateException("Master not found")), declinedAttendee.get(), null);
    }

    @Test
    public void testSingleEvent_AddPartyCrasher_propagateToAttendees() throws Exception {
        EventData eventToCreate = EventFactory.createSingleTwoHourEvent(0, summary);
        Attendee replyingAttendee = prepareCommonAttendees(eventToCreate);
        prepareSituation(eventToCreate, replyingAttendee, null);

        /*
         * Forward to other user of same context
         */
        TestUser partyCrasher = context2.acquireUser();
        String crashersMail = partyCrasher.getLogin();

        /*
         * Receive mail
         */
        MailComposeApi mailComposeApi = new MailComposeApi(apiClientC2);
        ComposeBody composeBody = new ComposeBody();
        composeBody.setId(receiveIMip(apiClientC2, userResponseC1.getData().getEmail1(), summary, 0, SchedulingMethod.REQUEST).getId());
        composeBody.setFolderId(ITipUtil.FOLDER_HUMAN_READABLE);
        MailComposeResponse mailCompose = mailComposeApi.postMailCompose("forward", Boolean.FALSE, null, Collections.singletonList(composeBody));
        assertNull(mailCompose.getError());

        /*
         * Set to new recipient
         */
        MailComposeResponseMessageModel data = mailCompose.getData();
        ArrayList<String> toList = new ArrayList<>(2);
        toList.add(partyCrasher.getUser());
        toList.add(crashersMail);
        ArrayList<String> fromList = new ArrayList<>(2);
        fromList.add(userResponseC2.getData().getDisplayName());
        fromList.add(userResponseC2.getData().getEmail1());
        data.setTo(Collections.singletonList(toList));
        data.setFrom(fromList);
        MailComposeSendResponse forwardedMail = mailComposeApi.postMailComposeSend(data.getId(), data.toJson(), null, null);
        assertNull(forwardedMail.getErrorDesc());

        /*
         * Receive forwarded mail as party crasher
         */
        ApiClient apiClient3 = partyCrasher.getApiClient();
        MailData iMip = receiveIMip(apiClient3, userResponseC2.getData().getEmail1(), summary, 0, SchedulingMethod.REQUEST);

        AnalysisChangeNewEvent newEvent = assertSingleChange(analyze(apiClient3, iMip)).getNewEvent();
        assertNotNull(newEvent);
        assertEquals(createdEvent.getUid(), newEvent.getUid());
        assertAttendeePartStat(newEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.NEEDS_ACTION.status);

        /*
         * reply with "accepted" as party crasher
         */
        EventData eventData = assertSingleEvent(accept(apiClient3, constructBody(iMip), null), createdEvent.getUid());
        assertAttendeePartStat(eventData.getAttendees(), crashersMail, PartStat.ACCEPTED.status);

        /*
         * Receive mail as organizer and check actions for party crasher
         */
        MailData reply = receiveIMip(apiClient, crashersMail, summary, 0, SchedulingMethod.REPLY);
        analyze(reply.getId(), CustomConsumers.PARTY_CRASHER);

        /*
         * Apply change as organizer via iTIP API
         */
        createdEvent = assertSingleEvent(update(constructBody(reply)));
        createdEvent = eventManager.getEvent(createdEvent.getFolder(), createdEvent.getId());
        Optional<Attendee> addedPartyCrasher = createdEvent.getAttendees().stream().filter(a -> a.getEmail().equals(crashersMail)).findFirst();
        assertTrue(addedPartyCrasher.isPresent());
        assertThat("Participant status is not correct.", PartStat.ACCEPTED.status, is(addedPartyCrasher.get().getPartStat()));

        /*
         * Check as original attendee if an update mail from the organizer was sent to inform about a new participant
         */
        MailData updateMail = receiveIMip(apiClientC2, userResponseC1.getData().getEmail1(), summary, 1, SchedulingMethod.REQUEST);
        analyze(analyze(apiClientC2, updateMail), CustomConsumers.ALL);
    }

    private EventData prepareSituation(EventData seriesToCreate, Attendee replyingAttendee, String comment) throws ApiException, Exception {
        createdEvent = eventManager.createEvent(seriesToCreate, true);

        /*
         * Receive mail as attendee
         */
        MailData iMip = receiveIMip(apiClientC2, userResponseC1.getData().getEmail1(), summary, 0, SchedulingMethod.REQUEST);
        AnalysisChangeNewEvent newEvent = assertSingleChange(analyze(apiClientC2, iMip)).getNewEvent();
        assertNotNull(newEvent);
        assertEquals(createdEvent.getUid(), newEvent.getUid());
        assertAttendeePartStat(newEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.NEEDS_ACTION.status);

        /*
         * reply with "accepted"
         */
        EventData attendeeEvent = assertSingleEvent(accept(apiClientC2, constructBody(iMip), comment), createdEvent.getUid());
        assertAttendeePartStat(attendeeEvent.getAttendees(), replyingAttendee.getEmail(), PartStat.ACCEPTED.status);

        /*
         * Receive mail as organizer and check actions
         */
        MailData reply = receiveIMip(apiClient, replyingAttendee.getEmail(), summary, 0, SchedulingMethod.REPLY);
        analyze(reply.getId());

        /*
         * Apply change as organizer via iTIP API
         */
        assertSingleEvent(update(constructBody(reply)), createdEvent.getUid());
        EventResponse eventResponse = chronosApi.getEvent(createdEvent.getId(), createdEvent.getFolder(), createdEvent.getRecurrenceId(), null, null);
        assertNull(eventResponse.getError(), eventResponse.getError());
        createdEvent = eventResponse.getData();
        for (Attendee attendee : createdEvent.getAttendees()) {
            assertThat("Participant status is not correct.", PartStat.ACCEPTED.status, is(attendee.getPartStat()));
        }
        return attendeeEvent;
    }

    private void checkComment(EventData event, Attendee replyingAttendee, String comment) {
        for (Attendee attendee : event.getAttendees()) {
            if (attendee.getEmail().equals(replyingAttendee.getEmail())) {
                assertTrue(null == comment ? Strings.isEmpty(attendee.getComment()) : comment.equalsIgnoreCase(attendee.getComment()));
                return;
            }
        }
        fail("Didn't find replying attendee");
    }

}
