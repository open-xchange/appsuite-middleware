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

import static com.openexchange.test.common.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.java.util.UUIDs;
import com.openexchange.test.common.test.TestClassConfig;

/**
 * {@link Bug21620Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug21620Test extends AbstractAJAXSession {

    private Appointment appointment;
    private AJAXClient clientA;
    private AJAXClient clientB;
    private AJAXClient clientC;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        /*
         * prepare clients
         */
        clientA = testUser.getAjaxClient();
        clientB = testUser2.getAjaxClient();
        clientC = testContext.acquireUser().getAjaxClient();
        /*
         * as user A, share a calendar folder to user B
         */
        catm.resetDefaultFolderPermissions();
        FolderObject sharedFolder = ftm.insertFolderOnServer(ftm.generateSharedFolder(
            UUIDs.getUnformattedStringFromRandom(), FolderObject.CALENDAR, clientA.getValues().getPrivateAppointmentFolder(), clientA.getValues().getUserId(), clientB.getValues().getUserId()));
        /*
         * prepare appointment with organizer set to user B ("acting user") and principal to user A ("folder owner")
         */
        appointment = new Appointment();
        appointment.setParentFolderID(sharedFolder.getObjectID());
        appointment.setIgnoreConflicts(true);
        appointment.setTitle("Bug 21620");
        appointment.setStartDate(D("next week at 13:00"));
        appointment.setEndDate(D("next week at 13:30"));
        UserParticipant userParticipantB = new UserParticipant(clientB.getValues().getUserId());
        userParticipantB.setConfirm(1);
        appointment.setUsers(new UserParticipant[] { userParticipantB });
        appointment.setParticipants(new Participant[] {
            new UserParticipant(clientA.getValues().getUserId()),
            new UserParticipant(clientC.getValues().getUserId()),
        });
        appointment.setModifiedBy(clientB.getValues().getUserId());
        appointment.setCreatedBy(clientB.getValues().getUserId());
        appointment.setOrganizer(clientB.getValues().getDefaultAddress());
        appointment.setOrganizerId(clientB.getValues().getUserId());
        appointment.setPrincipal(clientA.getValues().getDefaultAddress());
        appointment.setPrincipalId(clientA.getValues().getUserId());
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(2).build();
    }

    @Test
    public void testBug21620() throws Exception {
        /*
         * as user B, insert the appointment in user A's calendar
         */
        AppointmentInsertResponse insertResponse = clientB.execute(new InsertRequest(appointment, clientB.getValues().getTimeZone()));
        insertResponse.fillObject(appointment);
        /*
         * as user B, check principal / organizer in created appointment
         */
        GetResponse getResponse = clientB.execute(new GetRequest(appointment));
        Appointment loadedAppointment = getResponse.getAppointment(clientB.getValues().getTimeZone());
        assertEquals("Wrong organizer ID", clientB.getValues().getUserId(), loadedAppointment.getOrganizerId());
        assertEquals("Wrong principal ID", clientA.getValues().getUserId(), loadedAppointment.getPrincipalId());
        /*
         * as user A, also load & check appointment
         */
        loadedAppointment = catm.get(appointment);
        assertEquals("Wrong organizer ID", clientB.getValues().getUserId(), loadedAppointment.getOrganizerId());
        assertEquals("Wrong principal ID", clientA.getValues().getUserId(), loadedAppointment.getPrincipalId());
    }

}
