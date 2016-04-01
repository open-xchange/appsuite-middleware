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
                return sessionProvider.createSession(grant);
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

    public static interface SessionProvider {
        Session createSession(TestGrant grant);
    }

}
