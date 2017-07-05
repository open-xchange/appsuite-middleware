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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.Attendee.CuTypeEnum;
import com.openexchange.testing.httpclient.models.ChronosUpdateResponse;
import com.openexchange.testing.httpclient.models.ChronosUpdatesResponse;
import com.openexchange.testing.httpclient.models.CommonResponse;
import com.openexchange.testing.httpclient.models.EventConflictResponse;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventData.TranspEnum;
import com.openexchange.testing.httpclient.models.EventId;
import com.openexchange.testing.httpclient.models.EventResponse;
import com.openexchange.testing.httpclient.models.EventsResponse;
import edu.emory.mathcs.backport.java.util.Collections;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

/**
 *
 * {@link BasicSeriesEventTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class BasicSeriesEventTest extends AbstractChronosTest {

    private String folderId;

    @SuppressWarnings("unchecked")
    private EventData createSeriesEvent(String summary) {
        EventData seriesEvent = new EventData();
        seriesEvent.setPropertyClass("PUBLIC");
        Attendee attendee = new Attendee();
        attendee.entity(calUser);
        attendee.cuType(CuTypeEnum.INDIVIDUAL);
        attendee.setUri("mailto:" + this.testUser.getLogin());
        seriesEvent.setAttendees(Collections.singletonList(attendee));
        seriesEvent.setStartDate(System.currentTimeMillis());
        seriesEvent.setEndDate(System.currentTimeMillis() + 5000);
        seriesEvent.setTransp(TranspEnum.OPAQUE);
        seriesEvent.setAllDay(false);
        seriesEvent.setRrule("FREQ=DAILY;COUNT=3");
        seriesEvent.setSummary(summary);
        return seriesEvent;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        folderId = getDefaultFolder();
    }

    @Test
    public void testCreateSeries() throws Exception {
        EventConflictResponse createEvent = api.createEvent(session, folderId, createSeriesEvent("testCreateSeries"), false);
        assertNull(createEvent.getError(), createEvent.getError());
        assertNotNull(createEvent.getData());
        EventId eventId = new EventId();
        eventId.setId(createEvent.getData().getId());
        rememberEventId(eventId);
        EventResponse eventResponse = api.getEvent(session, createEvent.getData().getId(), null, null, null);
        assertNull(eventResponse.getError(), createEvent.getError());
        assertNotNull(eventResponse.getData());
        EventUtil.compare(createEvent.getData(), eventResponse.getData(), true);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeleteCompleteSeries() throws Exception {

        EventConflictResponse createEvent = api.createEvent(session, folderId, createSeriesEvent("testDeleteCompleteSeries"), false);
        assertNull(createEvent.getError(), createEvent.getError());
        assertNotNull(createEvent.getData());
        EventId eventId = new EventId();
        eventId.setId(createEvent.getData().getId());

        CommonResponse deleteResponse = api.deleteEvent(session, System.currentTimeMillis(), Collections.singletonList(eventId));
        assertNull(deleteResponse.getError());

        EventResponse eventResponse = api.getEvent(session, createEvent.getData().getId(), null, null, null);
        assertNotNull(eventResponse.getError());
        assertEquals("CAL-4040", eventResponse.getCode());

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeleteSeriesOccurence() throws Exception {

        Date date = new Date();
        Date today = getAPIDate(TimeZone.getDefault(), date, 0);
        Date nextWeek = getAPIDate(TimeZone.getDefault(), date, 7);

        EventConflictResponse createEvent = api.createEvent(session, folderId, createSeriesEvent("testDeleteSeriesOccurence"), false);
        assertNull(createEvent.getError(), createEvent.getError());
        assertNotNull(createEvent.getData());
        EventId eventId = new EventId();
        eventId.setId(createEvent.getData().getId());
        rememberEventId(eventId);

        EventsResponse eventsResponse = api.getAllEvents(session, folderId, today.getTime(), nextWeek.getTime(), null, null, null, null, true, true);
        assertNull(eventsResponse.getError(), eventsResponse.getError());
        assertNotNull(eventsResponse.getData());
        assertEquals(3, eventsResponse.getData().size());
        for (int x = 0; x < eventsResponse.getData().size(); x++) {
            assertEquals(createEvent.getData().getId(), eventsResponse.getData().get(x).getId());
        }


        EventId occurence = new EventId();
        occurence.setId(eventId.getId());
        occurence.setRecurrenceId(eventsResponse.getData().get(2).getRecurrenceId());

        CommonResponse deleteResponse = api.deleteEvent(session, System.currentTimeMillis(), Collections.singletonList(occurence));
        assertNull(deleteResponse.getError());

        EventResponse eventResponse = api.getEvent(session, occurence.getId(), occurence.getRecurrenceId(), null, null);
        assertNotNull(eventResponse.getError());
        assertEquals("CAL-4042", eventResponse.getCode());

        // Get updates
        ChronosUpdatesResponse updatesResponse = api.getUpdates(session, folderId, date.getTime(), null, null, null, null, null, null, true, true);
        assertNull(updatesResponse.getError(), updatesResponse.getErrorDesc());
        assertNotNull(updatesResponse.getData());
        assertEquals(2, updatesResponse.getData().getNewAndModified().size());
        for (int x = 0; x < updatesResponse.getData().getNewAndModified().size(); x++) {
            assertEquals(createEvent.getData().getId(), updatesResponse.getData().getNewAndModified().get(x).getId());
        }
    }

    @Test
    public void testUpdateSeriesMaster() throws Exception {
        EventData initialEvent = createSeriesEvent("testUpdateSeriesMaster");
        EventConflictResponse createEvent = api.createEvent(session, folderId, initialEvent, false);
        assertNull(createEvent.getError(), createEvent.getError());
        assertNotNull(createEvent.getData());
        EventId eventId = new EventId();
        eventId.setId(createEvent.getData().getId());
        rememberEventId(eventId);

        EventData updatedData = createEvent.getData();
        updatedData.setEndDate(updatedData.getEndDate()+5000);

        ChronosUpdateResponse updateResponse = api.updateEvent(session, folderId, eventId.getId(), updatedData, System.currentTimeMillis(), true, null);
        assertNull(updateResponse.getError());
        assertNotNull(updateResponse.getData());

        List<EventData> updates = updateResponse.getData().getUpdated();
        assertTrue(updates.size()==1);
        EventUtil.compare(initialEvent, updates.get(0), false);
        updatedData.setLastModified(updates.get(0).getLastModified());
        updatedData.setSequence(updates.get(0).getSequence());
        EventUtil.compare(updatedData, updates.get(0), true);

    }

    @Test
    public void testUpdateSeriesOccurence() throws Exception {
        Date date = new Date();
        Date today = getAPIDate(TimeZone.getDefault(), date, 0);
        Date nextWeek = getAPIDate(TimeZone.getDefault(), date, 7);

        EventData initialEvent = createSeriesEvent("testUpdateSeriesOccurence");
        EventConflictResponse createEvent = api.createEvent(session, folderId, initialEvent, false);
        assertNull(createEvent.getError(), createEvent.getError());
        assertNotNull(createEvent.getData());
        EventId masterId = new EventId();
        masterId.setId(createEvent.getData().getId());
        rememberEventId(masterId);

        EventsResponse eventsResponse = api.getAllEvents(session, folderId, today.getTime(), nextWeek.getTime(), null, null, null, null, true, true);
        assertNull(eventsResponse.getError(), eventsResponse.getError());
        assertNotNull(eventsResponse.getData());
        assertEquals(3, eventsResponse.getData().size());
        for (int x = 0; x < eventsResponse.getData().size(); x++) {
            assertEquals(createEvent.getData().getId(), eventsResponse.getData().get(x).getId());
        }

        EventId occurence = new EventId();
        occurence.setId(masterId.getId());
        occurence.setRecurrenceId(eventsResponse.getData().get(2).getRecurrenceId());

        EventData updatedData = new EventData();
        long newEndDate = eventsResponse.getData().get(2).getEndDate()+5000;
        updatedData.setEndDate(newEndDate);

        ChronosUpdateResponse updateResponse = api.updateEvent(session, folderId, occurence.getId(), updatedData,  eventsResponse.getTimestamp(), true, occurence.getRecurrenceId());
        assertNull(updateResponse.getError());
        assertNotNull(updateResponse.getData());

        List<EventData> updates = updateResponse.getData().getUpdated();
        assertTrue(updates.size()==1);

        // Get updates
        ChronosUpdatesResponse updatesResponse = api.getUpdates(session, folderId, eventsResponse.getTimestamp(), null, null, null, null, null, null, false, true);
        assertNull(updatesResponse.getError(), updatesResponse.getErrorDesc());
        assertNotNull(updatesResponse.getData());
        assertEquals(2, updatesResponse.getData().getNewAndModified().size());
        for(EventData event : updatesResponse.getData().getNewAndModified()){
            if(event.getId().equals(masterId.getId())){
                continue;
            }
            assertNotEquals(masterId.getId(), event.getId());
            assertEquals(occurence.getRecurrenceId(), event.getRecurrenceId());
            assertEquals(updatedData.getEndDate(), event.getEndDate());
        }

    }


    @Test
    public void testGetSeries() throws Exception {
        Date date = new Date();
        Date today = getAPIDate(TimeZone.getDefault(), date, 0);
        Date nextWeek = getAPIDate(TimeZone.getDefault(), date, 7);

        // Create a series event
        EventConflictResponse createEvent = api.createEvent(session, folderId, createSeriesEvent("testGetSeries"), false);
        assertNull(createEvent.getError(), createEvent.getError());
        assertNotNull(createEvent.getData());
        EventId eventId = new EventId();
        eventId.setId(createEvent.getData().getId());
        rememberEventId(eventId);

        // Get series master
        EventResponse eventResponse = api.getEvent(session, createEvent.getData().getId(), null, null, null);
        assertNull(eventResponse.getError(), eventResponse.getError());
        assertNotNull(eventResponse.getData());
        EventUtil.compare(createEvent.getData(), eventResponse.getData(), true);

        // Get all events
        EventsResponse eventsResponse = api.getAllEvents(session, folderId, today.getTime(), nextWeek.getTime(), null, null, null, null, true, true);
        assertNull(eventsResponse.getError(), eventsResponse.getError());
        assertNotNull(eventsResponse.getData());
        assertEquals(3, eventsResponse.getData().size());
        for (int x = 0; x < eventsResponse.getData().size(); x++) {
            assertEquals(createEvent.getData().getId(), eventsResponse.getData().get(x).getId());
        }

        // Get series occurence
        eventResponse = api.getEvent(session, createEvent.getData().getId(), eventsResponse.getData().get(2).getRecurrenceId(), null, null);
        assertNull(eventResponse.getError(), eventResponse.getError());
        assertNotNull(eventResponse.getData());
        assertEquals(createEvent.getData().getId(), eventResponse.getData().getId());
        assertNotEquals(createEvent.getData().getRecurrenceId(), eventResponse.getData().getRecurrenceId());
        Long expectedRecurrenceId = Long.valueOf((createEvent.getData().getStartDate()+TimeUnit.DAYS.toMillis(2)));
        expectedRecurrenceId -= TimeZone.getDefault().getOffset(expectedRecurrenceId);
        assertEquals(expectedRecurrenceId, eventResponse.getData().getRecurrenceId());


        // Get updates
        ChronosUpdatesResponse updatesResponse = api.getUpdates(session, folderId, date.getTime(), null, null, null, null, null, null, true, true);
        assertNull(updatesResponse.getError(), updatesResponse.getErrorDesc());
        assertNotNull(updatesResponse.getData());
        assertEquals(3, updatesResponse.getData().getNewAndModified().size());
        for (int x = 0; x < updatesResponse.getData().getNewAndModified().size(); x++) {
            assertEquals(createEvent.getData().getId(), updatesResponse.getData().getNewAndModified().get(x).getId());
        }

    }
}
