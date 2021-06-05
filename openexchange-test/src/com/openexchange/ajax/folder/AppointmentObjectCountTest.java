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

package com.openexchange.ajax.folder;

import static com.openexchange.test.common.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertEquals;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.FolderTestManager;

/**
 * {@link AppointmentObjectCountTest}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class AppointmentObjectCountTest extends AbstractObjectCountTest {

    @SuppressWarnings("hiding")
    private FolderTestManager ftm;

    private CalendarTestManager ctm1;

    private CalendarTestManager ctm2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ftm = new FolderTestManager(client1);
        ctm1 = new CalendarTestManager(client1);
        ctm2 = new CalendarTestManager(client2);
    }

    @Test
    public void testPrivate() throws Exception {
        int initialValue = getCount(client2);
        FolderObject folder = createPrivateFolder(client1, ftm, FolderObject.CALENDAR);
        assertEquals("Wrong amount.", 0, getCount(client1, folder));
        assertEquals("Wrong amount.", initialValue, getCount(client2));
        insertAppointments(ctm1, folder);
        assertEquals("Wrong amount.", 3, getCount(client1, folder));
        assertEquals("Wrong amount.", initialValue + 3, getCount(client2));
    }

    @Test
    public void testPublicReadOwn() throws Exception {
        FolderObject folder = createPublic(readOwnPermission());
        assertEquals("Wrong amount.", 0, getCount(client1, folder));
        assertEquals("Wrong amount.", 0, getCount(client2, folder));
        insertAppointments(ctm1, folder);
        assertEquals("Wrong amount.", 3, getCount(client1, folder));
        assertEquals("Wrong amount.", 0, getCount(client2, folder));
        insertAppointments(ctm2, folder);
        assertEquals("Wrong amount.", 6, getCount(client1, folder));
        assertEquals("Wrong amount.", 3, getCount(client2, folder));
    }

    @Test
    public void testPublicReadAll() throws Exception {
        FolderObject folder = createPublic(readAllPermission());
        assertEquals("Wrong amount.", 0, getCount(client1, folder));
        assertEquals("Wrong amount.", 0, getCount(client2, folder));
        insertAppointments(ctm1, folder);
        assertEquals("Wrong amount.", 3, getCount(client1, folder));
        assertEquals("Wrong amount.", 3, getCount(client2, folder));
        insertAppointments(ctm2, folder);
        assertEquals("Wrong amount.", 6, getCount(client1, folder));
        assertEquals("Wrong amount.", 6, getCount(client2, folder));

    }

    @Test
    public void testSharedReadOwn() throws Exception {
        FolderObject folder = createShared(readOwnPermission());
        assertEquals("Wrong amount.", 0, getCount(client1, folder));
        assertEquals("Wrong amount.", 0, getCount(client2, folder));
        insertAppointments(ctm1, folder);
        assertEquals("Wrong amount.", 3, getCount(client1, folder));
        assertEquals("Wrong amount.", 0, getCount(client2, folder));
        insertAppointments(ctm2, folder);
        assertEquals("Wrong amount.", 6, getCount(client1, folder));
        assertEquals("Wrong amount.", 3, getCount(client2, folder));
    }

    @Test
    public void testSharedReadAll() throws Exception {
        FolderObject folder = createShared(readAllPermission());
        assertEquals("Wrong amount.", 0, getCount(client1, folder));
        assertEquals("Wrong amount.", 0, getCount(client2, folder));
        insertAppointments(ctm1, folder);
        assertEquals("Wrong amount.", 3, getCount(client1, folder));
        assertEquals("Wrong amount.", 3, getCount(client2, folder));
        insertAppointments(ctm2, folder);
        assertEquals("Wrong amount.", 6, getCount(client1, folder));
        assertEquals("Wrong amount.", 6, getCount(client2, folder));
    }

    /*
     * Generates a single Appointment, a series and an exception. (Total: 3)
     */
    private void insertAppointments(CalendarTestManager ctm, FolderObject folder) throws Exception {
        Appointment single = new Appointment();
        single.setTitle("Single Appoinment " + System.currentTimeMillis());
        single.setStartDate(D("01.05.2013 08:00"));
        single.setEndDate(D("01.05.2013 09:00"));
        single.setUsers(new UserParticipant[] { new UserParticipant(client1.getValues().getUserId()), new UserParticipant(client2.getValues().getUserId()) });
        single.setParticipants(new Participant[] { new UserParticipant(client1.getValues().getUserId()), new UserParticipant(client2.getValues().getUserId()) });
        single.setIgnoreConflicts(true);
        single.setParentFolderID(folder.getObjectID());
        ctm.insert(single);

        Appointment series = new Appointment();
        series.setTitle("Single Appoinment " + System.currentTimeMillis());
        series.setStartDate(D("01.05.2013 08:00"));
        series.setEndDate(D("01.05.2013 09:00"));
        series.setRecurrenceType(Appointment.DAILY);
        series.setInterval(1);
        series.setOccurrence(5);
        series.setUsers(new UserParticipant[] { new UserParticipant(client1.getValues().getUserId()), new UserParticipant(client2.getValues().getUserId()) });
        series.setParticipants(new Participant[] { new UserParticipant(client1.getValues().getUserId()), new UserParticipant(client2.getValues().getUserId()) });
        series.setIgnoreConflicts(true);
        series.setParentFolderID(folder.getObjectID());
        ctm.insert(series);

        Appointment exception = ctm.createIdentifyingCopy(series);
        exception.setIgnoreConflicts(true);
        exception.setRecurrencePosition(3);
        exception.setStartDate(D("01.05.2013 08:30"));
        exception.setEndDate(D("01.05.2013 09:30"));
        ctm.update(exception);
    }

    private int getCount(AJAXClient client) throws Exception {
        return getFolder(client, client.getValues().getPrivateAppointmentFolder(), DEFAULT_COLUMNS).getTotal();
    }

    private int getCount(AJAXClient client, FolderObject folder) throws Exception {
        return getFolder(client, folder.getObjectID(), DEFAULT_COLUMNS).getTotal();
    }

    private FolderObject createPublic(OCLPermission permission) throws Exception {
        FolderObject folder = ftm.generatePublicFolder(UUID.randomUUID().toString(), FolderObject.CALENDAR, FolderObject.SYSTEM_PUBLIC_FOLDER_ID, client1.getValues().getUserId());
        folder.addPermission(permission);

        return ftm.insertFolderOnServer(folder);
    }

    private FolderObject createShared(OCLPermission permission) throws Exception {
        FolderObject folder = ftm.generatePublicFolder(UUID.randomUUID().toString(), FolderObject.CALENDAR, client1.getValues().getPrivateAppointmentFolder(), client1.getValues().getUserId());
        folder.addPermission(permission);

        return ftm.insertFolderOnServer(folder);
    }

    private OCLPermission readOwnPermission() throws Exception {
        OCLPermission permission = new OCLPermission();
        permission.setEntity(client2.getValues().getUserId());
        permission.setGroupPermission(false);
        permission.setFolderAdmin(false);
        permission.setAllPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_OWN_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);

        return permission;
    }

    private OCLPermission readAllPermission() throws Exception {
        OCLPermission permission = new OCLPermission();
        permission.setEntity(client2.getValues().getUserId());
        permission.setGroupPermission(false);
        permission.setFolderAdmin(false);
        permission.setAllPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);

        return permission;
    }
}
