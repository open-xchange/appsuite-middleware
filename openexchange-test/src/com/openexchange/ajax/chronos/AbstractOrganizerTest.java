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
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;
import org.junit.Before;
import com.openexchange.ajax.chronos.factory.AttendeeFactory;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.manager.EventManager;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.test.common.test.TestClassConfig;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.CalendarUser;
import com.openexchange.testing.httpclient.models.EventData;

/**
 * {@link AbstractOrganizerTest}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.2
 */
public abstract class AbstractOrganizerTest extends AbstractExtendedChronosTest {

    protected CalendarUser organizerCU;

    protected Attendee organizerAttendee;

    protected Attendee actingAttendee;

    protected EventData event;

    /**
     * Initializes a new {@link AbstractOrganizerTest}.
     */
    public AbstractOrganizerTest() {
        super();
    }

    @Override
    protected String getScope() {
        return "context";
    }

    @Override
    protected Map<String, String> getNeededConfigurations() {
        return Collections.singletonMap("com.openexchange.calendar.allowChangeOfOrganizer", Boolean.TRUE.toString());
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        setUpConfiguration();

        event = EventFactory.createSingleTwoHourEvent(testUser.getUserId(), getEventName());

        // The internal attendees
        organizerAttendee = createAttendee(I(testUser.getUserId()));
        actingAttendee = createAttendee(I(testUser2.getUserId()));

        LinkedList<Attendee> attendees = new LinkedList<>();
        attendees.add(organizerAttendee);
        attendees.add(actingAttendee);
        event.setAttendees(attendees);

        // The original organizer
        organizerCU = AttendeeFactory.createOrganizerFrom(organizerAttendee);
        event.setOrganizer(organizerCU);
        event.setCalendarUser(organizerCU);
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createApiClient().withUserPerContext(2).useEnhancedApiClients().build();
    }

    /**
     * The name for the events summary
     *
     * @return The name
     */
    abstract String getEventName();

    // ----------------------------- HELPER -----------------------------

    protected EventData getSecondOccurrence() throws ApiException {
        return getSecondOccurrence(eventManager);
    }

    protected EventData getSecondOccurrence(EventManager manager) throws ApiException {
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");
        Date from = CalendarUtils.truncateTime(new Date(), timeZone);
        Date until = CalendarUtils.add(from, Calendar.DATE, 7, timeZone);
        List<EventData> occurrences = manager.getAllEvents(null, from, until, true);
        occurrences = occurrences.stream().filter(x -> x.getId().equals(event.getId())).collect(Collectors.toList());

        return occurrences.get(2);
    }

    protected EventData getOccurrence(EventManager manager, String recurrecneId, String seriesId) throws ApiException {
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");
        Date from = CalendarUtils.truncateTime(new Date(), timeZone);
        Date until = CalendarUtils.add(from, Calendar.DATE, 7, timeZone);
        List<EventData> occurrences = manager.getAllEvents(null, from, until, true);
        return occurrences.stream().filter(x -> seriesId.equals(x.getSeriesId()) && recurrecneId.equals(x.getRecurrenceId())).findFirst().orElse(null);
    }
}
