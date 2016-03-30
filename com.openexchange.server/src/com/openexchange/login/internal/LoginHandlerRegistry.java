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
