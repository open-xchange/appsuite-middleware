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

package com.openexchange.chronos.schedjoules.impl.cache;

/**
 * {@link SchedJoulesCachedAPIKey}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SchedJoulesCachedAPIKey {

    private final String apiKeyHash;
    private final int contextId;

    /**
     * Initialises a new {@link SchedJoulesCachedAPIKey}.
     */
    public SchedJoulesCachedAPIKey(final String apiKeyHash, final int contextId) {
        super();
        this.apiKeyHash = apiKeyHash;
        this.contextId = contextId;
    }

    /**
     * Gets the apiKeyHash
     *
     * @return The apiKeyHash
     */
    public String getApiKeyHash() {
        return apiKeyHash;
    }

    /**
     * Gets the contextId
     *
     * @return The contextId
     */
    public int getContextId() {
        return contextId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((apiKeyHash == null) ? 0 : apiKeyHash.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SchedJoulesCachedAPIKey other = (SchedJoulesCachedAPIKey) obj;
        if (apiKeyHash == null) {
            if (other.apiKeyHash != null) {
                return false;
            }
        } else if (!apiKeyHash.equals(other.apiKeyHash)) {
            return false;
        }
        return true;
    }
}
