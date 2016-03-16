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
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.impl.DBPool;
import com.openexchange.session.Session;

/**
 * Interface for accessing methods of the tasks module.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class Tasks {

    /**
     * Singleton instance.
     */
    private static final Tasks SINGLETON = new TasksImpl();

    /**
     * Default constructor.
     */
    protected Tasks() {
        super();
    }

    /**
     * Factory method.
     * @return an implementation.
     */
    public static final Tasks getInstance() {
        return SINGLETON;
    }

    /**
     * @deprecated use {@link #containsNotSelfCreatedTasks(Session, Connection, int)}
     */
    @Deprecated
    public final boolean containsNotSelfCreatedTasks(final Session session,
        final int folderId) throws OXException {
        final Context ctx;
        final Connection con;
        try {
            ctx = Tools.getContext(session.getContextId());
            con = DBPool.pickup(ctx);
        } catch (final OXException e) {
            throw e;
        }
        try {
            return containsNotSelfCreatedTasks(session, con, folderId);
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
    }

    /**
     * Checks if a folder contains tasks that are not created by the user the
     * session belongs to.
     * @param session Session.
     * @param con readonly database connection.
     * @param folderId task folder.
     * @return <code>true</code> if the folder contains tasks that are created
     * by users other than the user of the session, <code>false</code>
     * otherwise.
     * @throws OXException if an error occurs.
     */
    public abstract boolean containsNotSelfCreatedTasks(Session session,
        Connection con, int folderId) throws OXException;

    /**
     * Deletes all tasks in a folder.
     * @param session Session.
     * @param con writable database connection.
     * @param folderId identifier of a folder that tasks should be deleted.
     * @throws OXException if a problem occurs.
     */
    public abstract void deleteTasksInFolder(Session session, Connection con,
        int folderId) throws OXException;

    /**
     * Tests if a folder is empty.
     * @param ctx Context.
     * @param folderId unique identifier of the folder.
     * @return <code>true</code> if the folder is empty.
     * @throws OXException if an error occurs.
     */
    public abstract boolean isFolderEmpty(Context ctx, int folderId)
        throws OXException;

    public abstract boolean isFolderEmpty(Context ctx, Connection con,
        int folderId) throws OXException;
}
