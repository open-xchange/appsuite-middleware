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


package com.openexchange.passwordchange;

import java.util.List;
import java.util.Map;
import com.openexchange.authentication.Cookie;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;

/**
 * {@link PasswordChangeEvent} - Event for password change containing the session of the user whose password shall be changed, the context,
 * the new password, and the old password (needed for verification)
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PasswordChangeEvent {

    private final Session session;
    private final Context ctx;
    private final String newPassword;
    private final String oldPassword;
    private final Map<String, List<String>> headers;
    private final Cookie[] cookies;
    private final String ip;

    /**
     * Initializes a new {@link PasswordChangeEvent}
     *
     * @param session The session of the user whose password shall be changed
     * @param ctx The context
     * @param newPassword The new password
     * @param oldPassword The old password (needed for verification)
     */
    public PasswordChangeEvent(final Session session, final Context ctx, final String newPassword, final String oldPassword) {
        this(session, ctx, newPassword, oldPassword, null, null, null);
    }

    public PasswordChangeEvent(Session session, Context ctx, String newPassword, String oldPassword, Map<String, List<String>> headers, Cookie[] cookies, String ip) {
        super();
        this.session = session;
        this.ctx = ctx;
        this.newPassword = newPassword;
        this.oldPassword = oldPassword;
        this.headers = headers;
        this.cookies = cookies;
        this.ip = ip;
    }

    /**
     * Gets the associated cookies
     *
     * @return The cookies or <code>null</code>
     */
    public Cookie[] getCookies() {
        return cookies;
    }

    /**
     * Gets the associated headers
     *
     * @return The headers or <code>null</code>
     */
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    /**
     * @return The session
     */
    public Session getSession() {
        return session;
    }

    /**
     * @return The context
     */
    public Context getContext() {
        return ctx;
    }

    /**
     * @return The new password
     */
    public String getNewPassword() {
        return newPassword;
    }

    /**
     * @return The old password
     */
    public String getOldPassword() {
        return oldPassword;
    }
    
    /**
     * Get the IP address of the client changing the password
     * 
     * @return The IP address or <code>null</code>
     */
    public String getIpAddress() {
        return ip;
    }
}
