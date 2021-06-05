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

package com.openexchange.mail.oauth.internal;

import com.openexchange.exception.OXException;
import com.openexchange.mail.autoconfig.Autoconfig;
import com.openexchange.mail.oauth.MailOAuthExceptionCodes;
import com.openexchange.mail.oauth.MailOAuthProvider;
import com.openexchange.mail.oauth.MailOAuthService;
import com.openexchange.mail.oauth.TokenInfo;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthUtil;
import com.openexchange.oauth.scope.OXScope;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;


/**
 * {@link MailOAuthServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class MailOAuthServiceImpl implements MailOAuthService {

    private final MailOAuthProviderRegistry registry;
    private final ServiceLookup services;

    /**
     * Initializes a new {@link MailOAuthServiceImpl}.
     */
    public MailOAuthServiceImpl(MailOAuthProviderRegistry registry, ServiceLookup services) {
        super();
        this.registry = registry;
        this.services = services;
    }

    private void checkOAuthAccount(OAuthAccount oAuthAccount, Session session) throws OXException {
        // Check if mail is supported
        OAuthUtil.checkScopesAvailableAndEnabled(oAuthAccount, session.getUserId(), session.getContextId(), OXScope.mail);
    }

    @Override
    public Autoconfig getAutoconfigFor(int oauthAccountId, Session session) throws OXException {
        OAuthService oAuthService = services.getOptionalService(OAuthService.class);
        if (null == oAuthService) {
            throw ServiceExceptionCode.absentService(OAuthService.class);
        }

        // Get the OAuth account
        OAuthAccount oAuthAccount = oAuthService.getAccount(session, oauthAccountId);
        checkOAuthAccount(oAuthAccount, session);

        // Try to compose the auto-configuration
        MailOAuthProvider provider = registry.getProviderFor(oAuthAccount.getMetaData().getId());
        return null == provider ? null : provider.getAutoconfigFor(oAuthAccount, session);
    }

    @Override
    public TokenInfo getTokenFor(int oauthAccountId, Session session) throws OXException {
        OAuthService oAuthService = services.getOptionalService(OAuthService.class);
        if (null == oAuthService) {
            throw ServiceExceptionCode.absentService(OAuthService.class);
        }

        // Get the OAuth account
        OAuthAccount oAuthAccount = oAuthService.getAccount(session, oauthAccountId);
        checkOAuthAccount(oAuthAccount, session);

        String oauthServiceId = oAuthAccount.getMetaData().getId();
        MailOAuthProvider provider = registry.getProviderFor(oauthServiceId);
        if (provider == null) {
            throw MailOAuthExceptionCodes.NO_SUCH_MAIL_OAUTH_PROVIDER.create(oauthServiceId);
        }
        return provider.getTokenFor(oAuthAccount, session);
    }

}
