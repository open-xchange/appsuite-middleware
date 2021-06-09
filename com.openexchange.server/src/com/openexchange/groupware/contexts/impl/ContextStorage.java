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

package com.openexchange.groupware.contexts.impl;

import java.util.List;
import java.util.Map;
import com.openexchange.context.PoolAndSchema;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.UpdateBehavior;
import com.openexchange.groupware.update.UpdateStatus;
import com.openexchange.groupware.update.Updater;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * This class defines the methods for accessing the storage of contexts. TODO We should introduce a logic layer above this context storage
 * layer. That layer should then trigger the update tasks. Nearly all accesses to the ContextStorage need then to be replaced with an access
 * to the ContextService.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class ContextStorage {

    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContextStorage.class);

    /**
     * Singleton implementation.
     */
    private static volatile ContextStorage impl;

    /**
     * Will be returned if a context cannot be found through its login info.
     */
    public static final int NOT_FOUND = -1;

    /**
     * Creates an instance implementing the context storage.
     *
     * @return an instance implementing the context storage.
     */
    public static ContextStorage getInstance() {
        ContextStorage tmp = impl;
        if (null == tmp) {
            synchronized (ContextStorage.class) {
                tmp = impl;
                if (null == tmp) {
                    try {
                        tmp = CachingContextStorage.parent = new CachingContextStorage(new RdbContextStorage());
                        tmp.startUp();
                        impl = tmp;
                    } catch (OXException e) {
                        // Cannot occur
                        LOG.warn("", e);
                    }
                }
            }
        }
        return tmp;
    }

    /**
     * Instantiates an implementation of the context interface and fill its attributes according to the needs to be able to separate
     * contexts.
     *
     * @param loginContextInfo the login info for the context.
     * @return the unique identifier of the context or <code>-1</code> if no matching context exists.
     * @throws OXException if an error occurs.
     */
    public abstract int getContextId(String loginContextInfo) throws OXException;

    public final Context getContext(final Session session) throws OXException {
        return getContext(session.getContextId());
    }

    /**
     * Creates a context implementation for the given context unique identifier.
     *
     * @param contextId unique identifier of the context.
     * @return an implementation of the context or <code>null</code> if the context with the given identifier can't be found.
     * @throws OXException if an error occurs.
     */
    public Context getContext(final int contextId) throws OXException {
        return getContext(contextId, UpdateBehavior.NONE);
    }

    /**
     * Creates a context implementation for the given context unique identifier.
     *
     * @param contextId unique identifier of the context.
     * @param updateBehavior The behavior to apply when detecting one or more pending update task for context-associated database schema
     * @return An implementation of the context or <code>null</code> if the context with the given identifier can't be found.
     * @throws OXException If an error occurs.
     */
    public Context getContext(final int contextId, UpdateBehavior updateBehavior) throws OXException {
        final ContextExtended retval = loadContext(contextId, updateBehavior);
        if (retval.isUpdating()) {
            Updater updater = Updater.getInstance();
            UpdateStatus status = updater.getStatus(contextId);
            if (status.blockingUpdatesRunning() == false || status.blockingUpdatesTimedOut()) {
                // Invalidate caches & repeat
                updater.invalidateCacheFor(status.getSchemaName(), status.getPoolId());
                return getContext(contextId, updateBehavior);
            }
            OXException exception = ContextExceptionCodes.UPDATE.create();
            LOG.info(exception.getMessage());
            throw exception;
        }
        if (retval.isUpdateNeeded()) {
            OXException exception = ContextExceptionCodes.UPDATE_NEEDED.create();
            LOG.info(exception.getMessage());
            throw exception;
        }
        return retval;
    }

    /**
     * Loads the context object.
     *
     * @param contextId unique identifier of the context to load.
     * @return The context object.
     * @throws OXException If loading the context fails.
     */
    public ContextExtended loadContext(int contextId) throws OXException {
        return loadContext(contextId, UpdateBehavior.NONE);
    }

    /**
     * Loads the context object.
     *
     * @param contextId unique identifier of the context to load.
     * @param updateBehavior The behavior to apply when detecting one or more pending update task for context-associated database schema
     * @return The context object.
     * @throws OXException If loading the context fails.
     */
    public abstract ContextExtended loadContext(int contextId, UpdateBehavior updateBehavior) throws OXException;

    /**
     * Checks if specified context does exist.
     *
     * @param contextId The context identifier
     * @return <code>true</code> if such a context is existent; otherwise <code>false</code>
     * @throws OXException If check for existence fails
     */
    public abstract boolean exists(int contextId) throws OXException;

    /**
     * Invalidates the context object in cache(s).
     *
     * @param contextId unique identifier of the context to invalidate
     * @throws OXException if invalidating the context fails
     */
    public void invalidateContext(final int contextId) throws OXException {
        LOG.trace("invalidateContext not implemented in {}", this.getClass().getCanonicalName());
    }

    /**
     * Invalidates the context objects in cache(s).
     *
     * @param contextIDs unique identifiers of the contexts to invalidate
     * @throws OXException if invalidating the context fails
     */
    public void invalidateContexts(final int[] contextIDs) throws OXException {
        LOG.trace("invalidateContext not implemented in {}", this.getClass().getCanonicalName());
    }

    /**
     * Invalidates a login information in the cache.
     *
     * @param loginContextInfo login information to invalidate.
     * @throws OXException if invalidating the login information fails.
     */
    public void invalidateLoginInfo(final String loginContextInfo) throws OXException {
        LOG.trace("invalidateLoginInfo not implemented in {}", this.getClass().getCanonicalName());
    }

    /**
     * Gives a list of all context ids which are stored in the config database.
     *
     * @return the list of context ids
     * @throws OXException if reading the contexts fails.
     */
    public abstract List<Integer> getAllContextIds() throws OXException;

    /**
     * Gets a listing of exactly one context per database schema
     *
     * @return A listing of distinct contexts per schema
     * @throws OXException If contexts cannot be returned
     */
    public abstract List<Integer> getDistinctContextsPerSchema() throws OXException;

    /**
     * Groups all context identifiers by their schema associations.
     *
     * @return A mapping of a representative schema-associated context identifier to other contexts residing in that schema taken from specified context identifiers
     * @throws OXException If the mapping cannot be returned
     */
    public abstract Map<PoolAndSchema, List<Integer>> getSchemaAssociations() throws OXException;

    /**
     * Groups specified context identifiers by their schema associations.
     *
     * @param contextIds The context identifiers to group by schema association
     * @return A mapping of a representative schema-associated context identifier to other contexts residing in that schema taken from specified context identifiers
     * @throws OXException If the mapping cannot be returned
     */
    public abstract Map<PoolAndSchema, List<Integer>> getSchemaAssociationsFor(List<Integer> contextIds) throws OXException;

    /**
     * Get a list of all context ids that are bound to the specified filestore id
     *
     * @param filestoreId the filestoreId
     * @return the list of context ids
     * @throws OXException if reading the contexts fails.
     */
    public abstract List<Integer> getAllContextIdsForFilestore(final int filestoreId) throws OXException;

    /**
     * Stores an internal context attribute. Internal context attributes must not be exposed to clients through the HTTP/JSON API.
     * <p>
     * This method might throw a {@link ContextExceptionCodes#CONCURRENT_ATTRIBUTES_UPDATE_DISPLAY} error in case a concurrent modification occurred. The
     * caller can decide to treat as an error or to simply ignore it.
     *
     * @param name Name of the attribute.
     * @param value Value of the attribute. If the value is <code>null</code>, the attribute is removed.
     * @param contextId Identifier of the context that attribute should be set.
     * @throws OXException if writing the attribute fails.
     * @see ContextExceptionCodes#CONCURRENT_ATTRIBUTES_UPDATE
     */
    public abstract void setAttribute(String name, String value, int contextId) throws OXException;

    /**
     * Internal start-up routine invoked in {@link #start()}
     *
     * @throws OXException If an error occurs
     */
    protected abstract void startUp() throws OXException;

    /**
     * Internal shut-down routine invoked in {@link #stop()}
     *
     * @throws OXException If an error occurs
     */
    protected abstract void shutDown() throws OXException;

    /**
     * Initialization.
     *
     * @throws OXException if initialization of contexts fails.
     */
    public static void start() throws OXException {
        ContextStorage impl = ContextStorage.impl;
        if (null != impl) {
            LOG.error("Duplicate initialization of ContextStorage.");
            return;
        }
        impl = CachingContextStorage.parent = new CachingContextStorage(new RdbContextStorage());
        impl.startUp();
        ContextStorage.impl = impl;
    }

    /**
     * Shutdown.
     */
    public static void stop() throws OXException {
        final ContextStorage impl = ContextStorage.impl;
        if (null == impl) {
            LOG.error("Duplicate shutdown of ContextStorage.");
            return;
        }
        impl.shutDown();
        ContextStorage.impl = null;
    }

    /**
     * Convenience method for getting the context.
     *
     * @param session The session providing the context ID
     * @return the context data object or null if the context with the given identifier can't be found.
     * @throws OXException if getting the context fails.
     */
    public static Context getStorageContext(final Session session) throws OXException {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getContext();
        }
        return getStorageContext(session.getContextId());
    }

    /**
     * Convenience method for getting the context.
     *
     * @param contextId unique identifier of the context.
     * @return the context data object or null if the context with the given identifier can't be found.
     * @throws OXException if getting the context fails.
     */
    public static Context getStorageContext(final int contextId) throws OXException {
        return getInstance().getContext(contextId);
    }
}
