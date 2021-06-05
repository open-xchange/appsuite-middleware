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

package com.openexchange.oidc;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link AuthenticationInfo} Contains all needed and additional information, that is
 * needed to authenticate a user.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public class AuthenticationInfo {

    private final Map<String, String> properties = new HashMap<>();

    private final int contextId;

    private final int userId;

    /**
     * Initializes a new {@link AuthenticationInfo}.
     *
     * @param contextId The context ID
     * @param userId The user ID
     */
    public AuthenticationInfo(int contextId, int userId) {
        super();
        this.contextId = contextId;
        this.userId = userId;
    }

    /**
     * Gets the context ID
     *
     * @return The context ID
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Gets the user ID
     *
     * @return The user ID
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the properties as mutable map.
     *
     * @return The properties, possibly empty but not <code>null</code>
     */
    public ImmutableMap<String, String> getProperties() {
        return ImmutableMap.copyOf(this.properties);
    }

    /**
     * Sets a property. Please note that internally some attributes are contributed to this
     * map. They will always be prefixed with <code>com.openexchange.oidc</code>. You should
     * either use your own namespace for those properties or use un-qualified keys. A property
     * will be overridden if it is set more than once.
     *
     * @param key The key
     * @param value The value
     */
    public void setProperty(String key, String value) {
        properties.put(key, value);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + contextId;
        result = prime * result + userId;
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AuthenticationInfo other = (AuthenticationInfo) obj;
        if (contextId != other.contextId) {
            return false;
        }
        if (userId != other.userId) {
            return false;
        }
        if (properties == null) {
            if (other.properties != null) {
                return false;
            }
        } else if (!properties.equals(other.properties)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AuthenticationInfo [contextId=");
        sb.append(contextId);
        sb.append(", userId=");
        sb.append(userId);
        sb.append(", properties=");
        sb.append(properties);
        sb.append("]");
        return sb.toString();
    }

}
