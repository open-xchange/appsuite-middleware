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

package com.openexchange.login.internal.format;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.Strings;
import com.openexchange.login.Interface;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.session.Session;
import com.openexchange.user.User;

/**
 * {@link TokenFormatter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum TokenFormatter implements LoginFormatter {

    /**
     * The login formatter.
     */
    LOGIN("login", new LoginFormatter() {

        @Override
        public void formatLogin(final LoginRequest request, final LoginResult result, final StringBuilder sb) {
            sb.append("Login:");
            sb.append(Strings.abbreviate(request.getLogin(), 256));
        }

        @Override
        public void formatLogout(final LoginResult result, final StringBuilder sb) {
            // Nothing
        }
    }),
    /**
     * The IP formatter.
     */
    IP("ip", new LoginFormatter() {

        @Override
        public void formatLogin(final LoginRequest request, final LoginResult result, final StringBuilder sb) {
            sb.append("IP:");
            sb.append(request.getClientIP());
        }

        @Override
        public void formatLogout(final LoginResult result, final StringBuilder sb) {
            // Nothing
        }
    }),
    /**
     * The authentication ID formatter.
     */
    AUTH_ID("auth", new LoginFormatter() {

        @Override
        public void formatLogin(final LoginRequest request, final LoginResult result, final StringBuilder sb) {
            sb.append("AuthID:");
            sb.append(Strings.sanitizeString(request.getAuthId()));
        }

        @Override
        public void formatLogout(final LoginResult result, final StringBuilder sb) {
            // Nothing
        }
    }),
    /**
     * The User-Agent formatter.
     */
    AGENT("agent", new LoginFormatter() {

        @Override
        public void formatLogin(final LoginRequest request, final LoginResult result, final StringBuilder sb) {
            sb.append("Agent:");
            sb.append(Strings.sanitizeString(request.getUserAgent()));
        }

        @Override
        public void formatLogout(final LoginResult result, final StringBuilder sb) {
            // Nothing
        }
    }),
    /**
     * The client formatter.
     */
    CLIENT("client", new LoginFormatter() {

        @Override
        public void formatLogin(final LoginRequest request, final LoginResult result, final StringBuilder sb) {
            sb.append(" Client:");
            sb.append(Strings.sanitizeString(request.getClient()));
            sb.append('(');
            sb.append(Strings.sanitizeString(request.getVersion()));
            sb.append(')');
        }

        @Override
        public void formatLogout(final LoginResult result, final StringBuilder sb) {
            // Nothing
        }
    }),
    /**
     * The interface formatter.
     */
    INTERFACE("iface", new LoginFormatter() {

        @Override
        public void formatLogin(final LoginRequest request, final LoginResult result, final StringBuilder sb) {
            final Interface iface = request.getInterface();
            if (null != iface) {
                sb.append("Interface:");
                sb.append(iface.toString());
            }
        }

        @Override
        public void formatLogout(final LoginResult result, final StringBuilder sb) {
            // Nothing
        }
    }),
    /**
     * The context formatter.
     */
    CONTEXT("c", new LoginFormatter() {

        @Override
        public void formatLogin(final LoginRequest request, final LoginResult result, final StringBuilder sb) {
            formatLogout(result, sb);
        }

        @Override
        public void formatLogout(final LoginResult result, final StringBuilder sb) {
            final Context ctx = result.getContext();
            if (null != ctx) {
                sb.append(" Context:");
                sb.append(ctx.getContextId());
                sb.append('(');
                sb.append(Strings.join(ctx.getLoginInfo(), ","));
                sb.append(')');
            }
        }
    }),
    /**
     * The user formatter.
     */
    USER("u", new LoginFormatter() {

        @Override
        public void formatLogin(final LoginRequest request, final LoginResult result, final StringBuilder sb) {
            formatLogout(result, sb);
        }

        @Override
        public void formatLogout(final LoginResult result, final StringBuilder sb) {
            final User user = result.getUser();
            if (null != user) {
                sb.append(" User:");
                sb.append(user.getId());
                sb.append('(');
                sb.append(user.getLoginInfo());
                sb.append(')');
            }
        }
    }),
    /**
     * The session formatter.
     */
    SESSION("s", new LoginFormatter() {

        @Override
        public void formatLogin(final LoginRequest request, final LoginResult result, final StringBuilder sb) {
            final Session session = result.getSession();
            if (null == session) {
                sb.append(" No session created.");
            } else {
                sb.append(" Session:");
                sb.append(session.getSessionID());
                sb.append(" Random:");
                sb.append(session.getRandomToken());
                sb.append(" Transient:");
                sb.append(session.isTransient());
            }
        }

        @Override
        public void formatLogout(final LoginResult result, final StringBuilder sb) {
            final Session session = result.getSession();
            if (null != session) {
                sb.append(" Session:");
                sb.append(session.getSessionID());
            }
        }
    }),

    ;

    private final String token;
    private final LoginFormatter formatter;

    private TokenFormatter(final String token, final LoginFormatter formatter) {
        this.token = token;
        this.formatter = formatter;
    }

    @Override
    public void formatLogin(final LoginRequest request, final LoginResult result, final StringBuilder logBuilder) {
        formatter.formatLogin(request, result, logBuilder);
    }

    @Override
    public void formatLogout(final LoginResult result, final StringBuilder logBuilder) {
        formatter.formatLogout(result, logBuilder);
    }

    public static TokenFormatter tokenFormatterFor(final String token) {
        if (Strings.isEmpty(token)) {
            return null;
        }
        for (final TokenFormatter tf : TokenFormatter.values()) {
            if (tf.token.equals(token)) {
                return tf;
            }
        }
        return null;
    }

}
