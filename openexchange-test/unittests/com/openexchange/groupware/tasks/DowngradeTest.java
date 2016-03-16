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

package com.openexchange.groupware.tasks;

import java.sql.Connection;
import java.util.Iterator;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import com.mysql.jdbc.AssertionFailedException;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.downgrade.DowngradeEvent;
import com.openexchange.groupware.folder.FolderToolkit;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserToolkit;
import com.openexchange.groupware.userconfiguration.AllowAllUserConfiguration;
import com.openexchange.groupware.userconfiguration.AllowAllUserPermissionBits;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.sessiond.impl.SessionObjectWrapper;
import com.openexchange.setuptools.TestConfig;
import com.openexchange.setuptools.TestContextToolkit;
import com.openexchange.tools.oxfolder.OXFolderManager;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class DowngradeTest extends TestCase {

    private Context ctx;
    private User user;
    private User secondUser;
    private Session session;

    public DowngradeTest(final String name) {
        super(name);
    }

    private static String getUsername(final String un) {
        final int pos = un.indexOf('@');
        return pos == -1 ? un : un.substring(0, pos);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Init.startServer();
        AJAXConfig.init();

        final TestConfig config = new TestConfig();
        final String userName = config.getUser();
        final TestContextToolkit tools = new TestContextToolkit();
        final String ctxName = config.getContextName();
        ctx = null == ctxName || ctxName.trim().length() == 0 ? tools.getDefaultContext() : tools.getContextByName(ctxName);

        user = UserToolkit.getUser(getUsername(userName), ctx);

        final String secondUserName = AJAXConfig.getProperty(AJAXConfig.Property.SECONDUSER);
        secondUser = UserToolkit.getUser(getUsername(secondUserName), ctx);

        session = SessionObjectWrapper.createSessionObject(user.getId(), ctx, "DowngradeTest");
    }

    @Override
    protected void tearDown() throws Exception {
        Init.stopServer();
        super.tearDown();
    }

    public void testRemovePrivateParticipants() throws Throwable {
        final int folderId = FolderToolkit.getStandardTaskFolder(user.getId(), ctx);
        final Task task = Create.createWithDefaults(folderId, "DowngradeTest");
        task.setParticipants(new Participant[] {
            new UserParticipant(user.getId()),
            new UserParticipant(secondUser.getId())
        });
        final TasksSQLInterface taskSQL = new TasksSQLImpl(session);
        taskSQL.insertTaskObject(task);
        try {
            downgradeDelegate();
            assertNoParticipants(folderId, task.getObjectID());
        } finally {
            TaskLogic.deleteTask(session, ctx, user.getId(), task, task.getLastModified());
        }
    }

    public void testRemovePublicParticipants() throws Throwable {
        final FolderObject folder = createPublicFolder();
        final int folderId = folder.getObjectID();
        final Task task = Create.createWithDefaults(folderId, "DowngradeTest");
        task.setParticipants(new Participant[] {
            new UserParticipant(user.getId()),
            new UserParticipant(secondUser.getId())
        });
        final TasksSQLInterface taskSQL = new TasksSQLImpl(session);
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

    public void testRemovePrivateTasks() throws OXException, OXException,
    OXException {
        final int folderId = FolderToolkit.getStandardTaskFolder(user.getId(),
            ctx);
        final Task task = Create.createWithDefaults(folderId, "DowngradeTest");
        final TasksSQLInterface taskSQL = new TasksSQLImpl(session);
        taskSQL.insertTaskObject(task);
        downgradeNoTasks();
        assertNoTask(task.getObjectID());
        // Task should be gone by downgrade. No delete necessary.
    }

    public void testRemovePublicTask() throws OXException, OXException,
    OXException {
        final FolderObject folder = createPublicFolder();
        final int folderId = folder.getObjectID();
        final Task task = Create.createWithDefaults(folderId, "DowngradeTest");
        task.setParticipants(new Participant[] {
            new UserParticipant(user.getId()),
            new UserParticipant(secondUser.getId())
        });
        final TasksSQLInterface taskSQL = new TasksSQLImpl(session);
        taskSQL.insertTaskObject(task);
        try {
            downgradeNoTasks();
            assertParticipants(folderId, task.getObjectID());
            updatePublicFolder(folder);
            downgradeNoTasks();
            assertNoTask(task.getObjectID());
        } finally {
            deletePublicFolder(folder);
        }
    }

    /* ----------------- Test help methods ---------------------*/

    private void downgradeDelegate() throws OXException, OXException {

        final UserConfiguration userConfig = new AllowAllUserConfiguration(user.getId(), user.getGroups(), ctx) {
            private static final long serialVersionUID = -6133954203762209965L;

            @Override
            public UserPermissionBits getUserPermissionBits() {
                return new AllowAllUserPermissionBits(userId, groups, ctx) {
                    private static final long serialVersionUID = 8557097436407742416L;

                    @Override
                    public boolean hasPermission(int permissionBit) {
                        if (permissionBit == UserPermissionBits.DELEGATE_TASKS) {
                            return false;
                        }
                        return true;
                    }
                };
            }
        };

        final Connection con = Database.get(ctx, true);
        try {
            final DowngradeEvent event = new DowngradeEvent(userConfig, con, ctx);
            new TasksDowngrade().downgradePerformed(event);
        } finally {
            Database.back(ctx, true, con);
        }
    }

    private void downgradeNoTasks() throws OXException, OXException {
        final UserConfiguration userConfig = new AllowAllUserConfiguration(user.getId(), user.getGroups(), ctx) {
            private static final long serialVersionUID = 400233948268970280L;

            @Override
            public UserPermissionBits getUserPermissionBits() {
                return new AllowAllUserPermissionBits(userId, groups, ctx) {
                    private static final long serialVersionUID = -1380938924019873373L;

                    @Override
                    public boolean hasPermission(int permissionBit) {
                        if (permissionBit == UserPermissionBits.DELEGATE_TASKS || permissionBit == UserPermissionBits.TASKS) {
                            return false;
                        }
                        return true;
                    }
                };
            }
        };
        final Connection con = Database.get(ctx, true);
        try {
            final DowngradeEvent event = new DowngradeEvent(userConfig, con, ctx);
            new TasksDowngrade().downgradePerformed(event);
        } finally {
            Database.back(ctx, true, con);
        }
    }

    private void assertNoParticipants(final int folderId, final int objectId) {
        try {
            final Task task = GetTask.load(ctx, folderId, objectId, StorageType.ACTIVE);
            final Participant[] parts = task.getParticipants();
            if (null != parts && parts.length > 0) {
                throw new AssertionFailedError("Task has participants.");
            }
        } catch (final OXException e) {
            throw new AssertionFailedException(e);
        }
    }

    private void assertParticipants(final int folderId, final int objectId) {
        try {
            final Task task = GetTask.load(ctx, folderId, objectId, StorageType.ACTIVE);
            final Participant[] parts = task.getParticipants();
            if (null == parts || parts.length == 0) {
                throw new AssertionFailedError("Task has no participants.");
            }
        } catch (final OXException e) {
            throw new AssertionFailedException(e);
        }
    }

    private void assertNoTask(final int objectId) {
        try {
            final TaskStorage stor = TaskStorage.getInstance();
            stor.selectTask(ctx, objectId, StorageType.ACTIVE);
            fail("Private task has not been removed on downgrade.");
        } catch (final OXException e) {
            // Getting the exception is fine.
        }
    }

    /* ---------------------- Special tests help methods -----------------*/

    private FolderObject createPublicFolder() throws OXException, OXException {
        final Connection con = DBPool.pickupWriteable(ctx);
        try {
            final OXFolderManager oxma = OXFolderManager.getInstance(session, con, con);
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
        throws OXException, OXException {
        final Connection con = DBPool.pickupWriteable(ctx);
        try {
            final OXFolderManager oxma = OXFolderManager.getInstance(session, con, con);
            final Iterator<OCLPermission> iter = folder.getPermissions().iterator();
            while (iter.hasNext()) {
                if (iter.next().getEntity() == secondUser.getId()) {
                    iter.remove();
                }
            }
            oxma.updateFolder(folder, false, false, System.currentTimeMillis());
        } finally {
            DBPool.pushWrite(ctx, con);
        }
    }

    private void deletePublicFolder(final FolderObject folder)
        throws OXException, OXException {
        final Connection con = DBPool.pickupWriteable(ctx);
        try {
            final OXFolderManager oxma = OXFolderManager.getInstance(session, con, con);
            oxma.deleteFolder(folder, false, System.currentTimeMillis());
        } finally {
            DBPool.pushWrite(ctx, con);
        }
    }
}
