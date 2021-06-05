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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Date;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.ajax.participant.ParticipantTools;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Bug13826Test extends AbstractAJAXSession {

    private int userId, sourceFolderId, targetFolderId;

    private FolderObject folder;

    private Appointment appointment, updateAppointment;

    public Bug13826Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        userId = getClient().getValues().getUserId();
        sourceFolderId = getClient().getValues().getPrivateAppointmentFolder();
        OCLPermission ocl = ocl(userId, false, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        folder = Create.folder(sourceFolderId, "Folder to test bug 13826-" + UUID.randomUUID().toString(), FolderObject.CALENDAR, FolderObject.PRIVATE, ocl);
        CommonInsertResponse response = getClient().execute(new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_OLD, folder));
        response.fillObject(folder);
        targetFolderId = folder.getObjectID();

        appointment = new Appointment();
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        appointment.setTitle("Test Bug 13826");
        appointment.setStartDate(new Date(TimeTools.getHour(0, getClient().getValues().getTimeZone())));
        appointment.setEndDate(new Date(TimeTools.getHour(1, getClient().getValues().getTimeZone())));
        appointment.setParticipants(ParticipantTools.createParticipants(userId));
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        appointment.setOccurrence(5);
        appointment.setIgnoreConflicts(true);
        InsertRequest request = new InsertRequest(appointment, getClient().getValues().getTimeZone());
        AppointmentInsertResponse insertResponse = getClient().execute(request);
        insertResponse.fillObject(appointment);

        updateAppointment = new Appointment();
        updateAppointment.setObjectID(appointment.getObjectID());
        updateAppointment.setLastModified(appointment.getLastModified());
        updateAppointment.setParentFolderID(targetFolderId);
        updateAppointment.setRecurrenceType(Appointment.DAILY);
        updateAppointment.setInterval(1);
        updateAppointment.setOccurrence(5);
    }

    @Test
    public void testBug13826() throws Exception {
        UpdateRequest update = new UpdateRequest(sourceFolderId, updateAppointment, getClient().getValues().getTimeZone(), false);
        UpdateResponse updateResponse = getClient().execute(update);
        if (!updateResponse.hasError()) {
            fail("Expected error.");
        } else {
            assertTrue("Wrong error message", updateResponse.getException().similarTo(OXCalendarExceptionCodes.RECURRING_FOLDER_MOVE.create()));
        }
        getClient().execute(new GetRequest(sourceFolderId, appointment.getObjectID(), true)).getAppointment(getClient().getValues().getTimeZone());
        GetResponse getResponse = getClient().execute(new GetRequest(targetFolderId, appointment.getObjectID(), false));
        if (!getResponse.hasError()) {
            getResponse.getAppointment(getClient().getValues().getTimeZone());
            fail("Expected error.");
        }
    }

}
