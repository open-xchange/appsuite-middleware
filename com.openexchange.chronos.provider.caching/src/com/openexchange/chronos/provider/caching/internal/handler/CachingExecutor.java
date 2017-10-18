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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.caching.CachingCalendarAccess;
import com.openexchange.chronos.provider.caching.DiffAwareExternalCalendarResult;
import com.openexchange.chronos.provider.caching.ExternalCalendarResult;
import com.openexchange.chronos.provider.caching.internal.handler.utils.EmptyUidUpdates;
import com.openexchange.chronos.provider.caching.internal.handler.utils.ResultCollector;
import com.openexchange.chronos.service.EventUpdates;
import com.openexchange.exception.OXException;

/**
 * The {@link CachingExecutor} ensures a generic execution of the caching process based on the provided {@link CachingHandler}.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class CachingExecutor {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CachingExecutor.class);

    private static final EventField[] FIELDS_TO_IGNORE = new EventField[] { EventField.CREATED_BY, EventField.FOLDER_ID, EventField.ID, EventField.CALENDAR_USER, EventField.CREATED, EventField.MODIFIED_BY, EventField.EXTENDED_PROPERTIES, EventField.TIMESTAMP };
    private static final EventField[] EQUALS_IDENTIFIER = new EventField[] { EventField.UID, EventField.RECURRENCE_ID };

    private final Set<FolderUpdateState> executionList;
    private final CachingCalendarAccess cachingCalendarAccess;

    public CachingExecutor(CachingCalendarAccess cachingCalendarAccess, Set<FolderUpdateState> executionList) {
        this.cachingCalendarAccess = cachingCalendarAccess;
        this.executionList = executionList;
    }

    public ResultCollector cache(List<OXException> warnings) {
        ResultCollector resultCollector = new ResultCollector(this.cachingCalendarAccess);

        if (executionList == null) {
            LOG.warn("Nothing to cache as the provided execution list is null.");
            return resultCollector;
        }
        for (FolderUpdateState folderUpdateState : executionList) {
            CachingHandler cachingHandler = CachingHandlerFactory.getInstance().get(folderUpdateState.getType(), cachingCalendarAccess);
            String calendarFolderId = folderUpdateState.getFolderId();

            try {
                ExternalCalendarResult externalCalendarResult = cachingHandler.getExternalEvents(calendarFolderId);
                if (!externalCalendarResult.isUpToDate()) {
                    List<Event> existingEvents = cachingHandler.getExistingEvents(calendarFolderId);
                    EventUpdates diff = null;
                    Map<String, Event> cachedEvents = new HashMap<>(existingEvents.size());
                    for(Event eve: existingEvents){
                        cachedEvents.put(eve.getUid(), eve);
                    }

                    if (externalCalendarResult instanceof DiffAwareExternalCalendarResult) {
                        diff = ((DiffAwareExternalCalendarResult) externalCalendarResult).calculateDiff(cachedEvents);
                    } else {

                        List<Event> externalEvents = externalCalendarResult.getEvents();

                        boolean containsUID = containsUid(externalEvents);
                        if (containsUID) {
                            diff = generateEventDiff(existingEvents, externalEvents);
                        } else {
                            //FIXME generate reproducible UID for upcoming refreshes
                            diff = new EmptyUidUpdates(existingEvents, externalEvents);
                        }
                    }

                    if (!diff.isEmpty()) {
                        cachingHandler.persist(calendarFolderId, diff);
                    }
                }
                cachingHandler.updateLastUpdated(calendarFolderId, System.currentTimeMillis());
            } catch (OXException e) {
                LOG.info("Unable to update cache for folder {} in account {}: {}", calendarFolderId, cachingCalendarAccess.getAccount().getAccountId(), e.getMessage(), e);
                warnings.add(e);

                handleInternally(cachingHandler, calendarFolderId, e);
                this.cachingCalendarAccess.handleExceptions(calendarFolderId, e);
            }
        }
        return resultCollector;
    }

    private void handleInternally(CachingHandler cachingHandler, String calendarFolderId, OXException e) {
        if (e.getExceptionCode() == null || e.getExceptionCode().equals(CalendarExceptionCodes.AUTH_FAILED.create(""))) {
            return;
        }
        long timeoutInMillis = TimeUnit.MINUTES.toMillis(this.cachingCalendarAccess.getExternalRequestTimeout());
        long nextProcessingAfter = System.currentTimeMillis() + timeoutInMillis;
        cachingHandler.updateLastUpdated(calendarFolderId, nextProcessingAfter);
    }

    /**
     * Returns if all provided {@link Event}s do contain a UID
     *
     * @param events A list of {@link Event}s to check for the UID
     * @return <code>true</code> if all {@link Event}s do have a UID; <code>false</code> if at least one {@link Event} is missing the UID field
     */
    private boolean containsUid(List<Event> events) {
        for (Event event : events) {
            if (!event.containsUid()) {
                return false;
            }
        }
        return true;
    }

    private EventUpdates generateEventDiff(List<Event> persistedEvents, List<Event> updatedEvents) throws OXException {
        return CalendarUtils.getEventUpdates(persistedEvents, updatedEvents, true, FIELDS_TO_IGNORE, EQUALS_IDENTIFIER);
    }
}
