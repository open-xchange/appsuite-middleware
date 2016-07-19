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
 *    trademarks of the OX Software GmbH. group of companies.
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

import static com.openexchange.tools.TimeZoneUtils.getTimeZone;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.parser.AppointmentParser;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.writer.AppointmentWriter;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.calendar.json.AppointmentAJAXRequest;
import com.openexchange.calendar.json.AppointmentActionFactory;
import com.openexchange.calendar.json.actions.chronos.ChronosAction;
import com.openexchange.calendar.json.actions.chronos.EventConverter;
import com.openexchange.chronos.CalendarParameters;
import com.openexchange.chronos.CalendarService;
import com.openexchange.chronos.UserizedEvent;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link UpdateAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
@Action(method = RequestMethod.PUT, name = "update", description = "Update an appointment.", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "id", description = "Object ID of the updated appointment."),
    @Parameter(name = "folder", description = "Object ID of the appointment's folder."),
    @Parameter(name = "timestamp", description = "Timestamp of the updated appointment. If the appointment was modified after the specified timestamp, then the update must fail.")
}, requestBody = "Appointment object as described in Common object data, Detailed task and appointment data and Detailed appointment data. The field recurrence_id is always present if it is present in the original appointment. The field recurrence_position is present if it is present in the original appointment and only this single appointment should be modified. The field id is not present because it is already included as a parameter. Other fields are present only if modified.",
responseDescription = "Nothing, except the standard response object with empty data, the timestamp of the updated appointment, and maybe errors.")
@OAuthAction(AppointmentActionFactory.OAUTH_WRITE_SCOPE)
public final class UpdateAction extends ChronosAction {

    private static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(UpdateAction.class);

    /**
     * Initializes a new {@link UpdateAction}.
     * @param services
     */
    public UpdateAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final AppointmentAJAXRequest req) throws OXException, JSONException {
        final int objectId = req.checkInt(AJAXServlet.PARAMETER_ID);
        final int inFolder = req.checkInt(AJAXServlet.PARAMETER_INFOLDER);
        Date timestamp = req.checkDate(AJAXServlet.PARAMETER_TIMESTAMP);
        final JSONObject jData = req.getData();
        final TimeZone timeZone;
        {
            final String timeZoneId = req.getParameter(AJAXServlet.PARAMETER_TIMEZONE);
            timeZone = null == timeZoneId ? req.getTimeZone() : getTimeZone(timeZoneId);
        }
        final ServerSession session = req.getSession();

        final CalendarDataObject appointmentObj = new CalendarDataObject();
        appointmentObj.setContext(session.getContext());

        final AppointmentParser appointmentParser = new AppointmentParser(timeZone);
        appointmentParser.parse(appointmentObj, jData);

        convertExternalToInternalUsersIfPossible(appointmentObj, session.getContext(), LOG);

        appointmentObj.setObjectID(objectId);

        final AppointmentSqlFactoryService factoryService = getService();
        if (null == factoryService) {
            throw ServiceExceptionCode.absentService(AppointmentSqlFactoryService.class);
        }
        final AppointmentSQLInterface appointmentsql = factoryService.createAppointmentSql(session);
        final Appointment[] conflicts = appointmentsql.updateAppointmentObject(appointmentObj, inFolder, timestamp);

        final JSONObject jsonResponseObj = new JSONObject();

        if (conflicts == null) {
            jsonResponseObj.put(DataFields.ID, appointmentObj.getObjectID());
            timestamp = appointmentObj.getLastModified();
        } else {
            final JSONArray jsonConflictArray = new JSONArray(conflicts.length);
            final AppointmentWriter appointmentWriter = new AppointmentWriter(timeZone).setSession(req.getSession());
            for (final Appointment conflict : conflicts) {
                final JSONObject jsonAppointmentObj = new JSONObject();
                appointmentWriter.writeAppointment(conflict, jsonAppointmentObj);
                jsonConflictArray.put(jsonAppointmentObj);
            }

            jsonResponseObj.put("conflicts", jsonConflictArray);
        }

        return new AJAXRequestResult(jsonResponseObj, timestamp, "json");
    }

    private static final String[] REQUIRED_PARAMETERS = {
        CalendarParameters.PARAMETER_TIMESTAMP
    };

    @Override
    protected AJAXRequestResult perform(CalendarService calendarService, AppointmentAJAXRequest request) throws OXException, JSONException {
        JSONObject jsonObject = request.getData();
        CalendarDataObject appointment = new CalendarDataObject();
        appointment.setContext(request.getSession().getContext());
        new AppointmentParser(request.getTimeZone()).parse(appointment, jsonObject);
        convertExternalToInternalUsersIfPossible(appointment, request.getSession().getContext(), LOG);
        int folderID = request.checkInt(AJAXServlet.PARAMETER_INFOLDER);
        CalendarParameters parameters = parseParameters(request, REQUIRED_PARAMETERS);
        if (appointment.containsNotification()) {
            parameters.set(CalendarParameters.PARAMETER_NOTIFICATION, Boolean.valueOf(appointment.getNotification()));
        }
        parameters.set(CalendarParameters.PARAMETER_IGNORE_CONFLICTS, Boolean.valueOf(appointment.getIgnoreConflicts()));
        UserizedEvent event = EventConverter.getEvent(appointment, request.getSession());
        UserizedEvent updatedEvent = calendarService.updateEvent(request.getSession(), folderID, event, parameters);
        return new AJAXRequestResult(new JSONObject().put(DataFields.ID, updatedEvent.getEvent().getId()), updatedEvent.getEvent().getLastModified(), "json");
    }

}
