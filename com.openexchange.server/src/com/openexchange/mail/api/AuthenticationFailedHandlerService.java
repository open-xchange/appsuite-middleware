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

package com.openexchange.mail.api;

import com.openexchange.exception.OXException;
import com.openexchange.mail.api.AuthenticationFailedHandler.Service;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link AuthenticationFailedHandlerService} - The service collecting registered instances of {@link AuthenticationFailedHandler} for chained handling of possible failed authentication errors.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
@SingletonService
public interface AuthenticationFailedHandlerService {

    /**
     * Calls the currently available chain of {@link AuthenticationFailedHandler handlers}.
     *
     * @param failedAuthentication The optional {@code OXException} instance that reflects the failed authentication
     * @param service The type of service that yielded the failed authentication
     * @param mailConfig The effective mail configuration for affected user (providing host, port, credentials, auth type, etc.)
     * @param session The user which couln't be authenticated.
     * @return An {@link AuthenticationFailureHandlerResult}
     * @throws OXException If handling the failed authentication is supposed being aborted with an error
     */
    AuthenticationFailureHandlerResult handleAuthenticationFailed(OXException failedAuthentication, Service service, MailConfig mailConfig, Session session) throws OXException;

}
