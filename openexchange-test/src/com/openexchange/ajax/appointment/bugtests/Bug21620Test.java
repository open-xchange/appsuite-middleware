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

import static com.openexchange.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.FolderTools;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link Bug21620Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug21620Test extends AbstractAJAXSession {

    private FolderObject sharedFolder;
    private Appointment appointment;
    private AJAXClient clientA;
    private AJAXClient clientB;
    private AJAXClient clientC;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        clientA = getClient();
        clientB = new AJAXClient(testContext.acquireUser());
        clientC = new AJAXClient(testContext.acquireUser());

        // as user A, create folder shared to user B
        sharedFolder = Create.createPrivateFolder("shared_" + System.currentTimeMillis(), FolderObject.CALENDAR, clientA.getValues().getUserId());
        sharedFolder.setParentFolderID(clientA.getValues().getPrivateAppointmentFolder());
        InsertResponse folderInsertResponse = clientA.execute(new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_NEW, sharedFolder));
        sharedFolder.setObjectID(folderInsertResponse.getId());
        sharedFolder.setLastModified(clientA.execute(new com.openexchange.ajax.folder.actions.GetRequest(EnumAPI.OX_NEW, sharedFolder.getObjectID())).getTimestamp());
        FolderTools.shareFolder(clientA, EnumAPI.OX_NEW, sharedFolder.getObjectID(), clientB.getValues().getUserId(), OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);

        // prepare appointment
        List<UserParticipant> users = new ArrayList<UserParticipant>();
        UserParticipant userB = new UserParticipant(clientB.getValues().getUserId());
        userB.setConfirm(1);
        users.add(userB);

        List<Participant> participants = new ArrayList<Participant>();
        Participant participantB = new UserParticipant(clientB.getValues().getUserId());
        Participant participantC = new UserParticipant(clientC.getValues().getUserId());
        participants.add(participantB);
        participants.add(participantC);

        appointment = new Appointment();
        appointment.setIgnoreConflicts(true);
        appointment.setUid(UUID.randomUUID().toString());
        appointment.setParentFolderID(sharedFolder.getObjectID());
        appointment.setTitle("Bug 21620");
        appointment.setStartDate(D("14.04.2012 04:00"));
        appointment.setEndDate(D("14.04.2012 04:30"));
        appointment.setNumberOfAttachments(0);
        appointment.setModifiedBy(clientA.getValues().getUserId());
        appointment.setCreatedBy(clientA.getValues().getUserId());
        appointment.setFullTime(false);
        appointment.setPrivateFlag(false);
        appointment.setTimezone("Europe/Berlin");
        appointment.setUsers(users);
        appointment.setParticipants(participants);
        appointment.setOrganizer(clientB.getValues().getDefaultAddress());
        appointment.setOrganizerId(clientB.getValues().getUserId());
        appointment.setPrincipal(clientA.getValues().getDefaultAddress());
        appointment.setPrincipalId(clientA.getValues().getUserId());
    }

    public void testBug21620() throws Exception {
        /*
         * insert appointment in user a's calendar as user B ("on behalf of user a")
         */
        InsertRequest insertRequest = new InsertRequest(appointment, clientB.getValues().getTimeZone());
        AppointmentInsertResponse insertResponse = clientB.execute(insertRequest);
        insertResponse.fillObject(appointment);
        /*
         * verify organizer & principal as user B
         */
        GetRequest getRequest = new GetRequest(appointment);
        GetResponse getResponse = clientB.execute(getRequest);
        Appointment loadedAppointment = getResponse.getAppointment(clientB.getValues().getTimeZone());
        assertEquals("Wrong organizer ID", clientB.getValues().getUserId(), loadedAppointment.getOrganizerId());
        assertEquals("Wrong principal ID", clientA.getValues().getUserId(), loadedAppointment.getPrincipalId());
        /*
         * verify organizer & principal as user A
         */
        getRequest = new GetRequest(appointment);
        getResponse = clientB.execute(getRequest);
        loadedAppointment = getResponse.getAppointment(clientA.getValues().getTimeZone());
        assertEquals("Wrong organizer ID", clientB.getValues().getUserId(), loadedAppointment.getOrganizerId());
        assertEquals("Wrong principal ID", clientA.getValues().getUserId(), loadedAppointment.getPrincipalId());
    }

    @After
    public void tearDown() throws Exception {
        try {
            getClient().execute(new DeleteRequest(appointment));
        } finally {
            super.tearDown();
        }
    }

}
