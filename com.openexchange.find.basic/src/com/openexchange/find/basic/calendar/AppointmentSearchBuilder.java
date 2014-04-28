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

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.find.FindExceptionCode;
import com.openexchange.find.Module;
import com.openexchange.find.basic.Services;
import com.openexchange.find.calendar.CalendarFacetType;
import com.openexchange.find.calendar.RecurringTypeDisplayItem;
import com.openexchange.find.calendar.RelativeDateDisplayItem;
import com.openexchange.find.calendar.StatusDisplayItem;
import com.openexchange.find.facet.Filter;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.CalendarContentType;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.search.AppointmentSearchObject;
import com.openexchange.java.SearchStrings;
import com.openexchange.java.Strings;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AppointmentSearchBuilder}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class AppointmentSearchBuilder {

    private final AppointmentSearchObject appointmentSearch;
    private final ServerSession session;
    private boolean hasFolderFilter;

    /**
     * Initializes a new {@link AppointmentSearchBuilder}.
     */
    public AppointmentSearchBuilder(ServerSession session) {
        super();
        this.session = session;
        this.appointmentSearch = new AppointmentSearchObject();
    }

    /**
     * Gets the constructed appointment search.
     *
     * @return The appointment search
     */
    public AppointmentSearchObject build() {
        return appointmentSearch;
    }

    /**
     * Gets a value indicating whether this appointment search indicates a <code>FALSE</code> condition with empty results.
     *
     * @return <code>true</code> if this search will lead to no results, <code>false</code>, otherwise
     */
    public boolean isFalse() {
        if (hasFolderFilter) {
            Set<Integer> folderIDs = appointmentSearch.getFolderIDs();
            if (null == folderIDs || 0 == folderIDs.size()) {
                return true; // folders specified in filter, but no suitable folders available
            }
        }
        return false;
    }

    /**
     * Applies the supplied filter to the constructed search.
     *
     * @param filter The filter to add
     * @return The builder
     * @throws OXException
     */
    public AppointmentSearchBuilder applyFilter(Filter filter) throws OXException {
        List<String> fields = filter.getFields();
        for (String field : fields) {
            if (CalendarFacetType.SUBJECT.getId().equals(field)) {
                applySubject(filter.getQueries());
            } else if (CalendarFacetType.DESCRIPTION.getId().equals(field)) {
                applyDescription(filter.getQueries());
            } else if (CalendarFacetType.LOCATION.getId().equals(field)) {
                applyLocation(filter.getQueries());
            } else if (CalendarFacetType.ATTACHMENT_NAME.getId().equals(field)) {
                applyAttachmentName(filter.getQueries());
            } else if (CalendarFacetType.RELATIVE_DATE.getId().equals(field)) {
                applyRelativeDate(filter.getQueries());
            } else if (CalendarFacetType.STATUS.getId().equals(field)) {
                applyStatus(filter.getQueries());
            } else if (CalendarFacetType.RECURRING_TYPE.getId().equals(field)) {
                applyRecurringType(filter.getQueries());
            } else if ("participants".equals(field)) {
                applyParticipants(filter.getQueries());
            } else if ("users".equals(field)) {
                applyUsers(filter.getQueries());
            } else {
                throw FindExceptionCode.UNSUPPORTED_FILTER_FIELD.create(field);
            }
        }
        return this;
    }

    /**
     * Applies the supplied filters to the constructed search.
     *
     * @param filters The filters to add
     * @return The builder
     * @throws OXException
     */
    public AppointmentSearchBuilder applyFilters(List<Filter> filters) throws OXException {
        for (Filter filter : filters) {
            applyFilter(filter);
        }
        return this;
    }

    private AppointmentSearchBuilder applyQuery(String query) throws OXException {
        if (false == isWildcardOnly(query)) {
            Set<String> queries = appointmentSearch.getQueries();
            if (null == queries) {
                queries = new HashSet<String>();
            }
            checkPatternLength(query);
            queries.add(addWildcards(query, true, true));
            appointmentSearch.setQueries(queries);
        }
        return this;
    }

    /**
     * Applies the supplied general queries to the search.
     *
     * @param queries The queries to append
     * @return The builder
     * @throws OXException
     */
    public AppointmentSearchBuilder applyQueries(List<String> queries) throws OXException {
        for (String query : queries) {
            applyQuery(query);
        }
        return this;
    }

    /**
     * Applies folder IDs to the search, depending on the existence of a specific
     * folder ID or a folder type.
     *
     * @param folderID The folder ID to apply, or <code>null</code> if not specified
     * @param folderType The folder type for that all folder shall be applied. -1 if not specified.
     * @return The builder
     * @throws OXException
     */
    public AppointmentSearchBuilder applyFolders(String folderID, int folderType) throws OXException {
        final Set<Integer> folderIDs;
        if (null == folderID) {
            Type type = null;
            if (FolderObject.PRIVATE == folderType) {
                type = PrivateType.getInstance();
            } else if (FolderObject.PUBLIC == folderType) {
                type = PublicType.getInstance();
            } else if (FolderObject.SHARED == folderType) {
                type = SharedType.getInstance();
            }

            if (type == null) {
                folderIDs = null;
            } else {
                hasFolderFilter = true;
                folderIDs = new HashSet<Integer>();
                FolderResponse<UserizedFolder[]> visibleFolders = Services.getFolderService().getVisibleFolders(
                    FolderStorage.REAL_TREE_ID, CalendarContentType.getInstance(), type, false, session, null);
                UserizedFolder[] folders = visibleFolders.getResponse();
                if (null != folders && 0 < folders.length) {
                    for (UserizedFolder folder : folders) {
                        try {
                            folderIDs.add(Integer.valueOf(folder.getID()));
                        } catch (NumberFormatException e) {
                            throw FindExceptionCode.INVALID_FOLDER_ID.create(folder.getID(), Module.CALENDAR.getIdentifier());
                        }
                    }
                }
            }
        } else {
            try {
                folderIDs = Collections.singleton(Integer.valueOf(folderID));
            } catch (NumberFormatException e) {
                throw FindExceptionCode.INVALID_FOLDER_ID.create(folderID, Module.CALENDAR.getIdentifier());
            }
        }

        appointmentSearch.setFolderIDs(folderIDs);
        return this;
    }

    private void applyParticipants(List<String> queries) throws OXException {
        Set<String> mailAddresses = new HashSet<String>();
        for (String query : queries) {
            if (false == isWildcardOnly(query)) {
                mailAddresses.add(query);
            }
        }
        if (0 < mailAddresses.size()) {
            Set<Set<String>> externalParticipants = appointmentSearch.getExternalParticipants();
            if (null == externalParticipants) {
                externalParticipants = new HashSet<Set<String>>();
            }
            externalParticipants.add(mailAddresses);
            appointmentSearch.setExternalParticipants(externalParticipants);
        }
    }

    private void applyUsers(List<String> queries) throws OXException {
        Set<Integer> userIDs = appointmentSearch.getUserIDs();
        if (null == userIDs) {
            userIDs = new HashSet<Integer>();
        }
        for (String query : queries) {
            try {
                userIDs.add(Integer.valueOf(query));
            } catch (NumberFormatException e) {
                throw FindExceptionCode.UNSUPPORTED_FILTER_QUERY.create(e, query);
            }
        }
        appointmentSearch.setUserIDs(userIDs);
    }

    private void applyStatus(List<String> queries) throws OXException {
        Set<Integer> ownStatus = appointmentSearch.getOwnStatus();
        if (null == ownStatus) {
            ownStatus = new HashSet<Integer>();
        }
        for (String query : queries) {
            int status;
            if (StatusDisplayItem.Status.ACCEPTED.getIdentifier().equals(query)) {
                status = CalendarDataObject.ACCEPT;
            } else if (StatusDisplayItem.Status.NONE.getIdentifier().equals(query)) {
                status = CalendarDataObject.NONE;
            } else if (StatusDisplayItem.Status.TENTATIVE.getIdentifier().equals(query)) {
                status = CalendarDataObject.TENTATIVE;
            } else if (StatusDisplayItem.Status.DECLINED.getIdentifier().equals(query)) {
                status = CalendarDataObject.DECLINE;
            } else {
                throw FindExceptionCode.UNSUPPORTED_FILTER_QUERY.create(query, CalendarFacetType.STATUS.getId());
            }
            ownStatus.add(status);
        }
        appointmentSearch.setOwnStatus(ownStatus);
    }

    private void applyRecurringType(List<String> queries) throws OXException {
        for (String query : queries) {
            if (RecurringTypeDisplayItem.RecurringType.SERIES.getIdentifier().equals(query)) {
                appointmentSearch.setExcludeNonRecurringAppointments(true);
            } else if (RecurringTypeDisplayItem.RecurringType.SINGLE.getIdentifier().equals(query)) {
                appointmentSearch.setExcludeRecurringAppointments(true);
            } else {
                throw FindExceptionCode.UNSUPPORTED_FILTER_QUERY.create(query, CalendarFacetType.RECURRING_TYPE.getId());
            }
        }
    }

    private void applyRelativeDate(List<String> queries) throws OXException {
        for (String query : queries) {
            if (RelativeDateDisplayItem.RelativeDate.COMING.getIdentifier().equals(query)) {
                appointmentSearch.setMinimumEndDate(new Date());
            } else if (RelativeDateDisplayItem.RelativeDate.PAST.getIdentifier().equals(query)) {
                appointmentSearch.setMaximumStartDate(new Date());
            } else {
                throw FindExceptionCode.UNSUPPORTED_FILTER_QUERY.create(query, CalendarFacetType.RELATIVE_DATE.getId());
            }
        }
    }

    private void applySubject(List<String> queries) throws OXException {
        Set<String> titles = appointmentSearch.getTitles();
        if (null == titles) {
            titles = new HashSet<String>();
        }
        for (String query : queries) {
            checkPatternLength(query);
            titles.add(addWildcards(query, true, true));
        }
        appointmentSearch.setTitles(titles);
    }

    private void applyLocation(List<String> queries) throws OXException {
        Set<String> locations = appointmentSearch.getLocations();
        if (null == locations) {
            locations = new HashSet<String>();
        }
        for (String query : queries) {
            checkPatternLength(query);
            locations.add(addWildcards(query, true, true));
        }
        appointmentSearch.setLocations(locations);
    }

    private void applyAttachmentName(List<String> queries) throws OXException {
        Set<String> attachmentNames = appointmentSearch.getAttachmentNames();
        if (null == attachmentNames) {
            attachmentNames = new HashSet<String>();
        }
        for (String query : queries) {
            checkPatternLength(query);
            attachmentNames.add(addWildcards(query, true, true));
        }
        appointmentSearch.setAttachmentNames(attachmentNames);
    }

    private void applyDescription(List<String> queries) throws OXException {
        Set<String> notes = appointmentSearch.getNotes();
        if (null == notes) {
            notes = new HashSet<String>();
        }
        for (String query : queries) {
            checkPatternLength(query);
            notes.add(addWildcards(query, true, true));
        }
        appointmentSearch.setNotes(notes);
    }

    private static boolean isWildcardOnly(String query) {
        return Strings.isEmpty(query) || "*".equals(query);
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

    private static void checkPatternLength(String pattern) throws OXException {
        int minimumSearchCharacters = ServerConfig.getInt(ServerConfig.Property.MINIMUM_SEARCH_CHARACTERS);
        if (null != pattern && 0 < minimumSearchCharacters && SearchStrings.lengthWithoutWildcards(pattern) < minimumSearchCharacters) {
            throw FindExceptionCode.QUERY_TOO_SHORT.create(Integer.valueOf(minimumSearchCharacters));
        }
    }

}
