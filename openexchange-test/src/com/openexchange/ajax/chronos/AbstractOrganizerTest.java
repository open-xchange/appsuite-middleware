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

        event = EventFactory.createSingleTwoHourEvent(apiClient.getUserId().intValue(), getEventName());

        // The internal attendees
        organizerAttendee = createAttendee(getClient().getValues().getUserId());
        actingAttendee = createAttendee(getClient2().getValues().getUserId());

        LinkedList<Attendee> attendees = new LinkedList<>();
        attendees.add(organizerAttendee);
        attendees.add(actingAttendee);
        event.setAttendees(attendees);

        // The original organizer
        organizerCU = AttendeeFactory.createOrganizerFrom(organizerAttendee);
        event.setOrganizer(organizerCU);
        event.setCalendarUser(organizerCU);
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
