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

package com.openexchange.chronos.provider.caching.internal.response;

import static com.openexchange.chronos.common.CalendarUtils.getFlags;
import static com.openexchange.chronos.common.CalendarUtils.getSearchTerm;
import java.util.ArrayList;
import java.util.List;
import com.google.common.collect.Lists;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.provider.caching.CachingCalendarAccess;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.operation.OSGiCalendarStorageOperation;
import com.openexchange.exception.OXException;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;

/**
 * {@link FolderEventsResponseGenerator}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class FolderEventsResponseGenerator extends ResponseGenerator {

    final String folderId;

    public FolderEventsResponseGenerator(CachingCalendarAccess cachedCalendarAccess, String folderId) {
        super(cachedCalendarAccess);
        this.folderId = folderId;
    }

    public List<Event> generate() throws OXException {
        return new OSGiCalendarStorageOperation<List<Event>>(Services.getServiceLookup(), this.cachedCalendarAccess.getSession().getContext().getContextId(), this.cachedCalendarAccess.getAccount().getAccountId()) {

            @Override
            protected List<Event> call(CalendarStorage storage) throws OXException {
                SearchTerm<?> searchTerm = getSearchTerm(EventField.FOLDER_ID, SingleOperation.EQUALS, getFolderId());
                CalendarParameters parameters = cachedCalendarAccess.getParameters();
                EventField[] fields = getFields(parameters.get(CalendarParameters.PARAMETER_FIELDS, EventField[].class));
                SearchOptions searchOptions = new SearchOptions(parameters);
                List<Event> events = storage.getEventStorage().searchEvents(searchTerm, searchOptions, fields);
                List<Event> enhancedEvents = storage.getUtilities().loadAdditionalEventData(cachedCalendarAccess.getAccount().getUserId(), events, fields);
                List<Event> allEvents = new ArrayList<>();
                for (Event event : enhancedEvents) {
                    event.setFlags(getFlags(event, cachedCalendarAccess.getAccount().getUserId()));
                    event.setFolderId(folderId);
                    if (CalendarUtils.isSeriesMaster(event) && isResolveOccurrences(parameters)) {
                        allEvents.addAll(Lists.newArrayList(Services.getService(RecurrenceService.class).iterateEventOccurrences(event, getFrom(parameters), getUntil(parameters))));
                    } else {
                        allEvents.add(event);
                    }
                }
                return allEvents;
            }
        }.executeQuery();
    }

    protected String getFolderId() {
        return folderId;
    }

}
