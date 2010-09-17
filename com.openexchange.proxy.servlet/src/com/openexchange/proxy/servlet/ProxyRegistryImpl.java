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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.proxy.servlet;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.proxy.ProxyException;
import com.openexchange.proxy.ProxyExceptionCodes;
import com.openexchange.proxy.ProxyRegistration;
import com.openexchange.proxy.ProxyRegistry;

/**
 * {@link ProxyRegistryImpl} - The servlet implementation of {@link ProxyRegistry}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ProxyRegistryImpl implements ProxyRegistry {

    private static final ProxyRegistryImpl INSTANCE = new ProxyRegistryImpl();

    /**
     * Gets the instance.
     * 
     * @return The instance
     */
    public static ProxyRegistryImpl getInstance() {
        return INSTANCE;
    }

    private final ConcurrentMap<String, ConcurrentMap<UUID, ProxyRegistration>> registry;

    /**
     * Initializes a new {@link ProxyRegistryImpl}.
     */
    private ProxyRegistryImpl() {
        super();
        registry = new ConcurrentHashMap<String, ConcurrentMap<UUID, ProxyRegistration>>();
    }

    public URI register(final ProxyRegistration registration) throws ProxyException {
        /*
         * Register
         */
        final String sessionId = registration.getSession().getSessionID();
        ConcurrentMap<UUID, ProxyRegistration> map = registry.get(sessionId);
        if (null == map) {
            final ConcurrentMap<UUID, ProxyRegistration> newmap = new ConcurrentHashMap<UUID, ProxyRegistration>();
            map = registry.putIfAbsent(sessionId, newmap);
            if (null == map) {
                map = newmap;
            }
        }
        /*
         * Generate UUID for registration
         */
        final UUID uuid = UUID.randomUUID();
        map.put(uuid, registration);
        /*
         * Generate URI
         */
        final String uriStr =
            new StringBuilder(Constants.PATH).append('?').append(AJAXServlet.PARAMETER_SESSION).append('=').append(sessionId).append('&').append(
                AJAXServlet.PARAMETER_UID).append('=').append(uuid.toString()).toString();
        try {
            return new URI(uriStr);
        } catch (URISyntaxException e) {
            throw ProxyExceptionCodes.MALFORMED_URI.create(uriStr);
        }
    }

    /**
     * Gets the registration for specified session ID and UUID.
     * 
     * @param sessionId The session ID
     * @param uuid The UUID
     * @return The registration or <code>null</code>
     */
    public ProxyRegistration getRegistration(final String sessionId, final UUID uuid) {
        final ConcurrentMap<UUID, ProxyRegistration> map = registry.get(sessionId);
        if (null == map) {
            return null;
        }
        return map.get(uuid);
    }

    /**
     * Drops all registrations associated with specified session ID.
     * 
     * @param sessionId The session ID
     */
    public void dropRegistrationsFor(final String sessionId) {
        registry.remove(sessionId);
    }

}
