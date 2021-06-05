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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.test.common.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.common.test.TestClassConfig;
import com.openexchange.test.common.test.pool.TestUser;

/**
 * {@link Bug41794Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.1
 */
public class Bug41794Test extends AbstractAJAXSession {

    private AJAXClient client3;
    private CalendarTestManager ctm2;
    private CalendarTestManager ctm3;
    private int groupParticipant;
    private Appointment appointment;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        TestUser user3 = testContext.acquireUser();
        client3 = user3.getAjaxClient();
        groupParticipant = i(testContext.acquireGroup(Optional.of(Collections.singletonList(I(user3.getUserId()))))); //TODO null check
        catm = new CalendarTestManager(getClient());
        ctm2 = new CalendarTestManager(testUser2.getAjaxClient());
        ctm3 = new CalendarTestManager(client3);

        appointment = new Appointment();
        appointment.setTitle(this.getClass().getSimpleName());
        appointment.setStartDate(D("01.11.2015 08:00"));
        appointment.setEndDate(D("01.11.2015 09:00"));
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);

        UserParticipant up = new UserParticipant(getClient().getValues().getUserId());
        GroupParticipant gp = getGroupParticipant(groupParticipant);
        appointment.setParticipants(new Participant[] { up, gp });
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(2).build();
    }

    @Test
    public void testBug41794() throws Exception {
        catm.insert(appointment);

        appointment.setParentFolderID(testUser2.getAjaxClient().getValues().getPrivateAppointmentFolder());
        ctm2.delete(appointment);

        assertNull("Did not expect appointment for user 2", ctm2.get(testUser2.getAjaxClient().getValues().getPrivateAppointmentFolder(), appointment.getObjectID(), false));

        Appointment loadedAppointment = ctm3.get(client3.getValues().getPrivateAppointmentFolder(), appointment.getObjectID());
        assertNotNull(loadedAppointment);
        loadedAppointment.setAlarm(15);
        loadedAppointment.setLastModified(new Date(Long.MAX_VALUE));
        loadedAppointment.setIgnoreConflicts(true);
        ctm3.confirm(loadedAppointment, Appointment.ACCEPT, "message");
        ctm3.update(loadedAppointment);

        assertNull("Did not expect appointment for user 2", ctm2.get(testUser2.getAjaxClient().getValues().getPrivateAppointmentFolder(), appointment.getObjectID(), false));
    }

    private GroupParticipant getGroupParticipant(int groupParticipantId) {
        GroupParticipant gpart = new GroupParticipant(groupParticipantId);
        return gpart;
    }

}
