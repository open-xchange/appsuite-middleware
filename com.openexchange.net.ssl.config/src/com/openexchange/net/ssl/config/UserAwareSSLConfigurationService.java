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

package com.openexchange.net.ssl.config;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link UserAwareSSLConfigurationService}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.3
 */
@SingletonService
public interface UserAwareSSLConfigurationService {

    public static final String USER_CONFIG_ENABLED_PROPERTY = "com.openexchange.net.ssl.user.configuration.enabled";

    /**
     * Name of the user attribute the configuration made by the user is handled for.
     */
    public static final String USER_ATTRIBUTE_NAME = "acceptUntrustedCertificates";

    /**
     * Returns if the user is allowed to define the trust level for external connections.
     * 
     * @param userId The id of the user to check
     * @param contextId The id of the context the user is associated to
     * @return <code>true</code> if the user is allowed to define the trust level; otherwise <code>false</code>
     */
    boolean isAllowedToDefineTrustLevel(int userId, int contextId);

    /**
     * Returns if the given user configured to trust all external connections.
     * 
     * @see #setTrustAll(int, Context, boolean)
     * @param userId The id of the user to check
     * @param contextId The id of the context the user is associated to
     * @return <code>true</code> if the user has configured to trust all connections; otherwise <code>false</code>
     */
    boolean isTrustAll(int userId, int contextId);

    /**
     * Sets if the given user would like to trust all external connections without taking the server configuration into account.
     * 
     * @param userId The id of the user to check
     * @param contextId The context the user is associated to
     * @param trustAll <code>true</code> to set if the user would like to trust all connections; otherwise <code>false</code>
     */
    void setTrustAll(int userId, Context context, boolean trustAll);

    /**
     * Returns <code>true</code> if the user is allowed to manage the SSL certificates; <code>false</code> otherwise
     * 
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if the user is allowed to manage the SSL certificates; <code>false</code> otherwise
     */
    boolean canManageCertificates(int userId, int contextId);

}
