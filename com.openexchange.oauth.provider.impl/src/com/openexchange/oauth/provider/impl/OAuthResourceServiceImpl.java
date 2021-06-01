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

import javax.servlet.http.HttpServletRequest;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.authorizationserver.spi.AuthorizationException;
import com.openexchange.oauth.provider.authorizationserver.spi.OAuthAuthorizationService;
import com.openexchange.oauth.provider.authorizationserver.spi.ValidationResponse;
import com.openexchange.oauth.provider.exceptions.OAuthInvalidTokenException;
import com.openexchange.oauth.provider.exceptions.OAuthInvalidTokenException.Reason;
import com.openexchange.oauth.provider.exceptions.OAuthProviderExceptionCodes;
import com.openexchange.oauth.provider.resourceserver.OAuthAccess;
import com.openexchange.oauth.provider.resourceserver.OAuthResourceService;
import com.openexchange.oauth.provider.resourceserver.scope.Scope;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;


/**
 * {@link OAuthResourceServiceImpl}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class OAuthResourceServiceImpl implements OAuthResourceService {

    private final ServiceListing<OAuthAuthorizationService>  authServices;

    private final ServiceLookup serviceLookup;

    private final SessionProvider sessionProvider;

	public OAuthResourceServiceImpl(ServiceListing<OAuthAuthorizationService> authServices, ServiceLookup serviceLookup) {
        super();
        this.authServices = authServices;
        this.serviceLookup = serviceLookup;
        sessionProvider = new SessionProvider(serviceLookup);
    }

    @Override
    public OAuthAccess checkAccessToken(String accessToken, HttpServletRequest httpRequest) throws OXException {
        ValidationResponse response;
        try {
			response = authServices.getServiceList().get(0).validateAccessToken(accessToken);
        } catch (AuthorizationException e) {
            throw OAuthProviderExceptionCodes.UNEXPECTED_ERROR.create(e, "An error occurred while trying to validate an access token.");
        }

        switch (response.getTokenStatus()) {
            case MALFORMED:
                throw new OAuthInvalidTokenException(Reason.TOKEN_MALFORMED);
            case UNKNOWN:
                throw new OAuthInvalidTokenException(Reason.TOKEN_UNKNOWN);
            case EXPIRED:
                throw new OAuthInvalidTokenException(Reason.TOKEN_EXPIRED);
            case INVALID:
                throw new OAuthInvalidTokenException(Reason.TOKEN_INVALID);
            case VALID:
                Session session = sessionProvider.getSession(accessToken, response.getContextId(), response.getUserId(), response.getClientName(), httpRequest);
                return new OAuthAccessImpl(session, Scope.newInstance(response.getScope()));
            default:
                throw new OAuthInvalidTokenException(Reason.TOKEN_UNKNOWN);
        }
    }
    @Override
    public boolean isProviderEnabled(int contextId, int userId) throws OXException {
        return serviceLookup.getServiceSafe(LeanConfigurationService.class).getBooleanProperty(userId, contextId, OAuthProviderProperties.ENABLED);
    }

    private static final class OAuthAccessImpl implements OAuthAccess {

        private final Session session;

        private final Scope scope;

        public OAuthAccessImpl(Session session, Scope scope) {
            super();
            this.session = session;
            this.scope = scope;
        }

        @Override
        public Session getSession() {
            return session;
        }

        @Override
        public Scope getScope() {
            return scope;
        }

    }

}
