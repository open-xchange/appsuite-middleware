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

package com.openexchange.chronos.provider.caching.internal.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.provider.caching.CachingCalendarAccess;
import com.openexchange.chronos.provider.caching.internal.handler.utils.HandlerHelper;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.EventUpdates;
import com.openexchange.exception.OXException;

/**
 * {@link CachingExecutor}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class CachingExecutor {

    private static final EventField[] FIELDS_TO_IGNORE = new EventField[] { EventField.CREATED_BY, EventField.FOLDER_ID, EventField.ID, EventField.CALENDAR_USER, EventField.CREATED, EventField.MODIFIED_BY, EventField.EXTENDED_PROPERTIES, EventField.TIMESTAMP };
    private static final EventField[] EQUALS_IDENTIFIER = new EventField[] { EventField.UID, EventField.RECURRENCE_ID };

    private CachingHandler cachingHandler;
    private CachingCalendarAccess cachingCalendarAccess;

    public CachingExecutor(CachingCalendarAccess cachingCalendarAccess, CachingHandler cachingHandler) {
        this.cachingCalendarAccess = cachingCalendarAccess;
        this.cachingHandler = cachingHandler;

    }

    public List<Event> doIt(String folderId, String eventId, RecurrenceId recurrenceId) throws OXException {
        List<Event> externalEvents = cachingHandler.getExternalEvents(folderId);
        List<Event> persistedEvents = cachingHandler.getPersistedEvents(folderId);

        EventUpdates diff = generateEventDiff(persistedEvents, externalEvents);
        if (diff.isEmpty()) {
            cachingHandler.updateLastUpdated();
            Event search = cachingHandler.search(folderId, eventId, recurrenceId);
            if (search == null) {
                return Collections.emptyList();
            }
            return Collections.singletonList(search);
        }

        cachingHandler.persist(diff);
        cachingHandler.updateLastUpdated();

        Event search = cachingHandler.search(folderId, eventId, recurrenceId);
        if (search == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(search);
    }

    public List<Event> doIt(String folderId) throws OXException {
        List<Event> externalEvents = cachingHandler.getExternalEvents(folderId);
        List<Event> persistedEvents = cachingHandler.getPersistedEvents(folderId);

        EventUpdates diff = generateEventDiff(persistedEvents, externalEvents);
        if (diff.isEmpty()) {
            cachingHandler.updateLastUpdated();
            return cachingHandler.search(folderId);
        }

        cachingHandler.persist(diff);
        cachingHandler.updateLastUpdated();

        return cachingHandler.search(folderId);
    }

    public List<Event> doIt(List<EventID> eventIds) throws OXException {
        Map<String, List<EventID>> sortEventIDsPerFolderId = HandlerHelper.sortEventIDsPerFolderId(eventIds);

        List<Event> externalEvents = new ArrayList<>();
        for (String folderId : sortEventIDsPerFolderId.keySet()) {
            List<Event> externalEventsForFolder = cachingHandler.getExternalEvents(folderId);
            if (externalEventsForFolder != null && !externalEventsForFolder.isEmpty()) {
                externalEvents.addAll(externalEventsForFolder);
            }
        }

        List<Event> persistedEvents = cachingHandler.getPersistedEvents(eventIds);

        EventUpdates diff = generateEventDiff(persistedEvents, externalEvents);
        if (diff.isEmpty()) {
            cachingHandler.updateLastUpdated();
            return cachingHandler.search(eventIds);
        }

        cachingHandler.persist(diff);
        cachingHandler.updateLastUpdated();

        return cachingHandler.search(eventIds);
    }

    private EventUpdates generateEventDiff(List<Event> persistedEvents, List<Event> updatedEvents) throws OXException {
        return CalendarUtils.getEventUpdates(persistedEvents, updatedEvents, false, FIELDS_TO_IGNORE, EQUALS_IDENTIFIER);
    }
}
