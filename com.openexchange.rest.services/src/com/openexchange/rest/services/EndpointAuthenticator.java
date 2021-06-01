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

package com.openexchange.rest.services;

import java.lang.reflect.Method;

/**
 * {@link EndpointAuthenticator} - Performs authentication for a certain REST end-point.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
public interface EndpointAuthenticator {

    /**
     * Whether all accesses are permitted.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * <b>Note</b>: If this method returns <code>true</code>, {@link #authenticate(String, String, Method) authenticate()} is no more called.
     * </div>
     *
     * @param invokedMethod The method of the REST end-point that is attempted being accessed
     * @return <code>true</code> if all accesses are permitted; otherwise <code>false</code>
     */
    default boolean permitAll(Method invokedMethod) {
        return false;
    }

    /**
     * Authenticates specified request context for a certain REST end-point.
     *
     * @param login The login to check
     * @param password The password to check
     * @param invokedMethod The method of the REST end-point that is attempted being accessed
     * @return <code>true</code> if successfully authenticated; otherwise <code>false</code>
     * @see #permitAll(Method)
     */
    boolean authenticate(String login, String password, Method invokedMethod);

    /**
     * Gets the name of the realm.
     *
     * @return The realm name
     */
    String getRealmName();
}
