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

import static com.openexchange.ajax.folder.Create.ocl;
import static com.openexchange.test.common.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.SearchRequest;
import com.openexchange.ajax.appointment.action.SearchResponse;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.common.test.TestClassConfig;

/**
 * {@link Bug18558Test}
 *
 * User A shares his calendar to User D.
 * User B shares his calendar to User D.
 * User C creates an appointment with A and B.
 * D should be able to access this appointment twice.
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug18558Test extends AbstractAJAXSession {

    private AJAXClient clientA;
    private AJAXClient clientB;
    private AJAXClient clientC;
    private AJAXClient clientD;
    private Appointment appointment;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        clientA = getClient();
        clientB = testUser2.getAjaxClient();
        clientC = testContext.acquireUser().getAjaxClient();
        clientD = testContext.acquireUser().getAjaxClient();

        FolderObject folderA = new FolderObject();
        folderA.setObjectID(clientA.getValues().getPrivateAppointmentFolder());
        folderA.setLastModified(new Date(Long.MAX_VALUE));
        folderA.setPermissionsAsArray(new OCLPermission[] {
            ocl(clientA.getValues().getUserId(), false, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION),
            ocl(clientD.getValues().getUserId(), false, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION) });

        CommonInsertResponse response = clientA.execute(new com.openexchange.ajax.folder.actions.UpdateRequest(EnumAPI.OX_OLD, folderA));
        response.fillObject(folderA);

        FolderObject folderB = new FolderObject();
        folderB.setObjectID(clientB.getValues().getPrivateAppointmentFolder());
        folderB.setLastModified(new Date(Long.MAX_VALUE));
        folderB.setPermissionsAsArray(new OCLPermission[] {
            ocl(clientB.getValues().getUserId(), false, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION),
            ocl(clientD.getValues().getUserId(), false, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION) });

        response = clientB.execute(new com.openexchange.ajax.folder.actions.UpdateRequest(EnumAPI.OX_OLD, folderB));
        response.fillObject(folderB);

        appointment = new Appointment();
        appointment.setTitle("Bug18558Test" + System.currentTimeMillis());
        appointment.setStartDate(D("01.04.2011 08:00"));
        appointment.setEndDate(D("01.04.2011 09:00"));
        appointment.setParentFolderID(clientC.getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);
        appointment.setUsers(new UserParticipant[] { new UserParticipant(clientA.getValues().getUserId()), new UserParticipant(clientB.getValues().getUserId()) });
        appointment.setParticipants(new Participant[] { new UserParticipant(clientA.getValues().getUserId()), new UserParticipant(clientB.getValues().getUserId()) });

        new CalendarTestManager(clientC).resetDefaultFolderPermissions();
        InsertRequest insertRequest = new InsertRequest(appointment, clientC.getValues().getTimeZone());
        AppointmentInsertResponse insertResponse = clientC.execute(insertRequest);
        insertResponse.fillObject(appointment);
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(4).build();
    }

    @Test
    public void testBug18558Test() throws Exception {
        SearchRequest search = new SearchRequest(appointment.getTitle(), -1, new int[] { Appointment.OBJECT_ID, Appointment.FOLDER_ID, Appointment.TITLE, Appointment.USERS, Appointment.PARTICIPANTS });
        SearchResponse searchResponse = clientD.execute(search);
        JSONArray jsonArray = (JSONArray) searchResponse.getResponse().getData();
        boolean foundA = false;
        boolean foundB = false;
        assertEquals("Expected appointment exactly twice.", 2, jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray result = jsonArray.getJSONArray(i);
            if (result.getInt(0) == appointment.getObjectID()) {
                if (result.getInt(1) == clientA.getValues().getPrivateAppointmentFolder()) {
                    foundA = true;
                }
                if (result.getInt(1) == clientB.getValues().getPrivateAppointmentFolder()) {
                    foundB = true;
                }
                assertEquals("Expected 3 Users.", 3, result.getJSONArray(3).length());
                assertEquals("Expected 3 Participants.", 3, result.getJSONArray(4).length());
                checkGet(appointment.getObjectID(), result.getInt(1));
            }
        }
        assertTrue("Did not find appointment in folder of first user.", foundA);
        assertTrue("Did not find appointment in folder of second user.", foundB);
    }

    private void checkGet(int objectId, int folderId) throws Exception {
        GetRequest getRequest = new GetRequest(folderId, objectId);
        GetResponse getResponse = clientD.execute(getRequest);
        assertFalse("No error expected.", getResponse.hasError());
    }

}
