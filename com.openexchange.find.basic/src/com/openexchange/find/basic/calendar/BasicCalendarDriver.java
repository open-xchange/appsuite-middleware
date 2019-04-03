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

import static com.openexchange.chronos.common.CalendarUtils.add;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.find.facet.Facets.newDefaultBuilder;
import static com.openexchange.find.facet.Facets.newExclusiveBuilder;
import static com.openexchange.find.facet.Facets.newSimpleBuilder;
import static com.openexchange.java.SimpleTokenizer.tokenize;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DataHandlers;
import com.openexchange.chronos.common.DefaultSearchFilter;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccessFactory;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.EventsResult;
import com.openexchange.chronos.service.RangeOption;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.service.SearchFilter;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.conversion.ConversionResult;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.find.AutocompleteRequest;
import com.openexchange.find.AutocompleteResult;
import com.openexchange.find.Document;
import com.openexchange.find.FindExceptionCode;
import com.openexchange.find.Module;
import com.openexchange.find.SearchRequest;
import com.openexchange.find.SearchResult;
import com.openexchange.find.basic.AbstractContactFacetingModuleSearchDriver;
import com.openexchange.find.basic.Folders;
import com.openexchange.find.basic.Services;
import com.openexchange.find.calendar.CalendarDocument;
import com.openexchange.find.calendar.CalendarFacetType;
import com.openexchange.find.calendar.CalendarFacetValues;
import com.openexchange.find.calendar.CalendarStrings;
import com.openexchange.find.common.CommonFacetType;
import com.openexchange.find.common.FolderType;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.util.DisplayItems;
import com.openexchange.groupware.container.Contact;
import com.openexchange.i18n.I18nServiceRegistry;
import com.openexchange.java.Strings;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link BasicCalendarDriver}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class BasicCalendarDriver extends AbstractContactFacetingModuleSearchDriver {

    /**
     * Initializes a new {@link BasicCalendarDriver}.
     */
    public BasicCalendarDriver() {
        super();
    }

    @Override
    public Module getModule() {
        return Module.CALENDAR;
    }

    @Override
    public boolean isValidFor(ServerSession session) throws OXException {
        return session.getUserConfiguration().hasCalendar();
    }

    @Override
    protected Set<FolderType> getSupportedFolderTypes(AutocompleteRequest autocompleteRequest, ServerSession session) {
        if (session.getUserPermissionBits().hasFullSharedFolderAccess()) {
            return ALL_FOLDER_TYPES;
        }
        return EnumSet.of(FolderType.PRIVATE, FolderType.PUBLIC);
    }

    @Override
    public AutocompleteResult doAutocomplete(AutocompleteRequest autocompleteRequest, ServerSession session) throws OXException {
        /*
         * collect possible facets for current auto-complete iteration
         */
        List<Facet> facets = new ArrayList<Facet>();
        String prefix = autocompleteRequest.getPrefix();
        int minimumSearchCharacters = ServerConfig.getInt(ServerConfig.Property.MINIMUM_SEARCH_CHARACTERS);
        if (Strings.isNotEmpty(prefix) && prefix.length() >= minimumSearchCharacters) {
            /*
             * add prefix-aware field facets
             */
            List<String> prefixTokens = tokenize(prefix, minimumSearchCharacters);
            if (prefixTokens.isEmpty()) {
                prefixTokens = Collections.singletonList(prefix);
            }
            facets.add(newSimpleBuilder(CommonFacetType.GLOBAL)
                .withSimpleDisplayItem(prefix)
                .withFilter(Filter.of(CommonFacetType.GLOBAL.getId(), prefixTokens))
            .build());
            facets.add(newSimpleBuilder(CalendarFacetType.SUBJECT)
                .withFormattableDisplayItem(CalendarStrings.SUBJECT, prefix)
                .withFilter(Filter.of(CalendarFacetType.SUBJECT.getId(), prefixTokens))
            .build());
            facets.add(newSimpleBuilder(CalendarFacetType.DESCRIPTION)
                .withFormattableDisplayItem(CalendarStrings.DESCRIPTION, prefix)
                .withFilter(Filter.of(CalendarFacetType.DESCRIPTION.getId(), prefixTokens))
            .build());
            facets.add(newSimpleBuilder(CalendarFacetType.LOCATION)
                .withFormattableDisplayItem(CalendarStrings.LOCATION, prefix)
                .withFilter(Filter.of(CalendarFacetType.LOCATION.getId(), prefixTokens))
            .build());
            facets.add(newSimpleBuilder(CalendarFacetType.ATTACHMENT_NAME)
                .withFormattableDisplayItem(CalendarStrings.ATTACHMENT_NAME, prefix)
                .withFilter(Filter.of(CalendarFacetType.ATTACHMENT_NAME.getId(), prefixTokens))
            .build());
        }
        /*
         * add participants facet dynamically
         */
        List<FacetValue> participantValues = getParticipantValues(autocompleteRequest, session);
        if (null != participantValues && 0 < participantValues.size()) {
            facets.add(newDefaultBuilder(CalendarFacetType.PARTICIPANT).withValues(participantValues).build());
        }
        /*
         * add other facets
         */
        facets.add(getStatusFacet());
        facets.add(getRangeFacet());
        facets.add(getRecurringTypeFacet());
        return new AutocompleteResult(facets);
    }

    @Override
    public SearchResult doSearch(SearchRequest searchRequest, ServerSession session) throws OXException {
        /*
         * prepare & perform the search
         */
        List<OXException> warnings = new ArrayList<OXException>();
        int limit = getHardResultLimit();
        List<String> folderIDs = Folders.getStringIDs(searchRequest, Module.CALENDAR, session);
        List<SearchFilter> filters = getFilters(session, searchRequest.getFilters());
        Map<String, EventsResult> eventsResults;
        IDBasedCalendarAccess calendarAccess = initCalendarAccess(searchRequest, session, limit + 1);
        boolean committed = false;
        try {
            calendarAccess.startTransaction();
            eventsResults = calendarAccess.searchEvents(folderIDs, filters, searchRequest.getQueries());
            calendarAccess.commit();
            committed = true;
        } finally {
            if (false == committed) {
                calendarAccess.rollback();
            }
            calendarAccess.finish();
        }
        /*
         * select suitable occurrences for series events
         */
        List<Event> events = new ArrayList<Event>();
        RangeOption searchRange = getSearchRange(filters);
        for (EventsResult eventsResult : eventsResults.values()) {
            OXException error = eventsResult.getError();
            if (null != eventsResult.getError()) {
                warnings.add(error);
                continue;
            }
            for (Event event : eventsResult.getEvents()) {
                if (isSeriesMaster(event)) {
                    event = getBestMatchingOccurrence(calendarAccess, event, searchRange);
                    if (null != event) {
                        events.add(event);
                    }
                } else {
                    events.add(event);
                }
            }
        }
        /*
         * check if limit has been exceeded & add calendar access warnings
         */
        if (0 < limit && limit < events.size()) {
            events.remove(events.size() - 1);
            warnings.add(FindExceptionCode.TOO_MANY_RESULTS.create());
        }
        List<OXException> accessWarnings = calendarAccess.getWarnings();
        if (null != accessWarnings && 0 < accessWarnings.size()) {
            warnings.addAll(accessWarnings);
        }
        /*
         * construct search result
         */
        if (1 < events.size()) {
            Collections.sort(events, new RankedEventComparator(CalendarUtils.optTimeZone(session.getUser().getTimeZone())));
        }
        return new SearchResult(events.size(), searchRequest.getStart(), getDocuments(events, searchRequest.getStart(), searchRequest.getSize()), searchRequest.getActiveFacets(), warnings);
    }

    /**
     * Initializes & prepares a calendar access for handling a specific search request.
     * 
     * @param searchRequest The handled search request
     * @param session The user's session
     * @param limit The limit to apply for the search
     * @return The initialized calendar access
     */
    private static IDBasedCalendarAccess initCalendarAccess(SearchRequest searchRequest, ServerSession session, int limit) throws OXException {
        IDBasedCalendarAccess calendarAccess = Services.requireService(IDBasedCalendarAccessFactory.class).createAccess(session);
        calendarAccess.set(CalendarParameters.PARAMETER_EXPAND_OCCURRENCES, Boolean.FALSE);
        calendarAccess.set(CalendarParameters.PARAMETER_RIGHT_HAND_LIMIT, limit + 1);
        if (null != searchRequest.getOptions().getTimeZone()) {
            calendarAccess.set(CalendarParameters.PARAMETER_TIMEZONE, TimeZone.getTimeZone(searchRequest.getOptions().getTimeZone()));
        }
        DataHandler dataHandler = Services.requireService(ConversionService.class).getDataHandler(DataHandlers.STRING_ARRAY_TO_EVENT_FIELDS);
        if (dataHandler != null) {
            ConversionResult processData = dataHandler.processData(new SimpleData<Object>(searchRequest.getColumns().getStringColumns()), new DataArguments(), session);
            if (processData != null) {
                calendarAccess.set(CalendarParameters.PARAMETER_FIELDS, processData.getData());
            }
        }
        return calendarAccess;
    }

    private static List<Document> getDocuments(List<Event> events, int start, int size) {
        if (start > events.size()) {
            return Collections.emptyList();
        }
        int startIndex = start;
        int stopIndex = 0 < size ? Math.min(events.size(), startIndex + size) : events.size();
        List<Document> documents = new ArrayList<Document>(stopIndex - startIndex);
        for (int i = startIndex; i < stopIndex; i++) {
            documents.add(new CalendarDocument(events.get(i), "event"));
        }
        return documents;
    }

    /**
     * Chooses a single occurrence from a recurring event series based on the supplied minimum end and maximum start date boundaries.
     * Invoking this method on a non-recurring event has no effect.
     *
     * @param calendarAccess The underlying calendar access
     * @param event The recurring event
     * @param range The time range as derived from the value of the query's range facet filter, or <code>null</code> if not restricted
     * @return The best matching occurrence, or <code>null</code> if there's no suitable occurrence available
     */
    private static Event getBestMatchingOccurrence(IDBasedCalendarAccess calendarAccess, Event event, RangeOption range) throws OXException {
        if (false == isSeriesMaster(event)) {
            return null;
        }
        Date from = null != range ? range.getFrom() : null;
        Date until = null != range ? range.getUntil() : null;
        RecurrenceService recurrenceService = Services.requireService(RecurrenceService.class);
        Event occurrence = null;
        Date now = new Date();
        /*
         * prefer the "next" occurrence if possible
         */
        Iterator<Event> iterator = recurrenceService.iterateEventOccurrences(event, now, until);
        if (iterator.hasNext()) {
            return iterator.next();
        }
        /*
         * prefer the "last" occurrence, otherwise
         */
        iterator = recurrenceService.iterateEventOccurrences(event, from, now);
        if (iterator.hasNext()) {
            while (iterator.hasNext()) {
                occurrence = iterator.next();
            }
            return occurrence;
        }
        /*
         * fall back to very first occurrence, otherwise
         */
        iterator = recurrenceService.iterateEventOccurrences(event, from, until);
        if (iterator.hasNext()) {
            return iterator.next();
        }
        /*
         * no matching occurrence, otherwise
         */
        return null;
    }

    private static SearchFilter getFilter(ServerSession session, String field, List<String> queries) throws OXException {
        if (CalendarFacetType.SUBJECT.getId().equals(field) || CalendarFacetType.LOCATION.getId().equals(field) ||
            CalendarFacetType.DESCRIPTION.getId().equals(field) || CalendarFacetType.ATTACHMENT_NAME.getId().equals(field)) {
            List<String> preparedQueries = new ArrayList<String>(queries.size());
            for (String query : queries) {
                preparedQueries.add(addWildcards(checkPatternLength(query), true, true));
            }
            return new DefaultSearchFilter(field, preparedQueries);
        }
        if (CalendarFacetType.STATUS.getId().equals(field)) {
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
        if (CalendarFacetType.PARTICIPANT.getId().equals(field)) {
            List<String> preparedQueries = new ArrayList<String>(queries.size());
            for (String query : queries) {
                preparedQueries.add(CalendarUtils.getURI(query));
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

    private static Facet getStatusFacet() {
        List<String> fields = Collections.singletonList(CalendarFacetType.STATUS.getId());
        return newExclusiveBuilder(CalendarFacetType.STATUS)
            .addValue(buildStatusFacetValue(CalendarFacetValues.STATUS_ACCEPTED, CalendarStrings.STATUS_ACCEPTED, fields))
            .addValue(buildStatusFacetValue(CalendarFacetValues.STATUS_DECLINED, CalendarStrings.STATUS_DECLINED, fields))
            .addValue(buildStatusFacetValue(CalendarFacetValues.STATUS_TENTATIVE, CalendarStrings.STATUS_TENTATIVE, fields))
            .addValue(buildStatusFacetValue(CalendarFacetValues.STATUS_NONE, CalendarStrings.STATUS_NONE, fields))
        .build();
    }

    private static FacetValue buildStatusFacetValue(String id, String displayName, List<String> filterFields) {
        return buildStaticFacetValue(id, displayName, filterFields, id);
    }

    private static Facet getRangeFacet() throws OXException {
        List<String> fields = Collections.singletonList(CalendarFacetType.RANGE.getId());
        return newExclusiveBuilder(CalendarFacetType.RANGE)
            .addValue(buildRangeFacetValue(CalendarFacetValues.RANGE_ONE_MONTH, CalendarStrings.RANGE_ONE_MONTH, fields))
            .addValue(buildRangeFacetValue(CalendarFacetValues.RANGE_THREE_MONTHS, CalendarStrings.RANGE_THREE_MONTHS, fields))
            .addValue(buildRangeFacetValue(CalendarFacetValues.RANGE_ONE_YEAR, CalendarStrings.RANGE_ONE_YEAR, fields))
        .build();
    }

    private static FacetValue buildRangeFacetValue(String id, String displayName, List<String> filterFields) {
        return buildStaticFacetValue(id, displayName, filterFields, id);
    }

    private static Facet getRecurringTypeFacet() {
        List<String> fields = Collections.singletonList(CalendarFacetType.RECURRING_TYPE.getId());
        return newExclusiveBuilder(CalendarFacetType.RECURRING_TYPE)
            .addValue(buildRecurringTypeFacetValue(CalendarFacetValues.RECURRING_TYPE_SERIES, CalendarStrings.RECURRING_TYPE_SERIES, fields))
            .addValue(buildRecurringTypeFacetValue(CalendarFacetValues.RECURRING_TYPE_SINGLE, CalendarStrings.RECURRING_TYPE_SINGLE, fields))
        .build();
    }

    private static FacetValue buildRecurringTypeFacetValue(String id, String displayName, List<String> filterFields) {
        return buildStaticFacetValue(id, displayName, filterFields, id);
    }

    private static FacetValue buildStaticFacetValue(String id, String displayName, List<String> filterFields, String filterQuery) {
        return FacetValue.newBuilder(id)
            .withLocalizableDisplayItem(displayName)
            .withFilter(Filter.of(filterFields, filterQuery))
        .build();
    }

    private List<FacetValue> getParticipantValues(AutocompleteRequest autocompleteRequest, ServerSession session) throws OXException {
        /*
         * search matching contacts (and users, implicitly)
         */
        List<Contact> contacts = autocompleteContacts(session, autocompleteRequest);
        List<FacetValue> contactFacets = new ArrayList<FacetValue>();
        for (Contact contact : contacts) {
            Filter filter = null;
            if (0 < contact.getInternalUserId()) {
                /*
                 * build "users" filter for internal user
                 */
                filter = Filter.of("users", String.valueOf(contact.getInternalUserId()));
            } else {
                /*
                 * build "participants" filter for email addresses
                 */
                Set<String> emailAddresses = extractEmailAddresses(contact);
                if (null != emailAddresses && 0 < emailAddresses.size()) {
                    filter = Filter.of("participants", new ArrayList<String>(emailAddresses));
                }
            }
            if (null != filter) {
                String valueId = prepareFacetValueId("contact", session.getContextId(), Integer.toString(contact.getObjectID()));
                contactFacets.add(FacetValue.newBuilder(valueId)
                    .withDisplayItem(DisplayItems.convert(contact, session.getUser().getLocale(), Services.optionalService(I18nServiceRegistry.class)))
                    .withFilter(filter)
                    .build());
            }
        }
        return contactFacets;
    }

    private static Set<String> extractEmailAddresses(Contact contact) {
        Set<String> emailAddresses = new HashSet<String>(3);
        for (String email : new String[] { contact.getEmail1(), contact.getEmail2(), contact.getEmail3() }) {
            if (Strings.isNotEmpty(email)) {
                emailAddresses.add(email);
            }
        }
        return emailAddresses;
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

    private static int getHardResultLimit() throws OXException {
        return Services.getConfigurationService().getIntProperty("com.openexchange.find.basic.calendar.hardResultLimit", 1000);
    }

    private static RangeOption getSearchRange(List<SearchFilter> filters) {
        if (null != filters && 0 < filters.size()) {
            for (SearchFilter filter : filters) {
                if (null != filter.getFields() && 1 == filter.getFields().size() && CalendarFacetType.RANGE.getId().equals(filter.getFields().get(0)) &&
                    null != filter.getQueries() && 1 == filter.getQueries().size()) {
                    return getSearchRange(filter.getQueries().get(0));
                }
            }
        }
        return null;
    }

    private static RangeOption getSearchRange(String rangeValue) {
        if (null == rangeValue) {
            return null;
        }
        Date now = new Date();
        switch (rangeValue) {
            case CalendarFacetValues.RANGE_ONE_MONTH:
                return new RangeOption().setRange(add(now, Calendar.MONTH, -1), add(now, Calendar.MONTH, +1));
            case CalendarFacetValues.RANGE_THREE_MONTHS:
                return new RangeOption().setRange(add(now, Calendar.MONTH, -3), add(now, Calendar.MONTH, +3));
            case CalendarFacetValues.RANGE_ONE_YEAR:
                return new RangeOption().setRange(add(now, Calendar.YEAR, -1), add(now, Calendar.YEAR, +1));
            default:
                return null;
        }
    }

}
