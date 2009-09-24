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

package com.openexchange.calendar.printing.blocks;

import java.util.List;
import com.openexchange.calendar.printing.CPAppointment;
import com.openexchange.calendar.printing.CPTool;
import com.openexchange.calendar.printing.CPType;
import com.openexchange.groupware.container.Appointment;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class WorkWeekPartitioningStrategy extends AbstractPartitioningStrategy implements CPPartitioningStrategy {

    public static final int DAYBREAK = 0;

    public static final int WEEKBREAK = 1;

    private CPAppointment lastAppointment;

    public boolean isPackaging(CPType type) {
        return type == CPType.WORKWEEKVIEW;
    }

    public CPPartition partition(List<CPAppointment> appointments) {
        CPTool tools = new CPTool();
        tools.sort(appointments);

        CPPartition blocks = new CPPartition();

        for (int i = 0, length = appointments.size(); i < length; i++) {
            CPAppointment appointment = appointments.get(i);
            if (i > 0)
                lastAppointment = appointments.get(i - 1);

            if (isWorkWeekAppointment(appointment))
                blocks.addAppointment(appointment);

            if (isSignalForNewDay(appointment))
                blocks.addFormattingInformation(new CPFormattingInfomation(i, DAYBREAK));

            if (isSignalForNewWeek(appointment))
                blocks.addFormattingInformation(new CPFormattingInfomation(i, WEEKBREAK));

            if (isWorkWeekAppointment(appointment))
                if (isOnTwoDays(appointment) || isInTwoWeeks(appointment))
                    blocks.addAppointment(appointment); // store again for use in second block
        }
        return blocks;
    }

    private boolean isOnTwoDays(CPAppointment appointment) {
        return isOnDifferentDays(appointment.getStart(), appointment.getEnd());
    }

    private boolean isInTwoWeeks(CPAppointment appointment) {
        return isInDifferentWeeks(appointment.getStart(), appointment.getEnd());
    }

    private boolean isSignalForNewDay(CPAppointment appointment) {
        if (lastAppointment == null)
            return false;

        return isOnDifferentDays(lastAppointment.getStart(), appointment.getStart()) || isOnDifferentDays(
            lastAppointment.getEnd(),
            appointment.getEnd());
    }

    private boolean isSignalForNewWeek(CPAppointment appointment) {
        if (lastAppointment == null)
            return false;

        return isInDifferentWeeks(lastAppointment.getStart(), appointment.getStart()) || isInDifferentWeeks(
            lastAppointment.getEnd(),
            appointment.getEnd());
    }

}
