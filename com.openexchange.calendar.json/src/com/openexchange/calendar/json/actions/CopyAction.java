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

import static com.openexchange.tools.TimeZoneUtils.getTimeZone;
import java.sql.SQLException;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.AppointmentFields;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.writer.AppointmentWriter;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.calendar.json.AppointmentAJAXRequest;
import com.openexchange.calendar.json.AppointmentActionFactory;
import com.openexchange.calendar.json.actions.chronos.ChronosAction;
import com.openexchange.chronos.CalendarParameters;
import com.openexchange.chronos.CalendarService;
import com.openexchange.chronos.CalendarSession;
import com.openexchange.chronos.CreateResult;
import com.openexchange.chronos.UserizedEvent;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link CopyAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
@Action(method = RequestMethod.PUT, name = "copy", description = "", parameters = {},
requestBody = "",
responseDescription = "")
@OAuthAction(AppointmentActionFactory.OAUTH_WRITE_SCOPE)
public final class CopyAction extends ChronosAction {

    /**
     * Initializes a new {@link CopyAction}.
     * @param services
     */
    public CopyAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final AppointmentAJAXRequest req) throws OXException, JSONException {
        final int id = req.checkInt(AJAXServlet.PARAMETER_ID);
        final int inFolder = req.checkInt(AJAXServlet.PARAMETER_FOLDERID);
        final boolean ignoreConflicts = req.checkBoolean(AppointmentFields.IGNORE_CONFLICTS);
        final JSONObject jData = req.getData();
        final int folderId = DataParser.checkInt(jData, FolderChildFields.FOLDER_ID);
        final TimeZone timeZone;
        {
            final String timeZoneId = req.getParameter(AJAXServlet.PARAMETER_TIMEZONE);
            timeZone = null == timeZoneId ? req.getTimeZone() : getTimeZone(timeZoneId);
        }

        final ServerSession session = req.getSession();

        final AppointmentSqlFactoryService factoryService = getService();
        if (null == factoryService) {
            throw ServiceExceptionCode.absentService(AppointmentSqlFactoryService.class);
        }
        final AppointmentSQLInterface appointmentSql = factoryService.createAppointmentSql(session);

        Date timestamp = new Date(0);

        // final JSONObject jsonResponseObject = new JSONObject();
        CalendarDataObject appointmentObj = null;
        try {
            appointmentObj = appointmentSql.getObjectById(id, inFolder);
        } catch (final SQLException exc) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(exc, new Object[0]);
        }

        appointmentObj.removeObjectID();
        appointmentObj.removeUid();
        appointmentObj.setParentFolderID(folderId);
        appointmentObj.setIgnoreConflicts(ignoreConflicts);
        final Appointment[] conflicts = appointmentSql.insertAppointmentObject(appointmentObj);

        final JSONObject jsonResponseObj = new JSONObject();

        if (conflicts != null) {
            final JSONArray jsonConflictArray = new JSONArray(conflicts.length);
            final AppointmentWriter appointmentWriter = new AppointmentWriter(timeZone).setSession(req.getSession());
            for (int a = 0; a < conflicts.length; a++) {
                final JSONObject jsonAppointmentObj = new JSONObject();
                appointmentWriter.writeAppointment(conflicts[a], jsonAppointmentObj);
                jsonConflictArray.put(jsonAppointmentObj);
            }
        } else {
            jsonResponseObj.put(DataFields.ID, appointmentObj.getObjectID());
            timestamp = appointmentObj.getLastModified();
            countObjectUse(session, appointmentObj);
        }

        return new AJAXRequestResult(jsonResponseObj, timestamp, "json");
    }

    @Override
    protected AJAXRequestResult perform(CalendarService calendarService, AppointmentAJAXRequest request) throws OXException, JSONException {
        CalendarSession calendarSession = initSession(request);
        calendarSession.set(CalendarParameters.PARAMETER_IGNORE_CONFLICTS, Boolean.valueOf(request.getParameter(AppointmentFields.IGNORE_CONFLICTS)));
        int objectID = request.checkInt(AJAXServlet.PARAMETER_ID);
        int folderID = request.checkInt(AJAXServlet.PARAMETER_FOLDERID);
        JSONObject jsonObject = request.getData();
        int targetFolderID = DataParser.checkInt(jsonObject, FolderChildFields.FOLDER_ID);
        UserizedEvent originalEvent = calendarService.getEvent(calendarSession, folderID, objectID);
        originalEvent.getEvent().removeId();
        originalEvent.getEvent().removeUid();
        originalEvent.getEvent().removePublicFolderId();
        UserizedEvent event = new UserizedEvent(request.getSession(), originalEvent.getEvent());
        if (originalEvent.containsAlarms()) {
            event.setAlarms(originalEvent.getAlarms());
        }
        event.setFolderId(targetFolderID);
        CreateResult result = calendarService.createEvent(calendarSession, event);
        return new AJAXRequestResult(new JSONObject().put(DataFields.ID, result.getCreatedEvent().getId()), result.getCreatedEvent().getLastModified(), "json");
    }

}
