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

package com.openexchange.ajax.appointment;

import static com.openexchange.java.Autoboxing.i;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.common.test.TestClassConfig;

public class GetTest extends AppointmentTest {

    @Test
    public void testGet() throws Exception {
        final Appointment appointmentObj = createAppointmentObject("testGet");
        appointmentObj.setOrganizer(getClient().getValues().getDefaultAddress());
        final int objectId = catm.insert(appointmentObj).getObjectID();

        final Appointment loadAppointment = catm.get(appointmentFolderId, objectId);

        appointmentObj.setObjectID(objectId);
        compareObject(appointmentObj, loadAppointment, appointmentObj.getStartDate().getTime(), appointmentObj.getEndDate().getTime());
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(2).build();
    }

    @Test
    public void testGetWithParticipants() throws Exception {
        final Appointment appointmentObj = createAppointmentObject("testGetWithParticipants");

        final int userParticipantId = testUser2.getAjaxClient().getValues().getUserId();
        final int groupParticipantId = i(testContext.acquireGroup(Optional.empty())); //TODO null check
        final int resourceParticipantId = i(testContext.acquireResource()); // TODO add null check

        final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[4];
        participants[0] = new UserParticipant(userId);
        participants[1] = new UserParticipant(userParticipantId);
        participants[2] = new GroupParticipant(groupParticipantId);
        participants[3] = new ResourceParticipant(resourceParticipantId);

        appointmentObj.setParticipants(participants);

        final int objectId = catm.insert(appointmentObj).getObjectID();
        assertFalse(catm.getLastResponse().hasConflicts());

        final Appointment loadAppointment = catm.get(appointmentFolderId, objectId);
        appointmentObj.setObjectID(objectId);
        compareObject(appointmentObj, loadAppointment, appointmentObj.getStartDate().getTime(), appointmentObj.getEndDate().getTime());
    }

    @Test
    public void testGetWithAllFields() throws Exception {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testGetWithAllFields");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setLocation("Location");
        appointmentObj.setShownAs(Appointment.FREE);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setPrivateFlag(false);
        appointmentObj.setFullTime(true);
        appointmentObj.setLabel(2);
        appointmentObj.setNote("note");
        appointmentObj.setCategories("testcat1,testcat2,testcat3");
        appointmentObj.setIgnoreConflicts(true);

        final int userParticipantId = testUser2.getAjaxClient().getValues().getUserId();
        final int groupParticipantId = i(testContext.acquireGroup(Optional.empty())); //TODO null check
        final int resourceParticipantId = i(testContext.acquireResource()); // TODO add null check

        final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[4];
        participants[0] = new UserParticipant(userId);
        participants[1] = new UserParticipant(userParticipantId);
        participants[2] = new GroupParticipant(groupParticipantId);
        participants[3] = new ResourceParticipant(resourceParticipantId);

        appointmentObj.setParticipants(participants);

        final int objectId = catm.insert(appointmentObj).getObjectID();

        final Appointment loadAppointment = catm.get(appointmentFolderId, objectId);

        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final long newStartTime = c.getTimeInMillis();
        final long newEndTime = newStartTime + dayInMillis;

        appointmentObj.setObjectID(objectId);
        compareObject(appointmentObj, loadAppointment, newStartTime, newEndTime);
    }

    @Test
    public void testGetWithAllFieldsOnUpdate() throws Exception {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testGetWithAllFieldsOnUpdate");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);

        final int objectId = catm.insert(appointmentObj).getObjectID();

        appointmentObj.setLocation("Location");
        appointmentObj.setShownAs(Appointment.FREE);
        appointmentObj.setPrivateFlag(false);
        appointmentObj.setFullTime(true);
        appointmentObj.setLabel(2);
        appointmentObj.setNote("note");
        appointmentObj.setCategories("testcat1,testcat2,testcat3");

        final int userParticipantId = testUser2.getAjaxClient().getValues().getUserId();
        final int groupParticipantId = i(testContext.acquireGroup(Optional.empty())); //TODO null check
        final int resourceParticipantId = i(testContext.acquireResource()); // TODO add null check

        final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[4];
        participants[0] = new UserParticipant(userId);
        participants[1] = new UserParticipant(userParticipantId);
        participants[2] = new GroupParticipant(groupParticipantId);
        participants[3] = new ResourceParticipant(resourceParticipantId);

        appointmentObj.setParticipants(participants);

        appointmentObj.removeParentFolderID();

        catm.update(appointmentFolderId, appointmentObj);

        final Appointment loadAppointment = catm.get(appointmentFolderId, objectId);

        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final long newStartTime = c.getTimeInMillis();
        final long newEndTime = newStartTime + dayInMillis;

        appointmentObj.setObjectID(objectId);
        appointmentObj.setParentFolderID(appointmentFolderId);
        compareObject(appointmentObj, loadAppointment, newStartTime, newEndTime);
    }

    // Node 2652    @Test
    @Test
    public void testLastModifiedUTC() throws Exception {
        final Appointment appointmentObj = createAppointmentObject("testShowLastModifiedUTC");
        appointmentObj.setStartDate(new Date());
        appointmentObj.setEndDate(new Date(System.currentTimeMillis() + 60 * 60 * 1000));
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = catm.insert(appointmentObj).getObjectID();
        final GetRequest getRequest = new GetRequest(appointmentFolderId, objectId);
        final GetResponse response = Executor.execute(getClient(), getRequest);
        final JSONObject appointment = (JSONObject) response.getResponse().getData();

        assertNotNull(appointment);
        assertTrue(appointment.has("last_modified_utc"));

    }
}
