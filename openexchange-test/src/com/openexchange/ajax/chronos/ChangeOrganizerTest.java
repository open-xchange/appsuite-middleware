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

import static com.openexchange.ajax.chronos.manager.EventManager.RecurrenceRange.THISANDFUTURE;
import static com.openexchange.ajax.chronos.manager.EventManager.RecurrenceRange.THISANDPRIOR;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import java.util.Calendar;
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
import com.openexchange.ajax.chronos.manager.ChronosApiException;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.Attendee.CuTypeEnum;
import com.openexchange.testing.httpclient.models.CalendarUser;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventId;
import com.openexchange.testing.httpclient.models.UserData;
import com.openexchange.testing.httpclient.models.UserResponse;
import com.openexchange.testing.httpclient.modules.UserApi;

/**
 * {@link ChangeOrganizerTest}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.2
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class ChangeOrganizerTest extends AbstractChronosTest {

    private CalendarUser originalOrganizer;

    private CalendarUser newOrganizer;

    private EventData event;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        event = EventFactory.createSingleTwoHourEvent(apiClient.getUserId().intValue(), "ChangeOrganizerTest");

        // The internal attendees
        Attendee organizer = createAttendee(getClient().getValues().getUserId());
        Attendee attendee = createAttendee(getClient2().getValues().getUserId());

        LinkedList<Attendee> attendees = new LinkedList<>();
        attendees.add(organizer);
        attendees.add(attendee);
        event.setAttendees(attendees);

        // The original organizer
        originalOrganizer = AttendeeFactory.createOrganizerFrom(organizer);
        event.setOrganizer(originalOrganizer);
        event.setCalendarUser(originalOrganizer);

        // The new organizer
        newOrganizer = AttendeeFactory.createOrganizerFrom(attendee);

    }

    @Override
    @After
    public void tearDown() throws Exception {
        if (null != event) {
            EventId id = new EventId();
            id.setId(event.getId());
            id.setFolder(null != event.getFolder() ? event.getFolder() : defaultFolderId);
            eventManager.deleteEvent(id);
        }
        super.tearDown();
    }

    @Test
    public void testUpdateToInternal() throws Exception {
        // Create event
        event = eventManager.createEvent(event);

        // Update to internal
        EventData data = eventManager.updateEventOrganizer(event, newOrganizer, null, null, null, false);
        assertThat("Organizer did not change", data.getOrganizer().getUri(), is(newOrganizer.getUri()));
    }

    @Test
    public void testUpdateToInternalWithComment() throws Exception {
        // Create event
        event = eventManager.createEvent(event);

        // Update to internal and set comment
        EventData data = eventManager.updateEventOrganizer(event, newOrganizer, "Comment4U", null, null, false);
        assertThat("Organizer did not change", data.getOrganizer().getUri(), is(newOrganizer.getUri()));
    }

    @Test(expected = ChronosApiException.class)
    public void testUpdateOnNonGroupScheduled() throws Exception {
        event.setAttendees(null);

        // Create event
        event = eventManager.createEvent(event);

        // Update an non group scheduled
        eventManager.updateEventOrganizer(event, newOrganizer, null, null, null, true);
    }

    @Test(expected = ChronosApiException.class)
    public void testUpdateToExternal() throws Exception {
        // Create event
        event = eventManager.createEvent(event);

        // Update to external
        newOrganizer = AttendeeFactory.createOrganizerFrom(AttendeeFactory.createIndividual("external@example.org"));
        eventManager.updateEventOrganizer(event, newOrganizer, null, null, null, true);
    }

    @Test(expected = ChronosApiException.class)
    public void testUpdateWithExternalAttendee() throws Exception {
        event.getAttendees().add(AttendeeFactory.createIndividual("external@example.org"));

        // Create event
        event = eventManager.createEvent(event);

        // Update with external attendee
        eventManager.updateEventOrganizer(event, newOrganizer, null, null, null, true);
    }

    @Test(expected = ChronosApiException.class)
    public void testUpdateOnSingleOccurrence() throws Exception {
        event.setRrule("FREQ=" + EventFactory.RecurringFrequency.DAILY.name() + ";COUNT=" + 10);

        // Create event
        event = eventManager.createEvent(event);

        EventData occurrence = getOccurrence();

        EventData exception = prepareException(occurrence);
        EventData master = eventManager.updateOccurenceEvent(exception, exception.getRecurrenceId(), true);

        assertThat("Too many change exceptions", Integer.valueOf(master.getChangeExceptionDates().size()), is(Integer.valueOf(1)));
        assertThat("Unable to find change exception", (occurrence = getOccurrence(master.getChangeExceptionDates().get(0), master.getId())), is(notNullValue()));

        // update on occurrence
        eventManager.updateEventOrganizer(occurrence, newOrganizer, null, occurrence.getRecurrenceId(), THISANDFUTURE, true);
    }

    @Test
    public void testUpdateThisAndFuture() throws Exception {
        event.setRrule("FREQ=" + EventFactory.RecurringFrequency.DAILY.name() + ";COUNT=" + 10);

        // Create event
        event = eventManager.createEvent(event);

        EventData occurrence = getOccurrence();
        // THISANDFUTURE
        EventData data = eventManager.updateEventOrganizer(event, newOrganizer, null, occurrence.getRecurrenceId(), THISANDFUTURE, false);
        assertThat("Organizer did not change", data.getOrganizer().getUri(), is(newOrganizer.getUri()));
    }

    @Test(expected = ChronosApiException.class)
    public void testUpdateThisAndPrior() throws Exception {
        event.setRrule("FREQ=" + EventFactory.RecurringFrequency.DAILY.name() + ";COUNT=" + 10);

        // Create event
        event = eventManager.createEvent(event);

        // THISANDPRIOR
        eventManager.updateEventOrganizer(event, newOrganizer, null, getOccurrence().getRecurrenceId(), THISANDPRIOR, true);
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
        UserApi api = new UserApi(getApiClient());
        UserResponse userResponse = api.getUser(getApiClient().getSession(), String.valueOf(userId));
        return userResponse.getData();
    }

    private EventData getOccurrence() throws ApiException {
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");
        Date from = CalendarUtils.truncateTime(new Date(), timeZone);
        Date until = CalendarUtils.add(from, Calendar.DATE, 7, timeZone);
        List<EventData> occurrences = eventManager.getAllEvents(event.getFolder(), from, until, true);
        occurrences = occurrences.stream().filter(x -> x.getId().equals(event.getId())).collect(Collectors.toList());

        return occurrences.get(2);
    }

    private EventData getOccurrence(String recurrecneId, String seriesId) throws ApiException {
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");
        Date from = CalendarUtils.truncateTime(new Date(), timeZone);
        Date until = CalendarUtils.add(from, Calendar.DATE, 7, timeZone);
        List<EventData> occurrences = eventManager.getAllEvents(event.getFolder(), from, until, true);
        return occurrences.stream().filter(x -> x.getSeriesId().equals(seriesId) && x.getRecurrenceId().equals(recurrecneId)).findFirst().orElse(null);
    }

    private EventData prepareException(EventData occurrence) {
        EventData exception = new EventData();
        exception.setSummary("NewSummaryChangeOrganizerTest");
        exception.setFolder(occurrence.getFolder());
        exception.setId(occurrence.getId());
        exception.setRecurrenceId(occurrence.getRecurrenceId());
        exception.setAttendees(occurrence.getAttendees());
        return exception;
    }

}
