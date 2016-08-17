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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.calendar.json.AppointmentAJAXRequest;
import com.openexchange.calendar.json.AppointmentActionFactory;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.iterator.SearchIterator;


/**
 * {@link FreeBusyAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
@Action(method = RequestMethod.PUT, name = "freebusy", description = "Free & Busy.", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "id", description = "Internal user id. Must be obtained from the contact module."),
    @Parameter(name = "type", description = "Constant for user or resource (1 for users, 3 for resources)."),
    @Parameter(name = "start", description = "Lower inclusive limit of the queried range as a Date. Only appointments which end on or after this date are returned."),
    @Parameter(name = "end", description = "Upper exclusive limit of the queried range as a Date. Only appointments which start before this date are returned.")
}, responseDescription = "An array of objects identifying the appointments which lie between start and end as described.")
@OAuthAction(AppointmentActionFactory.OAUTH_READ_SCOPE)
public final class FreeBusyAction extends AppointmentAction {

    /**
     * Initializes a new {@link FreeBusyAction}.
     * @param services
     */
    public FreeBusyAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final AppointmentAJAXRequest req) throws OXException, JSONException {
        final int userId = req.checkInt(AJAXServlet.PARAMETER_ID);
        final int type = req.checkInt("type");
        final TimeZone timeZone;
        {
            final String timeZoneId = req.getParameter(AJAXServlet.PARAMETER_TIMEZONE);
            timeZone = null == timeZoneId ? req.getTimeZone() : getTimeZone(timeZoneId);
        }

        final Date start = req.checkTime(AJAXServlet.PARAMETER_START, timeZone);
        final Date end = req.checkTime(AJAXServlet.PARAMETER_END, timeZone);


        Date timestamp = new Date(0);

        SearchIterator<Appointment> it = null;
        try {
            final List<Appointment> appointmentList = new ArrayList<Appointment>();
            final AppointmentSqlFactoryService factoryService = getService();
            if (null == factoryService) {
                throw ServiceExceptionCode.absentService(AppointmentSqlFactoryService.class);
            }
            final AppointmentSQLInterface appointmentsql = factoryService.createAppointmentSql(req.getSession());
            it = appointmentsql.getFreeBusyInformation(userId, type, start, end);
            while (it.hasNext()) {
                final Appointment appointmentObj = it.next();
                appointmentList.add(appointmentObj);

                if (null != appointmentObj.getLastModified() && timestamp.before(appointmentObj.getLastModified())) {
                    timestamp = appointmentObj.getLastModified();
                }
            }
            return new AJAXRequestResult(appointmentList, timestamp, "appointment");
        } finally {
            if (it != null) {
                it.close();
            }
        }
    }

}
