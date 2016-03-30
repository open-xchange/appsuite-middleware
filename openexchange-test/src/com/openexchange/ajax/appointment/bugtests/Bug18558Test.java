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

package com.openexchange.ajax.appointment.bugtests;

import static com.openexchange.ajax.folder.Create.ocl;
import static com.openexchange.groupware.calendar.TimeTools.D;
import java.util.Date;
import org.json.JSONArray;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.SearchRequest;
import com.openexchange.ajax.appointment.action.SearchResponse;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.server.impl.OCLPermission;

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

    public Bug18558Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        clientA = getClient();
        clientB = new AJAXClient(User.User2);
        clientC = new AJAXClient(User.User3);
        clientD = new AJAXClient(User.User4);

        FolderObject folderA = new FolderObject();
        folderA.setObjectID(clientA.getValues().getPrivateAppointmentFolder());
        folderA.setLastModified(new Date(Long.MAX_VALUE));
        folderA.setPermissionsAsArray(new OCLPermission[] {
            ocl(
                clientA.getValues().getUserId(),
                false,
                true,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION),
            ocl(
                clientD.getValues().getUserId(),
                false,
                false,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION) });

        CommonInsertResponse response = clientA.execute(new com.openexchange.ajax.folder.actions.UpdateRequest(EnumAPI.OX_OLD, folderA));
        response.fillObject(folderA);

        FolderObject folderB = new FolderObject();
        folderB.setObjectID(clientB.getValues().getPrivateAppointmentFolder());
        folderB.setLastModified(new Date(Long.MAX_VALUE));
        folderB.setPermissionsAsArray(new OCLPermission[] {
            ocl(
                clientB.getValues().getUserId(),
                false,
                true,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION),
            ocl(
                clientD.getValues().getUserId(),
                false,
                false,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION) });

        response = clientB.execute(new com.openexchange.ajax.folder.actions.UpdateRequest(EnumAPI.OX_OLD, folderB));
        response.fillObject(folderB);

        appointment = new Appointment();
        appointment.setTitle("Bug18558Test" + System.currentTimeMillis());
        appointment.setStartDate(D("01.04.2011 08:00"));
        appointment.setEndDate(D("01.04.2011 09:00"));
        appointment.setParentFolderID(clientC.getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);
        appointment.setUsers(new UserParticipant[] {new UserParticipant(clientA.getValues().getUserId()), new UserParticipant(clientB.getValues().getUserId()) });
        appointment.setParticipants(new Participant[] {new UserParticipant(clientA.getValues().getUserId()), new UserParticipant(clientB.getValues().getUserId()) });

        InsertRequest insertRequest = new InsertRequest(appointment, clientC.getValues().getTimeZone());
        AppointmentInsertResponse insertResponse = clientC.execute(insertRequest);
        insertResponse.fillObject(appointment);
    }

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

    @Override
    public void tearDown() throws Exception {
        appointment.setLastModified(new Date(Long.MAX_VALUE));
        if (appointment.getObjectID() > 0) {
            clientC.execute(new DeleteRequest(appointment));
        }
        super.tearDown();
    }
}
