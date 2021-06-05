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
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.session.Session;
import com.openexchange.user.User;


/**
 * {@link DefaultLoginFormatter} - The default {@link LoginFormatter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DefaultLoginFormatter implements LoginFormatter {

    private static final DefaultLoginFormatter INSTANCE = new DefaultLoginFormatter();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static DefaultLoginFormatter getInstance() {
        return INSTANCE;
    }

    // --------------------------------------------------------------------------------- //

    /**
     * Initializes a new {@link DefaultLoginFormatter}.
     */
    private DefaultLoginFormatter() {
        super();
    }

    @Override
    public void formatLogin(final LoginRequest request, final LoginResult result, final StringBuilder sb) {
        sb.append("Login:");
        sb.append(Strings.abbreviate(request.getLogin(), 256));
        sb.append(" IP:");
        sb.append(request.getClientIP());
        sb.append(" AuthID:");
        sb.append(request.getAuthId());
        sb.append(" Agent:");
        sb.append(request.getUserAgent());
        sb.append(" Client:");
        sb.append(request.getClient());
        sb.append('(');
        sb.append(request.getVersion());
        sb.append(") Interface:");
        sb.append(null == request.getInterface() ? null : request.getInterface().toString());
        final Context ctx = result.getContext();
        if (null != ctx) {
            sb.append(" Context:");
            sb.append(ctx.getContextId());
            sb.append('(');
            sb.append(Strings.join(ctx.getLoginInfo(), ","));
            sb.append(')');
        }
        final User user = result.getUser();
        if (null != user) {
            sb.append(" User:");
            sb.append(user.getId());
            sb.append('(');
            sb.append(user.getLoginInfo());
            sb.append(')');
        }
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
        sb.append("Logout ");
        final Context ctx = result.getContext();
        sb.append(" Context:");
        sb.append(ctx.getContextId());
        sb.append('(');
        sb.append(Strings.join(ctx.getLoginInfo(), ","));
        sb.append(')');
        final User user = result.getUser();
        sb.append(" User:");
        sb.append(user.getId());
        sb.append('(');
        sb.append(user.getLoginInfo());
        sb.append(')');
        final Session session = result.getSession();
        sb.append(" Session:");
        sb.append(session.getSessionID());
    }

}
