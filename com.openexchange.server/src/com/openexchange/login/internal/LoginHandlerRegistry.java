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

package com.openexchange.login.internal;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.login.LoginHandlerService;

/**
 * {@link LoginHandlerRegistry} - The registry for login handlers.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class LoginHandlerRegistry {

    private static volatile LoginHandlerRegistry instance;

    /**
     * Gets the login performer instance.
     *
     * @return The login performer instance.
     */
    public static LoginHandlerRegistry getInstance() {
        LoginHandlerRegistry tmp = instance;
        if (null == tmp) {
            synchronized (LoginHandlerRegistry.class) {
                tmp = instance;
                if (tmp == null) {
                    instance = tmp = new LoginHandlerRegistry();
                }
            }
        }
        return tmp;
    }

    /**
     * Releases the login performer instance.
     */
    public static void releaseInstance() {
        LoginHandlerRegistry tmp = instance;
        if (null != tmp) {
            synchronized (LoginHandlerRegistry.class) {
                tmp = instance;
                if (tmp != null) {
                    instance = null;
                }
            }
        }
    }

    private final Map<Class<? extends LoginHandlerService>, LoginHandlerService> handlers;

    /**
     * Initializes a new {@link LoginHandlerRegistry}.
     */
    private LoginHandlerRegistry() {
        super();
        handlers = new ConcurrentHashMap<Class<? extends LoginHandlerService>, LoginHandlerService>(4, 0.9f, 1);
    }

    /**
     * Adds specified login handler.
     *
     * @param loginHandler The login handler to add
     * @return <code>true</code> if login handler could be successfully added; otherwise <code>false</code>
     */
    public boolean addLoginHandler(final LoginHandlerService loginHandler) {
        final Class<? extends LoginHandlerService> clazz = loginHandler.getClass();
        if (handlers.containsKey(clazz)) {
            return false;
        }
        handlers.put(clazz, loginHandler);
        return true;
    }

    /**
     * Removes specified login handler.
     *
     * @param loginHandler The login handler to remove
     */
    public void removeLoginHandler(final LoginHandlerService loginHandler) {
        handlers.remove(loginHandler.getClass());
    }

    /**
     * Gets a read-only {@link Iterator iterator} over the login handlers in this registry.
     * <p>
     * Invoking {@link Iterator#remove() remove} will throw an {@link UnsupportedOperationException}.
     *
     * @return A read-only {@link Iterator iterator} over the login handlers in this registry.
     */
    public Iterator<LoginHandlerService> getLoginHandlers() {
        return Collections.unmodifiableCollection(handlers.values()).iterator();
    }
}
