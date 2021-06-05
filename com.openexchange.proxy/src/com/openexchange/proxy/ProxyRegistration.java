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

package com.openexchange.proxy;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * {@link ProxyRegistration} - A registration providing URL, session identifier and list of restrictions bound to URL.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ProxyRegistration {

    private final URL url;
    private final String sessionId;
    private final Collection<Restriction> restrictions;

    /**
     * Initializes a new {@link ProxyRegistration}.
     *
     * @param url The URL
     * @param sessionId The identifier of the session associated with this registration
     * @param restrictions The restrictions
     */
    public ProxyRegistration(final URL url, final String sessionId, final Collection<Restriction> restrictions) {
        super();
        this.url = url;
        this.sessionId = sessionId;
        this.restrictions = restrictions == null ? Collections.<Restriction> emptyList() : Collections.unmodifiableCollection(restrictions);
    }

    /**
     * Initializes a new {@link ProxyRegistration}.
     *
     * @param url The URL
     * @param sessionId The identifier of the session associated with this registration
     * @param restrictions The restrictions
     */
    public ProxyRegistration(final URL url, final String sessionId, final Restriction... restrictions) {
        super();
        this.url = url;
        this.sessionId = sessionId;
        this.restrictions =
            restrictions == null || restrictions.length == 0 ? Collections.<Restriction> emptyList() : Collections.unmodifiableCollection(Arrays.asList(restrictions));
    }

    /**
     * Gets the URL to proxy.
     *
     * @return The URL
     */
    public URL getURL() {
        return url;
    }

    /**
     * Gets the identifier of the session associated with this registration.
     *
     * @return The identifier of the session associated with this registration
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Gets the restrictions.
     *
     * @return The restrictions
     */
    public Collection<Restriction> getRestrictions() {
        return Collections.unmodifiableCollection(restrictions);
    }

}
