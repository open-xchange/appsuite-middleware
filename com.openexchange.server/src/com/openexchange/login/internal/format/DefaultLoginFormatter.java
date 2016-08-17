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

package com.openexchange.login.internal.format;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.session.Session;


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
