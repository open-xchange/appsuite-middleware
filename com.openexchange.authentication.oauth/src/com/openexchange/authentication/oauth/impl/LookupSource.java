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

package com.openexchange.authentication.oauth.impl;

import com.openexchange.authentication.NamePart;

/**
 * {@link LookupSource} denotes the input value used to resolve a user or context
 * within App Suite. The taken value might be evaluated further to determine the
 * actual identifier to lookup the user or context, see {@link NamePart}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @see NamePart
 * @since v7.10.3
 */
public enum LookupSource {

    /**
     * The name given as user input during login.
     */
    LOGIN_NAME("login-name"),
    /**
     * A response parameter from the authorization server.
     */
    RESPONSE_PARAMETER("response-parameter");

    private final String configName;

    private LookupSource(String configName) {
        this.configName = configName;
    }

    /**
     * Gets the name of this part as it would be defined in a configuration property.
     * 
     * @return The configuration name
     */
    public String getConfigName() {
        return configName;
    }

    /**
     * Gets the {@link NamePart} for a given config name or <code>null</code> if none matches.
     * 
     * @param configName
     * @return The config name or <code>null</code>
     */
    public static LookupSource of(String configName) {
        for (LookupSource value : LookupSource.values()) {
            if (value.configName.equals(configName)) {
                return value;
            }
        }

        return null;
    }

}
