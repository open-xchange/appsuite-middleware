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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import junit.framework.TestCase;
import com.openexchange.tools.events.TestEventAdmin;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.oxfolder.OXFolderManagerImpl;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.container.Participants;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.downgrade.DowngradeEvent;
import com.openexchange.groupware.downgrade.DowngradeFailedException;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.session.Session;
import com.openexchange.sessiond.impl.SessionObjectWrapper;
import com.openexchange.api2.OXException;
import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.server.impl.OCLPermission;

import java.util.List;
import java.util.LinkedList;
import java.util.Date;
import java.sql.SQLException;
import java.sql.Connection;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */



public class CalendarDowngradeUserTest extends TestCase {

    private int user;
    private int other_user;
    private Context ctx;
    private Session session;

    private int publicFolderId = -1;

    private static CalendarContextToolkit tools = new CalendarContextToolkit();
    private static CalendarFolderToolkit folders = new CalendarFolderToolkit();

    public void setUp() throws Exception {
        Init.startServer();
        AJAXConfig.init();
        
        TestEventAdmin.getInstance().clearEvents();

        ctx = tools.getDefaultContext();
        user = tools.resolveUser(AJAXConfig.getProperty(AJAXConfig.Property.LOGIN), ctx);
        other_user = tools.resolveUser(AJAXConfig.getProperty(AJAXConfig.Property.SECONDUSER), ctx);

        session = SessionObjectWrapper.createSessionObject(user,ctx,"calendarDeleteUserDataTest");

    }

    public void tearDown() {
        deleteAll();
        Init.stopServer();
    }

    private void runDelete(int user) {
        UserConfiguration config = new UserConfiguration(0, user, tools.getGroups(user, ctx) , ctx);
        Connection con = null;
        try {
            con = DBPool.pickupWriteable(ctx);
            DowngradeEvent event = new DowngradeEvent(config, con, ctx);
            new CalendarAdministration().downgradePerformed(event);
        } catch (DBPoolingException x) {
            x.printStackTrace();
            fail(x.getMessage());
        } catch (DowngradeFailedException x) {
            x.printStackTrace();
            fail(x.getMessage());
        } finally {
            if(con != null) {
                try {
                    DBPool.pushWrite(ctx, con);
                } catch (DBPoolingException e) {
                    //IGNORE
                    e.printStackTrace();
                }
            }
        }
    }

    public void testRemovePrivate() throws OXException {
        CalendarDataObject cdao = createPrivateAppointment(user);
        runDelete(user);

        assertAppointmentNotFound(cdao.getParentFolderID(), cdao.getObjectID());
        assertDeleteEvent(cdao.getParentFolderID(), cdao.getObjectID());
    }

    public void testRemoveFromParticipants() throws OXException{
        CalendarDataObject cdao = createPublicAppointmentWithSomeoneElse(user, other_user);
        runDelete(user);

        assertNotInUserParticipants(cdao.getParentFolderID(), cdao.getObjectID(), user);
        assertModificationEvent(cdao.getParentFolderID(), cdao.getObjectID());
    }

    public void testRemoveAppointmentWhenOnlyParticipant() throws OXException {
        CalendarDataObject cdao = createPublicAppointmentWithOnlyOneParticipant(user);
        runDelete(user);

        assertAppointmentNotFound(cdao.getParentFolderID(), cdao.getObjectID());
        assertDeleteEvent(cdao.getParentFolderID(), cdao.getObjectID());
        
    }


    // Helper Methods

    private List<CalendarDataObject> clean = new LinkedList<CalendarDataObject>();
    private List<FolderObject> cleanFolders = new LinkedList<FolderObject>();

    private void deleteAll() {
        // TODO
    }

    private CalendarDataObject createPrivateAppointment(int userId) throws OXException {
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTitle("Private appointment");
        cdao.setParentFolderID(folders.getStandardFolder(userId, ctx));
        cdao.setIgnoreConflicts(true);
        cdao.setStartDate(new Date(0));
        cdao.setEndDate(new Date(60*60*1000));
        cdao.setContext(ctx);

        CalendarSql csql = new CalendarSql(session);        
        csql.insertAppointmentObject(cdao);

        clean.add(cdao);
        
        return cdao;
    }

    private CalendarDataObject createPublicAppointmentWithSomeoneElse(int user, int other_user) throws OXException {
        int publicFolder = createPublicFolder();

        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTitle("Public appointment with two participants");
        cdao.setParentFolderID(publicFolder);
        cdao.setIgnoreConflicts(true);
        cdao.setStartDate(new Date(0));
        cdao.setEndDate(new Date(60*60*1000));
        cdao.setContext(ctx);
        setParticipants(cdao, user, other_user);

        CalendarSql csql = new CalendarSql(session);
        csql.insertAppointmentObject(cdao);

        clean.add(cdao);

        return cdao;
    }

    private CalendarDataObject createPublicAppointmentWithOnlyOneParticipant(int user) throws OXException {

        int publicFolder = createPublicFolder();

        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTitle("Public appointment with only one participant");
        cdao.setParentFolderID(publicFolder);
        cdao.setIgnoreConflicts(true);
        cdao.setStartDate(new Date(0));
        cdao.setEndDate(new Date(60*60*1000));
        cdao.setContext(ctx);
        setParticipants(cdao, user);

        CalendarSql csql = new CalendarSql(session);
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
            final OXFolderManager oxma = new OXFolderManagerImpl(session, writecon, writecon);
            OCLPermission oclp = new OCLPermission();
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
        } catch (DBPoolingException e) {
            e.printStackTrace();
            return -1;
        } finally {
            if(writecon != null) {
                try {
                    DBPool.pushWrite(ctx, writecon);
                } catch (DBPoolingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setParticipants(CalendarDataObject cdao, int...users) {
        Participants participants = new Participants();

        for(int uid : users) {
            participants.add(new UserParticipant(uid));            
        }

        cdao.setParticipants(participants.getList());
    }

    private void assertAppointmentNotFound(int parentFolderID, int objectID) {
        CalendarSql csql = new CalendarSql(session);
        try {
            csql.getObjectById(objectID, parentFolderID);
            fail("The appointment exists!");
        } catch(OXObjectNotFoundException x) {

        } catch (OXException x) {
            x.printStackTrace();
            fail(x.toString());
        } catch (SQLException x) {
            x.printStackTrace();
            fail(x.toString());
        }
    }

    private void assertDeleteEvent(int parentFolderID, int objectID) {
        //To change body of created methods use File | Settings | File Templates.
    }
    
    private void assertNotInUserParticipants(int parentFolderID, int objectID, int user) {
        CalendarSql csql = new CalendarSql(session);
        try {
            CalendarDataObject cdao = csql.getObjectById(objectID, parentFolderID);
            for(Participant participant : cdao.getParticipants()) {
                if(participant.getType() == Participant.USER && user == participant.getIdentifier()) {
                    fail("Participants should not contain user "+user);
                }
            }
        } catch (OXException x) {
            x.printStackTrace();
            fail(x.toString());
        } catch (SQLException x) {
            x.printStackTrace();
            fail(x.toString());
        }
    }
    
    private void assertModificationEvent(int parentFolderID, int objectID) {
        //To change body of created methods use File | Settings | File Templates.
    }
}
