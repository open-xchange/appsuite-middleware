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

package com.openexchange.groupware.tasks;

import java.sql.Connection;
import java.util.Iterator;
import java.util.List;

import com.mysql.jdbc.AssertionFailedException;
import com.openexchange.api2.OXException;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.database.Database;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.calendar.update.UpdateFolderIdInReminder;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.ContextToolkit;
import com.openexchange.groupware.downgrade.DowngradeEvent;
import com.openexchange.groupware.downgrade.DowngradeFailedException;
import com.openexchange.groupware.folder.FolderToolkit;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserToolkit;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.sessiond.impl.SessionObjectWrapper;
import com.openexchange.tools.oxfolder.OXFolderException;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.oxfolder.OXFolderManagerImpl;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class DowngradeTest extends TestCase {

    private Context ctx;

    private User user;

    private User secondUser;

    private Session session;

    /**
     * @param name
     */
    public DowngradeTest(String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Init.startServer();
        AJAXConfig.init();

        ctx = ContextToolkit.getDefaultContext();
        final String userName = AJAXConfig.getProperty(AJAXConfig.Property.LOGIN);
        user = UserToolkit.getUser(userName, ctx);
        final String secondUserName = AJAXConfig.getProperty(AJAXConfig.Property
            .SECONDUSER);
        secondUser = UserToolkit.getUser(secondUserName, ctx);

        session = SessionObjectWrapper.createSessionObject(user.getId(), ctx,
            "DowngradeTest");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void tearDown() throws Exception {
        Init.stopServer();
        super.tearDown();
    }

    public void testRemovePrivateParticipants() throws OXException,
        DBPoolingException, DowngradeFailedException,
        TaskException {
        final int folderId = FolderToolkit.getStandardTaskFolder(user.getId(),
            ctx);
        final Task task = Create.createWithDefaults(folderId, "DowngradeTest");
        task.setParticipants(new Participant[] {
            new UserParticipant(user.getId()),
            new UserParticipant(secondUser.getId())
        });
        final TasksSQLInterface taskSQL = new TasksSQLInterfaceImpl(session);
        taskSQL.insertTaskObject(task);
        try {
            downgradeDelegate();
            assertNoParticipants(folderId, task.getObjectID());
        } finally {
            TaskLogic.deleteTask(session, ctx, user.getId(), task, task
                .getLastModified());
        }
    }

    public void testRemovePublicParticipants() throws DBPoolingException,
        OXException, DowngradeFailedException, TaskException {
        final FolderObject folder = createPublicFolder();
        final int folderId = folder.getObjectID();
        final Task task = Create.createWithDefaults(folderId, "DowngradeTest");
        task.setParticipants(new Participant[] {
            new UserParticipant(user.getId()),
            new UserParticipant(secondUser.getId())
        });
        final TasksSQLInterface taskSQL = new TasksSQLInterfaceImpl(session);
        taskSQL.insertTaskObject(task);
        try {
            downgradeDelegate();
            assertParticipants(folderId, task.getObjectID());
            updatePublicFolder(folder);
            downgradeDelegate();
            assertNoParticipants(folderId, task.getObjectID());
        } finally {
            TaskLogic.deleteTask(session, ctx, user.getId(), task, task
                .getLastModified());
            deletePublicFolder(folder);
        }
    }

    /* ----------------- Test help methods ---------------------*/
    
    private void downgradeDelegate() throws DBPoolingException,
        DowngradeFailedException {
        final UserConfiguration userConfig = new UserConfiguration(
            Integer.MAX_VALUE ^ (1 << 17), user.getId(), user.getGroups(), ctx);
        final Connection con = Database.get(ctx, true);
        try {
            final DowngradeEvent event = new DowngradeEvent(userConfig, con,
                ctx);
            new TasksDowngrade().downgradePerformed(event);
        } finally {
            Database.back(ctx, true, con);
        }
    }

    private void assertNoParticipants(final int folderId, final int objectId) {
        try {
            final Task task = GetTask.load(ctx, folderId, objectId, StorageType
                .ACTIVE);
            final Participant[] parts = task.getParticipants();
            if (null != parts && parts.length > 0) {
                throw new AssertionFailedError("Task has participants.");
            }
        } catch (TaskException e) {
            throw new AssertionFailedException(e);
        }
    }

    private void assertParticipants(final int folderId, final int objectId) {
        try {
            final Task task = GetTask.load(ctx, folderId, objectId, StorageType
                .ACTIVE);
            final Participant[] parts = task.getParticipants();
            if (null == parts || parts.length == 0) {
                throw new AssertionFailedError("Task has no participants.");
            }
        } catch (TaskException e) {
            throw new AssertionFailedException(e);
        }
    }

    /* ---------------------- Special tests help methods -----------------*/

    private FolderObject createPublicFolder() throws DBPoolingException, OXException {
        final Connection con = DBPool.pickupWriteable(ctx);
        try {
            final OXFolderManager oxma = new OXFolderManagerImpl(session, con, con);
            final OCLPermission oclp1 = new OCLPermission();
            oclp1.setEntity(user.getId());
            oclp1.setAllPermission(
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION);
            oclp1.setFolderAdmin(true);
            final OCLPermission oclp2 = new OCLPermission();
            oclp2.setEntity(secondUser.getId());
            oclp2.setAllPermission(
                OCLPermission.CREATE_OBJECTS_IN_FOLDER,
                OCLPermission.READ_ALL_OBJECTS,
                OCLPermission.WRITE_ALL_OBJECTS,
                OCLPermission.DELETE_ALL_OBJECTS);
            oclp2.setFolderAdmin(false);
            FolderObject fo = new FolderObject();
            fo.setFolderName("DowngradeTest");
            fo.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
            fo.setModule(FolderObject.TASK);
            fo.setType(FolderObject.PUBLIC);
            fo.setPermissionsAsArray(new OCLPermission[] { oclp1, oclp2 });
            fo = oxma.createFolder(fo, true, System.currentTimeMillis());
            return fo;
        } finally {
            DBPool.pushWrite(ctx, con);
        }
    }

    private void updatePublicFolder(final FolderObject folder)
        throws DBPoolingException, OXException {
        final Connection con = DBPool.pickupWriteable(ctx);
        try {
            final OXFolderManager oxma = new OXFolderManagerImpl(session, con, con);
            final Iterator<OCLPermission> iter = folder.getPermissions().iterator();
            while (iter.hasNext()) {
                if (iter.next().getEntity() == secondUser.getId()) {
                    iter.remove();
                }
            }
            oxma.updateFolder(folder, false, System.currentTimeMillis());
        } finally {
            DBPool.pushWrite(ctx, con);
        }
    }

    private void deletePublicFolder(final FolderObject folder)
        throws DBPoolingException, OXException {
        final Connection con = DBPool.pickupWriteable(ctx);
        try {
            final OXFolderManager oxma = new OXFolderManagerImpl(session, con, con);
            oxma.deleteFolder(folder, false, System.currentTimeMillis());
        } finally {
            DBPool.pushWrite(ctx, con);
        }
    }
}
