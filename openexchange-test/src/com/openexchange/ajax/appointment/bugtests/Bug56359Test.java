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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.appointment.action.ConfirmRequest;
import com.openexchange.ajax.appointment.action.ConfirmResponse;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.test.common.test.TestClassConfig;

/**
 * {@link Bug56359Test}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class Bug56359Test extends AppointmentTest {

    /**
     * Initializes a new {@link Bug56359Test}.
     */
    public Bug56359Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        /*
         * reset default folder permissions prior test execution
         */
        catm.resetDefaultFolderPermissions();
    }

    @Test
    public void testConfirmForeignAppointment() throws Exception {
        /*
         * as user A, create a new appointment in the user's default folder
         */
        Appointment appointment = createAppointmentObject("Bug56359Test");
        appointment.setIgnoreConflicts(true);
        appointment = catm.insert(appointment);
        /*
         * as user B, try and add an external user via 'confirm' action (in user b's personal calendar folder)
         */
        int folderId = testUser2.getAjaxClient().getValues().getPrivateAppointmentFolder();
        int objectId = appointment.getObjectID();
        ConfirmResponse confirmResponse = testUser2.getAjaxClient().execute(new ConfirmRequest(
            folderId, objectId, Appointment.ACCEPT, "", "test@example.com", appointment.getLastModified(), false));
        assertTrue("No errors in confirm response", confirmResponse.hasError());
        assertEquals("Unexpected error in confirm response", "APP-0059", confirmResponse.getException().getErrorCode());
        /*
         * verify that participant was not added
         */
        appointment = catm.get(appointment);
        for (Participant participant : appointment.getParticipants()) {
            assertNotEquals("External participant was added", "test@example.com", participant.getEmailAddress());
        }
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(2).build();
    }

}
