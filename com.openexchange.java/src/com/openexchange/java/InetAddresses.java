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

package com.openexchange.java;

import java.net.InetAddress;

/**
 * {@link InetAddresses} - Static utility methods for {@link InetAddress} instances.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class InetAddresses {

    /**
     * Initializes a new {@link InetAddresses}.
     */
    private InetAddresses() {
        super();
    }

    /**
     * Checks if specified address denotes a network internal end-point.
     * <p>
     * The address is considered to be internal if all following checks are passed:
     * <ul>
     * <li>{@link InetAddress#isAnyLocalAddress() Any local address}</li>
     * <li>{@link InetAddress#isSiteLocalAddress() Site local address}</li>
     * <li>{@link InetAddress#isLoopbackAddress() Loop-back address}</li>
     * <li>{@link InetAddress#isLinkLocalAddress() Link local address}</li>
     * </ul>
     *
     * @param inetAddress The address to check
     * @return <code>true</code> if specified address denotes a network internal end-point; otherwise <code>false</code>
     */
    public static boolean isInternalAddress(InetAddress inetAddress) {
        if (null == inetAddress) {
            return false;
        }

        return (inetAddress.isAnyLocalAddress() || inetAddress.isSiteLocalAddress() || inetAddress.isLoopbackAddress() || inetAddress.isLinkLocalAddress());
    }

}
