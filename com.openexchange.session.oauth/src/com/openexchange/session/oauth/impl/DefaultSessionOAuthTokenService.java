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
 *    trademarks of the OX Software GmbH. group of companies.
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
        OAuthTokenUpdaterImpl updater = new OAuthTokenUpdaterImpl(session, refresher, refreshConfig, tokenGetterSetter, services);
        return updater.checkOrRefreshTokens();
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
