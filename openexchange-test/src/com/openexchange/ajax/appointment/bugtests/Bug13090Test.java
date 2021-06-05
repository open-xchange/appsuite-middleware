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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Date;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link Bug13090Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug13090Test extends AbstractAJAXSession {

    private Appointment appointment;
    private FolderObject folder;
    private Appointment exception;
    private Appointment updateAppointment;

    /**
     * Initializes a new {@link Bug13090Test}.
     * 
     * @param name
     */
    public Bug13090Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        folder = Create.folder(FolderObject.SYSTEM_PRIVATE_FOLDER_ID, "Bug 13090 Folder " + UUID.randomUUID().toString(), FolderObject.CALENDAR, FolderObject.PRIVATE, ocl(getClient().getValues().getUserId(), false, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION));

        CommonInsertResponse response = getClient().execute(new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_OLD, folder));
        response.fillObject(folder);

        appointment = new Appointment();
        appointment.setStartDate(D("11.04.2011 08:00"));
        appointment.setEndDate(D("11.04.2011 09:00"));
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        appointment.setOccurrence(3);
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);
        appointment.setTitle("Bug 13090 Test");

        InsertRequest insertRequest = new InsertRequest(appointment, getClient().getValues().getTimeZone());
        AppointmentInsertResponse insertResponse = getClient().execute(insertRequest);
        insertResponse.fillAppointment(appointment);

        exception = new Appointment();
        exception.setObjectID(appointment.getObjectID());
        exception.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        exception.setStartDate(D("12.04.2011 09:00"));
        exception.setEndDate(D("12.04.2011 10:00"));
        exception.setRecurrenceType(Appointment.NO_RECURRENCE);
        exception.setIgnoreConflicts(true);
        exception.setLastModified(new Date(Long.MAX_VALUE));
        exception.setRecurrencePosition(2);

        UpdateRequest updateRequest = new UpdateRequest(exception, getClient().getValues().getTimeZone());
        getClient().execute(updateRequest);

        updateAppointment = new Appointment();
        updateAppointment.setObjectID(appointment.getObjectID());
        updateAppointment.setParentFolderID(folder.getObjectID());
        updateAppointment.setIgnoreConflicts(true);
        updateAppointment.setLastModified(new Date(Long.MAX_VALUE));
    }

    @Test
    public void testErrorMessag() throws Exception {
        UpdateRequest updateRequest = new UpdateRequest(getClient().getValues().getPrivateAppointmentFolder(), updateAppointment, getClient().getValues().getTimeZone(), false);
        UpdateResponse updateResponse = getClient().execute(updateRequest);
        if (updateResponse.hasError()) {
            assertTrue("Wrong exception code.", updateResponse.getException().similarTo(OXCalendarExceptionCodes.RECURRING_FOLDER_MOVE.create()));
        } else {
            fail("Error expected.");
        }
    }

}
