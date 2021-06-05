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

package com.openexchange.mail.autoconfig.tools;


/**
 * {@link ConnectMode} - Specifies how to connect to the remote end-point.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum ConnectMode {

    /**
     * Initiate a plain connection; use STARTTLS if supported, but do not require it.
     */
    DONT_CARE,
    /**
     * Initiate a connection right from the start using an SSL socket
     */
    SSL,
    /**
     * STARTTLS is required; start off with a plain socket, but switch to a TLS-protected one through STARTTLS hand-shake
     */
    STARTTLS,
    ;

    /**
     * Determines the appropriate connect mode for given arguments.
     *
     * @param secure Whether an SSL socket is supposed to be established
     * @param requireTls Whether STARTTLS is required (in case no SSL Socket is created)
     * @return The connect mode
     */
    public static ConnectMode connectModeFor(boolean secure, boolean requireTls) {
        return secure ? ConnectMode.SSL : (requireTls ? ConnectMode.STARTTLS : ConnectMode.DONT_CARE);
    }

}
