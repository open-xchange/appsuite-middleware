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

package com.openexchange.proxy.authenticator;

import java.net.PasswordAuthentication;

/**
 * {@link PasswordAuthenticationProvider}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public interface PasswordAuthenticationProvider {

    /**
     * Retrieves the {@link PasswordAuthentication} in case the host and port matches.
     *
     * @param requestingHost The requesting host
     * @param requestingPort The requesting port
     * @return The {@link PasswordAuthentication} or <code>null</code>
     */
    PasswordAuthentication getPasswordAuthentication(String requestingHost, int requestingPort);

    /**
     * Gets the protocol for this provider
     *
     * @return The protocol
     */
    String getProtocol();
}
