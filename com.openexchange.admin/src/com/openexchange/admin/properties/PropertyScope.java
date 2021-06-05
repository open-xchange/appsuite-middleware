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

package com.openexchange.admin.properties;

import java.util.List;
import java.util.Optional;
import com.google.common.collect.ImmutableList;
import com.openexchange.config.cascade.ConfigViewScope;

/**
 * The scope for a configuration option.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class PropertyScope {

    /** A listing of scopes: <code>user</code> -&gt; <code>context</code> -&gt; <code>server</code> */
    private static final List<String> SCOPES_FROM_USER = ImmutableList.of(ConfigViewScope.USER.getScopeName(), ConfigViewScope.CONTEXT.getScopeName(), ConfigViewScope.RESELLER.getScopeName(), ConfigViewScope.SERVER.getScopeName());

    /** A listing of scopes: <code>context</code> -&gt; <code>server</code> */
    private static final List<String> SCOPES_FROM_CONTEXT = ImmutableList.of(ConfigViewScope.CONTEXT.getScopeName(), ConfigViewScope.RESELLER.getScopeName(), ConfigViewScope.SERVER.getScopeName());

    /** A listing of scopes: <code>server</code> */
    private static final List<String> SCOPES_FROM_SERVER = ImmutableList.of(ConfigViewScope.SERVER.getScopeName());

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static final PropertyScope REGEX_SCOPE_SERVER = new PropertyScope(-1, -1, SCOPES_FROM_SERVER);

    /**
     * Gets the property scope for server.
     *
     * @return The property scope
     */
    public static PropertyScope propertyScopeForServer() {
        return REGEX_SCOPE_SERVER;
    }

    /**
     * Gets the property scope for context.
     *
     * @param contextId The context identifier
     * @return The property scope
     */
    public static PropertyScope propertyScopeForContext(int contextId) {
        return new PropertyScope(-1, contextId, SCOPES_FROM_CONTEXT);
    }

    /**
     * Gets the property scope for user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The property scope
     */
    public static PropertyScope propertyScopeForUser(int userId, int contextId) {
        return new PropertyScope(userId, contextId, SCOPES_FROM_USER);
    }

    /**
     * Gets the property scope for default search path.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The property scope
     */
    public static PropertyScope propertyScopeForDefaultSearchPath(int userId, int contextId) {
        return new PropertyScope(userId, contextId, null);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final int contextId;
    private final int userId;
    private final Optional<List<String>> optionalScopes;

    /**
     * Initializes a new {@link PropertyScope}.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param scopes The scopes to iterate
     */
    private PropertyScope(int userId, int contextId, List<String> scopes) {
        super();
        this.userId = userId;
        this.contextId = contextId;
        this.optionalScopes = Optional.ofNullable(scopes);
    }

    /**
     * Gets the context identifier.
     *
     * @return The context identifier
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Gets the user identifier.
     *
     * @return The user identifier
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the optional scopes to iterate.
     * <p>
     * If absent/empty the regular search path is taken.
     *
     * @return The scopes
     */
    public Optional<List<String>> getScopes() {
        return optionalScopes;
    }
}