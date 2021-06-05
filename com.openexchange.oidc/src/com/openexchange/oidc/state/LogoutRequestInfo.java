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


/**
 * {@link LogoutRequestInfo} Storage Object for all needed information when a user
 * is intended to be logged out.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public interface LogoutRequestInfo {
    
    /**
     * The state of the client, that triggered the request.
     * 
     * @return The state of the client. Never <code>null</code>
     */
    String getState();
    
    /**
     * The domain name, the request is coming from.
     * 
     * @return The domain name of the client. Never <code>null</code>
     */
    String getDomainName();
    
    /**
     * Load the associated session id.
     * 
     * @return The session id
     */
    String getSessionId();
    
    /**
     * Load the URI from where the logout was requested
     * 
     * @return - The logout URI
     */
    String getRequestURI();
}
