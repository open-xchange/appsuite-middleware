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

package com.openexchange.rest.client.httpclient.internal;

import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.protocol.HttpContext;

/**
 * {@link KeepAliveStrategy}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a> - Moved and simplified with 7.10.4
 * @since v7.6.1
 */
public class KeepAliveStrategy extends DefaultConnectionKeepAliveStrategy {

    private final long keepAliveSeconds;

    /**
     * 
     * Initializes a new {@link KeepAliveStrategy}.
     *
     * @param keepAliveSeconds The time a connection can remain idle in <code>seconds</code>
     */
    public KeepAliveStrategy(int keepAliveSeconds) {
        super();
        this.keepAliveSeconds = keepAliveSeconds * 1000L;
    }

    @Override
    public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
        // Keep-alive for the shorter of 20 seconds or what the server specifies.
        long duration = super.getKeepAliveDuration(response, context);
        return -1 == duration ? keepAliveSeconds : Math.min(keepAliveSeconds, duration);
    }
}
