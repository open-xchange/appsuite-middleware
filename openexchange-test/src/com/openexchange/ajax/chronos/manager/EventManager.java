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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.chronos.UserApi;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.chronos.service.SortOrder;
import com.openexchange.configuration.asset.Asset;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.AlarmTriggerData;
import com.openexchange.testing.httpclient.models.AlarmTriggerResponse;
import com.openexchange.testing.httpclient.models.AttendeeAndAlarm;
import com.openexchange.testing.httpclient.models.CalendarResult;
import com.openexchange.testing.httpclient.models.ChronosCalendarResultResponse;
import com.openexchange.testing.httpclient.models.ChronosMultipleCalendarResultResponse;
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

    private static final boolean EXPAND_SERIES = false;

    private static final SimpleDateFormat UTC_DATE_FORMATTER = new SimpleDateFormat("yyyyMMdd'T'HHmmss");

    /**
     * Initializes a new {@link EventManager}.
     */
    public EventManager(UserApi userApi, String defaultFolder) {
        super();
        this.userApi = userApi;
        this.defaultFolder = defaultFolder;
        eventIds = new ArrayList<>();
    }

    /**
     * Removes all events that were created with this event manager during the session
     */
    public void cleanUp() {
        try {
            userApi.getChronosApi().deleteEvent(userApi.getSession(), System.currentTimeMillis(), eventIds, null, null, EXPAND_SERIES, false);
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
        return createEvent(eventData, false);
    }

    /**
     * Creates an event
     *
     * @param eventData The data of the event
     * @param ignoreConflicts Flag whether or not to ignore conflicts
     * @return The created {@link EventData}
     * @throws ApiException if an API error is occurred
     */
    public EventData createEvent(EventData eventData, boolean ignoreConflicts) throws ApiException {
        ChronosCalendarResultResponse createEvent = userApi.getChronosApi().createEvent(userApi.getSession(), getFolder(eventData), eventData, ignoreConflicts, false, false, null, null, EXPAND_SERIES);
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
     * @throws ChronosApiException if a Chronos API error is occurred
     */
    public JSONObject createEventWithAttachment(EventData eventData, Asset asset) throws ApiException, ChronosApiException {
        return createEventWithAttachment(eventData, asset, false);
    }

    /**
     * Creates an event and attaches the specified {@link Asset}
     *
     * @param eventData The {@link EventData}
     * @param asset The {@link Asset} to attach
     * @param expectedException flag to indicate that an exception is expected
     * @return The created {@link EventData}
     * @throws ApiException if an API error is occurred
     * @throws ChronosApiException if a Chronos API error is occurred
     */
    public JSONObject createEventWithAttachment(EventData eventData, Asset asset, boolean expectException) throws ApiException, ChronosApiException {
        String response = userApi.getEnhancedChronosApi().createEventWithAttachments(userApi.getEnhancedSession(), getFolder(eventData), eventData.toJson(), new File(asset.getAbsolutePath()), false, false, false);
        JSONObject responseData = extractBody(response);
        if (expectException) {
            assertNotNull("An error was expected", responseData.optString("error"));
            throw new ChronosApiException(responseData.optString("code"), responseData.optString("error"));
        }
        return handleCreation(response);
    }

    /**
     * Creates an event and attaches the specified {@link Asset}s
     *
     * @param eventData The {@link EventData}
     * @param assets The {@link Asset}s to attach
     * @return The created {@link EventData}
     * @throws ApiException if an API error is occurred
     * @throws ChronosApiException
     */
    public JSONObject createEventWithAttachments(EventData eventData, List<Asset> assets) throws ApiException, ChronosApiException {
        List<File> files = new ArrayList<>();
        for (Asset asset : assets) {
            files.add(new File(asset.getAbsolutePath()));
        }
        return handleCreation(userApi.getEnhancedChronosApi().createEventWithAttachments(userApi.getEnhancedSession(), getFolder(eventData), eventData.toJson(), files, false, false));
    }

    /**
     * Update the specified event and attach the specified {@link Asset}
     *
     * @param eventData The event
     * @param asset The {@link Asset} to attach
     * @return The updated {@link EventData}
     * @throws ApiException if an API error is occurred
     * @throws ChronosApiException
     */
    public JSONObject updateEventWithAttachment(EventData eventData, Asset asset) throws ApiException, ChronosApiException {
        return handleUpdate(userApi.getEnhancedChronosApi().updateEventWithAttachments(userApi.getEnhancedSession(), getFolder(eventData) , eventData.getId(), eventData.getLastModified(), eventData.toJson(), new File(asset.getAbsolutePath()), null, true, false, false));
    }

    private String getFolder(EventData eventData) {
        return eventData.getFolder() == null ? defaultFolder : eventData.getFolder();
    }

    /**
     * Get an event
     *
     * @param eventId The {@link EventId}
     * @return the {@link EventData}
     * @throws ApiException if an API error is occurred
     * @throws ChronosApiException if a Chronos API error is occurred
     */
    public EventData getEvent(String folderId, String eventId) throws ApiException, ChronosApiException {
        return getEvent(folderId, eventId, false);
    }

    public EventData getEvent(String folderId, String eventId, String recurrenceId, boolean expectException) throws ApiException, ChronosApiException {
        EventResponse eventsResponse = userApi.getChronosApi().getEvent(userApi.getSession(), eventId, folderId, recurrenceId, null, null);
        if (expectException) {
            assertNotNull("An error was expected", eventsResponse.getError());
            throw new ChronosApiException(eventsResponse.getCode(), eventsResponse.getError());
        } else {
            assertNull(eventsResponse.getError());
        }
        lastTimeStamp = eventsResponse.getTimestamp();
        return eventsResponse.getData();
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
    public EventData getEvent(String folder, String eventId, boolean expectException) throws ApiException, ChronosApiException {
        return getRecurringEvent(folder, eventId, null, expectException);
    }

    /**
     * Gets the occurrence of an event
     *
     * @param folder The folder id or null
     * @param eventId The {@link EventId}
     * @param reccurenceId The recurrence identifier
     * @param expectedException flag to indicate that an exception is expected
     * @return the {@link EventData}
     * @throws ApiException if an API error is occurred
     * @throws ChronosApiException if a Chronos API error is occurred
     */
    public EventData getRecurringEvent(String folder, String eventId, String reccurenceId, boolean expectException) throws ApiException, ChronosApiException {
        return getRecurringEvent(folder != null ? folder : defaultFolder, eventId, reccurenceId, expectException, false);
    }

    /**
     * Gets the occurrence of an event
     *
     * @param folder The folder or null
     * @param eventId The {@link EventId}
     * @param reccurenceId The recurrence identifier
     * @param expectedException flag to indicate that an exception is expected
     * @param extendedEntities Whether attendees should be extended with contact field or not
     * @return the {@link EventData}
     * @throws ApiException if an API error is occurred
     * @throws ChronosApiException if a Chronos API error is occurred
     */
    public EventData getRecurringEvent(String folder, String eventId, String reccurenceId, boolean expectException, boolean extendedEntities) throws ApiException, ChronosApiException {
        EventResponse eventsResponse = userApi.getChronosApi().getEvent(userApi.getSession(), eventId, folder != null ? folder : defaultFolder, reccurenceId, null, extendedEntities);
        if (expectException) {
            assertNotNull("An error was expected", eventsResponse.getError());
            throw new ChronosApiException(eventsResponse.getCode(), eventsResponse.getError());
        }
        checkResponse(eventsResponse.getError(), eventsResponse.getError(), eventsResponse.getCategories(), eventsResponse.getData());
        lastTimeStamp = eventsResponse.getTimestamp();
        return eventsResponse.getData();
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
    public CalendarResult shiftEvent(String eventId, String recurrence, EventData event, Calendar startTime, TimeUnit unit, int value, Long timestamp) throws ApiException {
        Calendar newStartTime = Calendar.getInstance(startTime.getTimeZone());
        newStartTime.setTimeInMillis(startTime.getTimeInMillis() + unit.toMillis(value));
        event.setStartDate(DateTimeUtil.getDateTime(newStartTime));

        Calendar endTime = Calendar.getInstance(startTime.getTimeZone());
        endTime.setTimeInMillis(startTime.getTimeInMillis());
        endTime.add(Calendar.HOUR, 1);
        event.setEndDate(DateTimeUtil.getDateTime(endTime));
        ChronosCalendarResultResponse updateEvent = userApi.getChronosApi().updateEvent(userApi.getSession(), getFolder(event), eventId, event, timestamp == null ? lastTimeStamp : timestamp, recurrence, null, false, false, false, null, null, EXPAND_SERIES);
        assertNull(updateEvent.getErrorDesc(), updateEvent.getError());
        assertNotNull("Missing timestamp", updateEvent.getTimestamp());
        lastTimeStamp = updateEvent.getTimestamp();
        return checkResponse(updateEvent.getError(), updateEvent.getErrorDesc(), updateEvent.getCategories(), updateEvent.getData());
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
        return getAttachment(eventId, attachmentId, defaultFolder);
    }

    /**
     * Retrieves the attachment of the specified event
     *
     * @param eventId The event identifier
     * @param attachmentId The attachment's identifier
     * @param folderId The folder id
     * @return The binary data of the attachment
     * @throws ApiException if an API error is occurred
     */
    public byte[] getAttachment(String eventId, int attachmentId, String folderId) throws ApiException {
        byte[] eventAttachment = userApi.getChronosApi().getEventAttachment(userApi.getSession(), eventId, folderId, attachmentId);
        assertNotNull(eventAttachment);
        return eventAttachment;
    }

    /**
     * Retrieves all events with in the specified interval (occurences will not be expanded)
     *
     * @param from The starting date
     * @param until The ending date
     * @return A {@link List} with {@link EventData}
     * @throws ApiException if an API error occurs
     */
    public List<EventData> getAllEvents(Date from, Date until) throws ApiException {
        return getAllEvents(defaultFolder, from, until, false);
    }

    /**
     * Retrieves all events within the specified interval
     *
     * @param from The starting date
     * @param until The ending date
     * @param expand Flag to expand the occurrences
     * @return A {@link List} with {@link EventData}
     * @throws ApiException if an API error occurs
     */
    public List<EventData> getAllEvents(String folder, Date from, Date until, boolean expand) throws ApiException {
        return getAllEvents(from, until, expand, folder==null ? defaultFolder : folder);
    }

    /**
     * Retrieves all events within the specified interval in the specified folder
     *
     * @param from The starting date
     * @param until The ending date
     * @param expand Flag to expand occurrences
     * @param folder The folder identifier
     * @return A {@link List} with {@link EventData}
     * @throws ApiException if an API error occurs
     */
    public List<EventData> getAllEvents(Date from, Date until, boolean expand, String folder) throws ApiException {
        return getAllEvents(from, until, expand, folder, null);
    }

    /**
     * Retrieves all events within the specified interval in the specified folder
     *
     * @param from The starting date
     * @param until The ending date
     * @param expand Flag to expand occurrences
     * @param folder The folder identifier
     * @param sortOrder The sortOder of the events
     * @return A {@link List} with {@link EventData}
     * @throws ApiException if an API error occurs
     */
    public List<EventData> getAllEvents(Date from, Date until, boolean expand, String folder, SortOrder sortOrder) throws ApiException {
        return getAllEvents(from, until, expand, folder, sortOrder, null);
    }

    /**
     * Retrieves all events within the specified interval in the specified folder
     *
     * @param from The starting date
     * @param until The ending date
     * @param expand Flag to expand occurrences
     * @param folder The folder identifier
     * @param sortOrder The sortOder of the events
     * @param fields The considered event fields
     * @return A {@link List} with {@link EventData}
     * @throws ApiException if an API error occurs
     */
    public List<EventData> getAllEvents(Date from, Date until, boolean expand, String folder, SortOrder sortOrder, String fields) throws ApiException {
        return getAllEvents(from, until, expand, folder, sortOrder, fields, true);
    }

    public List<EventData> getAllEvents(Date from, Date until, boolean expand, String folder, SortOrder sortOrder, String fields, boolean extendedEntities) throws ApiException {
        String sort = null;
        String order = null;
        if (sortOrder != null) {
            sort = sortOrder.getBy().name();
            order = sortOrder.isDescending() ? SortOrder.Order.DESC.name() : SortOrder.Order.ASC.name();
        }
        EventsResponse eventsResponse = userApi.getChronosApi().getAllEvents(userApi.getSession(), DateTimeUtil.getZuluDateTime(from.getTime()).getValue(), DateTimeUtil.getZuluDateTime(until.getTime()).getValue(), folder, fields, order, sort, expand, extendedEntities, false);
        return checkResponse(eventsResponse.getErrorDesc(), eventsResponse.getError(), eventsResponse.getCategories(), eventsResponse.getData());
    }

    /**
     * Lists the events with the specified identifiers
     *
     * @param ids The event identifiers
     * @return A {@link List} with {@link EventData}
     * @throws ApiException if an API error occurs
     */
    public List<EventData> listEvents(List<EventId> ids) throws ApiException {
        EventsResponse listResponse = userApi.getChronosApi().getEventList(userApi.getSession(), ids, null, Boolean.FALSE);
        return checkResponse(listResponse.getErrorDesc(), listResponse.getError(), listResponse.getCategories(), listResponse.getData());
    }

    /**
     * Deletes the event with the specified identifier
     *
     * @param eventId The {@link EventId}
     * @throws ApiException if an API error is occurred
     * @throws ChronosApiException if a Chronos API error is occurred
     */
    public void deleteEvent(EventId eventId) throws ApiException, ChronosApiException {
        ChronosMultipleCalendarResultResponse deleteResponse = userApi.getChronosApi().deleteEvent(userApi.getSession(), System.currentTimeMillis(), Collections.singletonList(eventId), null, null, EXPAND_SERIES, false);
        assertNull(deleteResponse.getErrorDesc(), deleteResponse.getError());
        forgetEventId(eventId);
        lastTimeStamp = deleteResponse.getTimestamp();
    }

    /**
     * Deletes the event with the specified identifier
     *
     * @param eventId The {@link EventId}
     * @throws ApiException if an API error is occurred
     */
    public void deleteEvent(EventId eventId, long timestamp) throws ApiException {
        ChronosMultipleCalendarResultResponse deleteResponse = userApi.getChronosApi().deleteEvent(userApi.getSession(), timestamp, Collections.singletonList(eventId), null, null, EXPAND_SERIES, false);
        assertNull(deleteResponse.getErrorDesc(), deleteResponse.getError());
        forgetEventId(eventId);
        lastTimeStamp = deleteResponse.getTimestamp();
    }

    /**
     * Updates the specified event and ignores conflicts
     *
     * @param eventData The data of the event
     * @return The updated event
     * @throws ApiException if an API error is occurred
     * @throws ChronosApiException if a Chronos API error is occurred
     */
    public EventData updateEvent(EventData eventData) throws ApiException, ChronosApiException {
        return updateEvent(eventData, false, true);
    }

    /**
     * Updates the specified event and ignores conflicts
     *
     * @param eventData The data of the event
     * @param expectException Whether an exception is expected or not
     * @param checkconflicts Whether to check for conflicts or not
     * @return The updated event
     * @throws ApiException if an API error is occurred
     * @throws ChronosApiException if a Chronos API error is occurred
     */
    public EventData updateEvent(EventData eventData, boolean expectException, boolean checkconflicts) throws ApiException, ChronosApiException {
        ChronosCalendarResultResponse updateResponse = userApi.getChronosApi().updateEvent(userApi.getSession(), getFolder(eventData), eventData.getId(), eventData, eventData.getLastModified(), null, null, checkconflicts, false, false, null, null, EXPAND_SERIES);
        return handleUpdate(updateResponse, expectException);
    }

    /**
     * Updates the specified recurrence event and ignores conflicts
     *
     * @param eventData The data of the event
     * @param the recurrence identifier
     * @return The updated event
     * @throws ApiException if an API error is occurred
     * @throws ChronosApiException if a Chronos API error is occurred
     */
    public EventData updateOccurenceEvent(EventData eventData, String recurrenceId) throws ApiException, ChronosApiException {
        return updateOccurenceEvent(eventData, recurrenceId, false);
    }

    /**
     * Updates the specified recurrence event and ignores conflicts
     *
     * @param eventData The data of the event
     * @param the recurrence identifier
     * @param expectException Whether an exception is expected or not
     * @return The updated event
     * @throws ApiException if an API error is occurred
     * @throws ChronosApiException if a Chronos API error is occurred
     */
    public EventData updateOccurenceEvent(EventData eventData, String recurrenceId, boolean expectException) throws ApiException, ChronosApiException {
        ChronosCalendarResultResponse updateResponse = userApi.getChronosApi().updateEvent(userApi.getSession(), getFolder(eventData), eventData.getId(), eventData, this.lastTimeStamp, recurrenceId, null, true, false, false, null, null, EXPAND_SERIES);
        return handleUpdate(updateResponse, expectException);
    }

    /**
     * Gets all changed events since the given timestamp (recurring events will not be expanded).
     *
     * @param since The timestamp
     * @return The {@link UpdatesResult}
     * @throws ApiException if an API error is occurred
     */
    public UpdatesResult getUpdates(Date since) throws ApiException {
        return getUpdates(since, false);
    }

    /**
     * Gets all changed events since the given timestamp.
     *
     * @param since The timestamp
     * @param expand Flag to expand any recurring events
     * @return The {@link UpdatesResult}
     * @throws ApiException if an API error is occurred
     */
    public UpdatesResult getUpdates(Date since, boolean expand) throws ApiException {
        return getUpdates(since, expand, defaultFolder);
    }

    /**
     * Gets all changed events in the specified folder since the given timestamp.
     *
     * @param since The timestamp
     * @param expand Flag to expand any recurring events
     * @param folderId The folder identifier
     * @return The {@link UpdatesResult}
     * @throws ApiException if an API error is occurred
     */
    public UpdatesResult getUpdates(Date since, boolean expand, String folderId) throws ApiException {
        return getUpdates(since, null, null, expand, folderId);
    }

    /**
     * Gets all changed events in the specified folder since the given timestamp.
     *
     * @param since The timestamp
     * @param expand Flag to expand any recurring events
     * @param folderId The folder identifier
     * @return The {@link UpdatesResult}
     * @throws ApiException if an API error is occurred
     */
    public UpdatesResult getUpdates(Date since, Date start, Date end, boolean expand, String folderId) throws ApiException {
        ChronosUpdatesResponse updatesResponse = userApi.getChronosApi().getUpdates(userApi.getSession(), folderId, since.getTime(), start != null ? UTC_DATE_FORMATTER.format(start) : null, end != null ? UTC_DATE_FORMATTER.format(end) : null, null, null, null, expand, false);
        this.lastTimeStamp = updatesResponse.getTimestamp();
        return checkResponse(updatesResponse.getErrorDesc(), updatesResponse.getError(), updatesResponse.getCategories(), updatesResponse.getData());
    }

    /**
     * Acknowledges the alarm with the specified identifier for the specified event
     *
     * @param eventId The event identifier
     * @param alarmId The alarm identifier
     * @return The updated {@link EventData} with the acknowledged alarm
     * @throws ApiException if an API error is occurred
     */
    public EventData acknowledgeAlarm(String eventId, int alarmId, String folderId) throws ApiException {
        ChronosCalendarResultResponse acknowledgeAlarm = userApi.getChronosApi().acknowledgeAlarm(userApi.getSession(), eventId, folderId != null ? folderId : defaultFolder, alarmId, false);
        CalendarResult checkResponse = checkResponse(acknowledgeAlarm.getError(), acknowledgeAlarm.getErrorDesc(), acknowledgeAlarm.getCategories(), acknowledgeAlarm.getData());
        assertEquals(1, checkResponse.getUpdated().size());
        EventData updated = checkResponse.getUpdated().get(0);
        Long acknowledged = updated.getAlarms().get(0).getAcknowledged();
        assertNotNull(acknowledged);

        return updated;
    }

    /**
     * Snoozes the alarm with the specified identifier for the specified event
     *
     * @param eventId The event identifier
     * @param alarmId The alarm identifier
     * @param snoozeTime The snooze time
     * @return The updated {@link EventData}
     * @throws ApiException if an API error is occurred
     */
    public EventData snoozeAlarm(String eventId, int alarmId, long snoozeTime, String folderId) throws ApiException {
        ChronosCalendarResultResponse snoozeResponse = userApi.getChronosApi().snoozeAlarm(userApi.getSession(), eventId, folderId != null ? folderId : defaultFolder, alarmId, snoozeTime, false);
        CalendarResult snoozeResult = checkResponse(snoozeResponse.getError(), snoozeResponse.getErrorDesc(), snoozeResponse.getCategories(), snoozeResponse.getData());
        assertEquals(1, snoozeResult.getUpdated().size());
        EventData updatedEvent = snoozeResult.getUpdated().get(0);
        assertEquals(2, updatedEvent.getAlarms().size()); // The previous snooze alarm should be replaced by a new one

        return updatedEvent;
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
        return checkResponse(triggerResponse.getError(), triggerResponse.getErrorDesc(), triggerResponse.getCategories(), triggerResponse.getData());
    }

    /**
     * Updates the attendee status of the event with the specified identifier.
     *
     * @param eventId The event identifier
     * @param attendeeAndAlarm The status of the attendee
     * @throws ApiException if an API error occurs
     * @throws ChronosApiException if a Chronos API error is occurred
     */
    public void updateAttendee(String eventId, AttendeeAndAlarm attendeeAndAlarm, boolean expectException) throws ApiException, ChronosApiException {
        ChronosCalendarResultResponse updateAttendee = userApi.getChronosApi().updateAttendee(userApi.getSession(), defaultFolder, eventId, lastTimeStamp, attendeeAndAlarm, null, false, true, false, null, null, EXPAND_SERIES);
        if (expectException) {
            assertNotNull("An error was expected", updateAttendee.getError());
            throw new ChronosApiException(updateAttendee.getCode(), updateAttendee.getError());
        }
        checkResponse(updateAttendee.getError(), updateAttendee.getErrorDesc(), updateAttendee.getCategories(), updateAttendee.getData());
        lastTimeStamp = updateAttendee.getTimestamp();
    }

    //////////////////////////////////////////////// HELPERS ///////////////////////////////////////////////////

    private JSONObject extractBody(String response) throws ChronosApiException {
        try {
            JSONObject responseData = new JSONObject(response);
            return checkResponse(responseData.optString("error", null), responseData.optString("error_desc", null), responseData.optString("categories", null), responseData.optJSONObject("data"));
        } catch (JSONException e) {
            throw new ChronosApiException("JSON_ERROR", e.getMessage());
        }
    }

    private JSONObject handleCreation(String response) throws ChronosApiException {
        try {
            JSONObject result = extractBody(response);
            JSONArray optJSONArray = result.optJSONArray("conflicts");
            if (optJSONArray == null) {
                optJSONArray = new JSONArray();
            }
            assertEquals("Found unexpected conflicts", 0, optJSONArray.length());
            JSONObject event = result.optJSONArray("created").getJSONObject(0);

            EventId eventId = new EventId();
            eventId.setId(event.optString("id"));
            eventId.setFolder(event.optString("folder"));
            rememberEventId(eventId);
            lastTimeStamp = event.optLong("timestamp");

            return event;
        } catch (JSONException e) {
            throw new ChronosApiException("JSON_ERROR", e.getMessage());
        }
    }

    private JSONObject handleUpdate(String response) throws ChronosApiException {
        try {
            JSONObject result = extractBody(response);
            JSONArray array = result.optJSONArray("updated");
            assertTrue(array.length() == 1);
            lastTimeStamp = array.getJSONObject(0).optLong("timestamp");
            return array.getJSONObject(0);
        } catch (JSONException e) {
            throw new ChronosApiException("JSON_ERROR", e.getMessage());
        }
    }

    /**
     * Handles the result response of an event creation
     *
     * @param createEvent The result
     * @return The created event
     */
    private EventData handleCreation(ChronosCalendarResultResponse createEvent) {
        CalendarResult result = checkResponse(createEvent.getError(), createEvent.getErrorDesc(), createEvent.getCategories(), createEvent.getData());
        assertEquals("Found unexpected conflicts", 0, result.getConflicts().size());
        EventData event = result.getCreated().get(0);

        EventId eventId = new EventId();
        eventId.setId(event.getId());
        eventId.setFolder(event.getFolder());
        rememberEventId(eventId);
        lastTimeStamp = createEvent.getTimestamp();

        return event;
    }

    /**
     * Handles the result response of an update event
     *
     * @param updateEvent The result
     * @return The updated event
     * @throws ChronosApiException if a Chronos API error is occurred
     */
    private EventData handleUpdate(ChronosCalendarResultResponse updateEvent, boolean expectException) throws ChronosApiException {
        if (expectException) {
            assertNotNull("An error was expected", updateEvent.getError());
            throw new ChronosApiException(updateEvent.getCode(), updateEvent.getError());
        }
        CalendarResult calendarResult = checkResponse(updateEvent.getErrorDesc(), updateEvent.getError(), updateEvent.getCategories(), updateEvent.getData());
        List<EventData> updates = calendarResult.getUpdated();
        assertEquals(1, updates.size());
        lastTimeStamp = updates.get(0).getTimestamp();
        return updates.get(0);
    }

    /**
     * Keeps track of the specified {@link EventId} for the specified user
     *
     * @param userApi The {@link UserApi}
     * @param eventId The {@link EventId}
     */
    public void rememberEventId(EventId eventId) {
        if (eventIds == null) {
            eventIds = new ArrayList<>();
        }
        eventIds.add(eventId);
    }

    /**
     * Keeps track of the specified list of {@link EventId} for the specified user
     *
     * @param eventIds A list of {@link EventId}
     */
    public void rememberEventIds(List<EventId> eventIdList) {
        if (eventIds == null) {
            eventIds = new ArrayList<>();
        }
        eventIds.addAll(eventIdList);
    }

    /**
     * Removes the specified {@link EventId} for the specified user from the cache
     *
     * @param userApi The {@link UserApi}
     * @param eventId The {@link EventId}
     */
    protected void forgetEventId(EventId eventId) {
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

    public static boolean isSeriesMaster(EventData event) {
        return null != event && null != event.getId() && event.getId().equals(event.getSeriesId()) && null == event.getRecurrenceId();
    }

}
