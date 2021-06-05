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
import static org.junit.Assert.assertTrue;
import java.util.Date;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.helper.AbstractAssertion;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.common.test.TestClassConfig;

public class Bug5144Test_UserGetsRemovedFromParticipantList extends ManagedAppointmentTest {

    public Bug5144Test_UserGetsRemovedFromParticipantList() {
        super();
    }

    @Test
    public void testIt() throws Exception {
        int uid1 = getClient().getValues().getUserId();

        UserParticipant other = new UserParticipant(testUser2.getAjaxClient().getValues().getUserId());
        assertTrue(other.getIdentifier() > 0);
        int fid1 = folder.getObjectID();

        Appointment app = AbstractAssertion.generateDefaultAppointment(fid1);
        app.addParticipant(other);

        catm.insert(app);

        int appId = app.getObjectID();
        int fid2 = testUser2.getAjaxClient().getValues().getPrivateAppointmentFolder();

        GetResponse getResponse = testUser2.getAjaxClient().execute(new GetRequest(fid2, appId));
        Date lastMod = getResponse.getTimestamp();

        testUser2.getAjaxClient().execute(new DeleteRequest(appId, fid2, lastMod));

        Appointment actual = catm.get(app);

        Participant[] participants = actual.getParticipants();

        assertEquals(2, participants.length);
        assertEquals(uid1, participants[0].getIdentifier());
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(2).build();
    }

}
