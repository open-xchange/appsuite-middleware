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

package com.openexchange.session;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.user.User;

/**
 * {@link ThreadLocalSessionHolder} - The session holder using a {@link ThreadLocal} instance.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ThreadLocalSessionHolder implements SessionHolderExtended {

    private static final ThreadLocalSessionHolder INSTANCE = new ThreadLocalSessionHolder();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static ThreadLocalSessionHolder getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------------------------------------------------------------------------

    private final ThreadLocal<UserContextSession> session;

    /**
     * Initializes a new {@link ThreadLocalSessionHolder}.
     */
    private ThreadLocalSessionHolder() {
        super();
        session = new ThreadLocal<UserContextSession>();
    }

    /**
     * Sets the specified <tt>Session</tt> instance.
     *
     * @param serverSession The <tt>Session</tt> instance
     */
    public void setSession(final UserContextSession serverSession) {
        session.set(serverSession);
    }

    public void clear() {
        session.remove();
    }

    @Override
    public Context getContext() {
        return getSessionObject().getContext();
    }

    @Override
    public Session optSessionObject() {
        return session.get();
    }

    @Override
    public UserContextSession getSessionObject() {
        return session.get();
    }

    @Override
    public User getUser() {
        return getSessionObject().getUser();
    }

}
