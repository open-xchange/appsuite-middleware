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

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.exception.OXException;

/**
 * Determines how login and logout exception should be handled by the backend.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public interface OIDCExceptionHandler {
    
    /**
     * What is supposed to happen after a failed user authentication? This method will be executed.
     * 
     * @param request The {@link HttpServletRequest} that triggered the authentication
     * @param response The {@link HttpServletResponse}
     * @param exception The {@link IOException} that led to the authentication failure
     * @throws IOException The {@link IOException}
     */
    void handleAuthenticationFailed(HttpServletRequest request, HttpServletResponse response, OXException exception) throws IOException;

    /**
     * What is supposed to happen after a failed user logout? This method will be executed.
     * 
     * @param request The {@link HttpServletRequest} that triggered the logout
     * @param response The {@link HttpServletResponse}
     * @param exception The {@link IOException} that led to the logout failure
     * @throws IOException The {@link IOException}
     */
    void handleLogoutFailed(HttpServletRequest request, HttpServletResponse response, OXException exception) throws IOException;
    
    /**
     * A default handler for the given exception.
     * 
     * @param request The {@link HttpServletRequest} that triggered the logout
     * @param response The {@link HttpServletResponse}
     * @param exception The {@link IOException} that led to the logout failure
     * @throws IOException The {@link IOException}
     */
    void handleResponseException(HttpServletRequest request, HttpServletResponse response, OXException exception) throws IOException;
    
}
