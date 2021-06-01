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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.manager.ChronosApiException;
import com.openexchange.ajax.chronos.util.AssertUtil;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.testing.httpclient.models.DateTimeData;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventId;
import com.openexchange.testing.httpclient.models.UpdatesResult;

/**
 *
 * {@link BasicSeriesEventTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class BasicSeriesEventTest extends AbstractChronosTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Tests the creation of a series event
     */
    @Test
    public void testCreateSeries() throws Exception {
        EventData toCreate = EventFactory.createSeriesEvent(defaultUserApi.getCalUser().intValue(), "testCreateSeries", 3, folderId);
        EventData expectedEventData = eventManager.createEvent(toCreate, true);
        EventData actualEventData = eventManager.getEvent(folderId, expectedEventData.getId());
        AssertUtil.assertEventsEqual(expectedEventData, actualEventData);
    }

    /**
     * Tests the complete deletion of a series event
     */
    @Test
    public void testDeleteCompleteSeries() throws Exception {
        EventData toCreate = EventFactory.createSeriesEvent(defaultUserApi.getCalUser().intValue(), "testDeleteCompleteSeries", 3, folderId);
        EventData expectedEventData = eventManager.createEvent(toCreate, true);

        EventId eventId = new EventId();
        eventId.setId(expectedEventData.getId());
        eventId.setFolder(expectedEventData.getFolder());
        eventManager.deleteEvent(eventId);

        try {
            eventManager.getEvent(folderId, expectedEventData.getId(), true);
            fail("Series exists. Expected 'CAL-4040' error");
        } catch (ChronosApiException e) {
            assertEquals("CAL-4040", e.getErrorCode());
        }
    }

    /**
     * Tests the deletion of an occurence from a series
     */
    @Test
    public void testDeleteSeriesOccurence() throws Exception {
        Calendar instance = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        instance.setTimeInMillis(System.currentTimeMillis());
        instance.add(Calendar.DAY_OF_MONTH, -1);
        Date from = instance.getTime();
        instance.add(Calendar.DAY_OF_MONTH, 7);
        Date until = instance.getTime();
        instance.add(Calendar.DAY_OF_MONTH, -7);

        EventData toCreate = EventFactory.createSeriesEvent(defaultUserApi.getCalUser().intValue(), "testDeleteSeriesOccurence", 3, folderId);
        EventData expectedEventData = eventManager.createEvent(toCreate, true);

        EventId eventId = new EventId();
        eventId.setId(expectedEventData.getId());
        eventId.setFolder(expectedEventData.getFolder());

        List<EventData> allEvents = eventManager.getAllEvents(folderId, from, until, true);
        allEvents = getEventsByUid(allEvents, expectedEventData.getUid());
        assertEquals("Expected 3 occurrences", 3, allEvents.size());
        for (int x = 0; x < allEvents.size(); x++) {
            assertEquals(expectedEventData.getId(), allEvents.get(x).getId());
        }

        EventId occurence = new EventId();
        occurence.setId(eventId.getId());
        occurence.setFolder(expectedEventData.getFolder());
        occurence.setRecurrenceId(allEvents.get(2).getRecurrenceId());

        eventManager.deleteEvent(occurence);

        try {
            eventManager.getRecurringEvent(folderId, occurence.getId(), occurence.getRecurrenceId(), true);
            fail("Series exists. Expected 'CAL-4042' error");
        } catch (ChronosApiException e) {
            assertEquals("CAL-4042", e.getErrorCode());
        }

        // Get updates
        UpdatesResult updates = eventManager.getUpdates(from, true, folderId);
        assertEquals(2, updates.getNewAndModified().size());
        for (int x = 0; x < updates.getNewAndModified().size(); x++) {
            assertEquals(expectedEventData.getId(), updates.getNewAndModified().get(x).getId());
        }
    }

    /**
     * Tests the update of a the master event of a series
     */
    @Test
    public void testUpdateSeriesMaster() throws Exception {
        EventData toCreate = EventFactory.createSeriesEvent(defaultUserApi.getCalUser().intValue(), "testUpdateSeriesMaster", 3, folderId);
        EventData expectedEventData = eventManager.createEvent(toCreate, true);

        EventId eventId = new EventId();
        eventId.setId(expectedEventData.getId());
        eventId.setFolder(expectedEventData.getFolder());

        expectedEventData.setEndDate(DateTimeUtil.incrementDateTimeData(expectedEventData.getEndDate(), 5000));

        // Update
        EventData updatedEvent = eventManager.updateEvent(expectedEventData);

        assertNotEquals("The timestamp matches", expectedEventData.getLastModified(), updatedEvent.getLastModified());
        assertNotEquals("The sequence matches", expectedEventData.getSequence(), updatedEvent.getSequence());

        expectedEventData.setLastModified(updatedEvent.getLastModified());
        expectedEventData.setSequence(updatedEvent.getSequence());
        AssertUtil.assertEventsEqual(expectedEventData, updatedEvent);

    }

    /**
     * Tests the update of an occurence with in a series
     */
    @Test
    public void testUpdateSeriesOccurence() throws Exception {
        Calendar instance = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        instance.setTimeInMillis(System.currentTimeMillis());
        instance.add(Calendar.DAY_OF_MONTH, -1);
        Date from = instance.getTime();
        instance.add(Calendar.DAY_OF_MONTH, 7);
        Date until = instance.getTime();
        instance.add(Calendar.DAY_OF_MONTH, -7);

        EventData toCreate = EventFactory.createSeriesEvent(defaultUserApi.getCalUser().intValue(), "testUpdateSeriesOccurence", 3, folderId);
        EventData expectedEventData = eventManager.createEvent(toCreate, true);

        EventId masterId = new EventId();
        masterId.setId(expectedEventData.getId());
        masterId.setFolder(expectedEventData.getFolder());

        List<EventData> allEvents = eventManager.getAllEvents(folderId, from, until, true);
        allEvents = getEventsByUid(allEvents, expectedEventData.getUid()); // Filter by series uid
        assertEquals("Expected 3 occurrences", 3, allEvents.size());
        for (int x = 0; x < allEvents.size(); x++) {
            assertEquals(expectedEventData.getId(), allEvents.get(x).getId());
        }

        EventId occurence = new EventId();
        occurence.setId(masterId.getId());
        occurence.setFolder(folderId);
        occurence.setRecurrenceId(allEvents.get(2).getRecurrenceId());

        EventData updatedData = new EventData();
        updatedData.setEndDate(DateTimeUtil.incrementDateTimeData(allEvents.get(2).getEndDate(), 5000));
        updatedData.setId(occurence.getId());
        updatedData.setFolder(folderId);

        eventManager.updateOccurenceEvent(updatedData, occurence.getRecurrenceId(), true);

        UpdatesResult updates = eventManager.getUpdates(from, false, folderId);

        // Get updates
        assertEquals(2, updates.getNewAndModified().size());
        for (EventData newOrModifiedEvent : updates.getNewAndModified()) {
            if (newOrModifiedEvent.getId().equals(masterId.getId())) {
                continue;
            }
            assertNotEquals(masterId.getId(), newOrModifiedEvent.getId());
            assertEquals(occurence.getRecurrenceId(), newOrModifiedEvent.getRecurrenceId());
            assertEquals(updatedData.getEndDate(), newOrModifiedEvent.getEndDate());
        }

    }

    /**
     * Tests getting the series
     */
    @Test
    public void testGetSeries() throws Exception {
        Calendar instance = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        instance.setTimeInMillis(System.currentTimeMillis());
        instance.add(Calendar.DAY_OF_MONTH, -1);
        Date from = instance.getTime();
        instance.add(Calendar.DAY_OF_MONTH, 7);
        Date until = instance.getTime();
        instance.add(Calendar.DAY_OF_MONTH, -7);

        // Create a series event
        EventData toCreate = EventFactory.createSeriesEvent(defaultUserApi.getCalUser().intValue(), "testGetSeries", 3, folderId);
        EventData expectedEventData = eventManager.createEvent(toCreate, true);

        // Get series master
        EventData actualEventData = eventManager.getEvent(folderId, expectedEventData.getId());
        AssertUtil.assertEventsEqual(expectedEventData, actualEventData);

        // Get all events
        List<EventData> allEvents = eventManager.getAllEvents(folderId, from, until, true);
        allEvents = getEventsByUid(allEvents, expectedEventData.getUid());
        assertEquals("Expected 3 occurrences", 3, allEvents.size());
        for (int x = 0; x < allEvents.size(); x++) {
            assertEquals(expectedEventData.getId(), allEvents.get(x).getId());
        }

        // Get series occurrence
        EventData recurringEvent = eventManager.getRecurringEvent(folderId, expectedEventData.getId(), allEvents.get(2).getRecurrenceId(), false);
        assertEquals(expectedEventData.getId(), recurringEvent.getId());
        assertNotEquals(expectedEventData.getRecurrenceId(), recurringEvent.getRecurrenceId());
        DateTimeData expectedRecurrenceId = DateTimeUtil.getDateTime(actualEventData.getStartDate().getTzid(), DateTimeUtil.parseDateTime(expectedEventData.getStartDate()).getTime() + TimeUnit.DAYS.toMillis(2));
        assertEquals(expectedRecurrenceId.getTzid() + ":" + expectedRecurrenceId.getValue(), recurringEvent.getRecurrenceId());

        // Get updates
        UpdatesResult updates = eventManager.getUpdates(from, true, folderId);
        assertEquals(3, updates.getNewAndModified().size());
        for (int x = 0; x < updates.getNewAndModified().size(); x++) {
            assertEquals(expectedEventData.getId(), updates.getNewAndModified().get(x).getId());
        }

    }

    /**
     * Tests floating series events
     */
    @Test
    public void testFloatingSeries() throws Exception {
        Calendar instance = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        instance.setTimeInMillis(System.currentTimeMillis());
        instance.add(Calendar.DAY_OF_MONTH, -1);
        Date from = instance.getTime();
        instance.add(Calendar.DAY_OF_MONTH, 7);
        Date until = instance.getTime();
        instance.add(Calendar.DAY_OF_MONTH, -7);

        // Create a series event
        EventData toCreate = EventFactory.createSeriesEvent(defaultUserApi.getCalUser().intValue(), "testFloatingSeries", 3, folderId);
        instance.add(Calendar.DAY_OF_MONTH, 1);
        toCreate.setStartDate(DateTimeUtil.getDateTime(null, instance.getTimeInMillis()));
        toCreate.setEndDate(DateTimeUtil.getDateTime(null, instance.getTimeInMillis() + 5000));
        EventData expectedEventData = eventManager.createEvent(toCreate, true);

        // Get series master
        EventData actualEventData = eventManager.getEvent(folderId, expectedEventData.getId());
        AssertUtil.assertEventsEqual(expectedEventData, actualEventData);

        // Get all events
        List<EventData> allEvents = eventManager.getAllEvents(folderId, from, until, true);
        assertEquals("Expected 3 occurrences", 3, allEvents.size());
        for (int x = 0; x < allEvents.size(); x++) {
            assertEquals(expectedEventData.getId(), allEvents.get(x).getId());
        }

        // Get series occurrence
        EventData recurringEvent = eventManager.getRecurringEvent(folderId, expectedEventData.getId(), allEvents.get(2).getRecurrenceId(), false);
        assertEquals(expectedEventData.getId(), recurringEvent.getId());
        assertNotEquals(expectedEventData.getRecurrenceId(), recurringEvent.getRecurrenceId());
        instance.add(Calendar.DAY_OF_MONTH, 2);
        DateTimeData expectedRecurrenceId = DateTimeUtil.getDateTime("UTC", instance.getTimeInMillis());
        assertEquals(expectedRecurrenceId.getValue(), recurringEvent.getStartDate().getValue());
        assertEquals(expectedRecurrenceId.getValue(), recurringEvent.getRecurrenceId());

        // Get updates
        UpdatesResult updates = eventManager.getUpdates(from, true, folderId);
        assertEquals(3, updates.getNewAndModified().size());
        for (int x = 0; x < updates.getNewAndModified().size(); x++) {
            assertEquals(expectedEventData.getId(), updates.getNewAndModified().get(x).getId());
        }
    }
}
