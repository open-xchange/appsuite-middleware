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

package com.openexchange.oauth.provider.resourceserver;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.exceptions.OAuthInvalidTokenException;
import com.openexchange.oauth.provider.exceptions.OAuthInvalidTokenException.Reason;
import com.openexchange.oauth.provider.resourceserver.scope.Scope;
import com.openexchange.session.Session;
import com.openexchange.session.SimSession;


/**
 * {@link SimOAuthResourceService}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class SimOAuthResourceService implements OAuthResourceService {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("[\\x41-\\x5a\\x61-\\x7a\\x30-\\x39-._~+/]+=*");

    private final Map<String, TestGrant> tokens = new HashMap<>();

    private final SessionProvider sessionProvider;

    public SimOAuthResourceService() {
        this(new SessionProvider() {
            @Override
            public Session createSession(TestGrant grant) {
                return new SimSession(grant.getUserId(), grant.getContextId());
            }
        });
    }

    public SimOAuthResourceService(SessionProvider sessionProvider) {
        super();
        this.sessionProvider = sessionProvider;
    }

    @Override
    public OAuthAccess checkAccessToken(String accessToken, HttpServletRequest httpRequest) throws OXException {
        if (!TOKEN_PATTERN.matcher(accessToken).matches()) {
            throw new OAuthInvalidTokenException(Reason.TOKEN_MALFORMED);
        }

        final TestGrant grant = tokens.get(accessToken);
        if (grant == null) {
            throw new OAuthInvalidTokenException(Reason.TOKEN_UNKNOWN);
        }

        if (new Date().after(grant.getExpirationDate())) {
            throw new OAuthInvalidTokenException(Reason.TOKEN_EXPIRED);
        }

        return new OAuthAccess() {

            @Override
            public Session getSession() {
                return getSessionProvider().createSession(grant);
            }

            @Override
            public Scope getScope() {
                return grant.getScope();
            }
        };
    }

    @Override
    public boolean isProviderEnabled(int contextId, int userId) {
        return true;
    }

    public static class TestGrant {

        private final int contextId;
        private final int userId;
        private final String accessToken;
        private final String refreshToken;
        private final Date expirationDate;
        private final Scope scope;

        public TestGrant(int contextId, int userId, String accessToken, String refreshToken, Date expirationDate, Scope scope) {
            super();
            this.contextId = contextId;
            this.userId = userId;
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.expirationDate = expirationDate;
            this.scope = scope;
        }

        public String getClientName() {
            return "TestClient";
        }

        public int getContextId() {
            return contextId;
        }

        public int getUserId() {
            return userId;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public Date getExpirationDate() {
            return expirationDate;
        }

        public Scope getScope() {
            return scope;
        }

    }

    public void addToken(TestGrant grant) {
        tokens.put(grant.getAccessToken(), grant);
    }

    public void removeToken(String accessToken) {
        tokens.remove(accessToken);
    }

    /**
     * Gets the sessionProvider
     *
     * @return The sessionProvider
     */
    public SessionProvider getSessionProvider() {
        return sessionProvider;
    }

    public static interface SessionProvider {

        Session createSession(TestGrant grant);
    }

}
