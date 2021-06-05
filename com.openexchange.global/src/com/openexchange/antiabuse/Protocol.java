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

package com.openexchange.antiabuse;

/**
 * {@link Protocol} - The protocol that was used to authenticate against the authority.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class Protocol {

    /**
     * The HTTP protocol.
     */
    public static final Protocol HTTP = new Protocol("http", false);

    /**
     * The HTTPS protocol.
     */
    public static final Protocol HTTPS = new Protocol("http", true);

    /**
     * Creates a new protocol instance.
     *
     * @param name The protocol name
     * @param secure Whether protocol uses a secure transport layer (TLS)
     * @return The protocol instance
     */
    public static Protocol newInstance(String name, boolean secure) {
        return new Protocol(name, secure);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final String name;
    private final boolean secure;

    private Protocol(String name, boolean secure) {
        this.name = name;
        this.secure = secure;
    }

    /**
     * Gets the protocol base name; e.g. <code>"http"</code>
     *
     * @return The protocol base name
     */
    public String getName() {
        return name;
    }

    /**
     * Checks if this protocol uses a secure transport layer (TLS)
     *
     * @return <code>true</code> for secure transport layer; otherwise <code>false</code>
     */
    public boolean isSecure() {
        return secure;
    }
}
