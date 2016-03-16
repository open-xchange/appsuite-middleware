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

package com.openexchange.halo.appointments;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.OrderFields;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.Order;
import com.openexchange.halo.AbstractContactHalo;
import com.openexchange.halo.HaloContactDataSource;
import com.openexchange.halo.HaloContactQuery;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

public class AppointmentContactHalo extends AbstractContactHalo implements HaloContactDataSource {

    private final ServiceLookup services;

    public AppointmentContactHalo(ServiceLookup services) {
        this.services = services;
    }

    @Override
    public String getId() {
        return "com.openexchange.halo.appointments";
    }

    @Override
    public AJAXRequestResult investigate(HaloContactQuery query, AJAXRequestData req, ServerSession session) throws OXException {
        AppointmentSQLInterface appointmentService = getAppointmentService(session);

        int[] columns = req.checkIntArray(AJAXServlet.PARAMETER_COLUMNS);
        String parameterStart = req.checkParameter(AJAXServlet.PARAMETER_START);
        Date start = new Date(Long.parseLong(parameterStart));
        String parameterEnd = req.checkParameter(AJAXServlet.PARAMETER_END);
        Date end = new Date(Long.parseLong(parameterEnd));
        String parameterSort = req.getParameter(AJAXServlet.PARAMETER_SORT);
        int orderBy = parameterSort == null ? 0 : Integer.parseInt(parameterSort);
        String parameterOrder = req.getParameter(AJAXServlet.PARAMETER_ORDER);
        Order order = OrderFields.parse(parameterOrder);

        List<Appointment> appointments = null;
        if (query.getUser() != null && query.getUser().getId() == session.getUser().getId()) {
            appointments = new LinkedList<Appointment>();
        } else if (query.getUser() != null) {
            appointments = appointmentService.getAppointmentsWithUserBetween(query.getUser(), columns, start, end, orderBy, order);
        } else {
            Contact searchContact = query.getContact();
            List<String> addresses = getEMailAddresses(searchContact);
            appointments = new LinkedList<Appointment>();
            for (String address : addresses) {
                appointments.addAll(appointmentService.getAppointmentsWithExternalParticipantBetween(
                    address,
                    columns,
                    start,
                    end,
                    orderBy,
                    order));
            }
        }

        Collections.sort(appointments, new Comparator<Appointment>() {

            @Override
            public int compare(Appointment app, Appointment other) {
                return app.getStartDate().compareTo(other.getStartDate());
            }
        });

        return new AJAXRequestResult(appointments, "appointment");
    }

    private AppointmentSQLInterface getAppointmentService(ServerSession session) {
        AppointmentSqlFactoryService factoryService = services.getService(AppointmentSqlFactoryService.class);
        return factoryService.createAppointmentSql(session);
    }

    @Override
    public boolean isAvailable(ServerSession session) {
        return session.getUserPermissionBits().hasCalendar();
    }

}
