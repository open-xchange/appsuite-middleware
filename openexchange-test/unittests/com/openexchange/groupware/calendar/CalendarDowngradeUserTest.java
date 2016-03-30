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
package com.openexchange.groupware.calendar;

import com.openexchange.exception.OXException;
import static com.openexchange.tools.events.EventAssertions.assertDeleteEvent;
import static com.openexchange.tools.events.EventAssertions.assertModificationEvent;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import com.openexchange.calendar.CalendarAdministration;
import com.openexchange.calendar.CalendarSql;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.Participants;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.downgrade.DowngradeEvent;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.sessiond.impl.SessionObjectWrapper;
import com.openexchange.setuptools.TestFolderToolkit;
import com.openexchange.setuptools.TestContextToolkit;
import com.openexchange.tools.events.TestEventAdmin;
import com.openexchange.tools.oxfolder.OXFolderManager;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */



public class CalendarDowngradeUserTest extends TestCase {

    private int user;
    private int other_user;
    private Context ctx;
    private Session session;

    private int publicFolderId = -1;

    private static TestContextToolkit tools = new TestContextToolkit();
    private static TestFolderToolkit folders = new TestFolderToolkit();

    @Override
	public void setUp() throws Exception {
        Init.startServer();
        AJAXConfig.init();

        TestEventAdmin.getInstance().clearEvents();

        ctx = tools.getDefaultContext();
        user = tools.resolveUser(AJAXConfig.getProperty(AJAXConfig.Property.LOGIN), ctx);
        other_user = tools.resolveUser(AJAXConfig.getProperty(AJAXConfig.Property.SECONDUSER), ctx);

        session = SessionObjectWrapper.createSessionObject(user,ctx,"calendarDeleteUserDataTest");

    }

    @Override
	public void tearDown() throws Exception {
        deleteAll();
        Init.stopServer();
    }

    private void runDelete(final int user) {
        final UserConfiguration config = new UserConfiguration(new HashSet<String>(), user, tools.getGroups(user, ctx) , ctx);
        Connection con = null;
        try {
            con = DBPool.pickupWriteable(ctx);
            final DowngradeEvent event = new DowngradeEvent(config, con, ctx);
            new CalendarAdministration().downgradePerformed(event);
        } catch (final OXException x) {
            x.printStackTrace();
            fail(x.getMessage());
        } finally {
            if(con != null) {
                DBPool.pushWrite(ctx, con);
            }
        }
    }

    public void testRemovePrivate() throws OXException {
        final CalendarDataObject cdao = createPrivateAppointment(user);
        runDelete(user);

        assertAppointmentNotFound(cdao.getParentFolderID(), cdao.getObjectID());
        assertDeleteEvent(Appointment.class, cdao.getParentFolderID(), cdao.getObjectID());
    }

    public void testRemoveFromParticipants() throws OXException{
        final CalendarDataObject cdao = createPublicAppointmentWithSomeoneElse(user, other_user);
        runDelete(user);

        assertNotInUserParticipants(cdao.getParentFolderID(), cdao.getObjectID(), user);
        assertModificationEvent(Appointment.class, cdao.getParentFolderID(), cdao.getObjectID());
    }

    public void testRemoveAppointmentWhenOnlyParticipant() throws OXException {
        final CalendarDataObject cdao = createPublicAppointmentWithOnlyOneParticipant(user);
        runDelete(user);

        assertAppointmentNotFound(cdao.getParentFolderID(), cdao.getObjectID());
        assertDeleteEvent(Appointment.class, cdao.getParentFolderID(), cdao.getObjectID());

    }


    // Helper Methods

    private final List<CalendarDataObject> clean = new LinkedList<CalendarDataObject>();
    private final List<FolderObject> cleanFolders = new LinkedList<FolderObject>();

    private void deleteAll() {
        final CalendarSql calendars = new CalendarSql(session);

        for(final CalendarDataObject cdao : clean) {
            try {
                calendars.deleteAppointmentObject(cdao, cdao.getParentFolderID(), new Date(Long.MAX_VALUE));
            } catch (final OXException e) {
                // IGNORE
            }
        }


        Connection writecon = null;
        try {
            writecon = DBPool.pickupWriteable(ctx);
            final OXFolderManager oxma = OXFolderManager.getInstance(session, calendars, writecon, writecon);
            for(final FolderObject folder : cleanFolders) {
                oxma.deleteFolder(folder,false, System.currentTimeMillis());
            }
        } catch (final OXException e) {
            // IGNORE
        } finally {
            if(writecon != null) {
                DBPool.pushWrite(ctx, writecon);
            }
        }



    }

    private CalendarDataObject newAppointment(final String title, final int folder, final Context ctx) {
        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTitle(title);
        cdao.setParentFolderID(folder);
        cdao.setIgnoreConflicts(true);
        cdao.setStartDate(new Date(0));
        cdao.setEndDate(new Date(60*60*1000));
        cdao.setContext(ctx);
        return cdao;
    }

    private CalendarDataObject createPrivateAppointment(final int userId) throws OXException {
        final CalendarDataObject cdao = newAppointment("Private appointment", folders.getStandardFolder(userId, ctx), ctx);
        final CalendarSql csql = new CalendarSql(session);
        csql.insertAppointmentObject(cdao);

        clean.add(cdao);

        return cdao;
    }

    private CalendarDataObject createPublicAppointmentWithSomeoneElse(final int user, final int other_user) throws OXException {
        final int publicFolder = createPublicFolder();

        final CalendarDataObject cdao = newAppointment("Public appointment  with two participants", publicFolder, ctx);
        setParticipants(cdao, user, other_user);

        final CalendarSql csql = new CalendarSql(session);
        csql.insertAppointmentObject(cdao);

        clean.add(cdao);

        return cdao;
    }

    private CalendarDataObject createPublicAppointmentWithOnlyOneParticipant(final int user) throws OXException {

        final int publicFolder = createPublicFolder();

        final CalendarDataObject cdao = newAppointment("Public appointment with only one participant", publicFolder, ctx);
        setParticipants(cdao, user);

        final CalendarSql csql = new CalendarSql(session);
        csql.insertAppointmentObject(cdao);

        clean.add(cdao);

        return cdao;
    }

    private int createPublicFolder() throws OXException {
        if(publicFolderId != -1) {
            return publicFolderId;
        }
        Connection writecon = null;
        try {
            writecon = DBPool.pickupWriteable(ctx);
            final OXFolderManager oxma = OXFolderManager.getInstance(session, writecon, writecon);
            final OCLPermission oclp = new OCLPermission();
            oclp.setEntity(user);
            oclp.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
            oclp.setFolderAdmin(true);
            FolderObject fo = new FolderObject();
            fo.setFolderName("Public Folder "+System.currentTimeMillis());
            fo.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
            fo.setModule(FolderObject.CALENDAR);
            fo.setType(FolderObject.PUBLIC);
            fo.setPermissionsAsArray(new OCLPermission[] { oclp });
            fo = oxma.createFolder(fo, true, System.currentTimeMillis());
            cleanFolders.add( fo );
            return publicFolderId = fo.getObjectID();
        } catch (final OXException e) {
            e.printStackTrace();
            return -1;
        } finally {
            if(writecon != null) {
                DBPool.pushWrite(ctx, writecon);
            }
        }
    }

    private void setParticipants(final CalendarDataObject cdao, final int...users) {
        final Participants participants = new Participants();

        for(final int uid : users) {
            participants.add(new UserParticipant(uid));
        }

        cdao.setParticipants(participants.getList());
    }

    private void assertAppointmentNotFound(final int parentFolderID, final int objectID) {
        final CalendarSql csql = new CalendarSql(session);
        try {
            csql.getObjectById(objectID, parentFolderID);
            fail("The appointment exists!");
        } catch (final OXException x) {
            if (x.getCode() != 1) {
                x.printStackTrace();
                fail(x.toString());
            }
        } catch (final SQLException x) {
            x.printStackTrace();
            fail(x.toString());
        }
    }

    private void assertNotInUserParticipants(final int parentFolderID, final int objectID, final int user) {
        final CalendarSql csql = new CalendarSql(session);
        try {
            final CalendarDataObject cdao = csql.getObjectById(objectID, parentFolderID);
            for(final Participant participant : cdao.getParticipants()) {
                if(participant.getType() == Participant.USER && user == participant.getIdentifier()) {
                    fail("Participants should not contain user "+user);
                }
            }
        } catch (final OXException x) {
            x.printStackTrace();
            fail(x.toString());
        } catch (final SQLException x) {
            x.printStackTrace();
            fail(x.toString());
        }
    }


}
