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

package com.openexchange.chronos.impl.performer;

import static com.openexchange.chronos.impl.performer.UpdatesPerformer.getResult;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.impl.AbstractCombineTest;
import com.openexchange.chronos.service.UpdatesResult;

/**
 * {@link UpdatesPerformerTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class UpdatesPerformerTest extends AbstractCombineTest {

    private List<Event> deletedEvents;
    private List<Event> newAndModifiedEvents;

    @Before
    public void setup() {
        deletedEvents = new ArrayList<Event>();
        for (int i = 1; i <= 100; i++) {
            deletedEvents.add(event(i, 30, UUID.randomUUID().toString(), i));
        }
        newAndModifiedEvents = new ArrayList<Event>();
        for (int i = 1; i <= 100; i++) {
            newAndModifiedEvents.add(event(i, 30, UUID.randomUUID().toString(), i));
        }
    }

    @Test
    public void testLimitedResults1() {
        assertUpdatesResult(getResult(newAndModifiedEvents, deletedEvents, 0), 100, 100, 100);
    }

    @Test
    public void testLimitedResults2() {
        assertUpdatesResult(getResult(newAndModifiedEvents, null, 0), 100, -1, 100);
    }

    @Test
    public void testLimitedResults3() {
        assertUpdatesResult(getResult(null, deletedEvents, 0), -1, 100, 100);
    }

    @Test
    public void testLimitedResults4() {
        assertUpdatesResult(getResult(null, null, 0), -1, -1, 0);
    }

    @Test
    public void testLimitedResults5() {
        assertUpdatesResult(getResult(newAndModifiedEvents, deletedEvents, 200), 100, 100, 100);
    }

    @Test
    public void testLimitedResults6() {
        assertUpdatesResult(getResult(newAndModifiedEvents, deletedEvents, 100), 99, 99, 99);
    }

    @Test
    public void testLimitedResults7() {
        assertUpdatesResult(getResult(newAndModifiedEvents, deletedEvents, 25), 25, 25, 25);
    }

    @Test
    public void testLimitedResults8() {
        assertUpdatesResult(getResult(newAndModifiedEvents, deletedEvents, 1), 1, 1, 1);
    }

    @Test
    public void testLimitedResults9() {
        assertUpdatesResult(getResult(newAndModifiedEvents, deletedEvents, 400), 100, 100, 100);
    }

    @Test
    public void testLimitedResults10() {
        List<Event> newAndModifiedEvents = new ArrayList<Event>();
        newAndModifiedEvents.add(event(1, 30, UUID.randomUUID().toString(), 9L));
        newAndModifiedEvents.add(event(2, 30, UUID.randomUUID().toString(), 12L));
        newAndModifiedEvents.add(event(3, 30, UUID.randomUUID().toString(), 16L));
        newAndModifiedEvents.add(event(4, 30, UUID.randomUUID().toString(), 19L));
        List<Event> deletedEvents = new ArrayList<Event>();
        deletedEvents.add(event(5, 30, UUID.randomUUID().toString(), 9L));
        deletedEvents.add(event(6, 30, UUID.randomUUID().toString(), 12L));
        deletedEvents.add(event(7, 30, UUID.randomUUID().toString(), 16L));
        deletedEvents.add(event(8, 30, UUID.randomUUID().toString(), 19L));
        assertUpdatesResult(getResult(newAndModifiedEvents, deletedEvents, 3), 3, 3, 16L);
    }

    @Test
    public void testLimitedResults11() {
        List<Event> newAndModifiedEvents = new ArrayList<Event>();
        newAndModifiedEvents.add(event(1, 30, UUID.randomUUID().toString(), 9L));
        newAndModifiedEvents.add(event(2, 30, UUID.randomUUID().toString(), 12L));
        newAndModifiedEvents.add(event(3, 30, UUID.randomUUID().toString(), 19L));
        newAndModifiedEvents.add(event(4, 30, UUID.randomUUID().toString(), 19L));
        List<Event> deletedEvents = new ArrayList<Event>();
        deletedEvents.add(event(5, 30, UUID.randomUUID().toString(), 9L));
        deletedEvents.add(event(6, 30, UUID.randomUUID().toString(), 12L));
        deletedEvents.add(event(7, 30, UUID.randomUUID().toString(), 16L));
        deletedEvents.add(event(8, 30, UUID.randomUUID().toString(), 19L));
        assertUpdatesResult(getResult(newAndModifiedEvents, deletedEvents, 3), 2, 2, 12L);
    }

    @Test
    public void testLimitedResults12() {
        List<Event> newAndModifiedEvents = new ArrayList<Event>();
        newAndModifiedEvents.add(event(1, 30, UUID.randomUUID().toString(), 9L));
        newAndModifiedEvents.add(event(2, 30, UUID.randomUUID().toString(), 12L));
        newAndModifiedEvents.add(event(3, 30, UUID.randomUUID().toString(), 16L));
        List<Event> deletedEvents = new ArrayList<Event>();
        deletedEvents.add(event(5, 30, UUID.randomUUID().toString(), 9L));
        deletedEvents.add(event(6, 30, UUID.randomUUID().toString(), 9L));
        deletedEvents.add(event(7, 30, UUID.randomUUID().toString(), 9L));
        deletedEvents.add(event(8, 30, UUID.randomUUID().toString(), 19L));
        assertUpdatesResult(getResult(newAndModifiedEvents, deletedEvents, 3), 2, 3, 12L);
    }

    @Test
    public void testTruncateList1() {
        List<Event> events = new ArrayList<Event>();
        events.add(event(1, 30, UUID.randomUUID().toString(), 9L));
        events.add(event(1, 30, UUID.randomUUID().toString(), 12L));
        events.add(event(1, 30, UUID.randomUUID().toString(), 16L));
        assertTrue(UpdatesPerformer.truncateEvents(events, 3));
        assertEquals(2, events.size());
        assertEquals(12L, events.get(1).getTimestamp());
    }

    @Test
    public void testTruncateList2() {
        List<Event> events = new ArrayList<Event>();
        events.add(event(1, 30, UUID.randomUUID().toString(), 9L));
        events.add(event(1, 30, UUID.randomUUID().toString(), 12L));
        events.add(event(1, 30, UUID.randomUUID().toString(), 12L));
        assertTrue(UpdatesPerformer.truncateEvents(events, 3));
        assertEquals(1, events.size());
        assertEquals(9L, events.get(0).getTimestamp());
    }

    @Test
    public void testTruncateList3() {
        assertFalse(UpdatesPerformer.truncateEvents(null, 3));
    }

    @Test
    public void testTruncateList4() {
        assertFalse(UpdatesPerformer.truncateEvents(new ArrayList<Event>(), 3));
    }

    @Test
    public void testTruncateList5() {
        List<Event> events = new ArrayList<Event>();
        events.add(event(1, 30, UUID.randomUUID().toString(), 9L));
        events.add(event(1, 30, UUID.randomUUID().toString(), 12L));
        events.add(event(1, 30, UUID.randomUUID().toString(), 12L));
        assertTrue(UpdatesPerformer.truncateEvents(events, 2));
        assertEquals(1, events.size());
        assertEquals(9L, events.get(0).getTimestamp());
    }

    @Test
    public void testTruncateList6() {
        List<Event> events = new ArrayList<Event>();
        events.add(event(1, 30, UUID.randomUUID().toString(), 9L));
        events.add(event(1, 30, UUID.randomUUID().toString(), 12L));
        events.add(event(1, 30, UUID.randomUUID().toString(), 12L));
        assertFalse(UpdatesPerformer.truncateEvents(events, 4));
        assertEquals(3, events.size());
        assertEquals(12L, events.get(2).getTimestamp());
    }

    private void assertUpdatesResult(UpdatesResult result, int expectedNewAndModifiedSize, int expectedDeletedSize, long expectedTimestamp) {
        assertNotNull(result);
        if (-1 == expectedNewAndModifiedSize) {
            assertNull(result.getNewAndModifiedEvents());
        } else {
            assertNotNull(result.getNewAndModifiedEvents());
            assertEquals(expectedNewAndModifiedSize, result.getNewAndModifiedEvents().size());
        }
        if (-1 == expectedDeletedSize) {
            assertNull(result.getDeletedEvents());
        } else {
            assertNotNull(result.getDeletedEvents());
            assertEquals(expectedDeletedSize, result.getDeletedEvents().size());
        }
        assertEquals(expectedTimestamp, result.getTimestamp());
    }

    private static Event event(int id, int folderId, String uid, long timestamp) {
        Event event = new Event();
        event.setId(String.valueOf(id));
        event.setFolderId(String.valueOf(folderId));
        event.setTimestamp(timestamp);
        event.setUid(uid);
        return event;
    }

}
