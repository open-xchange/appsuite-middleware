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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.sessiond.SessionObject;

/**
 * This class implements the method that are necessary for linking tasks.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Task2Links {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(Task2Links.class);

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
    public static boolean checkMayReadTask(final SessionObject session,
        final int taskId, final int folderId) {
        final Context ctx = session.getContext();
        final User user = UserStorage.getUser(session.getUserId(), session.getContext());
        final FolderObject folder;
        final Task task;
        try {
            folder = Tools.getFolder(ctx, folderId);
            final TaskStorage storage = TaskStorage.getInstance();
            task = storage.selectTask(ctx, taskId, StorageType.ACTIVE);
        } catch (TaskException e) {
            LOG.error("Problem while reading task from database.", e);
            return false;
        }
        try {
			Permission.canReadInFolder(ctx, user, UserConfigurationStorage.getInstance().getUserConfigurationSafe(
					session.getUserId(), session.getContext()), folder, task);
		} catch (TaskException e) {
            return false;
        }
        return true;
    }
}
