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

package com.openexchange.halo.events;

import static com.openexchange.chronos.common.CalendarUtils.getMaximumTimestamp;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_EXPAND_OCCURRENCES;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_ORDER;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_ORDER_BY;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_RANGE_END;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_RANGE_START;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultSearchFilter;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccessFactory;
import com.openexchange.chronos.service.SearchFilter;
import com.openexchange.chronos.service.SortOrder.Order;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.User;
import com.openexchange.halo.AbstractContactHalo;
import com.openexchange.halo.HaloContactDataSource;
import com.openexchange.halo.HaloContactQuery;
import com.openexchange.java.util.TimeZones;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link EventsContactHalo}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class EventsContactHalo extends AbstractContactHalo implements HaloContactDataSource {

    private final IDBasedCalendarAccessFactory calendarAccessFactory;

    /**
     * Initializes a new {@link EventsContactHalo}.
     *
     * @param calendarAccessFactory The calendar access factory
     */
    public EventsContactHalo(IDBasedCalendarAccessFactory calendarAccessFactory) {
        this.calendarAccessFactory = calendarAccessFactory;
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
        List<SearchFilter> filters = getSearchFilters(query);
        if (null == filters || 0 == filters.size()) {
            return AJAXRequestResult.EMPTY_REQUEST_RESULT;
        }
        /*
         * init calendar access, search matching events & return appropriate result
         */
        List<Event> events = initCalendarAccess(request).searchEvents(null, filters, null);
        return new AJAXRequestResult(events, new Date(getMaximumTimestamp(events)), "event");
    }

    private IDBasedCalendarAccess initCalendarAccess(AJAXRequestData request) throws OXException {
        IDBasedCalendarAccess calendarAccess = calendarAccessFactory.createAccess(request.getSession());
        String rangeStartParameter = request.checkParameter("rangeStart");
        try {
            DateTime rangeStart = DateTime.parse(TimeZones.UTC, rangeStartParameter);
            calendarAccess.set(PARAMETER_RANGE_START, new Date(rangeStart.getTimestamp()));
        } catch (IllegalArgumentException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(e, "rangeStart", rangeStartParameter);
        }
        String rangeEndParameter = request.checkParameter("rangeEnd");
        try {
            DateTime rangeEnd = DateTime.parse(TimeZones.UTC, rangeEndParameter);
            calendarAccess.set(PARAMETER_RANGE_END, new Date(rangeEnd.getTimestamp()));
        } catch (IllegalArgumentException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(e, "rangeEnd", rangeEndParameter);
        }
        calendarAccess.set(PARAMETER_EXPAND_OCCURRENCES, Boolean.FALSE);
        calendarAccess.set(PARAMETER_ORDER_BY, EventField.START_DATE);
        calendarAccess.set(PARAMETER_ORDER, Order.ASC);
        return calendarAccess;
    }

    private List<SearchFilter> getSearchFilters(HaloContactQuery query) {
        if (null != query.getUser()) {
            return Collections.singletonList(getUserFilter(query.getUser()));
        }
        if (null != query.getContact()) {
            SearchFilter filter = getParticipantFilter(query.getContact());
            if (null != filter) {
                return Collections.singletonList(filter);
            }
        }
        if (null != query.getMergedContacts() && 0 < query.getMergedContacts().size()) {
            for (Contact contact : query.getMergedContacts()) {
                SearchFilter filter = getParticipantFilter(contact);
                if (null != filter) {
                    return Collections.singletonList(filter);
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
        return new DefaultSearchFilter("participant", queries);
    }

    private SearchFilter getUserFilter(User user) {
        return new DefaultSearchFilter("users", Collections.singletonList(String.valueOf(user.getId())));
    }

}
