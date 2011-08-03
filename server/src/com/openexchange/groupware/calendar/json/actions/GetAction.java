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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.groupware.calendar.json.actions;

import static com.openexchange.tools.TimeZoneUtils.getTimeZone;
import java.sql.SQLException;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.writer.AppointmentWriter;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.calendar.json.AppointmentAJAXRequest;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link GetAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GetAction extends AbstractAppointmentAction {

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(GetAction.class));

    /**
     * Initializes a new {@link GetAction}.
     * @param services
     */
    public GetAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final AppointmentAJAXRequest req) throws OXException, JSONException {
        Date timestamp = null;
        final int id = req.checkInt( AJAXServlet.PARAMETER_ID);
        final int inFolder = req.checkInt( AJAXServlet.PARAMETER_FOLDERID);
        final int recurrencePosition = req.optInt(CalendarFields.RECURRENCE_POSITION);
        final TimeZone timeZone;
        {
            final String timeZoneId = req.getParameter(AJAXServlet.PARAMETER_TIMEZONE);
            timeZone = null == timeZoneId ? req.getTimeZone() : getTimeZone(timeZoneId);
        }

        final ServerSession session = req.getSession();
        final AppointmentSQLInterface appointmentsql = getService().createAppointmentSql(session);
        final CalendarCollectionService recColl = ServerServiceRegistry.getInstance().getService(CalendarCollectionService.class);
        try {
            final Appointment appointmentobject = appointmentsql.getObjectById(id, inFolder);
            if(appointmentobject.getPrivateFlag() && session.getUserId() != appointmentobject.getCreatedBy()) {
                anonymize(appointmentobject);
            }

            final AppointmentWriter appointmentwriter = new AppointmentWriter(timeZone);
            appointmentwriter.setSession(session);

            final JSONObject jsonResponseObj = new JSONObject();

            if (appointmentobject.getRecurrenceType() != CalendarObject.NONE && recurrencePosition > 0) {
                // Commented this because this is done in CalendarOperation.loadAppointment():207 that calls extractRecurringInformation()
                // appointmentobject.calculateRecurrence();
                final RecurringResultsInterface recuResults = recColl.calculateRecurring(
                    appointmentobject,
                    0,
                    0,
                    recurrencePosition,
                    recColl.MAX_OCCURRENCESE,
                    true);
                if (recuResults.size() == 0) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(new StringBuilder(32).append("No occurrence at position ").append(recurrencePosition));
                    }
                    throw OXCalendarExceptionCodes.UNKNOWN_RECURRENCE_POSITION.create(Integer.valueOf(recurrencePosition));
                }
                final RecurringResultInterface result = recuResults.getRecurringResult(0);
                appointmentobject.setStartDate(new Date(result.getStart()));
                appointmentobject.setEndDate(new Date(result.getEnd()));
                appointmentobject.setRecurrencePosition(result.getPosition());

                appointmentwriter.writeAppointment(appointmentobject, jsonResponseObj);
            } else {
                appointmentwriter.writeAppointment(appointmentobject, jsonResponseObj);
            }

            timestamp = appointmentobject.getLastModified();

            return new AJAXRequestResult(jsonResponseObj, timestamp, "json");
        } catch (final SQLException e) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(e, new Object[0]);
        }
    }

}
