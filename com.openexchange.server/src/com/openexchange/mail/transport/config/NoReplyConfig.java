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

package com.openexchange.mail.transport.config;

import javax.mail.internet.InternetAddress;



/**
 * {@link NoReplyConfig}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public interface NoReplyConfig {

    /**
     * The name of the special MIME header advertising a possible personal for the no-reply address.
     */
    public static final String HEADER_NO_REPLY_PERSONAL = com.openexchange.mail.mime.MessageHeaders.HDR_X_OX_NO_REPLY_PERSONAL;

    /**
     * The secure mode enumeration.
     */
    public static enum SecureMode {

        /**
         * The plain connection mode
         */
        PLAIN("plain"),
        /**
         * The SSL secure mode
         */
        SSL("SSL"),
        /**
         * The TLS secure mode
         */
        TLS("TLS"), ;

        private final String identifier;

        private SecureMode(String identifier) {
            this.identifier = identifier;
        }

        /**
         * Gets the identifier
         *
         * @return The identifier
         */
        public String getIdentifier() {
            return identifier;
        }

        /**
         * Gets the secure mode for given identifier.
         *
         * @param identifier The identifier
         * @return The associated secure mode or <code>null</code>
         */
        public static SecureMode secureModeFor(String identifier) {
            if (null == identifier) {
                return null;
            }

            for (SecureMode sm : SecureMode.values()) {
                if (identifier.equalsIgnoreCase(sm.identifier)) {
                    return sm;
                }
            }
            return null;
        }
    }

    /**
     * Gets the address
     *
     * @return The address
     */
    InternetAddress getAddress();

    /**
     * Gets the login
     *
     * @return The login
     */
    String getLogin();

    /**
     * Gets the password
     *
     * @return The password
     */
    String getPassword();

    /**
     * Gets the server
     *
     * @return The server
     */
    String getServer();

    /**
     * Gets the port
     *
     * @return The port
     */
    int getPort();

    /**
     * Gets the secure mode
     *
     * @return The secure mode
     */
    SecureMode getSecureMode();

}
