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

import static com.openexchange.ajax.chronos.itip.ITipUtil.convertToAttendee;
import static com.openexchange.java.Autoboxing.I;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Consumer;
import com.openexchange.ajax.chronos.factory.ConferenceBuilder;
import com.openexchange.ajax.chronos.manager.EventManager;
import com.openexchange.java.Strings;
import com.openexchange.junit.Assert;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.Analysis;
import com.openexchange.testing.httpclient.models.Analysis.ActionsEnum;
import com.openexchange.testing.httpclient.models.AnalyzeResponse;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.CalendarUser;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.MailDestinationData;

/**
 * {@link AbstractITipAnalyzeTest}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public abstract class AbstractITipAnalyzeTest extends AbstractITipTest {

    protected MailDestinationData mailData;

    protected EventData createdEvent;

    protected Attendee prepareCommonAttendees(EventData event) {
        Attendee replyingAttendee = convertToAttendee(testUserC2, Integer.valueOf(0));
        replyingAttendee.setPartStat(PartStat.NEEDS_ACTION.getStatus());
        Attendee organizingAttendee = convertToAttendee(testUser, I(testUser.getUserId()));
        organizingAttendee.setPartStat(PartStat.ACCEPTED.getStatus());
        ArrayList<Attendee> attendees = new ArrayList<>(5);
        attendees.add(organizingAttendee);
        attendees.add(replyingAttendee);
        event.setAttendees(attendees);
        CalendarUser c = new CalendarUser();
        c.cn(userResponseC1.getData().getDisplayName());
        c.email(userResponseC1.getData().getEmail1());
        c.entity(Integer.valueOf(userResponseC1.getData().getId()));
        event.setOrganizer(c);
        event.setCalendarUser(c);

        return replyingAttendee;
    }

    protected Attendee prepareAttendees(EventData event) {
        Attendee replyingAttendee = convertToAttendee(testUserC2, apiClientC2.getUserId());
        replyingAttendee.setEntity(Integer.valueOf(0));
        replyingAttendee.setPartStat(PartStat.NEEDS_ACTION.getStatus());

        Attendee organizer = convertToAttendee(testUser, I(testUser.getUserId()));
        organizer.setPartStat(PartStat.ACCEPTED.getStatus());

        List<Attendee> attendees = new ArrayList<Attendee>(2);
        attendees.add(organizer);
        attendees.add(replyingAttendee);

        event.setAttendees(attendees);
        CalendarUser c = new CalendarUser();
        c.cn(userResponseC1.getData().getDisplayName());
        c.email(userResponseC1.getData().getEmail1());
        c.entity(Integer.valueOf(userResponseC1.getData().getId()));
        event.setOrganizer(c);
        event.setCalendarUser(c);

        return replyingAttendee;
    }

    /**
     * Prepares a new event with necessary attributes set.
     *
     * @param eventData The event do base an delta event on
     * @return An new event
     */
    protected EventData prepareDeltaEvent(EventData eventData) {
        EventData delta = new EventData();
        delta.setId(eventData.getId());
        if (Strings.isNotEmpty(eventData.getFolder())) {
            delta.setFolder(eventData.getFolder());
        }
        if (null != eventData.getRecurrenceId()) {
            delta.setRecurrenceId(eventData.getRecurrenceId());
        }
        if (null != eventData.getAttendees()) {
            delta.setAttendees(new ArrayList<>(eventData.getAttendees()));
        }
        if (null != eventData.getConferences()) {
            delta.setConferences(eventData.getConferences());
        }
        return delta;
    }

    protected EventData prepareAttendeeConference(EventData eventData) {
        ConferenceBuilder builder = ConferenceBuilder.newBuilder() //@formatter:off
            .setDefaultFeatures()
            .setAttendeeLable()
            .setVideoChatUri()
            .setGroupId();
        eventData.addConferencesItem(builder.build()); //@formatter:on
        return eventData;
    }

    protected EventData prepareModeratorConference(EventData eventData) {
        ConferenceBuilder builder = ConferenceBuilder.newBuilder() //@formatter:off
            .setDefaultFeatures()
            .setModeratorLable()
            .setVideoChatUri(); 
        eventData.addConferencesItem(builder.build()); //@formatter:on
        return eventData;
    }

    /**
     * Get all events of of a series
     * <p>
     * Note: The {@link #createdEvent} must be a series
     *
     * @return All events of the series
     * @throws ApiException
     */
    protected List<EventData> getAllEventsOfCreatedEvent() throws ApiException {
        return getAllEventsOfEvent(eventManager, createdEvent);
    }

    /**
     * Get all events of of a series
     * <p>
     * Note: The {@link #createdEvent} must be a series
     *
     * @return All events of the series
     * @throws ApiException
     */
    protected List<EventData> getAllEventsOfEvent(EventManager eventManager, EventData event) throws ApiException {
        Calendar instance = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        instance.setTimeInMillis(System.currentTimeMillis());
        instance.add(Calendar.DAY_OF_MONTH, -1);
        Date from = instance.getTime();
        instance.add(Calendar.DAY_OF_MONTH, 7);
        Date until = instance.getTime();
        List<EventData> allEvents = eventManager.getAllEvents(null, from, until, true);
        allEvents = getEventsByUid(allEvents, event.getUid()); // Filter by series uid
        return allEvents;
    }

    protected void analyze(String mailId) throws Exception {
        analyze(mailId, CustomConsumers.UPDATE.getConsumer());
    }

    protected void analyze(String mailId, CustomConsumers consumer) throws Exception {
        analyze(mailId, consumer.getConsumer());
    }

    protected void analyze(String mailId, Consumer<Analysis> analysisValidator) throws Exception {
        AnalyzeResponse response = analyze(ITipUtil.constructBody(mailId));
        Assert.assertThat("Should have no error", response.getErrorId(), nullValue());

        analyze(response, analysisValidator);
    }

    protected void analyze(AnalyzeResponse response, CustomConsumers consumer) {
        analyze(response, consumer.getConsumer());
    }

    protected void analyze(AnalyzeResponse response, Consumer<Analysis> analysisValidator) {
        List<Analysis> data = response.getData();
        Assert.assertThat("No Analyze", data, notNullValue());
        Assert.assertThat("Only one event should have been analyzed", Integer.valueOf(data.size()), is(Integer.valueOf(1)));

        if (null == analysisValidator) {
            CustomConsumers.ACTIONS.getConsumer().accept(data.get(0));
        } else {
            analysisValidator.accept(data.get(0));
        }
    }

    public enum CustomConsumers {

        /** Validates that the response does contain all actions */
        ALL((Analysis t) -> {
            assertTrue("Missing action!", t.getActions().contains(ActionsEnum.ACCEPT) || t.getActions().contains(ActionsEnum.ACCEPT_AND_IGNORE_CONFLICTS));
            assertTrue("Missing action!", t.getActions().contains(ActionsEnum.TENTATIVE));
            assertTrue("Missing action!", t.getActions().contains(ActionsEnum.DECLINE));
            assertTrue("Missing action!", t.getActions().contains(ActionsEnum.UPDATE));
        }),
        /** Validates that the response does contain actions to set the users status */
        ACTIONS((Analysis t) -> {
            assertTrue("Missing action!", t.getActions().contains(ActionsEnum.ACCEPT) || t.getActions().contains(ActionsEnum.ACCEPT_AND_IGNORE_CONFLICTS));
            assertTrue("Missing action!", t.getActions().contains(ActionsEnum.TENTATIVE));
            assertTrue("Missing action!", t.getActions().contains(ActionsEnum.DECLINE));

            assertFalse("Unwanted action!", t.getActions().contains(ActionsEnum.UPDATE));
        }),
        /** Validates that the response does contain the UPDATE action */
        UPDATE((Analysis t) -> {
            assertFalse("Unwanted action!", t.getActions().contains(ActionsEnum.ACCEPT) || t.getActions().contains(ActionsEnum.ACCEPT_AND_IGNORE_CONFLICTS));
            assertFalse("Unwanted action!", t.getActions().contains(ActionsEnum.TENTATIVE));
            assertFalse("Unwanted action!", t.getActions().contains(ActionsEnum.DECLINE));

            assertTrue("Missing action!", t.getActions().contains(ActionsEnum.UPDATE));
        }),
        /** Validates that the response doesn't contain any action */
        EMPTY((Analysis t) -> {
            assertTrue("There should be no action, but was " + t.getActions(), t.getActions().isEmpty());
        }),
        /** Validates that the response does contain the party crasher action */
        PARTY_CRASHER((Analysis t) -> {
            assertTrue("There should be onyl one action! But was " + t.getActions(), t.getActions().size() == 1);
            assertTrue("Unwanted action!", t.getActions().contains(ActionsEnum.ACCEPT_PARTY_CRASHER));
        }),
        /** Validates that the response does contain the cancel action */
        CANCEL((Analysis t) -> {
            assertTrue("There should be only one action! But was " + t.getActions(), t.getActions().size() == 1);
            assertTrue("Unwanted action!", t.getActions().contains(ActionsEnum.DELETE));
        }),
        /** Validates that the response does contain the cancel action */
        IGNORE((Analysis t) -> {
            assertTrue("There should be only one action! But was " + t.getActions(), t.getActions().size() == 1);
            assertTrue("Unwanted action!", t.getActions().contains(ActionsEnum.IGNORE));
        }),
        ;

        private final Consumer<Analysis> consumer;

        CustomConsumers(Consumer<Analysis> consumer) {
            this.consumer = consumer;
        }

        public Consumer<Analysis> getConsumer() {
            return consumer;
        }

    }
}
