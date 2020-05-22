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

package com.openexchange.ajax.chronos;

import static com.openexchange.java.Autoboxing.I;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.ajax.chronos.factory.AttendeeFactory;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.manager.EventManager;
import com.openexchange.ajax.chronos.util.AssertUtil;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.test.pool.TestUser;
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

    protected TestUser testUser3;
    protected TestUser testUser4;
    private AJAXClient client3;
    private AJAXClient client4;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        testUser3 = testContext.acquireUser();
        testUser4 = testContext.acquireUser();
        client3 = generateClient(testUser3);
        client4 = generateClient(testUser4);

        event = EventFactory.createSeriesEvent(apiClient.getUserId().intValue(), System.currentTimeMillis() + "", 10, folderId);
        // The internal attendees
        organizerAttendee = createAttendee(I(getClient().getValues().getUserId()));
        actingAttendee1 = createAttendee(I(getClient2().getValues().getUserId()));
        actingAttendee2 = createAttendee(I(client3.getValues().getUserId()));
        actingAttendee3 = createAttendee(I(client4.getValues().getUserId()));

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
    public void tearDown() throws Exception {
        try {
            testContext.backUser(testUser3);
            testContext.backUser(testUser4);
            client3 = logoutClient(client3, true);
            client4 = logoutClient(client4, true);
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testCreateSeriesWithoutExceptions_returnOneNeedsAction() throws Exception {
        Calendar start = getStart();
        ApiClient client3 = generateApiClient(testUser3);
        rememberClient(client3);
        EnhancedApiClient enhancedClient3 = generateEnhancedClient(testUser3);
        rememberClient(enhancedClient3);
        UserApi userApi3 = new UserApi(client3, enhancedClient3, testUser3, false);

        Calendar end = getEnd();

        EventsResponse eventsNeedingAction = userApi3.getChronosApi().getEventsNeedingAction(client3.getSession(), DateTimeUtil.getDateTime(start).getValue(), DateTimeUtil.getDateTime(end).getValue(), null, null, null, null);

        Assert.assertEquals(1, eventsNeedingAction.getData().size());
    }

    @Test
    public void testCreateSeriesWithoutExceptionsAndOneSingleEventsFromDifferentUser_returnTwoNeedsAction() throws Exception {
        Calendar start = getStart();
        createSingleEvent();

        ApiClient client3 = generateApiClient(testUser3);
        rememberClient(client3);
        EnhancedApiClient enhancedClient3 = generateEnhancedClient(testUser3);
        rememberClient(enhancedClient3);
        UserApi userApi3 = new UserApi(client3, enhancedClient3, testUser3, false);

        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(end.getTimeInMillis() + TimeUnit.HOURS.toMillis(2));

        EventsResponse eventsNeedingAction = userApi3.getChronosApi().getEventsNeedingAction(client3.getSession(), DateTimeUtil.getDateTime(start).getValue(), DateTimeUtil.getDateTime(end).getValue(), null, null, null, null);

        Assert.assertEquals(2, eventsNeedingAction.getData().size());
    }

    @Test
    public void testCreateSeriesWithChangedSummary_returnTwoNeedsAction() throws Exception {
        Calendar start = getStart();
        EventData secondOccurrence = getSecondOccurrence(eventManager, event);
        secondOccurrence.setSummary("The summary changed and that should result in a dedicated action");
        eventManager.updateOccurenceEvent(secondOccurrence, secondOccurrence.getRecurrenceId(), true);

        ApiClient client3 = generateApiClient(testUser3);
        rememberClient(client3);
        EnhancedApiClient enhancedClient3 = generateEnhancedClient(testUser3);
        rememberClient(enhancedClient3);
        UserApi userApi3 = new UserApi(client3, enhancedClient3, testUser3, false);

        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(end.getTimeInMillis() + TimeUnit.DAYS.toMillis(14));

        EventsResponse eventsNeedingAction = userApi3.getChronosApi().getEventsNeedingAction(client3.getSession(), DateTimeUtil.getDateTime(start).getValue(), DateTimeUtil.getDateTime(end).getValue(), null, null, null, null);

        Assert.assertEquals(2, eventsNeedingAction.getData().size());
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

        ApiClient client3 = generateApiClient(testUser3);
        rememberClient(client3);
        EnhancedApiClient enhancedClient3 = generateEnhancedClient(testUser3);
        rememberClient(enhancedClient3);
        UserApi userApi3 = new UserApi(client3, enhancedClient3, testUser3, false);

        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(end.getTimeInMillis() + TimeUnit.DAYS.toMillis(14));

        EventsResponse eventsNeedingAction = userApi3.getChronosApi().getEventsNeedingAction(client3.getSession(), DateTimeUtil.getDateTime(start).getValue(), DateTimeUtil.getDateTime(end).getValue(), null, null, null, null);

        Assert.assertEquals(1, eventsNeedingAction.getData().size());
    }

    @Test
    public void testCreateSeriesWithChangedSummaryAndOneSingleEvent_returnThreeNeedsAction() throws Exception {
        Calendar start = getStart();
        EventData secondOccurrence = getSecondOccurrence(eventManager, event);
        secondOccurrence.setSummary("The summary changed and that should result in a dedicated action");
        eventManager.updateOccurenceEvent(secondOccurrence, secondOccurrence.getRecurrenceId(), true);

        createSingleEvent();

        ApiClient client3 = generateApiClient(testUser3);
        rememberClient(client3);
        EnhancedApiClient enhancedClient3 = generateEnhancedClient(testUser3);
        rememberClient(enhancedClient3);
        UserApi userApi3 = new UserApi(client3, enhancedClient3, testUser3, false);

        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(end.getTimeInMillis() + TimeUnit.DAYS.toMillis(14));

        EventsResponse eventsNeedingAction = userApi3.getChronosApi().getEventsNeedingAction(client3.getSession(), DateTimeUtil.getDateTime(start).getValue(), DateTimeUtil.getDateTime(end).getValue(), null, null, null, null);

        Assert.assertEquals(3, eventsNeedingAction.getData().size());
    }

    private void createSingleEvent() throws ApiException {
        EventData singleEvent = EventFactory.createSingleTwoHourEvent(apiClient.getUserId().intValue(), System.currentTimeMillis() + "");
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

}
