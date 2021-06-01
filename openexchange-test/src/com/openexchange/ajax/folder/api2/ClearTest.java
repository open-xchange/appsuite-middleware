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

package com.openexchange.ajax.folder.api2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import org.json.JSONArray;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.ClearRequest;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.groupware.calendar.Constants;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.common.configuration.AJAXConfig;

/**
 * {@link ClearTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ClearTest extends AbstractAJAXSession {

    /**
     * Initializes a new {@link ClearTest}.
     *
     * @param name The name of the test.
     */
    public ClearTest() {
        super();
    }

    @Test
    public void testClearPrivate() throws Throwable {
        AJAXClient client = getClient();
        // Get root folder
        String newId = null;
        try {
            final FolderObject fo = new FolderObject();
            fo.setParentFolderID(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
            fo.setFolderName("testCalendarFolder" + System.currentTimeMillis());
            fo.setModule(FolderObject.CALENDAR);

            final OCLPermission oclP = new OCLPermission();
            oclP.setEntity(client.getValues().getUserId());
            oclP.setGroupPermission(false);
            oclP.setFolderAdmin(true);
            oclP.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
            fo.setPermissionsAsArray(new OCLPermission[] { oclP });

            final InsertRequest request = new InsertRequest(EnumAPI.OUTLOOK, fo);
            final InsertResponse response = client.execute(request);

            newId = (String) response.getResponse().getData();
            assertNotNull("New ID must not be null!", newId);

            // Put some appointments in this folder
            String protocol = null == client.getProtocol() ? AJAXConfig.getProperty(AJAXConfig.Property.PROTOCOL) : client.getProtocol();
            if (!protocol.endsWith("://")) {
                protocol = protocol + "://";
            }

            final long s = System.currentTimeMillis();
            {
                final Appointment appointmentObj = new Appointment();
                appointmentObj.setTitle("test1Simple" + s);
                appointmentObj.setStartDate(new Date(s));
                appointmentObj.setEndDate(new Date(s + 3600000));
                appointmentObj.setShownAs(Appointment.ABSENT);
                appointmentObj.setParentFolderID(Integer.parseInt(newId));
                appointmentObj.setIgnoreConflicts(true);

                catm.insert(appointmentObj).getObjectID();
            }
            {
                final Appointment appointmentObj = new Appointment();
                appointmentObj.setTitle("test2Simple" + System.currentTimeMillis());
                appointmentObj.setStartDate(new Date(s + (2 * 3600000)));
                appointmentObj.setEndDate(new Date(s + (3 * 3600000)));
                appointmentObj.setShownAs(Appointment.ABSENT);
                appointmentObj.setParentFolderID(Integer.parseInt(newId));
                appointmentObj.setIgnoreConflicts(true);

                catm.insert(appointmentObj).getObjectID();
            }

            final CalendarTestManager calendarTestManager = new CalendarTestManager(client);
            final Appointment[] appointments = calendarTestManager.all(Integer.parseInt(newId), new Date(s - Constants.MILLI_WEEK), new Date(s + Constants.MILLI_WEEK));

            assertTrue("Appointments were not created.", null != appointments && appointments.length == 2);

            final ClearRequest clearRequest = new ClearRequest(EnumAPI.OUTLOOK, newId);
            final CommonDeleteResponse clearResponse = client.execute(clearRequest);

            final JSONArray nonClearedIDs = (JSONArray) clearResponse.getResponse().getData();

            assertEquals("Folder could not be cleared.", 0, nonClearedIDs.length());

            final Appointment[] emptyAppointments = calendarTestManager.all(Integer.parseInt(newId), new Date(s - Constants.MILLI_WEEK), new Date(s + Constants.MILLI_WEEK));

            assertTrue("Appointments were not cleared.", null == emptyAppointments || emptyAppointments.length == 0);

        } finally {
            if (null != newId) {
                // Delete folder
                try {
                    final DeleteRequest deleteRequest = new DeleteRequest(EnumAPI.OUTLOOK, newId, new Date());
                    client.execute(deleteRequest);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
