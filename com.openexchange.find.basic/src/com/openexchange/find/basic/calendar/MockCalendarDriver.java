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

import static java.util.Collections.singletonList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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
import com.openexchange.find.common.CommonFacetType;
import com.openexchange.find.common.CommonStrings;
import com.openexchange.find.common.ContactDisplayItem;
import com.openexchange.find.common.FolderTypeDisplayItem;
import com.openexchange.find.common.SimpleDisplayItem;
import com.openexchange.find.drive.DriveFacetType;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.Filter;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.search.AppointmentSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.java.Strings;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MockCalendarDriver}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class MockCalendarDriver extends AbstractContactFacetingModuleSearchDriver {

    @Override
    public Module getModule() {
        return Module.CALENDAR;
    }

    @Override
    public boolean isValidFor(ServerSession session) throws OXException {
        return session.getUserConfiguration().hasCalendar() && session.getUserConfiguration().hasContact();
    }

    public void getConfiguration(ServerSession session) throws OXException {
        List<Facet> facets = new LinkedList<Facet>();

        // Subject
        FacetValue subjectValue = new FacetValue("subject", new SimpleDisplayItem("subject"), FacetValue.UNKNOWN_COUNT, new Filter(
            singletonList("subject"),
            "override"));
        Facet subjectFacet = new Facet(CalendarFacetType.SUBJECT, singletonList(subjectValue));
        facets.add(subjectFacet);

        // Description
        FacetValue descriptionValue = new FacetValue("description", new SimpleDisplayItem("description"), FacetValue.UNKNOWN_COUNT, new Filter(
            singletonList("description"),
            "override"));
        Facet descriptionFacet = new Facet(CalendarFacetType.DESCRIPTION, singletonList(descriptionValue));
        facets.add(descriptionFacet);

        // Location
        FacetValue locationValue = new FacetValue("location", new SimpleDisplayItem("location"), FacetValue.UNKNOWN_COUNT, new Filter(
            singletonList("location"),
            "override"));
        Facet locationFacet = new Facet(CalendarFacetType.LOCATION, singletonList(locationValue));
        facets.add(locationFacet);

        // Status
        List<FacetValue> statusValues = new ArrayList<FacetValue>();
        statusValues.add(new FacetValue(
            StatusDisplayItem.Status.ACCEPTED.getIdentifier(),
            new StatusDisplayItem(CalendarStrings.STATUS_ACCEPTED, StatusDisplayItem.Status.ACCEPTED),
            FacetValue.UNKNOWN_COUNT,
            new Filter(singletonList("status"), StatusDisplayItem.Status.ACCEPTED.getIdentifier())));
        statusValues.add(new FacetValue(
            StatusDisplayItem.Status.DECLINED.getIdentifier(),
            new StatusDisplayItem(CalendarStrings.STATUS_DECLINED, StatusDisplayItem.Status.DECLINED),
            FacetValue.UNKNOWN_COUNT,
            new Filter(singletonList("status"), StatusDisplayItem.Status.DECLINED.getIdentifier())));
        statusValues.add(new FacetValue(
            StatusDisplayItem.Status.TENTATIVE.getIdentifier(),
            new StatusDisplayItem(CalendarStrings.STATUS_TENTATIVE, StatusDisplayItem.Status.TENTATIVE),
            FacetValue.UNKNOWN_COUNT,
            new Filter(singletonList("status"), StatusDisplayItem.Status.TENTATIVE.getIdentifier())));
        statusValues.add(new FacetValue(
            StatusDisplayItem.Status.NONE.getIdentifier(),
            new StatusDisplayItem(CalendarStrings.STATUS_NONE, StatusDisplayItem.Status.NONE),
            FacetValue.UNKNOWN_COUNT,
            new Filter(singletonList("status"), StatusDisplayItem.Status.NONE.getIdentifier())));
        Facet statusFacet = new Facet(CalendarFacetType.STATUS, statusValues);
        facets.add(statusFacet);

        // Relative Date
        List<FacetValue> dateValues = new ArrayList<FacetValue>();
        dateValues.add(new FacetValue(
            RelativeDateDisplayItem.RelativeDate.COMING.getIdentifier(),
            new RelativeDateDisplayItem(
            CalendarStrings.RELATIVE_DATE_COMMING,
            RelativeDateDisplayItem.RelativeDate.COMING), FacetValue.UNKNOWN_COUNT, new Filter(
                singletonList("relative_date"),
            RelativeDateDisplayItem.RelativeDate.COMING.getIdentifier())));
        dateValues.add(new FacetValue(
            RelativeDateDisplayItem.RelativeDate.PAST.getIdentifier(),
            new RelativeDateDisplayItem(
            CalendarStrings.RELATIVE_DATE_PAST,
            RelativeDateDisplayItem.RelativeDate.PAST), FacetValue.UNKNOWN_COUNT, new Filter(
                singletonList("relative_date"),
            RelativeDateDisplayItem.RelativeDate.PAST.getIdentifier())));
        Facet dateFacet = new Facet(CalendarFacetType.RELATIVE_DATE, dateValues);
        facets.add(dateFacet);

        // Recurring Type
        List<FacetValue> recurringTypeValues = new ArrayList<FacetValue>();
        recurringTypeValues.add(new FacetValue(
            RecurringTypeDisplayItem.RecurringType.SINGLE.getIdentifier(),
            new RecurringTypeDisplayItem(
            CalendarStrings.RECURRING_TYPE_SINGLE,
            RecurringTypeDisplayItem.RecurringType.SINGLE), FacetValue.UNKNOWN_COUNT, new Filter(
                singletonList("recurring_type"),
            RecurringTypeDisplayItem.RecurringType.SINGLE.getIdentifier())));
        recurringTypeValues.add(new FacetValue(
            RecurringTypeDisplayItem.RecurringType.SERIES.getIdentifier(),
            new RecurringTypeDisplayItem(
            CalendarStrings.RECURRING_TYPE_SERIES,
            RecurringTypeDisplayItem.RecurringType.SERIES), FacetValue.UNKNOWN_COUNT, new Filter(
                singletonList("recurring_type"),
            RecurringTypeDisplayItem.RecurringType.SERIES.getIdentifier())));
        Facet recurringTypeFacet = new Facet(CalendarFacetType.RECURRING_TYPE, recurringTypeValues);
        facets.add(recurringTypeFacet);

        // Folder Types
        List<FacetValue> folderValues = new ArrayList<FacetValue>();
        folderValues.add(new FacetValue(
            FolderTypeDisplayItem.Type.PRIVATE.getIdentifier(),
            new FolderTypeDisplayItem(CommonStrings.FOLDER_TYPE_PRIVATE, FolderTypeDisplayItem.Type.PRIVATE),
            FacetValue.UNKNOWN_COUNT,
            new Filter(singletonList("folder_type"), FolderTypeDisplayItem.Type.PRIVATE.getIdentifier())));
        folderValues.add(new FacetValue(
            FolderTypeDisplayItem.Type.PUBLIC.getIdentifier(),
            new FolderTypeDisplayItem(CommonStrings.FOLDER_TYPE_PUBLIC, FolderTypeDisplayItem.Type.PUBLIC),
            FacetValue.UNKNOWN_COUNT,
            new Filter(singletonList("folder_type"), FolderTypeDisplayItem.Type.PUBLIC.getIdentifier())));
        folderValues.add(new FacetValue(
            FolderTypeDisplayItem.Type.SHARED.getIdentifier(),
            new FolderTypeDisplayItem(CommonStrings.FOLDER_TYPE_SHARED, FolderTypeDisplayItem.Type.SHARED),
            FacetValue.UNKNOWN_COUNT,
            new Filter(singletonList("folder_type"), FolderTypeDisplayItem.Type.SHARED.getIdentifier())));
        Facet folderTypeFacet = new Facet(CommonFacetType.FOLDER_TYPE, folderValues);
        facets.add(folderTypeFacet);
    }

    @Override
    protected String getFormatStringForGlobalFacet() {
        return CalendarStrings.GLOBAL;
    }

    private static final List<String> PARTICIPANT_FILTER = singletonList("participants");

    @Override
    public AutocompleteResult doAutocomplete(AutocompleteRequest autocompleteRequest, ServerSession session) throws OXException {
        List<Facet> facets = new LinkedList<Facet>();

        List<Contact> contacts = autocompleteContacts(session, autocompleteRequest);
        List<FacetValue> values = new ArrayList<FacetValue>();
        for (Contact contact : contacts) {
            if (!Strings.isEmpty(contact.getEmail1())) {
                Filter filter = new Filter(PARTICIPANT_FILTER, contact.getEmail1());
                values.add(new FacetValue(prepareFacetValueId("contact", session.getContextId(), Integer.toString(contact.getObjectID())), new ContactDisplayItem(contact), FacetValue.UNKNOWN_COUNT, filter));
            }
            if (!Strings.isEmpty(contact.getEmail2())) {
                Filter filter = new Filter(PARTICIPANT_FILTER, contact.getEmail2());
                values.add(new FacetValue(prepareFacetValueId("contact", session.getContextId(), Integer.toString(contact.getObjectID())), new ContactDisplayItem(contact), FacetValue.UNKNOWN_COUNT, filter));
            }
            if (!Strings.isEmpty(contact.getEmail3())) {
                Filter filter = new Filter(PARTICIPANT_FILTER, contact.getEmail3());
                values.add(new FacetValue(prepareFacetValueId("contact", session.getContextId(), Integer.toString(contact.getObjectID())), new ContactDisplayItem(contact), FacetValue.UNKNOWN_COUNT, filter));
            }
            if (!Strings.isEmpty(contact.getDisplayName())) {
                Filter filter = new Filter(PARTICIPANT_FILTER, contact.getDisplayName());
                values.add(new FacetValue(prepareFacetValueId("contact", session.getContextId(), Integer.toString(contact.getObjectID())), new ContactDisplayItem(contact), FacetValue.UNKNOWN_COUNT, filter));
            }
        }
        facets.add(new Facet(DriveFacetType.CONTACTS, values));

        return new AutocompleteResult(facets);
    }

    final static int[] FIELDS = {
        DataObject.OBJECT_ID, DataObject.CREATED_BY, DataObject.CREATION_DATE, DataObject.LAST_MODIFIED, DataObject.MODIFIED_BY,
        FolderChildObject.FOLDER_ID, CommonObject.PRIVATE_FLAG, CommonObject.CATEGORIES, CalendarObject.TITLE, Appointment.LOCATION,
        CalendarObject.START_DATE, CalendarObject.END_DATE, CalendarObject.NOTE, CalendarObject.RECURRENCE_TYPE,
        CalendarObject.RECURRENCE_CALCULATOR, CalendarObject.RECURRENCE_ID, CalendarObject.RECURRENCE_POSITION,
        CalendarObject.PARTICIPANTS, CalendarObject.USERS, Appointment.SHOWN_AS, Appointment.DELETE_EXCEPTIONS,
        Appointment.CHANGE_EXCEPTIONS, Appointment.FULL_TIME, Appointment.COLOR_LABEL, Appointment.TIMEZONE, Appointment.ORGANIZER,
        Appointment.ORGANIZER_ID, Appointment.PRINCIPAL, Appointment.PRINCIPAL_ID, Appointment.UID, Appointment.SEQUENCE,
        Appointment.CONFIRMATIONS, Appointment.LAST_MODIFIED_OF_NEWEST_ATTACHMENT, Appointment.NUMBER_OF_ATTACHMENTS };

    @Override
    public SearchResult search(SearchRequest searchRequest, ServerSession session) throws OXException {
        AppointmentSQLInterface appointmentSql = Services.requireService(AppointmentSqlFactoryService.class).createAppointmentSql(session);

        AppointmentSearchObject searchObj = new AppointmentSearchObject();
        OXFolderAccess folderAccess = new OXFolderAccess(session.getContext());
        int folderID = folderAccess.getDefaultFolder(session.getUserId(), FolderObject.CALENDAR).getObjectID();
        searchObj.setFolderIDs(Collections.singleton(Integer.valueOf(folderID)));
        searchObj.setQueries(Collections.singleton("*"));
        SearchIterator<Appointment> appointments = appointmentSql.searchAppointments(
            searchObj,
            Appointment.START_DATE,
            Order.ASCENDING,
            FIELDS);

        List<Document> documents = new LinkedList<Document>();
        while (appointments.hasNext()) {
            documents.add(new CalendarDocument(appointments.next()));
        }
        return new SearchResult(documents.size(), searchRequest.getStart(), documents, searchRequest.getActiveFacets());
    }

}
