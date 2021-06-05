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

package com.openexchange.config.cascade;

import com.openexchange.java.Strings;

/**
 * {@link ConfigViewScope} - An enumeration of known config view scopes.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public enum ConfigViewScope {

    SERVER("server"),
    RESELLER("reseller"),
    CONTEXT_SETS("contextSets"),
    CONTEXT("context"),
    USER("user"),
    ;

    private final String scopeName;

    /**
     * Initializes a new {@link ConfigViewScope}.
     *
     * @param scopeName the scope name
     */
    private ConfigViewScope(String scopeName) {
        this.scopeName = scopeName;
    }

    /**
     * Returns the scope name
     *
     * @return the scope name
     */
    public String getScopeName() {
        return scopeName;
    }

    /**
     * Gets the config view scope for given identifier.
     *
     * @param scope The scope identifier to resolve
     * @return The config view scope or <code>null</code>
     */
    public static ConfigViewScope scopeFor(String scope) {
        if (Strings.isEmpty(scope)) {
            return null;
        }

        String lcs = Strings.asciiLowerCase(scope);
        for (ConfigViewScope configViewScope : ConfigViewScope.values()) {
            if (configViewScope.scopeName.equals(lcs)) {
                return configViewScope;
            }
        }
        return null;
    }

    /**
     * Gets all available scope names.
     *
     * @return All available scope names
     */
    public static String[] getAvailableScopeNames() {
        ConfigViewScope[] configViewScopes = ConfigViewScope.values();
        String[] scopez = new String[configViewScopes.length];
        for (int i = scopez.length; i-- > 0;) {
            scopez[i] = configViewScopes[i].getScopeName();
        }
        return scopez;
    }

}
