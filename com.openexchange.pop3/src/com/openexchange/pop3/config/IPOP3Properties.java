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

package com.openexchange.pop3.config;

import com.openexchange.mail.api.IMailProperties;

/**
 * {@link IPOP3Properties} - Properties for POP3.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface IPOP3Properties extends IMailProperties {

    /**
     * Gets the POP3 authentication encoding.
     *
     * @return The POP3 authentication encoding
     */
    public String getPOP3AuthEnc();

    /**
     * Gets the POP3 connection idle time in milliseconds.
     *
     * @return The POP3 connection idle time in milliseconds
     */
    public int getPOP3ConnectionIdleTime();

    /**
     * Gets the POP3 connection timeout in milliseconds.
     *
     * @return The POP3 connection timeout in milliseconds
     */
    public int getPOP3ConnectionTimeout();

    /**
     * Gets the POP3 temporary down in milliseconds.
     *
     * @return The POP3 temporary down in milliseconds
     */
    public int getPOP3TemporaryDown();

    /**
     * Gets the POP3 timeout in milliseconds.
     *
     * @return The POP3 timeout in milliseconds
     */
    public int getPOP3Timeout();

    /**
     * Gets the number of messages which are allowed to be fetched at once.
     *
     * @return The block size
     */
    public int getPOP3BlockSize();

    /**
     * Gets supported SSL protocols
     * @return Supported SSL protocols
     */
    public String getSSLProtocols();

    /**
     * Gets the SSL cipher suites that will be enabled for SSL connections. The property value is a whitespace separated list of tokens
     * acceptable to the <code>javax.net.ssl.SSLSocket.setEnabledProtocols</code> method.
     *
     * @return The SSL cipher suites
     */
    public String getSSLCipherSuites();

}
