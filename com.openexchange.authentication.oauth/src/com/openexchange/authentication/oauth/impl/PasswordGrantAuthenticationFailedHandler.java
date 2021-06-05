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

import com.openexchange.exception.OXException;
import com.openexchange.mail.api.AuthType;
import com.openexchange.mail.api.AuthenticationFailedHandler;
import com.openexchange.mail.api.AuthenticationFailureHandlerResult;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.session.Session;


/**
 * {@link PasswordGrantAuthenticationFailedHandler}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public class PasswordGrantAuthenticationFailedHandler implements AuthenticationFailedHandler {

    public PasswordGrantAuthenticationFailedHandler() {
        super();
    }

    @Override
    public AuthenticationFailureHandlerResult handleAuthenticationFailed(OXException failedAuthentication, Service service, MailConfig mailConfig, Session session) throws OXException {
        if (AuthType.isOAuthType(mailConfig.getAuthType()) && Boolean.TRUE.equals(session.getParameter(SessionParameters.PASSWORD_GRANT_MARKER))) {
            // re-throw; let SessionInspector handle eager refresh alone
            return AuthenticationFailureHandlerResult.createErrorResult(failedAuthentication);
        }

        return AuthenticationFailureHandlerResult.createContinueResult();
    }

}
