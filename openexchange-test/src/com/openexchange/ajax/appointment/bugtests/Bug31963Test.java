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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajax.appointment.bugtests;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.ConflictObject;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.java.util.TimeZones;

/**
 * {@link Bug31963Test}
 *
 * private all day appointment conflicts with appointments on the previous day?
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug31963Test extends AppointmentTest {

    /**
     * Initializes a new {@link Bug31963Test}.
     *
     * @param name The test name
     */
    public Bug31963Test(String name) {
        super(name);
    }

    public void testNotConflictingAppointment() throws Exception {
        int folderID = super.getClient().getValues().getPrivateAppointmentFolder();
        /*
         * create whole day appointment
         */
        Date start = TimeTools.D("next thursday at midnight", TimeZones.UTC);
        Calendar calendar = Calendar.getInstance(TimeZones.UTC);
        calendar.setTime(start);
        calendar.add(Calendar.DATE, 1);
        Date end = calendar.getTime();
        Appointment appointment = new Appointment();
        appointment.setParentFolderID(folderID);
        appointment.setTitle(getClass().getName());
        appointment.setStartDate(start);
        appointment.setEndDate(end);
        appointment.setFullTime(true);
        appointment.setIgnoreConflicts(true);
        create(appointment);
        /*
         * create appointment an hour before and check for conflicts
         */
        calendar.setTime(appointment.getStartDate());
        calendar.add(Calendar.HOUR_OF_DAY, -1);
        start = calendar.getTime();
        end = appointment.getStartDate();
        Appointment notConflictingAppointment = new Appointment();
        notConflictingAppointment.setParentFolderID(folderID);
        notConflictingAppointment.setTitle(getClass().getName());
        notConflictingAppointment.setStartDate(start);
        notConflictingAppointment.setEndDate(end);
        notConflictingAppointment.setIgnoreConflicts(false);
        InsertRequest insertRequest = new InsertRequest(notConflictingAppointment, TimeZones.UTC, true);
        AppointmentInsertResponse insertResponse = getClient().execute(insertRequest);
        List<ConflictObject> conflicts = insertResponse.getConflicts();
        assertTrue("conflicts detected", null == conflicts || 0 == conflicts.size());
        /*
         * create appointment an hour after and check for conflicts
         */
        calendar.setTime(appointment.getEndDate());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        end = calendar.getTime();
        start = appointment.getEndDate();
        notConflictingAppointment = new Appointment();
        notConflictingAppointment.setParentFolderID(folderID);
        notConflictingAppointment.setTitle(getClass().getName());
        notConflictingAppointment.setStartDate(start);
        notConflictingAppointment.setEndDate(end);
        notConflictingAppointment.setIgnoreConflicts(false);
        insertRequest = new InsertRequest(notConflictingAppointment, TimeZones.UTC, true);
        insertResponse = getClient().execute(insertRequest);
        conflicts = insertResponse.getConflicts();
        assertTrue("conflicts detected", null == conflicts || 0 == conflicts.size());
    }

}
