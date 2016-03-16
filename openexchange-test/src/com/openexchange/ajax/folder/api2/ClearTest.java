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

package com.openexchange.ajax.folder.api2;

import java.util.Date;
import org.json.JSONArray;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.folder.actions.ClearRequest;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.groupware.calendar.Constants;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link ClearTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ClearTest extends AbstractAJAXSession {

    private AJAXClient client;

    /**
     * Initializes a new {@link ClearTest}.
     *
     * @param name The name of the test.
     */
    public ClearTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
    }

    public void testClearPrivate() throws Throwable {
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
            oclP.setAllPermission(
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION);
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
            final String hostname =
                null == client.getHostname() ? AJAXConfig.getProperty(AJAXConfig.Property.HOSTNAME) : client.getHostname();

            final AJAXSession session = client.getSession();
            final UserValues values = client.getValues();
            final long s = System.currentTimeMillis();
            {
                final Appointment appointmentObj = new Appointment();
                appointmentObj.setTitle("test1Simple" + s);
                appointmentObj.setStartDate(new Date(s));
                appointmentObj.setEndDate(new Date(s + 3600000));
                appointmentObj.setShownAs(Appointment.ABSENT);
                appointmentObj.setParentFolderID(Integer.parseInt(newId));
                appointmentObj.setIgnoreConflicts(true);

                AppointmentTest.insertAppointment(
                    session.getConversation(),
                    appointmentObj,
                    values.getTimeZone(),
                    protocol + hostname,
                    session.getId());
            }
            {
                final Appointment appointmentObj = new Appointment();
                appointmentObj.setTitle("test2Simple" + System.currentTimeMillis());
                appointmentObj.setStartDate(new Date(s + (2 * 3600000)));
                appointmentObj.setEndDate(new Date(s + (3 * 3600000)));
                appointmentObj.setShownAs(Appointment.ABSENT);
                appointmentObj.setParentFolderID(Integer.parseInt(newId));
                appointmentObj.setIgnoreConflicts(true);

                AppointmentTest.insertAppointment(
                    session.getConversation(),
                    appointmentObj,
                    values.getTimeZone(),
                    protocol + hostname,
                    session.getId());
            }

            final CalendarTestManager calendarTestManager = new CalendarTestManager(client);
            final Appointment[] appointments =
                calendarTestManager.all(Integer.parseInt(newId), new Date(s - Constants.MILLI_WEEK), new Date(s + Constants.MILLI_WEEK));

            assertTrue("Appointments were not created.", null != appointments && appointments.length == 2);

            final ClearRequest clearRequest = new ClearRequest(EnumAPI.OUTLOOK, newId);
            final CommonDeleteResponse clearResponse = client.execute(clearRequest);

            final JSONArray nonClearedIDs = (JSONArray) clearResponse.getResponse().getData();

            assertEquals("Folder could not be cleared.", 0, nonClearedIDs.length());

            final Appointment[] emptyAppointments =
                calendarTestManager.all(Integer.parseInt(newId), new Date(s - Constants.MILLI_WEEK), new Date(s + Constants.MILLI_WEEK));

            assertTrue("Appointments were not cleared.", null == emptyAppointments || emptyAppointments.length == 0);

        } finally {
            if (null != newId) {
                // Delete folder
                try {
                    final DeleteRequest deleteRequest = new DeleteRequest(EnumAPI.OUTLOOK, newId, new Date());
                    client.execute(deleteRequest);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
