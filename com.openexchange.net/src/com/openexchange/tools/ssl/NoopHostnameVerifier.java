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

package com.openexchange.tools.ssl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * {@link NoopHostnameVerifier} - A NOOP <tt>HostnameVerifier</tt>, which turns host-name verification off.
 * <p>
 * This implementation is a no-op, and never throws an <tt>SSLException</tt>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class NoopHostnameVerifier implements HostnameVerifier {

    private static final NoopHostnameVerifier INSTANCE = new NoopHostnameVerifier();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static NoopHostnameVerifier getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link NoopHostnameVerifier}.
     */
    private NoopHostnameVerifier() {
        super();
    }

    /**
     * Verifies that the host name is an acceptable match with the server's authentication scheme.
     *
     * @param hostname The host name
     * @param sslSession The SSL session used on the connection to host
     * @return <code>true</code> if the host name is acceptable; otherwise <code>false</code>
     */
    @Override
    public boolean verify(String hostname, SSLSession sslSession) {
        return true;
    }

    @Override
    public final String toString() {
        return "NOOP";
    }

}
