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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.chronos.common.CalendarUtils;
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
            assertTrue((0 == i) == flags.contains(FlagsEnum.FIRST_OCCURRENCE));
            assertTrue((4 == i) == flags.contains(FlagsEnum.LAST_OCCURRENCE));
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
            assertTrue((0 == i) == flags.contains(FlagsEnum.FIRST_OCCURRENCE));
            assertTrue((4 == i) == flags.contains(FlagsEnum.LAST_OCCURRENCE));
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
            assertTrue((0 == i) == flags.contains(FlagsEnum.FIRST_OCCURRENCE));
            assertTrue((4 == i) == flags.contains(FlagsEnum.LAST_OCCURRENCE));
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
            assertTrue((0 == i) == flags.contains(FlagsEnum.FIRST_OCCURRENCE));
            assertTrue((4 == i) == flags.contains(FlagsEnum.LAST_OCCURRENCE));
        }
    }

}
