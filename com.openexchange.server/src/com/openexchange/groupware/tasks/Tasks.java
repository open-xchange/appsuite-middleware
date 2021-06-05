/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
        } catch (OXException e) {
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
