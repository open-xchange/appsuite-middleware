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

package com.openexchange.session.oauth.impl;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.session.oauth.OAuthTokens;
import com.openexchange.session.oauth.RefreshResult;
import com.openexchange.session.oauth.SessionOAuthTokenService;
import com.openexchange.session.oauth.TokenRefreshConfig;
import com.openexchange.session.oauth.TokenRefresher;


/**
 * {@link DefaultSessionOAuthTokenService}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public class DefaultSessionOAuthTokenService implements SessionOAuthTokenService {

    private final ServiceLookup services;
    private final OAuthTokensGetterSetter tokenGetterSetter;

    public DefaultSessionOAuthTokenService(ServiceLookup services) {
        super();
        this.services = services;
        tokenGetterSetter = new OAuthTokensGetterSetter(services);
    }

    @Override
    public RefreshResult checkOrRefreshTokens(Session session, TokenRefresher refresher, TokenRefreshConfig refreshConfig) throws InterruptedException, OXException {
        return new OAuthTokenUpdaterImpl(session, refresher, refreshConfig, tokenGetterSetter, services).checkOrRefreshTokens();
    }

    @Override
    public Optional<OAuthTokens> getFromSessionAtomic(Session session) throws InterruptedException {
        return tokenGetterSetter.getFromSessionAtomic(session);
    }

    @Override
    public void setInSessionAtomic(Session session, OAuthTokens tokens) throws InterruptedException {
        tokenGetterSetter.setInSessionAtomic(session, tokens);
    }

    @Override
    public void removeFromSessionAtomic(Session session) throws InterruptedException {
        tokenGetterSetter.removeFromSessionAtomic(session);
    }

    @Override
    public Optional<OAuthTokens> getFromSessionAtomic(Session session, long timeout, TimeUnit unit) throws TimeoutException, InterruptedException {
        return tokenGetterSetter.getFromSessionAtomic(session, timeout, unit);
    }

    @Override
    public void setInSessionAtomic(Session session, OAuthTokens tokens, long timeout, TimeUnit unit) throws TimeoutException, InterruptedException {
        tokenGetterSetter.setInSessionAtomic(session, tokens, timeout, unit);
    }

    @Override
    public void removeFromSessionAtomic(Session session, long timeout, TimeUnit unit) throws TimeoutException, InterruptedException {
        tokenGetterSetter.removeFromSessionAtomic(session, timeout, unit);
    }

    @Override
    public Optional<OAuthTokens> getFromSession(Session session) {
        return tokenGetterSetter.getFromSession(session);
    }

    @Override
    public void setInSession(Session session, OAuthTokens tokens) {
        tokenGetterSetter.setInSession(session, tokens);
    }

    @Override
    public void removeFromSession(Session session) {
        tokenGetterSetter.removeFromSession(session);
    }

}
