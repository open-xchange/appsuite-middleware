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

package com.openexchange.session.oauth;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;
import com.openexchange.sessionstorage.SessionStorageService;

/**
 * {@link SessionOAuthTokenService}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
@SingletonService
public interface SessionOAuthTokenService {

    /**
     * Checks whether the sessions oauth access token is expired or will expire within the
     * given number of threshold seconds. If so, the passed token refresher will be used to
     * obtain a fresh token set and store it within the session.
     * <p>
     * This method is thread safe and operates atomically on any token-related session
     * parameters. In case of an invalid token, it will call {@link TokenRefresher#execute(OAuthTokens)}.
     * This usually results in blocking remote calls, so consider carefully when to call this method.
     * <p>
     * In case of a successful refresh, the session contains up to date token parameters when
     * this call returns. Also it was triggered to update the session in {@link SessionStorageService}.
     * <p>
     * In case of an unsuccessful refresh ({@link RefreshResult.FailReason#INVALID_REFRESH_TOKEN}), any
     * oauth token parameters are removed from the session when this call returns.
     *
     * @param session The session
     * @param refresher The {@link TokenRefresher}
     * @param refreshConfig The token refresh configuration
     * @return {@link RefreshResult}
     * @throws InterruptedException If thread was interrupted while blocking
     * @throws OXException Any unexpected error re-thrown from {@link TokenRefresher#execute(OAuthTokens)}
     */
    RefreshResult checkOrRefreshTokens(Session session, TokenRefresher refresher, TokenRefreshConfig refreshConfig) throws InterruptedException, OXException;

    /**
     * Atomically gets {@link OAuthTokens} from a session. The different token attributes
     * (access token, expiry date, refresh token), which are internally managed as session
     * parameters, are guaranteed to form a consistent unit.
     * <p>
     * <em>Important:<em> Mutual exclusivity can only be guaranteed if OAuth session parameters
     * are always set/removed through according methods of this service!
     *
     * @param session The session
     * @return An optional {@link OAuthTokens} instance. If no valid token parameters where
     *         set in the session, the optional is empty.
     * @throws InterruptedException If thread is interrupted while trying to obtain a lock.
     */
    Optional<OAuthTokens> getFromSessionAtomic(Session session) throws InterruptedException;

    /**
     * Atomically sets {@link OAuthTokens} in a session. The different token attributes
     * (access token, expiry date, refresh token), which are internally managed as session
     * parameters, are set atomically within a locked transaction.
     *
     * @param session The session
     * @param tokens
     * @throws InterruptedException If thread is interrupted while trying to obtain a lock.
     */
    void setInSessionAtomic(Session session, OAuthTokens tokens) throws InterruptedException;

    /**
     * Atomically removes {@link OAuthTokens} in a session. The different token attributes
     * (access token, expiry date, refresh token), which are internally managed as session
     * parameters, are removed atomically within a locked transaction.
     *
     * @param session The session
     * @throws InterruptedException If thread is interrupted while trying to obtain a lock.
     */
    void removeFromSessionAtomic(Session session) throws InterruptedException;

    /**
     * Atomically gets {@link OAuthTokens} from a session. The different token attributes
     * (access token, expiry date, refresh token), which are internally managed as session
     * parameters, are guaranteed to form a consistent unit.
     * <p>
     * <em>Important:<em> Mutual exclusivity can only be guaranteed if OAuth session parameters
     * are always set/removed through according methods of this service!
     *
     * @param session The session
     * @param timeout The max. time to wait for obtaining the token lock
     * @param unit The lock timeout unit
     * @return An optional {@link OAuthTokens} instance. If no valid token parameters where
     *         set in the session, the optional is empty.
     * @throws TimeoutException If grant hasn't been acquired in time
     * @throws InterruptedException If thread is interrupted while trying to obtain a lock.
     */
    Optional<OAuthTokens> getFromSessionAtomic(Session session, long timeout, TimeUnit unit) throws TimeoutException, InterruptedException;

    /**
     * Atomically sets {@link OAuthTokens} in a session. The different token attributes
     * (access token, expiry date, refresh token), which are internally managed as session
     * parameters, are set atomically within a locked transaction.
     *
     * @param session The session
     * @param tokens The tokens to set
     * @param timeout The max. time to wait for obtaining the token lock
     * @param unit The lock timeout unit
     * @throws TimeoutException If grant hasn't been acquired in time
     * @throws InterruptedException If thread is interrupted while trying to obtain a lock.
     */
    void setInSessionAtomic(Session session, OAuthTokens tokens, long timeout, TimeUnit unit) throws TimeoutException, InterruptedException;

    /**
     * Atomically removes {@link OAuthTokens} in a session. The different token attributes
     * (access token, expiry date, refresh token), which are internally managed as session
     * parameters, are removed atomically within a locked transaction.
     *
     * @param session The session
     * @param timeout The max. time to wait for obtaining the token lock
     * @param unit The lock timeout unit
     * @throws TimeoutException If grant hasn't been acquired in time
     * @throws InterruptedException If thread is interrupted while trying to obtain a lock.
     */
    void removeFromSessionAtomic(Session session, long timeout, TimeUnit unit) throws TimeoutException, InterruptedException;

    /**
     * Gets {@link OAuthTokens} from a session. The different token attributes
     * (access token, expiry date, refresh token), which are internally managed as session
     * parameters, are fetched one after another without any locking. Thus it might be,
     * that access token, expiry date and refresh token don't "belong" to each other,
     * if another thread is updating them concurrently.
     *
     * @param session The session
     * @return An optional {@link OAuthTokens} instance. If no valid token parameters where
     *         set in the session, the optional is empty.
     */
    Optional<OAuthTokens> getFromSession(Session session);

    /**
     * Sets {@link OAuthTokens} in a session. The different token attributes
     * (access token, expiry date, refresh token), which are internally managed as session
     * parameters, are set one after another without any locking. Thus it might be,
     * that two concurrent attempts leave the attributes in an inconsistent state.
     *
     * @param session The session
     * @param tokens The tokens to set
     */
    void setInSession(Session session, OAuthTokens tokens);

    /**
     * Removes {@link OAuthTokens} from a session. The different token attributes
     * (access token, expiry date, refresh token), which are internally managed as session
     * parameters, are unset one after another without any locking. Thus it might be,
     * that concurrent get or set attempts leave return an inconsistent state.
     *
     * @param session The session
     */
    void removeFromSession(Session session);

}
