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

package com.openexchange.chronos.provider.caching.basic.handlers;

import static com.openexchange.java.Autoboxing.L;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultEventsResult;
import com.openexchange.chronos.common.DefaultUpdatesResult;
import com.openexchange.chronos.common.SearchUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.caching.basic.BasicCachingCalendarAccess;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.EventsResult;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.service.SortOrder;
import com.openexchange.chronos.service.UpdatesResult;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageUtilities;
import com.openexchange.chronos.storage.EventStorage;
import com.openexchange.chronos.storage.operation.OSGiCalendarStorageOperation;
import com.openexchange.exception.OXException;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.session.Session;
import com.openexchange.tools.arrays.Arrays;

/**
 * {@link SyncHandler}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SyncHandler extends AbstractExtensionHandler {

    private static final String IGNORE_DELETED = "deleted";
    private static final String IGNORE_CHANGED = "changed";

    /**
     * Initialises a new {@link SyncHandler}.
     */
    public SyncHandler(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        super(session, account, parameters);
    }

    /**
     * Gets lists of new and updated as well as deleted events since a specific timestamp.
     *
     * @param updatedSince The timestamp since when the updates should be retrieved
     * @return The updates result yielding lists of new/modified and deleted events
     * @throws OXException if the operation fails
     */
    public UpdatesResult getUpdatedEvents(long updatedSince) throws OXException {
        return new OSGiCalendarStorageOperation<UpdatesResult>(Services.getServiceLookup(), getSession().getContextId(), getAccount().getAccountId()) {

            @Override
            protected UpdatesResult call(CalendarStorage storage) throws OXException {
                SearchOptions searchOptions = getSearchOptions();
                SearchTerm<?> searchTerm = createSearchTerm(updatedSince);

                EventField[] eventFields = getEventFields();

                List<Event> newAndUpdated = null;
                String[] ignore = getParameters().get(CalendarParameters.PARAMETER_IGNORE, String[].class);

                CalendarStorageUtilities utilities = storage.getUtilities();
                EventStorage eventStorage = storage.getEventStorage();

                if (false == Arrays.contains(ignore, IGNORE_CHANGED)) {
                    newAndUpdated = eventStorage.searchEvents(searchTerm, searchOptions, null);
                    newAndUpdated = postProcess(utilities.loadAdditionalEventData(getSession().getUserId(), newAndUpdated, eventFields));
                }

                List<Event> tombstoneEvents = null;
                if (false == Arrays.contains(ignore, IGNORE_DELETED)) {
                    tombstoneEvents = eventStorage.searchEventTombstones(searchTerm, searchOptions, null);
                    tombstoneEvents = postProcess(utilities.loadAdditionalEventTombstoneData(tombstoneEvents, eventFields));
                }

                return new DefaultUpdatesResult(newAndUpdated, tombstoneEvents);
            }
        }.executeQuery();
    }

    /**
     * Gets the sequence number, which is the highest highest timestamp of all contained items.
     *
     * @return The sequence number
     */
    public long getSequenceNumber() throws OXException {
        return new OSGiCalendarStorageOperation<Long>(Services.getServiceLookup(), getSession().getContextId(), getAccount().getAccountId()) {

            @Override
            protected Long call(CalendarStorage storage) throws OXException {
                long timestamp = 0L;
                SearchOptions sortOptions = new SearchOptions().addOrder(SortOrder.getSortOrder(EventField.TIMESTAMP, SortOrder.Order.DESC)).setLimits(0, 1);
                EventField[] fields = { EventField.TIMESTAMP };
                List<Event> events = storage.getEventStorage().searchEvents(null, sortOptions, fields);
                if (0 < events.size() && timestamp < events.get(0).getTimestamp()) {
                    timestamp = events.get(0).getTimestamp();
                }
                List<Event> deletedEvents = storage.getEventStorage().searchEventTombstones(null, sortOptions, fields);
                if (0 < deletedEvents.size() && timestamp < deletedEvents.get(0).getTimestamp()) {
                    timestamp = deletedEvents.get(0).getTimestamp();
                }
                return timestamp;
            }
        }.executeQuery();
    }

    /**
     * Resolve the specified resource
     *
     * @param resourceNameThe name of the resource to resolve
     * @return A {@link List} with the resolved {@link Event}s
     * @throws OXException if an error is occurred
     */
    public List<Event> resolveResource(String resourceName) throws OXException {
        return new OSGiCalendarStorageOperation<List<Event>>(Services.getServiceLookup(), getSession().getContextId(), getAccount().getAccountId()) {

            @Override
            protected List<Event> call(CalendarStorage storage) throws OXException {
                return resolveResource(storage, resourceName);
            }
        }.executeQuery();
    }

    public Map<String, EventsResult> resolveResources(List<String> resourceNames) throws OXException {
        return new OSGiCalendarStorageOperation<Map<String, EventsResult>>(Services.getServiceLookup(), getSession().getContextId(), getAccount().getAccountId()) {

            @Override
            protected Map<String, EventsResult> call(CalendarStorage storage) throws OXException {
                Map<String, EventsResult> eventsResults = new HashMap<String, EventsResult>();
                for (String resourceName : resourceNames) {
                    try {
                        List<Event> events = resolveResource(storage, resourceName);
                        if (null == events || 0 == events.size()) {
                            OXException e = CalendarExceptionCodes.EVENT_NOT_FOUND_IN_FOLDER.create(BasicCachingCalendarAccess.FOLDER_ID, resourceName);
                            eventsResults.put(resourceName, new DefaultEventsResult(e));
                        } else {
                            eventsResults.put(resourceName, new DefaultEventsResult(events));
                        }
                    } catch (OXException e) {
                        eventsResults.put(resourceName, new DefaultEventsResult(e));
                    }
                }
                return eventsResults;
            }
        }.executeQuery();
    }

    /**
     * Compiles the {@link SearchTerm}
     *
     * @param updatedSince The updated since timestamp
     * @return The compiled {@link SearchTerm}
     */
    SearchTerm<?> createSearchTerm(long updatedSince) {
        return SearchUtils.getSearchTerm(EventField.TIMESTAMP, SingleOperation.GREATER_THAN, L(updatedSince));
    }

    List<Event> resolveResource(CalendarStorage storage, String resourceName) throws OXException {
        CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.OR);
        searchTerm.addSearchTerm(SearchUtils.getSearchTerm(EventField.UID, SingleOperation.EQUALS, resourceName));
        searchTerm.addSearchTerm(SearchUtils.getSearchTerm(EventField.FILENAME, SingleOperation.EQUALS, resourceName));

        EventField[] eventFields = getEventFields();

        List<Event> resolvedEvents = storage.getEventStorage().searchEvents(searchTerm, getSearchOptions(), eventFields);
        storage.getUtilities().loadAdditionalEventData(getSession().getUserId(), resolvedEvents, eventFields);

        // Ensure that the series master event is the first element in the returned list
        return CalendarUtils.sortSeriesMasterFirst(resolvedEvents);
    }

}
