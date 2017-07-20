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

package com.openexchange.chronos.json.action;

import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_FIELDS;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_IGNORE_CONFLICTS;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_INCLUDE_PRIVATE;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_NOTIFICATION;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_ORDER;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_ORDER_BY;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_RANGE_END;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_RANGE_START;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_RECURRENCE_MASTER;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_TIMESTAMP;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.DefaultRecurrenceId;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.composition.CompositeEventID;
import com.openexchange.chronos.provider.composition.CompositeFolderID;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccessFactory;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.SortOrder;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ChronosAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class ChronosAction implements AJAXActionService {

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GetAction.class);

    final ServiceLookup services;

    /**
     * Initializes a new {@link AbstractDriveShareAction}.
     *
     * @param services A service lookup reference
     */
    protected ChronosAction(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        IDBasedCalendarAccess calendarAccess = initCalendarAccess(requestData);
        AJAXRequestResult result = perform(calendarAccess, requestData);
        return result;
    }

    /**
     * Performs a request.
     *
     * @param calendarAccess The initialized calendar access to use
     * @param requestData The underlying request data
     * @return The request result
     */
    protected abstract AJAXRequestResult perform(IDBasedCalendarAccess calendarAccess, AJAXRequestData requestData) throws OXException;

    /**
     * Gets a list of required parameter names that will be evaluated. If missing in the request, an appropriate exception is thrown. By
     * default, an empty list is returned.
     *
     * @return The list of required parameters
     */
    protected Set<String> getRequiredParameters() {
        return Collections.emptySet();
    }

    /**
     * Gets a list of parameter names that will be evaluated if set, but are not required to fulfill the request. By default, an empty
     * list is returned.
     *
     * @return The list of optional parameters
     */
    protected Set<String> getOptionalParameters() {
        return Collections.emptySet();
    }

    protected <S extends Object> S requireService(Class<? extends S> clazz) throws OXException {
        return com.openexchange.osgi.Tools.requireService(clazz, services);
    }

    protected CompositeFolderID parseFolderParameter(AJAXRequestData requestData) throws OXException {
        String value = requestData.requireParameter(AJAXServlet.PARAMETER_FOLDERID);
        try {
            return CompositeFolderID.parse(value);
        } catch (IllegalArgumentException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(e, AJAXServlet.PARAMETER_FOLDERID, value);
        }
    }

    protected CompositeEventID parseIdParameter(AJAXRequestData requestData) throws OXException {
        String value = requestData.requireParameter(AJAXServlet.PARAMETER_ID);
        Long recurrenceId = parseRecurrenceIdParameter(requestData);
        //TODO: recurrence id as datetime string
        try {
            if (recurrenceId != null) {
                return new CompositeEventID(CompositeEventID.parse(value), new DefaultRecurrenceId(new DateTime(recurrenceId)));
            } else {
                return CompositeEventID.parse(value);
            }
        } catch (IllegalArgumentException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(e, AJAXServlet.PARAMETER_ID, value);
        }
    }

    private Long parseRecurrenceIdParameter(AJAXRequestData requestData) throws OXException {
        String value = requestData.getParameter(CalendarParameters.PARAMETER_RECURRENCE_ID);
        try {
            return value != null ? Long.valueOf(value) : null;
        } catch (IllegalArgumentException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(e, CalendarParameters.PARAMETER_RECURRENCE_ID, value);
        }
    }

    /**
     * Initializes the calendar access for a request and parses all known parameters supplied by the client, throwing an appropriate
     * exception in case a required parameters is missing.
     *
     * @param requestData The underlying request data
     * @return The initialized calendar access
     */
    protected IDBasedCalendarAccess initCalendarAccess(AJAXRequestData requestData) throws OXException {
        IDBasedCalendarAccess calendarAccess = requireService(IDBasedCalendarAccessFactory.class).createAccess(requestData.getSession());
        Set<String> requiredParameters = getRequiredParameters();
        Set<String> optionalParameters = getOptionalParameters();
        Set<String> parameters = new HashSet<String>();
        parameters.addAll(requiredParameters);
        parameters.addAll(optionalParameters);
        for (String parameter : parameters) {
            Entry<String, ?> entry = parseParameter(requestData, parameter, requiredParameters.contains(parameter));
            if (null != entry) {
                calendarAccess.set(entry.getKey(), entry.getValue());
            }
        }
        return calendarAccess;
    }

    /**
     * Retrieves the given parameter as an Entry object
     *
     * @param request The request
     * @param parameter The parameter name
     * @param required Defines if the parameter is required
     * @return The parameter or null if it isn't required
     * @throws OXException if the parameter is required and can't be found or if the parameter can't be parsed
     */
    protected static Entry<String, ?> parseParameter(AJAXRequestData request, String parameter, boolean required) throws OXException {
        String value = request.getParameter(parameter);
        if (Strings.isEmpty(value)) {
            if (false == required) {
                return null;
            }
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(parameter);
        }
        try {
            return parseParameter(parameter, value);
        } catch (IllegalArgumentException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(e, parameter, value);
        }
    }

    private static Entry<String, ?> parseParameter(String parameter, String value) throws IllegalArgumentException {
        switch (parameter) {
            case "rangeStart":
                DateTime startTime = DateTime.parse(TimeZone.getTimeZone("UTC"), value);
                return new AbstractMap.SimpleEntry<String, Date>(PARAMETER_RANGE_START, new Date(startTime.getTimestamp()));
            case "rangeEnd":
                DateTime endTime = DateTime.parse(TimeZone.getTimeZone("UTC"), value);
                return new AbstractMap.SimpleEntry<String, Date>(PARAMETER_RANGE_END, new Date(endTime.getTimestamp()));
            case "expand":
                return new AbstractMap.SimpleEntry<String, Boolean>(PARAMETER_RECURRENCE_MASTER, Boolean.valueOf(false == Boolean.parseBoolean(value)));
            case PARAMETER_IGNORE_CONFLICTS:
                return new AbstractMap.SimpleEntry<String, Boolean>(PARAMETER_IGNORE_CONFLICTS, Boolean.parseBoolean(value));
            case PARAMETER_TIMESTAMP:
                return new AbstractMap.SimpleEntry<String, Long>(PARAMETER_TIMESTAMP, Long.parseLong(value));
            case PARAMETER_ORDER_BY:
                return new AbstractMap.SimpleEntry<String, EventField>(PARAMETER_ORDER_BY, EventField.valueOf(value.toUpperCase()));
            case PARAMETER_ORDER:
                return new AbstractMap.SimpleEntry<String, SortOrder.Order>(PARAMETER_ORDER, SortOrder.Order.parse(value, SortOrder.Order.ASC));
            case PARAMETER_FIELDS:
                return new AbstractMap.SimpleEntry<String, EventField[]>(PARAMETER_FIELDS, parseFields(value));
            case PARAMETER_INCLUDE_PRIVATE:
                return new AbstractMap.SimpleEntry<String, Boolean>(PARAMETER_INCLUDE_PRIVATE, Boolean.parseBoolean(value));
            case "sendInternalNotifications":
                return new AbstractMap.SimpleEntry<String, Boolean>(PARAMETER_NOTIFICATION, Boolean.parseBoolean(value));
            default:
                throw new IllegalArgumentException("unknown paramter: " + parameter);
        }
    }

    private static EventField[] parseFields(String value){
        if(Strings.isEmpty(value)){
            return new EventField[0];
        }

        String[] splitByColon = Strings.splitByComma(value);
        EventField[] fields = new EventField[splitByColon.length];
        int x=0;
        for(String str: splitByColon){
            fields[x++] = EventField.valueOf(str.toUpperCase());
        }
        return fields;
    }

    protected boolean isConflict(OXException e){
        return Category.CATEGORY_CONFLICT.equals(e.getCategory()) && (CalendarExceptionCodes.HARD_EVENT_CONFLICTS.equals(e) || CalendarExceptionCodes.EVENT_CONFLICTS.equals(e));
    }

}
