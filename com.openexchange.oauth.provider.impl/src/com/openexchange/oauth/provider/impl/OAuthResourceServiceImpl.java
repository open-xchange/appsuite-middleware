/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.oauth.provider.impl;

import static com.openexchange.osgi.Tools.requireService;
import javax.servlet.http.HttpServletRequest;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
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
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;


/**
 * {@link OAuthResourceServiceImpl}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class OAuthResourceServiceImpl implements OAuthResourceService {

    private final OAuthAuthorizationService authService;

    private final ServiceLookup serviceLookup;

    private final SessionProvider sessionProvider;

    public OAuthResourceServiceImpl(OAuthAuthorizationService authService, ServiceLookup serviceLookup) {
        super();
        this.authService = authService;
        this.serviceLookup = serviceLookup;
        sessionProvider = new SessionProvider(serviceLookup);
    }

    @Override
    public OAuthAccess checkAccessToken(String accessToken, HttpServletRequest httpRequest) throws OXException {
        ValidationResponse response;
        try {
            response = authService.validateAccessToken(accessToken);
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
            case VALID:
                Session session = sessionProvider.getSession(accessToken, response.getContextId(), response.getUserId(), response.getClientName(), httpRequest);
                return new OAuthAccessImpl(session, Scope.newInstance(response.getScope()));
            default:
                throw new OAuthInvalidTokenException(Reason.TOKEN_UNKNOWN);
        }
    }

    @Override
    public boolean isProviderEnabled(int contextId, int userId) throws OXException {
        ConfigView configView = requireService(ConfigViewFactory.class, serviceLookup).getView(userId, contextId);
        return configView.opt(OAuthProviderProperties.ENABLED, Boolean.class, Boolean.TRUE).booleanValue();
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
