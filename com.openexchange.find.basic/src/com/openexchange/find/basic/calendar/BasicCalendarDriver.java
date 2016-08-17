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

import static com.openexchange.find.facet.Facets.newDefaultBuilder;
import static com.openexchange.find.facet.Facets.newExclusiveBuilder;
import static com.openexchange.find.facet.Facets.newSimpleBuilder;
import static com.openexchange.java.SimpleTokenizer.tokenize;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.find.AutocompleteRequest;
import com.openexchange.find.AutocompleteResult;
import com.openexchange.find.Document;
import com.openexchange.find.FindExceptionCode;
import com.openexchange.find.Module;
import com.openexchange.find.SearchRequest;
import com.openexchange.find.SearchResult;
import com.openexchange.find.basic.AbstractContactFacetingModuleSearchDriver;
import com.openexchange.find.basic.Services;
import com.openexchange.find.basic.calendar.sort.RankedAppointmentComparator;
import com.openexchange.find.calendar.CalendarDocument;
import com.openexchange.find.calendar.CalendarFacetType;
import com.openexchange.find.calendar.CalendarFacetValues;
import com.openexchange.find.calendar.CalendarStrings;
import com.openexchange.find.common.CommonFacetType;
import com.openexchange.find.common.FolderType;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.Facets.DefaultFacetBuilder;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.util.DisplayItems;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.search.AppointmentSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.java.Strings;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link BasicCalendarDriver}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class BasicCalendarDriver extends AbstractContactFacetingModuleSearchDriver {

    /**
     * The calendar fields that are requested when searching. They should be used regardless which columns are set by the client in the
     * search request - those columns are only considered for writing the JSON result.
     */
    private static final int[] DEFAULT_COLUMN_IDS = {
        DataObject.OBJECT_ID, DataObject.CREATED_BY, DataObject.CREATION_DATE, DataObject.LAST_MODIFIED, DataObject.MODIFIED_BY,
        FolderChildObject.FOLDER_ID, CommonObject.PRIVATE_FLAG, CommonObject.CATEGORIES, CalendarObject.TITLE, Appointment.LOCATION,
        CalendarObject.START_DATE, CalendarObject.END_DATE, CalendarObject.NOTE, CalendarObject.RECURRENCE_TYPE,
        CalendarObject.RECURRENCE_CALCULATOR, CalendarObject.RECURRENCE_ID, CalendarObject.RECURRENCE_POSITION,
        CalendarObject.PARTICIPANTS, CalendarObject.USERS, Appointment.SHOWN_AS, Appointment.DELETE_EXCEPTIONS,
        Appointment.CHANGE_EXCEPTIONS, Appointment.FULL_TIME, Appointment.COLOR_LABEL, Appointment.TIMEZONE, Appointment.ORGANIZER,
        Appointment.ORGANIZER_ID, Appointment.PRINCIPAL, Appointment.PRINCIPAL_ID, Appointment.UID, Appointment.SEQUENCE,
        Appointment.CONFIRMATIONS, Appointment.LAST_MODIFIED_OF_NEWEST_ATTACHMENT, Appointment.NUMBER_OF_ATTACHMENTS
    };

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
        UserPermissionBits userPermissionBits = session.getUserPermissionBits();
        if (userPermissionBits.hasFullSharedFolderAccess()) {
            return ALL_FOLDER_TYPES;
        }

        Set<FolderType> types = EnumSet.noneOf(FolderType.class);
        types.add(FolderType.PRIVATE);
        types.add(FolderType.PUBLIC);
        return types;
    }

    @Override
    public AutocompleteResult doAutocomplete(AutocompleteRequest autocompleteRequest, ServerSession session) throws OXException {
        /*
         * collect possible facets for current auto-complete iteration
         */
        List<Facet> facets = new ArrayList<Facet>();
        String prefix = autocompleteRequest.getPrefix();
        int minimumSearchCharacters = ServerConfig.getInt(ServerConfig.Property.MINIMUM_SEARCH_CHARACTERS);
        if (false == Strings.isEmpty(prefix) && prefix.length() >= minimumSearchCharacters) {
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
            facets.add(newDefaultBuilder(CalendarFacetType.PARTICIPANT)
                .withValues(participantValues)
                .build());
        }
        /*
         * add other facets
         */
        facets.add(getStatusFacet());
        facets.add(getRangeFacet());
        facets.add(getRecurringTypeFacet());
        return new AutocompleteResult(facets);
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
        DefaultFacetBuilder facetBuilder = newExclusiveBuilder(CalendarFacetType.RANGE)
            .addValue(buildRangeFacetValue(CalendarFacetValues.RANGE_ONE_MONTH, CalendarStrings.RANGE_ONE_MONTH, fields))
            .addValue(buildRangeFacetValue(CalendarFacetValues.RANGE_THREE_MONTHS, CalendarStrings.RANGE_THREE_MONTHS, fields))
            .addValue(buildRangeFacetValue(CalendarFacetValues.RANGE_ONE_YEAR, CalendarStrings.RANGE_ONE_YEAR, fields))
        ;
        return facetBuilder.build();
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

    @Override
    public SearchResult doSearch(SearchRequest searchRequest, ServerSession session) throws OXException {
        /*
         * build appointment search
         */
        AppointmentSearchBuilder searchBuilder = new AppointmentSearchBuilder(session);
        AppointmentSearchObject appointmentSearch = searchBuilder
            .applyFilters(searchRequest.getFilters())
            .applyQueries(searchRequest.getQueries())
            .applyFolders(searchRequest)
            .build();
        if (searchBuilder.isFalse()) {
            return SearchResult.EMPTY;
        }

        /*
         * perform search
         */
        List<OXException> warnings = new ArrayList<OXException>();
        int limit = getHardResultLimit();
        List<Appointment> appointments = new ArrayList<Appointment>();
        AppointmentSQLInterface appointmentSql = Services.requireService(AppointmentSqlFactoryService.class).createAppointmentSql(session);
        SearchIterator<Appointment> searchIterator = null;
        try {
            searchIterator = appointmentSql.searchAppointments(appointmentSearch, -1, Order.NO_ORDER, limit + 1, DEFAULT_COLUMN_IDS);
            while (searchIterator.hasNext()) {
                appointments.add(getBestMatchingOccurrence(searchIterator.next()));
            }
            OXException[] iteratorWarnings = searchIterator.getWarnings();
            if (null != iteratorWarnings && 0 < iteratorWarnings.length) {
                warnings.addAll(Arrays.asList(iteratorWarnings));
            }
        } finally {
            if (null != searchIterator) {
                searchIterator.close();
            }
        }
        /*
         * check if limit has been exceeded
         */
        if (0 < limit && limit < appointments.size()) {
            appointments.remove(appointments.size() - 1);
            warnings.add(FindExceptionCode.TOO_MANY_RESULTS.create());
        }
        /*
         * construct search result
         */
        if (1 < appointments.size()) {
            Collections.sort(appointments, new RankedAppointmentComparator());
        }
        return new SearchResult(appointments.size(), searchRequest.getStart(),
            getDocuments(appointments, searchRequest.getStart(), searchRequest.getSize()), searchRequest.getActiveFacets(), warnings);
    }

    private static List<Document> getDocuments(List<Appointment> appointments, int start, int size) {
        if (start > appointments.size()) {
            return Collections.emptyList();
        }
        int startIndex = start;
        int stopIndex = 0 < size ? Math.min(appointments.size(), startIndex + size) : appointments.size();
        List<Document> documents = new ArrayList<Document>(stopIndex - startIndex);
        for (int i = startIndex; i < stopIndex; i++) {
            documents.add(new CalendarDocument(appointments.get(i)));
        }
        return documents;
    }

    /**
     * Chooses a single occurrence from a recurring appointment series based on the supplied minimum end and maximum start date boundaries.
     * Invoking this method on a non-recurring appointment has no effect.
     *
     * @param appointment The recurring appointment
     * @return The supplied appointment, with the values of the best matching occurrence being applied
     */
    private static Appointment getBestMatchingOccurrence(Appointment appointment) throws OXException {
        if (CalendarObject.NONE != appointment.getRecurrenceType() && 0 == appointment.getRecurrencePosition()) {
            long rangeStart = 0;
            long rangeEnd = Long.MAX_VALUE;
            int pmaxtc =  CalendarCollectionService.MAX_OCCURRENCESE;
            boolean first = true;
            /*
             * choose "next" or "last" occurrence in case not specified
             */
            Date until = appointment.getUntil();
            long now = System.currentTimeMillis();
            if (null != until && until.getTime() > now) {
                rangeStart = now;
                pmaxtc = 1;
            } else {
                first = false;
            }
            /*
             * calculate & apply occurrence
             */
            RecurringResultsInterface recurringResults = Services.requireService(CalendarCollectionService.class).calculateRecurring(
                appointment, rangeStart, rangeEnd, 0, pmaxtc, true);
            if (0 < recurringResults.size()) {
                RecurringResultInterface result = recurringResults.getRecurringResult(first ? 0 : recurringResults.size() - 1);
                appointment.setStartDate(new Date(result.getStart()));
                appointment.setEndDate(new Date(result.getEnd()));
                appointment.setRecurrencePosition(result.getPosition());
            }
        }

        return appointment;
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
                    .withDisplayItem(DisplayItems.convert(contact))
                    .withFilter(filter)
                    .build());
            }
        }
        return contactFacets;
    }

    private static Set<String> extractEmailAddresses(Contact contact) {
        Set<String> emailAddresses = new HashSet<String>(3);
        for (String email : new String[] { contact.getEmail1(), contact.getEmail2(), contact.getEmail3() } ) {
            if (false == Strings.isEmpty(email)) {
                emailAddresses.add(email);
            }
        }
        return emailAddresses;
    }

    private static int getHardResultLimit() throws OXException {
        return Services.getConfigurationService().getIntProperty("com.openexchange.find.basic.calendar.hardResultLimit", 1000);
    }

}
