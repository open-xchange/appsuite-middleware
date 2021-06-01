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

package com.openexchange.chronos.impl;

import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.L;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.performer.CountEventsPerformer;
import com.openexchange.chronos.impl.performer.ForeignEventsPerformer;
import com.openexchange.chronos.impl.performer.ResolvePerformer;
import com.openexchange.chronos.service.CalendarInterceptor;
import com.openexchange.chronos.service.CalendarServiceUtilities;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.EventsResult;
import com.openexchange.chronos.service.RecurrenceData;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.ServiceSet;
import com.openexchange.quota.Quota;

/**
 * {@link CalendarServiceUtilitiesImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarServiceUtilitiesImpl implements CalendarServiceUtilities {

    private final ServiceSet<CalendarInterceptor> interceptors;

    /**
     * Initializes a new {@link CalendarServiceUtilitiesImpl}.
     * 
     * @param interceptors The calendar interceptor service set to use
     */
    public CalendarServiceUtilitiesImpl(ServiceSet<CalendarInterceptor> interceptors) {
        super();
        this.interceptors = interceptors;
    }

    @Override
    public Set<CalendarInterceptor> getInterceptors() {
        return interceptors;
    }

    @Override
    public boolean containsForeignEvents(CalendarSession session, String folderId) throws OXException {
        return new InternalCalendarStorageOperation<Boolean>(session) {

            @Override
            protected Boolean execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return B(new ForeignEventsPerformer(session, storage).perform(folderId));
            }
        }.executeQuery().booleanValue();
    }

    @Override
    public long countEvents(CalendarSession session, String folderId) throws OXException {
        return new InternalCalendarStorageOperation<Long>(session) {

            @Override
            protected Long execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return L(new CountEventsPerformer(session, storage).perform(folderId));
            }
        }.executeQuery().intValue();
    }

    @Override
    public String resolveByUID(CalendarSession session, String uid) throws OXException {
        return new InternalCalendarStorageOperation<String>(session) {

            @Override
            protected String execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new ResolvePerformer(session, storage).resolveByUid(uid);
            }
        }.executeQuery();
    }

    @Override
    public String resolveByUID(CalendarSession session, String uid, int calendarUserId) throws OXException {
        return new InternalCalendarStorageOperation<String>(session) {

            @Override
            protected String execute(CalendarSession session, CalendarStorage storage) throws OXException {
                EventID eventID = new ResolvePerformer(session, storage).resolveByUid(uid, calendarUserId);
                return null == eventID ? null : eventID.getObjectID();
            }
        }.executeQuery();
    }

    @Override
    public List<Event> resolveEventsByUID(CalendarSession session, String uid, int calendarUserId) throws OXException {
        return new InternalCalendarStorageOperation<List<Event>>(session) {

            @Override
            protected List<Event> execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new ResolvePerformer(session, storage).resolveEventsByUID(uid, calendarUserId);
            }
        }.executeQuery();
    }

    @Override
    public String resolveFolderIdByUID(CalendarSession session, String uid, int calendarUserId, boolean fallbackToDefault) throws OXException {
        return new InternalCalendarStorageOperation<String>(session) {

            @Override
            protected String execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new ResolvePerformer(session, storage).resolveFolderIdByUID(uid, calendarUserId, fallbackToDefault);
            }
        }.executeQuery();
    }

    @Override
    public String resolveByUID(CalendarSession session, String uid, RecurrenceId recurrenceId, int calendarUserId) throws OXException {
        return new InternalCalendarStorageOperation<String>(session) {

            @Override
            protected String execute(CalendarSession session, CalendarStorage storage) throws OXException {
                EventID eventID = new ResolvePerformer(session, storage).resolveByUid(uid, recurrenceId, calendarUserId);
                return null == eventID ? null : eventID.getObjectID();
            }
        }.executeQuery();
    }

    @Override
    public Quota[] getQuotas(CalendarSession session) throws OXException {
        return new InternalCalendarStorageOperation<Quota[]>(session) {

            @Override
            protected Quota[] execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new Quota[] { Utils.getQuota(session, storage) };
            }
        }.executeQuery();
    }

    @Override
    public RecurrenceData loadRecurrenceData(CalendarSession session, String seriesId) throws OXException {
        return new InternalCalendarStorageOperation<RecurrenceData>(session) {

            @Override
            protected RecurrenceData execute(CalendarSession session, CalendarStorage storage) throws OXException {
                EventField[] recurrenceDataFields = new EventField[] {
                    EventField.ID, EventField.SERIES_ID, EventField.RECURRENCE_RULE, EventField.START_DATE, EventField.END_DATE,
                    EventField.RECURRENCE_DATES, EventField.DELETE_EXCEPTION_DATES, EventField.CHANGE_EXCEPTION_DATES
                };
                Event seriesMaster = storage.getEventStorage().loadEvent(seriesId, recurrenceDataFields);
                if (null == seriesMaster || false == isSeriesMaster(seriesMaster)) {
                    throw CalendarExceptionCodes.EVENT_NOT_FOUND.create(seriesId);
                }
                return new DefaultRecurrenceData(seriesMaster);
            }
        }.executeQuery();
    }

    @Override
    public Event resolveByID(CalendarSession session, String id, Integer sequence) throws OXException {
        return new InternalCalendarStorageOperation<Event>(session) {

            @Override
            protected Event execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new ResolvePerformer(session, storage).resolveById(id, sequence);
            }
        }.executeQuery();
    }
    
    @Override
    public Event resolveByID(CalendarSession session, String id, Integer sequence, int calendarUserId) throws OXException {
        return new InternalCalendarStorageOperation<Event>(session) {
            
            @Override
            protected Event execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new ResolvePerformer(session, storage).resolveById(id, sequence, calendarUserId);
            }
        }.executeQuery();
    }

    @Override
    public List<Event> resolveResource(CalendarSession session, String folderId, String resourceName) throws OXException {
        return new InternalCalendarStorageOperation<List<Event>>(session) {

            @Override
            protected List<Event> execute(CalendarSession session, CalendarStorage storage) throws OXException {
                EventsResult eventsResult = new ResolvePerformer(session, storage).resolve(folderId, resourceName);
                if (null != eventsResult && null != eventsResult.getEvents() && 0 < eventsResult.getEvents().size()) {
                    return eventsResult.getEvents();
                }
                return null;
            }
        }.executeQuery();
    }

    @Override
    public Map<String, EventsResult> resolveResources(CalendarSession session, String folderId, List<String> resourceNames) throws OXException {
        return new InternalCalendarStorageOperation<Map<String, EventsResult>>(session) {

            @Override
            protected Map<String, EventsResult> execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new ResolvePerformer(session, storage).resolve(folderId, resourceNames);
            }
        }.executeQuery();
    }

}
