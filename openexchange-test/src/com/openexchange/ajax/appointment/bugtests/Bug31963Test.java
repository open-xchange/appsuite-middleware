/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.appointment.bugtests;

import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.List;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.ConflictObject;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.java.util.TimeZones;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link Bug31963Test}
 *
 * private all day appointment conflicts with appointments on the previous day?
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug31963Test extends AbstractAJAXSession {

    @Test
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
        appointment = catm.insert(appointment);
        /*
         * create appointment an hour before in user's timezone and check for conflicts
         */
        Appointment notConflictingAppointment = new Appointment();
        notConflictingAppointment.setParentFolderID(folderID);
        notConflictingAppointment.setTitle(getClass().getName());
        notConflictingAppointment.setStartDate(TimeTools.D("27.04." + nextYear + " 23:00", getClient().getValues().getTimeZone()));
        notConflictingAppointment.setEndDate(TimeTools.D("28.04." + nextYear + " 00:00", getClient().getValues().getTimeZone()));
        notConflictingAppointment.setIgnoreConflicts(false);
        catm.insert(notConflictingAppointment);
        List<ConflictObject> conflicts = catm.getLastResponse().getConflicts();
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
        catm.insert(notConflictingAppointment);
        conflicts = catm.getLastResponse().getConflicts();
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
        catm.insert(conflictingAppointment);
        conflicts = catm.getLastResponse().getConflicts();
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
        catm.insert(conflictingAppointment);
        conflicts = catm.getLastResponse().getConflicts();
        assertTrue("no conflicts detected", null != conflicts && 0 < conflicts.size());
    }

}
