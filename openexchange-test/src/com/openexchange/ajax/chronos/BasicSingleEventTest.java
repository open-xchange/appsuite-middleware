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
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.configuration.asset.Asset;
import com.openexchange.configuration.asset.AssetManager;
import com.openexchange.configuration.asset.AssetType;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.Attendee.CuTypeEnum;
import com.openexchange.testing.httpclient.models.ChronosAttachment;
import com.openexchange.testing.httpclient.models.ChronosCalendarResultResponse;
import com.openexchange.testing.httpclient.models.ChronosUpdatesResponse;
import com.openexchange.testing.httpclient.models.DateTimeData;
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
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class BasicSingleEventTest extends AbstractChronosTest {

    private String folderId;

    @SuppressWarnings("unchecked")
    private EventData createSingleEvent(String summary, DateTimeData startDate, DateTimeData endDate) {
        EventData singleEvent = new EventData();
        singleEvent.setPropertyClass("PUBLIC");
        Attendee attendee = new Attendee();
        attendee.entity(defaultUserApi.getCalUser());
        attendee.cuType(CuTypeEnum.INDIVIDUAL);
        attendee.setUri("mailto:" + this.testUser.getLogin());
        singleEvent.setAttendees(Collections.singletonList(attendee));
        singleEvent.setStartDate(startDate);
        singleEvent.setEndDate(endDate);
        singleEvent.setTransp(TranspEnum.OPAQUE);
        singleEvent.setAllDay(false);
        singleEvent.setSummary(summary);
        return singleEvent;
    }

    private EventData createSingleEventWithAttachment(String summary, DateTimeData startDate, DateTimeData endDate, Asset asset) {
        EventData eventData = createSingleEvent(summary, startDate, endDate);
        eventData.addAttachmentsItem(createAttachment(asset));
        return eventData;
    }

    private ChronosAttachment createAttachment(Asset asset) {
        ChronosAttachment attachment = new ChronosAttachment();
        attachment.setFilename(asset.getFilename());
        attachment.setFmtType(asset.getAssetType().name());
        attachment.setUri("file:///");

        return attachment;
    }

    private EventData createSingleEvent(String summary) {
        return createSingleEvent(summary, getDateTime(System.currentTimeMillis()), getDateTime(System.currentTimeMillis() + 5000));
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        folderId = getDefaultFolder();
    }

    @Test
    public void testCreateSingle() throws Exception {
        ChronosCalendarResultResponse createEvent = defaultUserApi.getApi().createEvent(defaultUserApi.getSession(), folderId, createSingleEvent("testCreateSingle"), false, false);
        EventData event = handleCreation(createEvent);
        EventResponse eventResponse = defaultUserApi.getApi().getEvent(defaultUserApi.getSession(), event.getId(), folderId, null, null);
        assertNull(eventResponse.getError(), eventResponse.getError());
        assertNotNull(eventResponse.getData());
        EventUtil.compare(event, eventResponse.getData(), true);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeleteSingle() throws Exception {

        ChronosCalendarResultResponse createEvent = defaultUserApi.getApi().createEvent(defaultUserApi.getSession(), folderId, createSingleEvent("testDeleteSingle"), false, false);
        assertNull(createEvent.getError(), createEvent.getError());
        assertNotNull(createEvent.getData());
        EventData event = createEvent.getData().getCreated().get(0);
        EventId eventId = new EventId();
        eventId.setId(event.getId());
        eventId.setFolderId(folderId);

        ChronosCalendarResultResponse deleteResponse = defaultUserApi.getApi().deleteEvent(defaultUserApi.getSession(), System.currentTimeMillis(), Collections.singletonList(eventId));
        assertNull(deleteResponse.getErrorDesc(), deleteResponse.getError());

        EventResponse eventResponse = defaultUserApi.getApi().getEvent(defaultUserApi.getSession(), event.getId(), folderId, null, null);
        assertNotNull(eventResponse.getError());
        assertEquals("CAL-4040", eventResponse.getCode());
    }

    @Test
    public void testUpdateSingle() throws Exception {
        EventData initialEvent = createSingleEvent("testUpdateSingle");
        ChronosCalendarResultResponse createEvent = defaultUserApi.getApi().createEvent(defaultUserApi.getSession(), folderId, initialEvent, false, false);
        EventData event = handleCreation(createEvent);

        event.setEndDate(addTimeToDateTimeData(event.getEndDate(), 5000));

        ChronosCalendarResultResponse updateResponse = defaultUserApi.getApi().updateEvent(defaultUserApi.getSession(), folderId, event.getId(), event, System.currentTimeMillis(), null, true, false);
        assertNull(updateResponse.getErrorDesc(), updateResponse.getError());
        assertNotNull(updateResponse.getData());

        List<EventData> updates = updateResponse.getData().getUpdated();
        assertTrue(updates.size() == 1);
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
        ChronosCalendarResultResponse createEvent = defaultUserApi.getApi().createEvent(defaultUserApi.getSession(), folderId, createSingleEvent("testGetEvent"), false, false);
        assertNull(createEvent.getError(), createEvent.getError());
        assertNotNull(createEvent.getData());
        EventData event = createEvent.getData().getCreated().get(0);
        EventId eventId = new EventId();
        eventId.setId(event.getId());
        eventId.setFolderId(folderId);
        rememberEventId(defaultUserApi, eventId);

        // Get event directly
        EventResponse eventResponse = defaultUserApi.getApi().getEvent(defaultUserApi.getSession(), event.getId(), folderId, null, null);
        assertNull(eventResponse.getErrorDesc(), eventResponse.getError());
        assertNotNull(eventResponse.getData());
        EventUtil.compare(event, eventResponse.getData(), true);

        // Get all events
        EventsResponse eventsResponse = defaultUserApi.getApi().getAllEvents(defaultUserApi.getSession(), getZuluDateTime(today.getTime()).getValue(), getZuluDateTime(tomorrow.getTime()).getValue(), folderId, null, null, null, false, true);
        assertNull(eventsResponse.getErrorDesc(), eventsResponse.getError());
        assertNotNull(eventsResponse.getData());
        assertEquals(1, eventsResponse.getData().size());
        EventUtil.compare(event, eventsResponse.getData().get(0), true);

        // Get updates
        ChronosUpdatesResponse updatesResponse = defaultUserApi.getApi().getUpdates(defaultUserApi.getSession(), folderId, date.getTime(), null, null, null, null, null, false, true);
        assertNull(updatesResponse.getErrorDesc(), updatesResponse.getError());
        assertNotNull(updatesResponse.getData());
        assertEquals(1, updatesResponse.getData().getNewAndModified().size());
        EventUtil.compare(event, updatesResponse.getData().getNewAndModified().get(0), true);

        // List events
        List<EventId> ids = new ArrayList<>(1);
        ids.add(eventId);
        EventsResponse listResponse = defaultUserApi.getApi().getEventList(defaultUserApi.getSession(), ids, null);
        assertNull(listResponse.getErrorDesc(), listResponse.getError());
        assertNotNull(listResponse.getData());
        assertEquals(1, listResponse.getData().size());
        EventUtil.compare(event, listResponse.getData().get(0), true);

    }

    @Test
    public void testCreateSingleWithDifferentTimeZones() throws Exception {

        Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start.setTimeInMillis(System.currentTimeMillis());

        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
        end.setTimeInMillis(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(10));

        ChronosCalendarResultResponse createEvent = defaultUserApi.getApi().createEvent(defaultUserApi.getSession(), folderId, createSingleEvent("testCreateSingle", getDateTime(start), getDateTime(end)), false, false);
        EventData event = handleCreation(createEvent);
        EventResponse eventResponse = defaultUserApi.getApi().getEvent(defaultUserApi.getSession(), event.getId(), folderId, null, null);
        assertNull(eventResponse.getError(), eventResponse.getError());
        assertNotNull(eventResponse.getData());
        EventUtil.compare(event, eventResponse.getData(), true);
    }

    ////////////////////////////////// Attachment Tests ///////////////////////////////////

    /**
     * Tests the creation of a single event with attachment
     */
    @Test
    public void testCreateSingleWithAttachment() throws Exception {
        EnhancedChronosApi eca = new EnhancedChronosApi(defaultUserApi.getClient());

        Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start.setTimeInMillis(System.currentTimeMillis());

        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
        end.setTimeInMillis(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(2));

        AssetManager assetManager = new AssetManager();
        Asset asset = assetManager.getRandomAsset(AssetType.jpg);

        ChronosCalendarResultResponse createEvent = eca.createEventWithAttachments(defaultUserApi.getSession(), folderId, createSingleEventWithAttachment("testCreateSingleWithAttachment", getDateTime(start), getDateTime(end), asset).toJson(), new File(asset.getAbsolutePath()), false, false);
        EventData event = handleCreation(createEvent);
        EventResponse eventResponse = defaultUserApi.getApi().getEvent(defaultUserApi.getSession(), event.getId(), folderId, null, null);
        assertNull(eventResponse.getError(), eventResponse.getError());
        assertNotNull(eventResponse.getData());
        EventUtil.compare(event, eventResponse.getData(), true);
        assertEquals("The amount of attachments is not correct", 1, event.getAttachments().size());
    }

    /**
     * Tests the update of a single event with attachment
     */
    @Test
    public void testUpdateSingleWithAttachment() throws Exception {
        EnhancedChronosApi eca = new EnhancedChronosApi(defaultUserApi.getClient());

        Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start.setTimeInMillis(System.currentTimeMillis());

        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
        end.setTimeInMillis(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(2));

        AssetManager assetManager = new AssetManager();
        Asset asset = assetManager.getRandomAsset(AssetType.jpg);

        EventData initialEvent = createSingleEventWithAttachment("testCreateSingleWithAttachment", getDateTime(start), getDateTime(end), asset);
        ChronosCalendarResultResponse createEvent = eca.createEventWithAttachments(defaultUserApi.getSession(), folderId, initialEvent.toJson(), new File(asset.getAbsolutePath()), false, false);
        EventData event = handleCreation(createEvent);
        EventResponse eventResponse = defaultUserApi.getApi().getEvent(defaultUserApi.getSession(), event.getId(), folderId, null, null);
        assertNull(eventResponse.getError(), eventResponse.getError());
        assertNotNull(eventResponse.getData());
        EventUtil.compare(event, eventResponse.getData(), true);

        List<ChronosAttachment> attachments = event.getAttachments();
        assertEquals("The amount of attachments is not correct", 1, attachments.size());

        asset = assetManager.getRandomAsset(AssetType.png);
        event.getAttachments().add(createAttachment(asset));

        ChronosCalendarResultResponse updateResponse = eca.updateEventWithAttachments(defaultUserApi.getSession(), folderId, event.getId(), System.currentTimeMillis(), event.toJson(), new File(asset.getAbsolutePath()), null, true, false);
        assertNull(updateResponse.getErrorDesc(), updateResponse.getError());
        assertNotNull(updateResponse.getData());

        List<EventData> updates = updateResponse.getData().getUpdated();
        assertTrue(updates.size() == 1);
        EventUtil.compare(initialEvent, updates.get(0), false);
        event.setLastModified(updates.get(0).getLastModified());
        event.setSequence(updates.get(0).getSequence());
        EventUtil.compare(event, updates.get(0), true);
    }
}
