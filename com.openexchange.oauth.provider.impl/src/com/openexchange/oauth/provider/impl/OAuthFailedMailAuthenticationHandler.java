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

package com.openexchange.oauth.provider.impl;

import com.openexchange.exception.OXException;
import com.openexchange.mail.api.AuthType;
import com.openexchange.mail.api.AuthenticationFailedHandler;
import com.openexchange.mail.api.AuthenticationFailureHandlerResult;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.oauth.provider.exceptions.OAuthInvalidTokenException;
import com.openexchange.oauth.provider.exceptions.OAuthInvalidTokenException.Reason;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;

/**
 * {@link OAuthFailedMailAuthenticationHandler} - is an mail {@link AuthenticationFailedHandler} which checks whether an oauth based access failed and throws an appropriate error.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.5
 */
public class OAuthFailedMailAuthenticationHandler implements AuthenticationFailedHandler {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link OAuthFailedMailAuthenticationHandler}.
     */
    public OAuthFailedMailAuthenticationHandler(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public AuthenticationFailureHandlerResult handleAuthenticationFailed(OXException failedAuthentication, Service service, MailConfig mailConfig, Session session) throws OXException {
        if (!AuthType.isOAuthType(mailConfig.getAuthType())) {
            return AuthenticationFailureHandlerResult.createContinueResult();
        }

        if (!isOAuth(session)) {
            return AuthenticationFailureHandlerResult.createContinueResult();
        }

        SessiondService sessiondService = services.getServiceSafe(SessiondService.class);
        sessiondService.removeSession(session.getSessionID());
        return AuthenticationFailureHandlerResult.createErrorResult(new OAuthInvalidTokenException(Reason.TOKEN_EXPIRED));
    }

    /**
     * Checks whether the current session is an oauth based session
     *
     * @param session The users session
     * @return <code>true</code> if its an oauth session, <code>false</code> otherwise
     */
    private boolean isOAuth(Session session) {
        return session.containsParameter(Session.PARAM_IS_OAUTH);
    }


}
