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

package com.openexchange.mail.api;


/**
 * {@link UrlInfo} - The URL information of an end-point for mail access or mail transport.
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public final class UrlInfo {

    private final String serverURL;
    private final boolean requireStartTls;

    /**
     * Initializes a new {@link UrlInfo}.
     */
    public UrlInfo(String serverURL, boolean requireStartTls) {
        super();
        this.serverURL = serverURL;
        this.requireStartTls = requireStartTls;
    }

    /**
     * Gets the server URL; e.g. <code>"imaps://imap.hosting.org:993"</code>
     * <p>
     * The URL is supposed to provide:
     * <ul>
     * <li>The protocol/scheme.<br>
     * This includes the plain and the secure protocol identifier; e.g. <code>"imap"</code> or <code>"imap<b>s</b>"</code><br>
     * If the protocol/scheme is absent, then the one configured through property <code>"com.openexchange.mail.defaultMailProvider"</code>
     * is used.
     * </li>
     * <li>The host name or IP address of the end-point</li>
     * <li>(Optional) The port number of the end-point</li>
     * </ul>
     *
     * @return The server URL
     */
    public String getServerURL() {
        return serverURL;
    }

    /**
     * Whether STARTTLS is required (in case no SSL socket is supposed to be established).
     * <p>
     * If the server's URL signals using an SSL socket (that is URL's protocol/scheme identifies a secure protocol such as
     * <code>"imap<b>s</b>"</code> or <code>"smtp<b>s</b>"</code>), then this flag has no meaning. But if the server's URL
     * lets us create a plain socket connection, then this flag determines whether the STARTTLS hand-shake is mandatory being
     * performed over that plain connection.
     *
     * @return <code>true</code> if STARTTLS is required; otherwise <code>false</code> to allow establishing a plain connection
     */
    public boolean isRequireStartTls() {
        return requireStartTls;
    }

}
