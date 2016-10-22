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

package com.openexchange.calendar.json.actions;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.calendar.json.AppointmentAJAXRequest;
import com.openexchange.calendar.json.AppointmentActionFactory;
import com.openexchange.calendar.json.actions.chronos.ChronosAction;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventID;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link DeleteAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
@OAuthAction(AppointmentActionFactory.OAUTH_WRITE_SCOPE)
public final class DeleteAction extends ChronosAction {

    /**
     * Initializes a new {@link DeleteAction}.
     *
     * @param services
     */
    public DeleteAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final AppointmentAJAXRequest req) throws OXException, JSONException {
        Date timestamp = req.checkDate(AJAXServlet.PARAMETER_TIMESTAMP);
        if (req.getData() instanceof JSONObject) {
            final JSONObject jData = req.getData();
            final CalendarDataObject appointmentObj = new CalendarDataObject();
            appointmentObj.setObjectID(DataParser.checkInt(jData, DataFields.ID));
            final int inFolder = DataParser.checkInt(jData, AJAXServlet.PARAMETER_INFOLDER);
            if (jData.has(CalendarFields.RECURRENCE_POSITION)) {
                appointmentObj.setRecurrencePosition(DataParser.checkInt(jData, CalendarFields.RECURRENCE_POSITION));
            } else if (jData.has(CalendarFields.RECURRENCE_DATE_POSITION)) {
                appointmentObj.setRecurrenceDatePosition(DataParser.checkDate(jData, CalendarFields.RECURRENCE_DATE_POSITION));

            }
            final ServerSession session = req.getSession();
            appointmentObj.setContext(session.getContext());
            final AppointmentSqlFactoryService factoryService = getService();
            if (null == factoryService) {
                throw ServiceExceptionCode.absentService(AppointmentSqlFactoryService.class);
            }
            final AppointmentSQLInterface appointmentsql = factoryService.createAppointmentSql(session);
            try {
                appointmentsql.deleteAppointmentObject(appointmentObj, inFolder, timestamp);
                if (appointmentObj.getLastModified() != null) {
                    timestamp = appointmentObj.getLastModified();
                }
            } catch (final SQLException e) {
                throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(e, new Object[0]);
            }
        } else if (req.getData() instanceof JSONArray) {
            JSONArray jsonArray = req.getData();
            for (int i = 0; i < jsonArray.length(); i++) {
                final JSONObject jData = jsonArray.getJSONObject(i);
                final CalendarDataObject appointmentObj = new CalendarDataObject();
                appointmentObj.setObjectID(DataParser.checkInt(jData, DataFields.ID));
                final int inFolder = DataParser.checkInt(jData, AJAXServlet.PARAMETER_INFOLDER);
                if (jData.has(CalendarFields.RECURRENCE_POSITION)) {
                    appointmentObj.setRecurrencePosition(DataParser.checkInt(jData, CalendarFields.RECURRENCE_POSITION));
                } else if (jData.has(CalendarFields.RECURRENCE_DATE_POSITION)) {
                    appointmentObj.setRecurrenceDatePosition(DataParser.checkDate(jData, CalendarFields.RECURRENCE_DATE_POSITION));

                }
                final ServerSession session = req.getSession();
                appointmentObj.setContext(session.getContext());
                final AppointmentSqlFactoryService factoryService = getService();
                if (null == factoryService) {
                    throw ServiceExceptionCode.absentService(AppointmentSqlFactoryService.class);
                }
                final AppointmentSQLInterface appointmentsql = factoryService.createAppointmentSql(session);
                try {
                    appointmentsql.deleteAppointmentObject(appointmentObj, inFolder, timestamp);
                    if (appointmentObj.getLastModified() != null) {
                        timestamp = appointmentObj.getLastModified();
                    }
                } catch (final SQLException e) {
                    throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(e, new Object[0]);
                }
            }
        }
        return new AJAXRequestResult(new JSONArray(0), timestamp, "json");
    }

    private static final Set<String> REQUIRED_PARAMETERS = com.openexchange.tools.arrays.Collections.unmodifiableSet(AJAXServlet.PARAMETER_TIMESTAMP);

    @Override
    protected Set<String> getRequiredParameters() {
        return REQUIRED_PARAMETERS;
    }

    @Override
    protected AJAXRequestResult perform(CalendarSession session, AppointmentAJAXRequest request) throws OXException, JSONException {
        List<EventID> requestedIDs = parseRequestedIDs(session, request);
        Map<EventID, CalendarResult> results = session.getCalendarService().deleteEvents(session, requestedIDs);
        Date timestamp = new Date(0L);
        for (CalendarResult result : results.values()) {
            timestamp = getLatestModified(timestamp, result.getTimestamp());
        }
        return new AJAXRequestResult(new JSONArray(0), timestamp, "json");
    }

}
