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

package com.openexchange.find.basic.calendar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.SearchFilter;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.find.Document;
import com.openexchange.find.FindExceptionCode;
import com.openexchange.find.Module;
import com.openexchange.find.SearchRequest;
import com.openexchange.find.SearchResult;
import com.openexchange.find.basic.Folders;
import com.openexchange.find.basic.Services;
import com.openexchange.find.basic.calendar.sort.RankedEventComparator;
import com.openexchange.find.calendar.CalendarDocument;
import com.openexchange.find.calendar.CalendarFacetType;
import com.openexchange.find.facet.Filter;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link BasicEventDriver}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class BasicEventDriver extends BasicCalendarDriver {

    /**
     * Initializes a new {@link BasicEventDriver}.
     */
    public BasicEventDriver() {
        super();
    }

    @Override
    public boolean isValidFor(ServerSession session) throws OXException {
        if (Services.requireService(CalendarService.class).init(session).getConfig().isUseLegacyStack()) {
            return false;
        }
        return super.isValidFor(session);
    }

    @Override
    public SearchResult doSearch(SearchRequest searchRequest, ServerSession session) throws OXException {
        /*
         * prepare & perform the search
         */
        List<OXException> warnings = new ArrayList<OXException>();
        int limit = getHardResultLimit();
        CalendarSession calendarSession = Services.requireService(CalendarService.class).init(session);
        calendarSession.set(CalendarParameters.PARAMETER_EXPAND_OCCURRENCES, Boolean.FALSE);
        calendarSession.set(CalendarParameters.PARAMETER_RIGHT_HAND_LIMIT, limit + 1);
        if (null != searchRequest.getOptions().getTimeZone()) {
            calendarSession.set(CalendarParameters.PARAMETER_TIMEZONE, TimeZone.getTimeZone(searchRequest.getOptions().getTimeZone()));
        }
        List<String> folderIDs = Folders.getStringIDs(searchRequest, Module.CALENDAR, session);
        List<SearchFilter> filters = getFilters(session, searchRequest.getFilters());
        List<Event> events = calendarSession.getCalendarService().searchEvents(
            calendarSession, null != folderIDs ? folderIDs.toArray(new String[folderIDs.size()]) : null, filters, searchRequest.getQueries());
        /*
         * select suitable occurrences for series events
         */
        for (int i = 0; i < events.size(); i++) {
            events.set(i, getBestMatchingOccurrence(calendarSession, events.get(i)));
        }
        /*
         * check if limit has been exceeded
         */
        if (0 < limit && limit < events.size()) {
            events.remove(events.size() - 1);
            warnings.add(FindExceptionCode.TOO_MANY_RESULTS.create());
        }
        /*
         * construct search result
         */
        if (1 < events.size()) {
            Collections.sort(events, new RankedEventComparator());
        }
        return new SearchResult(events.size(), searchRequest.getStart(), getDocuments(events, searchRequest.getStart(), searchRequest.getSize()), searchRequest.getActiveFacets(), warnings);
    }

    private static List<Document> getDocuments(List<Event> events, int start, int size) {
        if (start > events.size()) {
            return Collections.emptyList();
        }
        int startIndex = start;
        int stopIndex = 0 < size ? Math.min(events.size(), startIndex + size) : events.size();
        List<Document> documents = new ArrayList<Document>(stopIndex - startIndex);
        for (int i = startIndex; i < stopIndex; i++) {
            documents.add(new CalendarDocument(events.get(i), "eventDocument"));
        }
        return documents;
    }

    /**
     * Chooses a single occurrence from a recurring event series based on the supplied minimum end and maximum start date boundaries.
     * Invoking this method on a non-recurring event has no effect.
     *
     * @param event The recurring event
     * @return The best matching occurrence
     */
    private static Event getBestMatchingOccurrence(CalendarSession session, Event event) throws OXException {
        if (CalendarUtils.isSeriesMaster(event)) {
            Event occurrence = null;
            Date now = new Date();

            /*
             * prefer the "next" occurrence if possible
             */
            Iterator<Event> iterator = session.getRecurrenceService().iterateEventOccurrences(event, now, null);
            if (iterator.hasNext()) {
                occurrence = iterator.next();
            } else {
                /*
                 * prefer the "last" occurrence
                 */
                iterator = session.getRecurrenceService().iterateEventOccurrences(event, null, now);
                while (iterator.hasNext()) {
                    occurrence = iterator.next();
                }
            }
            if (null != occurrence) {
                return occurrence;
            } else {
                /*
                 * fall back to very first occurrence
                 */
                iterator = session.getRecurrenceService().iterateEventOccurrences(event, null, null);
                if (iterator.hasNext()) {
                    return iterator.next();
                }
            }
        }
        /*
         * return regular event, otherwise
         */
        return event;
    }

    private static SearchFilter getFilter(ServerSession session, String field, List<String> queries) throws OXException {
        if (CalendarFacetType.SUBJECT.getId().equals(field) || CalendarFacetType.LOCATION.getId().equals(field) ||
            CalendarFacetType.DESCRIPTION.getId().equals(field) || CalendarFacetType.ATTACHMENT_NAME.getId().equals(field)) {
            List<String> preparedQueries = new ArrayList<String>(queries.size());
            for (String query : queries) {
                preparedQueries.add(addWildcards(checkPatternLength(query), true, true));
            }
            return new DefaultSearchFilter(field, preparedQueries);
        } else if (CalendarFacetType.STATUS.getId().equals(field)) {
            List<String> preparedQueries = new ArrayList<String>(1 + queries.size());
            preparedQueries.add(String.valueOf(session.getUserId()));
            for (String query : queries) {
                ParticipationStatus partStat = new ParticipationStatus(query);
                if (false == partStat.isStandard()) {
                    throw FindExceptionCode.UNSUPPORTED_FILTER_QUERY.create(query, CalendarFacetType.STATUS.getId());
                }
                preparedQueries.add(String.valueOf(partStat));
            }
            return new DefaultSearchFilter(field, preparedQueries);
        }
        return new DefaultSearchFilter(field, queries);

    }

    private static List<SearchFilter> getFilters(ServerSession session, List<Filter> filters) throws OXException {
        if (null == filters) {
            return null;
        }
        List<SearchFilter> searchFilters = new ArrayList<SearchFilter>(filters.size());
        for (Filter filter : filters) {
            for (String field : filter.getFields()) {
                searchFilters.add(getFilter(session, field, filter.getQueries()));
            }
        }
        return searchFilters;
    }

    private static String addWildcards(String pattern, boolean prepend, boolean append) {
        if ((null == pattern || 0 == pattern.length()) && (append || prepend)) {
            return "*";
        }
        if (null != pattern) {
            if (prepend && '*' != pattern.charAt(0)) {
                pattern = "*" + pattern;
            }
            if (append && '*' != pattern.charAt(pattern.length() - 1)) {
                pattern = pattern + "*";
            }
        }
        return pattern;
    }

    private static String checkPatternLength(String pattern) throws OXException {
        int minimumSearchCharacters = ServerConfig.getInt(ServerConfig.Property.MINIMUM_SEARCH_CHARACTERS);
        if (null != pattern && 0 < minimumSearchCharacters && pattern.length() < minimumSearchCharacters) {
            throw FindExceptionCode.QUERY_TOO_SHORT.create(Integer.valueOf(minimumSearchCharacters));
        }
        return pattern;
    }

}
