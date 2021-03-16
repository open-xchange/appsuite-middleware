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

package com.openexchange.groupware.update;

import java.util.Collection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.update.internal.UpdaterImpl;

/**
 * Interface for the updater.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class Updater {

    /**
     * Factory method to get an updater.
     *
     * @return the updater.
     * @throws OXException if instantiating the implementation fails.
     */
    public static Updater getInstance() {
        return UpdaterImpl.getInstance();
    }

    // -----------------------------------------------------------------------------------------------------------------------------

    /**
     * Default constructor.
     */
    protected Updater() {
        super();
    }

    public final UpdateStatus getStatus(final Context ctx) throws OXException {
        return getStatus(ctx.getContextId());
    }

    public abstract UpdateStatus getStatus(int contextId) throws OXException;

    public abstract UpdateStatus getStatus(String schema, int writePoolId) throws OXException;

    /**
     * Unblocks specified schema (if updating for too long)
     *
     * @param schemaName The schema name
     * @param poolId The pool identifier
     * @param contextId The context identifier
     * @throws OXException If unblocking fails
     */
    public abstract void unblock(String schemaName, int poolId, int contextId) throws OXException;

    /**
     * Starts the update process on a schema.
     * @param contextId Context inside the schema.
     * @throws OXException if an exception occurs.
     */
    public final void startUpdate(final Context ctx) throws OXException {
        startUpdate(ctx.getContextId());
    }

    /**
     * Starts the update process on a schema.
     * @param contextId Identifier of a context inside the schema.
     * @throws OXException if an exception occurs.
     */
    public abstract void startUpdate(int contextId) throws OXException;

    public abstract UpdateTaskV2[] getAvailableUpdateTasks();

    /**
     * Gets a list of schemas whose update tasks have been scheduled for execution
     * or are currently running on this node.
     *
     * @return The list of schemas
     */
    public abstract Collection<String> getLocallyScheduledTasks();

    /**
     * Invalidates the caches for given context identifier.
     *
     * @param contextId The context identifier
     * @throws OXException If cache invalidation fails
     */
    public abstract void invalidateCacheFor(int contextId) throws OXException;

    /**
     * Invalidates the caches for given schema.
     *
     * @param schemaName The schema name
     * @param poolId The pool identifier
     * @throws OXException If cache invalidation fails
     */
    public abstract void invalidateCacheFor(String schemaName, int poolId) throws OXException;

}
