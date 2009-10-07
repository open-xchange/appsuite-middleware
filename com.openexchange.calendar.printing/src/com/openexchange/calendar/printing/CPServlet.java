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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.ajax.PermissionServlet;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.calendar.printing.blocks.CPFactory;
import com.openexchange.calendar.printing.blocks.CPFormattingInformation;
import com.openexchange.calendar.printing.blocks.CPPartition;
import com.openexchange.calendar.printing.blocks.WorkWeekPartitioningStrategy;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.java.Strings;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.TemplateService;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link CPServlet}
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a> - logic
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a> - skeleton
 */
public class CPServlet extends PermissionServlet {

    /**
     * 
     */
    private static final long serialVersionUID = -5186422014968264569L;

    private static TemplateService templates = null;

    private static final Log LOG = LogFactory.getLog(CPServlet.class);

    private static final String APPOINTMENTS = "appointments";

    private static final String VIEW_START = "start";

    private static final String VIEW_END = "end";

    private static final String DEBUG = "debuggingItems";

    private static final String FORMATTINGINFO = "formattinginfo";

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

    @Override
    protected boolean hasModulePermission(ServerSession session) {
        return session.getUserConfiguration().hasCalendar();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<String> debuggingItems = new LinkedList<String>();
        CPTool tool = new CPTool();
        
        resp.setContentType("text/html");

        ServerSession session = getSessionObject(req);
        try {
            CPParameters params = new CPParameters(req);
            if (params.isMissingFields()) {
                throw new ServletException("Missing one or more parameters: " + Strings.join(params.getMissingFields(), ","));
            }
            if(CPType.getByTemplateName(params.getTemplate()) == null){
                throw new ServletException("Cannot find template " + params.getTemplate());
            }

            if(tool.isBlockTemplate(params))
                tool.calculateNewStartAndEnd(params);
            OXTemplate template = templates.loadTemplate(params.getTemplate());

            AppointmentSQLInterface appointmentSql = appointmentSqlFactory.createAppointmentSql(session);
            SearchIterator<Appointment> iterator = appointmentSql.getAppointmentsBetweenInFolder(params.getFolder(), new int[] {
                Appointment.OBJECT_ID, Appointment.FOLDER_ID, Appointment.TITLE }, params.getStart(), params.getEnd(), -1, null);

            List<CPAppointment> expandedAppointments = tool.expandAppointements(
                SearchIteratorAdapter.toList(iterator),
                params.getStart(),
                params.getEnd(),
                appointmentSql,
                calendarTools);

            tool.sort(expandedAppointments);
            
            CPFactory factory = new CPFactory();
            factory.addStrategy(new WorkWeekPartitioningStrategy());
            factory.setTypeToProduce(CPType.getByTemplateName(params.getTemplate()));
            CPPartition partitions = factory.partition(expandedAppointments);

            Map<String, Object> variables = new HashMap<String, Object>();
            variables.put(APPOINTMENTS, partitions.getAppointments());
            variables.put(FORMATTINGINFO, partitions.getFormattingInformation());
            variables.put(VIEW_START, params.getStart());
            variables.put(VIEW_END, params.getEnd());
            variables.put(DEBUG, debuggingItems);

            for(CPAppointment app : partitions.getAppointments()){
                debuggingItems.add(app.getTitle());
            }
            for(CPFormattingInformation info: partitions.getFormattingInformation()){
                debuggingItems.add(info.toString());
            }
            
            template.process(variables, resp.getWriter());
        } catch (Throwable t) {
            writeException(resp, t);
        }
    }



    /**
     * Write an exception message as HTML to the response
     */
    private void writeException(HttpServletResponse resp, Throwable t) {
        LOG.error(t.getMessage(), t);
        // TODO Write HTML page as response
    }
}
