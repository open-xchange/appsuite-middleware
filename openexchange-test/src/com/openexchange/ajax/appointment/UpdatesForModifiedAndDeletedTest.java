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

package com.openexchange.ajax.appointment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.openexchange.ajax.appointment.action.AppointmentUpdatesResponse;
import com.openexchange.ajax.framework.AbstractUpdatesRequest.Ignore;
import com.openexchange.groupware.container.Appointment;


/**
 * {@link UpdatesForModifiedAndDeletedTest}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class UpdatesForModifiedAndDeletedTest extends AbstractAppointmentTest {

    /**
     * Initializes a new {@link UpdatesForModifiedAndDeletedTest}.
     * @param name
     */
    public UpdatesForModifiedAndDeletedTest(String name) {
        super(name);
    }
    
    /**
     * Test http://oxpedia.org/wiki/index.php?title=HTTP_API#Get_updated_appointments
     * @throws Exception
     */
    public void testUpdatesForModifiedAndDeleted() throws Exception{
        // insert some
        final int numberOfAppointments = 8;
        List<Appointment> newAppointments = createAndPersistSeveral("testAppointment", numberOfAppointments);

        // update 2
        List<Appointment> updatedAppointments = new ArrayList<Appointment>(2);
        List<Integer> expectUpdatedAppointmentIds = new ArrayList<Integer>(2);
        updatedAppointments.add(newAppointments.get(0));
        expectUpdatedAppointmentIds.add(newAppointments.get(0).getObjectID());
        updatedAppointments.add(newAppointments.get(1));
        expectUpdatedAppointmentIds.add(newAppointments.get(1).getObjectID());
        updateAppointments(updatedAppointments);

        // delete 2
        List<Appointment> deletedAppointments = new ArrayList<Appointment>(2);
        List<Integer> expectDeletedAppointmentIds = new ArrayList<Integer>(2);
        deletedAppointments.add(newAppointments.get(2));
        expectDeletedAppointmentIds.add(newAppointments.get(2).getObjectID());
        deletedAppointments.add(newAppointments.get(3));
        expectDeletedAppointmentIds.add(newAppointments.get(3).getObjectID());
        deleteAppointments(deletedAppointments);

        // check modified with timestamp from last 
        Date lastModified = newAppointments.get(numberOfAppointments-1).getLastModified();
        int[] cols = new int[]{ Appointment.OBJECT_ID, Appointment.TITLE};
        AppointmentUpdatesResponse modifiedAppointmentsResponse = listModifiedAppointments(appointmentFolderId, cols, lastModified, Ignore.NONE, false);
        assertTrue(modifiedAppointmentsResponse.getNewOrModifiedIds().containsAll(expectUpdatedAppointmentIds));
        assertTrue(modifiedAppointmentsResponse.getDeletedIds().containsAll(expectDeletedAppointmentIds));

        // cleanup: delete all remaining
        newAppointments.removeAll(deletedAppointments);
        deleteAppointments(newAppointments);

    }
}
