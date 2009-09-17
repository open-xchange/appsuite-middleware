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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.calendar.printing;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.ajax.PermissionServlet;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.TemplateException;
import com.openexchange.templating.TemplateService;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link CalendarPrintingServlet}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CalendarPrintingServlet extends PermissionServlet {

    private static TemplateService templates = null;

    public static void setTemplateService(TemplateService service) {
        templates = service;
    }

    private static AppointmentSqlFactoryService appointmentSqlFactory = null;

    public static void setAppointmentSqlFactoryService(AppointmentSqlFactoryService service) {
        appointmentSqlFactory = service;
    }

    private static CalendarCollectionService calendarTools = null;

    public static void setCalendarTools(CalendarCollectionService service) {
        calendarTools = service;
    }
    
    private static final Log LOG = LogFactory.getLog(CalendarPrintingServlet.class);
    private static final String APPOINTMENTS = "appointments";

    @Override
    protected boolean hasModulePermission(ServerSession session) {
        return session.getUserConfiguration().hasCalendar();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        String start = req.getParameter(PARAMETER_START);
        String end = req.getParameter(PARAMETER_END);
        String folder = req.getParameter(PARAMETER_FOLDERID);
        String templateName = req.getParameter(PARAMETER_TEMPLATE);
        ServerSession session = getSessionObject(req);
        try {
            OXTemplate template = templates.loadTemplate(templateName);
            
            AppointmentSQLInterface appointmentSql = appointmentSqlFactory.createAppointmentSql(session);
            SearchIterator<Appointment> iterator = appointmentSql.getAppointmentsBetweenInFolder(Integer.valueOf(folder), new int[]{Appointment.TITLE}, new Date(Long.valueOf(start)), new Date(Long.valueOf(end)), -1, null);
            List<Appointment> appointments = SearchIteratorAdapter.toList(iterator);
            
            Map<String, Object> variables = new HashMap<String, Object>();
            variables.put(APPOINTMENTS, appointments);
            
            template.process(variables, resp.getWriter());
            
            
        } catch (Throwable t) {
            writeException(resp, t);
        } 
    }
        
    private void writeException(HttpServletResponse resp, Throwable t) {
        LOG.error(t.getMessage(), t);
        //TODO Write HTML page as response
    }

}
