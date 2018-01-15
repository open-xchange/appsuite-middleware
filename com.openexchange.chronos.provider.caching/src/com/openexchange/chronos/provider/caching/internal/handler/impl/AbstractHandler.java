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

package com.openexchange.chronos.provider.caching.internal.handler.impl;

import static com.openexchange.chronos.common.CalendarUtils.getEventsByUID;
import static com.openexchange.chronos.common.CalendarUtils.getSearchTerm;
import static com.openexchange.chronos.common.CalendarUtils.sortSeriesMasterFirst;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.json.JSONObject;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ResourceId;
import com.openexchange.chronos.provider.caching.CachingCalendarAccess;
import com.openexchange.chronos.provider.caching.ExternalCalendarResult;
import com.openexchange.chronos.provider.caching.internal.CachingCalendarAccessConstants;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.chronos.provider.caching.internal.handler.CachingHandler;
import com.openexchange.chronos.provider.caching.internal.handler.utils.HandlerHelper;
import com.openexchange.chronos.provider.caching.internal.handler.utils.TruncationAwareCalendarStorage;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.Strings;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractHandler}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public abstract class AbstractHandler implements CachingHandler {

    private static final List<EventField> IGNORED_FIELDS = Arrays.asList(EventField.ATTACHMENTS);

    protected final CachingCalendarAccess cachedCalendarAccess;

    public AbstractHandler(CachingCalendarAccess cachedCalendarAccess) {
        this.cachedCalendarAccess = cachedCalendarAccess;
    }

    /**
     * Gets the calendar user representing the internal user associated with the underlying calendar account.
     *
     * @return The calendar user
     */
    public CalendarUser getCalendarUser() {
        ServerSession session = cachedCalendarAccess.getSession();
        CalendarUser calendarUser = new CalendarUser();
        calendarUser.setEntity(session.getUserId());
        calendarUser.setUri(ResourceId.forUser(session.getContextId(), session.getUserId()));
        return calendarUser;
    }

    protected CalendarStorage initStorage(DBProvider dbProvider) throws OXException {
        return Services.getService(CalendarStorageFactory.class).create(this.cachedCalendarAccess.getSession().getContext(), this.cachedCalendarAccess.getAccount().getAccountId(), null, dbProvider, DBTransactionPolicy.NO_TRANSACTIONS);
    }

    @Override
    public void updateLastUpdated(String folderId, long timestamp) {
        JSONObject folderConfig = getFolderCachingConfiguration(folderId);
        folderConfig.putSafe(CachingCalendarAccessConstants.LAST_UPDATE, Long.valueOf(timestamp));
    }

    protected JSONObject getFolderCachingConfiguration(String folderId) {
        JSONObject internalConfig = this.cachedCalendarAccess.getAccount().getInternalConfiguration();
        JSONObject caching = internalConfig.optJSONObject(CachingCalendarAccessConstants.CACHING);
        if (caching == null) {
            caching = new JSONObject();
            internalConfig.putSafe(CachingCalendarAccessConstants.CACHING, caching);
        }
        JSONObject folderConfig = caching.optJSONObject(folderId);
        if (folderConfig == null) {
            folderConfig = new JSONObject();
            caching.putSafe(folderId, folderConfig);
        }
        return folderConfig;
    }
    
    protected List<Event> getExistingEventsInFolder(String folderId) throws OXException {
        DatabaseService dbService = Services.getService(DatabaseService.class);
        Connection readConnection = null;

        Context context = this.cachedCalendarAccess.getSession().getContext();
        try {
            readConnection = dbService.getReadOnly(context);
            CalendarStorage calendarStorage = initStorage(new SimpleDBProvider(readConnection, null));
            SearchTerm<?> searchTerm = getSearchTerm(EventField.FOLDER_ID, SingleOperation.EQUALS, folderId);
            EventField[] fields = getFields();
            List<Event> events = calendarStorage.getEventStorage().searchEvents(searchTerm, null, fields);

            return calendarStorage.getUtilities().loadAdditionalEventData(this.cachedCalendarAccess.getAccount().getUserId(), events, fields);
        } finally {
            if (null != readConnection) {
                dbService.backReadOnly(context, readConnection);
            }
        }
    }

    protected ExternalCalendarResult getAndPrepareExtEvents(String folderId) throws OXException {
        ExternalCalendarResult externalCalendarResult = this.cachedCalendarAccess.getAllEvents(folderId);
        List<Event> extEventsInFolder = externalCalendarResult.getEvents();
        HandlerHelper.setFolderId(extEventsInFolder, folderId);

        return externalCalendarResult;
    }

    protected void create(String folderId, TruncationAwareCalendarStorage calendarStorage, List<Event> externalEvents) throws OXException {
        if (!externalEvents.isEmpty()) {
            Map<String, List<Event>> extEventsByUID = getEventsByUID(externalEvents, true);
            for (Entry<String, List<Event>> event : extEventsByUID.entrySet()) {
                create(folderId, calendarStorage, event);
            }
        }
    }

    protected void create(String folderId, TruncationAwareCalendarStorage calendarStorage, Entry<String, List<Event>> entry) throws OXException {
        Date now = new Date();
        List<Event> events = sortSeriesMasterFirst(entry.getValue());

        insertEvents(folderId, calendarStorage, now, events.toArray(new Event[events.size()]));
    }

    protected void insertEvents(String folderId, TruncationAwareCalendarStorage calendarStorage, Date now, Event... lEvents) throws OXException {
        if (null == lEvents || 0 == lEvents.length) {
            return;
        }
        List<Event> events = Arrays.asList(lEvents);

        String id = calendarStorage.getEventStorage().nextId();
        Event importedEvent = applyDefaults(folderId, events.get(0), now);
        importedEvent.setId(id);
        importedEvent.setCalendarUser(getCalendarUser());
        if (Strings.isNotEmpty(importedEvent.getRecurrenceRule())) {
            importedEvent.setSeriesId(id);
        }
        calendarStorage.insertEvent(importedEvent);
        List<Attendee> attendees = importedEvent.getAttendees();
        if (null != attendees && !attendees.isEmpty()) {
            calendarStorage.insertAttendees(id, attendees);
        }

        if (null != importedEvent.getAlarms() && !importedEvent.getAlarms().isEmpty()) {
            for (Alarm alarm : importedEvent.getAlarms()) {
                alarm.setId(calendarStorage.getAlarmStorage().nextId());
            }
            calendarStorage.getAlarmStorage().insertAlarms(importedEvent, this.cachedCalendarAccess.getSession().getUserId(), importedEvent.getAlarms());
        }

        if (1 < events.size()) {
            for (int i = 1; i < events.size(); i++) {
                Event importedChangeException = applyDefaults(folderId, events.get(i), now);
                importedChangeException.setSeriesId(id);
                importedChangeException.setId(calendarStorage.getEventStorage().nextId());
                calendarStorage.insertEvent(importedChangeException);
                List<Attendee> changeExceptionAttendees = importedChangeException.getAttendees();
                if (null != changeExceptionAttendees && !changeExceptionAttendees.isEmpty()) {
                    calendarStorage.insertAttendees(importedChangeException.getId(), changeExceptionAttendees);
                }
                if (null != importedChangeException.getAlarms() && !importedChangeException.getAlarms().isEmpty()) {
                    for (Alarm alarm : importedChangeException.getAlarms()) {
                        alarm.setId(calendarStorage.getAlarmStorage().nextId());
                    }
                    calendarStorage.getAlarmStorage().insertAlarms(importedChangeException, this.cachedCalendarAccess.getSession().getUserId(), importedChangeException.getAlarms());
                }
            }
        }
    }

    private Event applyDefaults(String folderId, Event importedEvent, Date now) {
        importedEvent.setCalendarUser(getCalendarUser());
        importedEvent.setTimestamp(now.getTime());
        importedEvent.setFolderId(folderId);
        return importedEvent;
    }

    protected EventField[] getFields() {
        EventField[] all = EventField.values();

        Set<EventField> fields = new HashSet<EventField>();
        fields.addAll(Arrays.asList(all));
        fields.removeAll(IGNORED_FIELDS);
        return fields.toArray(new EventField[fields.size()]);
    }
}
