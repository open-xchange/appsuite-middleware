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
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.FolderTools;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 *
 */
public class Bug17327Test extends AbstractAJAXSession {

    AJAXClient client;
    AJAXClient client2;
    FolderObject sharedFolder;
    Appointment appointment;
    TimeZone tz, tz2;
    Calendar cal;

    public Bug17327Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        client = getClient();
        tz = getClient().getValues().getTimeZone();
        System.out.println(tz);
        client2 = testUser2.getAjaxClient();
        tz2 = client2.getValues().getTimeZone();

        // Create shared folder
        sharedFolder = Create.createPrivateFolder("Bug 17327 shared folder", FolderObject.CALENDAR, getClient().getValues().getUserId());
        sharedFolder.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        InsertResponse folderInsertResponse = getClient().execute(new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OUTLOOK, sharedFolder, true));
        folderInsertResponse.fillObject(sharedFolder);
        FolderTools.shareFolder(client, EnumAPI.OUTLOOK, sharedFolder.getObjectID(), client2.getValues().getUserId(), OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);

        // Create appointment
        appointment = createAppointment();
    }

    @Test
    public void testAlarmInAllRequest() throws Exception {
        AppointmentInsertResponse insertResponse = getClient().execute(new InsertRequest(appointment, tz, true));
        insertResponse.fillAppointment(appointment);

        // Get appointment via AllRequest
        Date rangeStart = TimeTools.getAPIDate(tz, appointment.getStartDate(), 0);
        Date rangeEnd = TimeTools.getAPIDate(tz, appointment.getEndDate(), 1);
        CommonAllResponse allResponseBeforeUpdate = client2.execute(new AllRequest(sharedFolder.getObjectID(), new int[] { Appointment.ALARM }, rangeStart, rangeEnd, tz2));
        //        CommonListResponse listResponseBeforeUpdate = client2.execute(new ListRequest(new ListIDs(appointment.getParentFolderID(), appointment.getObjectID()), new int[] {Appointment.ALARM}));
        GetResponse getResponseBeforeUpdate = client2.execute(new GetRequest(appointment));
        Object alarmValue = allResponseBeforeUpdate.getValue(0, Appointment.ALARM);
        int alarmValueInt;
        if (alarmValue == null) {
            alarmValueInt = 0;
        } else {
            alarmValueInt = ((Integer) alarmValue).intValue();
        }

        assertEquals("Alarm is not equal in All- and GetRequest before update.", getResponseBeforeUpdate.getAppointment(tz).getAlarm(), alarmValueInt);

        // Update alarm
        appointment.setAlarm(15);
        UpdateResponse updateResponse = getClient().execute(new UpdateRequest(appointment, tz));
        appointment.setLastModified(updateResponse.getTimestamp());

        // Get appointment via AllRequest
        CommonAllResponse allResponseAfterUpdate = client2.execute(new AllRequest(sharedFolder.getObjectID(), new int[] { Appointment.ALARM }, rangeStart, rangeEnd, tz2));
        //        CommonListResponse listResponseAfterUpdate = client2.execute(new ListRequest(new ListIDs(appointment.getParentFolderID(), appointment.getObjectID()), new int[] {Appointment.ALARM}));
        GetResponse getResponseAfterUpdate = client2.execute(new GetRequest(appointment));
        alarmValue = allResponseAfterUpdate.getValue(0, Appointment.ALARM);
        if (alarmValue == null) {
            alarmValueInt = 0;
        } else {
            alarmValueInt = ((Integer) alarmValue).intValue();
        }

        assertEquals("Alarm is not equal in All- and GetRequest after update.", getResponseAfterUpdate.getAppointment(tz).getAlarm(), alarmValueInt);
    }

    public Appointment createAppointment() {
        final Appointment appointment = new Appointment();
        appointment.setTitle("Bug 17327 Testappointment");
        appointment.setParentFolderID(sharedFolder.getObjectID());
        appointment.setIgnoreConflicts(true);
        cal = TimeTools.createCalendar(tz);
        appointment.setStartDate(cal.getTime());
        cal.add(Calendar.HOUR, 2);
        appointment.setEndDate(cal.getTime());

        return appointment;
    }

}
