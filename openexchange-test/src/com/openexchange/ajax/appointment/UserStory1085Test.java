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

package com.openexchange.ajax.appointment;

import static com.openexchange.ajax.folder.Create.ocl;
import static com.openexchange.groupware.calendar.TimeTools.D;
import java.util.Date;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.server.impl.OCLPermission;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class UserStory1085Test extends AppointmentTest {

    private AJAXClient clientA, clientB, clientC;

    private int userIdA, userIdB, userIdC;

    private FolderObject folder;

    private Appointment appointmenShare, appointmentNormal, appointmentPrivate;

    private Date start, end;

    public UserStory1085Test(final String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        clientA = getClient();
        clientB = new AJAXClient(User.User2);
        clientC = new AJAXClient(User.User3);
        userIdA = clientA.getValues().getUserId();
        userIdB = clientB.getValues().getUserId();
        userIdC = clientC.getValues().getUserId();

        folder = Create.folder(
            FolderObject.SYSTEM_PRIVATE_FOLDER_ID,
            "UserStory1085Test - " + Long.toString(System.currentTimeMillis()),
            FolderObject.CALENDAR,
            FolderObject.PRIVATE,
            ocl(userIdB, false, true,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION),
            ocl(userIdA, false, false,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION));

        final CommonInsertResponse response = clientB.execute(new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_OLD, folder));
        response.fillObject(folder);

        appointmenShare = new Appointment();
        appointmenShare.setTitle("Full");
        appointmenShare.setStartDate(D("01.02.2009 12:00"));
        appointmenShare.setEndDate(D("01.02.2009 14:00"));
        appointmenShare.setParentFolderID(folder.getObjectID());
        appointmenShare.setIgnoreConflicts(true);
        CommonInsertResponse insertResponse = clientB.execute(new InsertRequest(appointmenShare, clientB.getValues().getTimeZone()));
        insertResponse.fillObject(appointmenShare);

        appointmentPrivate = new Appointment();
        appointmentPrivate.setTitle("Title of private flagged appointment");
        appointmentPrivate.setStartDate(D("01.02.2009 12:00"));
        appointmentPrivate.setEndDate(D("01.02.2009 14:00"));
        appointmentPrivate.setPrivateFlag(true);
        appointmentPrivate.setIgnoreConflicts(true);
        appointmentPrivate.setParentFolderID(clientC.getValues().getPrivateAppointmentFolder());
        insertResponse = clientC.execute(new InsertRequest(appointmentPrivate, clientC.getValues().getTimeZone()));
        insertResponse.fillObject(appointmentPrivate);

        appointmentNormal = new Appointment();
        appointmentNormal.setTitle("Title of appointment in not shared folder");
        appointmentNormal.setStartDate(D("01.02.2009 12:00"));
        appointmentNormal.setEndDate(D("01.02.2009 14:00"));
        appointmentNormal.setIgnoreConflicts(true);
        appointmentNormal.setParentFolderID(clientC.getValues().getPrivateAppointmentFolder());
        insertResponse = clientC.execute(new InsertRequest(appointmentNormal, clientC.getValues().getTimeZone()));
        insertResponse.fillObject(appointmentNormal);

        start = D("01.02.2009 00:00");
        end = D("02.02.2009 00:00");
    }

    @Override
    public void tearDown() throws Exception {
        clientB.execute(new DeleteRequest(appointmenShare));
        clientC.execute(new DeleteRequest(appointmentPrivate));
        clientC.execute(new DeleteRequest(appointmentNormal));
        clientB.execute(new com.openexchange.ajax.folder.actions.DeleteRequest(EnumAPI.OX_OLD, folder.getObjectID(), folder.getLastModified()));

        super.tearDown();
    }

    public void testUserStory1085() throws Exception {
        final Appointment[] appointmentsB = getFreeBusy(getWebConversation(), userIdB, Participant.USER, start, end, clientB.getValues().getTimeZone(), getHostName(), getSessionId());
        final Appointment[] appointmentsC = getFreeBusy(getWebConversation(), userIdC, Participant.USER, start, end, clientB.getValues().getTimeZone(), getHostName(), getSessionId());

        boolean foundShare = false;
        boolean foundPrivate = false;
        boolean foundNormal = false;

        for (final Appointment app : appointmentsB) {
            if (app.getObjectID() == appointmenShare.getObjectID()) {
                foundShare = true;
                validateShare(app);
            }
        }

        for (final Appointment app : appointmentsC) {
            if (app.getObjectID() == appointmentNormal.getObjectID()) {
                foundNormal = true;
                validateNormal(app);
            } else if (app.getObjectID() == appointmentPrivate.getObjectID()) {
                foundPrivate = true;
                validatePrivate(app);
            }
        }

        assertTrue("Missing appointment", foundShare);
        assertTrue("Missing appointment", foundPrivate);
        assertTrue("Missing appointment", foundNormal);
    }

    private void validatePrivate(final Appointment app) {
        assertFalse("No title for private flagged appointment expected but found: " + app.getTitle(), appointmentPrivate.getTitle().equals(app.getTitle()));
        assertFalse("No folderId expected", app.containsParentFolderID());
    }

    private void validateNormal(final Appointment app) {
        assertFalse("No title for appointment in not shared folder expected but found: " + app.getTitle(), appointmentNormal.getTitle().equals(app.getTitle()));
        assertFalse("No folderId expected", app.containsParentFolderID());
    }

    private void validateShare(final Appointment app) {
        assertEquals("Missing or wrong folderId in Appointment", folder.getObjectID(), app.getParentFolderID());
    }

}
