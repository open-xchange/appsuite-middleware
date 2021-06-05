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

package com.openexchange.session.oauth;

import com.openexchange.exception.OXException;

/**
 * A {@link TokenRefresher} performs the actual action of obtaining a new
 * OAuth token pair. Use a new implementation instance for every refresh
 * attempt.
 * <p>
 * Implementors are supposed to carry any needed state as fields. For the actual
 * execution, only the current session-specific tokens are passed. During execution,
 * the per-session token refresh lock is held.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public interface TokenRefresher {

    /**
     * Executes the token refresh. Any exceptions are re-thrown from
     * {@link SessionOAuthTokenService#checkOrRefreshTokens(com.openexchange.session.Session, TokenRefresher, TokenRefreshConfig)}.
     *
     * @param currentTokens The current OAuth tokens from a passed session
     * @return A {@link TokenRefreshResponse}
     * @throws InterruptedException If thread was interrupted during a blocking operation
     * @throws OXException Use only for unexpected errors or errors that cannot be announced with the result object
     */
    TokenRefreshResponse execute(OAuthTokens currentTokens) throws InterruptedException, OXException;

}
