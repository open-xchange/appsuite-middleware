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
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.downgrade.DowngradeEvent;
import com.openexchange.groupware.downgrade.DowngradeFailedException;
import com.openexchange.groupware.downgrade.DowngradeListener;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;

/**
 * This class implements the methods to delete tasks if a user loses
 * functionalities of the tasks module.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class TasksDowngrade extends DowngradeListener {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(TasksDowngrade.class);

    /**
     * Default constructor.
     */
    public TasksDowngrade() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void downgradePerformed(final DowngradeEvent event)
        throws DowngradeFailedException {
        final UserConfiguration userConfig = event.getNewUserConfiguration();
        final Session session = event.getSession();
        final Context ctx = event.getContext();
        final Connection con = event.getWriteCon();
        if (!userConfig.hasTask()) {
            // If the user completely loses tasks the following should be deleted:
            // - All tasks in private folders.
            // - The participation of the user in all tasks.
            // -
        } else if (!userConfig.canDelegateTasks()) {
            // Remove all delegations of tasks that the user created.
            try {
                removeDelegations(session, ctx, userConfig.getUserId(), userConfig, con);
            } catch (TaskException e) {
                throw new DowngradeFailedException(e);
            } catch (SearchIteratorException e) {
                throw new DowngradeFailedException(e);
            } catch (OXException e) {
                throw new DowngradeFailedException(e);
            }
        }
    }

    private void removeDelegations(final Session session, final Context ctx,
        final int userId, final UserConfiguration userConfig,
        final Connection con) throws TaskException, SearchIteratorException,
        OXException {
        final User user = Tools.getUser(ctx, userId);
        SearchIterator<FolderObject> iter = OXFolderIteratorSQL
            .getAllVisibleFoldersIteratorOfType(userId, user.getGroups(),
            new int[] { FolderObject.TASK }, FolderObject.PRIVATE,
            new int[] { FolderObject.TASK }, ctx);
        try {
            while (iter.hasNext()) {
                final FolderObject folder = iter.next();
                removeDelegationsInFolder(session, ctx, userConfig, con, user,
                    folder);
            }
        } finally {
            iter.close();
        }
        iter = OXFolderIteratorSQL.getAllVisibleFoldersIteratorOfType(userId,
            user.getGroups(), new int[] { FolderObject.TASK },
            FolderObject.PUBLIC, new int[] { FolderObject.TASK }, ctx);
        try {
            while (iter.hasNext()) {
                final FolderObject folder = iter.next();
                final OCLPermission[] ocls = folder.getPermissionsAsArray();
                boolean other = false;
                for (int i = 0; i < ocls.length && !other; i++) {
                    final OCLPermission perm = ocls[i];
                    if (perm.getEntity() != userId 
                        && perm.canWriteAllObjects()) {
                        other = true;
                    }
                }
                if (!other) {
                    removeDelegationsInFolder(session, ctx, userConfig, con,
                        user, folder);
                }
            }
        } finally {
            iter.close();
        }
    }

    private static FolderStorage foldStor = FolderStorage.getInstance();

    private void removeDelegationsInFolder(final Session session,
        final Context ctx, final UserConfiguration userConfig,
        final Connection con, final User user, final FolderObject folder)
        throws TaskException {
        final int[] taskIds = foldStor.getTasksInFolder(ctx, con, folder
            .getObjectID(), StorageType.ACTIVE);
        for (int taskId : taskIds) {
            final Task task = new Task();
            task.setObjectID(taskId);
            task.setParentFolderID(folder.getObjectID());
            task.setParticipants(new Participant[0]);
            final UpdateData update = new UpdateData(ctx, user, userConfig,
                folder, task, new Date());
            update.prepareWithoutChecks();
            update.doUpdate();
            try {
                update.sentEvent(session);
            } catch (OXException e) {
                LOG.error("Problem triggering event for updated task.", e);
            }
            try {
                update.updateReminder();
            } catch (OXException e) {
                LOG.error("Problem while updating reminder for a task.", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getOrder() {
        return 3;
    }
}
