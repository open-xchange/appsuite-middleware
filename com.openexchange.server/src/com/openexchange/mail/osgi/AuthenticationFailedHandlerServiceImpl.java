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

package com.openexchange.mail.osgi;

import org.osgi.framework.BundleContext;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.AuthenticationFailedHandler;
import com.openexchange.mail.api.AuthenticationFailedHandler.Service;
import com.openexchange.mail.api.AuthenticationFailedHandlerService;
import com.openexchange.mail.api.AuthenticationFailureHandlerResult;
import com.openexchange.mail.api.AuthenticationFailureHandlerResult.Type;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.session.Session;

/**
 * {@link AuthenticationFailedHandlerServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class AuthenticationFailedHandlerServiceImpl extends RankingAwareNearRegistryServiceTracker<AuthenticationFailedHandler> implements AuthenticationFailedHandlerService {

    /** Simply go ahead with original processing (that is to throw originating authentication error). */
    private static final Type CONTINUE = AuthenticationFailureHandlerResult.Type.CONTINUE;

    /**
     * Initializes a new {@link AuthenticationFailedHandlerServiceImpl}.
     */
    public AuthenticationFailedHandlerServiceImpl(BundleContext context) {
        super(context, AuthenticationFailedHandler.class);
    }

    @Override
    public AuthenticationFailureHandlerResult handleAuthenticationFailed(OXException failedAuthentication, Service service, MailConfig mailConfig, Session session) throws OXException {
        AuthenticationFailureHandlerResult result;
        for (AuthenticationFailedHandler handler : this) {
            result = handler.handleAuthenticationFailed(failedAuthentication, service, mailConfig, session);
            if (!CONTINUE.equals(result.getType())) {
                return result;
            }
        }

        return AuthenticationFailureHandlerResult.createErrorResult(failedAuthentication);
    }

}
