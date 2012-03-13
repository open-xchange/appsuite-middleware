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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.groupware.notify;

import com.openexchange.exception.OXException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javax.mail.internet.MimeMultipart;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.tools.CommonAppointments;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.setuptools.TestConfig;
import com.openexchange.setuptools.TestContextToolkit;
import com.openexchange.setuptools.TestFolderToolkit;
import com.openexchange.tools.oxfolder.OXFolderManager;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Bug13184Test extends ParticipantNotifyTest {

    private TestFolderToolkit folders;

    private Context ctx;

    private String user, secondUser;

    private int userId, secondUserId;

    private FolderObject folder;

    private CommonAppointments appointments;

    private CalendarDataObject appointment;

    private String userMail, secondUserMail;

    private Session so;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        folders = new TestFolderToolkit();
        final TestContextToolkit contextTools = new TestContextToolkit();
        ctx = contextTools.getDefaultContext();
        final TestConfig config = new TestConfig();
        user = config.getUser();
        secondUser = config.getSecondUser();
        userId = contextTools.resolveUser(user, ctx);
        secondUserId = contextTools.resolveUser(secondUser, ctx);
        userMail = contextTools.loadUser(userId, ctx).getMail();
        secondUserMail = contextTools.loadUser(secondUserId, ctx).getMail();

        so = contextTools.getSessionForUser(user, ctx);
        folder = createPublicFolderFor();

        appointments = new CommonAppointments(ctx, user);
        appointment = appointments.buildAppointmentWithUserParticipants(user, secondUser);
        appointment.setParentFolderID(folder.getObjectID());

        notify.realUsers = true;
    }

    @Override
    public void tearDown() throws Exception {
        folders.removeAll(so, new ArrayList<FolderObject>(){{add(folder);}});

        super.tearDown();
    }

    public void testBug13185() throws Exception {
        notify.appointmentCreated(appointment, so);
        final List<Message> messages = notify.getMessages();
        assertEquals("Wrong amount of notification messages.", 1, messages.size());
        final Message message = messages.get(0);
        assertTrue("Wrong recipient.", message.addresses.contains(secondUserMail));

        String msg = "";
        if (MimeMultipart.class.isInstance(message.message)) {
            MimeMultipart mpart = (MimeMultipart) message.message;
            for (int i = 0; i < mpart.getCount(); i++) {
                if (mpart.getBodyPart(i).getContentType().startsWith("text/plain")) {
                    msg = (String) mpart.getBodyPart(i).getContent();
                    break;
                }
            }
        } else {
            msg = (String) message.message;
        }
        assertFalse("Message should not contain a link to the apointment.", msg.contains("http://")); //TODO: Make more sophisticated.
    }

    private FolderObject createPublicFolderFor() {
        Connection writecon = null;
        try {
            writecon = DBPool.pickupWriteable(ctx);
            final OXFolderManager oxma = OXFolderManager.getInstance(so, writecon, writecon);

            final ArrayList<OCLPermission> permissions = new ArrayList<OCLPermission>();
            OCLPermission oclp = new OCLPermission();
            oclp.setEntity(userId);
            oclp.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
            oclp.setFolderAdmin(true);
            permissions.add(oclp);
            oclp = new OCLPermission();
            oclp.setEntity(secondUserId);
            oclp.setAllPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_OWN_OBJECTS, OCLPermission.READ_OWN_OBJECTS, OCLPermission.NO_PERMISSIONS);
            oclp.setFolderAdmin(true);
            permissions.add(oclp);

            FolderObject fo = new FolderObject();
            fo.setFolderName("Bug13184Test");
            fo.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
            fo.setModule(FolderObject.CALENDAR);
            fo.setType(FolderObject.PUBLIC);
            fo.setPermissions(permissions);
            fo = oxma.createFolder(fo, true, System.currentTimeMillis());
            return fo;
        } catch (final OXException e) {
            e.printStackTrace();
            return null;
        } finally {
            if(writecon != null) {
                DBPool.pushWrite(ctx, writecon);
            }
        }
    }
}
