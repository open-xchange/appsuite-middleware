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
import com.openexchange.session.Session;

/**
 * {@link AuthenticationFailedHandler} - Handles failed authentications that occurred while attempting to connect/log-in to a certain service.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public interface AuthenticationFailedHandler {

    /** The type of service that yielded the failed authentication */
    public static enum Service {
        /**
         * The mail access service; e.g. IMAP
         */
        MAIL,
        /**
         * The mail transport service; e.g. SMTP
         */
        TRANSPORT,
        /**
         * The mail filter service; e.g. SIEVE
         */
        MAIL_FILTER;
    }

    /**
     * This method is called in case the authentication has failed.
     *
     * @param failedAuthentication The optional {@code OXException} instance that reflects the failed authentication
     * @param service The type of service that yielded the failed authentication
     * @param mailConfig The effective mail configuration for affected user
     * @param session The user which couln't be authenticated.
     * @return The result that controls whether to proceed in invocation chain
     * @throws OXException If handling the failed authentication is supposed being aborted with an error
     */
    AuthenticationFailureHandlerResult handleAuthenticationFailed(OXException failedAuthentication, Service service, MailConfig mailConfig, Session session) throws OXException;

}
