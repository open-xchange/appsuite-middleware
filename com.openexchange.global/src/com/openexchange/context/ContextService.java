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

package com.openexchange.context;

import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.UpdateBehavior;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link ContextService} - Offers access method to context module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
@SingletonService
public interface ContextService {

    /**
     * Instantiates an implementation of the context interface and fill its
     * attributes according to the needs to be able to separate contexts.
     *
     * @param loginContextInfo
     *            the login info for the context.
     * @return the unique identifier of the context or <code>-1</code> if no
     *         matching context exists.
     * @throws OXException
     *             if an error occurs.
     */
    int getContextId(String loginContextInfo) throws OXException;

    /**
     * Checks if specified context does exist.
     *
     * @param contextId The context identifier
     * @return <code>true</code> if such a context is existent; otherwise <code>false</code>
     * @throws OXException If check for existence fails
     */
    default boolean exists(int contextId) throws OXException {
        try {
            Context context = getContext(contextId);
            return context != null;
        } catch (OXException e) {
            if (e.equalsCode(2, "CTX")) {
                return false;
            }
            throw e;
        }
    }

    /**
     * Gets the context for the given context unique identifier.
     *
     * @param contextId The unique identifier of the context.
     * @return The context
     * @throws OXException If the specified context cannot be found or the update is running/started.
     */
    default Context getContext(int contextId) throws OXException {
        return getContext(contextId, UpdateBehavior.NONE);
    }

    /**
     * Gets the context for the given context unique identifier.
     *
     * @param contextId The unique identifier of the context.
     * @param updateBehavior The behavior to apply when detecting one or more pending update task for context-associated database schema
     * @return The context
     * @throws OXException If the specified context cannot be found or the update is running/started.
     */
    Context getContext(int contextId, UpdateBehavior updateBehavior) throws OXException;

    /**
     * This method works like {@link #getContext(int)} but it does not give a {@link OXException} if an update is running or must is
     * started.
     * @param contextId unique identifier of the context.
     * @return an implementation of the context or <code>null</code> if the context with the given identifier can't be found.
     * @throws OXException if an error occurs.
     */
    default Context loadContext(int contextId) throws OXException {
        return loadContext(contextId, UpdateBehavior.NONE);
    }

    /**
     * This method works like {@link #getContext(int)} but it does not give a {@link OXException} if an update is running or must is
     * started.
     * @param contextId unique identifier of the context.
     * @param updateBehavior The behavior to apply when detecting one or more pending update task for context-associated database schema
     * @return an implementation of the context or <code>null</code> if the context with the given identifier can't be found.
     * @throws OXException if an error occurs.
     */
    Context loadContext(int contextId, UpdateBehavior updateBehavior) throws OXException;

    /**
     * Stores a internal context attribute.
     * <p>
     * This method might throw a {@link ContextExceptionCodes#CONCURRENT_ATTRIBUTES_UPDATE_DISPLAY} error in case a concurrent modification occurred. The
     * caller can decide to treat as an error or to simply ignore it.
     *
     * @param name Name of the attribute.
     * @param value Value of the attribute. If the value is <code>null</code>, the attribute is removed.
     * @param contextId Identifier of the context that attribute should be set.
     * @throws OXException if writing the attribute fails.
     */
    void setAttribute(String name, String value, int contextId) throws OXException;

    /**
     * Invalidates the context object in cache(s).
     *
     * @param contextId
     *            unique identifier of the context to invalidate
     * @throws OXException
     * @throws OXException
     *             if invalidating the context fails
     */
    void invalidateContext(int contextId) throws OXException;

    /**
     * Invalidates the context objects in cache(s).
     *
     * @param contextIDs unique identifiers of the contexts to invalidate
     * @throws OXException if invalidating the context fails
     */
    public void invalidateContexts(final int[] contextIDs) throws OXException;

    /**
     * Invalidates a login information in the cache.
     *
     * @param loginContextInfo
     *            login information to invalidate.
     * @throws OXException
     * @throws OXException
     *             if invalidating the login information fails.
     */
    void invalidateLoginInfo(String loginContextInfo) throws OXException;

    /**
     * Gives a list of all context ids which are stored in the config database.
     *
     * @return the list of context ids
     * @throws OXException
     *             if reading the contexts fails.
     */
    List<Integer> getAllContextIds() throws OXException;

    /**
     * Gets a listing of exactly one context per database schema
     *
     * @return A listing of distinct contexts per schema
     * @throws OXException If contexts cannot be returned
     */
    List<Integer> getDistinctContextsPerSchema() throws OXException;

    /**
     * Groups all context identifiers by their schema associations.
     *
     * @return A mapping of a schema to contexts residing in that schema
     * @throws OXException If the mapping cannot be returned
     */
    Map<PoolAndSchema, List<Integer>> getSchemaAssociations() throws OXException;

    /**
     * Groups specified context identifiers by their schema associations.
     *
     * @param contextIds The context identifiers to group by schema association
     * @return A mapping of a schema to contexts residing in that schema taken from specified context identifiers
     * @throws OXException If the mapping cannot be returned
     */
    Map<PoolAndSchema, List<Integer>> getSchemaAssociationsFor(List<Integer> contextIds) throws OXException;

}
