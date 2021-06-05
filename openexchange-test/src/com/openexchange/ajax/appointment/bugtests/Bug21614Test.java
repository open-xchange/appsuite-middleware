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
import static org.junit.Assert.assertFalse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.common.test.TestClassConfig;

/**
 * {@link Bug21614Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug21614Test extends AbstractAJAXSession {

    private Appointment appointment;

    private AJAXClient clientA;

    private AJAXClient clientB;

    public Bug21614Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        clientA = getClient();
        clientB = testUser2.getAjaxClient();

        List<Participant> participants = new ArrayList<Participant>();
        Participant p = new UserParticipant(clientB.getValues().getUserId());
        participants.add(p);

        appointment = new Appointment();
        appointment.setParentFolderID(clientA.getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);
        appointment.setTitle("Bug 21614");
        appointment.setStartDate(D("16.04.2012 08:00"));
        appointment.setEndDate(D("16.04.2012 09:00"));
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setOccurrence(5);
        appointment.setInterval(1);
        appointment.setParticipants(participants);
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(2).build();
    }

    @Test
    public void testBug21614() throws Exception {
        InsertRequest insertRequest = new InsertRequest(appointment, clientA.getValues().getTimeZone());
        AppointmentInsertResponse insertResponse = clientA.execute(insertRequest);
        insertResponse.fillObject(appointment);

        DeleteRequest deleteRequest = new DeleteRequest(appointment.getObjectID(), clientA.getValues().getPrivateAppointmentFolder(), 5, appointment.getLastModified());
        CommonDeleteResponse deleteResponse = clientA.execute(deleteRequest);
        appointment.setLastModified(deleteResponse.getTimestamp());

        assertNotFind(clientA);
        assertNotFind(clientB);

        deleteRequest = new DeleteRequest(appointment.getObjectID(), clientB.getValues().getPrivateAppointmentFolder(), 2, appointment.getLastModified());
        try {
            deleteResponse = clientB.execute(deleteRequest);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        appointment.setLastModified(new Date(Long.MAX_VALUE));

        assertNotFind(clientA);
        assertNotFind(clientB);
    }

    private void assertNotFind(AJAXClient c) throws IOException, JSONException, OXException {
        AllRequest allRequest = new AllRequest(c.getValues().getPrivateAppointmentFolder(), new int[] { Appointment.OBJECT_ID }, new Date(1334880000000L), new Date(1334966400000L), TimeZone.getTimeZone("UTC"), false);
        CommonAllResponse allResponse = c.execute(allRequest);

        boolean found = false;
        Object[][] objects = allResponse.getArray();
        for (Object[] object : objects) {
            if (((Integer) object[0]).intValue() == appointment.getObjectID()) {
                found = true;
            }
        }
        assertFalse("Should not find the appointment.", found);
    }

}
