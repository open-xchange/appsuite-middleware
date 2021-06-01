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

package com.openexchange.ajax.chronos;

import static com.openexchange.java.Autoboxing.I;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.ajax.chronos.factory.AttendeeFactory;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.manager.EventManager;
import com.openexchange.ajax.chronos.util.AssertUtil;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.test.common.test.TestClassConfig;
import com.openexchange.test.common.test.pool.TestUser;
import com.openexchange.test.common.tools.client.EnhancedApiClient;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.AttendeeAndAlarm;
import com.openexchange.testing.httpclient.models.CalendarUser;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventsResponse;

/**
 * {@link NeedsActionActionTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.4
 */
public class NeedsActionActionTest extends AbstractExtendedChronosTest {

    protected CalendarUser organizerCU;

    protected Attendee organizerAttendee;
    protected Attendee actingAttendee1;
    protected Attendee actingAttendee2;
    protected Attendee actingAttendee3;
    protected EventData event;
    private String summary;

    private TestUser testUser3;
    private TestUser testUser4;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        
        testUser3 = testContext.acquireUser();
        testUser4 = testContext.acquireUser();

        summary = this.getClass().getSimpleName() + UUID.randomUUID();
        event = EventFactory.createSeriesEvent(testUser.getUserId(), summary, 10, folderId);
        // The internal attendees
        organizerAttendee = createAttendee(I(testUser.getUserId()));
        actingAttendee1 = createAttendee(I(testUser2.getUserId()));
        actingAttendee2 = createAttendee(I(testUser3.getUserId()));
        actingAttendee3 = createAttendee(I(testUser4.getUserId()));

        LinkedList<Attendee> attendees = new LinkedList<>();
        attendees.add(organizerAttendee);
        attendees.add(actingAttendee1);
        attendees.add(actingAttendee2);
        attendees.add(actingAttendee3);
        event.setAttendees(attendees);

        // The original organizer
        organizerCU = AttendeeFactory.createOrganizerFrom(organizerAttendee);
        event.setOrganizer(organizerCU);
        event.setCalendarUser(organizerCU);

        EventData expectedEventData = eventManager.createEvent(event, true);
        event = eventManager.getEvent(folderId, expectedEventData.getId());
        AssertUtil.assertEventsEqual(expectedEventData, event);
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createApiClient().withUserPerContext(4).useEnhancedApiClients().build();
    }

    @Test
    public void testCreateSeriesWithoutExceptions_returnOneNeedsAction() throws Exception {
        Calendar start = getStart();
        ApiClient client3 = testUser3.getApiClient();
        EnhancedApiClient enhancedClient3 = (EnhancedApiClient) client3;
        UserApi userApi3 = new UserApi(client3, enhancedClient3, testUser3);

        Calendar end = getEnd();

        EventsResponse eventsNeedingAction = userApi3.getChronosApi().getEventsNeedingAction(DateTimeUtil.getDateTime(start).getValue(), DateTimeUtil.getDateTime(end).getValue(), null, null, null, null);

        Assert.assertEquals(1, filter(eventsNeedingAction).size());
    }

    @Test
    public void testCreateSeriesWithoutExceptionsAndOneSingleEventsFromDifferentUser_returnTwoNeedsAction() throws Exception {
        Calendar start = getStart();
        createSingleEvent();

        EnhancedApiClient enhancedClient3 = (EnhancedApiClient) testUser3.getApiClient();
        UserApi userApi3 = new UserApi(testUser3.getApiClient(), enhancedClient3, testUser3);

        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(end.getTimeInMillis() + TimeUnit.HOURS.toMillis(2));

        EventsResponse eventsNeedingAction = userApi3.getChronosApi().getEventsNeedingAction(DateTimeUtil.getDateTime(start).getValue(), DateTimeUtil.getDateTime(end).getValue(), null, null, null, null);
        Assert.assertEquals(2, filter(eventsNeedingAction).size());
    }

    @Test
    public void testCreateSeriesWithChangedSummary_returnTwoNeedsAction() throws Exception {
        Calendar start = getStart();
        EventData secondOccurrence = getSecondOccurrence(eventManager, event);
        secondOccurrence.setSummary(event.getSummary() + "The summary changed and that should result in a dedicated action");
        eventManager.updateOccurenceEvent(secondOccurrence, secondOccurrence.getRecurrenceId(), true);

        EnhancedApiClient enhancedClient3 = (EnhancedApiClient) testUser3.getApiClient();
        UserApi userApi3 = new UserApi(testUser3.getApiClient(), enhancedClient3, testUser3);

        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(end.getTimeInMillis() + TimeUnit.DAYS.toMillis(14));

        EventsResponse eventsNeedingAction = userApi3.getChronosApi().getEventsNeedingAction(DateTimeUtil.getDateTime(start).getValue(), DateTimeUtil.getDateTime(end).getValue(), null, null, null, null);

        Assert.assertEquals(2, filter(eventsNeedingAction).size());
    }

    @Test
    public void testCreateSeriesWithOneDeclineOccurrences_returnOneNeedsActionForSeriesOnly() throws Exception {
        Calendar start = getStart();
        AttendeeAndAlarm data = new AttendeeAndAlarm();
        organizerAttendee.setPartStat(ParticipationStatus.DECLINED.toString());
        organizerAttendee.setMember(null);
        data.setAttendee(organizerAttendee);

        EventData secondOccurrence = getSecondOccurrence(eventManager, event);
        eventManager.updateAttendee(secondOccurrence.getId(), secondOccurrence.getRecurrenceId(), folderId, data, false);

        EnhancedApiClient enhancedClient3 = (EnhancedApiClient) testUser3.getApiClient();
        UserApi userApi3 = new UserApi(testUser3.getApiClient(), enhancedClient3, testUser3);

        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(end.getTimeInMillis() + TimeUnit.DAYS.toMillis(14));

        EventsResponse eventsNeedingAction = userApi3.getChronosApi().getEventsNeedingAction(DateTimeUtil.getDateTime(start).getValue(), DateTimeUtil.getDateTime(end).getValue(), null, null, null, null);

        Assert.assertEquals(1, filter(eventsNeedingAction).size());
    }

    @Test
    public void testCreateSeriesWithChangedSummaryAndOneSingleEvent_returnThreeNeedsAction() throws Exception {
        Calendar start = getStart();
        EventData secondOccurrence = getSecondOccurrence(eventManager, event);
        secondOccurrence.setSummary(event.getSummary() + "The summary changed and that should result in a dedicated action");
        eventManager.updateOccurenceEvent(secondOccurrence, secondOccurrence.getRecurrenceId(), true);

        createSingleEvent();

        EnhancedApiClient enhancedClient3 = (EnhancedApiClient) testUser3.getApiClient();
        UserApi userApi3 = new UserApi(testUser3.getApiClient(), enhancedClient3, testUser3);

        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(end.getTimeInMillis() + TimeUnit.DAYS.toMillis(14));

        EventsResponse eventsNeedingAction = userApi3.getChronosApi().getEventsNeedingAction(DateTimeUtil.getDateTime(start).getValue(), DateTimeUtil.getDateTime(end).getValue(), null, null, null, null);

        Assert.assertEquals(3, filter(eventsNeedingAction).size());
    }

    private void createSingleEvent() throws ApiException {
        EventData singleEvent = EventFactory.createSingleTwoHourEvent(testUser.getUserId(), summary);
        LinkedList<Attendee> attendees = new LinkedList<>();
        attendees.add(organizerAttendee);
        attendees.add(actingAttendee1);
        attendees.add(actingAttendee2);
        attendees.add(actingAttendee3);
        singleEvent.setAttendees(attendees);
        // The original organizer
        CalendarUser organizerCU = AttendeeFactory.createOrganizerFrom(organizerAttendee);
        singleEvent.setOrganizer(organizerCU);
        singleEvent.setCalendarUser(organizerCU);
        eventManager.createEvent(singleEvent, true);
    }

    private static EventData getSecondOccurrence(EventManager manager, EventData event) throws ApiException {
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");
        Date from = CalendarUtils.truncateTime(new Date(), timeZone);
        Date until = CalendarUtils.add(from, Calendar.DATE, 7, timeZone);
        List<EventData> occurrences = manager.getAllEvents(event.getFolder(), from, until, true);
        occurrences = occurrences.stream().filter(x -> x.getId().equals(event.getId())).collect(Collectors.toList());

        return occurrences.get(2);
    }

    private Calendar getEnd() {
        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(end.getTimeInMillis() + TimeUnit.HOURS.toMillis(2));
        return end;
    }

    private Calendar getStart() {
        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(start.getTimeInMillis() - TimeUnit.HOURS.toMillis(1));
        return start;
    }

    private List<EventData> filter(EventsResponse eventsNeedingAction) {
        return EventManager.filterEventBySummary(eventsNeedingAction.getData(), summary);
    }

}
