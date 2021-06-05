/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.halo.events;

import static com.openexchange.chronos.common.CalendarUtils.getMaximumTimestamp;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_ORDER;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_ORDER_BY;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_RANGE_END;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_RANGE_START;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultSearchFilter;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccessFactory;
import com.openexchange.chronos.service.EventsResult;
import com.openexchange.chronos.service.SearchFilter;
import com.openexchange.chronos.service.SortOrder.Order;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.calendar.contentType.CalendarContentType;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.groupware.container.Contact;
import com.openexchange.halo.AbstractContactHalo;
import com.openexchange.halo.HaloContactDataSource;
import com.openexchange.halo.HaloContactQuery;
import com.openexchange.java.util.TimeZones;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

/**
 * {@link EventsContactHalo}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class EventsContactHalo extends AbstractContactHalo implements HaloContactDataSource {

    private final IDBasedCalendarAccessFactory calendarAccessFactory;
    private final FolderService folderService;

    /**
     * Initializes a new {@link EventsContactHalo}.
     *
     * @param calendarAccessFactory The calendar access factory
     * @param folderService
     */
    public EventsContactHalo(IDBasedCalendarAccessFactory calendarAccessFactory, FolderService folderService) {
        this.calendarAccessFactory = calendarAccessFactory;
        this.folderService = folderService;
    }

    @Override
    public String getId() {
        return "com.openexchange.halo.events";
    }

    @Override
    public boolean isAvailable(ServerSession session) {
        return session.getUserPermissionBits().hasCalendar();
    }

    @Override
    public AJAXRequestResult investigate(HaloContactQuery query, AJAXRequestData request, ServerSession session) throws OXException {
        /*
         * extract search filters from halo query
         */
        List<SearchFilter> filters = getSearchFilters(query, session);
        if (null == filters || 0 == filters.size()) {
            return AJAXRequestResult.EMPTY_REQUEST_RESULT;
        }
        /*
         * init calendar access, search matching events & return appropriate result
         */
        Map<String, EventsResult> resultsPerFolder = null;
        IDBasedCalendarAccess calendarAccess = initCalendarAccess(request);
        boolean committed = false;
        List<String> folders = visiblePrivateAndPublicFolders(session);
        if (folders == null || folders.isEmpty()) {
            return AJAXRequestResult.EMPTY_REQUEST_RESULT;
        }
        try {
            calendarAccess.startTransaction();
            resultsPerFolder = calendarAccess.searchEvents(folders, filters, null);
            calendarAccess.commit();
            committed = true;
        } finally {
            if (false == committed) {
                calendarAccess.rollback();
            }
            calendarAccess.finish();
        }
        Map<String, Event> events = new HashMap<>();
        for (Entry<String, EventsResult> entry : resultsPerFolder.entrySet()) {
            List<Event> eventsPerFolder = entry.getValue().getEvents();
            if (null != eventsPerFolder) {
                for (Event event : eventsPerFolder) {
                    events.put(event.getId(), event);
                }
            }
        }
        AJAXRequestResult result = new AJAXRequestResult(new ArrayList<>(events.values()), getMaximumTimestamp(events.values()), "event");
        List<OXException> warnings = calendarAccess.getWarnings();
        if (null != warnings && 0 < warnings.size()) {
            result.addWarnings(warnings);
        }
        return result;
    }

    private List<String> visiblePrivateAndPublicFolders(ServerSession session) throws OXException {
        List<String> folderIDs = new LinkedList<String>();
        FolderResponse<UserizedFolder[]> visibleFolders = folderService.getVisibleFolders(FolderStorage.REAL_TREE_ID, CalendarContentType.getInstance(), PrivateType.getInstance(), false, session, null);
        UserizedFolder[] folders = visibleFolders.getResponse();
        if (folders != null) {
            for (UserizedFolder folder : folders) {
                if (folder.getOwnPermission().getReadPermission() >= Permission.READ_OWN_OBJECTS) {
                    folderIDs.add(folder.getID());
                }
            }
        }
        visibleFolders = folderService.getVisibleFolders(FolderStorage.REAL_TREE_ID, CalendarContentType.getInstance(), PublicType.getInstance(), false, session, null);
        folders = visibleFolders.getResponse();
        if (folders != null) {
            for (UserizedFolder folder : folders) {
                if (folder.getOwnPermission().getReadPermission() >= Permission.READ_OWN_OBJECTS) {
                    folderIDs.add(folder.getID());
                }
            }
        }
        return folderIDs;
    }

    private IDBasedCalendarAccess initCalendarAccess(AJAXRequestData request) throws OXException {
        IDBasedCalendarAccess calendarAccess = calendarAccessFactory.createAccess(request.getSession());
        String rangeStartParameter = request.checkParameter(PARAMETER_RANGE_START);
        try {
            calendarAccess.set(PARAMETER_RANGE_START, new Date(DateTime.parse(TimeZones.UTC, rangeStartParameter).getTimestamp()));
        } catch (IllegalArgumentException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(e, PARAMETER_RANGE_START, rangeStartParameter);
        }
        String rangeEndParameter = request.checkParameter(PARAMETER_RANGE_END);
        try {
            calendarAccess.set(PARAMETER_RANGE_END, new Date(DateTime.parse(TimeZones.UTC, rangeEndParameter).getTimestamp()));
        } catch (IllegalArgumentException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(e, PARAMETER_RANGE_END, rangeEndParameter);
        }
        calendarAccess.set(PARAMETER_ORDER_BY, EventField.START_DATE);
        calendarAccess.set(PARAMETER_ORDER, Order.ASC);
        return calendarAccess;
    }

    private List<SearchFilter> getSearchFilters(HaloContactQuery query, ServerSession session) {
        if (null != query.getUser()) {
            return Collections.singletonList(getUserFilter(query.getUser(), session));
        }
        if (null != query.getContact()) {
            SearchFilter filter = getParticipantFilter(query.getContact());
            if (null != filter) {
                return Arrays.asList(filter, getUserFilter(null, session));
            }
        }
        if (null != query.getMergedContacts() && 0 < query.getMergedContacts().size()) {
            for (Contact contact : query.getMergedContacts()) {
                SearchFilter filter = getParticipantFilter(contact);
                if (null != filter) {
                    return Arrays.asList(filter, getUserFilter(null, session));
                }
            }
        }
        return null;
    }

    private SearchFilter getParticipantFilter(Contact contact) {
        List<String> addresses = getEMailAddresses(contact);
        if (null == addresses || 0 == addresses.size()) {
            return null;
        }
        List<String> queries = new ArrayList<String>(addresses.size());
        for (String address : addresses) {
            queries.add(CalendarUtils.getURI(address));
        }
        return new DefaultSearchFilter("participants", queries);
    }

    private SearchFilter getUserFilter(User user, ServerSession session) {
        List<String> queries = new ArrayList<>();
        queries.add(String.valueOf(session.getUserId()));
        if (user != null) {
            queries.add(String.valueOf(user.getId()));
        }
        return new DefaultSearchFilter("users", queries);
    }

}
