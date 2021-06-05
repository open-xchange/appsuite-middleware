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
        } catch (URISyntaxException e) {
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
