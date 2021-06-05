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

package com.openexchange.authentication;

/**
 * {@link ContextAndUserInfo} - Provides the context and user information that is supposed to be used to resolve to a (numeric) context/user identifier.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class ContextAndUserInfo {

    private final String userInfo;
    private final String contextInfo;
    private int hash; // No need to be synchronized

    /**
     * Initializes a new {@link ContextAndUserInfo} with <code>"defaultcontext"</code> as context information.
     *
     * @param userInfo The user information
     */
    public ContextAndUserInfo(String userInfo) {
        this(userInfo, "defaultcontext");
    }

    /**
     * Initializes a new {@link ContextAndUserInfo}.
     *
     * @param userInfo The user information
     * @param contextInfo The context information
     */
    public ContextAndUserInfo(String userInfo, String contextInfo) {
        super();
        this.userInfo = userInfo;
        this.contextInfo = contextInfo;
        hash = 0;
    }

    /**
     * Gets the user information
     *
     * @return The user information
     */
    public String getUserInfo() {
        return userInfo;
    }

    /**
     * Gets the context information
     *
     * @return The context information
     */
    public String getContextInfo() {
        return contextInfo;
    }

    @Override
    public int hashCode() {
        int h = this.hash;
        if (h == 0) {
            int prime = 31;
            h = prime * 1 + ((contextInfo == null) ? 0 : contextInfo.hashCode());
            h = prime * h + ((userInfo == null) ? 0 : userInfo.hashCode());
            this.hash = h;
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ContextAndUserInfo)) {
            return false;
        }
        ContextAndUserInfo other = (ContextAndUserInfo) obj;
        if (contextInfo == null) {
            if (other.contextInfo != null) {
                return false;
            }
        } else if (!contextInfo.equals(other.contextInfo)) {
            return false;
        }
        if (userInfo == null) {
            if (other.userInfo != null) {
                return false;
            }
        } else if (!userInfo.equals(other.userInfo)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(64);
        if (userInfo != null) {
            builder.append("userInfo=").append(userInfo).append(", ");
        }
        if (contextInfo != null) {
            builder.append("contextInfo=").append(contextInfo);
        }
        return builder.toString();
    }

}
