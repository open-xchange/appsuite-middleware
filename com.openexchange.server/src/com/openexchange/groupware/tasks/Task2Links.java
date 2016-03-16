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

import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.session.Session;

/**
 * This class implements the method that are necessary for linking tasks.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Task2Links {

    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Task2Links.class);

    /**
     * Prevent instantiation
     */
    private Task2Links() {
        super();
    }

    /**
     * Checks if a task referenced by a link may be read.
     * @param session Session.
     * @param taskId Unique identifier of the task.
     * @param folderId Unique identifier of the folder through that the task is
     * referenced.
     * @return <code>true</code> if the task may be read, <code>false</code>
     * otherwise.
     */
    public static boolean checkMayReadTask(final Session session, final Context ctx, final UserPermissionBits permissionBits, final int taskId) {
        final User user;
        final Task task;
        final Set<Folder> folders;
        try {
            user = Tools.getUser(ctx, session.getUserId());
            final TaskStorage storage = TaskStorage.getInstance();
            task = storage.selectTask(ctx, taskId, StorageType.ACTIVE);
            folders = FolderStorage.getInstance().selectFolder(ctx, taskId, StorageType.ACTIVE);
        } catch (final OXException e) {
            LOG.error("", e);
            return false;
        }
        for (final Folder folder : folders) {
            if (mayRead(ctx, user, permissionBits, task, folder)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkMayReadTask(final Session session, final Context ctx, final UserPermissionBits permissionBits, final int taskId, final int folderId) {
        final User user;
        final Task task;
        final Folder folder;
        try {
            user = Tools.getUser(ctx, session.getUserId());
            final TaskStorage storage = TaskStorage.getInstance();
            task = storage.selectTask(ctx, taskId, StorageType.ACTIVE);
            folder = FolderStorage.getInstance().selectFolderById(ctx, taskId, folderId, StorageType.ACTIVE);
        } catch (final OXException e) {
            LOG.error("", e);
            return false;
        }
        return null == folder ? false : mayRead(ctx, user, permissionBits, task, folder);
    }

    private static boolean mayRead(final Context ctx, final User user, final UserPermissionBits permissionBits, final Task task, final Folder folder) {
        final FolderObject folder2;
        try {
            folder2 = Tools.getFolder(ctx, folder.getIdentifier());
        } catch (final OXException e) {
            LOG.error("", e);
            return false;
        }
        try {
            Permission.isFolderVisible(ctx, user, permissionBits, folder2);
            Permission.canReadInFolder(ctx, user, permissionBits, folder2, task);
            return true;
        } catch (final OXException e) {
            return false;
        }
    }
}
