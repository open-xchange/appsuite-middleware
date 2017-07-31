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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.Attendee.CuTypeEnum;
import com.openexchange.testing.httpclient.models.ChronosCalendarResultResponse;
import com.openexchange.testing.httpclient.models.ChronosUpdatesResponse;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventData.TranspEnum;
import com.openexchange.testing.httpclient.models.EventId;
import com.openexchange.testing.httpclient.models.EventResponse;
import com.openexchange.testing.httpclient.models.EventsResponse;
import edu.emory.mathcs.backport.java.util.Collections;

/**
 *
 * {@link BasicSingleEventTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class BasicSingleEventTest extends AbstractChronosTest {

    private String folderId;

    @SuppressWarnings("unchecked")
    private EventData createSingleEvent(String summary) {
        EventData singleEvent = new EventData();
        singleEvent.setPropertyClass("PUBLIC");
        Attendee attendee = new Attendee();
        attendee.entity(calUser);
        attendee.cuType(CuTypeEnum.INDIVIDUAL);
        attendee.setUri("mailto:" + this.testUser.getLogin());
        singleEvent.setAttendees(Collections.singletonList(attendee));
        singleEvent.setStartDate(getDateTime(System.currentTimeMillis()));
        singleEvent.setEndDate(getDateTime(System.currentTimeMillis()+5000));
        singleEvent.setTransp(TranspEnum.OPAQUE);
        singleEvent.setAllDay(false);
        singleEvent.setSummary(summary);
        return singleEvent;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        folderId = getDefaultFolder();
    }

    @Test
    public void testCreateSingle() throws Exception {
        ChronosCalendarResultResponse createEvent = api.createEvent(session, folderId, createSingleEvent("testCreateSingle"), false, false);
        assertNull(createEvent.getError(), createEvent.getError());
        assertNotNull(createEvent.getData());
        EventData event = createEvent.getData().getCreated().get(0);
        EventId eventId = new EventId();
        eventId.setId(event.getId());
        rememberEventId(eventId);
        EventResponse eventResponse = api.getEvent(session, event.getId(), null, null);
        assertNull(eventResponse.getError(), eventResponse.getError());
        assertNotNull(eventResponse.getData());
        EventUtil.compare(event, eventResponse.getData(), true);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeleteSingle() throws Exception {

        ChronosCalendarResultResponse createEvent = api.createEvent(session, folderId, createSingleEvent("testDeleteSingle"), false, false);
        assertNull(createEvent.getError(), createEvent.getError());
        assertNotNull(createEvent.getData());
        EventData event = createEvent.getData().getCreated().get(0);
        EventId eventId = new EventId();
        eventId.setId(event.getId());

        ChronosCalendarResultResponse deleteResponse = api.deleteEvent(session, System.currentTimeMillis(), Collections.singletonList(eventId));
        assertNull(deleteResponse.getError());

        EventResponse eventResponse = api.getEvent(session, event.getId(), null, null);
        assertNotNull(eventResponse.getError());
        assertEquals("CAL-4040", eventResponse.getCode());
    }

    @Test
    public void testUpdateSingle() throws Exception {
        EventData initialEvent = createSingleEvent("testUpdateSingle");
        ChronosCalendarResultResponse createEvent = api.createEvent(session, folderId, initialEvent, false, false);
        assertNull(createEvent.getError(), createEvent.getError());
        assertNotNull(createEvent.getData());
        EventData event = createEvent.getData().getCreated().get(0);
        EventId eventId = new EventId();
        eventId.setId(event.getId());
        rememberEventId(eventId);

        event.setEndDate(addTimeToDateTimeData(event.getEndDate(), 5000));

        ChronosCalendarResultResponse updateResponse = api.updateEvent(session, folderId, eventId.getId(), event, System.currentTimeMillis(), true, null, false);
        assertNull(updateResponse.getErrorDesc(), updateResponse.getError());
        assertNotNull(updateResponse.getData());

        List<EventData> updates = updateResponse.getData().getUpdated();
        assertTrue(updates.size()==1);
        EventUtil.compare(initialEvent, updates.get(0), false);
        event.setLastModified(updates.get(0).getLastModified());
        event.setSequence(updates.get(0).getSequence());
        EventUtil.compare(event, updates.get(0), true);
    }


    @Test
    public void testGetEvent() throws Exception {
        Date date = new Date();
        Date today = getAPIDate(TimeZone.getTimeZone("UTC"), date, 0);
        Date tomorrow = getAPIDate(TimeZone.getTimeZone("UTC"), date, 1);

        // Create a single event
        ChronosCalendarResultResponse createEvent = api.createEvent(session, folderId, createSingleEvent("testGetEvent"), false, false);
        assertNull(createEvent.getError(), createEvent.getError());
        assertNotNull(createEvent.getData());
        EventData event = createEvent.getData().getCreated().get(0);
        EventId eventId = new EventId();
        eventId.setId(event.getId());
        rememberEventId(eventId);

        // Get event directly
        EventResponse eventResponse = api.getEvent(session, event.getId(), null, null);
        assertNull(eventResponse.getError(), eventResponse.getError());
        assertNotNull(eventResponse.getData());
        EventUtil.compare(event, eventResponse.getData(), true);

        // Get all events
        EventsResponse eventsResponse = api.getAllEvents(session, folderId, getZuluDateTime(today.getTime()).getValue(), getZuluDateTime(tomorrow.getTime()).getValue(), null, null, null, false, true);
        assertNull(eventsResponse.getError(), eventsResponse.getError());
        assertNotNull(eventsResponse.getData());
        assertEquals(1, eventsResponse.getData().size());
        EventUtil.compare(event, eventsResponse.getData().get(0), true);

        // Get updates
        ChronosUpdatesResponse updatesResponse = api.getUpdates(session, folderId, date.getTime(), null, null, null, null, null, false, true);
        assertNull(updatesResponse.getError(), updatesResponse.getErrorDesc());
        assertNotNull(updatesResponse.getData());
        assertEquals(1, updatesResponse.getData().getNewAndModified().size());
        EventUtil.compare(event, updatesResponse.getData().getNewAndModified().get(0), true);

        // List events
        List<EventId> ids = new ArrayList<>(1);
        ids.add(eventId);
        EventsResponse listResponse = api.getEventList(session, ids, null);
        assertNull(listResponse.getError(), listResponse.getError());
        assertNotNull(listResponse.getData());
        assertEquals(1, listResponse.getData().size());
        EventUtil.compare(event, listResponse.getData().get(0), true);

    }
}
