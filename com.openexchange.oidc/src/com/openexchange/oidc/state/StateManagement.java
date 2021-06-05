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

package com.openexchange.oidc.state;

import java.util.concurrent.TimeUnit;
import com.openexchange.exception.OXException;

/**
 * Manager of all client states, that try to login or logout with OpenID features.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public interface StateManagement {

    /**
     * Adds an AuthenticationRequestInfo object to be managed.
     *
     * @param authenticationRequestInfo - The {@link AuthenticationRequestInfo} to be managed by hazelcast
     * @param ttl The time to live
     * @param timeUnit The time unit of <code>ttl</code>
     */
    void addAuthenticationRequest(AuthenticationRequestInfo authenticationRequestInfo, long ttl, TimeUnit timeUnit) throws OXException;

    /**
     * Load the {@link AuthenticationRequestInfo} which is identified by the state.
     *
     * @param state The state to identify the {@link AuthenticationRequestInfo}
     * @return The {@link AuthenticationRequestInfo} or null if no state is given or no user information is stored
     */
    AuthenticationRequestInfo getAndRemoveAuthenticationInfo(String state) throws OXException;

    /**
     * Add the given {@link LogoutRequestInfo} to a hazelcast IMap, which is managed
     * by hazelcast and enables every node in the cluster to handle Logout requests.
     *
     * @param logoutRequestInfo - The {@link LogoutRequestInfo} to managed by hazelcast
     * @param ttl The time to live
     * @param timeUnit The time unit of <code>ttl</code>
     */
    void addLogoutRequest(LogoutRequestInfo logoutRequestInfo, long ttl, TimeUnit timeUnit) throws OXException;

    /**
     * Load the stored {@link LogoutRequestInfo} from hazelcasts {@link IMap} by
     * passing the state, which identifies the {@link LogoutRequestInfo}.
     *
     * @param state The identifier of the {@link LogoutRequestInfo}
     * @return The {@link LogoutRequestInfo}
     */
    LogoutRequestInfo getAndRemoveLogoutRequestInfo(String state) throws OXException;

}
