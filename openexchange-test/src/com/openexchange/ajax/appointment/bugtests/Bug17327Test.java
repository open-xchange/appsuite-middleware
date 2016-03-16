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

import java.util.Calendar;
import java.util.TimeZone;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
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
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

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

    public Bug17327Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        client = getClient();
        tz = client.getValues().getTimeZone();
        System.out.println(tz);
        client2 = new AJAXClient(User.User2);
        tz2 = client2.getValues().getTimeZone();

        // Create shared folder
        sharedFolder = Create.createPrivateFolder("Bug 17327 shared folder", FolderObject.CALENDAR, client.getValues().getUserId());
        sharedFolder.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        InsertResponse folderInsertResponse = client.execute(new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OUTLOOK, sharedFolder, true));
        folderInsertResponse.fillObject(sharedFolder);
        FolderTools.shareFolder(client, EnumAPI.OUTLOOK, sharedFolder.getObjectID(), client2.getValues().getUserId(),
            OCLPermission.CREATE_OBJECTS_IN_FOLDER,
            OCLPermission.READ_ALL_OBJECTS,
            OCLPermission.WRITE_ALL_OBJECTS,
            OCLPermission.DELETE_ALL_OBJECTS);

        // Create appointment
        appointment = createAppointment();
    }

    public void testAlarmInAllRequest() throws Exception {
        AppointmentInsertResponse insertResponse = client.execute(new InsertRequest(appointment, tz, true));
        insertResponse.fillAppointment(appointment);

        // Get appointment via AllRequest
        CommonAllResponse allResponseBeforeUpdate = client2.execute(new AllRequest(sharedFolder.getObjectID(), new int[] {Appointment.ALARM}, appointment.getStartDate(), appointment.getEndDate(), tz2));
//        CommonListResponse listResponseBeforeUpdate = client2.execute(new ListRequest(new ListIDs(appointment.getParentFolderID(), appointment.getObjectID()), new int[] {Appointment.ALARM}));
        GetResponse getResponseBeforeUpdate = client2.execute(new GetRequest(appointment));
        Object alarmValue = allResponseBeforeUpdate.getValue(0, Appointment.ALARM);
        int alarmValueInt;
        if (alarmValue == null) {
            alarmValueInt = 0;
        } else {
            alarmValueInt = (Integer) alarmValue;
        }

        assertEquals("Alarm is not equal in All- and GetRequest before update.", getResponseBeforeUpdate.getAppointment(tz).getAlarm(), alarmValueInt);

        // Update alarm
        appointment.setAlarm(15);
        UpdateResponse updateResponse = client.execute(new UpdateRequest(appointment, tz));
        appointment.setLastModified(updateResponse.getTimestamp());

        // Get appointment via AllRequest
        CommonAllResponse allResponseAfterUpdate = client2.execute(new AllRequest(sharedFolder.getObjectID(), new int[] {Appointment.ALARM}, appointment.getStartDate(), appointment.getEndDate(), tz2));
//        CommonListResponse listResponseAfterUpdate = client2.execute(new ListRequest(new ListIDs(appointment.getParentFolderID(), appointment.getObjectID()), new int[] {Appointment.ALARM}));
        GetResponse getResponseAfterUpdate = client2.execute(new GetRequest(appointment));
        alarmValue = allResponseAfterUpdate.getValue(0, Appointment.ALARM);
        if (alarmValue == null) {
            alarmValueInt = 0;
        } else {
            alarmValueInt = (Integer) alarmValue;
        }

        assertEquals("Alarm is not equal in All- and GetRequest after update.", getResponseAfterUpdate.getAppointment(tz).getAlarm(), alarmValueInt);
    }

    @Override
    public void tearDown() throws Exception {
        client.execute(new DeleteRequest(appointment));
        client.execute(new com.openexchange.ajax.folder.actions.DeleteRequest(EnumAPI.OUTLOOK, sharedFolder));
        client2.logout();

        super.tearDown();
    }

    public Appointment createAppointment() throws Exception {
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
