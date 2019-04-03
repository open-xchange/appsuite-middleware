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

import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_CHECK_CONFLICTS;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_EXPAND_OCCURRENCES;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_FIELDS;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_MASK_ID;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_NOTIFICATION;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_ORDER;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_ORDER_BY;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_PUSH_TOKEN;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_RANGE_END;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_RANGE_START;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_UPDATE_CACHE;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.dmfs.rfc5545.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.DefaultRecurrenceId;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.json.converter.mapper.EventMapper;
import com.openexchange.chronos.json.fields.ChronosJsonFields;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.SortOrder;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.java.util.TimeZones;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;

/**
 * {@link AbstractChronosAction}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public abstract class AbstractChronosAction implements AJAXActionService {

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractChronosAction.class);
    protected final ServiceLookup services;

    protected static final String PARAM_RANGE_START = "rangeStart";
    protected static final String PARAM_RANGE_END = "rangeEnd";
    protected static final String PARAM_EXPAND = "expand";
    protected static final String PARAM_CHECK_CONFLICTS = "checkConflicts";
    protected static final String PARAM_SEND_INTERNAL_NOTIFICATIONS = "sendInternalNotifications";
    protected static final String PARAM_RECURRENCE_ID = "recurrenceId";
    protected static final String PARAM_RECURRENCE_RANGE = "recurrenceRange";

    /**
     * Initializes a new {@link AbstractChronosAction}.
     */
    public AbstractChronosAction(ServiceLookup services) {
        this.services = services;
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

    /**
     * Constructs a full event identifier from the supplied folder-, object- and recurrence-id values.
     *
     * @param folderId The folder identifier
     * @param objectId The object identifier
     * @param optRecurrenceId The recurrence identifier value, or <code>null</code> if not set
     * @param optRecurrenceRange The recurrence range value, or <code>null</code> if not set
     * @return The full event identifier
     */
    protected EventID getEventID(String folderId, String objectId, String optRecurrenceId, String optRecurrenceRange) throws OXException {
        if (null == optRecurrenceId) {
            return new EventID(folderId, objectId);
        }
        try {
            return new EventID(folderId, objectId, new DefaultRecurrenceId(optRecurrenceId, optRecurrenceRange));
        } catch (IllegalArgumentException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(PARAM_RECURRENCE_ID, optRecurrenceId);
        }
    }

    /**
     * Parses a full event identifier based on the request parameters {@link AJAXServlet#PARAMETER_ID},
     * {@link AJAXServlet#PARAMETER_FOLDERID} and {@link AbstractChronosAction#PARAM_RECURRENCE_ID}.
     *
     * @param requestData The request data to parse the event identifier from
     * @return The parsed full event identifier
     */
    protected EventID parseIdParameter(AJAXRequestData requestData) throws OXException {
        String objectId = requestData.requireParameter(AJAXServlet.PARAMETER_ID);
        String folderId = requestData.requireParameter(AJAXServlet.PARAMETER_FOLDERID);
        String optRecurrenceId = requestData.getParameter(PARAM_RECURRENCE_ID);
        String optRecurrenceRange = requestData.getParameter(PARAM_RECURRENCE_RANGE);
        return getEventID(folderId, objectId, optRecurrenceId, optRecurrenceRange);
    }

    /**
     * Parses a full event identifier based on the request parameters {@link AJAXServlet#PARAMETER_ID},
     * {@link AJAXServlet#PARAMETER_FOLDERID} and {@link AbstractChronosAction#PARAM_RECURRENCE_ID}.
     *
     * @param jsonObject The JSON object to parse the event identifier from
     * @return The parsed full event identifier
     */
    protected EventID parseIdParameter(JSONObject jsonObject) throws OXException {
        String objectId;
        try {
            objectId = jsonObject.getString(AJAXServlet.PARAMETER_ID);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(e, AJAXServlet.PARAMETER_ID);
        }
        String folderId;
        try {
            folderId = jsonObject.getString(AJAXServlet.PARAMETER_FOLDERID);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(e, AJAXServlet.PARAMETER_FOLDERID);
        }
        String optRecurrenceId = jsonObject.optString(PARAM_RECURRENCE_ID, null);
        String optRecurrenceRange = jsonObject.optString(PARAM_RECURRENCE_RANGE, null);
        return getEventID(folderId, objectId, optRecurrenceId, optRecurrenceRange);
    }

    /**
     * Parses an array of full event identifiers, i.e. a JSON array with json object elements having the properties
     * {@link AJAXServlet#PARAMETER_ID}, {@link AJAXServlet#PARAMETER_FOLDERID} and optionally
     * {@link AbstractChronosAction#PARAM_RECURRENCE_ID}.
     *
     * @param jsonArray The json array off full event identifiers to parse
     * @return The parsed full event identifiers
     */
    protected List<EventID> parseEventIDs(JSONArray jsonArray) throws OXException {
        if (null == jsonArray) {
            return null;
        }
        List<EventID> eventIDs = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                eventIDs.add(parseIdParameter(jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e, e.getMessage());
            }
        }
        return eventIDs;
    }

    /**
     * Parses the attachment identifier from given request data
     *
     * @param requestData The request data
     * @return The parsed attachment identifier
     * @throws OXException If attachment identifier cannot be parsed
     */
    protected int parseAttachmentId(AJAXRequestData requestData) throws OXException {
        String managedId = requestData.requireParameter(ChronosJsonFields.Attachment.MANAGED_ID);
        try {
            return Integer.parseInt(managedId);
        } catch (NumberFormatException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER.create(e, ChronosJsonFields.Attachment.MANAGED_ID);
        }
    }

    protected long parseClientTimestamp(AJAXRequestData requestData) throws OXException {
        String parameter = requestData.checkParameter(AJAXServlet.PARAMETER_TIMESTAMP);
        try {
            return Long.parseLong(parameter.trim());
        } catch (NumberFormatException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(AJAXServlet.PARAMETER_TIMESTAMP, parameter);
        }
    }

    private static Entry<String, ?> parseParameter(String parameter, String value) throws IllegalArgumentException {
        switch (parameter) {
            case PARAM_RANGE_START:
                DateTime startTime = DateTime.parse(TimeZones.UTC, value);
                return new AbstractMap.SimpleEntry<String, Date>(PARAMETER_RANGE_START, new Date(startTime.getTimestamp()));
            case PARAM_RANGE_END:
                DateTime endTime = DateTime.parse(TimeZones.UTC, value);
                return new AbstractMap.SimpleEntry<String, Date>(PARAMETER_RANGE_END, new Date(endTime.getTimestamp()));
            case PARAM_EXPAND:
                return new AbstractMap.SimpleEntry<String, Boolean>(PARAMETER_EXPAND_OCCURRENCES, Boolean.valueOf(value));
            case PARAM_CHECK_CONFLICTS:
                return new AbstractMap.SimpleEntry<String, Boolean>(PARAMETER_CHECK_CONFLICTS, Boolean.valueOf(value));
            case PARAMETER_ORDER_BY:
                EventField mappedField = EventMapper.getInstance().getMappedField(value);
                if (mappedField == null) {
                    mappedField = EventField.valueOf(value.toUpperCase());
                }
                return new AbstractMap.SimpleEntry<String, EventField>(PARAMETER_ORDER_BY, mappedField);
            case PARAMETER_ORDER:
                return new AbstractMap.SimpleEntry<String, SortOrder.Order>(PARAMETER_ORDER, SortOrder.Order.parse(value, SortOrder.Order.ASC));
            case PARAMETER_FIELDS:
                return new AbstractMap.SimpleEntry<String, EventField[]>(PARAMETER_FIELDS, parseFields(value));
            case PARAM_SEND_INTERNAL_NOTIFICATIONS:
                return new AbstractMap.SimpleEntry<String, Boolean>(PARAMETER_NOTIFICATION, Boolean.valueOf(value));
            case PARAMETER_MASK_ID:
                return new AbstractMap.SimpleEntry<String, String>(PARAMETER_MASK_ID, value);
            case PARAMETER_PUSH_TOKEN:
                return new AbstractMap.SimpleEntry<String, String>(PARAMETER_PUSH_TOKEN, value);
            case PARAMETER_UPDATE_CACHE:
                return new AbstractMap.SimpleEntry<String, Boolean>(PARAMETER_UPDATE_CACHE, Boolean.valueOf(value));
            default:
                return null;
        }
    }

    private static EventField[] parseFields(String value) {
        if(Strings.isEmpty(value)){
            return new EventField[0];
        }

        String[] splitByColon = Strings.splitByComma(value);
        EventField[] fields = new EventField[splitByColon.length];
        int x=0;
        for(String str: splitByColon){
            EventField mappedField = EventMapper.getInstance().getMappedField(str);
            if (mappedField == null) {
                mappedField = EventField.valueOf(str.toUpperCase());
            }
            fields[x++] = mappedField;
        }
        return fields;
    }

    protected boolean isConflict(OXException e) {
        return Category.CATEGORY_CONFLICT.equals(e.getCategory()) && (CalendarExceptionCodes.HARD_EVENT_CONFLICTS.equals(e) || CalendarExceptionCodes.EVENT_CONFLICTS.equals(e));
    }

}

