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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.common.test.TestClassConfig;

/**
 * {@link Bug31779Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug31779Test extends AbstractAJAXSession {

    private int nextYear;

    private Appointment appointment;

    private CalendarTestManager ctm2;

    public Bug31779Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ctm2 = new CalendarTestManager(testUser2.getAjaxClient());

        nextYear = Calendar.getInstance().get(Calendar.YEAR) + 1;
        appointment = new Appointment();
        appointment.setTitle("Bug 31779 appointment.");
        appointment.setStartDate(D("01.04." + nextYear + " 08:00"));
        appointment.setEndDate(D("01.04." + nextYear + " 09:00"));
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        appointment.setIgnoreConflicts(true);
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        UserParticipant user1 = new UserParticipant(getClient().getValues().getUserId());
        UserParticipant user2 = new UserParticipant(testUser2.getAjaxClient().getValues().getUserId());
        appointment.setParticipants(new Participant[] { user1, user2 });
        appointment.setUsers(new UserParticipant[] { user1, user2 });

        catm.insert(appointment);
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(2).build();
    }

    /**
     * Tests the bug as written in the report: If the creator deletes an exception, the whole exception should disappear.
     *
     * @throws Exception
     */
    @Test
    public void testBug31779() throws Exception {
        Appointment exception = ctm2.createIdentifyingCopy(appointment);
        exception.setParentFolderID(testUser2.getAjaxClient().getValues().getPrivateAppointmentFolder());
        exception.setNote("Hello World");
        exception.setRecurrencePosition(2);
        ctm2.update(exception);
        exception.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        catm.delete(catm.createIdentifyingCopy(exception));
        exception.setParentFolderID(testUser2.getAjaxClient().getValues().getPrivateAppointmentFolder());
        Appointment loadedException = ctm2.get(exception);
        assertNull("No object expected.", loadedException);
        assertTrue("Error expected.", ctm2.getLastResponse().hasError());
        assertTrue("No object expected.", ctm2.getLastResponse().getErrorMessage().contains("Object not found"));
    }

    /**
     * Tests the case, that a participant deletes an exception: Only the participant should be removed.
     *
     * @throws Exception
     */
    @Test
    public void testDeleteByparticipant() throws Exception {
        Appointment exception = ctm2.createIdentifyingCopy(appointment);
        exception.setParentFolderID(testUser2.getAjaxClient().getValues().getPrivateAppointmentFolder());
        exception.setNote("Hello World");
        exception.setRecurrencePosition(2);
        ctm2.update(exception);
        ctm2.delete(ctm2.createIdentifyingCopy(exception));
        exception.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        Appointment loadedException = catm.get(exception);
        assertNotNull("Object expected.", loadedException);
        assertEquals("Wrong creator.", getClient().getValues().getUserId(), loadedException.getCreatedBy());
        assertEquals("Wrong changer.", testUser2.getAjaxClient().getValues().getUserId(), loadedException.getModifiedBy());
    }

}
