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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.ajax.chronos.UserApi;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.CalendarResult;
import com.openexchange.testing.httpclient.models.ChronosCalendarResultResponse;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.EventId;
import com.openexchange.testing.httpclient.models.EventResponse;

/**
 * {@link EventManager}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class EventManager extends AbstractManager {

    private final UserApi userApi;
    private final String defaultFolder;

    private Map<UserApi, List<EventId>> eventIds;
    private long lastTimeStamp;

    /**
     * Initialises a new {@link EventManager}.
     */
    public EventManager(UserApi userApi, String defaultFolder) {
        super();
        this.userApi = userApi;
        this.defaultFolder = defaultFolder;
    }

    /**
     * Creates an event
     * 
     * @param eventData The data of the event
     * @return The created {@link EventData}
     * @throws ApiException if an API error is occurred
     */
    public EventData createEvent(EventData eventData) throws ApiException {
        ChronosCalendarResultResponse createEvent = userApi.getApi().createEvent(userApi.getSession(), defaultFolder, EventFactory.createSingleTwoHourEvent(userApi.getCalUser(), userApi.getUser().getLogin(), "testCreateSingle"), false, false);
        EventData event = handleCreation(createEvent);
        return event;
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
     * 
     * @param eventId
     * @param expectException
     * @return
     * @throws ApiException
     * @throws ChronosApiException if a Chronos API error is occurred
     */
    public EventData getEvent(String eventId, boolean expectException) throws ApiException, ChronosApiException {
        EventResponse eventResponse = userApi.getApi().getEvent(userApi.getSession(), eventId, defaultFolder, null, null);
        if (expectException) {
            assertNotNull(eventResponse.getError());
            throw new ChronosApiException(eventResponse.getCode(), eventResponse.getError());
        }
        assertNull(eventResponse.getError(), eventResponse.getError());
        assertNotNull(eventResponse.getData());
        return eventResponse.getData();
    }

    /**
     * Deletes the event with the specified identifier
     * 
     * @param eventId The {@link EventId}
     * @throws ApiException if an API error is occurred
     */
    public void deleteEvent(EventId eventId) throws ApiException {
        ChronosCalendarResultResponse deleteResponse = userApi.getApi().deleteEvent(userApi.getSession(), System.currentTimeMillis(), Collections.singletonList(eventId));
        assertNull(deleteResponse.getErrorDesc(), deleteResponse.getError());
    }

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
        this.setLastTimestamp(createEvent.getTimestamp());

        return event;
    }

    /**
     * Sets the last timestamp
     * 
     * @param timestamp the last timestamp to set
     */
    public void setLastTimestamp(long timestamp) {
        this.lastTimeStamp = timestamp;
    }

    /**
     * Gets the last timestamp
     * 
     * @return the last timestamp
     */
    public long getLastTimestamp() {
        return lastTimeStamp;
    }

    /**
     * Keeps track of the specified {@link EventId} for the specified user
     * 
     * @param userApi The {@link UserApi}
     * @param eventId The {@link EventId}
     */
    protected void rememberEventId(UserApi userApi, EventId eventId) {
        if (eventIds == null) {
            eventIds = new HashMap<>();
        }
        if (!eventIds.containsKey(userApi)) {
            eventIds.put(userApi, new ArrayList<>(1));
        }
        eventIds.get(userApi).add(eventId);
    }

}
