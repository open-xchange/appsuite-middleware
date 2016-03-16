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

package com.openexchange.proxy.servlet;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.exception.OXException;
import com.openexchange.proxy.ProxyExceptionCodes;
import com.openexchange.proxy.ProxyRegistration;
import com.openexchange.proxy.ProxyRegistry;
import com.openexchange.proxy.servlet.services.ServiceRegistry;
import com.openexchange.sessiond.SessiondService;

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

    private final ConcurrentMap<String, ConcurrentMap<UUID, ProxyRegistrationEntry>> registry;

    /**
     * Initializes a new {@link ProxyRegistryImpl}.
     */
    private ProxyRegistryImpl() {
        super();
        registry = new ConcurrentHashMap<String, ConcurrentMap<UUID, ProxyRegistrationEntry>>();
    }

    @Override
    public URI register(final ProxyRegistration registration) throws OXException {
        final String sessionId = registration.getSessionId();
        /*
         * Check session identifier
         */
        final SessiondService sessiondService = ServiceRegistry.getInstance().getService(SessiondService.class, true);
        if (null == sessiondService.getSession(sessionId)) {
            throw ProxyExceptionCodes.INVALID_SESSION_ID.create(sessionId);
        }
        /*
         * Register in appropriate map
         */
        ConcurrentMap<UUID, ProxyRegistrationEntry> map = registry.get(sessionId);
        if (null == map) {
            final ConcurrentMap<UUID, ProxyRegistrationEntry> newmap = new ConcurrentHashMap<UUID, ProxyRegistrationEntry>();
            map = registry.putIfAbsent(sessionId, newmap);
            if (null == map) {
                map = newmap;
            }
        }
        /*
         * Generate UUID for registration
         */
        final UUID uuid = UUID.randomUUID();
        map.put(uuid, new ProxyRegistrationEntry(registration));
        /*
         * Generate URI
         */
        final String uriStr =
            new StringBuilder(Constants.PATH).append('?').append(AJAXServlet.PARAMETER_SESSION).append('=').append(sessionId).append('&').append(
                AJAXServlet.PARAMETER_UID).append('=').append(UUIDs.getUnformattedString(uuid)).toString();
        try {
            return new URI(uriStr);
        } catch (final URISyntaxException e) {
            throw ProxyExceptionCodes.MALFORMED_URI.create(uriStr);
        }
    }

    /**
     * Gets the registration for specified session identifier and UUID.
     *
     * @param sessionId The session identifier
     * @param uuid The UUID
     * @return The registration or <code>null</code>
     */
    public ProxyRegistration getRegistration(final String sessionId, final UUID uuid) {
        final ConcurrentMap<UUID, ProxyRegistrationEntry> map = registry.get(sessionId);
        if (null == map) {
            return null;
        }
        final ProxyRegistrationEntry entry = map.get(uuid);
        return null == entry ? null : entry.getProxyRegistration();
    }

    /**
     * Gets the values contained in this registry.
     *
     * @return The values contained in this registry
     */
    public Collection<ConcurrentMap<UUID, ProxyRegistrationEntry>> values() {
        return registry.values();
    }

    /**
     * Gets an iterator for contained session identifiers.
     *
     * @return An iterator for contained session identifiers
     */
    public Iterator<String> sessionIds() {
        return registry.keySet().iterator();
    }

    /**
     * Drops all registrations associated with specified session identifier.
     *
     * @param sessionId The session identifier
     */
    public void dropRegistrationsFor(final String sessionId) {
        registry.remove(sessionId);
    }

}
