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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.exception.OXException;
import com.openexchange.find.AutocompleteRequest;
import com.openexchange.find.AutocompleteResult;
import com.openexchange.find.Document;
import com.openexchange.find.Module;
import com.openexchange.find.SearchRequest;
import com.openexchange.find.SearchResult;
import com.openexchange.find.basic.AbstractContactFacetingModuleSearchDriver;
import com.openexchange.find.basic.Services;
import com.openexchange.find.calendar.CalendarDocument;
import com.openexchange.find.calendar.CalendarFacetType;
import com.openexchange.find.calendar.CalendarStrings;
import com.openexchange.find.calendar.RecurringTypeDisplayItem;
import com.openexchange.find.calendar.RelativeDateDisplayItem;
import com.openexchange.find.calendar.StatusDisplayItem;
import com.openexchange.find.common.ContactDisplayItem;
import com.openexchange.find.common.FormattableDisplayItem;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.FieldFacet;
import com.openexchange.find.facet.Filter;
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
     * The calendar fields that are requested when searching.
     */
    private static final int[] FIELDS = {
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
        return session.getUserConfiguration().hasCalendar() && session.getUserConfiguration().hasContact();
    }

    @Override
    protected String getFormatStringForGlobalFacet() {
        return CalendarStrings.GLOBAL;
    }

    @Override
    protected Set<Integer> getSupportedFolderTypes() {
        return ALL_FOLDER_TYPES;
    }

    @Override
    public AutocompleteResult doAutocomplete(AutocompleteRequest autocompleteRequest, ServerSession session) throws OXException {
        /*
         * collect possible facets for current auto-complete iteration
         */
        List<Facet> facets = new ArrayList<Facet>();
        String prefix = autocompleteRequest.getPrefix();
        if (false == Strings.isEmpty(prefix)) {
            /*
             * add prefix-aware field facets
             */
            facets.add(new FieldFacet(CalendarFacetType.SUBJECT, new FormattableDisplayItem(CalendarStrings.SUBJECT, prefix),
                CalendarFacetType.SUBJECT.getId(), prefix));
            facets.add(new FieldFacet(CalendarFacetType.DESCRIPTION, new FormattableDisplayItem(CalendarStrings.DESCRIPTION, prefix),
                CalendarFacetType.DESCRIPTION.getId(), prefix));
            facets.add(new FieldFacet(CalendarFacetType.LOCATION, new FormattableDisplayItem(CalendarStrings.LOCATION, prefix),
                CalendarFacetType.LOCATION.getId(), prefix));
            facets.add(new FieldFacet(CalendarFacetType.ATTACHMENT_NAME, new FormattableDisplayItem(CalendarStrings.ATTACHMENT_NAME, prefix),
                CalendarFacetType.ATTACHMENT_NAME.getId(), prefix));
        }
        /*
         * add participants facet dynamically
         */
        List<FacetValue> participantValues = getParticipantValues(autocompleteRequest, session);
        if (null != participantValues && 0 < participantValues.size()) {
            facets.add(new Facet(CalendarFacetType.PARTICIPANT, participantValues));
        }
        /*
         * add other facets
         */
        facets.add(getStatusFacet());
        facets.add(getRelativeDateFacet());
        facets.add(getRecurringTypeFacet());
        return new AutocompleteResult(facets);
    }

    private static Facet getStatusFacet() {
        List<FacetValue> statusValues = new ArrayList<FacetValue>();
        List<String> fields = Collections.singletonList(CalendarFacetType.STATUS.getId());
        statusValues.add(new FacetValue(StatusDisplayItem.Status.ACCEPTED.getIdentifier(),
            new StatusDisplayItem(CalendarStrings.STATUS_ACCEPTED, StatusDisplayItem.Status.ACCEPTED),
            FacetValue.UNKNOWN_COUNT, new Filter(fields, StatusDisplayItem.Status.ACCEPTED.getIdentifier())));
        statusValues.add(new FacetValue(StatusDisplayItem.Status.DECLINED.getIdentifier(),
            new StatusDisplayItem(CalendarStrings.STATUS_DECLINED, StatusDisplayItem.Status.DECLINED),
            FacetValue.UNKNOWN_COUNT, new Filter(fields, StatusDisplayItem.Status.DECLINED.getIdentifier())));
        statusValues.add(new FacetValue(StatusDisplayItem.Status.TENTATIVE.getIdentifier(),
            new StatusDisplayItem(CalendarStrings.STATUS_TENTATIVE, StatusDisplayItem.Status.TENTATIVE),
            FacetValue.UNKNOWN_COUNT, new Filter(fields, StatusDisplayItem.Status.TENTATIVE.getIdentifier())));
        statusValues.add(new FacetValue(StatusDisplayItem.Status.NONE.getIdentifier(),
            new StatusDisplayItem(CalendarStrings.STATUS_NONE, StatusDisplayItem.Status.NONE),
            FacetValue.UNKNOWN_COUNT, new Filter(fields, StatusDisplayItem.Status.NONE.getIdentifier())));
        return new Facet(CalendarFacetType.STATUS, statusValues);
    }

    private static Facet getRelativeDateFacet() {
        List<FacetValue> dateValues = new ArrayList<FacetValue>();
        List<String> fields = Collections.singletonList(CalendarFacetType.RELATIVE_DATE.getId());
        dateValues.add(new FacetValue(RelativeDateDisplayItem.RelativeDate.COMING.getIdentifier(),
            new RelativeDateDisplayItem(CalendarStrings.RELATIVE_DATE_COMING, RelativeDateDisplayItem.RelativeDate.COMING),
            FacetValue.UNKNOWN_COUNT, new Filter(fields, RelativeDateDisplayItem.RelativeDate.COMING.getIdentifier())));
        dateValues.add(new FacetValue(RelativeDateDisplayItem.RelativeDate.PAST.getIdentifier(),
            new RelativeDateDisplayItem(CalendarStrings.RELATIVE_DATE_PAST, RelativeDateDisplayItem.RelativeDate.PAST),
            FacetValue.UNKNOWN_COUNT, new Filter(fields, RelativeDateDisplayItem.RelativeDate.PAST.getIdentifier())));
        return new Facet(CalendarFacetType.RELATIVE_DATE, dateValues);
    }

    private static Facet getRecurringTypeFacet() {
        List<FacetValue> recurringTypeValues = new ArrayList<FacetValue>();
        List<String> fields = Collections.singletonList(CalendarFacetType.RECURRING_TYPE.getId());
        recurringTypeValues.add(new FacetValue(RecurringTypeDisplayItem.RecurringType.SINGLE.getIdentifier(),
            new RecurringTypeDisplayItem(CalendarStrings.RECURRING_TYPE_SINGLE, RecurringTypeDisplayItem.RecurringType.SINGLE),
            FacetValue.UNKNOWN_COUNT, new Filter(fields, RecurringTypeDisplayItem.RecurringType.SINGLE.getIdentifier())));
        recurringTypeValues.add(new FacetValue(RecurringTypeDisplayItem.RecurringType.SERIES.getIdentifier(),
            new RecurringTypeDisplayItem(CalendarStrings.RECURRING_TYPE_SERIES, RecurringTypeDisplayItem.RecurringType.SERIES),
            FacetValue.UNKNOWN_COUNT, new Filter(fields, RecurringTypeDisplayItem.RecurringType.SERIES.getIdentifier())));
        return new Facet(CalendarFacetType.RECURRING_TYPE, recurringTypeValues);
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
            .applyFolders(searchRequest.getFolderId(), searchRequest.getFolderType())
            .build();
        if (searchBuilder.isFalse()) {
            return SearchResult.EMPTY;
        }
        /*
         * perform search
         */
        List<Appointment> appointments = new ArrayList<Appointment>();
        AppointmentSQLInterface appointmentSql = Services.requireService(AppointmentSqlFactoryService.class).createAppointmentSql(session);
        SearchIterator<Appointment> searchIterator = null;
        try {
            searchIterator = appointmentSql.searchAppointments(appointmentSearch, Appointment.START_DATE, Order.ASCENDING, FIELDS);
            while (searchIterator.hasNext()) {
                appointments.add(getBestMatchingOccurrence(searchIterator.next(), appointmentSearch.getMinimumEndDate(), appointmentSearch.getMaximumStartDate()));
            }
        } finally {
            if (null != searchIterator) {
                searchIterator.close();
            }
        }
        if (1 < appointments.size()) {
            Collections.sort(appointments, STARTTIME_COMPARATOR);
        }
        /*
         * construct search result
         */
        return new SearchResult(appointments.size(), searchRequest.getStart(),
            getDocuments(appointments, searchRequest.getStart(), searchRequest.getSize()), searchRequest.getActiveFacets());
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
     * @param minimumEndDate The minimum end date to consider, or <code>null</code> if not defined
     * @param maximumStartDate The maximum start date to consider, or <code>null</code> if not defined
     * @return The supplied appointment, with the values of the best matching occurrence being applied
     * @throws OXException
     */
    private static Appointment getBestMatchingOccurrence(Appointment appointment, Date minimumEndDate, Date maximumStartDate) throws OXException {
        if (CalendarObject.NONE != appointment.getRecurrenceType() && 0 == appointment.getRecurrencePosition()) {
            long rangeStart = 0;
            long rangeEnd = Long.MAX_VALUE;
            int pmaxtc =  CalendarCollectionService.MAX_OCCURRENCESE;
            boolean first = true;
            if (null == minimumEndDate && null == maximumStartDate) {
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
            } else {
                /*
                 * get first occurrence after minimum end date
                 */
                if (null != minimumEndDate) {
                    rangeStart = minimumEndDate.getTime();
                    pmaxtc = 1;
                    first = true;
                }
                /*
                 * get last occurrence before maximum start date
                 */
                if (null != maximumStartDate) {
                    rangeEnd = maximumStartDate.getTime();
                    first = false;
                }
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

    private static final Comparator<Appointment> STARTTIME_COMPARATOR = new Comparator<Appointment>() {

        @Override
        public int compare(Appointment appointment1, Appointment appointment2) {
            //TODO: startdate of whole day appts in user timezone
            Date date1 = null != appointment1 ? appointment1.getStartDate() : null;
            Date date2 = null != appointment2 ? appointment2.getStartDate() : null;
            if (date1 == date2) {
                return 0;
            } else if (null == date1) {
                return -1;
            } else if (null == date2) {
                return 1;
            } else {
                return date1.compareTo(date2);
            }
        }
    };

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
                filter = new Filter(Collections.singletonList("users"), String.valueOf(contact.getInternalUserId()));
            } else {
                /*
                 * build "participants" filter for email addresses
                 */
                Set<String> emailAddresses = extractEmailAddresses(contact);
                if (null != emailAddresses && 0 < emailAddresses.size()) {
                    filter = new Filter(Collections.singletonList("participants"), new ArrayList<String>(emailAddresses));
                }
            }
            if (null != filter) {
                contactFacets.add(new FacetValue(
                    prepareFacetValueId("contact", session.getContextId(), Integer.toString(contact.getObjectID())),
                    new ContactDisplayItem(contact), FacetValue.UNKNOWN_COUNT, filter));
            }
        }
        return contactFacets;
    }

    private List<FacetValue> getAutocompleteResources(AutocompleteRequest autocompleteRequest, ServerSession session) throws OXException {
        //TODO
        return Collections.emptyList();
    }

    private List<FacetValue> getAutocompleteGroups(AutocompleteRequest autocompleteRequest, ServerSession session) throws OXException {
        //TODO
        return Collections.emptyList();
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

}
