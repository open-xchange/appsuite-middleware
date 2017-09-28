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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.ajax.chronos.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.openexchange.ajax.chronos.UserApi;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.configuration.asset.Asset;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.AlarmTriggerData;
import com.openexchange.testing.httpclient.models.AlarmTriggerResponse;
import com.openexchange.testing.httpclient.models.CalendarResult;
import com.openexchange.testing.httpclient.models.ChronosCalendarResultResponse;
import com.openexchange.testing.httpclient.models.ChronosUpdatesResponse;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventId;
import com.openexchange.testing.httpclient.models.EventResponse;
import com.openexchange.testing.httpclient.models.EventsResponse;
import com.openexchange.testing.httpclient.models.UpdatesResult;

/**
 * {@link EventManager}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 */
public class EventManager extends AbstractManager {

    private final UserApi userApi;
    private final String defaultFolder;

    private List<EventId> eventIds;
    private long lastTimeStamp;

    /**
     * Initialises a new {@link EventManager}.
     */
    public EventManager(UserApi userApi, String defaultFolder) {
        super();
        this.userApi = userApi;
        this.defaultFolder = defaultFolder;
        eventIds = new ArrayList<>();
    }

    public void cleanUp() {
        try {
            userApi.getChronosApi().deleteEvent(userApi.getSession(), System.currentTimeMillis(), eventIds);
        } catch (Exception e) {
            System.err.println("Could not clean up the events for user " + userApi.getCalUser() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates an event and does not ignore conflicts
     * 
     * @param eventData The data of the event
     * @return The created {@link EventData}
     * @throws ApiException if an API error is occurred
     */
    public EventData createEvent(EventData eventData) throws ApiException {
        ChronosCalendarResultResponse createEvent = userApi.getChronosApi().createEvent(userApi.getSession(), defaultFolder, eventData, false, false);
        EventData event = handleCreation(createEvent);
        return event;
    }

    /**
     * Creates an event and attaches the specified {@link Asset}
     * 
     * @param eventData The {@link EventData}
     * @param asset The {@link Asset} to attach
     * @return The created {@link EventData}
     * @throws ApiException if an API error is occurred
     */
    public EventData createEventWithAttachment(EventData eventData, Asset asset) throws ApiException {
        ChronosCalendarResultResponse createEvent = userApi.getEnhancedChronosApi().createEventWithAttachments(userApi.getSession(), defaultFolder, eventData.toJson(), new File(asset.getAbsolutePath()), false, false);
        EventData event = handleCreation(createEvent);
        return event;
    }

    /**
     * Creates an event and attaches the specified {@link Asset}s
     * 
     * @param eventData The {@link EventData}
     * @param assets The {@link Asset}s to attach
     * @return The created {@link EventData}
     * @throws ApiException if an API error is occurred
     */
    public EventData createEventWithAttachments(EventData eventData, List<Asset> assets) throws ApiException {
        List<File> files = new ArrayList<>();
        for (Asset asset : assets) {
            files.add(new File(asset.getAbsolutePath()));
        }
        ChronosCalendarResultResponse createEvent = userApi.getEnhancedChronosApi().createEventWithAttachments(userApi.getSession(), defaultFolder, eventData.toJson(), files, false, false);
        EventData event = handleCreation(createEvent);
        return event;
    }

    /**
     * Update the specified event and attach the specified {@link Asset}
     * 
     * @param eventData The event
     * @param asset The {@link Asset} to attach
     * @return The updated {@link EventData}
     * @throws ApiException if an API error is occurred
     */
    public EventData updateEventWithAttachment(EventData eventData, Asset asset) throws ApiException {
        ChronosCalendarResultResponse updateResponse = userApi.getEnhancedChronosApi().updateEventWithAttachments(userApi.getSession(), defaultFolder, eventData.getId(), System.currentTimeMillis(), eventData.toJson(), new File(asset.getAbsolutePath()), null, true, false);
        return handleUpdate(updateResponse);
    }

    /**
     * Get an event
     * 
     * @param eventId The {@link EventId}
     * @return the {@link EventData}
     * @throws ApiException if an API error is occurred
     * @throws ChronosApiException if a Chronos API error is occurred
     */
    public EventData getEvent(String eventId) throws ApiException, ChronosApiException {
        return getEvent(eventId, false);
    }

    /**
     * Get an event
     * 
     * @param eventId The {@link EventId}
     * @param expectedException flag to indicate that an exception is expected
     * @return the {@link EventData}
     * @throws ApiException if an API error is occurred
     * @throws ChronosApiException if a Chronos API error is occurred
     */
    public EventData getEvent(String eventId, boolean expectException) throws ApiException, ChronosApiException {
        EventResponse eventResponse = userApi.getChronosApi().getEvent(userApi.getSession(), eventId, defaultFolder, null, null);
        if (expectException) {
            assertNotNull(eventResponse.getError());
            throw new ChronosApiException(eventResponse.getCode(), eventResponse.getError());
        }
        return checkResponse(eventResponse.getError(), eventResponse.getError(), eventResponse.getData());
    }

    /**
     * Shifts a given event by the given amount
     *
     * @param eventId The event id
     * @param recurrence The recurrence id or null
     * @param event The event data to change
     * @param startTime The start time of the event
     * @param unit The unit of the shift
     * @param value The shifting amount
     * @param timestamp The timestamp of the last request
     * @return The {@link CalendarResult}
     * @throws ApiException if an API error is occurred
     */
    public CalendarResult shiftEvent(String eventId, String recurrence, EventData event, Calendar startTime, TimeUnit unit, int value, long timestamp) throws ApiException {
        Calendar newStartTime = Calendar.getInstance(startTime.getTimeZone());
        newStartTime.setTimeInMillis(startTime.getTimeInMillis() + unit.toMillis(value));
        event.setStartDate(DateTimeUtil.getDateTime(newStartTime));

        Calendar endTime = Calendar.getInstance(startTime.getTimeZone());
        endTime.setTimeInMillis(startTime.getTimeInMillis());
        endTime.add(Calendar.HOUR, 1);
        event.setEndDate(DateTimeUtil.getDateTime(endTime));

        ChronosCalendarResultResponse updateEvent = userApi.getChronosApi().updateEvent(userApi.getSession(), defaultFolder, eventId, event, timestamp, recurrence, false, false);
        lastTimeStamp = updateEvent.getTimestamp();
        return checkResponse(updateEvent.getError(), updateEvent.getErrorDesc(), updateEvent.getData());
    }

    /**
     * Retrieves the attachment of the specified event
     * 
     * @param eventId The event identifier
     * @param attachmentId The attachment's identifier
     * @return The binary data of the attachment
     * @throws ApiException if an API error is occurred
     */
    public byte[] getAttachment(String eventId, int attachmentId) throws ApiException {
        byte[] eventAttachment = userApi.getChronosApi().getEventAttachment(userApi.getSession(), eventId, defaultFolder, attachmentId);
        assertNotNull(eventAttachment);
        return eventAttachment;
    }

    /**
     * Retrieves all events with in the specified interval
     * 
     * @param from The starting date
     * @param until The ending date
     * @return A {@link List} with {@link EventData}
     * @throws ApiException if an API error occurs
     */
    public List<EventData> getAllEvents(Date from, Date until) throws ApiException {
        EventsResponse eventsResponse = userApi.getChronosApi().getAllEvents(userApi.getSession(), DateTimeUtil.getZuluDateTime(from.getTime()).getValue(), DateTimeUtil.getZuluDateTime(until.getTime()).getValue(), defaultFolder, null, null, null, false, true);
        return checkResponse(eventsResponse.getErrorDesc(), eventsResponse.getError(), eventsResponse.getData());
    }

    /**
     * Lists the events with the specified identifiers
     * 
     * @param ids The event identifiers
     * @return A {@link List} with {@link EventData}
     * @throws ApiException if an API error occurs
     */
    public List<EventData> listEvents(List<EventId> ids) throws ApiException {
        EventsResponse listResponse = userApi.getChronosApi().getEventList(userApi.getSession(), ids, null);
        return checkResponse(listResponse.getErrorDesc(), listResponse.getError(), listResponse.getData());
    }

    /**
     * Deletes the event with the specified identifier
     * 
     * @param eventId The {@link EventId}
     * @throws ApiException if an API error is occurred
     */
    public void deleteEvent(EventId eventId) throws ApiException {
        ChronosCalendarResultResponse deleteResponse = userApi.getChronosApi().deleteEvent(userApi.getSession(), System.currentTimeMillis(), Collections.singletonList(eventId));
        assertNull(deleteResponse.getErrorDesc(), deleteResponse.getError());
        forgetEventId(userApi, eventId);
        lastTimeStamp = deleteResponse.getTimestamp();
    }

    /**
     * Deletes the event with the specified identifier
     * 
     * @param eventId The {@link EventId}
     * @throws ApiException if an API error is occurred
     */
    public void deleteEvent(EventId eventId, long timestamp) throws ApiException {
        ChronosCalendarResultResponse deleteResponse = userApi.getChronosApi().deleteEvent(userApi.getSession(), timestamp, Collections.singletonList(eventId));
        assertNull(deleteResponse.getErrorDesc(), deleteResponse.getError());
        forgetEventId(userApi, eventId);
        lastTimeStamp = deleteResponse.getTimestamp();
    }

    /**
     * Updates the specified event and ignores conflicts
     * 
     * @param eventData The data of the event
     * @return The updated event
     * @throws ApiException if an API error is occurred
     */
    public EventData updateEvent(EventData eventData) throws ApiException {
        ChronosCalendarResultResponse updateResponse = userApi.getChronosApi().updateEvent(userApi.getSession(), defaultFolder, eventData.getId(), eventData, System.currentTimeMillis(), null, true, false);
        return handleUpdate(updateResponse);
    }

    /**
     * Gets all changed events since the given timestamp.
     * 
     * @param since The timestamp
     * @return The {@link UpdatesResult}
     * @throws ApiException if an API error is occurred
     */
    public UpdatesResult getUpdates(Date since) throws ApiException {
        ChronosUpdatesResponse updatesResponse = userApi.getChronosApi().getUpdates(userApi.getSession(), defaultFolder, since.getTime(), null, null, null, null, null, false, true);
        return checkResponse(updatesResponse.getErrorDesc(), updatesResponse.getError(), updatesResponse.getData());
    }

    /**
     * Retrieves not acknowledged alarm triggers.
     * 
     * @param until Upper exclusive limit of the queried range as a utc date-time value as specified
     *            in RFC 5545 chapter 3.3.5. E.g. \"20170708T220000Z\". Only events which should trigger before this date are returned.
     * @return The {@link AlarmTriggerData}
     * @throws ApiException if an API error is occurred
     */
    public AlarmTriggerData getAlarmTrigger(long until) throws ApiException {
        return getAlarmTrigger(until, null);
    }

    /**
     * Retrieves not acknowledged alarm triggers.
     * 
     * @param until Upper exclusive limit of the queried range as a utc date-time value as specified
     *            in RFC 5545 chapter 3.3.5. E.g. \"20170708T220000Z\". Only events which should trigger before this date are returned.
     * @param actions The actions to retrieve (comma separated string)
     * @return The {@link AlarmTriggerData}
     * @throws ApiException if an API error is occurred
     */
    public AlarmTriggerData getAlarmTrigger(long until, String actions) throws ApiException {
        AlarmTriggerResponse triggerResponse = userApi.getChronosApi().getAlarmTrigger(userApi.getSession(), DateTimeUtil.getZuluDateTime(until).getValue(), actions);
        return checkResponse(triggerResponse.getError(), triggerResponse.getErrorDesc(), triggerResponse.getData());
    }

    //////////////////////////////////////////////// HELPERS ///////////////////////////////////////////////////

    /**
     * Handles the result response of an event creation
     *
     * @param createEvent The result
     * @return The created event
     */
    private EventData handleCreation(ChronosCalendarResultResponse createEvent) {
        CalendarResult result = checkResponse(createEvent.getError(), createEvent.getErrorDesc(), createEvent.getData());
        assertEquals("Found unexpected conflicts", 0, result.getConflicts().size());
        EventData event = result.getCreated().get(0);

        EventId eventId = new EventId();
        eventId.setId(event.getId());
        eventId.setFolderId(event.getFolder());
        rememberEventId(userApi, eventId);
        lastTimeStamp = createEvent.getTimestamp();

        return event;
    }

    /**
     * Handles the result response of an update event
     * 
     * @param updateEvent The result
     * @return The updated event
     */
    private EventData handleUpdate(ChronosCalendarResultResponse updateEvent) {
        CalendarResult calendarResult = checkResponse(updateEvent.getErrorDesc(), updateEvent.getError(), updateEvent.getData());
        List<EventData> updates = calendarResult.getUpdated();
        assertTrue(updates.size() == 1);

        return updates.get(0);
    }

    /**
     * Keeps track of the specified {@link EventId} for the specified user
     * 
     * @param userApi The {@link UserApi}
     * @param eventId The {@link EventId}
     */
    private void rememberEventId(UserApi userApi, EventId eventId) {
        if (eventIds == null) {
            eventIds = new ArrayList<>();
        }
        eventIds.add(eventId);
    }

    /**
     * Removes the specified {@link EventId} for the specified user from the cache
     * 
     * @param userApi The {@link UserApi}
     * @param eventId The {@link EventId}
     */
    protected void forgetEventId(UserApi userApi, EventId eventId) {
        if (eventIds == null) {
            eventIds = new ArrayList<>();
        }
        eventIds.remove(eventId);
    }

    /**
     * Gets the lastTimeStamp
     *
     * @return The lastTimeStamp
     */
    public long getLastTimeStamp() {
        return lastTimeStamp;
    }

    /**
     * Sets the lastTimeStamp
     *
     * @param lastTimeStamp The lastTimeStamp to set
     */
    public void setLastTimeStamp(long lastTimeStamp) {
        this.lastTimeStamp = lastTimeStamp;
    }
}
