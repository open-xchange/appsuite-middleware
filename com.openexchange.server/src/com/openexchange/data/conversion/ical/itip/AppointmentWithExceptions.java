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

package com.openexchange.data.conversion.ical.itip;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import edu.emory.mathcs.backport.java.util.Collections;

/**
 * {@link AppointmentWithExceptions}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AppointmentWithExceptions {

    private CalendarDataObject cdao;
    private Appointment appointment;

    private final List<CalendarDataObject> exceptions;

    /**
     * Initializes a new {@link AppointmentWithExceptions}.
     */
    public AppointmentWithExceptions() {
        super();
        exceptions = new ArrayList<CalendarDataObject>();
    }

    public CalendarDataObject getDataObject() {
        return cdao;
    }

    public void setAppointment(CalendarDataObject appointment) {
        this.cdao = appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public Appointment getAppointment() {
        if (appointment == null) {
            return cdao;
        }
        return appointment;
    }

    public boolean addException(CalendarDataObject exception) {
        return exceptions.add(exception);
    }

    public boolean addException(Appointment exception) {
        if (CalendarDataObject.class.isInstance(exception)) {
            return addException((CalendarDataObject) exception);
        }
        CalendarDataObject copy = new CalendarDataObject();
        for (int i : Appointment.ALL_COLUMNS) {
            if (exception.contains(i)) {
                copy.set(i, exception.get(i));
            }
        }
        return addException(copy);
    }

    @SuppressWarnings("unchecked")
    public Iterable<CalendarDataObject> exceptions() {
        return Collections.unmodifiableList(exceptions);
    }

    public int numberOfExceptions() {
        return exceptions.size();
    }
}
