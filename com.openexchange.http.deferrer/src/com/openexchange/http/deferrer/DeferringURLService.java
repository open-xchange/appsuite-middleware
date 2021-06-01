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

package com.openexchange.http.deferrer;

import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link DeferringURLService} - The service to create a deferring URL.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
public interface DeferringURLService {

    /**
     * Generates a deferred URL for specified URL. Useful for a multi-domain setup to allow certain operations to jump to an extra step in a
     * singular domain; e.g. certain OAuth provider require a single domain for call-back actions.
     * <p>
     * If a single domain is configured through <code>com.openexchange.http.deferrer.url</code> property (<i>deferrer.properties</i>), the
     * resulting URL looks like:
     * <p>
     * &lt;deferrer-url&gt; + <code>"ajax/defer?redirect="</code> + <i>URLENC</i>(&lt;url&gt;)
     * <p>
     * If no such property is set, passed URL is returned unchanged
     *
     * @param url The URL to defer
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The deferred URL
     */
    String getDeferredURL(String url, int userId, int contextId);

    /**
     * Generates a deferred URL for specified URL using given <code>domain</code>.
     *
     * @param url The URL to defer
     * @param domain The singular domain to use
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The deferred URL
     */
    String deferredURLUsing(String url, String domain, int userId, int contextId);

    /**
     * Gets the basic defer URL.
     * <p>
     * If a single domain is configured through <code>com.openexchange.http.deferrer.url</code> property (<i>deferrer.properties</i>), the
     * resulting basic URL looks like:
     * <p>
     * &lt;deferrer-url&gt; + <code>"ajax/defer"</code>
     * <p>
     * If no such property is set, return value is a relative one according to <code>"ajax/defer"</code>.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The basic defer URL
     */
    String getBasicDeferrerURL(int userId, int contextId);

    /**
     * Performs a check if passed URL seems to be deferred.
     *
     * @param url The URL to check if deferred
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if deferred; otherwise <code>false</code>
     */
    boolean seemsDeferred(String url, int userId, int contextId);

    /**
     * Signals if a deferred URL is available.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if a deferred URL is available; otherwise <code>false</code>
     */
    boolean isDeferrerURLAvailable(int userId, int contextId);

}
