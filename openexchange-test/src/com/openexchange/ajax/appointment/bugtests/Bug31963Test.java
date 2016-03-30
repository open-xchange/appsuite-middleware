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

package com.openexchange.ajax.appointment.bugtests;

import java.util.Calendar;
import java.util.List;
import com.openexchange.ajax.appointment.action.ConflictObject;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.java.util.TimeZones;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link Bug31963Test}
 *
 * private all day appointment conflicts with appointments on the previous day?
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug31963Test extends AbstractAJAXSession {

    private CalendarTestManager ctm;

    /**
     * Initializes a new {@link Bug31963Test}.
     *
     * @param name The test name
     */
    public Bug31963Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ctm = new CalendarTestManager(getClient());
    }

    @Override
    protected void tearDown() throws Exception {
        ctm.cleanUp();
        super.tearDown();
    }

    public void testNotConflictingAppointment() throws Exception {
        int folderID = super.getClient().getValues().getPrivateAppointmentFolder();
        /*
         * create whole day appointment (client sends UTC dates)
         */
        int nextYear = Calendar.getInstance().get(Calendar.YEAR) + 1;
        Appointment appointment = new Appointment();
        appointment.setParentFolderID(folderID);
        appointment.setTitle(getClass().getName());
        appointment.setStartDate(TimeTools.D("28.04." + nextYear + " 00:00", TimeZones.UTC));
        appointment.setEndDate(TimeTools.D("29.04." + nextYear + " 00:00", TimeZones.UTC));
        appointment.setFullTime(true);
        appointment.setIgnoreConflicts(true);
        appointment = ctm.insert(appointment);
        /*
         * create appointment an hour before in user's timezone and check for conflicts
         */
        Appointment notConflictingAppointment = new Appointment();
        notConflictingAppointment.setParentFolderID(folderID);
        notConflictingAppointment.setTitle(getClass().getName());
        notConflictingAppointment.setStartDate(TimeTools.D("27.04." + nextYear + " 23:00", getClient().getValues().getTimeZone()));
        notConflictingAppointment.setEndDate(TimeTools.D("28.04." + nextYear + " 00:00", getClient().getValues().getTimeZone()));
        notConflictingAppointment.setIgnoreConflicts(false);
        ctm.insert(notConflictingAppointment);
        List<ConflictObject> conflicts = ctm.getLastResponse().getConflicts();
        assertTrue("conflicts detected", null == conflicts || 0 == conflicts.size());
        /*
         * create appointment an hour after in user's timezone and check for conflicts
         */
        notConflictingAppointment = new Appointment();
        notConflictingAppointment.setParentFolderID(folderID);
        notConflictingAppointment.setTitle(getClass().getName());
        notConflictingAppointment.setStartDate(TimeTools.D("29.04." + nextYear + " 00:00", getClient().getValues().getTimeZone()));
        notConflictingAppointment.setEndDate(TimeTools.D("29.04." + nextYear + " 01:00", getClient().getValues().getTimeZone()));
        notConflictingAppointment.setIgnoreConflicts(false);
        ctm.insert(notConflictingAppointment);
        conflicts = ctm.getLastResponse().getConflicts();
        assertTrue("conflicts detected", null == conflicts || 0 == conflicts.size());
        /*
         * create a (really) conflicting appointment in user's timezone and check for conflicts
         */
        Appointment conflictingAppointment = new Appointment();
        conflictingAppointment.setParentFolderID(folderID);
        conflictingAppointment.setTitle(getClass().getName());
        conflictingAppointment.setStartDate(TimeTools.D("28.04." + nextYear + " 00:00", getClient().getValues().getTimeZone()));
        conflictingAppointment.setEndDate(TimeTools.D("28.04." + nextYear + " 01:00", getClient().getValues().getTimeZone()));
        conflictingAppointment.setIgnoreConflicts(false);
        ctm.insert(conflictingAppointment);
        conflicts = ctm.getLastResponse().getConflicts();
        assertTrue("no conflicts detected", null != conflicts && 0 < conflicts.size());
        /*
         * create another (really) conflicting appointment in user's timezone and check for conflicts
         */
        conflictingAppointment = new Appointment();
        conflictingAppointment.setParentFolderID(folderID);
        conflictingAppointment.setTitle(getClass().getName());
        conflictingAppointment.setStartDate(TimeTools.D("28.04." + nextYear + " 23:00", getClient().getValues().getTimeZone()));
        conflictingAppointment.setEndDate(TimeTools.D("29.04." + nextYear + " 00:00", getClient().getValues().getTimeZone()));
        conflictingAppointment.setIgnoreConflicts(false);
        ctm.insert(conflictingAppointment);
        conflicts = ctm.getLastResponse().getConflicts();
        assertTrue("no conflicts detected", null != conflicts && 0 < conflicts.size());
    }

}
