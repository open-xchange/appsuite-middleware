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

package com.openexchange.ajax.chronos;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.ajax.chronos.factory.AttendeeFactory;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.factory.ICalFacotry;
import com.openexchange.ajax.chronos.manager.ChronosApiException;
import com.openexchange.ajax.chronos.manager.EventManager;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.Attendee.CuTypeEnum;
import com.openexchange.testing.httpclient.models.CalendarUser;
import com.openexchange.testing.httpclient.models.ChronosCalendarResultResponse;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventId;
import com.openexchange.testing.httpclient.models.UpdateBody;
import com.openexchange.testing.httpclient.models.UserData;
import com.openexchange.testing.httpclient.models.UserResponse;

/**
 * {@link AttendeePrivilegesTest}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.2
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class AttendeePrivilegesTest extends AbstractChronosTest {

    private CalendarUser calendarUser1;

    private Attendee organizerAttendee;

    private Attendee actingAttendee;

    private EventData event;

    private ApiClient apiClient2;

    private UserApi userApi2;

    private EventManager eventManager2;

    private String folderId2;

    public enum Privileges {
        DEFAULT,
        MODIFY;
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        event = EventFactory.createSingleTwoHourEvent(apiClient.getUserId().intValue(), "AttendeePrivilegesTest");

        setAttendeePrivileges(event);

        // The internal attendees
        organizerAttendee = createAttendee(getClient().getValues().getUserId());
        actingAttendee = createAttendee(getClient2().getValues().getUserId());

        LinkedList<Attendee> attendees = new LinkedList<>();
        attendees.add(organizerAttendee);
        attendees.add(actingAttendee);
        event.setAttendees(attendees);

        // The original organizer
        calendarUser1 = AttendeeFactory.createOrganizerFrom(organizerAttendee);
        event.setOrganizer(calendarUser1);
        event.setCalendarUser(calendarUser1);

        // Setup view of attendee
        apiClient2 = generateApiClient(testUser2);
        rememberClient(apiClient2);
        EnhancedApiClient enhancedClient = generateEnhancedClient(testUser2);
        rememberClient(enhancedClient);
        userApi2 = new UserApi(apiClient2, enhancedClient, testUser2, true);

        folderId2 = getDefaultFolder(userApi2.getSession(), apiClient2);
        eventManager2 = new EventManager(userApi2, folderId2);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        eventManager2.cleanUp();
        super.tearDown();
    }

    @Test
    public void testCorrectAttendeePrivileges() throws Exception {
        // Create event
        event = eventManager.createEvent(event);
        assertThat("Not the correct permission!", event.getAttendeePrivileges(), is(Privileges.MODIFY.name()));
    }

    @Test
    public void testDefaultAttendeePrivileges() throws Exception {
        // Create event
        event.setAttendeePrivileges(null);
        event = eventManager.createEvent(event);
        assertThat("Not the correct permission!", event.getAttendeePrivileges(), anyOf(is(Privileges.DEFAULT.name()), nullValue()));
    }

    @Test
    public void testAddExternalAttendee() throws Exception {
        // Create event
        event = eventManager.createEvent(event);

        addExternalAttendee(event, false);
        
        // Re-check as organizer
        EventData data = eventManager.getEvent(event.getFolder(), event.getId());
        assertThat("Attendee were not added", Integer.valueOf(data.getAttendees().size()), is(Integer.valueOf(3)));
    }

    @Test(expected = ChronosApiException.class)
    public void testUpdateWithExternalOrganizer() throws Exception {
        // Create event
        event.setAttendeePrivileges(null);
        Attendee external = AttendeeFactory.createIndividual("organizer@example.org");
        event.getAttendees().add(external);
        event.setOrganizer(AttendeeFactory.createOrganizerFrom(external));
        event = eventManager.createEvent(event);

        addExternalAttendee(event, true);
    }

    @Test
    public void testDeleteEventAsAttendee() throws Exception {
        // Create event
        event = eventManager.createEvent(event);

        EventId eventId = new EventId();
        eventId.setId(event.getId());
        eventId.setFolder(folderId2);

        // Delete event as attendee
        eventManager2.deleteEvent(eventId);

        // Assert that the event was "deleted" from the attendee point of view, not for other
        EventData data = eventManager.getEvent(event.getFolder(), event.getId());
        assertThat("Should not be null", data, notNullValue());
        assertThat("Attendee should not have been removed", Integer.valueOf(data.getAttendees().size()), is(Integer.valueOf(2)));
        Attendee hiden = data.getAttendees().stream().filter(a -> a.getEntity() == actingAttendee.getEntity()).findFirst().orElse(null);
        assertThat("Attendee is missing!", hiden, notNullValue());
        assertThat("Attendee status should be 'declined' from the organizer view", hiden.getPartStat(), is(ICalFacotry.PartStat.DECLINED.toString()));
    }

    @Test(expected = ChronosApiException.class)
    public void testRemoveOrganizerAsAttendee() throws Exception {
        // Create event
        event = eventManager.createEvent(event);

        EventData eventUpdate = prepareEventUpdate(event);
        eventUpdate.setAttendees(Collections.singletonList(actingAttendee));

        eventManager2.updateEvent(eventUpdate, true, false);
    }

    @Test
    public void testUpdateWithExternalAttendee() throws Exception {
        Attendee external = AttendeeFactory.createIndividual("external@example.org");
        event.getAttendees().add(external);

        // Create event
        event = eventManager.createEvent(event);

        String summary = "AttendeePrivilegesTest: Modify summary";
        EventData eventUpdate = prepareEventUpdate(event);
        eventUpdate.setSummary(summary);
        eventUpdate.setAttendees(event.getAttendees());
        EventData data = eventManager2.updateEvent(eventUpdate, false, false);
        assertThat("Summary should have changed", data.getSummary(), is(summary));
    }

    @Test
    public void testDeleteSingleOccurrence() throws Exception {
        event.setRrule("FREQ=" + EventFactory.RecurringFrequency.DAILY.name() + ";COUNT=" + 10);

        // Create event
        event = eventManager.createEvent(event);

        EventData occurrence = getOccurrence();

        EventData exception = prepareException(occurrence);
        EventData master = eventManager.updateOccurenceEvent(exception, exception.getRecurrenceId(), true);

        assertThat("Too many change exceptions", Integer.valueOf(master.getChangeExceptionDates().size()), is(Integer.valueOf(1)));
        assertThat("Unable to find change exception", (occurrence = getOccurrence(eventManager, master.getChangeExceptionDates().get(0), master.getId())), is(notNullValue()));

        EventId eventId = new EventId();
        eventId.setId(occurrence.getId());
        eventId.setFolder(folderId2);

        eventManager2.deleteEvent(eventId);

        // Assert that the event was "deleted" from the attendees point of view
        EventData data = eventManager.getEvent(event.getFolder(), occurrence.getId());
        assertThat("Should not be null", data, notNullValue());
        assertThat("Attendee should not have been removed", Integer.valueOf(data.getAttendees().size()), is(Integer.valueOf(2)));
        Attendee hiden = data.getAttendees().stream().filter(a -> a.getEntity() == actingAttendee.getEntity()).findFirst().orElse(null);
        assertThat("Attendee is missing!", hiden, notNullValue());
        assertThat("Attendee status should be 'declined' from the organizer view", hiden.getPartStat(), is(ICalFacotry.PartStat.DECLINED.toString()));
    }

    @Test
    public void testUpdateOnSingleOccurrence() throws Exception {
        event.setRrule("FREQ=" + EventFactory.RecurringFrequency.DAILY.name() + ";COUNT=" + 10);

        // Create event
        event = eventManager.createEvent(event);

        EventData occurrence = getOccurrence();

        EventData exception = prepareException(occurrence);
        EventData master = eventManager.updateOccurenceEvent(exception, exception.getRecurrenceId(), true);

        assertThat("Too many change exceptions", Integer.valueOf(master.getChangeExceptionDates().size()), is(Integer.valueOf(1)));
        assertThat("Unable to find change exception", (occurrence = getOccurrence(eventManager, master.getChangeExceptionDates().get(0), master.getId())), is(notNullValue()));

        // update on occurrence
        exception = getOccurrence(eventManager2, exception.getRecurrenceId(), master.getSeriesId());
        exception = eventManager2.getEvent(folderId2, exception.getId());
        addExternalAttendee(exception, false);
    }

    @Test
    public void testUpdateThisAndFutureAsAttendee() throws Exception {
        event.setRrule("FREQ=" + EventFactory.RecurringFrequency.DAILY.name() + ";COUNT=" + 10);

        // Create event
        event = eventManager.createEvent(event);

        EventData occurrence = getOccurrence(eventManager2);
        occurrence = eventManager2.getEvent(null, occurrence.getId(), occurrence.getRecurrenceId(), false);

        // Update as attendee
        EventData exception = prepareException(occurrence);
        occurrence.getAttendees().add(AttendeeFactory.createIndividual("external@example.org"));
        EventData master = eventManager2.updateOccurenceEvent(exception, exception.getRecurrenceId(), EventManager.RecurrenceRange.THISANDFUTURE, false, true);
        master = eventManager2.getEvent(null, master.getId());
        assertThat("Start date", Integer.valueOf(master.getAttendees().size()), is(Integer.valueOf(3)));

    }

    @Test(expected = ChronosApiException.class)
    public void testSetPropertyAsAttendee() throws Exception {
        // Create event
        event.setExtendedProperties(null);
        event = eventManager.createEvent(event);

        // Set extended properties and update as an attendee
        EventData data = eventManager2.getEvent(folderId2, event.getId());
        EventData eventUpdate = prepareEventUpdate(data);
        setAttendeePrivileges(eventUpdate);
        eventManager2.updateEvent(eventUpdate, true, false);
    }

    @Test
    public void testPrivilegesAsOrganizer() throws Exception {
        // Create event
        event.setExtendedProperties(null);
        event = eventManager.createEvent(event);

        // Set extended properties and update as an attendee
        EventData data = eventManager.getEvent(null, event.getId());
        EventData eventUpdate = prepareEventUpdate(data);
        setAttendeePrivileges(eventUpdate);
        eventUpdate.setAttendees(event.getAttendees());
        data = eventManager.updateEvent(eventUpdate, false, false);

        assertThat("Attendee privileges are not correct", data.getAttendeePrivileges(), is(Privileges.MODIFY.name()));

        data = eventManager2.getEvent(null, event.getId());
        assertThat("Attendee privileges are not correct", data.getAttendeePrivileges(), is(Privileges.MODIFY.name()));
    }

    @Test
    public void testChangeOrganizerAsAttendee() throws Exception {
        // Create event
        event = eventManager.createEvent(event);

        // Get data
        EventData data = eventManager2.getEvent(null, event.getId());
        EventData eventUpdate = prepareEventUpdate(data);

        // Set organizer as attendee
        CalendarUser newOrganizer = AttendeeFactory.createOrganizerFrom(actingAttendee);
        eventUpdate = eventManager2.changeEventOrganizer(eventUpdate, newOrganizer, null, false);

        assertThat("Organizer not set!", eventUpdate.getOrganizer(), notNullValue());
        assertThat("Organizer should have changes", eventUpdate.getOrganizer().getUri(), is(newOrganizer.getUri()));
    }

    @Test
    public void testRemoveAttendeeViaUpdate() throws Exception {
        // Create event
        event = eventManager.createEvent(event);

        // Get data
        EventData data = eventManager2.getEvent(null, event.getId());
        EventData eventUpdate = prepareEventUpdate(data);

        eventUpdate.setAttendees(Collections.singletonList(organizerAttendee));

        EventData updateEvent = eventManager2.updateEvent(eventUpdate, false, false);
        assertThat("Acting attendee should not be removed", Integer.valueOf(updateEvent.getAttendees().size()), is(Integer.valueOf(2)));
    }

    @Test
    public void testUpdateMasterAndExceptions() throws Exception {
        event.setAttendeePrivileges(null);
        event.setRrule("FREQ=" + EventFactory.RecurringFrequency.DAILY.name() + ";COUNT=" + 10);

        // Create event
        event = eventManager.createEvent(event);

        EventData occurrence = getOccurrence(eventManager);
        occurrence = eventManager.getEvent(null, occurrence.getId(), occurrence.getRecurrenceId(), false);

        // Update as attendee
        EventData exception = prepareException(occurrence);
        exception.getAttendees().add(AttendeeFactory.createIndividual("external@example.org"));
        EventData master = eventManager.updateOccurenceEvent(exception, exception.getRecurrenceId(), false, true);
        master = eventManager.getEvent(null, master.getId());

        EventData masterUpdate = prepareEventUpdate(event);
        setAttendeePrivileges(masterUpdate);
        masterUpdate.setChangeExceptionDates(master.getChangeExceptionDates());

        UpdateBody body = new UpdateBody();
        body.setEvent(masterUpdate);
        ChronosCalendarResultResponse updateResponse = defaultUserApi.getChronosApi().updateEvent(defaultUserApi.getSession(), defaultFolderId, masterUpdate.getId(), body, masterUpdate.getLastModified(), null, null, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, null, null, null, Boolean.FALSE);
        assertThat(Integer.valueOf(updateResponse.getData().getUpdated().size()), is(Integer.valueOf(2)));
        
        master = updateResponse.getData().getUpdated().stream().filter(e -> e.getId().equals(e.getSeriesId())).findAny().orElse(null);
        assertThat("\"Modify\" privilege should have been set", master.getAttendeePrivileges(), is(Privileges.MODIFY.name()));

        occurrence = updateResponse.getData().getUpdated().stream().filter(e -> false == e.getId().equals(e.getSeriesId())).findAny().orElse(null);
        assertThat("Exception should have new privilege", occurrence.getAttendeePrivileges(), is(Privileges.MODIFY.name()));
    }

    // ----------------------------- HELPER -----------------------------

    protected Attendee createAttendee(int userId) throws ApiException {
        Attendee attendee = AttendeeFactory.createAttendee(userId, CuTypeEnum.INDIVIDUAL);

        UserData userData = getUserInformation(userId);

        attendee.cn(userData.getDisplayName());
        attendee.email(userData.getEmail1());
        attendee.setUri("mailto:" + userData.getEmail1());
        attendee.entity(Integer.valueOf(userData.getId()));
        return attendee;
    }

    private UserData getUserInformation(int userId) throws ApiException {
        com.openexchange.testing.httpclient.modules.UserApi api = new com.openexchange.testing.httpclient.modules.UserApi(getApiClient());
        UserResponse userResponse = api.getUser(getApiClient().getSession(), String.valueOf(userId));
        return userResponse.getData();
    }

    private EventData getOccurrence() throws ApiException {
        return getOccurrence(eventManager);
    }

    private EventData getOccurrence(EventManager manager) throws ApiException {
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");
        Date from = CalendarUtils.truncateTime(new Date(), timeZone);
        Date until = CalendarUtils.add(from, Calendar.DATE, 7, timeZone);
        List<EventData> occurrences = manager.getAllEvents(null, from, until, true);
        occurrences = occurrences.stream().filter(x -> x.getId().equals(event.getId())).collect(Collectors.toList());

        return occurrences.get(2);
    }

    private EventData getOccurrence(EventManager manager, String recurrecneId, String seriesId) throws ApiException {
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");
        Date from = CalendarUtils.truncateTime(new Date(), timeZone);
        Date until = CalendarUtils.add(from, Calendar.DATE, 7, timeZone);
        List<EventData> occurrences = manager.getAllEvents(null, from, until, true);
        return occurrences.stream().filter(x -> seriesId.equals(x.getSeriesId()) && recurrecneId.equals(x.getRecurrenceId())).findFirst().orElse(null);
    }

    private EventData prepareException(EventData occurrence) {
        EventData exception = prepareEventUpdate(occurrence);
        exception.setSummary("AttendeePrivilegesTest: Changed summary");
        exception.setRecurrenceId(occurrence.getRecurrenceId());
        exception.setAttendees(occurrence.getAttendees());
        return exception;
    }

    private void addExternalAttendee(EventData eventData, boolean expectException) throws ApiException, ChronosApiException {
        ArrayList<Attendee> attendees = new ArrayList<>(eventData.getAttendees());
        attendees.add(AttendeeFactory.createIndividual("external@example.org"));

        EventData data = new EventData();
        data.setId(eventData.getId());
        data.setFolder(folderId2);
        data.setAttendees(attendees);
        data.setLastModified(Long.valueOf(System.currentTimeMillis()));
        data = eventManager2.updateEvent(data, expectException, false);
        assertThat("Attendees were not updated", Integer.valueOf(data.getAttendees().size()), is(Integer.valueOf(3)));
    }

    private void setAttendeePrivileges(EventData data) {
        data.setAttendeePrivileges(Privileges.MODIFY.name());
    }

    private EventData prepareEventUpdate(EventData data) {
        EventData eventUpdate = new EventData();
        eventUpdate.setId(data.getId());
        eventUpdate.setLastModified(Long.valueOf(System.currentTimeMillis()));
        return eventUpdate;
    }

}
