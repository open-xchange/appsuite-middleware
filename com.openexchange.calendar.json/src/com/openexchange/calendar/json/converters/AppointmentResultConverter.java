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

package com.openexchange.calendar.json.converters;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.calendar.json.AppointmentAJAXRequest;
import com.openexchange.calendar.json.compat.Appointment;
import com.openexchange.calendar.json.compat.AppointmentWriter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.results.CollectionDelta;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.collections.PropertizedList;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AppointmentResultConverter}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class AppointmentResultConverter extends AbstractCalendarJSONResultConverter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AppointmentResultConverter.class);

    private static final String INPUT_FORMAT = "appointment";

    private final ServiceLookup services;

    /**
     * Initializes a new {@link AppointmentListResultConverter}.
     */
    public AppointmentResultConverter(final ServiceLookup services) {
        super();
        this.services = services;
    }

    protected void convertCalendar(final String action, final Collection<Appointment> appointmentList, final AppointmentAJAXRequest req, final AJAXRequestResult result, final TimeZone userTimeZone) throws OXException {
        if (action.equalsIgnoreCase(AJAXServlet.ACTION_FREEBUSY)) {
            convert4FreeBusy(appointmentList, req, result, userTimeZone);
        } else {
            convert(appointmentList, req, result, userTimeZone);
        }
    }

    protected void convert(final Collection<Appointment> appointmentList, final AppointmentAJAXRequest req, final AJAXRequestResult result, final TimeZone userTimeZone) throws OXException {
        final Date startUTC = req.optDate(AJAXServlet.PARAMETER_START);
        final Date endUTC = req.optDate(AJAXServlet.PARAMETER_END);
        final int[] columns = req.checkIntArray(AJAXServlet.PARAMETER_COLUMNS);
        final boolean bRecurrenceMaster = Boolean.parseBoolean(req.getParameter(AJAXServlet.PARAMETER_RECURRENCE_MASTER));

        final TimeZone timeZone;
        {
            final String timeZoneId = req.getParameter(AJAXServlet.PARAMETER_TIMEZONE);
            timeZone = null == timeZoneId ? userTimeZone : getTimeZone(timeZoneId);
        }
        final JSONArray jsonResponseArray = new JSONArray();
        final AppointmentWriter writer = new AppointmentWriter(timeZone).setSession(req.getSession());

        for (final Appointment appointment : appointmentList) {
            try {
                if (bRecurrenceMaster) {
                    writer.writeArray(appointment, columns, jsonResponseArray);
                } else {
                    writer.writeArray(appointment, columns, startUTC, endUTC, jsonResponseArray);
                }
            } catch (JSONException e) {
                throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
            }
        }

        if (appointmentList instanceof PropertizedList) {
            final PropertizedList<Appointment> propList = (PropertizedList<Appointment>) appointmentList;
            final Integer i = (Integer) propList.getProperty("more");
            if (null != i && i.intValue() > 0) {
                result.setResponseProperty("more", i);
            }
        }

        result.setResultObject(jsonResponseArray, OUTPUT_FORMAT);
    }

    protected void convert4FreeBusy(final Collection<Appointment> appointmentList, final AppointmentAJAXRequest req, final AJAXRequestResult result, final TimeZone userTimeZone) throws OXException {
        final TimeZone timeZone;
        {
            final String timeZoneId = req.getParameter(AJAXServlet.PARAMETER_TIMEZONE);
            timeZone = null == timeZoneId ? userTimeZone : getTimeZone(timeZoneId);
        }

        final JSONArray jsonResponseArray = new JSONArray(appointmentList.size());
        final AppointmentWriter appointmentWriter = new AppointmentWriter(timeZone).setSession(req.getSession());
        for (final Appointment appointment : appointmentList) {
            final JSONObject jsonAppointmentObj = new JSONObject();
            try {
                appointmentWriter.writeAppointment(appointment, jsonAppointmentObj);
                jsonResponseArray.put(jsonAppointmentObj);
            } catch (JSONException e) {
                throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
            }
        }

        result.setResultObject(jsonResponseArray, OUTPUT_FORMAT);
    }

    protected void convert4Updates(final CollectionDelta<Appointment> appointments, final AppointmentAJAXRequest req, final AJAXRequestResult result, final TimeZone userTimeZone) throws OXException {
        final Date startUTC = req.optDate(AJAXServlet.PARAMETER_START);
        final Date endUTC = req.optDate(AJAXServlet.PARAMETER_END);
        final int[] columns = req.checkIntArray(AJAXServlet.PARAMETER_COLUMNS);

        final List<Appointment> deletedAppointments = appointments.getDeleted();
        final List<Appointment> appointmentList = appointments.getNewAndModified();

        final TimeZone timeZone;
        {
            final String timeZoneId = req.getParameter(AJAXServlet.PARAMETER_TIMEZONE);
            timeZone = null == timeZoneId ? userTimeZone : getTimeZone(timeZoneId);
        }
        final JSONArray jsonResponseArray = new JSONArray(appointmentList.size());
        final AppointmentWriter writer = new AppointmentWriter(timeZone).setSession(req.getSession());

        for (final Appointment appointment : appointmentList) {
            try {
                writer.writeArray(appointment, columns, startUTC, endUTC, jsonResponseArray);
            } catch (JSONException e) {
                throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
            }
        }

        if (!deletedAppointments.isEmpty()) {
            for (final Appointment appointment : deletedAppointments) {
                jsonResponseArray.put(appointment.getObjectID());
            }
        }

        result.setResultObject(jsonResponseArray, OUTPUT_FORMAT);
    }

    @Override
    public String getInputFormat() {
        return INPUT_FORMAT;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void convertCalendar(final AppointmentAJAXRequest request, final AJAXRequestResult result, final ServerSession session, final Converter converter, final TimeZone userTimeZone) throws OXException {
        final Object resultObject = result.getResultObject();
        final String action = request.getParameter(AJAXServlet.PARAMETER_ACTION);
        if (resultObject instanceof Appointment) {
            convertCalendar((Appointment) resultObject, request, result, session, userTimeZone);
        } else if (resultObject instanceof CollectionDelta) {
            convert4Updates((CollectionDelta<Appointment>) resultObject, request, result, userTimeZone);
        } else {
            final Collection<Appointment> appointments = (Collection<Appointment>) resultObject;
            convertCalendar(action, appointments, request, result, userTimeZone);
        }
    }

    private void convertCalendar(final Appointment appointmentobject, final AppointmentAJAXRequest request, final AJAXRequestResult result, final ServerSession session, final TimeZone userTimeZone) throws OXException {
        final JSONObject jsonResponseObj = new JSONObject();

        final TimeZone timeZone;
        {
            final String timeZoneId = request.getParameter(AJAXServlet.PARAMETER_TIMEZONE);
            timeZone = null == timeZoneId ? userTimeZone : getTimeZone(timeZoneId);
        }
        final AppointmentWriter appointmentwriter = new AppointmentWriter(timeZone).setSession(request.getSession());
        appointmentwriter.setSession(session);
        try {
            appointmentwriter.writeAppointment(appointmentobject, jsonResponseObj);
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
        }

        result.setResultObject(jsonResponseObj, OUTPUT_FORMAT);
    }

}
