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

package com.openexchange.auth;

import javax.management.MBeanException;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link Authenticator} - For administrative authentication.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.2
 */
@SingletonService
public interface Authenticator {

    /**
     * Checks if master authentication has been disabled for associated machine.
     *
     * @return <code>true</code> if master authentication has been disabled; otherwise <code>false</code>
     * @throws MBeanException If operation fails
     */
    boolean isMasterAuthenticationDisabled() throws OXException;

    /**
     * Checks if context authentication has been disabled for associated machine.
     *
     * @return <code>true</code> if context authentication has been disabled; otherwise <code>false</code>
     * @throws MBeanException If operation fails
     */
    boolean isContextAuthenticationDisabled() throws OXException;

    /**
     * Authenticates the master administrator
     *
     * @param authdata The credentials for master administrator to check
     * @throws OXException If credentials are invalid
     */
    void doAuthentication(Credentials authdata) throws OXException;

    /**
     * Authenticates the context administrator
     *
     * @param authdata The credentials for context administrator to check
     * @param contextId The context identifier
     * @throws OXException If credentials are invalid
     */
    void doAuthentication(Credentials authdata, int contextId) throws OXException;

    /**
     * Authenticates the context administrator
     *
     * @param authdata The credentials for context administrator to check
     * @param contextId The context identifier
     * @param plugInAware If set to <code>true</code> possibly registered {@link BasicAuthenticatorPluginInterface} instances are respected; otherwise such instances are <b>ignored</b>
     * @throws OXException If credentials are invalid
     */
    void doAuthentication(Credentials authdata, int contextId, boolean plugInAware) throws OXException;

    /**
     * Authenticates all users within a context.
     *
     * @param authdata The credentials for user to check
     * @param contextId The context identifier
     * @throws OXException If credentials are invalid
     */
    void doUserAuthentication(Credentials authdata, int contextId) throws OXException;

}
