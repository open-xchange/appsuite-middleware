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

import static com.openexchange.ajax.chronos.manager.EventManager.RecurrenceRange.THISANDFUTURE;
import static com.openexchange.ajax.chronos.manager.EventManager.RecurrenceRange.THISANDPRIOR;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.ajax.chronos.factory.AttendeeFactory;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.manager.ChronosApiException;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.CalendarUser;
import com.openexchange.testing.httpclient.models.EventData;

/**
 * {@link ChangeOrganizerTest}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.2
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class ChangeOrganizerTest extends AbstractOrganizerTest {

    private CalendarUser newOrganizer;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        // The new organizer
        newOrganizer = AttendeeFactory.createOrganizerFrom(actingAttendee);
    }

    @Override
    String getEventName() {
        return "ChangeOrganizerTest";
    }

    @Test
    public void testUpdateToInternal() throws Exception {
        // Create event
        event = createEvent();

        // Update to internal
        // Might fail if the original organizer shared the calendar with the new organizer, so both updated events will be returned to the client
        EventData data = eventManager.changeEventOrganizer(event, newOrganizer, null, false);
        assertThat("Organizer did not change", data.getOrganizer().getUri(), is(newOrganizer.getUri()));
    }

    @Test
    public void testUpdateToInternalWithComment() throws Exception {
        // Create event
        event = createEvent();

        // Update to internal and set comment
        EventData data = eventManager.changeEventOrganizer(event, newOrganizer, "Comment4U", false);
        assertThat("Organizer did not change", data.getOrganizer().getUri(), is(newOrganizer.getUri()));
    }

    @Test(expected = ChronosApiException.class)
    public void testUpdateToNone() throws Exception {
        // Create event
        event = createEvent();

        // Update to 'null'
        eventManager.changeEventOrganizer(event, null, null, true);
    }

    @Test(expected = ChronosApiException.class)
    public void testUpdateOnNonGroupScheduled() throws Exception {
        event.setAttendees(null);

        // Create event
        event = createEvent();

        // Update an non group scheduled
        eventManager.changeEventOrganizer(event, newOrganizer, null, true);
    }

    @Test(expected = ChronosApiException.class)
    public void testUpdateToExternal() throws Exception {
        // Create event
        event = createEvent();

        // Update to external
        newOrganizer = AttendeeFactory.createOrganizerFrom(AttendeeFactory.createIndividual("external@example.org"));
        eventManager.changeEventOrganizer(event, newOrganizer, null, true);
    }

    @Test(expected = ChronosApiException.class)
    public void testUpdateWithExternalAttendee() throws Exception {
        event.getAttendees().add(AttendeeFactory.createIndividual("external@example.org"));

        // Create event
        event = createEvent();

        // Update with external attendee
        eventManager.changeEventOrganizer(event, newOrganizer, null, true);
    }

    @Test(expected = ChronosApiException.class)
    public void testUpdateOnSingleOccurrence() throws Exception {
        event.setRrule("FREQ=" + EventFactory.RecurringFrequency.DAILY.name() + ";COUNT=" + 10);

        // Create event
        event = createEvent();

        EventData occurrence = getSecondOccurrence();

        EventData exception = prepareException(occurrence);
        EventData master = eventManager.updateOccurenceEvent(exception, exception.getRecurrenceId(), true);

        assertThat("Too many change exceptions", Integer.valueOf(master.getChangeExceptionDates().size()), is(Integer.valueOf(1)));
        assertThat("Unable to find change exception", (occurrence = getOccurrence(eventManager, master.getChangeExceptionDates().get(0), master.getId())), is(notNullValue()));

        // update on occurrence
        eventManager.changeEventOrganizer(occurrence, newOrganizer, null, occurrence.getRecurrenceId(), THISANDFUTURE, true);
    }

    @Test
    public void testUpdateThisAndFuture() throws Exception {
        event.setRrule("FREQ=" + EventFactory.RecurringFrequency.DAILY.name() + ";COUNT=" + 10);

        // Create event
        event = createEvent();

        EventData occurrence = getSecondOccurrence();
        // THISANDFUTURE
        EventData data = eventManager.changeEventOrganizer(event, newOrganizer, null, occurrence.getRecurrenceId(), THISANDFUTURE, false);
        assertThat("Organizer did not change", data.getOrganizer().getUri(), is(newOrganizer.getUri()));
    }

    @Test(expected = ChronosApiException.class)
    public void testUpdateThisAndPrior() throws Exception {
        event.setRrule("FREQ=" + EventFactory.RecurringFrequency.DAILY.name() + ";COUNT=" + 10);

        // Create event
        event = createEvent();

        // THISANDPRIOR
        eventManager.changeEventOrganizer(event, newOrganizer, null, getSecondOccurrence().getRecurrenceId(), THISANDPRIOR, true);
    }

    @Test(expected = ChronosApiException.class)
    public void testUpdateAsAttendee() throws Exception {
        // Create event
        event = createEvent();

        // Load from users view
        EventData data = eventManager2.getEvent(folderId2, event.getId());

        // Update as attendee
        eventManager2.changeEventOrganizer(data, newOrganizer, null, true);
    }

    @Test
    public void testDeleteOriginalOrganizer() throws Exception {
        // Create event
        event = createEvent();

        // Update to internal
        EventData data = eventManager.changeEventOrganizer(event, newOrganizer, null, false);
        assertThat("Organizer did not change", data.getOrganizer().getUri(), is(newOrganizer.getUri()));

        // Remove original organizer
        ArrayList<Attendee> attendees = new ArrayList<>();
        attendees.add(AttendeeFactory.createIndividual("external@example.org"));
        attendees.add(actingAttendee);

        data = eventManager2.getEvent(folderId2, data.getId());
        data.setAttendees(attendees);
        data.setLastModified(Long.valueOf(System.currentTimeMillis()));
        data = eventManager2.updateEvent(data);

        // Check if original has been removed
        for (Attendee attendee : data.getAttendees()) {
            Assert.assertThat("Old organizer found!", attendee.getUri(), is(not(organizerCU.getUri())));
        }

        // Check if changes as new organizer are possible
        String summary = "New summary: ChangeOrganizerTest";
        data.setSummary(summary);
        data = eventManager2.updateEvent(data);
        Assert.assertThat("Summary can't be changed by new organizer", data.getSummary(), is(summary));
    }

    private EventData createEvent() throws ApiException {
        return eventManager.createEvent(event, true);
    }

}
