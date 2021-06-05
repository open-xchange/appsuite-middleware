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

import static com.openexchange.ajax.framework.ListIDs.l;
import static com.openexchange.java.Autoboxing.i;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.appointment.action.ListRequest;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.framework.ListIDInt;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.FolderTestManager;
import com.openexchange.test.common.test.TestClassConfig;

public class ListTest extends AppointmentTest {

    private FolderObject folder;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        catm.setFailOnError(true);
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(2).build();
    }

    @Test
    public void testList() {
        final Appointment appointmentObj = createAppointmentObject("testList");
        appointmentObj.setIgnoreConflicts(true);
        final Appointment appointmentObj2 = createAppointmentObject("testList");
        appointmentObj2.setIgnoreConflicts(true);
        final Appointment appointmentObj3 = createAppointmentObject("testList");
        appointmentObj3.setIgnoreConflicts(true);

        final Appointment id1 = catm.insert(appointmentObj);
        final Appointment id2 = catm.insert(appointmentObj2);
        final Appointment id3 = catm.insert(appointmentObj3);

        final int cols[] = new int[] { Appointment.OBJECT_ID, Appointment.TITLE, Appointment.CREATED_BY, Appointment.FOLDER_ID, Appointment.USERS };

        ListIDs listIDs = new ListIDs();
        listIDs.add(new ListIDInt(appointmentFolderId, id1.getObjectID()));
        listIDs.add(new ListIDInt(appointmentFolderId, id2.getObjectID()));
        listIDs.add(new ListIDInt(appointmentFolderId, id3.getObjectID()));

        final List<Appointment> appointmentArray = catm.list(listIDs, cols);

        assertEquals("check response array", 3, appointmentArray.size());
    }

    @Test
    public void testListWithNoEntries() {
        final Appointment appointmentObj = createAppointmentObject("testList");
        appointmentObj.setIgnoreConflicts(true);
        final Appointment appointmentObj2 = createAppointmentObject("testList");
        appointmentObj2.setIgnoreConflicts(true);
        final Appointment appointmentObj3 = createAppointmentObject("testList");
        appointmentObj3.setIgnoreConflicts(true);

        catm.insert(appointmentObj);
        catm.insert(appointmentObj2);
        catm.insert(appointmentObj3);

        ListIDs listIDs = new ListIDs();
        listIDs.add(new ListIDInt(appointmentFolderId, 0));
        listIDs.add(new ListIDInt(appointmentFolderId, 0));
        listIDs.add(new ListIDInt(appointmentFolderId, 0));
        ;
        final int cols[] = new int[] { Appointment.OBJECT_ID, Appointment.TITLE, Appointment.CREATED_BY, Appointment.FOLDER_ID, Appointment.USERS };
        final List<Appointment> appointmentArray = catm.list(listIDs, cols);

        assertEquals("check response array", 0, appointmentArray.size());
    }

    @Test
    public void testListWithAllFields() throws Exception {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testListWithAllFields");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setLocation("Location");
        appointmentObj.setShownAs(Appointment.FREE);
        appointmentObj.setParentFolderID(appointmentFolderId);
        // appointmentObj.setPrivateFlag(true); // Currently not supported!
        appointmentObj.setFullTime(true);
        appointmentObj.setLabel(2);
        appointmentObj.setNote("note");
        appointmentObj.setCategories("testcat1,testcat2,testcat3");
        appointmentObj.setOrganizer("someone.else@example.com");
        appointmentObj.setUid("1234567890abcdef" + System.currentTimeMillis());
        appointmentObj.setSequence(0);

        final int userParticipantId = testUser2.getAjaxClient().getValues().getUserId();
        final int groupParticipantId = i(testContext.acquireGroup(Optional.empty()));
        final int resourceParticipantId = i(testContext.acquireResource());
        final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[4];
        participants[0] = new UserParticipant(userId);
        participants[1] = new UserParticipant(userParticipantId);
        participants[2] = new GroupParticipant(groupParticipantId);
        participants[3] = new ResourceParticipant(resourceParticipantId);

        appointmentObj.setParticipants(participants);
        appointmentObj.setIgnoreConflicts(true);

        Appointment appointment = catm.insert(appointmentObj);

        ListIDs listIDs = new ListIDs();
        listIDs.add(new ListIDInt(appointmentFolderId, appointment.getObjectID()));
        final List<Appointment> appointmentArray = catm.list(listIDs, APPOINTMENT_FIELDS);

        assertEquals("check response array", 1, appointmentArray.size());

        final Appointment loadAppointment = appointmentArray.get(0);

        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final long newStartTime = c.getTimeInMillis();
        final long newEndTime = newStartTime + 86400000;

        compareObject(appointmentObj, loadAppointment, newStartTime, newEndTime);

    }

    @Test
    public void testListWithRecurrencePosition() {
        final FolderObject folderObj = new FolderObject();
        folderObj.setFolderName("testListWithRecurrencePosition" + System.currentTimeMillis());
        folderObj.setParentFolderID(FolderObject.PUBLIC);
        folderObj.setModule(FolderObject.CALENDAR);
        folderObj.setType(FolderObject.PUBLIC);

        final OCLPermission[] permission = new OCLPermission[] { FolderTestManager.createPermission(userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION), };

        folderObj.setPermissionsAsArray(permission);

        folder = ftm.insertFolderOnServer(folderObj);
        final int publicFolderId = folder.getObjectID();

        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final Date until = new Date(c.getTimeInMillis() + (15 * dayInMillis));

        Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testListWithRecurrencePosition");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(publicFolderId);
        appointmentObj.setRecurrenceType(Appointment.DAILY);
        appointmentObj.setInterval(1);
        appointmentObj.setUntil(until);
        appointmentObj.setIgnoreConflicts(true);
        Appointment appointment1 = catm.insert(appointmentObj);

        Appointment appointmentObj2 = new Appointment();
        appointmentObj2.setTitle("testListWithRecurrencePosition2");
        appointmentObj2.setStartDate(new Date(startTime));
        appointmentObj2.setEndDate(new Date(endTime));
        appointmentObj2.setShownAs(Appointment.ABSENT);
        appointmentObj2.setIgnoreConflicts(true);
        appointmentObj2.setParentFolderID(appointmentFolderId);
        Appointment appointment2 = catm.insert(appointmentObj2);

        final Appointment[] appointmentList = new Appointment[3];
        appointmentList[0] = new Appointment();
        appointmentList[0].setObjectID(appointment1.getObjectID());
        appointmentList[0].setParentFolderID(publicFolderId);
        appointmentList[0].setRecurrencePosition(2);
        appointmentList[1] = new Appointment();
        appointmentList[1].setObjectID(appointment1.getObjectID());
        appointmentList[1].setParentFolderID(publicFolderId);
        appointmentList[1].setRecurrencePosition(3);
        appointmentList[2] = new Appointment();
        appointmentList[2].setObjectID(appointment2.getObjectID());
        appointmentList[2].setParentFolderID(appointmentFolderId);
    }

    // Node 2652    @Test
    @Test
    public void testLastModifiedUTC() throws Exception {
        final int cols[] = new int[] { Appointment.OBJECT_ID, Appointment.FOLDER_ID, Appointment.LAST_MODIFIED_UTC };

        final Appointment appointmentObj = createAppointmentObject("testShowLastModifiedUTC");
        appointmentObj.setStartDate(new Date());
        appointmentObj.setEndDate(new Date(System.currentTimeMillis() + 60 * 60 * 1000));
        appointmentObj.setIgnoreConflicts(true);
        Appointment appointment = catm.insert(appointmentObj);
        final ListRequest listRequest = new ListRequest(l(new int[] { appointmentFolderId, appointment.getObjectID() }), cols, true);
        final CommonListResponse response = Executor.execute(getClient(), listRequest);
        final JSONArray arr = (JSONArray) response.getResponse().getData();

        assertNotNull(arr);
        final int size = arr.length();
        assertTrue(size > 0);

        for (int i = 0; i < size; i++) {
            final JSONArray objectData = arr.optJSONArray(i);
            assertNotNull(objectData);
            assertNotNull(objectData.opt(2));
        }
    }

}
