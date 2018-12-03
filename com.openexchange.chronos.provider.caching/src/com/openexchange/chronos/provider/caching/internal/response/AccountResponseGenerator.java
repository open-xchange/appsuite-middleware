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
import static com.openexchange.chronos.common.CalendarUtils.isInRange;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import com.google.common.collect.Lists;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.provider.caching.basic.AlarmAwareCachingCalendarAccess;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.RecurrenceIterator;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.operation.OSGiCalendarStorageOperation;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link AccountResponseGenerator}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class AccountResponseGenerator extends ResponseGenerator {

    public AccountResponseGenerator(AlarmAwareCachingCalendarAccess cachedCalendarAccess) {
        super(cachedCalendarAccess);
    }

    public List<Event> generate() throws OXException {
        CalendarParameters parameters = cachedCalendarAccess.getParameters();
        Date from = getFrom(parameters);
        Date until = getUntil(parameters);
        TimeZone timeZone = getTimeZone(parameters, cachedCalendarAccess.getSession());
        SearchOptions searchOptions = new SearchOptions(parameters);
        return new OSGiCalendarStorageOperation<List<Event>>(Services.getServiceLookup(), this.cachedCalendarAccess.getSession().getContextId(), this.cachedCalendarAccess.getAccount().getAccountId()) {

            @Override
            protected List<Event> call(CalendarStorage storage) throws OXException {
                CalendarParameters parameters = cachedCalendarAccess.getParameters();
                EventField[] fields = getFields(parameters.get(CalendarParameters.PARAMETER_FIELDS, EventField[].class), EventField.FOLDER_ID);
                List<Event> events = storage.getEventStorage().searchEvents(null, searchOptions, fields);
                List<Event> enhancedEvents = storage.getUtilities().loadAdditionalEventData(cachedCalendarAccess.getAccount().getUserId(), events, fields);
                List<Event> allEvents = new ArrayList<>();
                for (Event event : enhancedEvents) {
                    event.setFlags(getFlags(event, cachedCalendarAccess.getAccount().getUserId()));
                    if (isSeriesMaster(event) && Strings.isNotEmpty(event.getRecurrenceRule())) {
                        RecurrenceIterator<Event> iterator = Services.getService(RecurrenceService.class).iterateEventOccurrences(event, from, until);
                        if (isResolveOccurrences(parameters)) {
                            allEvents.addAll(Lists.newArrayList(iterator));
                        } else if (iterator.hasNext()) {
                            allEvents.add(event);
                        }
                    } else if (isInRange(event, from, until, timeZone)) {
                        allEvents.add(event);
                    }
                }
                return allEvents;
            }
        }.executeQuery();
    }

}
