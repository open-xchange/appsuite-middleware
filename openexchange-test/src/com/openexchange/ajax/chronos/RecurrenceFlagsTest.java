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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultRecurrenceId;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventData.FlagsEnum;
import com.openexchange.time.TimeTools;

/**
 * {@link RecurrenceFlagsTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.1
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class RecurrenceFlagsTest extends AbstractChronosTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testNonOverriddenOccurrences() throws Exception {
        /*
         * create daily event series lasting 5 days
         */
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");
        EventData event = new EventData();
        event.setSummary("test");
        event.setRrule("FREQ=DAILY;COUNT=5");
        event.setStartDate(DateTimeUtil.getDateTime(timeZone.getID(), TimeTools.D("tomorrow at 3 pm", timeZone).getTime()));
        event.setEndDate(DateTimeUtil.getDateTime(timeZone.getID(), TimeTools.D("tomorrow at 4 pm", timeZone).getTime()));
        event.setUid(UUID.randomUUID().toString());
        EventData createdEvent = eventManager.createEvent(event);
        /*
         * get occurrences from 'all' response
         */
        Date from = CalendarUtils.truncateTime(new Date(), timeZone);
        Date until = CalendarUtils.add(from, Calendar.DATE, 7, timeZone);
        List<EventData> events = eventManager.getAllEvents(from, until, true, createdEvent.getFolder());
        events = getEventsByUid(events, event.getUid());
        assertEquals(5, events.size());
        /*
         * check series-related flags for each occurrence in 'all' response
         */
        for (int i = 0; i < events.size(); i++) {
            List<FlagsEnum> flags = events.get(i).getFlags();
            assertNotNull(flags);
            assertTrue(flags.contains(FlagsEnum.SERIES));
            assertFalse(flags.contains(FlagsEnum.OVERRIDDEN));
            assertEquals(0 == i, flags.contains(FlagsEnum.FIRST_OCCURRENCE));
            assertEquals(4 == i, flags.contains(FlagsEnum.LAST_OCCURRENCE));
        }
        /*
         * check series-related flags for each occurrence in 'get' response
         */
        for (int i = 0; i < events.size(); i++) {
            EventData occurrence = events.get(i);
            occurrence = eventManager.getEvent(occurrence.getFolder(), occurrence.getId(), occurrence.getRecurrenceId(), false);
            List<FlagsEnum> flags = occurrence.getFlags();
            assertNotNull(flags);
            assertTrue(flags.contains(FlagsEnum.SERIES));
            assertFalse(flags.contains(FlagsEnum.OVERRIDDEN));
            assertEquals(0 == i, flags.contains(FlagsEnum.FIRST_OCCURRENCE));
            assertEquals(4 == i, flags.contains(FlagsEnum.LAST_OCCURRENCE));
        }
    }

    @Test
    public void testOverriddenOccurrences() throws Exception {
        /*
         * create daily event series lasting 5 days
         */
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");
        EventData event = new EventData();
        event.setSummary("test");
        event.setRrule("FREQ=DAILY;COUNT=5");
        event.setStartDate(DateTimeUtil.getDateTime(timeZone.getID(), TimeTools.D("tomorrow at 3 pm", timeZone).getTime()));
        event.setEndDate(DateTimeUtil.getDateTime(timeZone.getID(), TimeTools.D("tomorrow at 4 pm", timeZone).getTime()));
        event.setUid(UUID.randomUUID().toString());
        EventData createdEvent = eventManager.createEvent(event);
        /*
         * get occurrences from 'all' response
         */        
        Date from = CalendarUtils.truncateTime(new Date(), timeZone);
        Date until = CalendarUtils.add(from, Calendar.DATE, 7, timeZone);
        List<EventData> events = eventManager.getAllEvents(from, until, true, createdEvent.getFolder());
        events = getEventsByUid(events, event.getUid());
        assertEquals(5, events.size());
        /*
         * create change exceptions for each occurrence
         */
        for (EventData occurrence : events) {
            EventData exception = new EventData();
            exception.setSummary("test_edit");
            exception.setFolder(occurrence.getFolder());
            exception.setId(occurrence.getId());
            exception.setRecurrenceId(occurrence.getRecurrenceId());
            eventManager.updateOccurenceEvent(exception, exception.getRecurrenceId(), true);
        }
        /*
         * get occurrences from 'all' response
         */
        events = eventManager.getAllEvents(from, until, true, createdEvent.getFolder());
        events = getEventsByUid(events, event.getUid());
        assertEquals(5, events.size());
        /*
         * check series-related flags for each occurrence in 'all' response
         */
        for (int i = 0; i < events.size(); i++) {
            List<FlagsEnum> flags = events.get(i).getFlags();
            assertNotNull(flags);
            assertFalse(flags.contains(FlagsEnum.SERIES));
            assertTrue(flags.contains(FlagsEnum.OVERRIDDEN));
            assertEquals(0 == i, flags.contains(FlagsEnum.FIRST_OCCURRENCE));
            assertEquals(4 == i, flags.contains(FlagsEnum.LAST_OCCURRENCE));
        }
        /*
         * check series-related flags for each occurrence in 'get' response
         */
        for (int i = 0; i < events.size(); i++) {
            EventData occurrence = events.get(i);
            occurrence = eventManager.getEvent(occurrence.getFolder(), occurrence.getId(), occurrence.getRecurrenceId(), false);
            List<FlagsEnum> flags = occurrence.getFlags();
            assertNotNull(flags);
            assertFalse(flags.contains(FlagsEnum.SERIES));
            assertTrue(flags.contains(FlagsEnum.OVERRIDDEN));
            assertEquals(0 == i, flags.contains(FlagsEnum.FIRST_OCCURRENCE));
            assertEquals(4 == i, flags.contains(FlagsEnum.LAST_OCCURRENCE));
        }
    }

    private static List<EventData> getEventsByUid(List<EventData> events, String uid) {
        List<EventData> matchingEvents = new ArrayList<EventData>();
        if (null != events) {
            for (EventData event : events) {
                if (uid.equals(event.getUid())) {
                    matchingEvents.add(event);
                }
            }
        }
        matchingEvents.sort(new Comparator<EventData>() {

            @Override
            public int compare(EventData event1, EventData event2) {
                String recurrenceId1 = event1.getRecurrenceId();
                String recurrenceId2 = event2.getRecurrenceId();
                if (null == recurrenceId1) {
                    return null == recurrenceId2 ? 0 : -1;
                }
                if (null == recurrenceId2) {
                    return 1;
                }
                return new DefaultRecurrenceId(recurrenceId1).compareTo(new DefaultRecurrenceId(recurrenceId2));
            }
        });
        return matchingEvents;
    }

}
