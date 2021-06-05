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

import com.openexchange.proxy.ProxyRegistration;

/**
 * {@link ProxyRegistrationEntry} - A registration entry which wraps a {@link ProxyRegistration registration} and provides registration time
 * stamp and time-to-live.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ProxyRegistrationEntry {

    private final ProxyRegistration proxyRegistration;

    private final long ttl;

    private final long timestamp;

    /**
     * Initializes a new {@link ProxyRegistrationEntry} bound to session.
     *
     * @param proxyRegistration The proxy registration
     */
    public ProxyRegistrationEntry(final ProxyRegistration proxyRegistration) {
        this(proxyRegistration, -1L);
    }

    /**
     * Initializes a new {@link ProxyRegistrationEntry} with given time-to-live.
     *
     * @param proxyRegistration The proxy registration
     * @param ttl The time-to-live value; a negative value means bound to session life time.
     */
    public ProxyRegistrationEntry(final ProxyRegistration proxyRegistration, final long ttl) {
        super();
        this.ttl = ttl;
        this.proxyRegistration = proxyRegistration;
        timestamp = System.currentTimeMillis();
    }

    /**
     * Gets the time-to-live for this entry. A negative value means this entry is bound to associated session's life time.
     *
     * @return The time-to-live or a negative value
     */
    public long getTTL() {
        return ttl;
    }

    /**
     * Gets the proxy registration
     *
     * @return The proxy registration
     */
    public ProxyRegistration getProxyRegistration() {
        return proxyRegistration;
    }

    /**
     * Gets the time stamp when this entry was registered.
     *
     * @return The register time stamp
     */
    public long getTimestamp() {
        return timestamp;
    }

}
