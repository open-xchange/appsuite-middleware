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

package com.openexchange.ajax.folder;

import static com.openexchange.groupware.calendar.TimeTools.D;
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

    private FolderTestManager ftm;

    private CalendarTestManager ctm1;

    private CalendarTestManager ctm2;

    /**
     * Initializes a new {@link AppointmentObjectCountTest}.
     * 
     * @param name
     */
    public AppointmentObjectCountTest(String name) {
        super(name);
    }

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
        assertEquals("Wrong amount.", 0, getCount(client2, folder));
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

    @Override
    public void tearDown() throws Exception {
        ctm1.cleanUp();
        ctm2.cleanUp();
        ftm.cleanUp();
        super.tearDown();
    }

    /*
     * Generates a single Appointment, a series and an exception. (Total: 3)
     */
    private void insertAppointments(CalendarTestManager ctm, FolderObject folder) throws Exception {
        Appointment single = new Appointment();
        single.setTitle("Single Appoinment " + System.currentTimeMillis());
        single.setStartDate(D("01.05.2013 08:00"));
        single.setEndDate(D("01.05.2013 09:00"));
        single.setUsers(new UserParticipant[] {
            new UserParticipant(client1.getValues().getUserId()), new UserParticipant(client2.getValues().getUserId()) });
        single.setParticipants(new Participant[] {
            new UserParticipant(client1.getValues().getUserId()), new UserParticipant(client2.getValues().getUserId()) });
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
        series.setUsers(new UserParticipant[] {
            new UserParticipant(client1.getValues().getUserId()), new UserParticipant(client2.getValues().getUserId()) });
        series.setParticipants(new Participant[] {
            new UserParticipant(client1.getValues().getUserId()), new UserParticipant(client2.getValues().getUserId()) });
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
        FolderObject folder = ftm.generatePublicFolder(
            UUID.randomUUID().toString(),
            FolderObject.CALENDAR,
            FolderObject.SYSTEM_PUBLIC_FOLDER_ID,
            client1.getValues().getUserId());
        folder.addPermission(permission);

        return ftm.insertFolderOnServer(folder);
    }

    private FolderObject createShared(OCLPermission permission) throws Exception {
        FolderObject folder = ftm.generatePublicFolder(
            UUID.randomUUID().toString(),
            FolderObject.CALENDAR,
            client1.getValues().getPrivateAppointmentFolder(),
            client1.getValues().getUserId());
        folder.addPermission(permission);

        return ftm.insertFolderOnServer(folder);
    }

    private OCLPermission readOwnPermission() throws Exception {
        OCLPermission permission = new OCLPermission();
        permission.setEntity(client2.getValues().getUserId());
        permission.setGroupPermission(false);
        permission.setFolderAdmin(false);
        permission.setAllPermission(
            OCLPermission.CREATE_OBJECTS_IN_FOLDER,
            OCLPermission.READ_OWN_OBJECTS,
            OCLPermission.WRITE_ALL_OBJECTS,
            OCLPermission.DELETE_ALL_OBJECTS);

        return permission;
    }

    private OCLPermission readAllPermission() throws Exception {
        OCLPermission permission = new OCLPermission();
        permission.setEntity(client2.getValues().getUserId());
        permission.setGroupPermission(false);
        permission.setFolderAdmin(false);
        permission.setAllPermission(
            OCLPermission.CREATE_OBJECTS_IN_FOLDER,
            OCLPermission.READ_ALL_OBJECTS,
            OCLPermission.WRITE_ALL_OBJECTS,
            OCLPermission.DELETE_ALL_OBJECTS);

        return permission;
    }
}
