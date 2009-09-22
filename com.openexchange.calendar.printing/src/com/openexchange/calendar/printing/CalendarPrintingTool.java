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

import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.Appointment;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class CalendarPrintingTool {

    /**
     * Based on the selected template, this method determines new start and end dates to present exactly the block that the template needs.
     */
    public void calculateNewStartAndEnd(CalendarPrintingParameters params) {
        // TODO Auto-generated method stub

    }

    /**
     * Checks whether the template given is one that prints a specific timeframe as a block, which might be different from the given start
     * and end date.
     */
    public boolean isBlockTemplate(CalendarPrintingParameters params) {
        return true;
    }

    /**
     * Sort a list of appointments by start date.
     */
    public void sort(List<Appointment> appointments) {
        Collections.sort(appointments, new StartDateComparator());
    }

    /**
     * Expands all appointments in a list using their recurrence information for a certain given timeframe
     */
    public List<Appointment> expandAppointements(List<Appointment> compressedAppointments, Date start, Date end, AppointmentSQLInterface appointmentSql, CalendarCollectionService calendarTools) throws OXObjectNotFoundException, OXException, SQLException {
        List<Appointment> expandedAppointments = new LinkedList<Appointment>();
        for (Appointment appointment : compressedAppointments) {
            Appointment temp = appointmentSql.getObjectById(appointment.getObjectID(), appointment.getParentFolderID());
            expandedAppointments.addAll(expandRecurrence(temp, start, end, calendarTools));
        }
        return expandedAppointments;
    }

    /**
     * Takes an appointment and interprets its recurrence information to find all occurrences between start and end date.
     */
    public List<Appointment> expandRecurrence(Appointment appointment, Date start, Date end, CalendarCollectionService calendarTools) throws OXException {
        RecurringResultsInterface recurrences = calendarTools.calculateRecurring(appointment, start.getTime(), end.getTime(), 0);
        List<Appointment> all = new LinkedList<Appointment>();
        if (recurrences == null) {
            all.add(appointment);
            return all;
        }

        for (int i = 0, length = recurrences.size(); i < length; i++) {
            Appointment temp = new Appointment();
            temp.setTitle(appointment.getTitle());
            RecurringResultInterface recurringResult = recurrences.getRecurringResult(i);
            temp.setStartDate(new Date(recurringResult.getStart()));
            temp.setEndDate(new Date(recurringResult.getEnd()));
            all.add(temp);
        }
        return all;
    }

}
