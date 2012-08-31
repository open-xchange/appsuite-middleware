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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.ajp13;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.servlet.http.HttpServletRequest;

/**
 * {@link AjpLongRunningRegistry} - A registry for long-running requests.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AjpLongRunningRegistry {

    private static final AjpLongRunningRegistry INSTANCE = new AjpLongRunningRegistry();

    /**
     * Gets the registry instance.
     *
     * @return The instance
     */
    public static AjpLongRunningRegistry getInstance() {
        return INSTANCE;
    }

    private static final Object PRESENT = new Object();

    private final ConcurrentMap<Key, Object> map;

    /**
     * Initializes a new {@link AjpLongRunningRegistry}.
     */
    public AjpLongRunningRegistry() {
        super();
        map = new ConcurrentHashMap<AjpLongRunningRegistry.Key, Object>();
    }

    /**
     * Tries to register a long-running request.
     *
     * @param request The long-running request
     * @return <code>true</code> if request could be registered; otherwise <code>false</code> if there is already a long-running request in
     *         progress
     */
    public boolean registerLongRunning(final HttpServletRequest request) {
        return (null == map.putIfAbsent(new Key(request.getParameter("User"), request.getRemoteAddr(), request.getRemotePort()), PRESENT));
    }

    /**
     * De-Registers specified long-running request.
     *
     * @param request The long-running request
     */
    public void deregisterLongRunning(final HttpServletRequest request) {
        map.remove(new Key(request.getParameter("User"), request.getRemoteAddr(), request.getRemotePort()));
    }

    private static final class Key {

        private final int port;

        private final String host;

        private final String user;

        private final int hash;

        public Key(final String user, final String host, final int port) {
            super();
            this.user = user;
            this.host = host;
            this.port = port;
            final int prime = 31;
            int result = 1;
            result = prime * result + (null == host ? 0 : host.hashCode());
            result = prime * result + (null == user ? 0 : user.hashCode());
            result = prime * result + port;
            hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Key)) {
                return false;
            }
            final Key other = (Key) obj;
            if (null == user) {
                if (null != other.user) {
                    return false;
                }
            } else if (!user.equals(other.user)) {
                return false;
            }
            if (null == host) {
                if (null != other.host) {
                    return false;
                }
            } else if (!host.equals(other.host)) {
                return false;
            }
            if (port != other.port) {
                return false;
            }
            return true;
        }

    } // End of class Key
}
